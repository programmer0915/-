package com.dream.androidapp.recognization;

import android.os.Handler;
import android.os.Message;

import static com.dream.androidapp.WeatherActivity.MSG_RECOG;


public class CityRecogListener extends StatusRecogListener {
    private Handler handler;

    public CityRecogListener(Handler handler){
        this.handler=handler;
    }
    @Override

    public void onAsrFinalResult(String[] results, RecogResult recogResult) {
        super.onAsrFinalResult(results, recogResult);
        Message msg=new Message();
        msg.obj=results[0];
        msg.what=MSG_RECOG;

        handler.sendMessage(msg);
    }
}
