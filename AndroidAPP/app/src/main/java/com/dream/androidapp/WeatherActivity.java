package com.dream.androidapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.Voice;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.dream.androidapp.control.InitConfig;
import com.dream.androidapp.control.MyRecognizer;
import com.dream.androidapp.control.MySyntherizer;
import com.dream.androidapp.control.NonBlockSyntherizer;
import com.dream.androidapp.listener.UiMessageListener;
import com.dream.androidapp.recognization.CityRecogListener;
import com.dream.androidapp.recognization.MessageStatusRecogListener;
import com.dream.androidapp.recognization.StatusRecogListener;
import com.dream.androidapp.recognization.offline.OfflineRecogParams;
import com.dream.androidapp.util.AutoCheck;
import com.dream.androidapp.util.OfflineResource;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WeatherActivity extends Activity {

    private static final String TAG = "WeatherActivity";

    public static final int MSG_Query = 1;
    public static final int MSG_RECOG = 2;

    private ImageButton voice;

    private TextView content;

    private EditText wendu;

    private EditText shidu;

    private EditText name;

    private EditText high;

    private EditText low;


    private ImageView image = null;

    private Bitmap bitmapWeather = null;


    private Handler handler = new Handler() {
        //消息处理
        public void handleMessage(Message msg) {
            Runnable task = null;
            switch (msg.what) {
                case MSG_Query:
                    try {
                        JSONObject yesterday = (JSONObject)((JSONObject) msg.obj).get("yesterday");
                        content.setText(yesterday.getString("notice"));
                        high.setText(yesterday.getString("high"));
                        low.setText(yesterday.getString("low"));
                        String type = yesterday.getString("type");
                        switch(type){
                            case "晴":
                                Drawable sun= ContextCompat.getDrawable(getApplicationContext(),R.drawable.sun);
                                image.setBackground(sun);
                                break;
                                default:
                                    Drawable cloud= ContextCompat.getDrawable(getApplicationContext(),R.drawable.cloud);
                                    image.setBackground(cloud);
                                    break;
                        }

                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(),"解析有误",Toast.LENGTH_SHORT).show();
                    }
                    try {
                        JSONObject object = (JSONObject) msg.obj;
                        wendu.setText("温度 " + object.getString("wendu") + ".0℃");
                        shidu.setText("湿度  " + object.getString("shidu"));
                    }catch (Exception e){
                       Toast.makeText(getApplicationContext(),"解析有误",Toast.LENGTH_SHORT).show();
                    }
                    String cityvalue = name.getText().toString();
                    String wenduvalue = wendu.getText().toString();
                    String shiduvalue = shidu.getText().toString();
                    String highvalue = high.getText().toString();
                    String lowvalue = low.getText().toString();
                    String contentvalue = content.getText().toString();
                    speek.Speeking("当前城市"+ cityvalue + wenduvalue  + shiduvalue + highvalue + lowvalue + contentvalue);
                    break;

                case MSG_RECOG:

                    final String city = (String) msg.obj;
                    EditText value = findViewById(R.id.name);
                    value.setText(city);
                    task = new Runnable() {
                        @Override
                        public void run() {
                            fetchWeather(city);
                        }
                    };
                    break;
            }
            if (task != null) {
                Thread fetchImagTread = new Thread(task);
                fetchImagTread.start();
            }
        }
    };


    /*语音识别 - - - start*/
    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;
    /*
     * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
     */
    protected boolean enableOffline = false;

    /*语音识别 - - - end*/
    private Speek speek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        speek=new Speek(this);
        initView();
        speek.Speeking("欢迎使用智能天气预报");
        voice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startRough();
            }
        });
        initRecog();
        initPermission();
    }


    private void initView() {
        content = findViewById(R.id.content);
        name =  findViewById(R.id.name);
        shidu = findViewById(R.id.shidu);
        wendu = findViewById(R.id.wendu);
        high =  findViewById(R.id.high);
        low =  findViewById(R.id.low);
        voice =  findViewById(R.id.voice);
        image =  findViewById(R.id.image);
    }
    /**
     * 在onCreate中调用。初始化识别控制类MyRecognizer
     */
    protected void initRecog() {
        StatusRecogListener listener = new MessageStatusRecogListener(handler);
        myRecognizer = new MyRecognizer(this, listener);

        if (enableOffline) {
            myRecognizer.loadOfflineEngine(OfflineRecogParams.fetchOfflineParams());
        }
    }
    /**
     * 测试demo成功后可以修改这个方法
     * 粗略测试，将原来的start方法注释，这个方法改为start即可。
     * 点击开始按钮使用，注意此时与本demo的UI已经解绑，UI上不会显示，请自行看logcat日志
     */
    protected void startRough() {
        // initRecog中已经初始化，这里释放。不需要集成到您的代码中
        myRecognizer.release();
        myRecognizer = null;
        // 上面不需要集成到您的代码中

        /*********************************************/
        // 1. 确定识别参数
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // 具体的params的值在 测试demo成功后，myRecognizer.start(params);中打印

        // 2. 初始化IRecogListener
        StatusRecogListener listener = new CityRecogListener(handler);
        // 日志显示在logcat里，UI界面上是没有的。需要显示在界面上， 这里设置为handler

        // 3 初始化 MyRecognizer
        myRecognizer = new MyRecognizer(this, listener);

        // 4. 启动识别
        myRecognizer.start(params);
        // 日志显示在logcat里，UI界面上是没有的。

        // 5 识别结束了别忘了释放。

        // 需要离线识别过程，需要加上 myRecognizer.loadOfflineEngine(OfflineRecogParams.fetchOfflineParams());
        // 注意这个loadOfflineEngine是异步的， 不能连着调用 start
    }

    /**
     * 销毁时需要释放识别资源。
     */
    @Override
    protected void onDestroy() {
        myRecognizer.release();
        Log.i(TAG, "onDestory");
        //释放资源
        speek.Destory();
        super.onDestroy();
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.RECORD_AUDIO,
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }


    private void fetchWeather(String strCity) {
        HttpURLConnection connection=null;
        try{
            //打开数据库输出流
            DBManager dbManager = new DBManager();
            SQLiteDatabase sqLiteDatabase = dbManager.openDatabase(getApplicationContext());
            Cursor cursor = sqLiteDatabase.rawQuery("select * from city_table where CITY=?", new String[]{strCity});
            String weather = null;
            if (cursor.moveToFirst()) {
                weather = cursor.getString(cursor.getColumnIndex("WEATHER_ID"));
                String weatherUrl = "http://t.weather.sojson.com/api/weather/city/" + weather;
                    String weatherJson = queryStringForGet(weatherUrl);
                JSONObject jsonObject = new JSONObject(weatherJson);
                JSONObject weatherObject = jsonObject.getJSONObject("data");
                    Message message = new Message();
                    message.obj = weatherObject;
                    message.what = MSG_Query ;
                    handler.sendMessage(message);
            }
            cursor.close();
        }catch(Exception e){
            Toast.makeText(getApplicationContext(),"解析有误",Toast.LENGTH_SHORT).show();
        }
    }
    private String queryStringForGet(String url) {
        String result = null;
        BufferedReader reader;
        HttpURLConnection connection;
        try {
            URL WeatherUrl = new URL(url);
            connection = (HttpURLConnection) WeatherUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            int backCode = connection.getResponseCode();
            InputStream in = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in));
            if (backCode == 200) {
                String Line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((Line = reader.readLine()) != null) {
                    stringBuilder.append(Line);
                    result = stringBuilder.toString();
                }
                reader.close();
                connection.disconnect();
            } else {
                Toast.makeText(WeatherActivity.this, "网络错误",Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}