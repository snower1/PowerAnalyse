package com.wallen.poweranalyse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
            int intLevel= 0;   //剩余电量
    private int intVolta;   //电压
    private int intStatus;  //是否充电
    private int intTemper;  //温度
    private TextView textView;
    private LinearLayout lineArc;
    private WaterWaveView wwvBat;
    private RelativeLayout linear;


    //从外部获取剩余电量
    public int getIntLevel (){
        return intLevel;
    }


    // 创建BroadcastReceiver
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
                onBatteryInfoReceiver( intVolta,intStatus,intTemper);
                wwvBat.setWateLevel(getIntLevel()/100.00f);


            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //创建广播接受者对象
        BatInfoReceiver batteryReceiver = new BatInfoReceiver();

        registerReceiver(batteryReceiver, intentFilter);

        //  绘制电量圈图
        wwvBat = (WaterWaveView) findViewById(R.id.ownView);
      //wwvBat.setWateLevel(getIntLevel()/100.00f);
        wwvBat.startWave();


        //其他电池信息
        textView = (TextView) findViewById(R.id.tv_BatInfo);


        //关于记录电池历史纪录的的服务启动
        Intent serviceIntent = new  Intent(this,SaveRecordService.class);
        startService(serviceIntent);


        //线性图  范围10-100
        //  List<Integer> lists = new ArrayList<Integer>();//线性图  范围10-100
        List<Integer> lists = new ArrayList<>();

        try {
            lists = getHistory(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        linear= (RelativeLayout) findViewById(R.id.linear);
        linear.addView(new HomeDiagram(this,lists));



    }





    //得到电池电量变化应该执行的动作
    private void onBatteryInfoReceiver (int intVolta,int intStatus,int intTemper) {



        String batteryInfo;

        if(intStatus == 0) {  //0 表示在放电
            batteryInfo= "温度:" + intTemper + "℃ 电压:" + intVolta/1000.00f +"V\n已断开电源" ;
        }
        else
            batteryInfo= "温度:" + intTemper + "℃ 电压:" + intVolta/1000.00f +"V\n已接通电源";

        textView.setText(batteryInfo);


    }


    // 按钮事件，打开另一个activity
    public void turnToPowerRank (View view) {
        Intent intent = new Intent(this,ListDetailActivity.class);
        startActivity(intent);
    }



    //读取记录电池历史文件的内容
    public List<Integer> getHistory(Context context ) throws IOException {
        List<Integer> list= new ArrayList<Integer>();
        String[] listHistory = null;

        FileInputStream inStream =context.openFileInput("History.bin");
        String history = inputStream2String(inStream);

        listHistory = history.split(" ");

        //System.out.println(listHistory[1]);
        for(int li = 0 ;li < listHistory.length;li++) {
            list.add(Integer.parseInt(listHistory[li]));
        }


        return list;
    }

    //输入流转成字符串
    public String  inputStream2String (InputStream in)  throws  IOException  {
        StringBuffer out = new StringBuffer();
        byte[]  b = new byte[4096];
        for   (int n;   (n = in.read(b)) !=  -1;)   {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

}
