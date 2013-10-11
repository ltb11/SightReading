package org.sightreading;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

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
	private static final String TAG = "SightReadingActivity";

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
				testImageLoad();
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
		testImageLoad();

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

	public void testImageLoad() {
		File sdDir = Environment.getExternalStorageDirectory();
		String sdPath = sdDir.getAbsolutePath();
		Mat houghMat = Highgui.imread(sdPath + "/DCIM/square.png", 0);

		if (houghMat == null) {
			Log.i(TAG, "There was a problem loading the image");
		}

		//Imgproc.Canny(houghMat, houghMat, 0, 100);

		for (int i = 0; i < houghMat.height(); i++) {
			for (int j = 0; j < houghMat.width(); j++) {
				for (int k = 0; k < houghMat.channels(); k++) {
					double[] values = houghMat.get(i,j);
					for (int d = 0; d < values.length; d++) {
						values[d] = 255 - values[d];
					}
					houghMat.put(i, j, values);
				}
			}
		}
		
		//Mat houghMatBis = new Mat(houghMat, Range.all());

		//Highgui.imwrite(sdPath + "/DCIM/workedOutBars2.png", houghMat);

		// Mat test = new Mat(houghMat.size(), houghMat.type());

		Mat lines = new Mat();

		Imgproc.HoughLines(houghMat, lines, 1, Math.PI / 180, 200);
		
		for (int i = 0; i < lines.cols();i++) {
			Log.v(TAG, lines.get(0, i)[0] + "," + lines.get(0, i)[1]*180/Math.PI);
		}
		
		Log.v(TAG, lines.toString());

		double[] data;
		double rho, theta;
		Point pt1 = new Point();
		Point pt2 = new Point();
		double a, b;
		double x0, y0;
		Scalar color = new Scalar(255, 0, 0);

		Mat imageMat = new Mat();
		Imgproc.cvtColor(houghMat, imageMat, Imgproc.COLOR_GRAY2BGR);
		
		//imageMat.convert

		for (int i = 0; i < lines.cols(); i++) {
			data = lines.get(0, i);
			rho = data[0];
			theta = data[1];
			a = Math.cos(theta);
			b = Math.sin(theta);
			x0 = a * rho;
			y0 = b * rho;
			pt1.x = Math.round(x0 + 10*(-b));
			pt1.y = Math.round(y0 + 10*a);
			pt2.x = Math.round(x0 - 10*(-b));
			pt2.y = Math.round(y0 - 10*a);
			Core.line(imageMat, pt1, pt2, color, 1);
			// Core.line(test, pt1, pt2, color, 1);
		}

		// Bitmap bmp = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(),
		// Bitmap.Config.ARGB_8888);
		
		Highgui.imwrite(sdPath + "/DCIM/squareOut.png", imageMat);
		// Highgui.imwrite(sdPath + "/DCIM/test.png", test);
		finish();
	}
}