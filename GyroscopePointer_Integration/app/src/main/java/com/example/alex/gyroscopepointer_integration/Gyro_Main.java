package com.example.alex.gyroscopepointer_integration;

import android.app.Activity;
import android.hardware.SensorEvent;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SensorManager;
import android.hardware.Sensor;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;


public class Gyro_Main extends Activity implements SensorEventListener {
    private static int samplingRate = 100;
    private static int samplingTime = 1/samplingRate*1000000; //1/rate*1000000
    private static float EPSILON = 0.5f;

    private SensorManager sensorManager;

    private TextView title;
    private Button push_button;

    private float[] angles;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyro__main);

        // Initialize widgets functions
        title = (TextView)findViewById(R.id.title);
        push_button = (Button)findViewById(R.id.push);

        // Set up network access policy permissions
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Activate sensor service
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Initialize parameters
        angles = new float[4];

        // Initialize screen touch listener
        push_button.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                Socket socket;

                try{
                    socket = new Socket("192.168.0.9",1149);
                    // Translate data from float to byte
                    ByteBuffer byteBuffer = ByteBuffer.allocate(4 * angles.length);
                    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
                    floatBuffer.put(angles);
                    byte[] output = byteBuffer.array();

                    // Send out data
                    OutputStream os = socket.getOutputStream();
                    os.write(output);
                    os.flush();
                    socket.close();

                }catch (UnknownHostException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }

                return  true;
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        // check if the sensor is online
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            gyroscopeActivity(event);
        }
    }

    private void gyroscopeActivity(SensorEvent event){
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float)Math.sqrt((double)axisX*axisX + axisY*axisY + axisZ*axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT;
            float sinThetaOverTwo =(float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo =(float) Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        //Log.d("Debug: ","delta rotation is:  " + Float.toString(deltaRotationVector[0])
         //       + " " + Float.toString(deltaRotationVector[1])
        //        + " " + Float.toString(deltaRotationVector[2]));
        for(int i = 0; i < 3; ++i)
        angles[i] += deltaRotationVector[i];
        //Log.d("Debug: ","current angle is: " + Float.toString(angles[0])
          //      + " " + Float.toString(angles[1])
        //        + " " + Float.toString(angles[2]));
        timestamp = event.timestamp;
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                samplingTime // sampling rate about 0.05 sec
        );
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int e){

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();

    }
}
