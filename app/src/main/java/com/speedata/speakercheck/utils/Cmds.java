package com.speedata.speakercheck.utils;

import static com.speedata.speakercheck.utils.DataConversionUtils.HexString2Bytes;
import static com.speedata.speakercheck.utils.Parts.CKSUM_BEFORE;
import static com.speedata.speakercheck.utils.Parts.CMD_0x01;
import static com.speedata.speakercheck.utils.Parts.CMD_0x02;
import static com.speedata.speakercheck.utils.Parts.CMD_0x06;
import static com.speedata.speakercheck.utils.Parts.CMD_0x0b;
import static com.speedata.speakercheck.utils.Parts.CMD_0x13;
import static com.speedata.speakercheck.utils.Parts.CMD_0x14;
import static com.speedata.speakercheck.utils.Parts.CMD_0x25;
import static com.speedata.speakercheck.utils.Parts.CMD_0x35;
import static com.speedata.speakercheck.utils.Parts.CMD_0x36;
import static com.speedata.speakercheck.utils.Parts.CMD_0x37;
import static com.speedata.speakercheck.utils.Parts.CMD_0x38;
import static com.speedata.speakercheck.utils.Parts.HEAD_0x68;
import static com.speedata.speakercheck.utils.Parts.RW_0x01;
import static com.speedata.speakercheck.utils.Parts.SRS_0x01;
import static com.speedata.speakercheck.utils.Parts.TAIL_0x10;

/**
 * Created by xu on 2017/3/17.
 * 程序只采用了模块默认的信道,其中大部分指令在测试中调用
 */

public class Cmds {

    //计算效验和
    //对讲机效验和计算
    private  short PcCheckSum(byte[] buf) {
        long sum = 0;
        int len = buf.length;
        int i = 0;
        while (len > 1) {
            sum += (0xFFFF & (btoi(buf[i]) << 8 | btoi(buf[i + 1])));
            i += 2;
            len -= 2;
        } if (len > 0) {
            sum += (0xFF & buf[i]) << 8;
        }
        sum &= 0xFFFFFFFF;
        while ((sum >>> 16) > 0) {
            sum = (sum & 0xFFFF) + (sum >>> 16);
        }
        return (short)((short) sum ^ 0xFFFF);
    }

    private int btoi(byte a) {
        return (a < 0 ? a + 256 : a);
    }


    //填入0000的命令,通过此方法计算效验和后的完整指令
    private String checkSum(String input) {
        String str = Integer.toHexString(PcCheckSum(HexString2Bytes(input)));
        if(str.length() < 4) {
            for (int i = 0; i < 4; i ++) {
                str = "0" + str;
                if (str.length() == 4){
                    return str.substring(str.length() - 4);
                }
            }
        }
        return str.substring(str.length() - 4);
    }

//-----------------------------------------------------------------------

    //信道切换

    public byte[] changeChannel(String number) { // DATA：范围1到16，对应16个信道

        String cc1 = HEAD_0x68 + CMD_0x01 + RW_0x01 + SRS_0x01 + CKSUM_BEFORE + "0001" + number + TAIL_0x10;

        String cc = HEAD_0x68 + CMD_0x01 + RW_0x01 + SRS_0x01 + checkSum(cc1) + "0001" + number + TAIL_0x10;

        byte[] ccbyte = HexString2Bytes(cc); //这是要发送的串口指令

        return ccbyte;

    }

    //接受音量设置
    public byte[] volumeSetting(String number) { //DATA:从1到9，默认8

        String vs1 = HEAD_0x68 + CMD_0x02 + RW_0x01 + SRS_0x01 + CKSUM_BEFORE + "0001" + number + TAIL_0x10;

        String vs = HEAD_0x68 + CMD_0x02 + RW_0x01 + SRS_0x01 + checkSum(vs1) + "0001" + number + TAIL_0x10;

        byte[] vsbyte = HexString2Bytes(vs);

        return vsbyte;

    }


    //----------------------------收发语音的串口协议包格式 --------------------------------

    //开始/停止语音呼叫
    public byte[] voiceCall(String call, String number) { // 0x01：呼叫开始  0xFF：呼叫结束

        String vc1 = HEAD_0x68 + CMD_0x06 + RW_0x01 + call + CKSUM_BEFORE + "0004" + number + TAIL_0x10;

        String vc = HEAD_0x68 + CMD_0x06 + RW_0x01 + call + checkSum(vc1) + "0004" + number + TAIL_0x10;

        byte[] vcbyte = HexString2Bytes(vc);

        return vcbyte;
    }

    //收发亚音频类型设置

    public byte[] subAudioType(String number) { //DATA:接收亚音类型+发送亚音类型  1 为载波，2 为 CTCSS，3 为 CDCSS，4 为反向 CDCSS

        String sat1 = HEAD_0x68 + CMD_0x13 + RW_0x01 + SRS_0x01 + CKSUM_BEFORE + "0002" + number + TAIL_0x10;

        String sat = HEAD_0x68 + CMD_0x13 + RW_0x01 + SRS_0x01 + checkSum(sat1) + "0002" + number + TAIL_0x10;

        byte[] satbyte = HexString2Bytes(sat);

        return satbyte;

    }

