package com.example.myapplication.Adapter;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecast;

import java.util.List;

public class WeatherAdapter extends BaseAdapter {

    private List<LocalDayWeatherForecast> mWeatherForecasts;

    public void setWeatherForecasts(List<LocalDayWeatherForecast> weatherForecasts) {
        mWeatherForecasts = weatherForecasts;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mWeatherForecasts != null ? mWeatherForecasts.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mWeatherForecasts != null ? mWeatherForecasts.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        LocalDayWeatherForecast weatherForecast = mWeatherForecasts.get(position);
        String weatherInfo = (weatherForecast.getDate() + "  星期" + weatherForecast.getWeek() + ":  白天：" +
                weatherForecast.getDayWeather() + " " + weatherForecast.getDayTemp() + "°  " + "晚上：" + weatherForecast.getNightWeather() + " " + weatherForecast.getNightTemp()) + "°";
        TextView textView = convertView.findViewById(android.R.id.text1);
// 创建一个 SpannableString 对象，用于在 TextView 中设置样式
        SpannableString spannableString = new SpannableString(weatherInfo);
// 设置要改变颜色的文字的起始位置和结束位置
        int start = weatherInfo.indexOf("白天");
        int end = start + "白天".length();
        int start1 = weatherInfo.indexOf("晚上");
        int end2 = start + "晚上".length();
// 创建一个 ForegroundColorSpan 对象，设置文字颜色
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
// 将颜色样式应用到 SpannableString 对象中的指定范围
        spannableString.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// 将 SpannableString 设置到 TextView 中
        textView.setText(spannableString.toString());
        return convertView;
    }
}


