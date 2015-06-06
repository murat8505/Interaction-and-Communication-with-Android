package com.example.joeroger.samplesensors.utils;


import android.hardware.Sensor;

import java.util.InputMismatchException;

public final class SensorUtils {

    public static String getSensorType(Sensor sensor) {
        switch (sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "Ambient Temperature";
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                return "Game Rotation Vector";
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                return "Geomagnetic Rotation Vector";
            case Sensor.TYPE_GRAVITY:
                return "Gravity";
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                return "Gyroscope Uncalibrated";
            case Sensor.TYPE_HEART_RATE:
                return "Heart Rate";
            case Sensor.TYPE_LIGHT:
                return "Light";
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "Linear Acceleration";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "Magnetic Field";
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                return "Magnetic Field Uncalibrated";
            case Sensor.TYPE_ORIENTATION:
                return "Orientation";
            case Sensor.TYPE_PRESSURE:
                return "Pressure";
            case Sensor.TYPE_PROXIMITY:
                return "Proximity";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Humidity";
            case Sensor.TYPE_ROTATION_VECTOR:
                return "Rotation Vector";
            case Sensor.TYPE_SIGNIFICANT_MOTION:
                return "Significant Motion";
            case Sensor.TYPE_STEP_COUNTER:
                return "Step Counter";
            case Sensor.TYPE_STEP_DETECTOR:
                return "Step Detector";
            case Sensor.TYPE_TEMPERATURE:
                return "Temperature";
            default:
                return "Unknown " + Integer.toString(sensor.getType());
        }
    }

    public static String getLabel(int sensorType, int position) {
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_LINEAR_ACCELERATION:
                switch (position) {
                    case 0:
                        return "m/s^2 x-axis:";
                    case 1:
                        return "m/s^2 y-axis:";
                    case 2:
                        return "m/s^2 z-axis:";
                }
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_TEMPERATURE:
                return "Degrees Celsius:";
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
            case Sensor.TYPE_ROTATION_VECTOR:
                switch (position) {
                    case 0:
                        return "x*sin(θ/2):";
                    case 1:
                        return "y*sin(θ/2):";
                    case 2:
                        return "z*sin(θ/2):";
                    case 3:
                        return "cos(θ/2):";
                    case 4:
                        return "heading accuracy (radians):";
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                switch (position) {
                    case 0:
                        return "rad/s x-axis:";
                    case 1:
                        return "rad/s y-axis:";
                    case 2:
                        return "rad/s z-axis:";
                    case 3:
                        return "drift rad/s x-axis:";
                    case 4:
                        return "drift rad/s y-axis:";
                    case 5:
                        return "drift rad/s z-axis:";
                }
                break;
            case Sensor.TYPE_HEART_RATE:
                return "Beats Per Second:";
            case Sensor.TYPE_LIGHT:
                return "SI lux:";
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                switch (position) {
                    case 0:
                        return "uT x-axis:";
                    case 1:
                        return "uT y-axis:";
                    case 2:
                        return "uT z-axis:";
                    case 3:
                        return "uT iron-bias x-axis:";
                    case 4:
                        return "uT iron-bias y-axis:";
                    case 5:
                        return "uT iron-bias z-axis:";
                }
                break;
            case Sensor.TYPE_ORIENTATION:
                switch (position) {
                    case 0:
                        return "Azimuth:";
                    case 1:
                        return "Pitch:";
                    case 2:
                        return "Roll:";
                }
                break;
            case Sensor.TYPE_PRESSURE:
                return "hPa:";
            case Sensor.TYPE_PROXIMITY:
                return "cm:";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Humidity %:";
            case Sensor.TYPE_STEP_COUNTER:
                return "Steps:";
            case Sensor.TYPE_STEP_DETECTOR:
                return "Steps:";
        }

        return "Unknown value " + Integer.toString(position) + ":";
    }

    public static String getAccuracyString(int accuracy) {
        switch (accuracy) {
            case 1:
                return "Low";
            case 2:
                return "Medium";
            case 3:
                return "High";
            case -1:
                return "No Contact";
            default:
                return "Unreliable";
        }
    }

    public static String getReportingMode(int mode) {
        switch (mode) {
            case Sensor.REPORTING_MODE_CONTINUOUS:
                return "Continuous";
            case Sensor.REPORTING_MODE_ON_CHANGE:
                return "On Change";
            case Sensor.REPORTING_MODE_ONE_SHOT:
                return "One Shot";
            case Sensor.REPORTING_MODE_SPECIAL_TRIGGER:
                return "Special Trigger";
            default:
                return "Unknown";
        }
    }
}
