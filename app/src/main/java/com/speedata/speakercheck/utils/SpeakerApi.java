package com.speedata.speakercheck.utils;

import android.serialport.DeviceControl;
import android.serialport.SerialPort;
import android.util.Log;

import java.io.IOException;


/**
 * Created by xu on 2017/3/21.
 * 此为对讲机的api接口,可以调用对应方法.
 * 此方法暂时弃用
 */

public class SpeakerApi {

    private String channel16; //得到的类似"00"的16位信道字符串
    private static final String TAG = "Speaker_DEV"; //测试用的TAG
    public SerialPort IDDev; //设备控制
    private DeviceControl DevCtrl; //GPIO控制
    private static final String SERIALPORT_PATH = "/dev/ttyMT1"; //path
    public int IDFd; //用于设备控制
    private byte[] cardtemp = null;
    private Cmds cmds = new Cmds(); //封装了发送的命令的cmds,可用的为其中几项


    //初始化时打开设备控制,设备控制的文件路径
    public void openSerialPort() {
        Log.i(TAG, "id_init is called");
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
    }

    //退出程序时关闭设备控制
    public void closePorts() {
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
        }else {
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
        IDDev.WriteSerialByte(IDFd, cardtemp);
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
                return "01";

            case 1:
                return "02";

            case 2:
                return "03";

            case 3:
                return "04";

            case 4:
                return "05";

            case 5:
                return "06";

            case 6:
                return "07";

            case 7:
                return "08";

            case 8:
                return "09";

            case 9:
                return "0a";

            case 10:
                return "0b";

            case 11:
                return "0c";

            case 12:
                return "0d";

            case 13:
                return "0e";

            case 14:
                return "0f";

            case 15:
                return "10";

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
    public void setVol(int vol){ //vol只有1到9
        cardtemp = cmds.volumeSetting("0" + vol);
        IDDev.WriteSerialByte(IDFd, cardtemp);
    }
}
