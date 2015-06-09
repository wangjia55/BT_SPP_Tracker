package com.cvte.william.bt_spp_tracker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mediatek.wearable.WearableListener;
import com.mediatek.wearable.WearableManager;


public class MTK_Connect extends ActionBarActivity {

    private final String TAG = "cvte_zxl";

    private EditText mEditTextBtMac = null;
    private Button mButtonConnect = null;


    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;

    private static final int CVTE_UI_UPDATE_BUTTON_CONNECT = 1;
    private static final int CVTE_DEVICE_CONNECT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mtk__connect);

        mEditTextBtMac = (EditText)findViewById(R.id.editText_BT_MAC);
        mButtonConnect = (Button)findViewById(R.id.button_mtk_connect);

        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                    Toast.makeText(MTK_Connect.this, "Try to enable BT, Please wait for a moment.", Toast.LENGTH_SHORT).show();
                }
                if (mBluetoothAdapter.isEnabled()) {
                    try {
                        WearableManager.getInstance().registerWearableListener(mWearableListener);
                        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mEditTextBtMac.getText().toString().trim());

                        WearableManager mW = WearableManager.getInstance();
                        if (mW == null) {
                            Log.v(TAG, "WearableManager.getInstance() == null");
                            return;
                        }

                        if (mW.getWorkingMode() == WearableManager.MODE_DOGP) {
                            Log.v(TAG, "It is DOGP Mode");
                        } else if (mW.getWorkingMode() == WearableManager.MODE_SPP) {
                            Log.v(TAG, "It is SPP Mode");
                        }

                        //Log.v(TAG, "connect BT MAC:" + mEditTextBtMac.getText().toString().trim());
                        //WearableManager.getInstance().setRemoteDevice(mBluetoothDevice);
                        //WearableManager.getInstance().connect();
                        Log.v(TAG, "connect status:" + mW.getConnectState());
                        WearableManager.getInstance().scanDevice(true);

                    } catch (Exception e) {
                        mButtonConnect.setText(R.string.button_connect);
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MTK_Connect.this, "Try to enable BT, Please wait for a moment.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CVTE_UI_UPDATE_BUTTON_CONNECT:
                    switch ((int)msg.obj) {
                        case WearableManager.STATE_CONNECTED:
                            mButtonConnect.setText(R.string.button_connected);
                            break;
                        case WearableManager.STATE_CONNECTING:
                            mButtonConnect.setText(R.string.button_connecting);
                            break;
                        case WearableManager.STATE_CONNECT_LOST:
                            mButtonConnect.setText(R.string.button_connect);
                            break;
                        case WearableManager.STATE_CONNECT_FAIL:
                            mButtonConnect.setText(R.string.button_connect);
                            break;
                        default:
                            mButtonConnect.setText(R.string.button_connect);
                            break;
                    }
                    break;
                case CVTE_DEVICE_CONNECT:
                    WearableManager.getInstance().setRemoteDevice((BluetoothDevice)msg.obj);
                    WearableManager.getInstance().connect();
                    break;
            }
        }
    };

    private WearableListener mWearableListener = new WearableListener() {
        @Override
        public void onDeviceChange(BluetoothDevice device) {
        }

        @Override
        public void onConnectChange(int oldState, int newState) {
            Message msg = mHandler.obtainMessage();
            msg.what = CVTE_UI_UPDATE_BUTTON_CONNECT;
            msg.obj = newState;
            mHandler.sendMessage(msg);
            Log.v(TAG, "onConnectChange");
        }

        @Override
        public void onDeviceScan(BluetoothDevice device) {
            Log.v(TAG, "onDeviceScan:" + device.getAddress());
            if (device.getAddress().equals(mEditTextBtMac.getText().toString().trim())) {
                Message msg = mHandler.obtainMessage();
                msg.what = CVTE_DEVICE_CONNECT;
                msg.obj = device;
                mHandler.sendMessage(msg);
            }
        }

        @Override
        public void onModeSwitch(int newMode) {
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mtk__connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
