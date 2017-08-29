package com.speedata.speakercheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.speedata.speakercheck.activity.SpeakerActivity.START_SCAN_ACTION_F4;
import static com.speedata.speakercheck.activity.SpeakerActivity.START_SCAN_ACTION_F5;
import static com.speedata.speakercheck.activity.SpeakerActivity.STOP_SCAN_ACTION_F4;
import static com.speedata.speakercheck.activity.SpeakerActivity.STOP_SCAN_ACTION_F5;

public class ScanBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "Reginer";
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getAction();
        Log.d(TAG, "onReceive: " + state);
        if (state.equals(START_SCAN_ACTION_F4) || state.equals(START_SCAN_ACTION_F5)) {
            Log.d(TAG, "onReceive: start");
        } else if (state.equals(STOP_SCAN_ACTION_F4) || state.equals(STOP_SCAN_ACTION_F5)) {
            Log.d(TAG, "onReceive: stop");
        }
    }
}
