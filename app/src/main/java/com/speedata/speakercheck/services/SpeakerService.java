package com.speedata.speakercheck.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;

import com.speedata.speakercheck.App;
import com.speedata.speakercheck.Constant;
import com.speedata.speakercheck.dialogs.WaveViewDialog;
import com.speedata.speakercheck.utils.LogUtils;
import com.speedata.speakercheck.utils.SpeakerApi;

/**
 * Created by xu on 2017/7/5.
 */

public class SpeakerService extends Service {
    private boolean isFirst = true;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constant.START_SCAN_ACTION_F5.equals(action)) {
                LogUtils.d("收到广播START_SCAN_ACTION_F5");
                if (isFirst) {
                    startSpeak();
                } else {
                    stopSpeak();
                }
            }
        }
    };
    private WaveViewDialog waveViewDialog;

    private void startSpeak() {
        waveViewDialog = new WaveViewDialog(this);
        waveViewDialog.setCancelable(false);
        waveViewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waveViewDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0新特性
            waveViewDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY - 1);
        } else {
            waveViewDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        }
        waveViewDialog.show();

        if (App.currentChannel < 8) { //数字信道语音发送结束
            SpeakerApi.getInstance().startSpeak(true);
        } else { //模拟信道语音发送结束
            SpeakerApi.getInstance().startSpeak(false);
        }
        isFirst = false;
    }

    private void stopSpeak() {
        if (waveViewDialog != null) {
            waveViewDialog.dismiss();
        }


        if (App.currentChannel < 8) { //数字信道语音发送结束
            SpeakerApi.getInstance().finishSpeak(true);
        } else { //模拟信道语音发送结束
            SpeakerApi.getInstance().finishSpeak(false);
        }
        isFirst = true;
    }

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
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.START_SCAN_ACTION_F5);
        filter.addAction(Constant.STOP_SCAN_ACTION_F5);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
    }

}
