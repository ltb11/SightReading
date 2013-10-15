package org.sightreading;

import java.io.File;

import musicdetection.DetectMusic;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class SightReadingActivity extends Activity {
	public static final String TAG = "SightReadingActivity";

	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				//testImage("oneStave.png", "oneStaveOut.png");
				//testImage("twoStaves.png", "twoStavesOut.png");
				//testImage("threeStaves.png", "threeStavesOut.png");
				//testImage("complexStaves.png", "complexStavesOut.png");
				testImage("baaBaaDistorted.jpg", "baaBaaOut.png");
				
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

	}

	public void testImage(String src, String dst) {
		File sdDir = Environment.getExternalStorageDirectory();
		String sdPath = sdDir.getAbsolutePath() + "/DCIM/";
		Mat houghMat = Highgui.imread(sdPath + "input/" + src, 0);
		DetectMusic.noteHead = Highgui.imread(sdPath + "assets/notehead.png", 0);

		if (houghMat == null)
			Log.i(TAG, "There was a problem loading the image");

		Mat imageMat = DetectMusic.detectMusic(houghMat);
		Highgui.imwrite(sdPath + "output/" + dst, imageMat);
		finish();
	}
}
