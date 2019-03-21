package com.example.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.application.MyApplication;
import com.example.pojo.FishInfo;

public class RecordDetailActivity extends AppCompatActivity {

    private Activity activity;

    private int position = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);
        activity = this;


        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        FishInfo fishInfo = MyApplication.recordList.get(position);

        TextView poolText = findViewById(R.id.detail_pool_id);
        String poolId = fishInfo.getPoolId();
        poolText.setText(poolId + "号池塘");

        TextView feedText = findViewById(R.id.detail_feed_quantity);
        String feedQuantity1 = fishInfo.getFeedQuantity1();
        String feedQuantity2 = fishInfo.getFeedQuantity2();
        String feedQuantity3 = fishInfo.getFeedQuantity3();
        String feedQuantity4 = fishInfo.getFeedQuantity4();
        feedText.setText("投料量：" + feedQuantity1 + "," + feedQuantity2 + "," + feedQuantity3 + "," + feedQuantity4);

        TextView waterText = findViewById(R.id.detail_water_quantity);
        String waterQuantity = fishInfo.getWaterQuantity();
        waterText.setText("换水量：" + waterQuantity);

        TextView phText = findViewById(R.id.detail_ph);
        final String phAM = fishInfo.getPhAM();
        String phPM = fishInfo.getPhPM();
        phText.setText("PH值：" + "上午 " + phAM + "，下午 " + phPM);

        TextView nh4nText = findViewById(R.id.detail_nh4n);
        String nh4n = fishInfo.getNh4n();
        nh4nText.setText("氨氮：" + nh4n);

        TextView nano2Text = findViewById(R.id.detail_nano2);
        String nano2 = fishInfo.getNano2();
        nano2Text.setText("亚硝酸盐：" + nano2);

        TextView recordText = findViewById(R.id.detail_medication_record);
        String medicationRecord = fishInfo.getMedicationRecord();
        recordText.setText("用药记录：" + medicationRecord);


        View barView = LayoutInflater.from(this).inflate(R.layout.bar_detail, new RelativeLayout(this), false);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(barView);
        }
        ImageView returnBtn = barView.findViewById(R.id.detail_bar_return);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }
}
