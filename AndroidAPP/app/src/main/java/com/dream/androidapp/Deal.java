package com.dream.androidapp;

import android.text.TextUtils;

public class Deal {
    private String temp = "";
    private String humd = "";
    private String buff = "";

    public void parseContent(String content){
        buff += content;
        if (TextUtils.isEmpty(buff)){
            humd = "";
            temp = "";
        }else{
            int humd_value = buff.indexOf("%");
            humd = buff.substring(0, humd_value);
            //temp = buff.substring(humd_value,1);
        }
        }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getHumd() {
        return humd;
    }

    public void setHumd(String humd) {
        this.humd = humd;
    }
}
