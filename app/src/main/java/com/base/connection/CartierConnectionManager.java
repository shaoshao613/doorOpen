package com.base.connection;

import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.sensoro.bootcompleted.MyApplication;
import com.sensoro.bootcompleted.NetworkUtil;
import com.sensoro.bootcompleted.Store;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CartierConnectionManager {

    private static final int CORE_POOL_SIZE = 20;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 5;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    // private static final String API_SECRET =
    // "c6dd9d408c0bcbeda381d42955e08a3f";
    public static final String API_SECRET = "a1211288d9e7a1eb35e1203c113c603f";
    public static String mToken = null;
    public static Gson GSON;
    private static AsyncHttpClient mClient;
    @SuppressWarnings("unused")
    private static String APP_VERSION = "";
    public static String DEVICE_UDID = "";
    private static long BAPI_NONCE;
    private static String BAPI_HASH = "";
    private static String user_id = "";
    private static PersistentCookieStore myCookieStore;


    private static class AsyncTaskFactory implements ThreadFactory {
        private final AtomicInteger count;
        {
            count = new AtomicInteger(1);
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "http Thread #" + count.getAndIncrement());
        }
    }
    
    public enum HTTP_METHOD {
        GET, POST, DELETE, PUT,POSTTRACK
    }
    
//    protected static void get(String url, HashMap<String, Object> params, final HttpResponseHandler handler) {
//
//        String paramsData ="";
//        RequestParams request =null;
//        if (params != null){
//            request = MapToParameter.mapToRequestParams(params);
//            paramsData = AsyncHttpClient.getUrlWithQueryString(false, "", request) == null
//                || AsyncHttpClient.getUrlWithQueryString(false, "", request) == "" ? "" : AsyncHttpClient
//                .getUrlWithQueryString(false, "", request);
//        }
//        sendRequest(HTTP_METHOD.GET, url, paramsData, handler,request);
//    }
//
    protected static void postTrack(String url, String paramsData, final HttpResponseHandler handler) {
        if(!TextUtils.isEmpty(paramsData))
        sendRequest(HTTP_METHOD.POSTTRACK, url, paramsData, handler,null);
    }
    protected static void post(String url, HashMap<String, Object> params, final HttpResponseHandler handler) {
        String paramsData = "";
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
        GSON = gsonBuilder.create();
        paramsData = GSON.toJson(params);
        sendRequest(HTTP_METHOD.POST, url, paramsData, handler, null);
    }


    
    @SuppressWarnings({ "incomplete-switch", "rawtypes", "unchecked" })
    private static void sendRequest(HTTP_METHOD method, final String url, String paramsData,
            final HttpResponseHandler responseHandler, final RequestParams request) {
        BAPI_NONCE = System.currentTimeMillis();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
        GSON = gsonBuilder.create();
        mClient = new AsyncHttpClient();
        BlockingQueue<Runnable> concurrentPoolWorkQueue = new LinkedBlockingQueue<Runnable>(30);
        ThreadFactory concurrentThreadFactory = new AsyncTaskFactory();
        mClient.setThreadPool(new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TIME_UNIT,
                concurrentPoolWorkQueue, concurrentThreadFactory));
        mClient.setTimeout(5000);
        mClient.addHeader("BAPI-APP-KEY", "api_cartier");
        DEVICE_UDID = Secure.getString(MyApplication.getContext().getContentResolver(), Secure.ANDROID_ID);
        mClient.addHeader("UDID", DEVICE_UDID);
        mToken = Store.getSharedPreferenceString(Store.KEY_TOKEN);
        // mClient.addHeader("APP_VERSION", APP_VERSION);
        // BAPI_HASH = MD5Util.MD5();
        if (!TextUtils.isEmpty(mToken)) {
            mClient.removeHeader("BAPI-USER-TOKEN");
            mClient.addHeader("BAPI-USER-TOKEN", mToken);
        }
        myCookieStore = new PersistentCookieStore(MyApplication.getContext());
        mClient.setCookieStore(myCookieStore);

        mClient.addHeader("BAPI-NONCE", String.valueOf(BAPI_NONCE));
        BAPI_HASH = DEVICE_UDID + (mToken == "" || mToken == null ? "" : mToken) + "/api/mobile/" + url + paramsData
                + API_SECRET + BAPI_NONCE;
        Log.e("hash", BAPI_HASH);
        mClient.addHeader("BAPI-HASH", NetworkUtil.getMD5(BAPI_HASH));
        Log.i("api", url + " paramsData:" + paramsData);
        Log.e("hashCode", NetworkUtil.getMD5(BAPI_HASH));
        TextHttpResponseHandler handler = new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                responseHandler.onFailure(i, s);
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                responseHandler.onSuccess(i, s);
            }
        };

        switch (method) {
            case GET:
                mClient.get(MyApplication.BASE_MOBILE_URL + url, request, handler);
                break;

            case POST:
                try {
                    if (url.contains("arch/enableWebAccess")) {
                        mClient.post(MyApplication.getContext(), MyApplication.BASE_URL + url,
                                new StringEntity(paramsData, HTTP.UTF_8), "application/json", handler);
                        break;
                    }
                    mClient.post(MyApplication.getContext(), getAbsoluteUrl(url), new StringEntity(paramsData,
                            HTTP.UTF_8), "application/json", handler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            //track 接口
            case POSTTRACK:
                try {
                    mClient.addHeader("BAPI-HASH", NetworkUtil.getMd5ISO(BAPI_HASH));
                    ByteArrayEntity bae = new ByteArrayEntity(paramsData.getBytes(HTTP.ISO_8859_1));
                    mClient.post(MyApplication.getContext(), getAbsoluteUrl(url),bae, "application/octet-stream", handler);
                } catch (Exception e){
                    Log.v("tinglog", "gzip e" + e.toString());
                }
                break;
        }
    }
    
    private static String getAbsoluteUrl(String relativeUrl) {
        return MyApplication.BASE_MOBILE_URL + relativeUrl;
    }
}
