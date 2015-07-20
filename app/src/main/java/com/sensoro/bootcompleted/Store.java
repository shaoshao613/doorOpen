package com.sensoro.bootcompleted;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by shaoting on 14/12/30.
 */
public class Store {
    public static final String RONGYUN_CONNECT_SUCCESS = "rongyun_connect_success";
    public static final String LOCATION_PLACE_NAME = "location_place_name";
    public static final String LOCATION_PLACE_ENGLISH_NAME = "location_place_english_name";
    public static final String LOCATION_PLACE_ID = "location_place_id";
    public static final String USER_ID = "user_id";
    public static final String HOME_CONFIG_V2 = "home_config_v2";
    public static final String USER_PROFILE = "user_profile";
    public static final String PHONE_NUM_HINT = "phone_num_hint";
    public static final String SEARCH_RECORD = "search_record";
    public static final String KEY_TOKEN = "key_token";
    public static final String USER_RONGYUN_TOKEN = "user_rongyun_token";
    public static final String LAST_USED_GID = "last_used_gid";
    public static final String LOGIN_COUNT = "login_count";
    public static final String All_CITY="city_all"; //省市
    public static final String POP_CITY="city_pop";
    public static final String MAP_CITY="map_pop"; //市
    public static final String LAST_INSERT="last_insert"; //市
    public static final String TRACK_INFO="track_info";
    public static final String TRACK_NB_APPLY_STATUS="track_nb_apply_status";
    public static final String TRACK_NB_APPLY_FAIL="track_nb_apply_FAIL";
    public static SharedPreferences mSharedPreferences;

    public static void init(Application application){

        mSharedPreferences= PreferenceManager.getDefaultSharedPreferences(application);
    }



    public static String getSharedPreferenceString(String key) {
        return Store.mSharedPreferences.getString(key, null);
    }

    public static void saveSharedPreferenceInteger(String key, int value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value).commit();
    }

    public static int getSharedPreferenceInteger(String key) {
        return mSharedPreferences.getInt(key, 0);
    }

    public static void saveSharedPreferenceString(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value).commit();
    }

    public static void removeSharedPreferenceValue(String key) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(key).commit();
    }
    public static boolean isUpdate(String tagName,int version){
        if(getSharedPreferenceInteger(tagName)==version)
            return true;
        saveSharedPreferenceInteger(tagName,version);
        return false;
    }


}
