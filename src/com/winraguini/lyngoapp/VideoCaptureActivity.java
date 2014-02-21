package com.winraguini.lyngoapp;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;

import com.winraguini.lyngoapp.R;
import com.winraguini.lyngoapp.R.id;
import com.winraguini.lyngoapp.R.layout;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;


public class VideoCaptureActivity extends Activity implements Callback {

    @Override
    protected void onDestroy() {
        stopRecording();
        super.onDestroy();
    }

    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    public MediaRecorder mrec = new MediaRecorder();    
    private Camera mCamera;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        
        setContentView(R.layout.activity_video_capture);

       
        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mCamera = Camera.open();
        
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        menu.add(0, 0, 0, "Start");        
        return super.onCreateOptionsMenu(menu);
    }

    
    public void onStartClicked(View v) {
    	try {
            
            startRecording();

        } catch (Exception e) {

            String message = e.getMessage();
            Log.i(null, "Problem " + message);
            mrec.release();
        }
    }
    
    public void onStopClicked(View v) {
    	mrec.stop();
    	mrec.reset();   
        mrec.release();
        mrec = null;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getTitle().equals("Start"))
        {
            try {
                
                startRecording();
                item.setTitle("Stop");

            } catch (Exception e) {

                String message = e.getMessage();
                Log.i(null, "Problem " + message);
                mrec.release();
            }

        }
        else if(item.getTitle().equals("Stop"))
        {
            mrec.stop();
            mrec.release();
            mrec = null;
            item.setTitle("Start");
        }

        return super.onOptionsItemSelected(item);
    }

    protected void startRecording() throws IOException
    {
        if(mCamera==null) {
            mCamera = Camera.open();
        }
         String filename;
         String path;
        
         path= Environment.getExternalStorageDirectory().getAbsolutePath().toString();
         
         Date date=new Date();
         filename="/rec"+date.toString().replace(" ", "_").replace(":", "_")+".mp4";
         
         //create empty file it must use
         File file=new File(path,filename);
         
        mrec = new MediaRecorder(); 

        mCamera.lock();
        mCamera.unlock();

        // Please maintain sequence of following code. 
        setDisplayOrientation(mCamera, 90);
        // If you change sequence it will not work.
        mrec.setCamera(mCamera);    
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC);     
        mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mrec.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile(path+filename);
        mrec.prepare();
        mrec.start();
    }

    protected void stopRecording() {

        if(mrec!=null)
        {
            mrec.stop();
            mrec.release();
            if (mCamera != null) {
            	mCamera.release();
            	mCamera.lock();
            }
        }
    }

    private void releaseMediaRecorder() {

        if (mrec != null) {
            mrec.reset(); // clear recorder configuration
            mrec.release(); // release the recorder object
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {      

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {       

        if (mCamera != null) {
            Parameters params = mCamera.getParameters();
            params.set("orientation", "portrait");
            mCamera.setParameters(params);

            //camera.setParameters(parameters);
            Log.i("Surface", "Created");
        }
        
        else {
            Toast.makeText(getApplicationContext(), "Camera not available!",
                    Toast.LENGTH_LONG).show();

            finish();
        }

    }
    
    protected void setDisplayOrientation(Camera camera, int angle){
        Method downPolymorphic;
        try
        {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[] { angle });
        }
        catch (Exception e1)
        {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	if (mCamera != null) {
    		mCamera.stopPreview();
    		mCamera.release(); 
    	}
    	releaseCamera();
    	releaseMediaRecorder();

    }
}
