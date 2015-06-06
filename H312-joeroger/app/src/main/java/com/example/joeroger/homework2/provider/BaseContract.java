package com.example.joeroger.homework2.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.joeroger.homework2.BuildConfig;

public abstract class BaseContract {
    /* package */ static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.WeatherProvider";

    /* package */ static final Uri BASE_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .build();

    public interface BaseWeatherColumns extends BaseColumns {
        public String CITY_ID = "city_id";
        public String CITY_NAME = "city_name";
        public String ICON = "icon";
        public String CONDITION = "condition";
        public String HUMIDITY = "humidity";
        public String PRESSURE = "pressure";
        public String WIND_SPEED = "wind_speed";
        public String WIND_DIRECTION = "wind_direction";
        public String DATA_TIME = "data_time";
        public String UPDATED = "updated";
    }
}
