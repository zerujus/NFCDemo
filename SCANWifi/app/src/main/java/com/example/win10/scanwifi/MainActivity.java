package com.example.win10.scanwifi;

import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.win10.scanwifi.WifiController.WifiCipherType.WIFICIPHER_NOPASS;
import static com.example.win10.scanwifi.WifiController.WifiCipherType.WIFICIPHER_WEP;
import static com.example.win10.scanwifi.WifiController.WifiCipherType.WIFICIPHER_WPA;

public class MainActivity extends AppCompatActivity {

    private TextView textView_broadcase;
    private RadioGroup radioGroup;
    private WifiController.WifiCipherType type;
    private EditText editText_SSID;
    private EditText editText_pwd;
    private TextView textView_lib;
    private EditText editText_loop;
    private RadioButton radioButton3;
    private Button button;

    public static int num = 0;
    private int i;

    private MyReceiver receiver = new MyReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_broadcase = findViewById(R.id.textView_broadcase);
        radioGroup = findViewById(R.id.radioGroup);
        editText_SSID = findViewById(R.id.editText_ssid);
        editText_pwd = findViewById(R.id.editText_pwd);
        textView_lib = findViewById(R.id.textView_lib);
        editText_loop = findViewById(R.id.editText_loop);
        radioButton3 = findViewById(R.id.radioButton3);
        button = findViewById(R.id.button);


        //默认为wpa
        type = WIFICIPHER_WPA;
        radioButton3.setChecked(true);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButton) {
                    type = WIFICIPHER_NOPASS;
                    editText_pwd.setFocusable(false);
                } else if (checkedId == R.id.radioButton2) {
                    editText_pwd.setFocusable(true);
                    editText_pwd.requestFocus();
                    editText_pwd.setFocusableInTouchMode(true);
                    type = WIFICIPHER_WEP;
                } else if (checkedId == R.id.radioButton3) {
                    editText_pwd.setFocusable(true);
                    editText_pwd.requestFocus();
                    editText_pwd.setFocusableInTouchMode(true);
                    type = WIFICIPHER_WPA;
                }
            }
        });

        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    /**
     * button点击事件
     *
     * @param v
     */
    public void doConn(View v) {

        button.setFocusable(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }).start();
    }

    public void connect() {
        //默认遍历次数为1
        int len = 1;
        String loop = editText_loop.getText().toString();
        if (loop != null && !loop.equals("") && !loop.isEmpty()) {
            len = Integer.valueOf(loop);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView_lib.setText("开始wifi连接操作");
            }
        });

        WifiController controller = new WifiController(this);

        //配置wifi info
        WifiConfiguration config = controller.createWifiInfo(editText_SSID.getText().toString(), editText_pwd.getText().toString(), type);

        //循环连接、中断wifi，且不记忆wifi密码
        for (i = 0; i < len; i++) {
            synchronized (this) {
                int result = controller.connect(config);
                if (result == -1) {
                    Log.d("zerujus", "连接失败发生在" + i);
                    continue;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                controller.disconnect(result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView_lib.setText("已完成第" + String.valueOf(i) + "次连接");
                    }
                });
                Log.d("zerujus", "已完成第" + String.valueOf(i) + "次连接");
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (i == len - 1) {
                i++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView_lib.setText("已完成" + i + "次wifi连接操作");
                    }
                });
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView_broadcase.setText("接受error广播: " + String.valueOf(num) + "次");
            }
        });
        Log.d("zerujus", String.valueOf(num));
    }

    @Override
    protected void onDestroy() {
        //注销广播
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
