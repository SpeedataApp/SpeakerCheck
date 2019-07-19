package com.speedata.speakercheck.activity.speakermain;

import android.content.Context;

import com.speedata.speakercheck.activity.mvp.BasePresenter;
import com.speedata.speakercheck.activity.mvp.BaseView;

/**
 * MVPPlugin
 *  邮箱 784787081@qq.com
 */

public class SpeakerMainContract {
    interface View extends BaseView {
        
    }

    interface  Presenter extends BasePresenter<View> {
        
    }
}
