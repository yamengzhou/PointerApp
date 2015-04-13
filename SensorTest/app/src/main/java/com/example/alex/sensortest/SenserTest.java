package com.example.alex.sensortest;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;


public class SenserTest extends Activity implements SensorEventListener {
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float EPSILON = 10f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    private SensorManager sensorManager;
    private boolean color = false;
    private LinearLayout background;
    private long lastUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senser_test);

        background = (LinearLayout)findViewById(R.id.background);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
    }


    public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            getGyroscope(event);
        }
    }

    private void getGyroscope(SensorEvent event){
        float[] values = event.values;
        if(timestamp != 0){
            final float dT = (event.timestamp - timestamp) * NS2S;

            // Axis of the rotation sample, not normalized yet
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate angular speed of the sample
            float omegaMagnitute = (float)Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

            if(omegaMagnitute > EPSILON){
                axisX /= omegaMagnitute;
                axisY /= omegaMagnitute;
                axisZ /= omegaMagnitute;
            }

            background.setBackgroundColor(Color.rgb((int)axisX * 256, (int)axisY * 256, (int)axisZ * 256));

            float thetaOverTwo = omegaMagnitute * dT/ 2.0f;
            float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }

        timestamp = event.timestamp;
        float[] deltaRotationMatix = new float[9];

        SensorManager.getRotationMatrixFromVector(deltaRotationMatix,deltaRotationVector);

    }

    @Override
    protected void onResume(){
        super.onResume();

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }
}
