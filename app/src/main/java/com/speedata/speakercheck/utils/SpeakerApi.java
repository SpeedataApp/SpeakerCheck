package com.speedata.speakercheck.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.serialport.DeviceControlSpd;
import android.serialport.SerialPortSpd;
import android.util.Log;

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
    private static final byte[] lock = new byte[0];
    private SerialPortSpd IDDev; //设备控制
    private int IDFd; //用于设备控制
    private DeviceControlSpd DevCtrl; //GPIO控制
    private DeviceControlSpd DevCtrl76;
    private byte[] cardtemp = null;
    private Cmds cmds = new Cmds(); //封装了发送的命令的cmds,可用的为其中几项
    private Handler handler;

    private SpeakerApi() {

    }

    private static class Holder {
        private static SpeakerApi api = new SpeakerApi();
    }

    public static SpeakerApi getInstance() {
        return Holder.api;
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
        try {
            DevCtrl = new DeviceControlSpd(DeviceControlSpd.PowerType.NEW_MAIN, 0, 12, 75);
            DevCtrl76 = new DeviceControlSpd(DeviceControlSpd.PowerType.NEW_MAIN, 76);

            DevCtrl.PowerOnDevice();
            DevCtrl76.PowerOffDevice();
            LogUtils.d("DevCtrl is open DevCtrl = " + DevCtrl);
        } catch (IOException e) {
            LogUtils.e(e.toString());
        }
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


    //根据spinner的选择切换信道,从1到16个数字和模拟的信道
    public void changeChannels(int position) { //输入的是0-15的spinner选择结果
        //得到的类似"00"的16位信道字符串
        String channel16 = getchannel(position);
        cardtemp = cmds.changeChannel(channel16);
        if (IDDev == null) {
            return;
        }
        Integer integer = valueOf(channel16, 16);
        if (integer < 9) {
            cpsChannel(channel16);
        } else {
            IDDev.WriteSerialByte(IDFd, cardtemp);
        }
    }

    //初始上电后,根据记录的选择结果切换信道
    public void initChannels(String channel) {
        cardtemp = cmds.changeChannel(channel);
        IDDev.WriteSerialByte(IDFd, cardtemp);
    }

    //为信道选择处理成字符串型data
    private String getchannel(int select) { //从spinner的选项,对应所需发送的命令
        channelRemember = String.format("%02x", select + 1);
        return channelRemember;
    }

    //开始发送语音,需要区分模拟信道还是数字信道
    public void startSpeak(boolean digit) {
        if (digit) {
            cardtemp = cmds.voiceCall("01", "04ffffff");
            IDDev.WriteSerialByte(IDFd, cardtemp);
        } else {
            cardtemp = cmds.voiceCall("01", "00000000");
            IDDev.WriteSerialByte(IDFd, cardtemp);
        }
    }

    //结束发送语音
    public void finishSpeak(boolean digit) {
        if (digit) {
            cardtemp = cmds.voiceCall("ff", "04ffffff");
            IDDev.WriteSerialByte(IDFd, cardtemp);
        } else {
            cardtemp = cmds.voiceCall("ff", "00000000");
            IDDev.WriteSerialByte(IDFd, cardtemp);
        }
    }

    //音量设置
    public void setVol(int vol) { //vol只有1到9
        cardtemp = cmds.volumeSetting("0" + vol);
        IDDev.WriteSerialByte(IDFd, cardtemp);
    }


    //初始化
    @SuppressLint("HandlerLeak")
    public void init(Handler handler) {
        ReadThread reader = new ReadThread();
        reader.start();
        this.handler = handler;
    }

    private class ReadThread extends Thread { //读取反馈的线程
        @Override
        public void run() {
            super.run();
            LogUtils.d("thread start");
            while (!isInterrupted()) {
                Message msg = new Message();
                byte[] buf;
                try {
                    buf = IDDev.ReadSerial(IDFd, 128);
                    LogUtils.d("thread start buf");
                } catch (IOException e) {
                    LogUtils.e("ReadSerial error");
                    return;
                }
                if (buf != null) {
                    if ("44c6b5b5c0313b0d0a".equals(byteArrayToString(buf))) {
                        Integer integer = valueOf(channelRemember, 16);
                        if (integer < 11) {
                            cpsChannel(channelRemember);
                        } else {
                            cardtemp = cmds.changeChannel(channelRemember);
                            IDDev.WriteSerialByte(IDFd, cardtemp);
                        }

                    } else if ((btoi(buf[0]) == 0x68) && (btoi(buf[buf.length - 1]) == 0x10)) {
                        LogUtils.d("read end");
                        msg.what = 1;
                        msg.obj = buf;
                        handler.sendMessage(msg);
                    }

                }
            }
        }
    }


    public static int btoi(byte a) {
        return (a < 0 ? a + 256 : a);
    }


    public static CharSequence basicOrder(int btoi) {
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
        LogUtils.d("readEm55state: " + state);
        return state;
    }


    private void cpsChannel(String channel) {
        switch (channel) {
            case "01":
                setChannel("433375000", "433375000", "888", "1", "8", "1");
                break;
            case "02":
                setChannel("437225000", "437225000", "888", "1", "8", "1");
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
                setChannel("40602500", "40602500", "888", "1", "8", "1");
                break;
            case "07":
                setChannel("40702500", "40702500", "888", "1", "8", "1");
                break;
            case "08":
                setChannel("40802500", "40802500", "888", "1", "8", "1");
                break;
            case "09":
                setChannel("40902500", "40902500", "100", "100");
                break;
            case "10":
                setChannel("41002500", "41002500", "67", "67");
                break;
            case "11":
                setChannel("41102500", "41102500", "67", "67");
                break;
            case "12":
                setChannel("41202500", "41202500", "67", "67");
                break;
            case "13":
                setChannel("41302500", "41302500", "67", "67");
                break;
            case "14":
                setChannel("41402500", "41402500", "67", "67");
                break;
            case "15":
                setChannel("41502500", "41502500", "67", "67");
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
