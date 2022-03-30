package com.snabeel.systeminformation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkChangeListener extends BroadcastReceiver {

    CallBackForReceivers callBackForReceivers;
    public NetworkChangeListener(CallBackForReceivers callBackForReceivers){
        this.callBackForReceivers = callBackForReceivers;
    }

    public NetworkChangeListener(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(isOnline(context)){
//            Toast.makeText(context,"Online", Toast.LENGTH_SHORT).show();
            callBackForReceivers.onStateChange("On");
        }else{
            callBackForReceivers.onStateChange("Off");
//            Toast.makeText(context,"Offline", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }
}
