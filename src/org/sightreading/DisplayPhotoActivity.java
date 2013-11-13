package org.sightreading;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class DisplayPhotoActivity extends Activity implements OnTouchListener {

	public static final String TAG = "SRCameraActivity";
	public static EditText currentFileName;
	public final static long startTime = System.currentTimeMillis();

	private Button accept;
	private Button discard;
	
	public static Bitmap image;

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCamCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_image_view);
		
		BitmapDrawable drawable = new BitmapDrawable(this.getResources(), image);
		ImageView layout = (ImageView) findViewById(R.id.imagePreview);
		layout.setImageDrawable(drawable);
		
		initialiseButtons();
	}
	
	private void initialiseButtons() {
		accept = (Button) findViewById(R.id.buttonCameraKeepImage);
		accept.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				DisplayPhotoActivity.this.keepImage();
			}
		});
		discard = (Button) findViewById(R.id.buttonCameraDiscardImage);
		discard.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				DisplayPhotoActivity.this.discardImage();
			}
		});
	}

	private  void discardImage() {
		finish();
	}

	private void keepImage() {
		CameraActivity.savePage(image);
		finish();
	}
	
	
}
