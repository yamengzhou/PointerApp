package com.example.alex.move_dot_with_gyroscope;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.LinearLayout;

public class Gyro_Dot extends Activity implements SensorEventListener{
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float EPSILON = 0.1f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    private double start_time;
    private SensorManager sensorManager;
    private final float[] mRotationMatrix = new float[16];
    private float[] deltaRotationMatix = new float[9];
    private float[] values = new float[3];
    private float[] angles = new float[3];
    private float[] obj_speed = new float[3];

    private static final float G = 9.8f;

    private LinearLayout linearLayout;
    private MyView myview;

    private int start_x;
    private int start_y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyro__dot);

        linearLayout = (LinearLayout)findViewById(R.id.ll);

        myview = new MyView(this);
        linearLayout.addView(myview);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        start_time = System.currentTimeMillis() / 1000;
        start_x = 500;
        start_y = 500;

        obj_speed[0] = 5;
        obj_speed[1] = 5;
    }

    public class MyView extends View {
        public MyView(Context context){
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);

            int radius = 100;

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);

            //CalcLoc(x,y);

            //Log.e("Debug: ", "The position is " + Integer.toString(start_x)+ " " + Integer.toString(start_y));
            paint.setColor(Color.parseColor("#CD5C5C"));
            canvas.drawCircle(start_x,start_y,radius,paint);
            invalidate();
        }

        public void CalcLoc(int x, int y){

            double current_time = System.currentTimeMillis() / 1000;
            double elapse_time = current_time - start_time;
            for(int i = 0; i < angles.length; ++i) {
                angles[i] += values[i] * elapse_time;
            }

//            obj_speed[0] += G / Math.tan((double)angles[2]);
//            obj_speed[1] += G / Math.tan((double)angles[0]);
            /*
            start_x += (int)(obj_speed[0] * elapse_time);
            start_y += (int)(obj_speed[1] * elapse_time);
            */

            if(angles[2] > 0)
                start_x += (int)(obj_speed[0] * elapse_time);
            else if(angles[2] < 0)
                start_x -= (int)(obj_speed[0] * elapse_time);
            if(angles[0] > 0)
                start_y += (int)(obj_speed[1] * elapse_time);
            else if(angles[0] < 0)
                start_y -= (int)(obj_speed[1] * elapse_time);
            start_time = current_time;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){

            getGyroscope(event);
            myview.CalcLoc(start_x,start_y);
        }
    }

    private void getGyroscope(SensorEvent event){
        values = event.values;

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
            //values[0] = axisX;
            // values[1] = axisY;
            // values[2] = axisZ;
            //background.setBackgroundColor(Color.rgb((int)axisX * 256, (int)axisY * 256, (int)axisZ * 256));

            float thetaOverTwo = omegaMagnitute * dT/ 2.0f;
            float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }

        //timestamp = event.timestamp;

        SensorManager.getRotationMatrixFromVector(deltaRotationMatix,deltaRotationVector);

    }

    @Override
    protected void onResume(){
        super.onResume();

        sensorManager.registerListener(
                this,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST
        );
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int e){

    }
}
