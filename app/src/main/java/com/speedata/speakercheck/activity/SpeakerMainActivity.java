package com.speedata.speakercheck.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.speedata.speakercheck.App;
import com.speedata.speakercheck.R;
import com.speedata.speakercheck.dialogs.DigitDialog;
import com.speedata.speakercheck.dialogs.MoniDialog;
import com.speedata.speakercheck.services.SpeakerService;
import com.speedata.speakercheck.utils.LogUtils;
import com.speedata.speakercheck.utils.SpeakerApi;

import static com.speedata.speakercheck.utils.SpeakerApi.basicOrder;
import static com.speedata.speakercheck.utils.SpeakerApi.btoi;


/**
 * 对讲主界面
 * Created by 张明_ on 2019/07/17.
 * Email 741183142@qq.com
 */
public class SpeakerMainActivity extends Activity implements
        CompoundButton.OnCheckedChangeListener, View.OnClickListener, View.OnTouchListener {
    private SpeakerApi speakerApi;
    private Button btnSpeaker;
    private TextView tvChannel; //选择信道的spinner控件
    private AnimationDrawable animation; //显示对象
    private Button volSub, volAdd; //音量增减按键
    private Button btnYayin;
    private TextView tvVol; //显示音量
    private boolean use = false;
    private int vol = 8; //默认音量大小
    private Intent serviceIntent;
    private String[] channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_speaker);
        initTitle();
        speakerApi = SpeakerApi.getInstance(); //对讲机api
        initView();
        buttonUse(false);

    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            byte[] buf = (byte[]) msg.obj;
            messageManage(buf);
        }
    };

    //界面标题栏优化美化
    private void initTitle() {
        Window window = getWindow();
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(Color.rgb(39, 39, 37));
        ViewGroup mContentView =  findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
            ViewCompat.setFitsSystemWindows(mChildView, true);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vol_sub:
                if (vol > 1) {
                    vol--;
                    speakerApi.setVol(vol);
                    tvVol.setText(String.valueOf(vol));
                } else {
                    Toast.makeText(this, "当前为最低音量等级1", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case R.id.vol_add:
                if (vol < 9) {
                    vol++;
                    speakerApi.setVol(vol);
                    tvVol.setText(String.valueOf(vol));
                } else {
                    Toast.makeText(this, "当前为最高音量等级9", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case R.id.btn_yayin:
                if (App.currentChannel >= 8 && use) {
                    MoniDialog setMoni = new MoniDialog(speakerApi, this);
                    setMoni.setTitle("模拟设置");
                    setMoni.show();
                } else if (App.currentChannel < 8 && use) {
                    DigitDialog setDigit = new DigitDialog(speakerApi, this);
                    setDigit.setTitle("数字设置");
                    setDigit.show();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.toggleButton_power) {
            if (isChecked) { //开启
                speakerApi.openSerialPort();
                speakerApi.init(handler); //初始化并起线程
                App.currentChannel = speakerApi.readFunction();
                tvChannel.setText(channel[App.currentChannel]);
                speakerApi.changeChannels(App.currentChannel);
                buttonUse(true);

                serviceIntent = new Intent(SpeakerMainActivity.this, SpeakerService.class);
                startService(serviceIntent);
            } else { //关闭
                speakerApi.closePorts();
                buttonUse(false);
                stopService(serviceIntent);
                animation.setOneShot(true);
            }
        }
    }

    //界面初始化
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initView() {
        tvChannel = this.findViewById(R.id.sp_channel);
        //界面中间语音显示
        //显示接收语音的声音显示条
        ImageView imageView =findViewById(R.id.image_view);
        //设置动画背景
        //其中animation_list就是上一步准备的动画描述文件的资源名
        imageView.setBackgroundResource(R.drawable.animation_list);
        //获得动画对象
        animation = (AnimationDrawable) imageView.getBackground();
        //是否仅仅启动一次？
        animation.setOneShot(false);

        btnSpeaker = findViewById(R.id.btn_speaker_1);
        btnSpeaker.setOnTouchListener(this);
        btnSpeaker.setBackgroundResource(R.drawable.speakeroff);

        volSub = findViewById(R.id.vol_sub);
        volSub.setOnClickListener(this);
        volAdd = findViewById(R.id.vol_add);
        volAdd.setOnClickListener(this);
        btnYayin = findViewById(R.id.btn_yayin);
        btnYayin.setOnClickListener(this);
        tvVol = findViewById(R.id.tv_vol);
        tvVol.setText(String.valueOf(vol));
        ToggleButton powerBtn = findViewById(R.id.toggleButton_power);
        powerBtn.setOnCheckedChangeListener(this);

        //下拉列表
        channel = getResources().getStringArray(R.array.channel);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) { //结束发送
            LogUtils.d("cansal button ---> cancel");
            btnSpeaker.setBackgroundResource(R.drawable.speakeroff);
            animation.setOneShot(true);
            if (App.currentChannel < 8) { //数字信道语音发送结束
                speakerApi.finishSpeak(true);
            } else { //模拟信道语音发送结束
                speakerApi.finishSpeak(false);
            }
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) { //开始发送
            LogUtils.d("cansal button ---> down");
            btnSpeaker.setBackgroundResource(R.drawable.speakeron);
            animation.stop();
            animation.setOneShot(false);
            animation.start(); //启动
            if (App.currentChannel < 8) { //数字信道语音发送结束
                speakerApi.startSpeak(true);
            } else { //模拟信道语音发送结束
                speakerApi.startSpeak(false);
            }
        }
        return false;
    }


    private void buttonUse(boolean b) {
        use = b;
        volSub.setEnabled(b);
        volAdd.setEnabled(b);
        btnSpeaker.setEnabled(b);
        btnYayin.setEnabled(b);
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

    public int order7631(int btoi) {
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtils.d("cansal button ---> down" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_F3) {
            App.currentChannel = speakerApi.readFunction();
            tvChannel.setText(channel[App.currentChannel]);
            speakerApi.changeChannels(App.currentChannel);
        }
        return super.onKeyDown(keyCode, event);
    }
}
