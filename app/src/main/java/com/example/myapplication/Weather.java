package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.example.myapplication.Adapter.WeatherAdapter;

import java.util.List;

public class Weather extends AppCompatActivity implements WeatherSearch.OnWeatherSearchListener {

    private TextView city;
    private TextView desc;
    private TextView temp;
    private TextView wind;
    private TextView humidity;
    private String city_name;
    private TextView time;
    WeatherAdapter mAdapter = null;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weather);
        city = findViewById(R.id.tv_weather_city);
        desc = findViewById(R.id.tv_weather_desc);
        temp = findViewById(R.id.tv_temperature);
        wind = findViewById(R.id.tv_wind_speed);
        humidity = findViewById(R.id.tv_humidity);
        time = findViewById(R.id.tv_publish_time);
        mAdapter = new WeatherAdapter();
        listView = findViewById(R.id.lv_weather_forecast);
        listView.setAdapter(mAdapter);

        city_name = getIntent().getStringExtra("city");
        city.setText(city_name);

        // 创建 WeatherSearch 对象
        WeatherSearch mWeatherSearch = null;
        try {
            mWeatherSearch = new WeatherSearch(this);
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }
        WeatherSearch mWeatherForestSearch = null;
        try {
            mWeatherForestSearch = new WeatherSearch(this);
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }
        //设置两个监听
        mWeatherSearch.setOnWeatherSearchListener(this);
        mWeatherForestSearch.setOnWeatherSearchListener(this);
        // 创建实时天气查询对象
        WeatherSearchQuery liveQuery = new WeatherSearchQuery(city_name, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        mWeatherSearch.setQuery(liveQuery);
        mWeatherSearch.searchWeatherAsyn();
        // 创建预报天气查询对象
        WeatherSearchQuery forecastQuery = new WeatherSearchQuery(city_name, WeatherSearchQuery.WEATHER_TYPE_FORECAST);
        mWeatherForestSearch.setQuery(forecastQuery);
        mWeatherForestSearch.searchWeatherAsyn();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult, int i) {
        if (i == 1000) {
            if (weatherLiveResult != null && weatherLiveResult.getLiveResult() != null) {
                LocalWeatherLive weatherlive = weatherLiveResult.getLiveResult();
                time.setText(weatherlive.getReportTime() + "发布");
                desc.setText(weatherlive.getWeather());
                temp.setText(weatherlive.getTemperature() + "°");
                wind.setText(weatherlive.getWindDirection() + "风     " + weatherlive.getWindPower() + "级");
                humidity.setText("湿度         " + weatherlive.getHumidity() + "%");
            }
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult forecastResult, int i) {
        // 处理预报天气搜索结果
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            if (forecastResult != null && forecastResult.getForecastResult() != null &&
                    forecastResult.getForecastResult().getWeatherForecast() != null &&
                    forecastResult.getForecastResult().getWeatherForecast().size() > 0) {
                List<LocalDayWeatherForecast> weatherForecasts = forecastResult.getForecastResult().getWeatherForecast();
                mAdapter.setWeatherForecasts(weatherForecasts);
            } else {
                // 预报天气结果为空
                Log.e("WeatherActivity", "Weather forecast result is empty");
            }
        }
    }
}