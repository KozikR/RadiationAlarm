package eu.camdetector.radiationalarm;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class CalibrationActivity extends Activity {
	public final static String FIRST_TH = "first_th";
	public final static String ALARM_LEVEL = "alarm_level";
	
	public final static int FIRST_TH_OFFSET = 15;
	protected static final String TAG = "CAL";
	
	public ProgressBar progress;
	private EditText first_th_edit;
	private EditText alarm_level_edit;
	private Button save_button;
	private Button back_button;
	private HistogramView histogram_view;
	
	private int first_th = 0;
	private int alarm_level = 0;
	
	private int[] histogram = new int[256];
	private int number_of_frames;
	public int frames = 200;
	
	private Camera mCamera;
	private SurfaceTexture mSurfaceTexture;	
	
	@Override
	 protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(Activity.RESULT_CANCELED);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.calibration);
        
                     
        progress = (ProgressBar) findViewById(R.id.progressBar);
        first_th_edit = (EditText) findViewById(R.id.c_first_th);
        alarm_level_edit = (EditText) findViewById(R.id.c_second_th);
        save_button = (Button) findViewById(R.id.c_save);
        back_button = (Button) findViewById(R.id.c_back);
        histogram_view = (HistogramView) findViewById(R.id.histogram);
        
        first_th = getIntent().getExtras().getInt(FIRST_TH);
        first_th_edit.setText(Integer.toString(first_th));
        alarm_level = getIntent().getExtras().getInt(ALARM_LEVEL);
        alarm_level_edit.setText(Integer.toString(alarm_level));        
     
        back_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				finish();
			}        	
        });
        save_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				
            	intent.putExtra(FIRST_TH, Integer.parseInt(first_th_edit.getText().toString()));
            	intent.putExtra(ALARM_LEVEL, Integer.parseInt(alarm_level_edit.getText().toString()));
            	// Set result and finish this Activity
	            setResult(Activity.RESULT_OK, intent);
	            finish();				
			}       	
        });
        
        getCameraInstance();
        
        new AlertDialog.Builder(this)
    	.setTitle(R.string.calibrate)
    	.setItems(R.array.CalibrationOptions, 
    			new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialoginterface, int i) {
    			if(i == 1)	frames = 1000;
    			else frames = 150;
    	        Log.d(TAG, "Frames " + frames);
    	        progress.setMax(frames);
    		}
    	}).show();     

   	}
	
	
	@Override
	protected void onPause() {
	    super.onPause();	
	    if (mCamera != null){
        	try {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
            } catch (Exception e){
              // ignore: tried to stop a non-existent preview
            }
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    getCameraInstance();
	}
	
	private PreviewCallback mPreviemCallback = new PreviewCallback() {			
		@Override
		public void onPreviewFrame(byte[] data, Camera camera)
		{
			number_of_frames++;
			progress.setProgress(number_of_frames);
			if(number_of_frames <= frames)
			{
				Parameters cameraParameters = camera.getParameters();
				int imageFormat = cameraParameters.getPreviewFormat();
				if (imageFormat == ImageFormat.NV21) {
					Size size = camera.getParameters().getPreviewSize();
					for(int i=0; i < size.height; i++)
						for(int j=0; j < size.width; j++)
						{							
							{
								int nValue = 0xff & (int) data[i*size.width + j];
								histogram[nValue]++;											
							}
						}			
				}
				histogram_view.drawHistogram(histogram);
			}
			else
			{				
				for(first_th = 255; first_th >= 0; first_th--)
					if(histogram[first_th] > 0)
					{
						first_th++;
						break;
					}
				
				first_th+=FIRST_TH_OFFSET;
				
				first_th_edit.setText(Integer.toString(first_th));
								
				save_button.setEnabled(true);
				save_button.setFocusable(true);
			}
		}
	};
	
	public void getCameraInstance(){
	    if(mCamera == null)
	    {
	    try {
	        mCamera = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    if(mCamera == null)	; //if error when open camera
	    else
	    {
			Camera.Parameters parameters=mCamera.getParameters();
		    Camera.Size size=getBestPreviewSize(parameters);
		    		    
		    if (size != null) {
		    	parameters.setPreviewSize(size.width, size.height);
		    	mCamera.setParameters(parameters);          
		    }
		   
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
		    mSurfaceTexture = new SurfaceTexture(1);
		    try {
				mCamera.setPreviewTexture(mSurfaceTexture);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    }
		    mCamera.setPreviewCallback(mPreviemCallback);
		    mCamera.startPreview();
		    }
	    }
	}
	 private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
	 		Camera.Size result=parameters.getPreviewSize();
	 		
	 		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
	 			
	 			int resultArea=result.width * result.height;
	 			int newArea=size.width * size.height;
				if (newArea > resultArea) result=size;
	 		}
	 		return(result);
	 }
}
