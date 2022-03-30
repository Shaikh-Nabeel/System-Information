package com.snabeel.systeminformation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;

public class ChargerConnectedReceiver extends Service {

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "AppNameBackgroundService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BAtteryTest", "starting service");
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Log.d("BAtteryTest", "broad cast set up");

        startForeground();
        Log.d("BAtteryTest", "running foreground");
        return super.onStartCommand(intent, flags, startId);
    }

    private void startForeground() {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);

        if( Build.VERSION.SDK_INT > Build.VERSION_CODES.R) return;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Background charging service is running")
                .setContentIntent(pendingIntent)
                .build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NOTIF_CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

//    CallBackForReceivers callBackForReceivers;
//    public ChargerConnectedReceiver(CallBackForReceivers callBackForReceivers){
//        this.callBackForReceivers = callBackForReceivers;
//    }
    long start = 0;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            if(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) < 31){
                uploadBatteryPercentage(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
            }
            boolean isCharging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0;

            if(isCharging){
                start = System.currentTimeMillis();

            }else{
                long end = System.currentTimeMillis();
                if(start != 0)
                    uploadOnFirebase(start,end);
            }
            Toast.makeText(ChargerConnectedReceiver.this,"start "+start, Toast.LENGTH_SHORT).show();
            Log.d("BAtteryTest", "jals;kdjfa;;;;;;;;;a;lsdjflksadjdfl a;lskjdflkjasdlkfjlksd ;alsdjflk");
        }
    };

    private void uploadBatteryPercentage(int intProperty) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference countBelowThirty = db.collection("CountBelowThirty");
        BelowThirtyCount belowThirtyCount = new BelowThirtyCount(System.currentTimeMillis(),String.valueOf(intProperty));
        countBelowThirty.document().set(belowThirtyCount);
    }

    private void uploadOnFirebase(long start, long end) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference chargeTime = db.collection("ChargeTimeAvg");
        ChargeTime obj = new ChargeTime(start,end);
        chargeTime.document().set(obj);
        this.stopForeground(true);
    }

}
