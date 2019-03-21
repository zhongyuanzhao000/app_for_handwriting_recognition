package com.example.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.adapter.ResAdapter;
import com.example.application.MyApplication;
import com.example.pojo.FishInfo;
import com.example.util.SharedPreferencesUtil;
import com.google.gson.Gson;

import java.util.List;


public class ResActivity extends AppCompatActivity {
    private static ResAdapter resAdapter;

    private ListView listView;
    private Activity activity;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_res);
        activity = this;
        context = this;

        final List<FishInfo> resList = MyApplication.resList;

        SharedPreferencesUtil.saveStringValue(context, "data", "");

        View barView = LayoutInflater.from(this).inflate(R.layout.bar_res, new RelativeLayout(this), false);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(barView);
        }
        ImageView returnBtn = barView.findViewById(R.id.res_bar_return);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });

        ImageView okBtn = barView.findViewById(R.id.res_bar_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2019/1/20 保存识别结果
                Gson gson = new Gson();
                final String jsonRes = gson.toJson(resList);
                SharedPreferencesUtil.saveStringValue(context, "data", jsonRes);

                Intent intent = new Intent(context, RecordActivity.class);
                activity.startActivity(intent);
            }
        });

        resAdapter = new ResAdapter(context, R.layout.item_res, resList);
        listView = findViewById(R.id.list_res);
        listView.setAdapter(resAdapter);
        getData();
    }

    public void getData() {
        // TODO: 2019/1/19 获取数据
    }

}
