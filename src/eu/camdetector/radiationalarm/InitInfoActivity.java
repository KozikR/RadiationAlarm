package eu.camdetector.radiationalarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class InitInfoActivity extends Activity {
	private final static int REQUEST_CALIBRATION = 1;
	
	protected static final String FIRST_TH = "first_th";
	protected static final String ALARM_LEVEL = "alarm_level";
	protected static final String SHOW_INFO = "show_info";
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setTitle(R.string.Info);
		setContentView(R.layout.init_info);
		
		Button back = (Button) findViewById(R.id.info_cali);
		back.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), CalibrationActivity.class);
				intent.putExtra(CalibrationActivity.FIRST_TH, 60);
				intent.putExtra(CalibrationActivity.ALARM_LEVEL, 5);
				startActivityForResult(intent, REQUEST_CALIBRATION);				
			}
		});
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CALIBRATION:
        	if (resultCode == Activity.RESULT_OK)
        	{
        		Intent intent = new Intent();
            	intent.putExtra(FIRST_TH, data.getExtras().getInt(CalibrationActivity.FIRST_TH));
            	intent.putExtra(ALARM_LEVEL, data.getExtras().getInt(CalibrationActivity.ALARM_LEVEL));
            	CheckBox show_info_box = (CheckBox) findViewById(R.id.info_dont_show);
            	int show_info_status = 1;
            	if(show_info_box.isChecked()) show_info_status = 2;            	        		
            	intent.putExtra(SHOW_INFO, show_info_status);
	            setResult(Activity.RESULT_OK, intent);
	            finish();  		
        	}
        	else
        	{        		
        	}
        	break;
        default:
        	break;
        }
    }

}
