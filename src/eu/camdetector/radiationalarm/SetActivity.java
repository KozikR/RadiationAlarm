package eu.camdetector.radiationalarm;


import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class SetActivity extends Activity {
	protected static final String TAG = "Set";
	protected static final String THRESHOLD = "threshold";
	protected static final String ALARM_LEVEL = "alarm_level";
	protected static final int REQUEST_CALIBRATION = 1;
	
	private EditText editThreshold;
	private EditText editAlarm_level;
	private Button buttonCalibrate;
	private Button buttonSave;
	private Button buttonBack;
	
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // Setup the window
	        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	        setContentView(R.layout.set);
	        //Set view
	        editThreshold = (EditText) findViewById(R.id.threshold);
	        editAlarm_level = (EditText) findViewById(R.id.alarm_level);
	        buttonCalibrate = (Button) findViewById(R.id.calibrate);
	        buttonSave = (Button) findViewById(R.id.save);
	        buttonBack = (Button) findViewById(R.id.back);
	        
	        editThreshold.setText(Integer.toString(getIntent().getExtras().getInt(THRESHOLD)));
	        editAlarm_level.setText(Integer.toString(getIntent().getExtras().getInt(ALARM_LEVEL)));
	        	        
	        buttonBack.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					finish();					
				}	        	
	        });
	        buttonSave.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
	            	Intent intent = new Intent();
	            	intent.putExtra(THRESHOLD, Integer.parseInt(editThreshold.getText().toString()));
	            	intent.putExtra(ALARM_LEVEL, Integer.parseInt(editAlarm_level.getText().toString()));
	            	 // Set result and finish this Activity
		            setResult(Activity.RESULT_OK, intent);
		            finish();					
				}	        	
	        });
	        buttonCalibrate.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(), CalibrationActivity.class);
					startActivityForResult(intent, REQUEST_CALIBRATION);
				}
	        	
	        });

	        // Set result CANCELED incase the user backs out
	        setResult(Activity.RESULT_CANCELED);
	 }

/*	 private String getLocalIPAddress() {
		 WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		 WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		 int ip = wifiInfo.getIpAddress();
		 String ipString = String.format(
				 "%d.%d.%d.%d",
				 (ip & 0xff),
				 (ip >> 8 & 0xff),
				 (ip >> 16 & 0xff),
				 (ip >> 24 & 0xff));
		 return ipString;		 
	 }
*/	 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CALIBRATION:
        	if (resultCode == Activity.RESULT_OK)
        	{
        		//editThreshold.setText(Integer.toString(data.getExtras().getInt(CalibrationActivity.THRESHOLD)));
        		//editAlarm_level.setText(Integer.toString(data.getExtras().getInt(CalibrationActivity.ALARM_LEVEL)));        		
        	}
        	else
        	{}
        	break;
        default:
        	break;
        }
    }

}
