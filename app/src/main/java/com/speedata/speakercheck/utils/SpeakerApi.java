package com.speedata.speakercheck.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.serialport.DeviceControlSpd;
import android.serialport.SerialPortSpd;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static com.speedata.speakercheck.utils.DataConversionUtils.byteArrayToString;
import static java.lang.Integer.valueOf;


/**
 * Created by xu on 2017/3/21.
 * 此为对讲机的api接口,可以调用对应方法.
 * 此方法暂时弃用
 */

public class SpeakerApi {
    public String channelRemember = "01"; //记录选择的信道,上电后直接开始
    private static final String TAG = "Speaker_DEV"; //测试用的TAG
    private static final String SERIALPORT_PATH = "/dev/ttyMT0"; //path
    public static SpeakerApi api;
    private static byte[] lock = new byte[0];
    public SerialPortSpd IDDev; //设备控制
    public int IDFd; //用于设备控制
    private String channel16; //得到的类似"00"的16位信道字符串
    private DeviceControlSpd DevCtrl; //GPIO控制
    private DeviceControlSpd DevCtrl76;
    private byte[] cardtemp = null;
    private Cmds cmds = new Cmds(); //封装了发送的命令的cmds,可用的为其中几项
    private Context mContext;

    private SpeakerApi(Context mContext) {
        this.mContext = mContext;
    }


    public static SpeakerApi getIntance(Context mContext) {
        synchronized (lock) {
            if (api == null) {
                api = new SpeakerApi(mContext);
            }
            return api;
        }
    }

    //初始化时打开设备控制,设备控制的文件路径
    public void openSerialPort() {
        LogUtils.d("id_init is called");
        IDDev = new SerialPortSpd();
        try {
            IDDev.OpenSerial(SERIALPORT_PATH, 57600);
            IDFd = IDDev.getFd();
            LogUtils.d("SerialPort is open IDFd = " + IDFd);
        } catch (IOException e) {
            LogUtils.e("open serial error");
            return;
        }
        SystemClock.sleep(30);
        try {
            DevCtrl = new DeviceControlSpd(DeviceControlSpd.PowerType.NEW_MAIN, 0, 12, 75);
            DevCtrl76 = new DeviceControlSpd(DeviceControlSpd.PowerType.NEW_MAIN, 76);

            DevCtrl.PowerOnDevice();
            DevCtrl76.PowerOffDevice();
            LogUtils.d("DevCtrl is open DevCtrl = " + DevCtrl);
        } catch (IOException e) {
            LogUtils.e(e.toString());
            return;
        }
        SystemClock.sleep(30);
    }

    //退出程序时关闭设备控制
    public void closePorts() {
        if (IDDev != null) {
            LogUtils.d("close serial port");
            IDDev.CloseSerial(IDFd);
            IDFd = 0;
        }
        if (DevCtrl != null) {
            LogUtils.d("close dev power");
            try {
                DevCtrl.PowerOffDevice();
                DevCtrl76.PowerOnDevice();
            } catch (IOException e) {
                LogUtils.e("close power error");
            }
        }
    }


    //读取模块返回的信息
    public byte[] readSerial() {
        byte[] buf;
        try {
            buf = IDDev.ReadSerial(IDFd, 128);
            Log.d(TAG, "thread start buf");
        } catch (IOException e) {
            Log.e(TAG, "ReadSerial error");
            return null;
        }
        return buf;
    }

    //打开关闭对讲机按钮(上下电)
    public void power(boolean on) {
        if (on) {
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
        } else {
            Log.i(TAG, "powerBtn is called off");
            try {
                DevCtrl.PowerOffDevice();
            } catch (IOException e) {
            }
        }
    }

