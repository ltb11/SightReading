package org.sightreading;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import musicdetection.DetectMusic;
import musicdetection.Line;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

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
				// testImage("twoStaves.png", "twoStavesOut.png");
				// testImage("threeStaves.png", "threeStavesOut.png");
				// testImage("complexStaves.png", "complexStavesOut.png");
				// testImage("baaBaa.jpg", "baaBaaOut.png");
				// testImage("baaBaaSection.jpg", "baaBaaSectionOut.png");
				testImage("Distorted.jpg", "distortedOut.jpg");
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

	private Mat testProcessing(Mat sheet) {
		Mat mat = sheet.clone();
		Utils.preprocessImage(sheet);
		Mat projection = sheet.clone();
		Mat proj = Utils.horizontalProjection(projection);
		LinkedList<Integer> divisions = Utils.detectDivisions(proj, 190);
		List<Mat> result = Utils.cut(sheet, divisions);
		List<Mat> actualResult = new LinkedList<Mat>();
		Map<Mat, List<Line>> map = new HashMap<Mat, List<Line>>();
		for (Mat m : result) {
			Mat clone = m.clone();
			Utils.invertColors(m);
			Mat lines = new Mat();
			Imgproc.HoughLinesP(m, lines, 1, Math.PI / 180, 100);
			Imgproc.cvtColor(clone, clone, Imgproc.COLOR_GRAY2BGR);
			map.add
			actualResult.add(clone);
		}
		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2BGR);
		Utils.rebuildMatrix(actualResult, mat, divisions);
		return mat;
	}

	public void testImage(String src, String dst) {
		String srcPath = Utils.getPath("input/" + src);
		Mat houghMat = Utils.readImage(srcPath);
		String notePath = Utils.getPath("assets/notehead.png");
		DetectMusic.noteHead = Utils.readImage(notePath);
		// Mat imageMat = DetectMusic.detectMusic(houghMat);
		Mat imageMat = testProcessing(houghMat);
		Utils.writeImage(imageMat, Utils.getPath("output/" + dst));
		finish();
	}
}
