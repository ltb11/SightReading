package org.sightreading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import musicdetection.MusicDetector;
import musicdetection.Line;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import playback.Playback;

import utils.SheetStrip;
import utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SightReadingActivity extends Activity {
	public static final String TAG = "SightReadingActivity";
	public static EditText currentFileName;
	private Button scan;

	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				// Due to an unfinished implementation of the GUI, please use
				// this method to change the file name. The app will run,
				// displaying no
				// GUI and exit when done scanning
				
				//Playback.test();
				//Playback.playMidiFile("teddybear.mid");
				
				((EditText) findViewById(R.id.filePath))
						.setText("Distorted.jpg");
				scan.performClick();
				
				
				
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public SightReadingActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
		
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_surface_view);

		initialiseButtons();

		/*
		 * button.setHeight(100); button.setText("I'm a motherfucking button!");
		 * RelativeLayout l = new RelativeLayout(this); l.addView(button, 200,
		 * 200); setContentView(l);
		 */

		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this,
				mOpenCVCallBack)) {
			Log.e("TEST", "Cannot connect to OpenCV Manager");
		}

		(new File(Utils.getPath("") + File.separator + "input")).mkdirs();
		(new File(Utils.getPath("") + File.separator + "output")).mkdirs();
		(new File(Utils.getPath("") + File.separator + "assets")).mkdirs();

		// finish();

	}

	private void initialiseButtons() {
		scan = (Button) findViewById(R.id.scan);
		scan.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((TextView) findViewById(R.id.scanning))
						.setVisibility(View.VISIBLE);
				v.refreshDrawableState();
				scanImage();
			}
		});

		ImageButton imageButton = (ImageButton) findViewById(R.id.camera);
		imageButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				takePicture();
			}
		});

		currentFileName = (EditText) findViewById(R.id.filePath);
		currentFileName
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == KeyEvent.KEYCODE_ENTER) {
							// hide virtual keyboard
							InputMethodManager imm = (InputMethodManager) v
									.getContext().getSystemService(
											Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
							currentFileName.setCursorVisible(false);
							return true;
						}
						return false;
					}
				});

		LinearLayout l = (LinearLayout) findViewById(R.id.globalLayout);
		l.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (currentFileName.isFocused()) {
						Rect outRect = new Rect();
						currentFileName.getGlobalVisibleRect(outRect);
						if (!outRect.contains((int) event.getRawX(),
								(int) event.getRawY())) {
							currentFileName.clearFocus();
							InputMethodManager imm = (InputMethodManager) v
									.getContext().getSystemService(
											Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
						}
					}
				}
				return false;
			}
		});
	}

	private void takePicture() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, 2);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 2) {
			Bitmap photo = (Bitmap) data.getExtras().get("data");
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyymmddhhmmss", Locale.UK);
			String date = dateFormat.format(new Date());
			String photoFile = Utils.getPath("pictures/IMG_" + date + ".jpg");
			try {
				FileOutputStream out = new FileOutputStream(photoFile);
				photo.compress(Bitmap.CompressFormat.PNG, 0, out);
				ImageView currentView = ((ImageView) findViewById(R.id.currentImage));
				Bitmap bitmapPic = BitmapFactory.decodeFile(photoFile);
				currentView.setImageBitmap(bitmapPic);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else
			super.onActivityResult(requestCode, resultCode, data);
	}

	private void scanImage() {
		String src = ((EditText) findViewById(R.id.filePath)).getText()
				.toString();
		testImage(src, Utils.getDest(src));
	}

	public void testImage(String src, String dst) {
		String srcPath = Utils.getPath("input/" + src);
		Mat input = Utils.readImage(srcPath);
		Mat scaledInput = Utils.resizeImage(input, Utils.STANDARD_IMAGE_WIDTH);

		Mat output = scaledInput.clone();
		Imgproc.cvtColor(output, output, Imgproc.COLOR_GRAY2BGR);

		MusicDetector detector = new MusicDetector(scaledInput);
		detector.detect();
		detector.print(output);

		Utils.writeImage(output, Utils.getPath("output/" + dst));
		finish();
	}
}
