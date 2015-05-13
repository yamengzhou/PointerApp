package com.uofa.yameng.laserpointertracking;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by Alex on 5/12/2015.
 */
public class VideoProcess {
    private VideoView videoView;
    private ImageView imageView;
    private String path;
    private Context context;
    public VideoProcess(){};

    public VideoProcess(VideoView videoView,String path, Context context){
        this.videoView = videoView;
        this.path = path;
        this.context = context;
    }

    public VideoProcess(ImageView imageView,String path, Context context){
        this.imageView = imageView;
        this.path = path;
        this.context = context;
    }

    public void play(){
        MediaController mediaController = new MediaController(context);
        videoView.setMediaController(mediaController);

        Uri uri = Uri.parse(path);

        videoView.setVideoURI(uri);

        videoView.requestFocus();
        videoView.start();
    }

    public void processFrames(){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        File file = new File(path);
        try{
            retriever.setDataSource(file.getAbsolutePath());
            imageView.setImageBitmap(retriever.getFrameAtTime(30000000,MediaMetadataRetriever.OPTION_CLOSEST));
            Bitmap bitmap = retriever.getFrameAtTime(3000000,MediaMetadataRetriever.OPTION_CLOSEST);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }catch (RuntimeException e){
            e.printStackTrace();
        }finally {
            {
                try{
                    retriever.release();
                }catch (RuntimeException e){

                }
            }
        }
    }
}
