package com.speedata.speakercheck.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.serialport.DeviceControlSpd;
import android.serialport.SerialPortSpd;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.speedata.speakercheck.R;
import com.speedata.speakercheck.dialogs.DigitDialog;
import com.speedata.speakercheck.dialogs.MoniDialog;
import com.speedata.speakercheck.utils.Cmds;
import com.speedata.speakercheck.utils.DataConversionUtils;
import com.speedata.speakercheck.utils.SpeakerApi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static android.content.ContentValues.TAG;
import static com.speedata.speakercheck.utils.DataConversionUtils.byteArrayToString;
import static java.lang.Integer.valueOf;

public class SpeakerActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private TextView channelSpinner; //选择信道的spinner控件
    private String channel16; //得到的类似"00"的16位信道字符串
    private static final String TAG = "Speaker_DEV"; //测试用的TAG
    public SerialPortSpd IDDev; //设备控制
    private DeviceControlSpd DevCtrl; //GPIO控制
    //    private DeviceControlSpd DevCtrl75; //GPIO控制
    private DeviceControlSpd DevCtrl76; //GPIO控制
    private static final String SERIALPORT_PATH = "/dev/ttyMT0"; //path
    public int IDFd; //用于设备控制
    private ReadThread reader; //读取模块反馈的线程
    private Handler handler;
    private Cmds cmds; //封装了发送的命令的cmds,可用的为其中几项
    private int vol = 8; //默认音量大小
    private Button volSub, volAdd; //音量增减按键
    private Button btnSpeaker; //控制对讲机语音收发开始结束的按钮
    public String channelRemember = "01"; //记录选择的信道,上电后直接开始
    private String rememberLast = "00"; //记录上一次选择的信道
    private int channelNumber = 0; //记录spinner的选择项
    private TextView tvVol; //显示音量
    private ImageView imageView; //显示接收语音的声音显示条
    private AnimationDrawable animation; //显示对象
    private SpeakerActivity activity;
    private SpeakerApi speakerApi; //准备添加并修改为api的接口调用
    /**
     * 退出当前界面时弹出的对话框
     */
    private AlertDialog mExitDialog;

    private Button btnYayin;
    public boolean use = false;
    //先监听音量键-的按下与抬起动作
    //之后要改为侧键监听
    private DigitDialog setDigit;
    private MoniDialog setMoni;
    private int cishu = 1;

    //侧键以及实体按键的按压广播
    public static final String START_SCAN_ACTION_F5 = "keycode.f5.down";
    public static final String STOP_SCAN_ACTION_F5 = "keycode.f5.up";
    public static final String START_SCAN_ACTION_F4 = "keycode.f4.down";
    public static final String STOP_SCAN_ACTION_F4 = "keycode.f4.up";
    public static final String LONGPRESSED_F4 = "keycode.f4.longpressed";
    public static final String LONGPRESSED_F5 = "keycode.f5.longpressed";


    //通知
    int ID = 0x123;
    NotificationManager mNotificationManager;
    private Context context;
    //mic增益
    public String micGain = "12"; //记录数字信道的mic增益值，默认为12
    private String[] channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = SpeakerActivity.this;
        setContentView(R.layout.activity_speaker);
        initTitle();
//        speakerApi = new SpeakerApi(); //对讲机api
        initView();
        init(); //初始化并起线程
        acquireWakeLock();
        intentFilter();
        initNot(); //常驻通知栏
        int readFunction = readFunction();
        channelSpinner.setText(channel[readFunction]);
        changeChannel(readFunction);
    }

    private void initNot() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("对讲机")//设置通知栏标题
                .setContentText("点击打开对讲机界面") //设置通知栏显示内容
                .setContentIntent(getDefalutIntent(Notification.FLAG_NO_CLEAR)) //设置通知栏点击意图
//  .setNumber(number) //设置通知集合的数量
                .setTicker("进入对讲机应用") //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
