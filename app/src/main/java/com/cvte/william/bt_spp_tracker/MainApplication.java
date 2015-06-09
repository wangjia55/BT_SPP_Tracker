package com.cvte.william.bt_spp_tracker;
import android.app.Application;
import com.mediatek.wearable.WearableManager;

/**
 * Created by William on 2015/4/15.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // wearable init
        WearableManager.getInstance().init(true, getApplicationContext(), "we had"); //这是必须的
    }
}
