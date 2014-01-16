package org.sightreader;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import utils.OurUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
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
	static int rotation = 0;
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

		// (new File(OurUtils.getPath("") + File.separator + "input")).mkdirs();
		// (new File(OurUtils.getPath("") + File.separator +
		// "output")).mkdirs();
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
				if (getTotalImages() > 0) {
					ProcessingActivity.SetPageNum(getTotalImages());
					CameraActivity.reset();

					Intent i = new Intent(CameraActivity.this,
							ProcessingActivity.class);
					startActivity(i);
					finish();
				} else {
					// TODO
				}
			}
		});
	}

	protected static void reset() {
		totalImages = 0;
	}

	private void updateText() {
		TextView text = (TextView) findViewById(R.id.savedPagesText);
		text.setText("  You have saved " + getTotalImages() + " pages");
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
		Mat mRgba = correctOrientation(CameraActivity.this, inputFrame.rgba());
		return mRgba;
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
		mOpenCvCameraView.setCallback(this);
		mOpenCvCameraView.takePicture(rotation);

		return false;
	}

	public Mat correctOrientation(Activity activity, Mat rgba) {
		rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();

		Mat rgbaT;

		switch (rotation) {
		case Surface.ROTATION_0:
			rgbaT = rgba.t();
			Core.flip(rgba.t(), rgbaT, 1);
			Imgproc.resize(rgbaT, rgbaT, rgba.size());
			return rgbaT;
		case Surface.ROTATION_90:
			return rgba;
		case Surface.ROTATION_180:
			rgbaT = rgba.t();
			Core.flip(rgba.t(), rgbaT, 0);
			Imgproc.resize(rgbaT, rgbaT, rgba.size());
			return rgbaT;
		case Surface.ROTATION_270:
			rgbaT = rgba;
			Core.flip(rgba, rgbaT, 1);
			Core.flip(rgbaT, rgbaT, 0);
			return rgbaT;
		}

		// TODO compensate for mirror if front facing camera
		return rgba;
	}

	public static int getTotalImages() {
		return totalImages;
	}

	public static void incrementTotalImages() {
		totalImages++;
	}
}
