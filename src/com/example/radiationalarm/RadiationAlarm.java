package com.example.radiationalarm;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;




public class RadiationAlarm extends Activity {
	protected static final String TAG = "RadiationAlarm";
	private Camera mCamera;
	
	int threshold = 10;
	int alarm_level = 10;
	
	private PreviewCallback mPreviemCallback = new PreviewCallback() {			
		@Override
		public void onPreviewFrame(byte[] data, Camera camera)
		{
			
			int number_of_bright_px = 0;
			Parameters cameraParameters = camera.getParameters();
			int imageFormat = cameraParameters.getPreviewFormat();
			if (imageFormat == ImageFormat.NV21) {
				Size size = camera.getParameters().getPreviewSize();
				for(int i=0; i < size.height; i++)
					for(int j=0; j < size.width; j++)
					{
						int nValue = 0xff & (int) data[i*size.width + j];
						if(nValue >= threshold)	number_of_bright_px++;	
					}				
				//Log.d(TAG, size.width + ":" + size.height  + "NofBP: " + number_of_bright_px);
				
				TextView text_bright = (TextView) findViewById(R.id.text_bright);				
				
				String to_format = getResources().getString(R.string.info_bright);
				text_bright.setText(String.format(to_format, number_of_bright_px));
				if(number_of_bright_px > alarm_level)	change_state(2);
				else	change_state(1);
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_radiation_alarm);		
		TextView text_bright = (TextView) findViewById(R.id.text_bright);
		
		//Set number of bright pixels to 0 
		String to_format = getResources().getString(R.string.info_bright);
		text_bright.setText(String.format(to_format, 0));
		Log.d(TAG, "START");
		//Create an instance of Camera
		getCameraInstance();
		
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
	
	/** A safe way to get an instance of the Camera object. */
	public void getCameraInstance(){
	    if(mCamera == null)
	    {
	    try {
	        mCamera = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    if(mCamera == null)	change_state(3); //if error when open camera
	    else
	    {
			Camera.Parameters parameters=mCamera.getParameters();
		    Camera.Size size=getBestPreviewSize(parameters);
		    Log.d(TAG, size.width + "x" + size.height);
		    
		    if (size != null) {
		    	parameters.setPreviewSize(size.width, size.height);
		    	mCamera.setParameters(parameters);          
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
	
	 @Override
	 public void onConfigurationChanged(Configuration newConfig) {
		
	     super.onConfigurationChanged(newConfig);
	     
	     try
	     {
	     mCamera.stopPreview();
	     mCamera.setDisplayOrientation(newConfig.orientation);
	     mCamera.startPreview();
	     }
	     catch (Exception e){
	    	// ignore: tried to stop a non-existent preview
	     }
//	     if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//	         Intent el = new Intent(MapaMovilAR.this,
//	                 cl.puc.memoria.EmergenciesList.class);
//	         startActivity(el);
//	     } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//	         Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
//	     }
	 }
	/**Change state of display **/
	void change_state(int state)
	{
		TextView text_info = (TextView) findViewById(R.id.text_info);
		if(state == 1)
		{			
			text_info.setText(getResources().getString(R.string.info_save));
			text_info.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ok), null, null);
			text_info.setTextColor(getResources().getColor(R.color.no_radiation));
		}
		else if(state == 2)
		{
			text_info.setText(getResources().getString(R.string.info_ded));
			text_info.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.radiation_detect), null, null);
			text_info.setTextColor(getResources().getColor(R.color.radiation_detect));
		}
		else if(state == 3)
		{
			text_info.setText(getResources().getString(R.string.info_no_cam));
			text_info.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
	}	
}
