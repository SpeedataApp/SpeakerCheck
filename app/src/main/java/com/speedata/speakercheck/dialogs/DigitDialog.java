package com.speedata.speakercheck.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.speedata.speakercheck.App;
import com.speedata.speakercheck.R;
import com.speedata.speakercheck.activity.SpeakerActivity;
import com.speedata.speakercheck.utils.Cmds;
import com.speedata.speakercheck.utils.SpeakerApi;

import static java.lang.Integer.valueOf;

public class DigitDialog extends Dialog implements
        View.OnClickListener {

    private final SpeakerApi speakerApi;
    private Context mContext;

    private Cmds cmds;

    public DigitDialog(SpeakerApi speakerApi, Context context) {
        super(context);
        this.speakerApi = speakerApi;
        mContext = context;

        gongLv = mContext.getResources()
                .getStringArray(R.array.gonglv);

        seMa = mContext.getResources().getStringArray(
                R.array.sema);
        lianXirenleixing = mContext.getResources().getStringArray(R.array.lianxirenleixing);
        jiaMikaiguan = mContext.getResources().getStringArray(R.array.jiamikaiguan);

    }

    private Button goback;
    private Button checkSetting;
    private Button sureSetting;

    private String[] gongLv;
    private String[] seMa;
    private String[] lianXirenleixing;
    private String[] jiaMikaiguan;

    private String gonglv16;

    private String seMa16;
    private String lianXirenleixing16;
    private String jiaMikaiguan16;
    private String lianXirenleixing10;
    public String channel;
    private String channelLast;

    private EditText et1;
    private EditText et2;
    private EditText et3;
    private EditText et4;
    private EditText et5;
    private EditText et6;
    private EditText etMic;

    private Spinner gonglvSpinner;
    private Spinner lianXirenleixingSpinner;
    private Spinner jiaMikaiguanSpinner;
    private Spinner seMaSpinner;

    private String micset; //mic增益值

    private String TAG = "speakercheck";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digit_setting_dialog);

        cmds = new Cmds();

        goback = (Button) this.findViewById(R.id.backto);
        checkSetting = (Button) this.findViewById(R.id.check_setting);

        sureSetting = (Button) this.findViewById(R.id.sure_setting);

        gonglvSpinner = (Spinner) this.findViewById(R.id.spinner2);

        lianXirenleixingSpinner = (Spinner) this.findViewById(R.id.spinner4);
        seMaSpinner = (Spinner) this.findViewById(R.id.spinner3);
        jiaMikaiguanSpinner = (Spinner) this.findViewById(R.id.spinner5);

        goback.setOnClickListener(this);
        checkSetting.setOnClickListener(this);
        sureSetting.setOnClickListener(this);

        initView();
        channelLast = "00";
        channel = speakerApi.channelRemember;
        micset = App.micGain;


        ArrayAdapter<String> gonglvAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, gongLv);
        gonglvAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gonglvSpinner.setAdapter(gonglvAdapter);
        gonglvSpinner
                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        arg0.setVisibility(View.VISIBLE);

                        String a1 = gonglvSpinner
                                .getSelectedItem().toString();
                        if ("高功率".equals(a1)) {
                            gonglv16 = "01";
                        } else {
                            gonglv16 = "00";
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });


        ArrayAdapter<String> lianXirenleixingAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, lianXirenleixing);
        lianXirenleixingAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lianXirenleixingSpinner.setAdapter(lianXirenleixingAdapter);
        lianXirenleixingSpinner
                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        // "/proc/driver/scan","/proc/driver/as3992","/proc/driver/tda3992","/proc/driver/sirf"
                        arg0.setVisibility(View.VISIBLE);

                        if (position == 0 && id == 0) {
                            lianXirenleixing16 = "01";
                            lianXirenleixing10 = "1";
                        }
                        if (position == 1 && id == 1) {
                            lianXirenleixing16 = "02";
                            lianXirenleixing10 = "2";
                        }
                        if (position == 2 && id == 2) {
                            lianXirenleixing16 = "03";
                            lianXirenleixing10 = "3";
                        }
                        if (position == 3 && id == 3) {
                            lianXirenleixing16 = "04";
                            lianXirenleixing10 = "4";
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });
        ArrayAdapter<String> seMaAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, seMa);
        // Log.w(TAG,"WARN");
        seMaAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seMaSpinner.setAdapter(seMaAdapter);
        seMaSpinner
                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        arg0.setVisibility(View.VISIBLE);

                        int select = seMaSpinner.getSelectedItemPosition();
                        if (select < 10) {
                            seMa16 = "0" + select;
                        } else if (select > 9) {
                            seMa16 = getSema(select);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });
        ArrayAdapter<String> jiaMikaiguanAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, jiaMikaiguan);
        // Log.w(TAG,"WARN");
        jiaMikaiguanAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jiaMikaiguanSpinner.setAdapter(jiaMikaiguanAdapter);
        jiaMikaiguanSpinner
                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        arg0.setVisibility(View.VISIBLE);
                        if (position == 0 && id == 0) {
                            jiaMikaiguan16 = "01";

                        }
                        if (position == 1 && id == 1) {
                            jiaMikaiguan16 = "ff";

                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });

        gonglvSpinner.setSelection(0);
        seMaSpinner.setSelection(1);
        lianXirenleixingSpinner.setSelection(1);
        jiaMikaiguanSpinner.setSelection(1);

        if (!channel.equals(channelLast)) {
            setView(channel);
        }

    }

    private String getSema(int select) {
        switch (select) {
            case 10:
                return "0a";

            case 11:
                return "0b";

            case 12:
                return "0c";

            case 13:
                return "0d";

            case 14:
                return "0e";

            case 15:
                return "0f";
        }
        return "00";
    }

    //弄好ettext们
    private void initView() {
        et1 = (EditText) this.findViewById(R.id.et_1); //接收频率
        et2 = (EditText) this.findViewById(R.id.et_2); //发射频率
        et3 = (EditText) this.findViewById(R.id.et_3); //本机ID
        et4 = (EditText) this.findViewById(R.id.et_4); //联系人号码
        et5 = (EditText) this.findViewById(R.id.et_5); //密钥
        et6 = (EditText) this.findViewById(R.id.et_6); //接收组列表
        etMic = (EditText) this.findViewById(R.id.et_mic); //设置


        et1.setText("420000000");
        et2.setText("420000000");
        et3.setText("888");
        et4.setText("1");
        et5.setText("8");
        et6.setText("1");
        etMic.setText(micset);

    }


    @Override
    public void onClick(View v) {
        byte[] cardtemp = null;

        if (v == goback) {
            dismiss();

        } else if (v == checkSetting) { //检查设置项是否有问题
            String jieshou = et1.getText().toString();
            String fasong = et2.getText().toString();
            String benjiid = et3.getText().toString();
            String lianxiren = et4.getText().toString();
            String miyao = et5.getText().toString();
            String jieshouzu = et6.getText().toString();
            String mic = etMic.getText().toString();

            int e3 = Integer.parseInt(benjiid);
            int e4 = Integer.parseInt(lianxiren);
            int eM;
            if ("".equals(mic)) {
                eM = 12;
            } else {
                eM = Integer.parseInt(mic);
            }


            if (jieshou.length() != 9 || fasong.length() != 9) {
                Toast.makeText(mContext, "请确认接收频率和发送频率的输入格式是否正确", Toast.LENGTH_SHORT).show();
                return;
            } else if (e3 < 1 || e3 > 16776415) {
                Toast.makeText(mContext, "本机ID的范围是:1-16776415", Toast.LENGTH_SHORT).show();
                return;
            } else if (e4 < 1 || e4 > 16776415) {
                Toast.makeText(mContext, "联系人号码的范围是:1-16776415", Toast.LENGTH_SHORT).show();
                return;
            } else if (eM < 0 || eM > 15) {
                Toast.makeText(mContext, "MIC增益的范围是:0-15", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(mContext, "检查完毕", Toast.LENGTH_SHORT).show();

        } else if (v == sureSetting) {
            String jieshou = et1.getText().toString();
            String fasong = et2.getText().toString();
            String benjiid = et3.getText().toString();
            String lianxiren = et4.getText().toString();
            String miyao = et5.getText().toString();
            String jieshouzu = et6.getText().toString();
            String mic = etMic.getText().toString();


            jieshou = Integer.toHexString(valueOf(jieshou));
            String sum = "";
            for (int i = 0; i < jieshou.length(); i = i + 2) {
                String s = jieshou.substring(jieshou.length() - (2 + i), jieshou.length() - i);
                sum = sum + s;
            }
            jieshou = sum;

            fasong = Integer.toHexString(valueOf(fasong));
            String sum2 = "";
            for (int i = 0; i < fasong.length(); i = i + 2) {
                String s = fasong.substring(fasong.length() - (2 + i), fasong.length() - i);
                sum2 = sum2 + s;
            }
            fasong = sum2;

            benjiid = Integer.toHexString(valueOf(benjiid)); //10进制做成16进制数
            lianxiren = Integer.toHexString(valueOf(lianxiren)); //10进制做成16进制数

            for (int i = benjiid.length(); i < 8; i++) {
                benjiid = "0" + benjiid;
            }
            for (int i = lianxiren.length(); i < 8; i++) {
                lianxiren = "0" + lianxiren;
            }

            if ("".equals(miyao)) {
                miyao = "0";
            }
            miyao = Integer.toHexString(valueOf(miyao));
            for (int i = miyao.length(); i < 16; i++) {
                miyao = "0" + miyao;
            }
            if ("".equals(jieshouzu)) {
                jieshouzu = "0";
            }
            jieshouzu = getJieshouzu(jieshouzu);


            if ("".equals(mic)) {
            } else {
                micset = mic;
                App.micGain = micset;
                int eM = Integer.parseInt(mic);
                if (eM < 10) {
                    mic = "0" + eM;
                } else if (eM > 9) {
                    mic = getSema(eM);
                }
                cardtemp = cmds.micGain(mic);
                speakerApi.writeSerialByte(cardtemp);
                Log.d(TAG, mic);
            }


            //这是设置数字组命令中的DATA部分
            String all = gonglv16 + jieshou + fasong + benjiid + seMa16 + lianXirenleixing16
                    + lianxiren + jiaMikaiguan16 + miyao + jieshouzu;

            cardtemp = cmds.setNumberGroupCommand(all);
            speakerApi.writeSerialByte(cardtemp);

        }

    }

    private String getJieshouzu(String jieshouzu) {
        String[] arr = jieshouzu.split(",");
        String sum = "";
        for (int i = 0; i < arr.length; i++) {
            String a = Integer.toHexString(valueOf(arr[i]));
            for (int j = a.length(); j < 8; j++) {
                a = "0" + a;
            }
            sum = a + sum;
        }

        for (int k = sum.length(); k < 256; k++) {
            sum = sum + "0";
        }

        return sum;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                sendBroadcastKey("down");
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                sendBroadcastKey("up");
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void sendBroadcastKey(String action) {
        Intent intent = new Intent("wrist.action." + action);
        mContext.sendBroadcast(intent);
    }

    public void DisplayToast(String str) {
        Toast toast = Toast.makeText(mContext, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 220);
        toast.show();
    }

    private void setView(String ch) {
        channelLast = ch;
        switch (ch) {
            case "01":

                et1.setText("433375000");
                et2.setText("433375000");
                etMic.setText(micset);

                break;
            case "02":

                et1.setText("437225000");
                et2.setText("437225000");
                etMic.setText(micset);
                break;
            case "03":

                et1.setText("431375000");
                et2.setText("431375000");
                etMic.setText(micset);
                break;
            case "04":

                et1.setText("436255000");
                et2.setText("436255000");
                etMic.setText(micset);
                break;
            case "05":

                et1.setText("439975000");
                et2.setText("439975000");
                etMic.setText(micset);
                break;
            case "06":

                et1.setText("400000000");
                et2.setText("400000000");
                etMic.setText(micset);
                break;
            case "07":

                et1.setText("435000000");
                et2.setText("435000000");
                etMic.setText(micset);
                break;
            case "08":

                et1.setText("470000000");
                et2.setText("470000000");
                etMic.setText(micset);
                break;

        }

    }

}