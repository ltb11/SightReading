package org.sightreading;

import java.io.File;

import musicdetection.MusicDetector;
import musicdetection.NoMusicDetectedException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import utils.OurUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class SightReadingActivity extends Activity {
	public static final String TAG = "SightReadingActivity";
	public static EditText currentFileName;
	private Button scan;
	public final static long startTime = System.currentTimeMillis();

	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {

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
		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this,
				mOpenCVCallBack)) {
			Log.e("TEST", "Cannot connect to OpenCV Manager");
		}

		(new File(OurUtils.getPath("") + File.separator + "input")).mkdirs();
		(new File(OurUtils.getPath("") + File.separator + "output")).mkdirs();
		(new File(OurUtils.getPath("") + File.separator + "assets")).mkdirs();
	}

	private void initialiseButtons() {
		scan = (Button) findViewById(R.id.scan);
		scan.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(SightReadingActivity.this,
						CameraActivity.class);
				startActivity(i);
			}
		});
		findViewById(R.id.parse).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String toTest = "Distorted.jpg";
				testImage(toTest, OurUtils.getDestImage(toTest),
						OurUtils.getDestMid(toTest));
				finish();
			}
		});
		
		findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				testProg();
			}
		});
	}

	private void testImage(String src, String dstImage, String destMid) {
		String srcPath = OurUtils.getPath("input/" + src);
		Mat input = OurUtils.readImage(srcPath);
		Mat scaledInput = OurUtils.resizeImage(input,
				OurUtils.STANDARD_IMAGE_WIDTH);

		Mat output = scaledInput.clone();
		Imgproc.cvtColor(output, output, Imgproc.COLOR_GRAY2BGR);

		MusicDetector detector = null;
		try {
			detector = new MusicDetector(scaledInput);
		} catch (NoMusicDetectedException e) {
			e.printStackTrace();
		}
		detector.detect();
		detector.print(output);

		// Piece piece = detector.toPiece();
		// MidiFile f = Converter.Convert(piece);

		// Playback.saveMidiFile(f, destMid);
		// Playback.playMidiFile("test.mid");

		OurUtils.writeImage(output, OurUtils.getPath("output/" + dstImage));
		//finish();
	}

	public void testProg() {
		String[] tests = new String[] { "Distorted.jpg", "Baabaa.jpg",
				"Baabaa 13-11-13 2.jpg" };
		for (String s : tests) {
			String dstImage = OurUtils.getDestImage(s);
			testImage(s, dstImage, OurUtils.getDestMid(s));
			Mat ref = OurUtils.readImage(OurUtils.getPath("ref/" + dstImage));
			//Imgproc.cvtColor(ref, ref, Imgproc.COLOR_GRAY2BGR);
			Mat toTest = OurUtils.readImage(OurUtils.getPath("output/" + dstImage));
			//Imgproc.cvtColor(toTest, toTest, Imgproc.COLOR_GRAY2BGR);
			/*for (int i = 0; i < ref.rows(); i++) {
				for (int j = 0; j < ref.cols(); j++) {
					for (int k = 0; k < 3; k++) {
						//Log.v("Guillaume", ref.get(i, j)[0] + "/" + toTest.get(i, j)[0]);
						assert (ref.get(i, j)[k] == toTest.get(i, j)[k]) : "Pixels not identical: "
								+ i + "," + j;
					}
				}
			}*/
			Mat dest = new Mat();
			Core.bitwise_xor(ref, toTest, dest);
			assert (Core.norm(dest, Core.NORM_INF) == 0) : "Pixels not identical";
			Log.v("Guillaume", s + " fully parsed");
		}
		finish();
	}

}
