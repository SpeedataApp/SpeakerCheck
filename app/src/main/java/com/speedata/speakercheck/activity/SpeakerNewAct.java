package com.speedata.speakercheck.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedata.speakercheck.R;
import com.speedata.speakercheck.utils.Cmds;
import com.speedata.speakercheck.utils.SpeakerApi;

/**
 * //                            _ooOoo_
 * //                           o8888888o
 * //                           88" . "88
 * //                           (| -_- |)
 * //                            O\ = /O
 * //                        ____/`---'\____
 * //                      .   ' \\| |// `.
 * //                       / \\||| : |||// \
 * //                     / _||||| -:- |||||- \
 * //                       | | \\\ - /// | |
 * //                     | \_| ''\---/'' | |
 * //                      \ .-\__ `-` ___/-. /
 * //                   ___`. .' /--.--\ `. . __
 * //                ."" '< `.___\_<|>_/___.' >'"".
 * //               | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * //                 \ \ `-. \_ __\ /__ _/ .-` / /
 * //         ======`-.____`-.___\_____/___.-`____.-'======
 * //                            `=---='
 * //
 * //         .............................................
 * //                  佛祖镇楼                  BUG辟易
 *
 * @author :EchoXBR in  2019-07-16 17:25.
 * 功能描述:重构界面
 */
public class SpeakerNewAct extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    SpeakerApi speakerApi;
    int currentChannel;
    Button btnSpeaker;
    private TextView tvChannel; //选择信道的spinner控件
    private Cmds cmds; //封装了发送的命令的cmds,可用的为其中几项
    private ImageView imageView; //显示接收语音的声音显示条
    private AnimationDrawable animation; //显示对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_speaker);
        cmds = new Cmds();
        initTitle();
        speakerApi = SpeakerApi.getIntance(this); //对讲机api
        initView();
        speakerApi.init(); //初始化并起线程

        currentChannel = speakerApi.readFunction();
        tvChannel.setText(currentChannel + "");
        speakerApi.changeChannels(currentChannel);
    }

    //界面标题栏优化美化
    private void initTitle() {

        Window window = getWindow();
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(Color.rgb(39, 39, 37));
        ViewGroup mContentView = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
            ViewCompat.setFitsSystemWindows(mChildView, true);
        }

        btnSpeaker = (Button) findViewById(R.id.btn_speaker_1);

        btnSpeaker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { //结束发送
                    Log.d("test", "cansal button ---> cancel");
                    btnSpeaker.setBackgroundResource(R.drawable.speakeroff);
                    animation.setOneShot(true);
                    if (currentChannel < 8) { //数字信道语音发送结束
                        speakerApi.startSpeak(false);
                        //                        speakerApi.startSpeak(true);
                    } else { //模拟信道语音发送结束
                        speakerApi.startSpeak(true);
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { //开始发送
                    Log.d("test", "cansal button ---> down");
                    btnSpeaker.setBackgroundResource(R.drawable.speakeron);
                    animation.stop();
                    animation.setOneShot(false);
                    animation.start(); //启动
                    if (currentChannel < 8) { //数字信道语音发送结束
                        speakerApi.startSpeak(false);
                        //                        speakerApi.startSpeak(true);
                    } else { //模拟信道语音发送结束
                        speakerApi.startSpeak(true);
                    }
                }

                return false;
            }
        });
        btnSpeaker.setBackgroundResource(R.drawable.speakeroff);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    //界面初始化
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initView() {
        tvChannel = this.findViewById(R.id.sp_channel);
        //界面中间语音显示
        imageView = (ImageView) findViewById(R.id.image_view);
        //设置动画背景
        imageView.setBackgroundResource(R.drawable.animation_list); //其中animation_list就是上一步准备的动画描述文件的资源名
        //获得动画对象
        animation = (AnimationDrawable) imageView.getBackground();
        //是否仅仅启动一次？
        animation.setOneShot(false);
    }

}
