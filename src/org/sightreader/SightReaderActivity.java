package org.sightreader;

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
import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;

public class SightReaderActivity extends Activity {
	public static final String TAG = "SightReaderActivity";
	public static EditText currentFileName;
	private Button scan;
	private Button play;
	public final static long startTime = System.currentTimeMillis();

	// load the OpenCV library
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

	public SightReaderActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_surface_view);

		initialiseButtons();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this,
				mOpenCVCallBack)) {
			Log.e("TEST", "Cannot connect to OpenCV Manager");
		}

		// Make sure the necessary folders exist
		(new File(OurUtils.getPath("") + File.separator + "input")).mkdirs();
		(new File(OurUtils.getPath("") + File.separator + "output")).mkdirs();
		(new File(OurUtils.getPath("") + File.separator + "assets")).mkdirs();
	}

	private void initialiseButtons() {
		// Set up the button which takes you to the camera
		scan = (Button) findViewById(R.id.scan);
		scan.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(SightReaderActivity.this,
						CameraActivity.class);
				startActivity(i);
			}
		});

		// Set up the button which takes you to playback
		play = (Button) findViewById(R.id.play);
		play.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// to get the file dialogue library use the below command:
				// svn checkout
				// http://android-file-dialog.googlecode.com/svn/trunk/
				// android-file-dialog-read-only
				// then import the project to eclipse and add the project to the
				// android library
				Intent intent = new Intent(SightReaderActivity.this,
						FileDialogActivity.class);
				// maybe context should be getBaseContext()?
				intent.putExtra(FileDialog.START_PATH, OurUtils.getPath(""));

				// set user not able to select directories
				intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
				// set user not able to create files
				intent.putExtra(FileDialog.SELECTION_MODE,
						SelectionMode.MODE_OPEN);

				// restrict file types visible
				intent.putExtra(FileDialog.FORMAT_FILTER,
						new String[] { "sr" });

				startActivityForResult(intent, 0);

			}
		});

		// Set up button to test parsing
		findViewById(R.id.parse).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String toTest = "closeYourEyes.jpg";
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

		// TODO: there is a button in the view that is not set up here!
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
