package com.speedata.speakercheck.utils;

/**
 * Created by xu on 2017/3/17.
 */

public class Parts {

    //包头 1
    public static final String HEAD_0x68 = "68";

    //指令列表,其中大部分是在测试中调用,只使用模块默认的部分 1

    public static final String CMD_0x01 = "01"; //信道切换 
    public static final String CMD_0x02 = "02"; //接收音量设置 
    public static final String CMD_0x06 = "06"; //各种呼叫模式（呼叫类别）
    public static final String CMD_0x13 = "13"; //收发亚音频类型设置
    public static final String CMD_0x14 = "14"; //CTCSS/DCS 亚音设置
    public static final String CMD_0x25 = "25"; //发送软件版本号
    public static final String CMD_0x35 = "35"; //设置模拟组命令
    public static final String CMD_0x36 = "36"; //设置数字组命令
    public static final String CMD_0x37 = "37"; //查询模拟组设置命令
    public static final String CMD_0x38 = "38"; //查询数字组设置命令
    public static final String CMD_0x0b = "0b"; //Mic 增益配置


    //操作方式 1
    /*
    0x00：读；
    0x01：写；
    (外部 CPU 发为写，外部 CPU 收为读)
    0x02：主动发送
     */
    public static final String RW_0x00 = "00"; //读
    public static final String RW_0x01 = "01"; //写
    public static final String RW_0x02 = "02"; //主动发送

    //设置/回答指令
    /*
    设置：
    0x01：表示开始设置
     */
    public static final String SRS_0x01 = "01"; //表示开始设置

    /*
     回答：
    0x00 设置成功
    0x01 模块繁忙或者设置失败（注1）
    0x02 无此信道或信道错误（注2）
    0x07 模块被毙
    0x09 校验错误

     */
    public static final String SR_0x00 = "00"; //设置成功
    public static final String SR_0x01 = "01"; //模块繁忙或者设置失败（注1）
    public static final String SR_0x02 = "02"; //无此信道或信道错误（注2）
    public static final String SR_0x07 = "07"; //模块被毙
    public static final String SR_0x09 = "09"; //校验错误

    //检验和 2  CKSUM需要计算
    /*
    整个串口包数据校验和（注 3）  无数据则为 0x00,0x00
     */
    public static final String CKSUM_BEFORE = "0000"; //无数据的效验和

    //数据段长度 2  LEN
    /*
    DATA 数据段长度，若无数据段信息，则 LEN 值为0x00
     */
    public static final String LEN_0x00 = "0000"; //没有数据，长度为0

    //数据段信息 len    DATA
    /*

     */

    //包尾 1
    public static final String TAIL_0x10 = "10"; //包尾

}
