package com.speedata.speakercheck.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.speedata.speakercheck.Constant;
import com.speedata.speakercheck.utils.OpenAccessibilitySettingHelper;

/**
 * Created by xu on 2017/7/5.
 */

public class SpeakerService extends Service {
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(Constant.START_PTT);
        registerReceiver(receiver, filter);
        if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this,
                SpeakerAccessibilityService.class.getName())) {// 判断服务是否开启
            OpenAccessibilitySettingHelper.jumpToSettingPage(this);// 跳转到开启页面
        }

    }

    @Override
    public void onDestroy() {

        unregisterReceiver(receiver);
    }
}
