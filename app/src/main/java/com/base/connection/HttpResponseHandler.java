package com.base.connection;

/**
 * Created by shaoting on 15/6/11.
 */
public interface HttpResponseHandler {
    public void onSuccess(int statusCode, String obj);

    public void onFailure(int statusCode,  String obj);
}
