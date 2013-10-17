package org.sightreading;

import musicdetection.DetectMusic;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import utils.Utils;
import android.app.Activity;
import android.os.Bundle;
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

	}

	public void testImage(String src, String dst) {
		String srcPath = getPath("input/" + src);
		
		Mat houghMat = readImage(srcPath);
		String notePath = getPath("assets/notehead.png");
		DetectMusic.noteHead = readImage(notePath);

		Mat imageMat = DetectMusic.detectMusic(houghMat);
		writeImage(imageMat, getPath("output/" + dst));
		finish();
	}
	
	public Mat readImage(String src){
		
		Mat img =  Highgui.imread(src, 0);
		if (img == null)
			Log.i(TAG, "There was a problem loading the image " + src);
		return img;
	}
	
	public static void writeImage(Mat src, String dst){
		Highgui.imwrite(dst, src);
	}
	
	//returns the path of a given src image, assuming root directory of DCIM
	public static String getPath(String src){
		return Utils.sdPath + src;
	}
}
