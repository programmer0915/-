package com.dream.androidapp;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DBManager {

   public SQLiteDatabase openDatabase(Context context) {
       //数据库存储路径
        String DBPath = "data/data/com.dream.androidapp/citychina.db";
       //数据库存放的文件夹
        String DBFolder = "data/data/com.dream.androidapp";
       File File = new File(DBPath);
       //查看数据库文件是否存在
       if (File.exists()) {
           Log.i("提示", "存在数据库");
           //存在则直接返回打开的数据库
           return SQLiteDatabase.openOrCreateDatabase(File, null);
       } else {
           //不存在先创建文件夹
           java.io.File File2 = new File(DBFolder);
           if (File2.mkdir()) {
               Log.i("提示", "创建成功");
           } else {
               Log.i("提示", "创建失败");
           }
           ;
           try {
               //得到资源
               AssetManager am = context.getAssets();
               //得到数据库的输入流
               InputStream is = am.open("citychina.db");
               //用输出流写到SDcard上面
               FileOutputStream fos = new FileOutputStream(DBPath);
               //创建byte数组  用于1KB写一次
               byte[] buffer = new byte[1024];
               int count = 0;
               while ((count = is.read(buffer)) > 0) {
                   fos.write(buffer, 0, count);
               }
               //关闭
               fos.flush();
               fos.close();
               is.close();
           } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
               return null;
           }
           return openDatabase(context);
       }
   }
}