    //根据spinner的选择切换信道,从1到16个数字和模拟的信道
    public String changeChannels(int position) { //输入的是0-15的spinner选择结果
        channel16 = getchannel(position);
        cardtemp = cmds.changeChannel(channel16);
        if (IDDev == null) {
            return null;
        }
        if ("01".equals(channel16) || "02".equals(channel16) || "03".equals(channel16) || "04".equals(channel16)
                || "05".equals(channel16) || "06".equals(channel16) || "07".equals(channel16) || "08".equals(channel16)) {
            cpsChannel(channel16);
        } else {
            IDDev.WriteSerialByte(IDFd, cardtemp);
        }
        return channel16;
    }

    //初始上电后,根据记录的选择结果切换信道
    public void initChannels(String channel) {
        cardtemp = cmds.changeChannel(channel);
        IDDev.WriteSerialByte(IDFd, cardtemp);
    }

    //为信道选择处理成字符串型data
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

    //开始发送语音,需要区分模拟信道还是数字信道
    public void startSpeak(boolean digit) {
        if (digit) {
            cardtemp = cmds.voiceCall("ff", "04ffffff");
            IDDev.WriteSerialByte(IDFd, cardtemp);
        } else {
            cardtemp = cmds.voiceCall("ff", "00000000");
            IDDev.WriteSerialByte(IDFd, cardtemp);
        }
    }

    //结束发送语音
    public void finishSpeak(boolean digit) {
        if (digit) {
            cardtemp = cmds.voiceCall("01", "04ffffff");
            IDDev.WriteSerialByte(IDFd, cardtemp);
        } else {
            cardtemp = cmds.voiceCall("01", "00000000");
            IDDev.WriteSerialByte(IDFd, cardtemp);
        }
    }

    //音量设置
    public void setVol(int vol) { //vol只有1到9
        cardtemp = cmds.volumeSetting("0" + vol);
        IDDev.WriteSerialByte(IDFd, cardtemp);
    }


    //处理对讲模块反馈的信息
    private void messageManage(byte[] buf) {
        int voice = 0;
        switch (btoi(buf[1])) {

            case 0x06://语音呼叫，根据流程来设置反馈
                if (btoi(buf[2]) == 0x02) {
                    voice = order7631(btoi(buf[3]));
                }

                BusMessage busMessage = new BusMessage();
                busMessage.setCode(voice);
                EventBus.getDefault().post(voice);
//                if (voice == 1) {
//                    animation.stop(); //停止
//                    animation.setOneShot(false);
//                    animation.start(); //启动
//                } else if (voice == 0) {
//                    animation.setOneShot(true);
//                }
                break;

            case 0x01:
                Toast.makeText(mContext, "信道切换：" + basicOrder(btoi(buf[3])), Toast.LENGTH_SHORT).show();
                break;

            case 0x35:
                Toast.makeText(mContext, "模拟信道：" + basicOrder(btoi(buf[3])), Toast.LENGTH_SHORT).show();
                break;

            case 0x36:
                Toast.makeText(mContext, "数字信道：" + basicOrder(btoi(buf[3])), Toast.LENGTH_SHORT).show();
                break;

            case 0x0b:
                Toast.makeText(mContext, "MIC 增益：" + basicOrder(btoi(buf[3])), Toast.LENGTH_SHORT).show();
                break;

            default:
                break;

        }

    }

    ReadThread reader;
    Handler handler;

    //初始化
    @SuppressLint("HandlerLeak")
    public void init() {
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


    private int order7631(int btoi) {
        switch (btoi) {
            //语音呼叫反馈包
            //语音接收开始/结束串口包
            case 0x6f: //"语音接收结束"
                Toast.makeText(mContext, "语音接收结束", Toast.LENGTH_SHORT).show();
                return 0;
            case 0x60: //"语音接收开始"
                Toast.makeText(mContext, "语音接收开始", Toast.LENGTH_SHORT).show();
                return 1;
            default:
                break;
        }
        return 0;
    }

    private static int btoi(byte a) {
        return (a < 0 ? a + 256 : a);
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

    public int readFunction() {
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

    public void writeSerialByte(byte[] bytes) {
        IDDev.WriteSerialByte(IDFd, bytes);
    }
}
