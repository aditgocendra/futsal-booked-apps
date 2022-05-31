package com.ark.futsalbookedapps.Globals;

import java.util.HashMap;

public class Data {
    // Data Account User
    public static String uid;
    public static String username;
    public static String email;
    public static String numberPhone;
    public static int role;

    // Data notification
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMessageHeaders = null;
    public static HashMap<String, String> getRemoteMessageHeaders(){
        if (remoteMessageHeaders == null){
            remoteMessageHeaders = new HashMap<>();
            remoteMessageHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAjEZ1E-o:APA91bGaHptEx-cVf30C8g9wV94XST5Q5fWhU85YrfOmjLfW1OZAvACQCgVDe9WjTDAyNlDWPTzgFs3lAtSvdAP4nng8wjZE5RuXFXx4E_zMsm17R37fd3w_mwg_E72La-65ackfjGnC"
            );
            remoteMessageHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMessageHeaders;
    }

}
