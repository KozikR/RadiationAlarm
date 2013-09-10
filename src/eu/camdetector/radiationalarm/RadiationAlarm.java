package eu.camdetector.radiationalarm;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;




public class RadiationAlarm extends Activity {
	protected static final String TAG = "RadiationAlarm";
	private static final int REQUEST_CALIBRATION = 1;
	private static final int REQUEST_INIT_INFO = 2;
	
	private static final int ALARM_LENGHT = 30;
	
	private static final int FIRST_TH_W = 1;
	
	private SharedPreferences sharedPref;
	private Camera mCamera;
	private SurfaceTexture mSurfaceTexture;
	
	private TextView text_info;
	private TextView image_info;
	
	public Handler Handler;
	
	public int first_th = 60;
	public int show_info;
	public int alarm = 0;
	public int alarm_level = 4;
	
	private final static int frames = 100;
	private int[] bright_px;
	private int current_frame = 0;
	private int sum = 0;
		
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
						if(nValue >= first_th)	number_of_bright_px+=FIRST_TH_W;
					}
				current_frame++;
				if(current_frame >= frames)	current_frame = 0;
				sum -= bright_px[current_frame];
				sum += number_of_bright_px;
				bright_px[current_frame] = number_of_bright_px;
			//Log.d(TAG, size.width + ":" + size.height  + "NofBP: " + number_of_bright_px);
			//	Log.d(TAG, "New threshold" + threshold);
					
				
				TextView text_bright = (TextView) findViewById(R.id.text_bright);				
				
				String to_format = getResources().getString(R.string.info_bright);
				text_bright.setText(String.format(to_format, number_of_bright_px) + "\nTh: " + first_th + " ALv: " + alarm_level + "\nSum: " + sum + " AlarmTime: " + alarm );
				
				
				
				if(sum > alarm_level)	alarm = ALARM_LENGHT;
				if(alarm > 0)
				{
					change_state(2);
					alarm--;
				}
				else	change_state(1);
			}
		}
	};
	private void load_data(){
		sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        first_th = sharedPref.getInt(getResources().getString(R.string.saved_threshold), 15);
        alarm_level = sharedPref.getInt(getResources().getString(R.string.saved_alarm_level), 4);
        show_info = sharedPref.getInt(getResources().getString(R.string.saved_info_show), 1);
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_radiation_alarm);		
		TextView text_bright = (TextView) findViewById(R.id.text_bright);
		text_info = (TextView) findViewById(R.id.text_info);
		image_info = (TextView) findViewById(R.id.image_info);
		//Set number of bright pixels to 0 
		String to_format = getResources().getString(R.string.info_bright);
		text_bright.setText(String.format(to_format, 0));
		
		load_data();
        
        if(show_info == 1)
        {
        	//show info and calibrate!!!
			Intent intent = new Intent(getApplicationContext(), InitInfoActivity.class);
			startActivityForResult(intent, REQUEST_INIT_INFO);
        }

        
		Log.d(TAG, "START");
		//Create an instance of Camera
		getCameraInstance();
		
		bright_px = new int[frames];
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
	    load_data();
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
		    //Log.d(TAG, size.width + "x" + size.height);
		    
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
		if(state == 1)
		{			
			image_info.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ok), null, null);
			text_info.setText(getResources().getString(R.string.info_save));
			text_info.setTextColor(getResources().getColor(R.color.no_radiation));
		}
		else if(state == 2)
		{
			text_info.setText(getResources().getString(R.string.info_ded));
			image_info.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.radiation_detect), null, null);
			text_info.setTextColor(getResources().getColor(R.color.radiation_detect));
		}
		else if(state == 3)
		{
			text_info.setText(getResources().getString(R.string.info_no_cam));
			image_info.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
	}	
	
	//MENU*******************************************************
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		MenuInflater v_MenuInflater = getMenuInflater();
		v_MenuInflater.inflate(R.menu.activity_radiation_alarm, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem element){
		switch(element.getItemId()){
		case R.id.menu_settings:
			Intent setIntent = new Intent(this, CalibrationActivity.class);
			setIntent.putExtra(CalibrationActivity.FIRST_TH, first_th);
			setIntent.putExtra(CalibrationActivity.ALARM_LEVEL, alarm_level);
            startActivityForResult(setIntent, REQUEST_CALIBRATION);
			return true;
		case R.id.menu_info:
			new InfoDialog(this).show();
			return true;
		}
		return false;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CALIBRATION:
        	if (resultCode == Activity.RESULT_OK)
        	{
        		Editor edit = sharedPref.edit();
        		
        		first_th = data.getExtras().getInt(CalibrationActivity.FIRST_TH);
        		edit.putInt(getResources().getString(R.string.saved_threshold), first_th);
        		
        		alarm_level = data.getExtras().getInt(CalibrationActivity.ALARM_LEVEL);
        		edit.putInt(getResources().getString(R.string.saved_alarm_level), alarm_level);
        		
   		   		edit.commit();
        		
        	}
        	else
        	{
        		
        	}
        	break;
        case REQUEST_INIT_INFO:
        	if(resultCode == Activity.RESULT_OK)
        	{
        		Editor edit = sharedPref.edit();
        		
        		first_th = data.getExtras().getInt(InitInfoActivity.FIRST_TH);
        		edit.putInt(getResources().getString(R.string.saved_threshold), first_th);
        		
        		alarm_level = data.getExtras().getInt(InitInfoActivity.ALARM_LEVEL);
        		edit.putInt(getResources().getString(R.string.saved_alarm_level), alarm_level);
        		
        		show_info = data.getExtras().getInt(InitInfoActivity.SHOW_INFO);
        		edit.putInt(getResources().getString(R.string.saved_info_show), show_info);
        		
        		edit.commit();
        		
        	}
        default:
        	break;
        }
    }
	
}


