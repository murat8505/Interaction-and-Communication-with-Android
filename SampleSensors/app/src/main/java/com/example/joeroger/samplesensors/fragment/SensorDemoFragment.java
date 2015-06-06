package com.example.joeroger.samplesensors.fragment;


import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.joeroger.samplesensors.R;
import com.example.joeroger.samplesensors.utils.SensorUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class SensorDemoFragment extends Fragment
        implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;
    private int sensorType;
    private MyTriggerEventListener triggerEventListener;
    private boolean viewsConfigured = false;

    public SensorDemoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sensor_demo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sensorType != Sensor.TYPE_SIGNIFICANT_MOTION) {
            // An additional parameter can be passed which "batches" sensor data, starting in API 19.
            // This is useful, if you do not need to "process" data immediately. A good example is
            // step counter. Essentially could receive updates every 60s vs as soon as its available.
            // For this demo, since we are showing all sorts of sensors in real time, this is not configured.
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            triggerEventListener = new MyTriggerEventListener();
            sensorManager.requestTriggerSensor(triggerEventListener, sensor);
        }
    }

    @Override
    public void onPause() {
        if (triggerEventListener == null) {
            sensorManager.unregisterListener(this);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        sensorManager = null;
        sensor = null;
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        ViewHolder holder = getViewHolder();
        if (holder != null) {
            holder.accuracy.setText(SensorUtils.getAccuracyString(accuracy));
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        processEvent(event.timestamp, event.values);
    }

    /* package */ void processEvent(long timestamp, float[] values) {
        ViewHolder holder = getViewHolder();
        if (holder != null) {
            holder.nanoseconds.setText(Long.toString(timestamp));

            int len = values.length <= 6 ? values.length : 6;

            for (int i = 0; i < len; ++i) {
                holder.values[i].setText(Float.toString(values[i]));
            }

            // Only do the following section once, as it will not change no matter
            // how many events arrive.
            if (!viewsConfigured) {
                viewsConfigured = true;

                for (int i = 0; i < len; ++i) {
                    holder.labels[i].setText(SensorUtils.getLabel(sensorType, i));
                }

                for (int i = len; i < 6; ++i) {
                    holder.labels[i].setVisibility(View.GONE);
                    holder.values[i].setVisibility(View.GONE);
                }
            }
        }
    }

    public void setSensorType(int sensorType) {
        this.sensorType = sensorType;
        sensor = sensorManager.getDefaultSensor(sensorType);

        ViewHolder holder = getViewHolder();
        if (holder != null) {
            holder.sensorType.setText(SensorUtils.getSensorType(sensor));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.reportingMode.setText(SensorUtils.getReportingMode(sensor.getReportingMode()));
            }
        }
    }

    @Nullable
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    static class ViewHolder {

        final TextView sensorType;
        final TextView accuracy;
        final TextView reportingMode;
        final TextView nanoseconds;
        final TextView[] labels = new TextView[6];
        final TextView[] values = new TextView[6];

        public ViewHolder(View view) {
            sensorType = (TextView) view.findViewById(R.id.sensor_type);
            accuracy = (TextView) view.findViewById(R.id.accuracy);
            reportingMode = (TextView) view.findViewById(R.id.reporting_mode);
            nanoseconds = (TextView) view.findViewById(R.id.nanoseconds);
            labels[0] = (TextView) view.findViewById(R.id.value_label_0);
            values[0] = (TextView) view.findViewById(R.id.value_0);
            labels[1] = (TextView) view.findViewById(R.id.value_label_1);
            values[1] = (TextView) view.findViewById(R.id.value_1);
            labels[2] = (TextView) view.findViewById(R.id.value_label_2);
            values[2] = (TextView) view.findViewById(R.id.value_2);
            labels[3] = (TextView) view.findViewById(R.id.value_label_3);
            values[3] = (TextView) view.findViewById(R.id.value_3);
            labels[4] = (TextView) view.findViewById(R.id.value_label_4);
            values[4] = (TextView) view.findViewById(R.id.value_4);
            labels[5] = (TextView) view.findViewById(R.id.value_label_5);
            values[5] = (TextView) view.findViewById(R.id.value_5);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    class MyTriggerEventListener extends TriggerEventListener {
        @Override
        public void onTrigger(TriggerEvent event) {
            processEvent(event.timestamp, event.values);
        }
    }
}
