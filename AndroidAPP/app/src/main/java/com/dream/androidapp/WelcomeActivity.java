package com.dream.androidapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends Activity {
    private ImageView welcome;
    private Button btn_go;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Toast toast = Toast.makeText(WelcomeActivity.this,
                "3s后自动进入...",
                Toast.LENGTH_SHORT);
        toast.show();
        //添加定时器
        Timer timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                Intent intent1=new Intent(WelcomeActivity.this,MainActivity.class);
                startActivity(intent1);
                WelcomeActivity.this.finish();
            }
        };
        timer.schedule(timerTask,3000);

        welcome =(ImageView) findViewById(R.id.image);
        //创建Animation对象，关联nim文件
        Animation anim= AnimationUtils.loadAnimation(this, R.anim.begin);
        //将ImageView对象执行动画
        welcome.startAnimation(anim);
    }
}
