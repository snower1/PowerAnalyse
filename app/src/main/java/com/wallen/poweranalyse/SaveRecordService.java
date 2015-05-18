package com.wallen.poweranalyse;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveRecordService extends Service {
    private static final String TAG = "SaveRecordService";
    private int intLevel,intVolta,intStatus,intTemper;

    class  BatInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // 如果捕捉到action是ACRION_BATTERY_CHANGED
            // 就运行onBatteryInfoReceiver()
            if (intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                intLevel = intent.getIntExtra("level", 0);
                intVolta = intent.getIntExtra("voltage",0);
                intStatus = intent.getIntExtra("plugged",0);
                //获取的实际温度需要除以10；
                intTemper = intent.getIntExtra("temperature",20) / 10 ;
                // boolean b = (intent.getStringExtra("temperature") == null)?true:false;
                //Log.i("Boolean rf", b + "");

            }
        }
    }


    public SaveRecordService() {


    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"SaveRecordService Created");
        final  long delayedTime = 1000*5;  //定时器时间 单位：毫秒；

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //创建广播接受者对象
        BatInfoReceiver batteryReceiver = new BatInfoReceiver();

        registerReceiver(batteryReceiver, intentFilter);
        //Insert runnable Object
        final Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            @Override
            public void run() {
            // TODO Auto-generated method stub
                try {
                    toDO();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                handler.postDelayed(this, delayedTime);// 延时时长
            }
        };
        handler.postDelayed(runnable, delayedTime);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"SaveRecordService Destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }




    //定时执行电池剩余电量检查并记录
    public void saveRecord ( ) throws FileNotFoundException {

        String mData ;

        FileOutputStream outStream;
        mData = intLevel+" " ;

            outStream = this.openFileOutput("History.bin", Context.MODE_APPEND);

        try {
            outStream.write(mData.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //对记录文件进行重置，并记录新的数据。
    public void resetRecord() throws FileNotFoundException {
        String mData ;

        FileOutputStream outStream = null;
        mData = intLevel+" " ;

            outStream = this.openFileOutput("History.bin", Context.MODE_PRIVATE);


        try {
            outStream.write(mData.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //需要重复执行的任务
    public void toDO() throws FileNotFoundException {
         if (intLevel==100 &&intStatus==1) {
             resetRecord();
         }else{
             saveRecord();
         }
     }
}
