package com.example.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.activity.EditActivity;
import com.example.activity.R;
import com.example.activity.ResActivity;
import com.example.application.MyApplication;
import com.example.pojo.FishInfo;

import java.util.List;


/**
 * 适配器
 */
public class ResAdapter extends ArrayAdapter<FishInfo> {

    /**
     * 布局文件id
     */
    private int resourceId;

    public ResAdapter(@NonNull Context context, int resource, List<FishInfo> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final FishInfo fishInfo = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        } else {
            view = convertView;
        }

        if (fishInfo != null) {
            TextView poolText = view.findViewById(R.id.res_pool_id);
            String poolId = fishInfo.getPoolId();
            poolText.setText(poolId + "号池塘");

            TextView feedText = view.findViewById(R.id.res_feed_quantity);
            String feedQuantity1 = fishInfo.getFeedQuantity1();
            String feedQuantity2 = fishInfo.getFeedQuantity2();
            String feedQuantity3 = fishInfo.getFeedQuantity3();
            String feedQuantity4 = fishInfo.getFeedQuantity4();
            feedText.setText("投料量：" + feedQuantity1 + "," + feedQuantity2 + "," + feedQuantity3 + "," + feedQuantity4);

            TextView waterText = view.findViewById(R.id.res_water_quantity);
            String waterQuantity = fishInfo.getWaterQuantity();
            waterText.setText("换水量：" + waterQuantity);

            TextView phText = view.findViewById(R.id.res_ph);
            final String phAM = fishInfo.getPhAM();
            String phPM = fishInfo.getPhPM();
            phText.setText("PH值：" + "上午 " + phAM + "，下午 " + phPM);

            TextView nh4nText = view.findViewById(R.id.res_nh4n);
            String nh4n = fishInfo.getNh4n();
            nh4nText.setText("氨氮：" + nh4n);

            TextView nano2Text = view.findViewById(R.id.res_nano2);
            String nano2 = fishInfo.getNano2();
            nano2Text.setText("亚硝酸盐：" + nano2);

            TextView recordText = view.findViewById(R.id.res_medication_record);
            String medicationRecord = fishInfo.getMedicationRecord();
            recordText.setText("用药记录：" + medicationRecord);

            Button deleteButton = view.findViewById(R.id.res_btn_delete);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           MyApplication.resList.remove(position);
                            notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.setMessage("确定要删除这条记录？");
                    builder.setTitle("删除");
                    builder.show();
                }
            });

            Button editButton = view.findViewById(R.id.res_btn_edit);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), EditActivity.class);
                    intent.putExtra("position", position);
                    getContext().startActivity(intent);
                }
            });
        }
        return view;
    }
}
