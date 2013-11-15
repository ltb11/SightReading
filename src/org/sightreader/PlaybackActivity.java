package org.sightreader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;

public class PlaybackActivity extends Activity implements OnTouchListener {

	public static final String TAG = "SRPlaybackActivity";
	public final static long startTime = System.currentTimeMillis();

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "playback creating");
		super.onCreate(savedInstanceState);
		String filePath = savedInstanceState.getString("FILEPATH");
		
	}
}
