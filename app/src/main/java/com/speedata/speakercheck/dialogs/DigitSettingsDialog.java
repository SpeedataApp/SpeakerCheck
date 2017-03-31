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

import com.speedata.speakercheck.test.MainActivity;
import com.speedata.speakercheck.R;
import com.speedata.speakercheck.utils.Cmds;

import static com.speedata.speakercheck.utils.DataConversionUtils.byteArrayToString;
import static java.lang.Integer.valueOf;

public class DigitSettingsDialog extends Dialog implements
        View.OnClickListener {

    private final MainActivity mainActivity;
    Context mContext;
    int fd;
    Cmds cmds;

    public DigitSettingsDialog(MainActivity mainActivity, Context context) {
        super(context);
        this.mainActivity = mainActivity;
        mContext = context;

        gongLv = mContext.getResources()
                .getStringArray(R.array.gonglv);

        seMa = mContext.getResources().getStringArray(
                R.array.sema);
        lianXirenleixing = mContext.getResources().getStringArray(R.array.lianxirenleixing);
        jiaMikaiguan = mContext.getResources().getStringArray(R.array.jiamikaiguan);

    }

    private Button goback;
    Button checkSetting;
    Button sureSetting;


    String[] gongLv;
    String[] seMa;
    String[] lianXirenleixing;
    String[] jiaMikaiguan;

    String gonglv16;

    String seMa16;
    String lianXirenleixing16;
    String jiaMikaiguan16;
    String lianXirenleixing10;
    String jiaMikaiguan10;

    EditText et1,et2,et3,et4,et5,et6;



    Spinner gonglv_Spinner;

    Spinner seMa_Spinner;
    Spinner jiaMikaiguan_Spinner;
    private Spinner lianXirenleixing_Spinner;


    int baudrate = 0;
    String serial_path = "";
    private String power_path;
    int stopbitt;

    ArrayAdapter<String> lianXirenleixing_adapter;
    ArrayAdapter<String> gonglv_adapter;

    ArrayAdapter<String> seMa_adapter;
    ArrayAdapter<String> jiaMikaiguan_adapter;

    int crc_num = 0;
    int powercount = 0;
    int serial = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digit_setting_dialog);

        cmds = new Cmds();

        goback = (Button) this.findViewById(R.id.backto);
        checkSetting = (Button) this.findViewById(R.id.check_setting);

        sureSetting = (Button) this.findViewById(R.id.sure_setting);

        gonglv_Spinner = (Spinner) this.findViewById(R.id.spinner2);

        lianXirenleixing_Spinner = (Spinner) this.findViewById(R.id.spinner4);
        seMa_Spinner = (Spinner) this.findViewById(R.id.spinner3);
        jiaMikaiguan_Spinner = (Spinner) this.findViewById(R.id.spinner5);

        goback.setOnClickListener(this);
        checkSetting.setOnClickListener(this);
        sureSetting.setOnClickListener(this);


        initView();


        gonglv_adapter = new ArrayAdapter<String>(mainActivity,
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


        lianXirenleixing_adapter = new ArrayAdapter<String>(mainActivity,
                android.R.layout.simple_spinner_item, lianXirenleixing);
        lianXirenleixing_adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lianXirenleixing_Spinner.setAdapter(lianXirenleixing_adapter);
        lianXirenleixing_Spinner
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
        seMa_adapter = new ArrayAdapter<String>(mainActivity,
                android.R.layout.simple_spinner_item, seMa);
        // Log.w(TAG,"WARN");
        seMa_adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seMa_Spinner.setAdapter(seMa_adapter);
        seMa_Spinner
                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        arg0.setVisibility(View.VISIBLE);

                        int select = seMa_Spinner.getSelectedItemPosition();
                        if(select<10){
                            seMa16 = "0"+select;
                        } else if (select>9){
                            seMa16 = getSema(select);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {


                    }

                });
        jiaMikaiguan_adapter = new ArrayAdapter<String>(mainActivity,
                android.R.layout.simple_spinner_item, jiaMikaiguan);
        // Log.w(TAG,"WARN");
        jiaMikaiguan_adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jiaMikaiguan_Spinner.setAdapter(jiaMikaiguan_adapter);
        jiaMikaiguan_Spinner
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

        gonglv_Spinner.setSelection(1);
        seMa_Spinner.setSelection(1);
        lianXirenleixing_Spinner.setSelection(1);
        jiaMikaiguan_Spinner.setSelection(1);

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


        et1.setText("420000000");
        et2.setText("420000000");
        et3.setText("888");
        et4.setText("3");
        et5.setText("8");
        et6.setText("1");

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

        } else if (v == checkSetting) {//检查设置项是否有问题
            String jieshou = et1.getText().toString();
            String fasong = et2.getText().toString();
            String benjiid = et3.getText().toString();
            String lianxiren = et4.getText().toString();
            String miyao = et5.getText().toString();
            String jieshouzu = et6.getText().toString();

            int e3 = Integer.parseInt(benjiid);
            int e4 = Integer.parseInt(lianxiren);

            if(jieshou.length() != 9 || fasong.length() != 9) {
                Toast.makeText(mainActivity, "请确认接收频率和发送频率的输入格式是否正确", Toast.LENGTH_SHORT).show();
                return;
            } else if (e3 < 1 || e3 > 16776415) {
                Toast.makeText(mainActivity, "本机ID的范围是:1-16776415", Toast.LENGTH_SHORT).show();
                return;
            } else if (e4 < 1 || e4 > 16776415) {
                Toast.makeText(mainActivity, "联系人号码的范围是:1-16776415", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(mainActivity, "检查完毕", Toast.LENGTH_SHORT).show();

        } else if (v == sureSetting) {
            String jieshou = et1.getText().toString();
            String fasong = et2.getText().toString();
            String benjiid = et3.getText().toString();
            String lianxiren = et4.getText().toString();
            String miyao = et5.getText().toString();
            String jieshouzu = et6.getText().toString();

            jieshou = Integer.toHexString(valueOf(jieshou));
            String sum="";
            for (int i = 0; i < jieshou.length(); i = i + 2) {
                String s = jieshou.substring(jieshou.length() - (2 + i), jieshou.length() - i);
                sum = sum + s;
            }
            jieshou = sum;

            fasong = Integer.toHexString(valueOf(fasong));
            String sum2="";
            for (int i = 0; i < fasong.length(); i = i + 2) {
                String s = fasong.substring(fasong.length() - (2 + i), fasong.length() - i);
                sum2 = sum2 + s;
            }
            fasong = sum2;

            benjiid = Integer.toHexString(valueOf(benjiid));//10进制做成16进制数
            lianxiren = Integer.toHexString(valueOf(lianxiren));//10进制做成16进制数

            for (int i = benjiid.length(); i < 8; i++) {
                benjiid = "0" + benjiid;
            }
            for (int i = lianxiren.length(); i < 8; i++) {
                lianxiren = "0" + lianxiren;
            }

            if ("".equals(miyao)){
                miyao = "0";
            }
            miyao = Integer.toHexString(valueOf(miyao));
            for (int i = miyao.length(); i < 16; i++) {
                miyao = "0" + miyao;
            }
            if ("".equals(jieshouzu)){
                jieshouzu = "0";
            }
            jieshouzu = getJieshouzu(jieshouzu);



            //这是设置数字组命令中的DATA部分
            String all = gonglv16 + jieshou + fasong + benjiid + seMa16 + lianXirenleixing16
                    + lianxiren + jiaMikaiguan16 + miyao + jieshouzu;

            cardtemp = cmds.setNumberGroupCommand(all);
            mainActivity.IDDev.WriteSerialByte(mainActivity.IDFd, cardtemp);
            mainActivity.mingling = (byteArrayToString(cardtemp));

        }

    }

    private String getJieshouzu(String jieshouzu) {
        String[] arr = jieshouzu.split(",");
        String sum = "";
        for (int i = 0; i < arr.length; i++){
            String a = Integer.toHexString(valueOf(arr[i]));
            for (int j = a.length(); j < 8; j++) {
                a = "0" + a;
            }
            sum = a + sum;
        }

        for (int k = sum.length(); k < 256; k++){
            sum =sum + "0";
        }

        return sum;
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