package com.speedata.speakercheck.services;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.speedata.speakercheck.Constant;
import com.speedata.speakercheck.utils.LogUtils;

/**
 * Created by xu on 2017/7/5.
 */

public class SpeakerAccessibilityService extends AccessibilityService {




    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int key = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        int read = Integer.parseInt(SystemProperties.get("persist.sys.speaker", "135"));


        if (read == key) {
            if (repeatCount == 0 && action == KeyEvent.ACTION_DOWN) {
                Intent intent = new Intent(Constant.START_PTT);
                sendBroadcast(intent);
                LogUtils.d("发送广播");
            }
        }
        return super.onKeyEvent(event);
    }

    /**
     * 启动
     */
    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        LogUtils.d("onServiceConnected");
    }

}
