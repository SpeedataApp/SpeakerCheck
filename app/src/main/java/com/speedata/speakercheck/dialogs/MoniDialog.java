package com.speedata.speakercheck.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.speedata.speakercheck.R;
import com.speedata.speakercheck.activity.SpeakerActivity;
import com.speedata.speakercheck.utils.Cmds;
import com.speedata.speakercheck.utils.SpeakerApi;

import static java.lang.Integer.valueOf;

public class MoniDialog extends Dialog implements
        View.OnClickListener {
    private final SpeakerApi speakerApi;
    private Context mContext;
    int fd;
    private Cmds cmds;

    public MoniDialog(SpeakerApi speakerApi, Context context) {
        super(context);
        this.speakerApi = speakerApi;
        mContext = context;

        gongLv = mContext.getResources()
                .getStringArray(R.array.gonglv);
        daiKuan = mContext.getResources().getStringArray(
                R.array.daikuan);
        jingZaodengji = mContext.getResources().getStringArray(
                R.array.jingzaodengji);
        jieShouyayin = mContext.getResources().getStringArray(R.array.jieshouyayin);
        faSongyayin = mContext.getResources().getStringArray(R.array.fasheyayin);

    }

    private Button goback;
    private Button checkSetting;
    private Button sureSetting;


    private String[] gongLv;
    private String[] daiKuan;
    private String[] jingZaodengji;
    private String[] jieShouyayin;
    private String[] faSongyayin;

    private String gonglv16;
    private String daikuan16;
    private String jingzaodengji16;
    private String jieshouyayin16;
    private String fasongyayin16;
    private String jieshouyayin10;
    private String fasongyayin10;

    private EditText et1;
    private EditText et2;
    private EditText et3;
    private EditText et4;


    private Spinner gonglv_Spinner;
    private Spinner daikuan_Spinner;
    private Spinner jingzaodengji_Spinner;
    private Spinner fasongyayin_Spinner;
    private Spinner jieshouyayin_Spinner;


    int baudrate = 0;
    String serial_path = "";
    private String power_path;
    int stopbitt;

    private ArrayAdapter<String> jieshouyayin_adapter;
    private ArrayAdapter<String> gonglv_adapter;
    private ArrayAdapter<String> daikuan_adapter;
    private ArrayAdapter<String> jingzaodengji_adapter;
    private ArrayAdapter<String> fasongyayin_adapter;

    int crc_num = 0;
    int powercount = 0;
    int serial = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second);

        cmds = new Cmds();

        goback = (Button) this.findViewById(R.id.backto);
        checkSetting = (Button) this.findViewById(R.id.check_setting);

        sureSetting = (Button) this.findViewById(R.id.sure_setting);

        gonglv_Spinner = (Spinner) this.findViewById(R.id.spinner2);
        daikuan_Spinner = (Spinner) this.findViewById(R.id.spinner1);
        jieshouyayin_Spinner = (Spinner) this.findViewById(R.id.spinner4);
        jingzaodengji_Spinner = (Spinner) this.findViewById(R.id.spinner3);
        fasongyayin_Spinner = (Spinner) this.findViewById(R.id.spinner5);

        goback.setOnClickListener(this);
        checkSetting.setOnClickListener(this);
        sureSetting.setOnClickListener(this);


        initView();


        gonglv_adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, gongLv);
        gonglv_adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gonglv_Spinner.setAdapter(gonglv_adapter);
        gonglv_Spinner
                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        arg0.setVisibility(View.VISIBLE);

                        String a1 = gonglv_Spinner
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
        daikuan_adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, daiKuan);
        daikuan_adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daikuan_Spinner.setAdapter(daikuan_adapter);
        daikuan_Spinner
                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        arg0.setVisibility(View.VISIBLE);

                        String a2 = MoniDialog.this.daikuan_Spinner
                                .getSelectedItem().toString();

                        if ("宽带".equals(a2)) {
                            daikuan16 = "80";
                        } else {
                            daikuan16 = "00";
                        }


                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });

        jieshouyayin_adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, jieShouyayin);
        jieshouyayin_adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jieshouyayin_Spinner.setAdapter(jieshouyayin_adapter);
        jieshouyayin_Spinner
                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        // "/proc/driver/scan","/proc/driver/as3992","/proc/driver/tda3992","/proc/driver/sirf"
                        arg0.setVisibility(View.VISIBLE);

                        if (position == 0 && id == 0) {
                            jieshouyayin16 = "01";
                            jieshouyayin10 = "1";
                        }
                        if (position == 1 && id == 1) {
                            jieshouyayin16 = "02";
                            jieshouyayin10 = "2";
                        }
                        if (position == 2 && id == 2) {
                            jieshouyayin16 = "03";
                            jieshouyayin10 = "3";
                        }
                        if (position == 3 && id == 3) {
                            jieshouyayin16 = "04";
                            jieshouyayin10 = "4";
                        }


                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {


                    }

                });
        jingzaodengji_adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, jingZaodengji);
        // Log.w(TAG,"WARN");
        jingzaodengji_adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jingzaodengji_Spinner.setAdapter(jingzaodengji_adapter);
        jingzaodengji_Spinner
                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        arg0.setVisibility(View.VISIBLE);

                        int select = jingzaodengji_Spinner.getSelectedItemPosition();
                        jingzaodengji16 = "0" + select;

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });
        fasongyayin_adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, faSongyayin);
        // Log.w(TAG,"WARN");
        fasongyayin_adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fasongyayin_Spinner.setAdapter(fasongyayin_adapter);
        fasongyayin_Spinner
                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        arg0.setVisibility(View.VISIBLE);
                        if (position == 0 && id == 0) {
                            fasongyayin16 = "01";
                            fasongyayin10 = "1";
                        }
                        if (position == 1 && id == 1) {
                            fasongyayin16 = "02";
                            fasongyayin10 = "2";
                        }
                        if (position == 2 && id == 2) {
                            fasongyayin16 = "03";
                            fasongyayin10 = "3";
                        }
                        if (position == 3 && id == 3) {
                            fasongyayin16 = "04";
                            fasongyayin10 = "4";
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });
        daikuan_Spinner.setSelection(1);
        gonglv_Spinner.setSelection(1);
        jingzaodengji_Spinner.setSelection(1);

    }

    //弄好ettext们
    private void initView() {
        et1 = (EditText) this.findViewById(R.id.et_1); //接收频率
        et2 = (EditText) this.findViewById(R.id.et_2); //发射频率
        et3 = (EditText) this.findViewById(R.id.et_3); //亚音接收
        et4 = (EditText) this.findViewById(R.id.et_4); //亚音发射


        et1.setText("433375000");
        et2.setText("433375000");
        et3.setText("0");
        et4.setText("0");
    }


    // DeviceControl DevCtrl2;

    String power_off_write = "";
    String power_on_write = "";
    String actual_path = "";

    @Override
    public void onClick(View v) {
        byte[] cardtemp = null;

        if (v == goback) {
            dismiss();

        } else if (v == checkSetting) { //检查设置项是否有问题
            String jieshou = et1.getText().toString();
            String fasong = et2.getText().toString();
            String jieshouyayin = et3.getText().toString();
            String fasongyayin = et4.getText().toString();
            int e3 = Integer.parseInt(jieshouyayin);
            int e4 = Integer.parseInt(fasongyayin);

            if (jieshou.length() != 9 || fasong.length() != 9) {
                Toast.makeText(mContext, "请确认接收频率和发送频率的输入格式是否正确", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("01".equals(jieshouyayin16)) {
                if (e3 != 0) {
                    Toast.makeText(mContext, "载波类型下，对应的频率为应为0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if ("01".equals(fasongyayin16)) {
                if (e4 != 0) {
                    Toast.makeText(mContext, "载波类型下，对应的频率为应为0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if ("02".equals(jieshouyayin16)) {
                if (e3 > 50 || e3 < 0) {
                    Toast.makeText(mContext, "CTCSS类型下，频率范围是0-50", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if ("02".equals(fasongyayin16)) {
                if (e4 > 50 || e4 < 0) {
                    Toast.makeText(mContext, "CTCSS类型下，频率范围是0-50", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if ("03".equals(jieshouyayin16) || "04".equals(jieshouyayin16)) {
                if (e3 > 82 || e3 < 0) {
                    Toast.makeText(mContext, "CDCSS类型下，频率范围是0-82", Toast.LENGTH_SHORT).show();
                    return;
                }

            } else if ("03".equals(fasongyayin16) || "04".equals(fasongyayin16)) {
                if (e4 > 82 || e4 < 0) {
                    Toast.makeText(mContext, "CDCSS类型下，频率范围是0-82", Toast.LENGTH_SHORT).show();
                    return;
                }

            }

            Toast.makeText(mContext, "检查完毕", Toast.LENGTH_SHORT).show();

        } else if (v == sureSetting) {
            String jieshou = et1.getText().toString();
            String fasong = et2.getText().toString();
            String jieshouyayin = et3.getText().toString();
            String fasongyayin = et4.getText().toString();

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

            jieshouyayin = Integer.toHexString(valueOf(jieshouyayin));

            fasongyayin = Integer.toHexString(valueOf(fasongyayin));
            if (jieshouyayin.length() == 1) {
                jieshouyayin = "0" + jieshouyayin;
            }
            if (fasongyayin.length() == 1) {
                fasongyayin = "0" + fasongyayin;
            }

            //这是设置模拟组命令中的DATA部分
            String all = daikuan16 + gonglv16 + jieshou + fasong + jingzaodengji16 + jieshouyayin16
                    + jieshouyayin + fasongyayin16 + fasongyayin;


            cardtemp = cmds.setSimulationGroupCommand(all);
            speakerApi.writeSerialByte(cardtemp);

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
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


}