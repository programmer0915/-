package com.dream.androidapp;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dream.androidapp.service.MusicPlayerService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lecho.lib.hellocharts.view.LineChartView;


public class ChatActivity extends Activity {

    private LineChart mLineChart;

    private SensorManager sensorManager;
    private Vibrator vibrator;
    private ShakeListener shakeListener;

    private ImageButton btnSend;
    private ImageButton btnDisconnect;

    private ImageButton btnopen;
    private ImageButton btnclose;

    private ImageButton btncurtain;
    private ImageButton btnclosecurtain;

    private ImageButton btnclear;
    private ImageButton btnpress;
    private ImageButton btnpress1;

    private ImageButton btnstopmusic;

    private ImageButton btnstoptemp;
    private ImageButton btntemp;

    private ArrayAdapter<String> chatMsgAdapter = null;
    private List<String> msgList = new ArrayList<>();

    private EditText edtTxtMsg;
    private ListView lstViewMsgs;
    private TextView temp;
    private TextView hump;

    String tempvalue="";

    String humpvalue="";

    Deal deal = new Deal();

    private BluetoothServerSocket serverSocket = null;
    private BluetoothSocket socket = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice bluetoothDevice = null;
    private static final String SERVER_CLIENT_UUID = "00001101-0000-1000-8000-00805f9b34fb";
    private static final String SERVER_NAME = "bleApp";
    private ServerThread serverThread;
    private ClientThread clientThread;
    private ReadThread readThread;

    public  static final int PLAY =0;

    public  static final int PAUSE =1;

