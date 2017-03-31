package com.speedata.speakercheck.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.serialport.DeviceControl;
import android.serialport.SerialPort;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.speedata.speakercheck.R;
import com.speedata.speakercheck.activity.SpeakerActivity;
import com.speedata.speakercheck.dialogs.DigitSettingsDialog;
import com.speedata.speakercheck.dialogs.SettingsDialog;
import com.speedata.speakercheck.utils.Cmds;

import java.io.IOException;
import java.util.Timer;

import static com.speedata.speakercheck.utils.DataConversionUtils.byteArrayToInt;
import static com.speedata.speakercheck.utils.DataConversionUtils.byteArrayToString;

/**
 * 此界面是在调校测试对讲机功能时开发使用的,也可以参考此页面的配置信息
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Button setMoni;
    private Button setShuzi;
    private Button volSub;
    private Button volAdd;
    private Button btnQueryMoni;
    private Button btnQueryShuzi;
    private ToggleButton speakerBtn;
    private EditText showView;
    
    //输入法管理器
    protected InputMethodManager mimm = null;
    private static final String TAG = "Speaker_DEV";
    Timer timer;
    public SerialPort IDDev;
    private DeviceControl DevCtrl;
    private static final String SERIALPORT_PATH = "/dev/ttyMT1";
    public int IDFd;
    public int i;
    private ReadThread reader;
    private Handler handler;
    private SettingsDialog setPBP;
    private DigitSettingsDialog setDigit;
    
    private byte[] idversion = new byte[18];
    private byte[] receivingfrequency = new byte[4];
    private byte[] emissionfrequency = new byte[4];

    //数字组所需
    private byte[] nativeid = new byte[4];
    private byte[] contactnumber = new byte[4];
    private byte[] secretkey = new byte[8];
    private byte[] receivegrouplist = new byte[128];
    private byte[] receivelist = new byte[4];

    private Cmds cmds;
    private int vol = 8;
    public String mingling;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();
        Log.i(TAG, "onCreate is called");
    }

    private void initView() {
        //输入法管理
        mimm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        cmds = new Cmds();
        showView = (EditText) findViewById(R.id.displaywindow_et);

        setPBP = new SettingsDialog(this, this);
        setDigit = new DigitSettingsDialog(this, this);

        setMoni = (Button) findViewById(R.id.set_moni_btn);
        setMoni.setOnClickListener(this);
        setShuzi = (Button) findViewById(R.id.set_shuzi_btn);
        setShuzi.setOnClickListener(this);
        btnQueryMoni = (Button) findViewById(R.id.query_moni_btn);
        btnQueryMoni.setOnClickListener(this);
        btnQueryShuzi = (Button) findViewById(R.id.query_shuzi_btn);
        btnQueryShuzi.setOnClickListener(this);
        Button btnOut = (Button) findViewById(R.id.back_btn);
        btnOut.setOnClickListener(this);
        Button btnClean = (Button) findViewById(R.id.clean_window);
        btnClean.setOnClickListener(this);
        Button btnTest = (Button) findViewById(R.id.test);
        btnTest.setOnClickListener(this);
        Button btnNew = (Button) findViewById(R.id.new_speaker);
        btnNew.setOnClickListener(this);

        volSub = (Button) findViewById(R.id.vol_sub);
        volSub.setOnClickListener(this);
        volAdd = (Button) findViewById(R.id.vol_add);
        volAdd.setOnClickListener(this);

        Button btnVersion = (Button) findViewById(R.id.btn_version);
        btnVersion.setOnClickListener(this);

        ToggleButton powerBtn = (ToggleButton) findViewById(R.id.toggleButton_power);
        powerBtn.setOnCheckedChangeListener(this);
        speakerBtn = (ToggleButton) findViewById(R.id.toggleButton_speaker);
        speakerBtn.setOnCheckedChangeListener(this);

        buttonUseless();
    
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }



    @Override
    public void onClick(View view) {
        byte[] cardtemp = null;
        switch (view.getId()) {
            case R.id.set_moni_btn:
                setPBP.setTitle(R.string.sure);
                setPBP.show();
                break;

            case R.id.set_shuzi_btn:
                setDigit.setTitle(R.string.sure);
                setDigit.show();
                break;

            case R.id.query_moni_btn:
                cardtemp = cmds.queryGroup("01");
                showView.append(byteArrayToString(cardtemp) + "\n");
                IDDev.WriteSerialByte(IDFd, cardtemp);
                break;

            case R.id.query_shuzi_btn:
                cardtemp = cmds.queryGroupSettings("01");
                showView.append(byteArrayToString(cardtemp) + "\n");
                IDDev.WriteSerialByte(IDFd, cardtemp);
                break;

            case R.id.vol_sub:
                if (vol > 1) {
                    vol--;
                    cardtemp = cmds.volumeSetting("0" + vol);
                    showView.append(byteArrayToString(cardtemp) + "\n");
                    IDDev.WriteSerialByte(IDFd, cardtemp);
                } else {
                    showView.append("当前为最低音量等级1，不能在降低音量了" + "\n");
                    return;
                }

                break;
            case R.id.vol_add:
                if (vol < 9) {
                    vol++;
                    cardtemp = cmds.volumeSetting("0" + vol);
                    showView.append(byteArrayToString(cardtemp) + "\n");
                    IDDev.WriteSerialByte(IDFd, cardtemp);
                } else {
                    showView.append("当前为最高音量等级9，不能在提升音量了" + "\n");
                    return;
                }
                break;

            case R.id.clean_window:
                showView.setText("");
                break;

            case R.id.test:
                cardtemp = cmds.changeChannel("09");
                IDDev.WriteSerialByte(IDFd, cardtemp);
                break;

            case R.id.new_speaker:
                Intent intent = new Intent(MainActivity.this, SpeakerActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_version:
                cardtemp = cmds.querySoftwareVersion("01");
                IDDev.WriteSerialByte(IDFd, cardtemp);
                break;

            case R.id.back_btn:
                closePort();
                finish();
                break;
        }
    }

    private void buttonUseless() {
        speakerBtn.setEnabled(false);
        volSub.setEnabled(false);
        volAdd.setEnabled(false);
        setShuzi.setEnabled(false);
        setMoni.setEnabled(false);
        btnQueryMoni.setEnabled(false);
        btnQueryShuzi.setEnabled(false);

    }
    private void buttonUse() {
        speakerBtn.setEnabled(true);
        volSub.setEnabled(true);
        volAdd.setEnabled(true);
        setShuzi.setEnabled(true);
        setMoni.setEnabled(true);
        btnQueryMoni.setEnabled(true);
        btnQueryShuzi.setEnabled(true);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        byte[] cardtemp = null;
            switch (compoundButton.getId()) {
                case R.id.toggleButton_power:
                if (b) {
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

                    buttonUse();
                } else {
                    buttonUseless();
                
                    Log.i(TAG, "powerBtn is called off");
                    if (timer != null) {
                        timer.cancel();
                    } try {
                        DevCtrl.PowerOffDevice();
                    } catch (IOException e) {
                        showView.append(getString(R.string.Status_ManipulateFail) + "\n");

                    }
                }
                break;

            case R.id.toggleButton_speaker:
                if (b) { //开始语音发送
                    cardtemp = cmds.voiceCall("01", "00000000");
                    showView.append(byteArrayToString(cardtemp) + "\n");
                    IDDev.WriteSerialByte(IDFd, cardtemp);
                } else { //结束语音发送
                    cardtemp = cmds.voiceCall("ff", "00000000");
                    showView.append(byteArrayToString(cardtemp) + "\n");
                    IDDev.WriteSerialByte(IDFd, cardtemp);
                }
                break;
        }
        
    }



    public void onDestroy() {

        super.onDestroy();
        Log.i(TAG, "onDestory is called");
        closePort();
    }
    private void closePort() {
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
        if (timer != null) {
            timer.cancel();
        }
        if (reader != null) {
            reader.interrupt();
        }
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
        try   {
            Thread.currentThread();
            Thread.sleep(30);
        } catch (InterruptedException   e) {
        }
        reader = new ReadThread();
        reader.start();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    Log.i(TAG, "handler is called");
                    byte[] buf = (byte[]) msg.obj;

                    if ((btoi(buf[0]) == 0x68) && (btoi(buf[buf.length - 1]) == 0x10)) {
                        messageManage(buf);
                    } else {
                        showView.append(byteArrayToString(buf) + "\n");
                    }
                }
            }
        };
    }

    private void messageManage(byte[] buf) {
        byte[] cardtemp = null;
        switch (btoi(buf[1])) {
            case 0x01:
                showView.append(basicOrder(btoi(buf[3])) + "\n");
                break;
            case 0x02:
                showView.append(basicOrder(btoi(buf[3])) + "\n");
                if (btoi(buf[3]) == 0x00)
                    showView.append("当前音量为" + vol + "\n");
                break;
            case 0x06://语音呼叫，根据流程来设置反馈
                showView.append(byteArrayToString(buf) + "\n");
                if (btoi(buf[2]) == 0x00) {
                    if (btoi(buf[3]) == 0x00) {
                        showView.append("接收数据OK\n");
                        showView.append("语音发送中\n");
                    } else {
                        showView.append("效验错误\n");
                        //效验错误，再次发送语音开始命令
                        cardtemp = cmds.voiceCall("01", "00000000");
                        showView.append(byteArrayToString(cardtemp) + "\n");
                        IDDev.WriteSerialByte(IDFd, cardtemp);
                    }
                } else if (btoi(buf[2]) == 0x02) {
                    showView.append(order7631(btoi(buf[3])) + "\n");
                }
                showView.append("语音反馈：" + byteArrayToString(buf) + "\n");
                break;
            case 0x25:
                showView.append(basicOrder2(btoi(buf[3])) + "\n");
                System.arraycopy(buf, 8, idversion, 0, 18);
                showView.append(byteArrayToString(buf) + "\n");
                break;
            case 0x35:
                showView.append(basicOrder(btoi(buf[3])) + "\n");
                showView.append(byteArrayToString(buf) + "\n");
                break;
            case 0x36:
                showView.append(mingling + "\n");
                showView.append(basicOrder(btoi(buf[3])) + "\n");
                showView.append(byteArrayToString(buf) + "\n");
                break;
            case 0x37://查询模拟组设置命令反馈包
                if (btoi(buf[3]) == 0x00) {
                    showView.append(byteArrayToString(buf) + "\n");
                    showView.append("带宽:" + order371(btoi(buf[8])) + "\n");
                    showView.append("功率: " + order372(btoi(buf[9])) + "\n");
                    System.arraycopy(buf, 10, receivingfrequency, 0, 4);
                    showView.append("接收频率: " + byteArrayToInt(receivingfrequency, false) + "\n");
                    System.arraycopy(buf, 14, emissionfrequency, 0, 4);
                    showView.append("发射频率: " + byteArrayToInt(emissionfrequency, false) + "\n");
                    showView.append("静噪等级: " + order373(btoi(buf[18])) + "\n");
                    showView.append("接收亚音类型: " + order374(btoi(buf[19])) + "\n");
                    String str = btoi(buf[20]) + "";
                    showView.append("接收亚音频率: " + Integer.parseInt(str.substring(str.length() - 1, str.length()), 16) + "\n");
                    showView.append("发射亚音类型: " + order374(btoi(buf[21])) + "\n");
                    String str2 = btoi(buf[22]) + "";
                    showView.append("发射亚音频率: " + Integer.parseInt(str2.substring(str2.length() - 1, str2.length()), 16) + "\n");
                } else {
                    showView.append(basicOrder(btoi(buf[3])) + "\n");
                    showView.append(byteArrayToString(buf) + "\n");
                }
                break;
            case 0x38:
                showView.append(byteArrayToString(buf) + "\n");
                showView.append("功率: " + order372(btoi(buf[8])) + "\n");
                System.arraycopy(buf, 9, receivingfrequency, 0, 4);
                showView.append("接收频率: " + byteArrayToInt(receivingfrequency, false) + "\n");
                System.arraycopy(buf, 13, emissionfrequency, 0, 4);
                showView.append("发送频率: " + byteArrayToInt(emissionfrequency, false) + "\n");
                System.arraycopy(buf, 17, nativeid, 0, 4);
                showView.append("本机 ID：" + byteArrayToInt(nativeid, true) + "\n");
                String str3 = btoi(buf[21]) + "";
                showView.append("色码：" + Integer.parseInt(str3.substring(str3.length() - 1, str3.length()), 16) + "\n");
                showView.append(order7632(btoi(buf[22])) + "\n");
                System.arraycopy(buf, 23, contactnumber, 0, 4);
                showView.append("联系人号码：" + byteArrayToInt(contactnumber, true) + "\n");
                showView.append("加密开关：" + order375(btoi(buf[27])) + "\n");
                System.arraycopy(buf, 28, secretkey, 0, 8);
                showView.append("秘钥：" + byteArrayToInt(secretkey, true) + "\n");
                System.arraycopy(buf, 36, receivegrouplist, 0, 128);
                for (int i = 0; i < 32; i++) {
                    int a = 4 * i + 36;
                    System.arraycopy(buf, a, receivelist, 0, 4);
                    int b = byteArrayToInt(receivelist, true);
                    if (b != 0) {
                        showView.append("接收组：" + b + "\n");
                    } else {
                      return;
                    }
                }
                break;

        }

    }


    private String order375(int btoi) {
        switch (btoi) {
            case 0x01:
                return "开";

            case 0xff:
                return "关";

        }
        return "未知错误";
    }

    private String order374(int btoi) {
        switch (btoi) {
            case 0x01:
                return "载波";

            case 0x02:
                return "CTCSS";

            case 0x03:
                return "正向 CDCSS";

            case 0x04:
                return "反向 CDCSS";

        }
        return "未知错误";
    }

    private String order373(int btoi) {
        switch (btoi) {
            case 0x00:
                return "0，长接收";

            case 0x06:
                return "6";

            case 0x07:
                return "7";

            case 0x08:
                return "8";

            case 0x09:
                return "9";

            case 0x01:
                return "1";

            case 0x02:
                return "2";

            case 0x03:
                return "3";

            case 0x04:
                return "4";

            case 0x05:
                return "5";

        }
        return "未知错误";
    }

    private String order372(int btoi) {
        switch (btoi) {
            case 0x00:
                return "低功率 ";

            case 0x01:
                return "高功率";

        }
        return "未知错误";
    }

    private String order371(int btoi) {
        switch (btoi) {
            case 0x00:
                return "窄带 ";

            case 0x80:
                return "宽带";

        }
        return "未知错误";
    }


    private String order7632(int btoi) {
        switch (btoi) {
            case 0x01:
                return "PATCS 个呼";

            case 0x02:
                return "组呼";

            case 0x03:
                return "无地址呼";

            case 0x04:
                return "全呼和广播";

        }
        return "未知错误";
    }

    private String order7631(int btoi) {
        switch (btoi) {
            //语音呼叫反馈包
            case 0x62:
                return "发送结束";

            case 0x6e:
                return "超时反馈";

            case 0x6d:
                return "拒绝发送";

            case 0x6c:
                return "BS 激活超时";

            case 0x6f:
                return "语音接收结束";

            //语音接收开始/结束串口包
            case 0x61:
                return "发送成功";

            case 0x60:
                return "语音接收开始";


        }
        return "未知错误";
    }



    private String basicOrder2(int btoi) {
        switch (btoi) {
            case 0x00:
                return "成功";

            case 0x01:
                return "模块繁忙或者设置失败";

            case 0x02:
                return "无此信道或信道错误";

            case 0x07:
                return "模块被毙";

            case 0x09:
                return "校验错误";

        }
        return "未知错误";
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

        }
                return "未知错误";
    }

    private static int btoi(byte a) {
        return (a < 0 ? a + 256 : a);
    }


    private class ReadThread extends Thread {
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

}
