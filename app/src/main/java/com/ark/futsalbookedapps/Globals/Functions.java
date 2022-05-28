package com.ark.futsalbookedapps.Globals;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;

import java.text.NumberFormat;
import java.util.Locale;

public class Functions {
    public static void updateUI(Context from, Class to){
        Intent intent = new Intent(from, to);
        from.startActivity(intent);
    }

    public static String currencyRp(String number){
        double decimalNumber = Double.parseDouble(number);

        Locale localeID = new Locale("in", "ID");
        NumberFormat rupiahFormat = NumberFormat.getNumberInstance(localeID);
        return "Rp. "+rupiahFormat.format(decimalNumber);
    }

    public static void checkWindowSetFlag(Activity activity){
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);

        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public static boolean isGPSEnabled(Context mContext){
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void turnOnGPS(LocationRequest locationRequest, Activity activity) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> responseTask = LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build());

        responseTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                try {
                    task.getResult(ApiException.class);
                    Toast.makeText(activity, "GPS IS ON", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(activity, 1005);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Toast.makeText(activity, "Device do not have location", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            } else {

                Toast.makeText(activity, "GPS is off", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void previewStreetMaps(double latitude, double longitude, Context mContext){
        String geo = "google.streetview:cbll="+latitude+","+longitude;
        Uri gmmIntentUri = Uri.parse(geo);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(mapIntent);
        }
    }
}
