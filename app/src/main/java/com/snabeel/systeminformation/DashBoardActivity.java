package com.snabeel.systeminformation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.snabeel.systeminformation.databinding.ActivityDashBoardBinding;

import java.util.ArrayList;
import java.util.List;

public class DashBoardActivity extends AppCompatActivity {

    ActivityDashBoardBinding binding;
    List<SystemData> list = new ArrayList<>();
    SystemInfoAdapter adapter;
    long divisor = 0, days = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        try{
            String isFromAvgTime = getIntent().getStringExtra("FromWhichBtn");
            if(isFromAvgTime.equals("AvgTime")){
                binding.progressBar.setVisibility(View.VISIBLE);

                String[] items = new String[]{"Day", "Week", "Month"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
                binding.spinner1.setAdapter(adapter);
                binding.spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0:
                                divisor = 1000*60;
                                days = 1000 * 24 * 60 * 60L;
                                selected = "Day";
                                break;
                            case 1:
                                divisor = 1000* 60 * 7;
                                days = 1000 * 24 * 7 * 60 * 60L;
                                selected = "Week";
                                break;
                            case 2:
                                divisor = 1000 * 60 * 30;
                                days = 1000 * 24 * 30 * 60 * 60L;
                                selected = "Month";
                                break;
                        }
                        belowThirty = 0;
                        averageTime = 0;
                        calculateAvgTime();
                        calculateBelowThirtyCount();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                binding.spinner1.setSelection(0);
                binding.systemInfoRV.setVisibility(View.GONE);
            }else if(isFromAvgTime.equals("dashboard")){
                binding.avgChargeTime.setVisibility(View.GONE);
                binding.spinner1.setVisibility(View.GONE);
                adapter = new SystemInfoAdapter(list);
                binding.systemInfoRV.setAdapter(adapter);
                binding.systemInfoRV.setLayoutManager(new LinearLayoutManager(this));
                binding.progressBar.setVisibility(View.VISIBLE);
                getAllData();

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    int belowThirty = 0;
    String selected = "";
    private void calculateBelowThirtyCount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference ref = db.collection("CountBelowThirty");
        ref.orderBy("date")
                .whereLessThan("date",System.currentTimeMillis())
                .whereGreaterThan("date", System.currentTimeMillis() - days)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        belowThirty = task.getResult().size();
                        binding.avgCountBelowThirty.setText("Battery goes below thirty for "+belowThirty+" times in a "+selected);
                        Toast.makeText(DashBoardActivity.this, ""+task.getResult().size(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    long averageTime = 0;
    int totalCount =0;
    private void calculateAvgTime() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference ref = db.collection("ChargeTimeAvg");

        ref.orderBy("startTime")
                .whereLessThan("startTime",System.currentTimeMillis())
                .whereGreaterThan("startTime", System.currentTimeMillis() - days)
                .get()
                .addOnCompleteListener(task -> {
                    runOnUiThread(()->{
                        binding.progressBar.setVisibility(View.GONE);
                    });
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ChargeTime chargeTime = document.toObject(ChargeTime.class);
                            long start = chargeTime.getStartTime();
                            long end = chargeTime.getEndTime();
                            long totalTime = end - start;
                            averageTime += totalTime;
                            totalCount++;
                            runOnUiThread(() -> {
                                String time = "";
                                if(averageTime < 59*1000){
                                    time = (double) averageTime / 1000 + " Seconds";
                                }else if(averageTime < 60*1000*60){
                                    time = String.valueOf((double)averageTime/(60*1000)).substring(0,5)+" Minutes";
                                }else{
                                    time = String.valueOf((double)averageTime/(60*1000*60)).substring(0,6)+" Hours";
                                }
                                binding.avgChargeTime.setText("average charge time : "+ time+" in a "+selected);
                            });
                        }
                        Log.d("AvgTimeCharge", "task is "+task.isSuccessful());
                    }

                });
    }

    private void getAllData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference ref = db.collection("SystemInformation");
        ref.get().addOnCompleteListener(task -> {
            binding.progressBar.setVisibility(View.GONE);
            if(task.isSuccessful()){
                for (QueryDocumentSnapshot document : task.getResult()) {
                    adapter.updateAdapter(document.toObject(SystemData.class));
                }
            }
        });
    }
}