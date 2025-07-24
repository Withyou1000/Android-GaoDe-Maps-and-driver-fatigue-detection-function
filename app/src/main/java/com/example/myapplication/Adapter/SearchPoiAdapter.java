package com.example.myapplication.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.help.Tip;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class SearchPoiAdapter extends RecyclerView.Adapter<SearchPoiAdapter.MyHolder> {
    List<Tip> datas;
    Context context;
    private OnItemClickListener onItemClickListener;

    public SearchPoiAdapter(Context context, List<Tip> datas) {
        this.datas = datas;
        this.context = context;
    }


    public void SetData(List<Tip> tipList) {
        if (tipList == null) {
            return;
        }
        this.datas.clear();
        this.datas.addAll(tipList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.simple_dropdown_item_1line,
                parent, false);
        return new MyHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Tip tip = datas.get(position);
        holder.name.setText(tip.getName());
        holder.address.setText(tip.getAddress());
        holder.position = position;
        holder.tip = tip;
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Tip tip);
    }

    public void setItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView address;
        int position;
        Tip tip;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text1);
            address=itemView.findViewById(R.id.text2);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(position, tip);
                    }
                }
            });
        }
    }
}
