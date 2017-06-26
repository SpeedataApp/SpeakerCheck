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

import static java.lang.Integer.valueOf;

/**
 * Created by xu on 2017/5/24.
 */

public class YayinDialog  extends Dialog implements
        View.OnClickListener {

    private final SpeakerActivity mainActivity;
    Context mContext;
    int fd;
    Cmds cmds;

    public YayinDialog(SpeakerActivity mainActivity, Context context) {
        super(context);
        this.mainActivity = mainActivity;
        mContext = context;
        
        jieShouyayin = mContext.getResources().getStringArray(R.array.jieshouyayin);
        faSongyayin = mContext.getResources().getStringArray(R.array.fasheyayin);

    }

    private Button goback;
    Button checkSetting;
    Button sureSetting;


    String[] jieShouyayin;
    String[] faSongyayin;


    String jieshouyayin16;
    String fasongyayin16;
    String jieshouyayin10;
    String fasongyayin10;

    EditText et3,et4;

    
    Spinner fasongyayin_Spinner;
    private Spinner jieshouyayin_Spinner;


    int baudrate = 0;
    String serial_path = "";
    private String power_path;
    int stopbitt;

    ArrayAdapter<String> jieshouyayin_adapter;

    ArrayAdapter<String> fasongyayin_adapter;

    int crc_num = 0;
    int powercount = 0;
    int serial = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yayin_dialog);

        cmds = new Cmds();

        goback = (Button) this.findViewById(R.id.backto);
        checkSetting = (Button) this.findViewById(R.id.check_setting);

        sureSetting = (Button) this.findViewById(R.id.sure_setting);


        jieshouyayin_Spinner = (Spinner) this.findViewById(R.id.spinner4);

        fasongyayin_Spinner = (Spinner) this.findViewById(R.id.spinner5);

        goback.setOnClickListener(this);
        checkSetting.setOnClickListener(this);
        sureSetting.setOnClickListener(this);


        initView();


        jieshouyayin_adapter = new ArrayAdapter<String>(mainActivity,
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
       
        fasongyayin_adapter = new ArrayAdapter<String>(mainActivity,
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
       

    }

    //弄好ettext们
    private void initView() {

        et3 = (EditText) this.findViewById(R.id.et_3); //亚音接收
        et4 = (EditText) this.findViewById(R.id.et_4); //亚音发射

        et3.setText("0");
        et4.setText("0");
    }

    // DeviceControl DevCtrl2;

    String power_off_write = "";
    String power_on_write = "";
    String actual_path = "";

    @Override
    public void onClick(View v) {
        byte[] cardtemptype = null;
        byte[] cardtempfreq = null;

        if (v == goback) {
            dismiss();

        } else if (v == checkSetting) { //检查设置项是否有问题
           
            String jieshouyayin = et3.getText().toString();
            String fasongyayin = et4.getText().toString();
            int e3 = Integer.parseInt(jieshouyayin);
            int e4 = Integer.parseInt(fasongyayin);

            if ("01".equals(jieshouyayin16)) {
                if (e3 != 0) {
                    Toast.makeText(mainActivity, "载波类型下，对应的频率为应为0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if ("01".equals(fasongyayin16)) {
                if (e4 != 0) {
                    Toast.makeText(mainActivity, "载波类型下，对应的频率为应为0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if ("02".equals(jieshouyayin16)) {
                if (e3 > 50 || e3 < 0) {
                    Toast.makeText(mainActivity, "CTCSS类型下，频率范围是0-50", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if ("02".equals(fasongyayin16)) {
                if (e4 > 50 || e4 < 0) {
                    Toast.makeText(mainActivity, "CTCSS类型下，频率范围是0-50", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if ("03".equals(jieshouyayin16) || "04".equals(jieshouyayin16)) {
                if (e3 > 82 || e3 < 0) {
                    Toast.makeText(mainActivity, "CDCSS类型下，频率范围是0-82", Toast.LENGTH_SHORT).show();
                    return;
                }

            } else if ("03".equals(fasongyayin16) || "04".equals(fasongyayin16)) {
                if (e4 > 82 || e4 < 0) {
                    Toast.makeText(mainActivity, "CDCSS类型下，频率范围是0-82", Toast.LENGTH_SHORT).show();
                    return;
                }

            }

            Toast.makeText(mainActivity, "检查完毕", Toast.LENGTH_SHORT).show();

        } else if (v == sureSetting) {
            
            String jieshouyayin = et3.getText().toString();
            String fasongyayin = et4.getText().toString();

            jieshouyayin = Integer.toHexString(valueOf(jieshouyayin));

            fasongyayin = Integer.toHexString(valueOf(fasongyayin));
            if (jieshouyayin.length() == 1) {
                jieshouyayin = "0" + jieshouyayin;
            }
            if (fasongyayin.length() == 1) {
                fasongyayin = "0" + fasongyayin;
            }

            //这是与修改亚音相关的部分

            String type = jieshouyayin16 + fasongyayin16;
            String freq = jieshouyayin + fasongyayin;

            cardtemptype = cmds.subAudioType(type);
            cardtempfreq = cmds.ctcssDcs(freq);

            //此时进行结果判断，都是载波只发送载波，不修改亚音
            //当修改为亚音的3种时，修改后继续修改亚音
            if ("1".equals(fasongyayin10) && "1".equals(jieshouyayin10)) {
                //载波不用发送修改亚音的命令
                mainActivity.IDDev.WriteSerialByte(mainActivity.IDFd, cardtemptype);

            } else {
                //非载波就得多发一个修改亚音的命令
                mainActivity.IDDev.WriteSerialByte(mainActivity.IDFd, cardtemptype);

                mainActivity.IDDev.WriteSerialByte(mainActivity.IDFd, cardtempfreq);
            }


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