package com.uofa.yameng.laserpointertracking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

public class LaserPointerTracking extends Activity {

    private Context context;
    private Button b_gallery;
    private static int RESULT_LOAD_IMAGE = 1;
    private VideoView videoView;
    private ImageView imageView;
    private VideoProcess videoProcess;

    private Uri currImageURI;
    private String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser_pointer_tracking);

        context = getApplicationContext();
       // videoView = (VideoView)findViewById(R.id.video);
        imageView = (ImageView)findViewById(R.id.image);

        b_gallery = (Button)findViewById(R.id.b_gallery);
        b_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);

                mediaChooser.setType("video/*");
                startActivityForResult(Intent.createChooser(mediaChooser,"Select video"),RESULT_LOAD_IMAGE);
                Log.d("Debug","Stop here!!!!");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK){
          //  if (requestCode ==1){
            currImageURI = data.getData();
                //String path = getRealPathFromURI(currImageURI);
            path = currImageURI.getPath();
            Log.i("Path is: ",path );
            //videoProcess = new VideoProcess(videoView,path,context);
            videoProcess = new VideoProcess(imageView,path,context);
            videoProcess.processFrames();
            //videoProcess.play();
           // }
        }
    }

    public String getRealPathFromURI(Uri contentUri){

        String [] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getApplicationContext().getContentResolver().query(contentUri,
                proj,
                null,
                null,
                null);

        int column_idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_idx);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}