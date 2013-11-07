package org.sightreading;

import java.io.File;

import midiconversion.Converter;
import musicdetection.MusicDetector;
import musicrepresentation.Piece;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import playback.Playback;
import utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leff.midi.MidiFile;

public class SightReadingActivity extends Activity {
	public static final String TAG = "SightReadingActivity";
	public static EditText currentFileName;
	private Button scan;
	public final static long startTime = System.currentTimeMillis();
	private View view;

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

		(new File(Utils.getPath("") + File.separator + "input")).mkdirs();
		(new File(Utils.getPath("") + File.separator + "output")).mkdirs();
		(new File(Utils.getPath("") + File.separator + "assets")).mkdirs();
	}

	private void initialiseButtons() {
		scan = (Button) findViewById(R.id.scan);
		scan.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(SightReadingActivity.this, SRCameraActivity.class);
		        startActivity(i);
			}
		});
	}

	private void testImage(String src, String dstImage, String destMid) {
		String srcPath = Utils.getPath("input/" + src);
		Mat input = Utils.readImage(srcPath);
		Mat scaledInput = Utils.resizeImage(input, Utils.STANDARD_IMAGE_WIDTH);

		Mat output = scaledInput.clone();
		Imgproc.cvtColor(output, output, Imgproc.COLOR_GRAY2BGR);

		MusicDetector detector = new MusicDetector(scaledInput);
		detector.detect();
		detector.print(output);

		Piece piece = detector.toPiece();
		MidiFile f = Converter.Convert(piece);

		Playback.saveMidiFile(f, destMid);
		// Playback.playMidiFile("test.mid");

		Utils.writeImage(output, Utils.getPath("output/" + dstImage));
		finish();
	}
}
