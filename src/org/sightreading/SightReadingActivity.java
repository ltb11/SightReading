package org.sightreading;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import musicdetection.DetectMusic;
import musicdetection.Line;
import musicdetection.Stave;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import utils.SheetStrip;
import utils.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

public class SightReadingActivity extends Activity {
	public static final String TAG = "SightReadingActivity";

	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				// testImage("twoStaves.png", "twoStavesOut.png");
				// testImage("threeStaves.png", "threeStavesOut.png");
				testImage("complexStaves.png", "complexStavesOut.png");
				testImage("baaBaa.jpg", "baaBaaOut.png");
				// testImage("baaBaaSection.jpg", "baaBaaSectionOut.png");
				// testImage("Distorted.jpg", "distortedOut.jpg");
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

		/*
		 * Button button = new Button(this); button.setWidth(100);
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

		finish();

	}

	private Mat testProcessing(Mat input) {
		Mat sheet = Utils.resizeImage(input, Utils.STANDARD_IMAGE_WIDTH);
		
		Mat output = sheet.clone();
		Imgproc.cvtColor(output, output, Imgproc.COLOR_GRAY2BGR);

		Utils.preprocessImage(sheet);
		Mat projection = sheet.clone();
		Mat proj = Utils.horizontalProjection(projection);
		// 190 threshold for white from 255 used to detect spaces between staves
		LinkedList<Integer> divisions = Utils.detectDivisions(proj, 190);
		List<SheetStrip> strips = Utils.SliceSheet(sheet, divisions);

		List<Line> lines = new LinkedList<Line>();
		for (SheetStrip strip : strips) {
			lines.addAll(strip.FindLines());
		}
		List<Stave> staves = DetectMusic.detectStaves(lines);
		for(Stave s : staves) {
			s.draw(output);
		}

		return output;

		/*
		 * List<Mat> actualResult = new LinkedList<Mat>(); Map<Mat, List<Line>>
		 * staveMap = new HashMap<Mat, List<Line>>(); for (Mat m : strips) { Mat
		 * clone = m.clone(); Utils.invertColors(m); Mat lines = new Mat();
		 * Imgproc.HoughLinesP(m, lines, 1, Math.PI / 180, 100);
		 * Imgproc.cvtColor(clone, clone, Imgproc.COLOR_GRAY2BGR);
		 * staveMap.put(clone, Utils.getHoughLinesFromMat(lines));
		 * actualResult.add(clone); }
		 */

	}

	public void testImage(String src, String dst) {
		String srcPath = Utils.getPath("input/" + src);
		Mat houghMat = Utils.readImage(srcPath);
		String notePath = Utils.getPath("assets/notehead.png");
		DetectMusic.noteHead = Utils.readImage(notePath);

		Mat imageMat = testProcessing(houghMat);
		Utils.writeImage(imageMat, Utils.getPath("output/" + dst));
		finish();
	}
}