//  .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.drawable.ic_launcher); //设置通知小ICON

        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        mNotificationManager.notify(ID, notification);

    }

    private PendingIntent getDefalutIntent(int flags) {
        Intent mIntent = new Intent(Intent.ACTION_MAIN);
        mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mIntent.setComponent(new ComponentName(context.getPackageName(), SpeakerActivity.class.getCanonicalName()));
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        return PendingIntent.getActivity(context, 1, mIntent, flags);

    }

    private void intentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(START_SCAN_ACTION_F4);
        filter.addAction(STOP_SCAN_ACTION_F4);
        filter.addAction(START_SCAN_ACTION_F5);
        filter.addAction(STOP_SCAN_ACTION_F5);
        filter.addAction(LONGPRESSED_F4);
        filter.addAction(LONGPRESSED_F5);

        registerReceiver(receiver, filter);
    }

    PowerManager.WakeLock wakeLock = null;

    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    @SuppressLint({"WakelockTimeout", "InvalidWakeLockTag"})
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            }
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock) {
            wakeLock.release();
            wakeLock = null;
        }
    }


    //界面标题栏优化美化
    private void initTitle() {
        activity = this;
        Window window = activity.getWindow();
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(Color.rgb(39, 39, 37));
        ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
            ViewCompat.setFitsSystemWindows(mChildView, true);
        }
    }

    //初始化
    @SuppressLint("HandlerLeak")
    private void init() {

        Log.i(TAG, "init is called");
        IDDev = new SerialPortSpd();
        try {
            IDDev.OpenSerial(SERIALPORT_PATH, 57600);
            IDFd = IDDev.getFd();
            Log.i(TAG, "SerialPort is open IDFd = " + IDFd);
        } catch (IOException e) {
            Log.e(TAG, "open serial error");
            return;
        }
        try {
            Thread.currentThread();
            Thread.sleep(30);
        } catch (InterruptedException ignored) {
        }
        try {
//            DevCtrl = new DeviceControlSpd();
            DevCtrl = new DeviceControlSpd(DeviceControlSpd.PowerType.NEW_MAIN, 0,12,75);
            DevCtrl76 = new DeviceControlSpd(DeviceControlSpd.PowerType.NEW_MAIN, 76);
            //DevCtrl.PowerOnDevice();
            Log.d(TAG, "DevCtrl is open DevCtrl = " + DevCtrl);
        } catch (IOException e) {
            Log.e(TAG, "open power error");
            return;
        }
        try {
            Thread.currentThread();
            Thread.sleep(30);
        } catch (InterruptedException ignored) {
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
                    byte[] cardtemp;
                    //上电后,模块返回的串口反馈包
                    if ("44c6b5b5c0313b0d0a".equals(byteArrayToString(buf))) {

                        if ("01".equals(channelRemember) || "02".equals(channelRemember) || "03".equals(channelRemember) || "04".equals(channelRemember)
                                || "05".equals(channelRemember) || "06".equals(channelRemember) || "07".equals(channelRemember) || "08".equals(channelRemember)
                                || "09".equals(channelRemember) || "10".equals(channelRemember)) {
                            cpsChannel(channelRemember);
                        } else {
                            cardtemp = cmds.changeChannel(channelRemember);
                            IDDev.WriteSerialByte(IDFd, cardtemp);
                        }

//                          speakerApi.initChannels(channelRemember);
                    } else if ((btoi(buf[0]) == 0x68) && (btoi(buf[buf.length - 1]) == 0x10)) {
                        messageManage(buf);

                    }
                }
            }
        };
    }

    //处理对讲模块反馈的信息
    private void messageManage(byte[] buf) {
        int voice = 0;
        switch (btoi(buf[1])) {

            case 0x06://语音呼叫，根据流程来设置反馈
                if (btoi(buf[2]) == 0x02) {
                    voice = order7631(btoi(buf[3]));
                }
                if (voice == 1) {
                    animation.stop(); //停止
                    animation.setOneShot(false);
                    animation.start(); //启动
                } else if (voice == 0) {
                    animation.setOneShot(true);
                }
                break;

            case 0x01:
                Toast.makeText(this, "信道切换：" + basicOrder(btoi(buf[3])), Toast.LENGTH_SHORT).show();
                break;

            case 0x35:
                Toast.makeText(this, "模拟信道：" + basicOrder(btoi(buf[3])), Toast.LENGTH_SHORT).show();
                break;

            case 0x36:
                Toast.makeText(this, "数字信道：" + basicOrder(btoi(buf[3])), Toast.LENGTH_SHORT).show();
                break;

            case 0x0b:
                Toast.makeText(this, "MIC 增益：" + basicOrder(btoi(buf[3])), Toast.LENGTH_SHORT).show();
                break;

            default:
                break;

        }

    }

    private int order7631(int btoi) {
        switch (btoi) {
            //语音呼叫反馈包
            //语音接收开始/结束串口包
            case 0x6f: //"语音接收结束"
                Toast.makeText(this, "语音接收结束", Toast.LENGTH_SHORT).show();
                return 0;
            case 0x60: //"语音接收开始"
                Toast.makeText(this, "语音接收开始", Toast.LENGTH_SHORT).show();
                return 1;
            default:
                break;
        }
        return 0;
    }


    private static int btoi(byte a) {
        return (a < 0 ? a + 256 : a);
    }

    //界面初始化
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initView() {
        cmds = new Cmds();
        volSub = (Button) findViewById(R.id.vol_sub);
        volSub.setOnClickListener(this);
        volAdd = (Button) findViewById(R.id.vol_add);
        volAdd.setOnClickListener(this);

        tvVol = (TextView) findViewById(R.id.tv_vol);
        tvVol.setText(vol + "");

        btnYayin = (Button) findViewById(R.id.btn_yayin);
        btnYayin.setOnClickListener(this);
//        setDigit = new DigitDialog(this, this);
//        setMoni = new MoniDialog(this, this);

        ToggleButton powerBtn = (ToggleButton) findViewById(R.id.toggleButton_power);
        powerBtn.setOnCheckedChangeListener(this);

        //界面中间语音显示
        imageView = (ImageView) findViewById(R.id.image_view);
        //设置动画背景
        imageView.setBackgroundResource(R.drawable.animation_list); //其中animation_list就是上一步准备的动画描述文件的资源名
        //获得动画对象
        animation = (AnimationDrawable) imageView.getBackground();
        //是否仅仅启动一次？
        animation.setOneShot(false);

        ButtonListener b = new ButtonListener();
        btnSpeaker = (Button) findViewById(R.id.btn_speaker_1);
        btnSpeaker.setOnClickListener(b);
        btnSpeaker.setOnTouchListener(b);
        btnSpeaker.setBackgroundResource(R.drawable.speakeroff);

        buttonUseless();

        //下拉列表
        channel = getResources().getStringArray(
                R.array.channel);

        channelSpinner = this.findViewById(R.id.sp_channel);

//        ArrayAdapter<String> channelAdapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_spinner_item, channel);
//        // Log.w(TAG,"WARN");
//        channelAdapter
//                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        channelSpinner.setAdapter(channelAdapter);
//        channelSpinner
//                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
//                    byte[] cardtemp = null;
//
//                    @Override
//                    public void onItemSelected(AdapterView<?> arg0, View arg1,
//                                               int position, long id) {
//                        arg0.setVisibility(View.VISIBLE);
//
//                        TextView v1 = (TextView) arg1;
//                        v1.setTextColor(Color.WHITE); //可以随意设置自己要的颜色值
//
//                        int select = channelSpinner.getSelectedItemPosition();
//                        changeChannel(select);
////                      channelRemember = speakerApi.changeChannels(channelNumber);
//                    }
//
//
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> arg0) {
//                    }
//                });
//
//        channelSpinner.setSelection(0);


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

    private void changeChannel(int select) {
        channelNumber = select;

        channel16 = getchannel(select);

        if ("01".equals(channel16) || "02".equals(channel16) || "03".equals(channel16) || "04".equals(channel16)
                || "05".equals(channel16) || "06".equals(channel16) || "07".equals(channel16) || "08".equals(channel16)) {
            cpsChannel(channel16);
        } else {
            byte[] cardtemp = cmds.changeChannel(channel16);
            IDDev.WriteSerialByte(IDFd, cardtemp);
        }
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

            default:
                break;

        }
        return "01";
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.toggleButton_power:
                if (isChecked) { //开启
                    Log.i(TAG, "powerBtn is called on");
                    try {
                        //设备反馈信息接收
                        messageRev(cishu);
//                        DevCtrl.newSetGpioOn(75);
//                        DevCtrl.newSetGpioOn(0);
//                        DevCtrl.newSetGpioOn(12);
//                        DevCtrl.newSetGpioOff(76);
                        DevCtrl.PowerOnDevice();
                        DevCtrl76.PowerOffDevice();

                        Log.d(TAG, "power on");
                        try {
                            Thread.currentThread();
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {
                        }

//                        File DeviceName = new File(DeviceControlSpd.POWER_NEWMAIN);
//                        //open file
//                        BufferedWriter ctrlfile = new BufferedWriter(new FileWriter(DeviceName, false));
//                        //将GPIO设置为高电平
//                        ctrlfile.write("mode 75 0");
//                        ctrlfile.flush();
//                        ctrlfile.write("dir 75 1");
//                        ctrlfile.flush();
//                        ctrlfile.write("out 75 1");
//                        ctrlfile.flush();
                    } catch (IOException e) {
                        Log.e(TAG, "open power error");
                    }
//                    speakerApi.power(true);

                    buttonUse();

                } else { //关闭
                    buttonUseless();
                    Log.i(TAG, "powerBtn is called off");
                    try {
//                        DevCtrl.newSetGpioOff(0);
//                        DevCtrl.newSetGpioOff(12);
////                        DevCtrl.newSetGpioOff(75);
//                        DevCtrl.newSetGpioOn(76);
                        DevCtrl.PowerOffDevice();
                        DevCtrl76.PowerOnDevice();
                    } catch (IOException ignored) {

                    }
//                    speakerApi.power(false);
                }
                break;
            default:
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private void messageRev(int in) {
        if (in == 1) {
            cishu++;
            return;
        }
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
                        if ("01".equals(channelRemember) || "02".equals(channelRemember) || "03".equals(channelRemember) || "04".equals(channelRemember)
                                || "05".equals(channelRemember) || "06".equals(channelRemember) || "07".equals(channelRemember) || "08".equals(channelRemember)) {
                            cpsChannel(channelRemember);
                        } else {
                            cardtemp = cmds.changeChannel(channelRemember);
                            IDDev.WriteSerialByte(IDFd, cardtemp);
                        }
//                          speakerApi.initChannels(channelRemember);
                    } else if ((btoi(buf[0]) == 0x68) && (btoi(buf[buf.length - 1]) == 0x10)) {
                        messageManage(buf);
                    }
                }
            }
        };
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        byte[] cardtemp;
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
            case R.id.btn_yayin:
                if (channelNumber >= 8 && use) {

                    setMoni.setTitle("模拟设置");
                    setMoni.show();
                } else if (channelNumber < 8 && use) {

                    if (!channelRemember.equals(rememberLast)) {
//                        setDigit = new DigitDialog(this, this);
                        rememberLast = channelRemember;
                    }

                    setDigit.setTitle("数字设置");
                    setDigit.show();
                    rememberLast = channelRemember;
                }

                break;

            default:
                break;
        }
    }

    private void buttonUseless() {
        use = false;
        volSub.setEnabled(false);
        volAdd.setEnabled(false);
        btnSpeaker.setEnabled(false);
        btnYayin.setEnabled(false);
    }

    private void buttonUse() {
        use = true;
        volSub.setEnabled(true);
        volAdd.setEnabled(true);
        btnSpeaker.setEnabled(true);
        btnYayin.setEnabled(true);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        unregisterReceiver(receiver);
        mNotificationManager.cancel(ID);
        Log.i(TAG, "onDestory is called");
        closePort();
        releaseWakeLock();
    }

    private void closePort() { //关闭下电
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
//                DevCtrl75.DeviceClose();
//                DevCtrl76.DeviceClose();
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
        @Override
        public void run() {
            super.run();
            Log.d(TAG, "thread start");
            while (!isInterrupted()) {
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

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_speaker_1) {
                Log.d("test", "cansal button ---> click");
            }
        }

        //语音按钮的监听控制
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) { //获取按钮状态,监听按下与抬起的动作
            if (v.getId() == R.id.btn_speaker_1) {
                if (event.getAction() == MotionEvent.ACTION_UP) { //结束发送
                    Log.d("test", "cansal button ---> cancel");
                    btnSpeaker.setBackgroundResource(R.drawable.speakeroff);
                    animation.setOneShot(true);
                    if (channelNumber < 8) { //数字信道语音发送结束
                        cardtemp = cmds.voiceCall("ff", "04ffffff");
                        Log.d(TAG, "cardtemp 结束发送: " + DataConversionUtils.byteArrayToString(cardtemp));
                        int writeSerialByte = IDDev.WriteSerialByte(IDFd, cardtemp);
                        Log.d(TAG, "onTouch: " + writeSerialByte);
//                        speakerApi.startSpeak(true);
                    } else { //模拟信道语音发送结束
                        cardtemp = cmds.voiceCall("ff", "00000000");
                        IDDev.WriteSerialByte(IDFd, cardtemp);
//                        speakerApi.startSpeak(false);
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { //开始发送
                    Log.d("test", "cansal button ---> down");
                    btnSpeaker.setBackgroundResource(R.drawable.speakeron);
                    animation.stop();
                    animation.setOneShot(false);
                    animation.start(); //启动
                    if (channelNumber < 8) { //数字信道语音发送开始
                        cardtemp = cmds.voiceCall("01", "04ffffff");
                        Log.d(TAG, "cardtemp 开始发送: " + DataConversionUtils.byteArrayToString(cardtemp));
                        IDDev.WriteSerialByte(IDFd, cardtemp);

//                        speakerApi.finishSpeak(true);
                    } else { //模拟信道语音发送开始
                        cardtemp = cmds.voiceCall("01", "00000000");
                        IDDev.WriteSerialByte(IDFd, cardtemp);
//                        speakerApi.finishSpeak(false);
                    }
                }
            }
            return false;
        }
    }

    //侧键按键抬起监听，结束语音发送
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("test", "cansal button ---> up" + keyCode);

//        byte[] cardtemp = null;
//        if (keyCode == KeyEvent.KEYCODE_F5) {
////            btnSpeaker.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 350, 350, 0));
//            Log.d("test", "cansal button ---> cancel");
//            btnSpeaker.setBackgroundResource(R.drawable.speakeroff);
//            animation.setOneShot(true);
//            if (channelNumber < 8) { //数字信道语音发送结束
//                cardtemp = cmds.voiceCall("ff", "04ffffff");
//                IDDev.WriteSerialByte(IDFd, cardtemp);
////                        speakerApi.startSpeak(true);
//            } else { //模拟信道语音发送结束
//                cardtemp = cmds.voiceCall("ff", "00000000");
//                IDDev.WriteSerialByte(IDFd, cardtemp);
////                        speakerApi.startSpeak(false);
//            }
//            return true;
//        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Log.d("test1", "cansal button ---> press" + keyCode);
        return super.onKeyLongPress(keyCode, event);
    }

    //侧键按键按压监听，开始语音发送
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("test", "cansal button ---> down" + keyCode);
        //    byte[] cardtemp = null;
        if (keyCode == KeyEvent.KEYCODE_F3) {
            int function = readFunction();
            channelSpinner.setText(channel[function]);
            changeChannel(function);
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 弹出退出时的对话框
            mExitDialog.show();
            // 返回true以表示消费事件，避免按默认的方式处理“BACK”键的事件
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private static int readFunction() {
        int state = 0;
        File file = new File("/sys/class/misc/hwoper/function");
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            state = Integer.parseInt(bufferedReader.readLine());
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "readEm55state: " + state);
        return state;
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

                default:
                    break;
            }
        }
    }


    private CharSequence basicOrder(int btoi) {
        switch (btoi) {
            case 0x00:
                return "设置成功";

            case 0x01:
                return "模块繁忙或者设置失败";

            case 0x02:
                return "无此信道或信道错误";

            case 0x07:
                return "模块被毙";

            case 0x09:
                return "校验错误";

            default:
                break;

        }
        return "未知错误";
    }


    private void cpsChannel(String channel) {
        switch (channel) {
            case "01":
                setChannel("403012500", "403012500", "888", "1", "8", "1");
                break;
            case "02":
                setChannel("409912500", "409912500", "888", "1", "8", "1");
                break;
            case "03":
                setChannel("431375000", "431375000", "888", "1", "8", "1");
                break;
            case "04":
                setChannel("436255000", "436255000", "888", "1", "8", "1");
                break;
            case "05":
                setChannel("439975000", "439975000", "888", "1", "8", "1");
                break;
            case "06":
                setChannel("400000000", "400000000", "888", "1", "8", "1");
                break;
            case "07":
                setChannel("435000000", "435000000", "888", "1", "8", "1");
                break;
            case "08":
                setChannel("470000000", "470000000", "888", "1", "8", "1");
                break;
            case "09":
                setChannel("403012500", "403012500", "100", "100");
                break;
            case "10":
                setChannel("409912500", "409912500", "67", "67");
                break;
            default:
                break;
        }

    }


    private void setChannel(String et1, String et2, String et3, String et4, String et5, String et6) {
        byte[] cardtemp;
        String jieshou = et1;
        String fasong = et2;
        StringBuilder benjiid = new StringBuilder(et3);
        StringBuilder lianxiren = new StringBuilder(et4);
        StringBuilder miyao = new StringBuilder(et5);
        String jieshouzu = et6;

        jieshou = Integer.toHexString(valueOf(jieshou));
        StringBuilder sum = new StringBuilder();
        for (int i = 0; i < jieshou.length(); i = i + 2) {
            String s = jieshou.substring(jieshou.length() - (2 + i), jieshou.length() - i);
            sum.append(s);
        }
        jieshou = sum.toString();

        fasong = Integer.toHexString(valueOf(fasong));
        StringBuilder sum2 = new StringBuilder();
        for (int i = 0; i < fasong.length(); i = i + 2) {
            String s = fasong.substring(fasong.length() - (2 + i), fasong.length() - i);
            sum2.append(s);
        }
        fasong = sum2.toString();

        benjiid = new StringBuilder(Integer.toHexString(valueOf(benjiid.toString()))); //10进制做成16进制数
        lianxiren = new StringBuilder(Integer.toHexString(valueOf(lianxiren.toString()))); //10进制做成16进制数

        for (int i = benjiid.length(); i < 8; i++) {
            benjiid.insert(0, "0");
        }
        for (int i = lianxiren.length(); i < 8; i++) {
            lianxiren.insert(0, "0");
        }

        if ("".equals(miyao.toString())) {
            miyao = new StringBuilder("0");
        }
        miyao = new StringBuilder(Integer.toHexString(valueOf(miyao.toString())));
        for (int i = miyao.length(); i < 16; i++) {
            miyao.insert(0, "0");
        }
        if ("".equals(jieshouzu)) {
            jieshouzu = "0";
        }
        jieshouzu = getJieshouzu(jieshouzu);


        //这是设置数字组命令中的DATA部分
        String all = "00" + jieshou + fasong + benjiid + "01" + "02"
                + lianxiren + "ff" + miyao + jieshouzu;

        cardtemp = cmds.setNumberGroupCommand(all);
        String byteArrayToString = DataConversionUtils.byteArrayToString(cardtemp);
        Log.d(TAG, "setChannel: " + byteArrayToString);
        IDDev.WriteSerialByte(IDFd, cardtemp);
    }


    private void setChannel(String et1, String et2, String et3, String et4) {
        byte[] cardtemp;
        String jieshou = et1;
        String fasong = et2;
        String jieshouyayin = et3;
        String fasongyayin = et4;

        jieshou = Integer.toHexString(valueOf(jieshou));
        StringBuilder sum = new StringBuilder();
        for (int i = 0; i < jieshou.length(); i = i + 2) {
            String s = jieshou.substring(jieshou.length() - (2 + i), jieshou.length() - i);
            sum.append(s);
        }
        jieshou = sum.toString();

        fasong = Integer.toHexString(valueOf(fasong));
        StringBuilder sum2 = new StringBuilder();
        for (int i = 0; i < fasong.length(); i = i + 2) {
            String s = fasong.substring(fasong.length() - (2 + i), fasong.length() - i);
            sum2.append(s);
        }
        fasong = sum2.toString();

        jieshouyayin = Integer.toHexString(valueOf(jieshouyayin));

        fasongyayin = Integer.toHexString(valueOf(fasongyayin));
        if (jieshouyayin.length() == 1) {
            jieshouyayin = "0" + jieshouyayin;
        }
        if (fasongyayin.length() == 1) {
            fasongyayin = "0" + fasongyayin;
        }

        //这是设置模拟组命令中的DATA部分
        String all = "80" + "01" + jieshou + fasong + "01" + "01"
                + jieshouyayin + "01" + fasongyayin;


        cardtemp = cmds.setSimulationGroupCommand(all);
        IDDev.WriteSerialByte(IDFd, cardtemp);

    }


    private String getJieshouzu(String jieshouzu) {
        String[] arr = jieshouzu.split(",");
        StringBuilder sum = new StringBuilder();
        for (String anArr : arr) {
            StringBuilder a = new StringBuilder(Integer.toHexString(valueOf(anArr)));
            for (int j = a.length(); j < 8; j++) {
                a.insert(0, "0");
            }
            sum.insert(0, a.toString());
        }

        for (int k = sum.length(); k < 256; k++) {
            sum.append("0");
        }

        return sum.toString();
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getAction();
            byte[] cardtemp;
            if (state != null) {
                if ( //state.equals(START_SCAN_ACTION_F4) || state.equals(START_SCAN_ACTION_F5) ||
                        state.equals(LONGPRESSED_F4) || state.equals(LONGPRESSED_F5)) { //假设是start和stop来控制开始和结束
                    //目前监听了F4和F5的按下广播和长按广播,先以长按广播为主。
                    if (!use) {
                        return;
                    }
                    Log.d("test", "cansal button ---> down");
                    btnSpeaker.setBackgroundResource(R.drawable.speakeron);
                    animation.stop();
                    animation.setOneShot(false);
                    animation.start(); //启动
                    if (channelNumber < 8) { //数字信道语音发送开始
                        cardtemp = cmds.voiceCall("01", "04ffffff");
                        IDDev.WriteSerialByte(IDFd, cardtemp);
                        //                        speakerApi.finishSpeak(true);
                    } else { //模拟信道语音发送开始
                        cardtemp = cmds.voiceCall("01", "00000000");
                        IDDev.WriteSerialByte(IDFd, cardtemp);
                        //                        speakerApi.finishSpeak(false);
                    }

                } else if (state.equals(STOP_SCAN_ACTION_F4) || state.equals(STOP_SCAN_ACTION_F5)) { //监听了F4和F5的按钮抬起广播

                    if (!use) {
                        return;
                    }
                    Log.d("test", "cansal button ---> cancel");
                    btnSpeaker.setBackgroundResource(R.drawable.speakeroff);
                    animation.setOneShot(true);

                    if (channelNumber < 8) { //数字信道语音发送结束
                        cardtemp = cmds.voiceCall("ff", "04ffffff");
                        IDDev.WriteSerialByte(IDFd, cardtemp);
                        //                        speakerApi.startSpeak(true);
                    } else { //模拟信道语音发送结束
                        cardtemp = cmds.voiceCall("ff", "00000000");
                        IDDev.WriteSerialByte(IDFd, cardtemp);
                        //                        speakerApi.startSpeak(false);
                    }
                }
            }
        }
    };
}
