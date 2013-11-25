package org.sightreader;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import utils.OurUtils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CameraActivity extends Activity implements OnTouchListener,
		CvCameraViewListener2 {
	public static final String TAG = "SRCameraActivity";
	public static EditText currentFileName;
	public final static long startTime = System.currentTimeMillis();

	private SRCameraView mOpenCvCameraView;
	private Mat mRgba;
	private static int totalImages = 0;

	private Button done;

	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				mOpenCvCameraView.enableView();
				mOpenCvCameraView.setOnTouchListener(CameraActivity.this);
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCamCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_camera_view);

		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this,
				mOpenCVCallBack)) {
			Log.e("TEST", "Cannot connect to OpenCV Manager in cam");
		}

		(new File(OurUtils.getPath("") + File.separator + "input")).mkdirs();
		(new File(OurUtils.getPath("") + File.separator + "output")).mkdirs();
		(new File(OurUtils.getPath("") + File.separator + "assets")).mkdirs();
		(new File(OurUtils.getPath("") + File.separator + "captured")).mkdirs();

		mOpenCvCameraView = (SRCameraView) findViewById(R.id.sight_reading_camera_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);

		initialiseButtons();
	}

	private void initialiseButtons() {
		done = (Button) findViewById(R.id.buttonCameraDone);
		done.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (totalImages > 0) {
					Intent i = new Intent(CameraActivity.this,
							ProcessingActivity.class);
					startActivity(i);
				}
			}
		});
	}

	public static int totalImagesCaptured() {
		return totalImages;
	}
	
	private void updateText() {
		TextView text = (TextView) findViewById(R.id.savedPagesText);
		text.setText("You have saved " + totalImages + " pages");
	}

	public static void savePage(Bitmap bitmap) {
		totalImages++;
		String fName = "page"+ totalImages;
		
		// QUICK FIX
		Bitmap rotated = OurUtils.RotateBitmap(bitmap, 360);
		
		OurUtils.saveTempImage(rotated,fName);
	}

	public CameraActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());

	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mOpenCVCallBack);
		updateText();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// mRgba = inputFrame.rgba();
		// return mRgba;

		Mat input = inputFrame.rgba();
		mRgba = input;

		return mRgba;

		// Mat mRgbaT = input.t();
		// Core.flip(input.t(), mRgbaT, 1);

		// Imgproc.resize(mRgbaT, mRgbaT, new
		// Size(input.height(),input.width()));
		// Imgproc.resize(mRgbaT, mRgbaT, new
		// Size(input.width(),input.height()));

		/*
		 * Bitmap image = Bitmap.createBitmap(mRgba.width(), mRgba.height(),
		 * Bitmap.Config.ARGB_8888); Utils.matToBitmap(mRgba, image);
		 * 
		 * ImageView layout = (ImageView) findViewById(R.id.cameraPreview);
		 * layout.setImageDrawable(new BitmapDrawable(this.getResources(),
		 * image));
		 */

		// return mRgbaT;
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(width, height, CvType.CV_8UC4);
	}

	@Override
	public void onCameraViewStopped() {
		mRgba.release();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		/*
		 * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		 * String currentDateandTime = sdf.format(new Date()); String fileName =
		 * Utils.getPath("") + "/captured/" + currentDateandTime + ".jpg";
		 * mOpenCvCameraView.takePicture(fileName);
		 * 
		 * Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
		 */
		Bitmap image = Bitmap.createBitmap(mRgba.width(), mRgba.height(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(mRgba, image);

		Intent i = new Intent(CameraActivity.this, DisplayPhotoActivity.class);
		DisplayPhotoActivity.image = image;
		startActivity(i);

		return false;
	}
}
