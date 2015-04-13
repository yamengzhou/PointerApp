package com.example.alex.client_test;

import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;
import java.nio.Buffer;
import android.app.Activity;

public class Wifi_Client extends Activity {

    private static final Random rand  = new Random();
    private float randNum;
    private float[] vector = {3.14f,1.57f,1.57f};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi__client);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        System.out.println("Start");

        Log.e("sysDebug: ","Start.....");
        final TextView bText = (TextView)findViewById(R.id.bTex);

        Button gen = (Button)findViewById(R.id.bGen);
        gen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.e("sysDebug: ","Generating.....");
                randNum = rand.nextFloat();
                String number = Float.toString(randNum);
                bText.setText(number);
            }
        });

        Button button = (Button)findViewById(R.id.bSend);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Socket sock;

                try{
                    Log.e("sysDebug: ","Connecting....");
                    sock = new Socket("192.168.0.9",1149);
                    System.out.println("Connecting....");

                    /*
                    byte[] output = ByteBuffer.allocate(4).putFloat(randNum).array();

                    OutputStream os = sock.getOutputStream();
                    System.out.println("Sending....");
                    os.write(output,0,output.length);
                    os.flush();
                       */

                    ByteBuffer byteBuf = ByteBuffer.allocate(4 * vector.length);
                    FloatBuffer floatbuf = byteBuf.asFloatBuffer();
                    floatbuf.put(vector);
                    byte[] output = byteBuf.array();
                    Log.d("sysDebug: ","Sending....");

                    OutputStream os = sock.getOutputStream();
                    os.write(output);
                    os.flush();

                    sock.close();
                }catch (UnknownHostException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        });
    }



}