    //CTCSS/DCS 亚音频率设置
    public byte[] ctcssDcs(String number) { //DATA:接收亚音频 +发送亚音频  CTCSS 范围为 0~50；CDCSS，反向 CDCSS 范围为 0~82

        String cd1 = HEAD_0x68 + CMD_0x14 + RW_0x01 + SRS_0x01 + CKSUM_BEFORE + "0002" + number + TAIL_0x10;

        String cd = HEAD_0x68 + CMD_0x14 + RW_0x01 + SRS_0x01 + checkSum(cd1) + "0002" + number + TAIL_0x10;

        byte[] cdbyte = HexString2Bytes(cd);

        return cdbyte;

    }

    //查询软件版本号
    public byte[] querySoftwareVersion(String number) { //DATA:0X01

        String qsv1 = HEAD_0x68 + CMD_0x25 + RW_0x01 + SRS_0x01 + CKSUM_BEFORE + "0001" + number + TAIL_0x10;

        String qsv = HEAD_0x68 + CMD_0x25 + RW_0x01 + SRS_0x01 + checkSum(qsv1) + "0001" + number + TAIL_0x10;

        byte[] qsvbyte = HexString2Bytes(qsv);

        return qsvbyte;
    }

    //设置模拟组命令
    /*
    第 0 字节为带宽（窄带：0X00 宽带：0X80）
    第 1 字节为功率（低功率：0 高功率：1）
    第 2-5 字节为接收频率（频率值为小端模式）
    第 6-9 字节为发射频率（频率值为小端模式）
    第 10 字节为静噪等级（0-9 级，0 为长接收）
    第11 字节为接收亚音类型（1：载波2：CTCSS  3：正向 CDCSS 4：反向 CDCSS）
    第 12 字节为接收亚音频率（CTCSS：0-50  CDCSS：0-82）
    第13 字节为发射亚音类型（1：载波2：CTCSS  3：正向 CDCSS 4：反向 CDCSS）
    第 14 字节为发射亚音频率（CTCSS：0-50  CDCSS：0-82）
     */
    public byte[] setSimulationGroupCommand(String number) {

        String ssgc1 = HEAD_0x68 + CMD_0x35 + RW_0x01 + SRS_0x01 + CKSUM_BEFORE + "000F" + number + TAIL_0x10;

        String ssgc = HEAD_0x68 + CMD_0x35 + RW_0x01 + SRS_0x01 + checkSum(ssgc1) + "000F" + number + TAIL_0x10;

        byte[] ssgcbyte = HexString2Bytes(ssgc);

        return ssgcbyte;

    }

    // 设置数字组命令
    /*
     第 0 字节为功率（低功率：0 高功率：1）
    第 1-4 字节为接收频率（频率值为小端模式）
    第 5-8 字节为发射频率（频率值为小端模式）
    第 9-12 字节为设置本机 ID（ID：1-16776415）
    第 13 字节为色码（色码：0-15）
    第 14 字节为联系人类型（1：个呼 2：组呼 3：无地址呼 4：全呼）
    第 15-18 字节为联系人号码（1-16776415）
    第 19 字节为加密开关（开：0x01 关：0xff）
    第 20-27 字节为密钥
    第 28-155 字节为接收组列表（1 个组号占 4个字节，不够 32 个组号填 0），
     */
    public byte[] setNumberGroupCommand(String number) {

        String sngc1 = HEAD_0x68 + CMD_0x36 + RW_0x01 + SRS_0x01 + CKSUM_BEFORE + "009c" + number + TAIL_0x10;

        String sngc = HEAD_0x68 + CMD_0x36 + RW_0x01 + SRS_0x01 + checkSum(sngc1) + "009c" + number + TAIL_0x10;

        byte[] sngcbyte = HexString2Bytes(sngc);

        return sngcbyte;

    }

    // 查询模拟组设置命令
    public byte[] queryGroup(String number) {//DATA:0x01

        String qg1 = HEAD_0x68 + CMD_0x37 + RW_0x01 + SRS_0x01 + CKSUM_BEFORE + "0001" + number + TAIL_0x10;

        String qg = HEAD_0x68 + CMD_0x37 + RW_0x01 + SRS_0x01 + checkSum(qg1) + "0001" + number + TAIL_0x10;

        byte[] qgbyte = HexString2Bytes(qg);

        return qgbyte;

    }

    // 查询数字组设置命令
    public byte[] queryGroupSettings(String number) { //DATA:0x01

        String qgs1 = HEAD_0x68 + CMD_0x38 + RW_0x01 + SRS_0x01 + CKSUM_BEFORE + "0001" + number + TAIL_0x10;

        String qgs = HEAD_0x68 + CMD_0x38 + RW_0x01 + SRS_0x01 + checkSum(qgs1) + "0001" + number + TAIL_0x10;

        byte[] qgsbyte = HexString2Bytes(qgs);

        return qgsbyte;

    }

    //14.Mic 增益配置
    public byte[] micGain(String number) { //DATA: 范围 0~15,默认 12

        String mg1 = HEAD_0x68 + CMD_0x0b + RW_0x01 + SRS_0x01 + CKSUM_BEFORE + "0001" + number + TAIL_0x10;

        String mg = HEAD_0x68 + CMD_0x0b + RW_0x01 + SRS_0x01 + checkSum(mg1) + "0001" + number + TAIL_0x10;

        byte[] mgbyte = HexString2Bytes(mg);

        return mgbyte;

    }


}
