package com.sensoro.bootcompleted;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.base.connection.BaseConnectionManager;
import com.base.connection.HttpResponseHandler;
import com.com.sensoro.model.BxModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sensoro.beacon.kit.Beacon;
import com.sensoro.beacon.kit.BeaconManagerListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 10000;

    private MyApplication application;
    private BluetoothBroadcastReceiver bluetoothBroadcastReceiver;
    private HashMap<String,String> deviceMap=new HashMap<String,String>();
    private HashMap<String,Double> deviceDistanceMap=new HashMap<String,Double>();
    public Boolean hasShowToast=false;
    public String sn;
    public Context mContext=this;
    private boolean autoOpen=true;
    private boolean lengque=true;
    private boolean left=true;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    final String s = msg.obj.toString();
                    //
                        if(autoOpen=true&&left){
                            autoOpen=false;
                            handler.sendMessage(handler.obtainMessage(7, s));
                        }
                        message.setText("开启" + s + "楼的大门");
                        message.setBackgroundResource(R.color.link_text_material_light);
                        message.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openDoor(s);
                            }
                        });
                        hasShowToast=true;
                    break;
                case 2:
                    hasShowToast=false;
                    message.setText("暂不可用");
                    message.setOnClickListener(null);
                    message.setBackgroundResource(R.color.background_floating_material_light);
                    break;
                case 3:
                    lengque=true;
                    if(distance<2){
                        message.setText("开启大门");
                        message.setBackgroundResource(R.color.link_text_material_light);
                        message.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openDoor("");
                            }
                        });
                    }
                    listMsg2.add("冷却完成");
                    arrayAdapter2.notifyDataSetChanged();
                    break;

                case 4:
                    String messageStr=msg.obj.toString();

                    listMsg.add(messageStr+getCurrentDate());
                    arrayAdapter.notifyDataSetChanged();

                    break;
                case 6:
                    String messageStr2=msg.obj.toString();

                    listMsg2.add(messageStr2+getCurrentDate());
                    arrayAdapter2.notifyDataSetChanged();

                    break;
                case 7:
                    if(lengque){
                        handler.sendMessage(handler.obtainMessage(6, "自动开启"));
                        String messageStr3=msg.obj.toString();
                        openDoor(messageStr3);
                        left=false;
                    }

                    break;
                case 5:
                    listMsg2.add(msg.obj.toString()+getCurrentDate());
                    arrayAdapter2.notifyDataSetChanged();


                    break;
            }

        }
    };
    private TextView message;
    private String floor="18";
    private String mobile="15121198105";
    private ListView listview;
    private List<String> listMsg=new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private ArrayAdapter<String> arrayAdapter2;
    private ListView listview2;
    private List<String> listMsg2=new ArrayList<String>();

    public void openDoor(String s){

        WinToast.toast(mContext, "正在为您打开"+s+"的大门");
        HashMap<String,Object> map=new HashMap<String,Object>();
        map.put("f",floor);
        map.put("m",mobile);
        String hash=NetworkUtil.getMD5(NetworkUtil.getMD5(mobile+floor)+"6717e165cf47fe542595e03b5751a8ed");
        map.put("h",hash);

        message.setOnClickListener(null);
        message.setText("暂不可用");
        message.setBackgroundResource(R.color.background_floating_material_light);
        handler.sendMessage(handler.obtainMessage(6,"已开启"));
        lengque=false;
        handler.sendEmptyMessageDelayed(3,6000*2);

        Log.v("tinglog","门开");
        BaseConnectionManager.openDoor(map, new HttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, String obj) {
                try {
                    JSONObject object=new JSONObject(obj);
                    if(object.optString("return").contains("OK")){
                        WinToast.toast(mContext, "已开启");

                        lengque=false;
                        handler.sendEmptyMessageDelayed(3,6000*2);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, String obj) {


            }
        });

    }

    public double distance;
    public static String getCurrentDate() {
        Date d = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss");
        return sf.format(d);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        message= (TextView) findViewById(android.R.id.message);
        message.setBackgroundResource(R.color.background_floating_material_light);
        listview = (ListView) findViewById(R.id.listview);
        listMsg.add("日志开启");
        arrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, listMsg);
        listview.setAdapter(arrayAdapter);
        listview2 = (ListView) findViewById(R.id.listview2);
        listMsg2.add("日志开启");
        arrayAdapter2 = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, listMsg2);
        listview2.setAdapter(arrayAdapter2);
        application = (MyApplication) getApplication();
        application.registerListener(new BeaconManagerListener() {
            @Override
            public void onNewBeacon(Beacon beacon) {


            }

            @Override
            public void onGoneBeacon(Beacon beacon) {
                if(deviceMap.containsKey(beacon.getSerialNumber())){
                    handler.sendMessageDelayed(handler.obtainMessage(5, "离开" ),1000);
                }
            }

            @Override
            public void onUpdateBeacon(ArrayList<Beacon> beacons) {

                for(Beacon beacon:beacons){
                    if(deviceMap.containsKey(beacon.getSerialNumber())){

                        //String l="您距离"+deviceMap.get(beacon.getSerialNumber())+"还有"+beacon.getAccuracy()*100+"厘米";
                        Double oldDistance=-1.0;
                        if(deviceDistanceMap.containsKey(beacon.getSerialNumber())){
                            oldDistance=deviceDistanceMap.get(beacon.getSerialNumber());
                        }
                        deviceDistanceMap.put(beacon.getSerialNumber(), beacon.getAccuracy());
                        distance=beacon.getAccuracy();
                        Log.v("tinglog","old:"+oldDistance+"new:"+distance);
                        if(distance>5&&!left){
                            left=true;
                            if(lengque) {
                                autoOpen = true;
                            }
                            handler.sendMessage(handler.obtainMessage(5, "离开"));
                        }
                        if(Math.abs(distance-oldDistance)<0.5)
                            return;
                        handler.sendMessage(handler.obtainMessage(4,"距离"+deviceMap.get(beacon.getSerialNumber())+"楼大门:"+distance+" "+autoOpen));
                        handler.sendMessage(handler.obtainMessage(4,"自动开启:"+autoOpen+" 曾经远离:"+left+" 技能冷却:"+lengque));
                        if(Math.abs(distance)>2){
                            //设为不可开启
                            handler.sendMessageDelayed(handler.obtainMessage(2, deviceMap.get(beacon.getSerialNumber())),1000);
                            return;
                        }
                        //还未开启过
                        if(!hasShowToast){
                            handler.sendMessage(handler.obtainMessage(6,"已靠近门"));
                            sn=beacon.getSerialNumber();
                            floor=deviceMap.get(beacon.getSerialNumber());
                            handler.sendMessageDelayed(handler.obtainMessage(1, deviceMap.get(beacon.getSerialNumber())),1000);
                            return;
                        }

                    }
                }

            }
        });
