package com.example.myapplication.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.Class.Bean;
import com.example.myapplication.R;

import java.util.List;

public class MovePoiAdapter extends BaseAdapter {
    private final List<Bean> poiItemList;
    private final Context context;
    private int selectPosition=0;
    public MovePoiAdapter(List<Bean> poiItemList, Context context) {
        this.poiItemList = poiItemList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return poiItemList == null ? 0:poiItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return poiItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
            holder = new ViewHolder();
            holder.text1 = convertView.findViewById(android.R.id.text1);
            holder.text2 = convertView.findViewById(android.R.id.text2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Bean bean=poiItemList.get(position);
        holder.text1.setText(bean.getName());
        holder.text2.setText(bean.getAddress());
        if (selectPosition == position){
            holder.text1.setTextColor(Color.parseColor("#008000"));
            holder.text2.setTextColor(Color.parseColor("#008000"));
        }
        else {
            holder.text1.setTextColor(context.getColor(R.color.black));
            holder.text2.setTextColor(context.getColor(R.color.black));
        }
        return convertView;
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }

    static class ViewHolder {
        TextView text1,text2;
    }
}
