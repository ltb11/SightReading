package org.sightreading;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;

import utils.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

public class SightReadingActivity extends Activity implements OnTouchListener,
		CvCameraViewListener2 {
	public static final String TAG = "SightReadingActivity";

	private boolean mIsColorSelected = false;
	private Mat mRgba;
	private Scalar mBlobColorRgba;
	private Mat mSpectrum;

	private CameraBridgeViewBase mOpenCvCameraView;

	/*
	 * private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
	 * {
	 * 
	 * @Override public void onManagerConnected(int status) { switch (status) {
	 * case LoaderCallbackInterface.SUCCESS: { Log.i(TAG,
	 * "OpenCV loaded successfully"); mOpenCvCameraView.enableView();
	 * mOpenCvCameraView.setOnTouchListener(SightReadingActivity.this); } break;
	 * default: { super.onManagerConnected(status); } break; } } };
	 */

	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				testImage("oneStave.png", "oneStaveOut.png");
				testImage("twoStaves.png", "twoStavesOut.png");
				testImage("threeStaves.png", "threeStavesOut.png");
				testImage("complexStaves.png", "complexStavesOut.png");
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this,
				mOpenCVCallBack)) {
			Log.e("TEST", "Cannot connect to OpenCV Manager");
		}

		setContentView(R.layout.sight_reading_surface_view);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.sight_reading_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mOpenCVCallBack);
		// mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mSpectrum = new Mat();
		mBlobColorRgba = new Scalar(255);
	}

	public void onCameraViewStopped() {
		mRgba.release();
	}

	public boolean onTouch(View v, MotionEvent event) {
		//testImage("/DCIM/simpleBars.png", "/DCIM/simpleBarsOut.png");

		return false; // don't need subsequent touch events
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();

		if (mIsColorSelected) {
			Mat colorLabel = mRgba.submat(4, 68, 4, 68);
			colorLabel.setTo(mBlobColorRgba);

			Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70,
					70 + mSpectrum.cols());
			mSpectrum.copyTo(spectrumLabel);
		}

		return mRgba;
	}

	public void testImage(String src, String dst) {
		File sdDir = Environment.getExternalStorageDirectory();
		String sdPath = sdDir.getAbsolutePath() + "/DCIM/";
		Mat houghMat = Highgui.imread(sdPath + "input/" + src, 0);

		if (houghMat == null)
			Log.i(TAG, "There was a problem loading the image");
		
		Mat imageMat = Utils.detectMusic(houghMat);
		Highgui.imwrite(sdPath + "output/" + dst, imageMat);
		finish();
	}
}