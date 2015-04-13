package com.example.alex.transmit_rotation_mat_through_wifi;

import android.app.Activity;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.app.Activity;

import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.Buffer;
import java.util.Random;


public class Transmission extends Activity implements SensorEventListener {
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float EPSILON = 0.1f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    private SensorManager sensorManager;
    private final float[] mRotationMatrix = new float[16];
    float[] deltaRotationMatix = new float[9];
    float[] values = new float[3];
    float[] send_data = new float[4];
    private TextView textView;
    private Button sButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmission);

        // Get network access policy permission
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Activate sensor service
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        textView = (TextView)findViewById(R.id.Status);
        sButton = (Button)findViewById(R.id.bSend);

        //sButton.setOnClickListener(new View.OnClickListener() {
        sButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            //public void onClick(View v){
            public boolean onTouch(View v, MotionEvent event) {
                // Change status display to "On"
                textView.setText("On");
                textView.setTextColor(Color.parseColor("#FF0000"));

                Socket socket;

                try {
                    // Try to reach the server
                   //Log.e("Debug: ", "Connecting...");
                    socket = new Socket("192.168.0.9", 1149);
/*
                    ByteBuffer byteBuffer = ByteBuffer.allocate(4 * deltaRotationMatix.length);
                    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
                    floatBuffer.put(deltaRotationMatix);
*/

                    ByteBuffer byteBuffer = ByteBuffer.allocate(4 * send_data.length);
                    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
                    floatBuffer.put(send_data);

                    byte[] output = byteBuffer.array();

                    //Log.e("Debug: ", "Sending.....");

                    OutputStream os = socket.getOutputStream();
                    os.write(output);
                    os.flush();

                    socket.close();

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
            }
        });
    }


    @Override
    public void onSensorChanged(SensorEvent event){
        // check if the sensor is online
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            // transform event vector into rotation matrix
            //SensorManager.getRotationMatrixFromVector(mRotationMatrix,event.values);

            getGyroscope(event);
        }
    }


    private void getGyroscope(SensorEvent event){
        values = event.values;

        if(timestamp != 0){
            final float dT = (event.timestamp - timestamp) * NS2S;

            for(int i = 0; i < 3; ++i)
                send_data[i] = values[i];
            send_data[3] = dT;
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

        timestamp = event.timestamp;

        SensorManager.getRotationMatrixFromVector(deltaRotationMatix,deltaRotationVector);

    }


    @Override
    protected void onResume(){
        super.onResume();

        sensorManager.registerListener(
                this,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor,int e){

    }
}
