package com.example.alex.filesavingtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.net.Uri;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.OutputStream;
import java.util.ArrayList;

import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.DropboxAPI.Entry;
import org.w3c.dom.Text;


public class File_Saving extends Activity {
    protected EditText e_filename;
    protected Button b_save;

    protected TextView transStatus;

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

    private static float EPSILON = 0.5f;
    private float[] angles;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float[] deltaRotationVector = new float[4];
    private float timestamp_gyro;

    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    protected Button b_sensor;

    private ArrayList<String> readings;
    private long timestamp;

    private float acc_x, acc_y, acc_z,gyro_x, gyro_y, gyro_z;

    private TextView readingCounter;

    final static private String APP_KEY = "kr4r670uwmq30uq";
    final static private String APP_SECRET = "2frqcrgs44p81y6";

    private DropboxAPI<AndroidAuthSession> mDBApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file__saving);

        // Set up network access policy permissions
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Register sensors

        sensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;
                if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                    getAccReadings(event);
                }else if(sensor.getType() == Sensor.TYPE_GYROSCOPE){
                    gyroscopeActivity(event);
                }
                writeData();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

            private void getAccReadings(SensorEvent sensorEvent){
                acc_x = sensorEvent.values[0];
                acc_y = sensorEvent.values[1];
                acc_z = sensorEvent.values[2];
            }

            private void gyroscopeActivity(SensorEvent event){
                if (timestamp_gyro != 0) {
                    final float dT = (event.timestamp - timestamp_gyro) * NS2S;
                    // Axis of the rotation sample, not normalized yet.
                    float axisX = event.values[0];
                    float axisY = event.values[1];
                    float axisZ = event.values[2];

                    gyro_x = event.values[0];
                    gyro_y = event.values[1];
                    gyro_z = event.values[2];

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

               // Log.d("Gyroscope_out",Float.toString(deltaRotationVector[0]) + "," + Float.toString(deltaRotationVector[1]) + "," + Float.toString(deltaRotationVector[2]));
                timestamp_gyro = event.timestamp;
            }

            private void writeData(){
                long curTime = System.currentTimeMillis();
                /*
                String reading = Float.toString(acc_x) + "," + Float.toString(acc_y) + "," + Float.toString(acc_z)
                        + "," + Float.toString(deltaRotationVector[0]) + ","
                        + Float.toString(deltaRotationVector[1]) + ","
                        + Float.toString(deltaRotationVector[2]) + ","
                        + Integer.toString((int)(curTime - timestamp)) + " ";
                */
                String reading = Float.toString(acc_x) + "," + Float.toString(acc_y) + "," + Float.toString(acc_z)
                        + "," + Float.toString(gyro_x) + ","
                        + Float.toString(gyro_y) + ","
                        + Float.toString(gyro_z) + ","
                        + Integer.toString((int)(curTime - timestamp)) + " ";

                if(sensorStatus == true){
                    readings.add(reading);

                    int length = readings.size();
                   // Log.d("Gyroscope_write",Float.toString(deltaRotationVector[0]) + "," + Float.toString(deltaRotationVector[1]) + "," + Float.toString(deltaRotationVector[2]));
                    readingCounter.setText(Integer.toString(length));
                }

            }


        };
        sensorManager.registerListener(sensorEventListener,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),samplingTime);
        sensorManager.registerListener(sensorEventListener,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),samplingTime);

        transStatus = (TextView)findViewById(R.id.transStatus);
        readingCounter = (TextView)findViewById(R.id.reading);

        readings = new ArrayList<String>();
        timestamp = System.currentTimeMillis();

        initDropbox();
        saveReadings();
        //saveFile();
        sendFile();
    }

    private void initDropbox(){
        AppKeyPair appKeys = new AppKeyPair(APP_KEY,APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        mDBApi.getSession().startOAuth2Authentication(File_Saving.this);
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
        //e_filename = (EditText)findViewById(R.id.filename);
        //e_email = (EditText)findViewById(R.id.email);
        b_send = (Button)findViewById(R.id.b_send);
        b_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //e_filename.setVisibility(View.INVISIBLE);

                /*****************************************/
                //e_filename.setText("SendingTest");
                /*****************************************/

                //filename = e_filename.getText().toString();

                //if(filename == null)
                //    filename = "test";
                filename = "test";
                filename = filename + format;
                //e_filename.setVisibility(View.VISIBLE);
                createExternalStorageFile();

                //ActivateEmail();
                ActivateDropbox();
            }

            private void ActivateDropbox(){
                if(file != null){
                    try {
                        FileInputStream inputStream = new FileInputStream(file);
                        Time now = new Time();
                        now.setToNow();
                        String dbname = Integer.toString(now.year) + "_"
                                + Integer.toString(now.month)
                                + "_" + Integer.toString(now.monthDay)
                                + "_" + Integer.toString(now.hour)
                                + "_" + Integer.toString(now.minute);
                        Entry response = mDBApi.putFile("/forYameng/Accelerometer_Test_App/Test_Result/" +dbname+".txt",
                                inputStream,file.length(),null,null);
                        Log.i("DbExampleLog", "The uploaded file's rev is: " + response.rev);

                        transStatus.setText("Success");
                    }catch (IOException e){
                        Log.i("IOExeption","No file existed!");
                    }catch (DropboxException e){
                        Log.i("DropboxUploading","Uploading failed");
                    }
                }
            }

/*
            private void ActivateEmail(){
                e_email.setVisibility(View.INVISIBLE);
                emailAddress = e_email.getText().toString();
                if(emailAddress == null)
                    emailAddress = "yamengzhou@email.arizona.edu";
                e_email.setVisibility(View.VISIBLE);

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
*/
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

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(mDBApi.getSession().authenticationSuccessful()){
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();
            }catch(IllegalStateException e){
                Log.i("DbAuthoLog","Error authenticating",e);
            }
        }
    }
}
