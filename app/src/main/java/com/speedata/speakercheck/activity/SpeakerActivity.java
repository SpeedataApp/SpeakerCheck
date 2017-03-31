package com.speedata.speakercheck.activity;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.serialport.DeviceControl;
import android.serialport.SerialPort;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.speedata.speakercheck.R;
import com.speedata.speakercheck.utils.Cmds;
import com.speedata.speakercheck.utils.SpeakerApi;

import java.io.IOException;


import static com.speedata.speakercheck.utils.DataConversionUtils.byteArrayToString;

public class SpeakerActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private Spinner channelSpinner; //选择信道的spinner控件
    private String channel16; //得到的类似"00"的16位信道字符串
    private static final String TAG = "Speaker_DEV"; //测试用的TAG

    public SerialPort IDDev; //设备控制
    private DeviceControl DevCtrl; //GPIO控制
    private static final String SERIALPORT_PATH = "/dev/ttyMT1"; //path
    public int IDFd; //用于设备控制
    private ReadThread reader; //读取模块反馈的线程
    private Handler handler;
    private Cmds cmds; //封装了发送的命令的cmds,可用的为其中几项
    private int vol = 8; //默认音量大小
    private Button volSub, volAdd; //音量增减按键
    private Button btnSpeaker; //控制对讲机语音收发开始结束的按钮
    private String channelRemember = "01"; //记录选择的信道,上电后直接开始
    private int channelNumber = 0; //记录spinner的选择项
    private TextView tvVol; //显示音量

    private SpeakerApi speakerApi; //准备添加并修改为api的接口调用
    /**
     * 退出当前界面时弹出的对话框
     */
    private AlertDialog mExitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker);

        speakerApi = new SpeakerApi(); //对讲机api
        initView();
        init(); //初始化并起线程
    }

    private void init() {

        Log.i(TAG, "init is called");
        IDDev = new SerialPort();
        try {
            IDDev.OpenSerial(SERIALPORT_PATH, 57600);
            IDFd = IDDev.getFd();
            Log.i(TAG, "SerialPort is open IDFd = " + IDFd);
        } catch (IOException e) {
            Log.e(TAG, "open serial error");
            return;
        }
        try   {
            Thread.currentThread();
            Thread.sleep(30);
        } catch (InterruptedException e) {
        }
        try {
            DevCtrl = new DeviceControl("/sys/class/misc/mtgpio/pin");
                //DevCtrl.PowerOnDevice();
            Log.d(TAG, "DevCtrl is open DevCtrl = " + DevCtrl);
        } catch (IOException e) {
            Log.e(TAG, "open power error");
            return;
        }
        try {
            Thread.currentThread();
            Thread.sleep(30);
        } catch (InterruptedException   e) {
        }
//            speakerApi.openSerialPort();
        reader = new ReadThread();
        reader.start();

        handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 1) {
                        Log.i(TAG, "handler is called");
                        byte[] buf = (byte[]) msg.obj;
                        byte[] cardtemp = null;
                        //上电后,模块返回的串口反馈包
                        if ("44c6b5b5c0313b0d0a".equals(byteArrayToString(buf))) {
                            cardtemp = cmds.changeChannel(channelRemember);
                            IDDev.WriteSerialByte(IDFd, cardtemp);
//                          speakerApi.initChannels(channelRemember);
                        }
                    }
                }
            };
    }

    private void initView() {
        cmds = new Cmds();
        volSub = (Button) findViewById(R.id.vol_sub);
        volSub.setOnClickListener(this);
        volAdd = (Button) findViewById(R.id.vol_add);
        volAdd.setOnClickListener(this);

        tvVol = (TextView) findViewById(R.id.tv_vol);
        tvVol.setText(vol + "");

        ToggleButton powerBtn = (ToggleButton) findViewById(R.id.toggleButton_power);
        powerBtn.setOnCheckedChangeListener(this);

        ButtonListener b = new ButtonListener();
        btnSpeaker = (Button) findViewById(R.id.btn_speaker_1);
        btnSpeaker.setOnClickListener(b);
        btnSpeaker.setOnTouchListener(b);
        btnSpeaker.setBackgroundResource(R.color.green);

        buttonUseless();

        String[] channel = getResources().getStringArray(
                R.array.channel);

        channelSpinner = (Spinner) this.findViewById(R.id.sp_channel);

        ArrayAdapter<String> channelAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, channel);
        // Log.w(TAG,"WARN");
        channelAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        channelSpinner.setAdapter(channelAdapter);
        channelSpinner
                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    byte[] cardtemp = null;
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        arg0.setVisibility(View.VISIBLE);

                        int select = channelSpinner.getSelectedItemPosition();
                        channelNumber = select;

                        channel16 = getchannel(select);
                        cardtemp = cmds.changeChannel(channel16);
                        IDDev.WriteSerialByte(IDFd, cardtemp);
