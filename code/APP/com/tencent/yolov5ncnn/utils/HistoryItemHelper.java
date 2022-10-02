package com.tencent.yolov5ncnn.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tencent.yolov5ncnn.R;

import java.util.List;

public class HistoryItemHelper extends ArrayAdapter<HistoryItem> {
    private int resId;

    public HistoryItemHelper(@NonNull Context context, int resource, @NonNull List<HistoryItem> objects) {
        super(context, resource, objects);
        resId = resource;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        HistoryItem item = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resId, parent, false);
        TextView ID = (TextView) view.findViewById(R.id.ID);
        TextView category = (TextView) view.findViewById(R.id.category);
        TextView time = (TextView) view.findViewById(R.id.time);
        TextView prob = (TextView) view.findViewById(R.id.prob);
        ID.setText(Integer.toString(item.getID()));
        category.setText(item.getCategory());
        time.setText(item.getTime());
        float probability = item.getProb();
        if(probability > 80) { prob.setTextColor(ContextCompat.getColor(getContext(), R.color.green)); }
        else if(probability > 50) { prob.setTextColor(ContextCompat.getColor(getContext(), R.color.orange)); }
        else{ prob.setTextColor(ContextCompat.getColor(getContext(), R.color.red)); }
        prob.setText(String.format("%.1f", probability)+ "%");
        return view;
    }
}
