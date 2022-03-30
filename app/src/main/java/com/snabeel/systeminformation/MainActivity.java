package com.snabeel.systeminformation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snabeel.systeminformation.databinding.ActivityMainBinding;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements CallBackForReceivers{

    String mInternetConnection;
    String mIsCharging;
    String mlastChargingStatus;
    String mLocation;
    String mMemoryEmpty;
    String mCharged;

    ActivityMainBinding binding;
    NetworkChangeListener networkChangeListener;
    public static final String LAST_BATTERY_CHANGE = "BATTERY_CHANGE_TIME";

    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private static final long MIN_TIME = 100L;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            binding.batteryPercentage.setText("Battery Percentage : "+bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)+"%");
            mCharged = "Battery Percentage : "+bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)+"%";
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            mlastChargingStatus = "Change in battery status: "+timestamp.toString();
            binding.lastChangeInChargingStatus.setText("Change in battery status: "+timestamp.toString());
            putValueInPref(System.currentTimeMillis(),getSharedPref());
//            setBatteryStatus();
            boolean isCharging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0;
            if(isCharging){
                binding.batteryCharging.setText("Charging : Yes");
                mIsCharging = "Charging : Yes";
            }else{
                binding.batteryCharging.setText("Charging : No");
                mIsCharging = "Charging : No";
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding.progressBar1.setVisibility(View.GONE);
        networkChangeListener = new NetworkChangeListener(this);
        registerReceiver(networkChangeListener,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        startService(new Intent(this, ChargerConnectedReceiver.class));

        getMemoryAvailable();
        fn_getLocation();

        binding.syncBtn.setOnClickListener(v-> uploadData());
        binding.dashboardBtn.setOnClickListener(v->{
            startActivity(new Intent(this, DashBoardActivity.class).putExtra("FromWhichBtn", "dashboard"));
        });

        binding.averageChargingTime.setOnClickListener(v->{
            startActivity(new Intent(this, DashBoardActivity.class).putExtra("FromWhichBtn", "AvgTime"));
        });

        new Handler().postDelayed(this::hideInformations, 10*1000);
        final int[] timeLeft = {10};
        Thread t = new Thread(){
            @Override
            public void run() {
                super.run();
                while (timeLeft[0] != 0) {
                    try {
                        sleep(1000);
                        timeLeft[0]--;
                        runOnUiThread(() -> binding.timer.setText("Data will be hidden in " + timeLeft[0] + " seconds"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    private void hideInformations() {
        binding.scrollView.setVisibility(View.GONE);
    }

    private void uploadData() {
        binding.progressBar1.setVisibility(View.VISIBLE);
        SystemData systemData = new SystemData(mInternetConnection,mIsCharging,mlastChargingStatus,mCharged,mLocation,mMemoryEmpty,String.valueOf(System.currentTimeMillis()));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference chargeTime = db.collection("SystemInformation");
        chargeTime.document().set(systemData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                binding.progressBar1.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Data Synced Successfully.",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "Unable to Sync.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getMemoryAvailable() {

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        double megAvailable =(double) bytesAvailable / (1024 * 1024 * 1024);
        double totalMemory = (double) (stat.getBlockCountLong()* stat.getBlockSizeLong()) / (1024 * 1024 * 1024);
//        Log.e("","Available MB : "+megAvailable);

        binding.memoryUtilization.setText("Available Memory : "+String.valueOf(megAvailable).substring(0,6)+"GB\nTotal Memory : "+String.valueOf(totalMemory).substring(0,6)+"GB");
        mMemoryEmpty = "Available Memory : "+String.valueOf(megAvailable).substring(0,6);
    }

    public SharedPreferences getSharedPref(){
        return getPreferences(Context.MODE_PRIVATE);
    }

    public void putValueInPref(long time,SharedPreferences preferences){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(LAST_BATTERY_CHANGE, time);
        editor.apply();
    }

    public long getValueInPref(SharedPreferences preferences){
        return preferences.getLong(LAST_BATTERY_CHANGE, -1L);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onStateChange(String value) {
        switch (value){
            case "Yes":
            case "No":
                binding.batteryCharging.setText("Charging : "+value);
                break;
            case "On":
            case "Off":
                binding.internetConnection.setText("Internet Connection : "+value);
                mInternetConnection = value;
                break;
        }
    }


    private void fn_getLocation() {
        try {
            boolean isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.d("location_trace", "gps: " + isGPSEnable + " network: " + isNetworkEnable);
            Location location;
            if (!isGPSEnable && !isNetworkEnable) {
                alertDialogToEnableGPS();
            } else {
                if (isNetworkEnable) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                locationPermission();
                                return;
                            }
                        }
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, 0F, locationListener);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                Log.e("location_trace", location.getLatitude() + " N");
                                Log.e("location_trace", location.getLongitude() + " N");
                                updateUI(location);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (isGPSEnable) {
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, 0, locationListener);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                Log.e("location_trace", location.getLatitude() + " G");
                                Log.e("location_trace", location.getLongitude() + " G");
                                updateUI(location);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI(Location location) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Address address = addresses.get(0);
            binding.location.setText("Location : "+address.getLocality() +" "+ address.getSubLocality()+" "+address.getSubAdminArea()+" "+address.getAdminArea());
            mLocation = "Location : "+address.getLocality() +" "+ address.getSubLocality()+" "+address.getSubAdminArea()+" "+address.getAdminArea();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alertDialogToEnableGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS is disabled, do want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 101))
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                fn_getLocation();
            } else {
                Toast.makeText(this,"Cannot fetch accurate location without gps",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void locationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                146);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == 146) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                fn_getLocation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            updateUI(location);
        }
    }
}