//                      channelRemember = speakerApi.changeChannels(channelNumber);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });

        channelSpinner.setSelection(0);

        // 创建退出时的对话框，此处根据需要显示的先后顺序决定按钮应该使用Neutral、Negative或Positive
        DialogButtonOnClickListener dialogButtonOnClickListener = new DialogButtonOnClickListener();
        mExitDialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定要退出对讲机吗?")
                .setCancelable(false)
                .setNeutralButton("退出", dialogButtonOnClickListener)
                .setPositiveButton("取消", dialogButtonOnClickListener)
                .create();
    }

    private String getchannel(int select) { //从spinner的选项,对应所需发送的命令
        switch (select) {

            case 0:
                channelRemember = "01";
                return "01";

            case 1:
                channelRemember = "02";
                return "02";

            case 2:
                channelRemember = "03";
                return "03";

            case 3:
                channelRemember = "04";
                return "04";

            case 4:
                channelRemember = "05";
                return "05";

            case 5:
                channelRemember = "06";
                return "06";

            case 6:
                channelRemember = "07";
                return "07";

            case 7:
                channelRemember = "08";
                return "08";

            case 8:
                channelRemember = "09";
                return "09";

            case 9:
                channelRemember = "0a";
                return "0a";

            case 10:
                channelRemember = "0b";
                return "0b";

            case 11:
                channelRemember = "0c";
                return "0c";

            case 12:
                channelRemember = "0d";
                return "0d";

            case 13:
                channelRemember = "0e";
                return "0e";

            case 14:
                channelRemember = "0f";
                return "0f";

            case 15:
                channelRemember = "10";
                return "10";

        }
        return "01";
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        byte[] cardtemp = null;
        switch (buttonView.getId()) {
            case R.id.toggleButton_power:
                if (isChecked) { //开启
                    Log.i(TAG, "powerBtn is called on");
                    try {
                        DevCtrl.PowerOnDevice();
                        Log.d(TAG, "power on");
                        try {
                            Thread.currentThread();
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "open power error");
                    }
//                    speakerApi.power(true);

                    buttonUse();
                } else { //关闭
                    buttonUseless();
                    Log.i(TAG, "powerBtn is called off");
                    try {
                        DevCtrl.PowerOffDevice();
                    } catch (IOException e) {

                    }
//                    speakerApi.power(false);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        byte[] cardtemp = null;
        switch (v.getId()) {
            case R.id.vol_sub:
                if (vol > 1) {
                    vol--;
                    cardtemp = cmds.volumeSetting("0" + vol);
                    IDDev.WriteSerialByte(IDFd, cardtemp);
//                    speakerApi.setVol(vol);
                    tvVol.setText(vol + "");
                } else {
                    Toast.makeText(this, "当前为最低音量等级1", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case R.id.vol_add:
                if (vol < 9) {
                    vol++;
                    cardtemp = cmds.volumeSetting("0" + vol);
                    IDDev.WriteSerialByte(IDFd, cardtemp);
//                    speakerApi.setVol(vol);
                    tvVol.setText(vol + "");
                } else {
                    Toast.makeText(this, "当前为最高音量等级9", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
    }

    private void buttonUseless() {
        volSub.setEnabled(false);
        volAdd.setEnabled(false);
    }

    private void buttonUse() {
        volSub.setEnabled(true);
        volAdd.setEnabled(true);
    }

    public void onDestroy() {

        super.onDestroy();
        Log.i(TAG, "onDestory is called");
        closePort();
    }
    private void closePort(){ //关闭下电
        if (IDDev != null) {
            Log.i(TAG, "close serial port");
            IDDev.CloseSerial(IDFd);
            IDFd = 0;
        }
        if (DevCtrl != null) {
            Log.i(TAG, "close dev power");
            try {
            //    DevCtrl.PowerOnDevice();
                DevCtrl.DeviceClose();
            } catch (IOException e) {
                Log.e(TAG, "close power error");
            }
        }
//        speakerApi.closePorts();

        if (reader != null) {
            reader.interrupt();
        }
    }

    private class ReadThread extends Thread { //读取反馈的线程
        public void run() {
            super.run();
            Log.d(TAG, "thread start");
            while(!isInterrupted()) {
                Message msg = new Message();
                //Log.d(TAG, "thread start Message");
                byte[] buf;
                try {
                    buf = IDDev.ReadSerial(IDFd, 128);
                    Log.d(TAG, "thread start buf");
                } catch (IOException e) {
                    Log.e(TAG, "ReadSerial error");
                    return;
                }
//                buf = speakerApi.readSerial();
                if (buf != null) {
                    Log.d(TAG, "read end");
                    msg.what = 1;
                    msg.obj = buf;
                    handler.sendMessage(msg);
                }
            }
            Log.d(TAG, "thread stop");
        }
    }


    private class ButtonListener implements View.OnClickListener, View.OnTouchListener {
        byte[] cardtemp = null;
        public void onClick(View v) {
            if (v.getId() == R.id.btn_speaker_1) {
                Log.d("test", "cansal button ---> click");
            }
        }

        public boolean onTouch(View v, MotionEvent event) { //获取按钮状态,监听按下与抬起的动作
            if (v.getId() == R.id.btn_speaker_1) {
                if (event.getAction() == MotionEvent.ACTION_UP){ //结束发送
                    Log.d("test", "cansal button ---> cancel");
                    btnSpeaker.setBackgroundResource(R.color.green);
                    if (channelNumber < 8) { //数字信道语音发送开始
                        cardtemp = cmds.voiceCall("ff", "04ffffff");
                        IDDev.WriteSerialByte(IDFd, cardtemp);
//                        speakerApi.startSpeak(true);
                    } else { //模拟信道语音发送开始
                        cardtemp = cmds.voiceCall("ff", "00000000");
                        IDDev.WriteSerialByte(IDFd, cardtemp);
//                        speakerApi.startSpeak(false);
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { //开始发送
                    Log.d("test", "cansal button ---> down");
                    btnSpeaker.setBackgroundResource(R.color.yellow);
                    if (channelNumber < 8) { //数字信道语音发送结束
                        cardtemp = cmds.voiceCall("01", "04ffffff");
                        IDDev.WriteSerialByte(IDFd, cardtemp);
//                        speakerApi.finishSpeak(true);
                    } else { //模拟信道语音发送结束
                        cardtemp = cmds.voiceCall("01", "00000000");
                        IDDev.WriteSerialByte(IDFd, cardtemp);
//                        speakerApi.finishSpeak(false);
                    }
                }
            }
            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 判断是否按下“BACK”(返回)键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 弹出退出时的对话框
            mExitDialog.show();
            // 返回true以表示消费事件，避免按默认的方式处理“BACK”键的事件
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出时的对话框的按钮点击事件
     */
    private class DialogButtonOnClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: // 取消
                    // 取消显示对话框
                    mExitDialog.dismiss();
                    break;

                case DialogInterface.BUTTON_NEUTRAL: // 退出程序
                    // 结束，将导致onDestroy()方法被回调
                    closePort();
                    finish();
                    break;
            }
        }
    }
}
