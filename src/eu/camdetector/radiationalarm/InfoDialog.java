package eu.camdetector.radiationalarm;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class InfoDialog extends Dialog {

	public InfoDialog(Context context) {
		super(context);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setTitle(R.string.Info);
		setContentView(R.layout.info);
		
		Button back = (Button) findViewById(R.id.info_back);
		back.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
}
