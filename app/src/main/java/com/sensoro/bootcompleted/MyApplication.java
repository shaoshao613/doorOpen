package com.sensoro.bootcompleted;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sensoro.beacon.kit.Beacon;
import com.sensoro.beacon.kit.BeaconManagerListener;
import com.sensoro.cloud.SensoroManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sensoro on 15/3/11.
 */
public class MyApplication extends Application implements BeaconManagerListener{
    private static final String TAG = MyApplication.class.getSimpleName();
    public static String BASE_MOBILE_URL="www.baixing.com";
    private SensoroManager sensoroManager;
    public static String BASE_URL="http://utseus.sinaapp.com/";
    private static MyApplication mContext;
    List<BeaconManagerListener> listenerList=new ArrayList<BeaconManagerListener>();
    public static Context getContext(){
        return mContext;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mContext=this;
        initSensoroSDK();
        Store.init(this);

        /**
         * Start SDK in Service.
         */
        Intent intent = new Intent();
        intent.setClass(this,MyService.class);
        startService(intent);
    }

    /**
     * Initial Sensoro SDK.
     */
    private void initSensoroSDK() {
        sensoroManager = SensoroManager.getInstance(getApplicationContext());
        sensoroManager.setCloudServiceEnable(true);
        sensoroManager.addBroadcastKey("7b4b5ff594fdaf8f9fc7f2b494e400016f461205");
        sensoroManager.setBeaconManagerListener(this);
    }
    public void registerListener(BeaconManagerListener listener){
        listenerList.add(listener);
    }
    public void unRegisterListener(BeaconManagerListener listener){
        listenerList.remove(listener);
    }

    /**
     * Start Sensoro SDK.
     */
    public void startSensoroSDK() {
        try {
            sensoroManager.startService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check whether bluetooth enabled.
     * @return
     */
    public boolean isBluetoothEnabled(){
        return sensoroManager.isBluetoothEnabled();
    }

    @Override
    public void onNewBeacon(Beacon beacon) {
        Log.v("debuglog","onNewBeacon(");
        /**
         * Check whether SDK started in logs.
         */
        for(BeaconManagerListener listener:listenerList){
            listener.onNewBeacon(beacon);
        }

    }

    @Override
    public void onGoneBeacon(Beacon beacon) {
        Log.v("debuglog","onGoneBeacon");
        for(BeaconManagerListener listener:listenerList){
            listener.onNewBeacon(beacon);
        }
    }

    @Override
    public void onUpdateBeacon(ArrayList<Beacon> arrayList) {
        Log.v("debuglog","update Beacon listener number"+listenerList.size());
        for(BeaconManagerListener listener:listenerList){
            for(Beacon beacon:arrayList){
                Log.v("debuglog", "update application "+beacon.getSerialNumber());
            }
            listener.onUpdateBeacon(arrayList);
        }
    }
}