    public  static final int STOP =2;

/******************曲线部分*************************/


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);//获取振动器Vibrator
        shakeListener = new ShakeListener();
        //获取加速度传感器设置回调的频率
        sensorManager.registerListener(shakeListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_NORMAL);
        initView();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            chatMsgAdapter.notifyDataSetChanged();
            String obj = (String) msg.obj;

           if (obj.equals("m")) {
                PlayMusic();
            }
            switch (msg.what){
                case 1:
                    Log.i("123",obj);
                    break;
                default:
                    break;
            }
              lstViewMsgs.setSelection(msgList.size() - 1);
            }

    };

    //摇一摇监听器
    public class ShakeListener implements SensorEventListener {
        /**
         * 检测的时间间隔
         */
        static final int UPDATE_INTERVAL = 100;
        /**
         * 上一次检测的时间
         */
        long mLastUpdateTime;
        /**
         * 上一次检测时，加速度在x、y、z方向上的分量，用于和当前加速度比较求差。
         */
        float mLastX, mLastY, mLastZ;

        /**
         * 摇晃检测阈值，决定了对摇晃的敏感程度，越小越敏感。
         */
        public int shakeThreshold = 3000;

        @Override
        public void onSensorChanged(SensorEvent event) {

            long currentTime = System.currentTimeMillis();
            long diffTime = currentTime - mLastUpdateTime;
            if (diffTime < UPDATE_INTERVAL) {
                return;
            }
            mLastUpdateTime = currentTime;
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float deltaX = x - mLastX;
            float deltaY = y - mLastY;
            float deltaZ = z - mLastZ;
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            float delta = (float) (Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / diffTime * 10000);
            // 当加速度的差值大于指定的阈值，认为这是一个摇晃
            if (delta > shakeThreshold) {
                vibrator.vibrate(300);
                edtTxtMsg.setText("");
                edtTxtMsg.setText("5");
                btnSend.performClick();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private void initView() {
        chatMsgAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, msgList);
        lstViewMsgs = findViewById(R.id.lstViewChatContent);
        lstViewMsgs.setAdapter(chatMsgAdapter);
        lstViewMsgs.setFastScrollEnabled(true);
        temp = findViewById(R.id.temp);
        hump = findViewById(R.id.hump);

        edtTxtMsg = findViewById(R.id.edtTxtMsg);
        edtTxtMsg.clearFocus();

        btnopen = (ImageButton) findViewById(R.id.btnopen);
        btnopen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtTxtMsg.setText("");
                edtTxtMsg.setText("1");
                btnSend.performClick();
            }
        });

        btnclose = (ImageButton) findViewById(R.id.btnclose);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtTxtMsg.setText("");
                edtTxtMsg.setText("2");
                btnSend.performClick();

            }
        });
        btncurtain = (ImageButton) findViewById(R.id.btncurtain);
        btncurtain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtTxtMsg.setText("");
                edtTxtMsg.setText("3");
                btnSend.performClick();
            }
        });
        btnclosecurtain = (ImageButton) findViewById(R.id.btnclosecurtain);
        btnclosecurtain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtTxtMsg.setText("");
                edtTxtMsg.setText("4");
                btnSend.performClick();
            }
        });

        btnpress = (ImageButton) findViewById(R.id.btnpress);
        btnpress.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                edtTxtMsg.setText("");
                edtTxtMsg.setText("5");
                btnSend.performClick();
                return false;
            }
        });
        btnpress1 = (ImageButton) findViewById(R.id.btnpress1);
        btnpress1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                edtTxtMsg.setText("");
                edtTxtMsg.setText("6");
                btnSend.performClick();
                return false;
            }
        });
        btnstopmusic = (ImageButton) findViewById(R.id.btnstop);
        btnstopmusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PauseMusic();
                edtTxtMsg.setText("");
                edtTxtMsg.setText("j");
                btnSend.performClick();
            }
        });
        btntemp = (ImageButton) findViewById(R.id.btntemp);
        btntemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtTxtMsg.setText("");
                edtTxtMsg.setText("7");
                btnSend.performClick();
        }
        });
        btnstoptemp = (ImageButton) findViewById(R.id.btnstoptemp);
        btnstoptemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtTxtMsg.setText("");
                edtTxtMsg.setText("8");
                btnSend.performClick();
            }
        });

        btnclear = (ImageButton) findViewById(R.id.btnclear);
        btnclear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             clear();
                edtTxtMsg.setText("");
                temp.setText("温度:");
                hump.setText("湿度:");
            }
        });

        btnSend = (ImageButton) findViewById(R.id.btnSendMsg);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = edtTxtMsg.getText().toString();
                if (msg.length() > 0) {
                    sendMessage(msg);

                    edtTxtMsg.setText("");
                    edtTxtMsg.clearFocus();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtTxtMsg.getWindowToken(), 0);
                } else {
                    Toast.makeText(ChatActivity.this, "请输入消息内容", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnDisconnect = findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BluetoothUtil.isServer) {
                    shutdownServer();
                } else {
                    shutdownClient();
                }
                BluetoothUtil.isOpen = false;
                Toast.makeText(ChatActivity.this, "已断开连接", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void clear() {
        msgList.clear();
        chatMsgAdapter.notifyDataSetChanged();
    }
    private  void  PauseMusic(){
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.putExtra("type",PAUSE);
        startService(intent);
    }
    private  void  PlayMusic(){
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.putExtra("type",PLAY);
        startService(intent);
    }

    private void sendMessage(String msg) {
        if (socket == null) {
            Toast.makeText(this, "蓝牙没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            OutputStream os = socket.getOutputStream();
            os.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        msgList.add(msg);
        chatMsgAdapter.notifyDataSetChanged();
        lstViewMsgs.setSelection(msgList.size() - 1);
    }

    @Override
    protected void onPostResume() {

        //服务器
        //  BluetoothUtil.isServer = true;
        if (BluetoothUtil.isOpen) {
            Toast.makeText(this, "连接已经打开,可以直接通信！", Toast.LENGTH_SHORT).show();
        } else {
            if (BluetoothUtil.isServer) {
                serverThread = new ServerThread();
                serverThread.start();
                BluetoothUtil.isOpen = true;
            } else {
                if (BluetoothUtil.bluetoothAddress == null) {
                    Toast.makeText(this, "蓝牙地址为空！", Toast.LENGTH_SHORT).show();
                } else {
                    bluetoothDevice = bluetoothAdapter.getRemoteDevice(BluetoothUtil.bluetoothAddress);
                    clientThread = new ClientThread();
                    clientThread.start();
                    BluetoothUtil.isOpen = true;
                }
            }
        }
        super.onPostResume();
    }



    private void shutdownServer() {
        new Thread() {
            public void run() {
                if (serverThread != null) {
                    serverThread.interrupt();
                    serverThread = null;
                }
                if (readThread != null) {
                    readThread.interrupt();
                    readThread = null;
                }
                try {
                    if (socket != null) {
                        socket.close();
                        socket = null;
                    }
                    if (serverSocket != null) {
                        serverSocket.close();
                        serverSocket = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void shutdownClient() {
        new Thread() {
            public void run() {
                if (clientThread != null) {
                    clientThread.interrupt();
                    clientThread = null;
                }
                if (readThread != null) {
                    readThread.interrupt();
                    readThread = null;
                }
                try {
                    if (socket != null) {
                        socket.close();
                        socket = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private class ClientThread extends Thread {
        public void run() {
            try {
                socket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(SERVER_CLIENT_UUID));
                Message msg = new Message();
                msg.obj = "客户端连接服务器中：" + BluetoothUtil.bluetoothAddress;
                handler.sendMessage(msg);
                msg = new Message();
                socket.connect();
                msg.obj = "已经连接上服务器! 可以发送短信";
                handler.sendMessage(msg);
                readThread = new ReadThread();
                readThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class ServerThread extends Thread {
        public void run() {
            try {
                serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(SERVER_NAME,
                        UUID.fromString(SERVER_CLIENT_UUID));
                Message msg = new Message();
                msg.obj = "等待客户端连接中...";
                handler.sendMessage(msg);

                msg = new Message();
                socket = serverSocket.accept();
                msg.obj = "客户端已经连接! 可以发送短信";
                handler.sendMessage(msg);

                readThread = new ReadThread();
                readThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReadThread extends Thread {
        public void run() {
            byte[] buffer = new byte[1024];
            InputStream is = null;
            int ch;
            try {
                 is = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    if ((ch = is.read(buffer)) > 0) {
                        byte[] readed = new byte[ch];
                        System.arraycopy(buffer, 0, readed, 0, ch);
                        String s = new String(readed);
                        Message msg = new Message();
                        msg.obj = s;
                        msg.what = 1;
                        handler.sendMessage(msg);
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
