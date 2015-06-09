package com.cvte.william.bt_spp_tracker;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class BT_SPP extends ActionBarActivity {
    private final String TAG = "cvte_zxl";
    private final String CVTE_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private final String CVTE_COMMAND_PREFIX = "at+[1588]cft:";

    private final int CVTE_BT_COM_RECEIVE_DATA = 1;
    private final int CVTE_BT_COM_SEND_DATA = 2;
    private final int CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER = 3;
    private final int CVTE_INPUT_REMOTE_FILE_PATH = 4;

    private final int CVTE_ACTIVITY_RESULT_CODE_SELECT_FILE = 1001;

    //UI
    private EditText mEditTextBtMac;
    private EditText mEditTextOutputData;
    private Button mButtonConnectSpp;
    private Button mButtonSendData;
    private TextView mTextViewData;
    private TextView mTextViewDataFliter;
    private ScrollView mScrollViewData = null;
    private ScrollView mScrollViewDataFliter = null;
    private Spinner mSpinnerCommand = null;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothSocket mBluetoothSocket = null;
    private OutputStream mOutputStream = null;
    private InputStream mInputStream = null;
    private ReadThread mReadThread = null;

    private String mRemoteFilePath = null;
    private Boolean mResponse = false;
    private String mSendData = null;
    private Integer mFileSize = 0;
    private byte[] mReceiveBuffer = null;

    private Uri mPushFileUri = null;

    //COMMAND {
    private static final String CVTE_COMMAND_GET_IMEI = "Get IMEI";
    private static final String CVTE_COMMAND_GET_IMEI_DATA = "gmi:";
    private static final String CVTE_COMMAND_GET_IMSI = "Get IMSI";
    private static final String CVTE_COMMAND_GET_IMSI_DATA = "gii:";
    private static final String CVTE_COMMAND_GET_BATTERY = "Get Battery Status";
    private static final String CVTE_COMMAND_GET_BATTERY_DATA = "gbi:";
    private static final String CVTE_COMMAND_GET_GSM = "Get GSM Status";
    private static final String CVTE_COMMAND_GET_GSM_DATA = "ggi:";
    private static final String CVTE_COMMAND_GET_WIFI_MAC = "Get WIFI MAC";
    private static final String CVTE_COMMAND_GET_WIFI_MAC_DATA = "gwm:";
    private static final String CVTE_COMMAND_GET_BT_MAC = "Get BT MAC";
    private static final String CVTE_COMMAND_GET_BT_MAC_DATA = "gbm:";
    private static final String CVTE_COMMAND_REBOOT = "Reboot";
    private static final String CVTE_COMMAND_REBOOT_DATA = "rb:";
    private static final String CVTE_COMMAND_POWER_OFF = "Power Off";
    private static final String CVTE_COMMAND_POWER_OFF_DATA = "pof:";

    private static final String CVTE_COMMAND_PULL_FILE = "Pull File";
    private static final String CVTE_COMMAND_PUSH_FILE = "Push File";
    private static final String CVTE_COMMAND_OPEN_FILE_DATA = "fo:";
    private static final String CVTE_COMMAND_SET_FILE_PATH_DATA = "fsp:";
    private static final String CVTE_COMMAND_CLOSE_FILE_DATA = "fc:";
    private static final String CVTE_COMMAND_FILE_READ_DATA = "fr:";
    private static final String CVTE_COMMAND_FILE_WRITE_DATA = "fw:";

    private final int CVTE_FILE_MODE_READ = 1;
    private final int CVTE_FILE_MODE_CREATE = 4;

    private final int READ_MAX_SIZE = 38;
    private final int WRITE_MAX_SIZE = 54;

    private static final String[] mCommandString = {
            CVTE_COMMAND_GET_IMEI,
            CVTE_COMMAND_GET_IMSI,
            CVTE_COMMAND_GET_BATTERY,
            CVTE_COMMAND_GET_GSM,
            CVTE_COMMAND_GET_WIFI_MAC,
            CVTE_COMMAND_GET_BT_MAC,
            CVTE_COMMAND_REBOOT,
            CVTE_COMMAND_POWER_OFF,
            CVTE_COMMAND_PULL_FILE,
            CVTE_COMMAND_PUSH_FILE,
    };

    private void parseSendCommand(String command) {
        switch (command) {
            case CVTE_COMMAND_GET_IMEI:
                mEditTextOutputData.setText(CVTE_COMMAND_GET_IMEI_DATA);
                break;
            case CVTE_COMMAND_GET_IMSI:
                mEditTextOutputData.setText(CVTE_COMMAND_GET_IMSI_DATA);
                break;
            case CVTE_COMMAND_GET_BATTERY:
                mEditTextOutputData.setText(CVTE_COMMAND_GET_BATTERY_DATA);
                break;
            case CVTE_COMMAND_GET_GSM:
                mEditTextOutputData.setText(CVTE_COMMAND_GET_GSM_DATA);
                break;
            case CVTE_COMMAND_GET_WIFI_MAC:
                mEditTextOutputData.setText(CVTE_COMMAND_GET_WIFI_MAC_DATA);
                break;
            case CVTE_COMMAND_GET_BT_MAC:
                mEditTextOutputData.setText(CVTE_COMMAND_GET_BT_MAC_DATA);
                break;
            case CVTE_COMMAND_REBOOT:
                mEditTextOutputData.setText(CVTE_COMMAND_REBOOT_DATA);
                break;
            case CVTE_COMMAND_POWER_OFF:
                mEditTextOutputData.setText(CVTE_COMMAND_POWER_OFF_DATA);
                break;
            case CVTE_COMMAND_PULL_FILE:

                final EditText pathEditText = new EditText(this);
                new AlertDialog.Builder(this).setTitle("请输入目标文件路径：")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(pathEditText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mRemoteFilePath = pathEditText.getText().toString();
                                new PullFileThread().start();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                break;
            case CVTE_COMMAND_PUSH_FILE:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent, CVTE_ACTIVITY_RESULT_CODE_SELECT_FILE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CVTE_ACTIVITY_RESULT_CODE_SELECT_FILE) {
            final String msgString;
            Uri fileUri = data.getData();
            Log.v(TAG, "fileUri:" + fileUri);
            mPushFileUri = fileUri;

            msgString = "Push File: " + fileUri.getPath();

            final EditText pathEditText = new EditText(this);
            new AlertDialog.Builder(this).setTitle("请输入目标文件路径：")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(pathEditText)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mRemoteFilePath = pathEditText.getText().toString();

                            Message msg = mHandler.obtainMessage();
                            msg.what = CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER;
                            msg.obj = mTextViewDataFliter.getText().toString() + msgString
                                    + pathEditText.getText().toString() + "\r\n";
                            mHandler.sendMessage(msg);

                            new PushFileThread().start();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }
    }

    private class PushFileThread extends Thread {
        private int fileWrite(byte[] buffer) {
            int writeSize, writeLen, totalWrite = 0;
            String command;
            int len = buffer.length;

            while (len > 0) {
                writeSize = (len > WRITE_MAX_SIZE ? WRITE_MAX_SIZE : len);
                command = CVTE_COMMAND_FILE_WRITE_DATA + String.format("%02x", writeSize);
                for (int loop = 0; loop < writeSize; loop++) {
                    command = command + String.format("%02x", buffer[totalWrite + loop]);
                }
                mSendData = command;

                Message msg = mHandler.obtainMessage();
                msg.obj = CVTE_COMMAND_FILE_WRITE_DATA;
                msg.what = CVTE_BT_COM_SEND_DATA;
                mHandler.sendMessage(msg);

                if (waitResponse() == false) {
                    mSendData = null;
                    return -1;
                }

                writeLen = getInt8(mReceiveBuffer);
                if (writeLen < 0) {
                    return -3;
                }

                totalWrite += writeLen;
                len -= writeLen;
            }
            return totalWrite;
        }

        private boolean waitResponse() {
            int count = 0;
            mResponse = false;
            while (count < 3) {
                try {
                    count++;
                    sleep(500);
                    if (mResponse == true) {
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        public void run() {
            Log.v(TAG, "PushFileThread");
            Message msg = mHandler.obtainMessage();
            String command;

            //close file
            command = CVTE_COMMAND_CLOSE_FILE_DATA;
            msg.obj = command;
            msg.what = CVTE_BT_COM_SEND_DATA;
            mHandler.sendMessage(msg);
            if (waitResponse() == false) {
                mSendData = null;
                return;
            }

            //set file path
            command = CVTE_COMMAND_SET_FILE_PATH_DATA + "S";
            for (int i = 0; i < mRemoteFilePath.length(); i++) {
                command = command + String.format("%04x", mRemoteFilePath.toCharArray()[i] & 0xFFFF);
            }

            mSendData = command;
            msg = mHandler.obtainMessage();
            msg.obj = CVTE_COMMAND_SET_FILE_PATH_DATA;
            msg.what = CVTE_BT_COM_SEND_DATA;
            mHandler.sendMessage(msg);

            if (waitResponse() == false) {
                mSendData = null;
                return;
            }
            mSendData = null;

            //open file and get file size
            command = String.format(CVTE_COMMAND_OPEN_FILE_DATA + "%08x%08x", CVTE_FILE_MODE_CREATE, 1);
            mSendData = command;

            msg = mHandler.obtainMessage();
            msg.obj = CVTE_COMMAND_OPEN_FILE_DATA;
            msg.what = CVTE_BT_COM_SEND_DATA;
            mHandler.sendMessage(msg);

            if (waitResponse() == false) {
                mSendData = null;
                return;
            }

            File localFile = new File(mPushFileUri.getPath());
            try {
                InputStream in = new FileInputStream(localFile);
                byte buffer[] = new byte[(int) localFile.length()];
                in.read(buffer);
                in.close();
                fileWrite(buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //close file
            command = CVTE_COMMAND_CLOSE_FILE_DATA;
            msg = mHandler.obtainMessage();
            msg.obj = command;
            msg.what = CVTE_BT_COM_SEND_DATA;
            mHandler.sendMessage(msg);
            if (waitResponse() == false) {
                mSendData = null;
                return;
            }

            msg = mHandler.obtainMessage();
            msg.what = CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER;
            msg.obj = mTextViewDataFliter.getText().toString() + "Push Finished." + "\r\n";
            mHandler.sendMessage(msg);
        }
    }

    private class PullFileThread extends Thread {
        private int fileRead(byte[] buf, int len) {
            int readSize, readLen, totalRead = 0, value;
            String command;

            readSize = (len > READ_MAX_SIZE ? READ_MAX_SIZE : len);
            command = CVTE_COMMAND_FILE_READ_DATA + String.format("%08x", readSize);
            while (readSize > 0) {
                mSendData = command;

                Message msg = mHandler.obtainMessage();
                msg.obj = CVTE_COMMAND_FILE_READ_DATA;
                msg.what = CVTE_BT_COM_SEND_DATA;
                mHandler.sendMessage(msg);

                if (waitResponse() == false) {
                    mSendData = null;
                    return -1;
                }
                if (mReceiveBuffer == null) {
                    return -2;
                }
                readLen = getInt8(mReceiveBuffer);
                if (readLen < 0) {
                    return -3;
                }
                if (readLen == 0) {
                    break;
                }

                for (int i = 0; i < readLen; i++) {
                    byte[] data = new byte[2];
                    System.arraycopy(mReceiveBuffer, 2 + 2 * i, data, 0, 2);
                    value = getInt8(data);
                    buf[totalRead + i] = Integer.valueOf(value).byteValue();
                }
                totalRead += readLen;
                len -= readLen;
                readSize -= readLen;
            }
            return totalRead;
        }

        private boolean waitResponse() {
            int count = 0;
            mResponse = false;
            while (count < 3) {
                try {
                    count++;
                    sleep(500);
                    if (mResponse == true) {
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        public void run() {
            Message msg = mHandler.obtainMessage();
            String command;
            String print;

            //close file
            command = CVTE_COMMAND_CLOSE_FILE_DATA;
            msg.obj = command;
            msg.what = CVTE_BT_COM_SEND_DATA;
            mHandler.sendMessage(msg);

            if (waitResponse() == false) {
                mSendData = null;
                return;
            }

            //set file path
            command = CVTE_COMMAND_SET_FILE_PATH_DATA + "S";
            for (int i = 0; i < mRemoteFilePath.length(); i++) {
                command = command + String.format("%04x", mRemoteFilePath.toCharArray()[i] & 0xFFFF);
            }

            msg = mHandler.obtainMessage();
            mSendData = command;
            msg.obj = CVTE_COMMAND_SET_FILE_PATH_DATA;
            msg.what = CVTE_BT_COM_SEND_DATA;
            mHandler.sendMessage(msg);

            if (waitResponse() == false) {
                mSendData = null;
                return;
            }
            mSendData = null;

            //open file and get file size
            command = String.format(CVTE_COMMAND_OPEN_FILE_DATA + "%08x%08x", CVTE_FILE_MODE_READ, 1);
            msg = mHandler.obtainMessage();
            msg.obj = CVTE_COMMAND_OPEN_FILE_DATA;
            msg.what = CVTE_BT_COM_SEND_DATA;
            mHandler.sendMessage(msg);

            if (waitResponse() == false) {
                mSendData = null;
                return;
            }

            int totalRead = 0;
            int readLen = 0;

            print = mTextViewDataFliter.getText().toString() + "## File: " + mRemoteFilePath + " ##" + "\r\n";

            File file = null;
            OutputStream os = null;
            SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");
            String date = sDateFormat.format(new java.util.Date());

            String file_name = mRemoteFilePath + "_" + date.toString();
            file_name = file_name.replace(":\\", "_");
            file_name = file_name.replace('\\', '_');

            try {
                file = new File("/sdcard/" + file_name);
                if (!file.exists()) {
                    file.createNewFile();
                }

                os = new FileOutputStream(file);
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (totalRead < mFileSize) {
                byte[] buf = new byte[READ_MAX_SIZE];

                readLen = fileRead(buf, mFileSize - totalRead);
                if (readLen < 0) {
                    break;
                }
                try {
                    os.write((new String(buf).getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                print = print + new String(buf);

                totalRead += readLen;
            }
            print = print + "## END ##" + "\r\n";

            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            msg = mHandler.obtainMessage();
            msg.what = CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER;
            msg.obj = print;
            mHandler.sendMessage(msg);
        }
    }

    private void parseReceiveData(byte[] data) {
        String data_str = new String(data);
        if (data_str.equals(":cmatruxze:0\t")) {
            mResponse = true;
        } else if (data_str.equals("Ok")) {
            mResponse = true;
        } else if (data_str.startsWith(":cmatruxze:0")) {
            String temp = data_str.replace(":cmatruxze:0\t", " ");
            data_str = temp;
            temp = data_str.replace(" ", "");
            data_str = temp;
            parseReceiveData(data_str.getBytes());
        } else {
            byte[] data_int = new byte[8];
            Message msg = mHandler.obtainMessage();
            TextView selected_view = (TextView) mSpinnerCommand.getSelectedView();
            switch (selected_view.getText().toString()) {
                case CVTE_COMMAND_GET_IMEI:
                    byte[] imei = new byte[data.length - 2];
                    System.arraycopy(data, 2, imei, 0, data.length - 2);
                    msg.obj = mTextViewDataFliter.getText().toString() + "IMEI: " + new String(imei) + "\r\n";
                    msg.what = CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER;
                    mHandler.sendMessage(msg);
                    break;

                case CVTE_COMMAND_GET_IMSI:
                    byte[] imsi = new byte[data.length - 2];
                    System.arraycopy(data, 2, imsi, 0, data.length - 2);
                    msg.obj = mTextViewDataFliter.getText().toString() + "IMSI: " + new String(imsi) + "\r\n";
                    msg.what = CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER;
                    mHandler.sendMessage(msg);
                    break;

                case CVTE_COMMAND_GET_BATTERY:
                    System.arraycopy(data, 2, data_int, 0, 8);
                    int battery_level = getInt32(data_int);

                    System.arraycopy(data, 10, data_int, 0, 8);
                    int battery_voltage = getInt32(data_int);

                    msg.obj = mTextViewDataFliter.getText().toString() + "Battery Level: " + String.valueOf(battery_level) + " __ Battery Voltage: " + String.valueOf(battery_voltage) + "mV\r\n";
                    msg.what = CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER;
                    mHandler.sendMessage(msg);
                    break;

                case CVTE_COMMAND_GET_GSM:
                    System.arraycopy(data, 2, data_int, 0, 8);
                    int gsm_dbm = getInt32(data_int);
                    //gsm_dbm = ~gsm_dbm - 1;
                    String text = mTextViewDataFliter.getText().toString()
                            + "GSM rssi: " + String.valueOf(gsm_dbm)
                            + "dBm\r\nGSM Operator: ";

                    String tmp = text;

                    switch (data[11]) {
                        case '0':
                            text = tmp + "NO SIM";
                            break;
                        case '1':
                            text = tmp + "Unknown";
                            break;
                        case '2':
                            text = tmp + "CMCC";
                            break;
                        case '3':
                            text = tmp + "UNICOM";
                            break;
                        case '4':
                            text = tmp + "CNC";
                            break;
                        case '5':
                            text = tmp + "CNTELCOM";
                            break;
                        case '6':
                            text = tmp + "ALL";
                            break;
                    }

                    tmp = text + "\r\nSIM Status: ";

                    switch (data[13]) {
                        case 'f':
                            text = tmp + "获取失败";
                            break;
                        case '0':
                            text = tmp + "无SIM卡或SIM卡损坏";
                            break;
                        case '1':
                            text = tmp + "SIM卡工作正常";
                            break;
                    }

                    msg.obj = text + "\r\n";
                    msg.what = CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER;
                    mHandler.sendMessage(msg);
                    break;
                case CVTE_COMMAND_GET_WIFI_MAC:
                    byte[] wifi_mac = new byte[data.length - 2];
                    System.arraycopy(data, 2, wifi_mac, 0, 12); //No checksum

                    msg.obj = mTextViewDataFliter.getText().toString() + "WIFI MAC: " + new String(wifi_mac) + "\r\n";
                    msg.what = CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER;
                    mHandler.sendMessage(msg);
                    break;

                case CVTE_COMMAND_GET_BT_MAC:
                    byte[] bt_mac = new byte[data.length - 2];
                    System.arraycopy(data, 2, bt_mac, 0, 12); //No checksum

                    msg.obj = mTextViewDataFliter.getText().toString() + "BT MAC: " + new String(bt_mac) + "\r\n";
                    msg.what = CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER;
                    mHandler.sendMessage(msg);
                    break;

                case CVTE_COMMAND_PULL_FILE:
                    switch (mEditTextOutputData.getText().toString()) {
                        case CVTE_COMMAND_OPEN_FILE_DATA:
                            byte[] file_size = new byte[data.length - 2];
                            System.arraycopy(data, 2, file_size, 0, data.length - 2);
                            String[] stringArray = new String(file_size).split(",");

                            try {
                                mFileSize = Integer.parseInt(stringArray[0]);
                            } catch (Exception e) {
                                e.printStackTrace();
                                break;
                            }

                            msg.obj = mTextViewDataFliter.getText().toString() + "File: " + mRemoteFilePath + "\r\n" + "File Size: " + mFileSize.toString() + " Bytes\r\n";
                            msg.what = CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER;
                            mHandler.sendMessage(msg);
                            mResponse = true;
                            break;

                        case CVTE_COMMAND_FILE_READ_DATA:
                            mResponse = true;
                            if (new String(data).equals("Fail:Read fail.")) {
                                mReceiveBuffer = null;
                            } else {
                                mReceiveBuffer = new byte[data.length - 2 - 1];
                                System.arraycopy(data, 2, mReceiveBuffer, 0, data.length - 2 - 1);
                            }
                            break;
                    }
                    break;

                case CVTE_COMMAND_PUSH_FILE:
                    switch (mEditTextOutputData.getText().toString()) {
                        case CVTE_COMMAND_OPEN_FILE_DATA:
                            byte[] file_size = new byte[data.length - 2];
                            System.arraycopy(data, 2, file_size, 0, data.length - 2);
                            String[] stringArray = new String(file_size).split(",");

                            try {
                                mFileSize = Integer.parseInt(stringArray[0]);
                            } catch (Exception e) {
                                e.printStackTrace();
                                break;
                            }

                            msg.obj = mTextViewDataFliter.getText().toString() + "File: " + mRemoteFilePath + "\r\n" + "File Size: " + mFileSize.toString() + " Bytes\r\n";
                            msg.what = CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER;
                            mHandler.sendMessage(msg);
                            mResponse = true;
                            break;

                        case CVTE_COMMAND_FILE_WRITE_DATA:
                            mResponse = true;
                            if (new String(data).equals("Fail:Write fail.")) {
                                mReceiveBuffer = null;
                            } else {
                                mReceiveBuffer = new byte[data.length - 2 - 1];
                                System.arraycopy(data, 2, mReceiveBuffer, 0, data.length - 2 - 1);
                            }
                            break;
                    }
                    break;
            }
            mScrollViewDataFliter.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
    //COMMAND }

    private int getInt32(byte[] data) {
        int value = 0;
        for (int i = 0; i < 8; i++) {
            value <<= 4;
            if ((data[i] >= '0') && (data[i] <= '9')) {
                value += data[i] - '0';
            } else if ((data[i] >= 'a') && (data[i] <= 'f')) {
                value += data[i] - 'a' + 10;
            } else if ((data[i] >= 'A') && (data[i] <= 'F')) {
                value += data[i] - 'A' + 10;
            } else {
                return 0;
            }
        }
        return value;
    }

    private int getInt8(byte[] data) {
        int value = 0;
        for (int i = 0; i < 2; i++) {
            value <<= 4;
            if ((data[i] >= '0') && (data[i] <= '9')) {
                value += data[i] - '0';
            } else if ((data[i] >= 'a') && (data[i] <= 'f')) {
                value += data[i] - 'a' + 10;
            } else if ((data[i] >= 'A') && (data[i] <= 'F')) {
                value += data[i] - 'A' + 10;
            } else {
                return -1;
            }
        }
        return value;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_spp);

        mEditTextBtMac = (EditText) findViewById(R.id.editText_BT_MAC);
        mEditTextOutputData = (EditText) findViewById(R.id.editText_output_data);
        mButtonConnectSpp = (Button) findViewById(R.id.button_connect_spp);
        mButtonSendData = (Button) findViewById(R.id.button_send_data);
        mTextViewData = (TextView) findViewById(R.id.textView_data);
        mTextViewDataFliter = (TextView) findViewById(R.id.textView_data_fliter);
        mScrollViewData = (ScrollView) findViewById(R.id.scrollView_data);
        mScrollViewDataFliter = (ScrollView) findViewById(R.id.scrollView_data_fliter);
        mSpinnerCommand = (Spinner) findViewById(R.id.spinner_command);

        mEditTextOutputData.setEnabled(false);
        mButtonSendData.setEnabled(false);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mCommandString);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCommand.setAdapter(adapter);
        mSpinnerCommand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                parseSendCommand(textView.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mButtonConnectSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtonConnectSpp.setText(R.string.button_connecting_spp);
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter.isEnabled()) {
                    try {
                        if (mBluetoothSocket != null) {
                            mBluetoothSocket.close();
                            mBluetoothSocket = null;
                            mOutputStream = null;
                            mInputStream = null;

                            //UI update {
                            mButtonConnectSpp.setText(R.string.button_connect_spp);
                            //mEditTextOutputData.setEnabled(false);
                            mButtonSendData.setEnabled(false);
                            mEditTextBtMac.setEnabled(true);
                            mTextViewData.setText(mTextViewData.getText().toString() + "Disconnect!!!\r\n");
                            mScrollViewData.fullScroll(ScrollView.FOCUS_DOWN);
                            //UI update }

                            if (mReadThread != null) {
                            }
                            return;
                        }

                        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mEditTextBtMac.getText().toString().trim());
                        Log.v(TAG, "connect BT MAC:" + mEditTextBtMac.getText().toString().trim());

                        mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(CVTE_UUID));
                        if (mBluetoothSocket != null) {
                            mBluetoothSocket.connect();
                            mBluetoothSocket.close();

                            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(CVTE_UUID));
                            if (mBluetoothSocket != null) {
                                mBluetoothSocket.connect();

                                //UI update {
                                mButtonConnectSpp.setText(R.string.button_disconnect_spp);
                                mTextViewData.setText(mTextViewData.getText().toString() + "New connection!!!\r\n");
                                mEditTextBtMac.setEnabled(false);
                                //mEditTextOutputData.setEnabled(true);
                                //mEditTextOutputData.requestFocus();
                                mButtonSendData.setEnabled(true);
                                mScrollViewData.fullScroll(ScrollView.FOCUS_DOWN);
                                //UI update }

                                mOutputStream = mBluetoothSocket.getOutputStream();
                                mInputStream = mBluetoothSocket.getInputStream();

                                mReadThread = new ReadThread();
                                mReadThread.start();
                            }
                        }
                    } catch (Exception e) {
                        mButtonConnectSpp.setText(R.string.button_connect_spp);
                        e.printStackTrace();
                        mBluetoothSocket = null;
                        mTextViewData.setText(mTextViewData.getText().toString() + "Connect Error!!!\r\n");
                    }
                } else {
                    mBluetoothAdapter.enable();
                    Toast.makeText(BT_SPP.this, "Try to enable BT, Please wait for a moment.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mButtonSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOutputStream != null) {
                    try {
                        String text = mTextViewData.getText().toString();
                        String command = CVTE_COMMAND_PREFIX + mEditTextOutputData.getText().toString();

                        if (mSendData != null) {
                            command = CVTE_COMMAND_PREFIX + mSendData;
                            mSendData = null;
                        }

                        mOutputStream.write(command.getBytes());
                        mTextViewData.setText(text + "SEND -- " + command + "\r\n");

                        mOutputStream.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes = 0;
            if (mInputStream == null) {
                return;
            }
            while (true) {
                try {
                    bytes = mInputStream.read(buffer);
                    if (bytes > 0) {
                        final byte[] tmp_buffer = new byte[bytes];
                        System.arraycopy(buffer, 0, tmp_buffer, 0, bytes);
                        Message msg = new Message();
                        msg.obj = tmp_buffer;
                        msg.what = CVTE_BT_COM_RECEIVE_DATA;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CVTE_BT_COM_RECEIVE_DATA:
                    String text = mTextViewData.getText().toString();
                    String data_str = new String((byte[]) msg.obj);
                    mTextViewData.setText(text + "RECV -- " + data_str + "\r\n");
                    mScrollViewData.fullScroll(ScrollView.FOCUS_DOWN);

                    parseReceiveData((byte[]) msg.obj);
                    break;
                case CVTE_BT_COM_SEND_DATA:
                    Log.e("Command:",(String) msg.obj);
                    mEditTextOutputData.setText((String) msg.obj);
                    mButtonSendData.callOnClick();
                    break;

                case CVTE_UPDATE_UI_TEXTVIEW_RECEIVE_DATA_FLITER:
                    mTextViewDataFliter.setText((String) msg.obj);
                    break;

                case CVTE_INPUT_REMOTE_FILE_PATH:
                    break;
            }
        }
    };

}