//        bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
//        registerReceiver(bluetoothBroadcastReceiver,new IntentFilter(Constant.BLE_STATE_CHANGED_ACTION));

        if (application.isBluetoothEnabled()){
            application.startSensoroSDK();
        } else {
            /**
             * Enable bluetooth by user permission.
             */
//            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(bluetoothIntent, REQUEST_ENABLE_BT);

            /**
             * Enable bluetooth in background.
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.enable();
        }
        final Gson gson=new Gson();
   //    if(TextUtils.isEmpty(Store.getSharedPreferenceString("deviceMap"))){
            BaseConnectionManager.getDeviceInfoList(new BaseConnectionManager.BXHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, List<BxModel> list) {


                    for (BxModel model : list) {
                        deviceMap.put(model.sn,model.tag);
                    }
                    Store.saveSharedPreferenceString("deviceMap", gson.toJson(deviceMap));
                }

                @Override
                public void onFailure(int statusCode, JSONObject json) {
                    String jsonMap=Store.getSharedPreferenceString("deviceMap");
                    if(!TextUtils.isEmpty(jsonMap))
                    deviceMap = gson.fromJson(jsonMap,
                            new TypeToken<HashMap<String, String>>() {
                            }.getType());

                }
            });
//        }else{
//            String json=Store.getSharedPreferenceString("deviceMap");
//            deviceMap = gson.fromJson(json,
//                    new TypeToken<HashMap<String, String>>() {
//                    }.getType());
//        }



    }
    @Override
    protected  void onResume(){
        lengque=true;
        autoOpen=true;
       // handler.sendMessage(handler.obtainMessage(5, "重启"));
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
            application.startSensoroSDK();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
 //      unregisterReceiver(bluetoothBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class BluetoothBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.BLE_STATE_CHANGED_ACTION)){
                if (application.isBluetoothEnabled()){
                    application.startSensoroSDK();
                }
            }
        }
    }
}
