package com.example.joeroger.homework1.network;

/**
 * Created by jrogers on 4/17/15.
 */
public final class JsonConstants {

    // hPa to inches of mercury
    public static final double PRESSURE_CONVERSION = 33.8638866667d;
    // meters per sec to miles per hour
    public static final double WIND_SPEED_CONVERSION = 2.2369362920544d;

    public interface Conditions {
        public String CITY_ID = City.CITY_ID;
        public String CITY_NAME = City.CITY_NAME;
        public String DATA_TIME = "dt";
        public String MAIN = "main";
        public String WEATHER = "weather";
        public String WIND = "wind";
    }

    public interface Forecast {
        public String CITY = "city";
        public String COUNT = "cnt";
        public String LIST = "list";
    }

    public interface Main {
        public String HUMIDITY = "humidity";
        public String TEMPERATURE = "temp";
        public String PRESSURE = "pressure";
    }
    public interface Weather {
        public String CONDITION = "main";
        public String ICON = "icon";
    }

    public interface Wind {
        public String WIND_SPEED = "speed";
        public String WIND_DIRECTION = "deg";
    }

    public interface City {
        public String CITY_ID = "id";
        public String CITY_NAME= "name";
    }

    public interface DailyForecast {
        public String DATA_TIME = Conditions.DATA_TIME;
        public String TEMP = "temp";
        public String HUMIDITY = Main.HUMIDITY;
        public String PRESSURE = Main.PRESSURE;
        public String WEATHER = Conditions.WEATHER;
        public String WIND_SPEED = Wind.WIND_SPEED;
        public String WIND_DIRECTION = Wind.WIND_DIRECTION;
    }

    public interface Temp {
        public String MAX_TEMP = "max";
        public String MIN_TEMP = "min";
    }
}
