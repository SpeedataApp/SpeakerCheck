package com.speedata.speakercheck.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.speedata.speakercheck.R;


/**
 * Created by 张明_ on 2018/12/17.
 * Email 741183142@qq.com
 */
public class WaveViewDialog extends Dialog {

    private ImageView imageView;
    private AnimationDrawable animation; //显示对象

    public WaveViewDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_voice);
        //界面中间语音显示
        imageView = (ImageView) findViewById(R.id.image_view);
        //设置动画背景
        //其中animation_list就是上一步准备的动画描述文件的资源名
        imageView.setBackgroundResource(R.drawable.animation_list);
        //获得动画对象
        animation = (AnimationDrawable) imageView.getBackground();
        //是否仅仅启动一次？
        animation.setOneShot(false);


        animation.stop();
        animation.setOneShot(false);
        animation.start(); //启动
    }

    @Override
    protected void onStop() {
        super.onStop();
        animation.setOneShot(true);
    }
}
