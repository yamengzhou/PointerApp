package com.example.alex.filesavingtest;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.OutputStream;
import java.util.ArrayList;

import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;


public class File_Saving extends ActionBarActivity implements SensorEventListener{
    protected EditText e_filename;
    protected Button b_save;

    private File file;
    private static String filename;
    private final static String format = ".txt";

    protected EditText e_email;
    protected Button b_send;
    private String emailAddress;

    private static String output_data = "Hello world";

    private boolean sensorStatus = false;
    private static int samplingRate = 100;
    private static int samplingTime = 1/samplingRate*1000000;

    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    protected Button b_sensor;

    private ArrayList<String> readings;
    private long timestamp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file__saving);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAccelerometer , samplingTime);

        readings = new ArrayList<String>();
        timestamp = System.currentTimeMillis();
        saveReadings();
        saveFile();
        sendFile();
    }

    private void saveReadings(){
        b_sensor = (Button)findViewById(R.id.sensor);
        b_sensor.setText("Start");
        b_sensor.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(sensorStatus == false){
                    sensorStatus = true;
                    b_sensor.setText("Stop");
                }else{
                    sensorStatus = false;
                    b_sensor.setText("Start");
                }
            }
        });
    }

    private void sendFile(){
        e_email = (EditText)findViewById(R.id.email);
        b_send = (Button)findViewById(R.id.b_send);
        b_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                e_email.setVisibility(View.INVISIBLE);
                emailAddress = e_email.getText().toString();
                e_email.setVisibility(View.VISIBLE);
                ActivateEmail();
            }

            private void ActivateEmail(){
                if(file != null){
                    Uri U = Uri.fromFile(file);
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("vnd.android.cursor.dir/email");
                    String to[] = {emailAddress};
                    i.putExtra(Intent.EXTRA_EMAIL,to);
                    // The attachment
                    i.putExtra(Intent.EXTRA_STREAM,U);
                    // The email subject
                    i.putExtra(Intent.EXTRA_SUBJECT,"Sending test");
                    startActivity(Intent.createChooser(i,"Send email"));
                }
            }
        });

    }

    private void saveFile(){
        e_filename = (EditText)findViewById(R.id.filename);
        b_save = (Button)findViewById(R.id.b_save);
        b_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                e_filename.setVisibility(View.INVISIBLE);

                /*****************************************/
                //e_filename.setText("SendingTest");
                /*****************************************/

                filename = e_filename.getText().toString();
                filename = filename + format;
                e_filename.setVisibility(View.VISIBLE);
                createExternalStorageFile();
            }

            private void createExternalStorageFile(){
                if(!isExternalStorageReadable() || !isExternalStorageWritable())
                    return;

                file = new File(getExternalFilesDir("/sdcard/formal_test/"),filename);

                try{
                    OutputStream os = new FileOutputStream(file);
                    if(readings.size() == 0)
                        return;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(baos);
                    for(String element:readings){
                        out.writeBytes(element);
                    }
                    //byte[] data = output_data.getBytes();
                    byte[] data = baos.toByteArray();
                    os.write(data);
                    os.close();
                }catch (IOException e){
                    Log.w("ExternalStorage","Error writing" + file, e);
                }
            }

            /* Checks if external storage is available for read and write */
            public boolean isExternalStorageWritable() {
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    return true;
                }
                return false;
            }

            /* Checks if external storage is available to at least read */
            public boolean isExternalStorageReadable() {
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state) ||
                        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                    return true;
                }
                return false;
            }
        });
    }

    private void getReadings(SensorEvent sensorEvent){
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        long curTime = System.currentTimeMillis();

        String reading = Float.toString(x) + "," + Float.toString(y) + "," + Float.toString(z)
                +","+Integer.toString((int)(curTime - timestamp)) + " ";
        if(sensorStatus == true){
            readings.add(reading);
        }
        timestamp = curTime;
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        Sensor sensor = event.sensor;
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            getReadings(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this,senAccelerometer,samplingTime);
    }
}
