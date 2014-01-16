package org.sightreader;

import utils.OurUtils;
import android.app.Activity;
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

public class DisplayPhotoActivity extends Activity {

	public static final String TAG = "DisplayPhotoActivity";
	public static EditText currentFileName;
	// public final static long startTime = System.currentTimeMillis();

	private Button accept;
	private Button discard;

	public static Bitmap image;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate DisplayPhotoActivity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_image_view);

		BitmapDrawable drawable = new BitmapDrawable(this.getResources(), image);
		ImageView layout = (ImageView) findViewById(R.id.imagePreview);
		layout.setImageDrawable(drawable);

		initialiseButtons();
	}

	public void onDestroy() {
		super.onDestroy();
		image = null;
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

	private void discardImage() {
		finish();
	}

	private void keepImage() {
		savePage(image, CameraActivity.getTotalImages());
		CameraActivity.incrementTotalImages();
		finish();
	}

	public static void savePage(Bitmap bitmap, int totalImages) {
		Log.i(TAG, "Saving bitmap to file");
		long startTime = System.currentTimeMillis();
		Log.i(TAG, "before save " + (System.currentTimeMillis() - startTime));

		String fName = "page" + totalImages;

		OurUtils.saveTempImage(bitmap, fName);

		Log.i(TAG, "after save " + (System.currentTimeMillis() - startTime));
	}

}
