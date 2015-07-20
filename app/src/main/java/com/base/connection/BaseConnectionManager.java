package com.base.connection;

import com.com.sensoro.model.BxModel;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

/**
 * Created by shaoting on 15/5/13.
 */
public class BaseConnectionManager extends  ConnectionManager{
    public static void getDeviceInfoList(final BXHttpResponseHandler handler){
        post("ibeacon.php", null, new HttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, String obj) {


                Type listTypeToken = new TypeToken<List<BxModel>>() {
                }.getType();
                List<BxModel> list = GSON.fromJson(obj.toString(), listTypeToken);
                handler.onSuccess(statusCode, list);
            }

            @Override
            public void onFailure(int statusCode, String obj) {

            }

        });
    }

    public static void openDoor(HashMap<String,Object> map,final HttpResponseHandler handler){


        get("http://www.baixing.com/arch/pennyworth_app/",map,handler);

    }


    public interface BXHttpResponseHandler {
        public void onSuccess(int statusCode, List<BxModel> list);

        public void onFailure(int statusCode,  JSONObject json);
    }
}
