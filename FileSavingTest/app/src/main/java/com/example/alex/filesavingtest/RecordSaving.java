package com.example.alex.filesavingtest;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
/**
 * Created by Alex on 4/30/2015.
 */
public class RecordSaving extends Activity{

    private EditText e_filename;
    private Button b_save;
    private static String filename;
    private final static String format = ".txt";
    private String output_data;


    public RecordSaving(){

    }

    public RecordSaving(EditText e_filename, Button b_save, String output_data){
        this.e_filename = e_filename;
        this.b_save = b_save;
        this.output_data = output_data;
    }

    public void saveFile(){
        e_filename = (EditText)findViewById(R.id.filename);
        b_save = (Button)findViewById(R.id.b_save);
        b_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                e_filename.setVisibility(View.INVISIBLE);
                filename = e_filename.getText().toString();
                filename = filename + format;
                e_filename.setVisibility(View.VISIBLE);
                createExternalStorageFile();
            }

            private void createExternalStorageFile(){
                if(!isExternalStorageReadable() || !isExternalStorageWritable())
                    return;

                File file = new File(("/sdcard/saving_test/"),filename);
                try{
                    OutputStream os = new FileOutputStream(file);
                    byte[] data = output_data.getBytes();
                    os.write(data);
                    os.close();
                }catch (IOException e){
                    Log.w("ExternalStorage", "Error writing" + file, e);
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
}
