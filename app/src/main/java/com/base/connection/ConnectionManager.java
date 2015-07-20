package com.base.connection;

import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.sensoro.bootcompleted.MyApplication;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionManager {

    private static final int CORE_POOL_SIZE = 20;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 5;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    // private static final String API_SECRET =
    // "c6dd9d408c0bcbeda381d42955e08a3f";
    public static final String API_SECRET = "a1211288d9e7a1eb35e1203c113c603f";
    public static String mToken = null;
    public static Gson GSON=new Gson();
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
    

    
    protected static void get(String url, HashMap<String, Object> params, final HttpResponseHandler handler) {

        String paramsData ="";
        RequestParams request =null;
        if (params != null){
            request = mapToRequestParams(params);
            paramsData = AsyncHttpClient.getUrlWithQueryString(false, "", request) == null
                || AsyncHttpClient.getUrlWithQueryString(false, "", request) == "" ? "" : AsyncHttpClient
                .getUrlWithQueryString(false, "", request);
        }
        sendRequest(HTTP_METHOD.GET, url, paramsData, handler,request);
    }
    public static RequestParams mapToRequestParams(Map<String, Object> map) {

        String key;
        Object value;
        RequestParams params = new RequestParams();
        Iterator<String> iter = map.keySet().iterator();

        while (iter.hasNext()) {
            key = iter.next();
            value = map.get(key);
            if (value instanceof String[]) {
                String[] temp = (String[]) value;
                for (int i = 0; i < temp.length; i++) {
                    params.add(key + "[" + i + "]", temp[i]);
                }
            } else {
                params.put(key, value);
            }
        }
        return params;
    }


    protected static void post(String url, HashMap<String, Object> params, final HttpResponseHandler handler) {
        String paramsData = "";

        paramsData = GSON.toJson(params);

        Log.v("tinglog","param"+paramsData);
        sendRequest(HTTP_METHOD.POST, url, paramsData, handler, null);
    }


    
    @SuppressWarnings({ "incomplete-switch", "rawtypes", "unchecked" })
    private static void sendRequest(HTTP_METHOD method, final String url, String paramsData,
            final HttpResponseHandler responseHandler,RequestParams request) {
        mClient = new AsyncHttpClient();
        BlockingQueue<Runnable> concurrentPoolWorkQueue = new LinkedBlockingQueue<Runnable>(30);
        ThreadFactory concurrentThreadFactory = new AsyncTaskFactory();
        mClient.setThreadPool(new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TIME_UNIT,
                concurrentPoolWorkQueue, concurrentThreadFactory));
        mClient.setTimeout(5000);
        TextHttpResponseHandler handler = new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                responseHandler.onFailure(i, s);
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                responseHandler.onSuccess(i,s);
            }
        };

        String sendUrl=url;
        if(!url.contains("http"))
            sendUrl=MyApplication.BASE_URL + url;

            

        switch (method) {
            case GET:
                Log.v("tinglog","get"+request.toString());
                mClient.get(sendUrl, request, handler);

                break;

            case POST:

                try {
                    Log.v("tinglog","post:"+paramsData);
                    mClient.post(MyApplication.getContext(),sendUrl, new StringEntity(paramsData, HTTP.UTF_8), "application/json", handler);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
        }

    }
}
