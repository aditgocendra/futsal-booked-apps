package com.ark.futsalbookedapps.Notification;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ServiceAPI {
    @POST("send")
    Call<String> sendMessageNotification(
            @HeaderMap HashMap<String, String> headers,
            @Body String messageBody
    );
}
