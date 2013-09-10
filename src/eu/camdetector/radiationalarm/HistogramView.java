package eu.camdetector.radiationalarm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.util.AttributeSet;
import android.view.View;

//wyswietla histogram

public class HistogramView extends View {
	private static final String TAG = "HistogramView";
	private float width = 0;
	private float height = 100;
	private float r_width = 0;
	private float part_hight;
	
	private int[] data_h;
	
	Path chart = new Path();
	Paint paint_chart = new Paint();
	Paint back = new Paint();

	public HistogramView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint_chart.setColor(Color.BLUE);
		back.setColor(Color.WHITE);
	}
	
	public void drawHistogram(int[] data)
	{
		r_width = getWidth()/256f;
		int max_value = 0;
		for(int i=0; i < 256; i++)
			if(data[i] > max_value)
				max_value = data[i];
		part_hight = 0;
		if(max_value != 0)
			part_hight = getHeight() / (1.0f * max_value);
		else
			part_hight = 1;
		
		chart.reset();
		for(int i = 0; i < 256; i++)
			chart.addRect(i*r_width, getHeight(), (i+1)*r_width, getHeight()-part_hight*data[i], Direction.CW);
		data_h = data;
		invalidate();
	}
	
	protected void onDraw(Canvas canvas){
		canvas.drawRect(0, 0, getWidth(), getHeight(), back);
		canvas.drawPath(chart, paint_chart);	
		if(data_h instanceof int[])
		{
			r_width = getWidth()/256f;
			for(int i =0; i < 256; i++)
				canvas.drawRect(i*r_width, (float) getHeight(), (i+1)*r_width, getHeight()-part_hight*data_h[i], paint_chart);
		}
		//Log.d(TAG, "DRAW");
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		width = w;
		height = h;
		r_width = w / 256f;
		super.onSizeChanged(w, h, oldw, oldh);
	}

}
