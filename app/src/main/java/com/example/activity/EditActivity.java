package com.example.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.application.MyApplication;
import com.example.pojo.FishInfo;


public class EditActivity extends AppCompatActivity {

    private Activity activity;

    private EditText poolIdText;
    private EditText feedQuantityText1;
    private EditText feedQuantityText2;
    private EditText feedQuantityText3;
    private EditText feedQuantityText4;
    private EditText waterQuantityText;
    private EditText phAmText;
    private EditText phPmText;
    private EditText nh4nText;
    private EditText nano2Text;
    private EditText medicationRecordText;

    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        activity = this;

        poolIdText = findViewById(R.id.edit_pool_id);
        feedQuantityText1 = findViewById(R.id.edit_feed1);
        feedQuantityText2 = findViewById(R.id.edit_feed2);
        feedQuantityText3 = findViewById(R.id.edit_feed3);
        feedQuantityText4 = findViewById(R.id.edit_feed4);
        waterQuantityText = findViewById(R.id.edit_water);
        phAmText = findViewById(R.id.edit_ph_am);
        phPmText = findViewById(R.id.edit_ph_pm);
        nh4nText = findViewById(R.id.edit_nh4n);
        nano2Text = findViewById(R.id.edit_nano2);
        medicationRecordText = findViewById(R.id.edit_medication_record);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);

        FishInfo fishInfo = MyApplication.resList.get(position);
        poolIdText.setText(fishInfo.getPoolId());
        feedQuantityText1.setText(fishInfo.getFeedQuantity1());
        feedQuantityText2.setText(fishInfo.getFeedQuantity2());
        feedQuantityText3.setText(fishInfo.getFeedQuantity3());
        feedQuantityText4.setText(fishInfo.getFeedQuantity4());
        waterQuantityText.setText(fishInfo.getWaterQuantity());
        phAmText.setText(fishInfo.getPhAM());
        phPmText.setText(fishInfo.getPhPM());
        nh4nText.setText(fishInfo.getNh4n());
        nano2Text.setText(fishInfo.getNano2());
        medicationRecordText.setText(fishInfo.getMedicationRecord());

        View barView = LayoutInflater.from(this).inflate(R.layout.bar_edit, new RelativeLayout(this), false);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(barView);
        }
        ImageView returnBtn = barView.findViewById(R.id.edit_bar_return);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });

        ImageView okBtn = barView.findViewById(R.id.edit_bar_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FishInfo fishInfo = MyApplication.resList.get(position);
                fishInfo.setPoolId(poolIdText.getText().toString());
                fishInfo.setFeedQuantity1(feedQuantityText1.getText().toString());
                fishInfo.setFeedQuantity2(feedQuantityText2.getText().toString());
                fishInfo.setFeedQuantity3(feedQuantityText3.getText().toString());
                fishInfo.setFeedQuantity4(feedQuantityText4.getText().toString());
                fishInfo.setWaterQuantity(waterQuantityText.getText().toString());
                fishInfo.setPhAM(phAmText.getText().toString());
                fishInfo.setPhPM(phPmText.getText().toString());
                fishInfo.setNh4n(nh4nText.getText().toString());
                fishInfo.setNano2(nano2Text.getText().toString());
                fishInfo.setMedicationRecord(medicationRecordText.getText().toString());

                // TODO: 2019/1/20 保存编辑结果
                Intent intent = new Intent(activity, ResActivity.class);
                activity.startActivity(intent);
            }
        });
    }
}
