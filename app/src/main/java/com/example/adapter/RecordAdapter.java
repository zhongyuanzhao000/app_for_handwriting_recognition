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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.activity.R;
import com.example.activity.RecordDetailActivity;
import com.example.activity.RecordEditActivity;
import com.example.application.MyApplication;
import com.example.pojo.FishInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * 适配器
 */
public class RecordAdapter extends ArrayAdapter<FishInfo> {

    /**
     * 布局文件id
     */
    private int resourceId;

    public RecordAdapter(@NonNull Context context, int resource, List<FishInfo> objects) {
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
            TextView poolText = view.findViewById(R.id.record_pool_id);
            String poolId = fishInfo.getPoolId();
            poolText.setText(poolId + "号池塘");

            TextView timeText = view.findViewById(R.id.record_time);
            Date date = fishInfo.getDate();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:ss");
            String timeStr = simpleDateFormat.format(date);
            timeText.setText(timeStr);

            TextView recordText = view.findViewById(R.id.record_medication_record);
            String medicationRecord = fishInfo.getMedicationRecord();
            recordText.setText("用药记录：" + medicationRecord);

            ImageView deleteBtn = view.findViewById(R.id.record_btn_delete);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MyApplication.recordList.remove(position);
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

            ImageView editBtn = view.findViewById(R.id.record_btn_edit);
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), RecordEditActivity.class);
                    intent.putExtra("position", position);
                    getContext().startActivity(intent);
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), RecordDetailActivity.class);
                    intent.putExtra("position", position);
                    getContext().startActivity(intent);
                }
            });

        }
        return view;
    }
}
