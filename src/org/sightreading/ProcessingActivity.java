package org.sightreading;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class ProcessingActivity extends Activity {

	public static final String TAG = "SRCameraActivity";
	public static EditText currentFileName;
	public final static long startTime = System.currentTimeMillis();

	public static Bitmap image;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onProcCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_processing_view);
		
		initialiseButtons();
	}
	
	private void initialiseButtons() {

	}
	
}
