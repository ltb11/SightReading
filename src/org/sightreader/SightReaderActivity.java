package org.sightreader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.lamerman.SelectionMode;
import com.leff.midi.MidiFile;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import midiconversion.Converter;
import musicdetection.MusicDetector;
import musicdetection.NoMusicDetectedException;
import musicrepresentation.Piece;
import playback.Playback;
import utils.OurUtils;

public class SightReaderActivity extends Activity {
	public static final String TAG = "SightReaderActivity";
    private Button scan;
	private Button play;
    private static final int CAMERA_REQUEST = 1888;
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
		(new File(OurUtils.getPath("") + File.separator + "midi")).mkdirs();
		(new File(OurUtils.getPath("") + File.separator + "output")).mkdirs();
		(new File(OurUtils.getPath("") + File.separator + "assets")).mkdirs();
	}

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            Intent i = new Intent(SightReaderActivity.this,
                    ProcessingActivity.class);
            startActivity(i);
        }
    }

	private void initialiseButtons() {
		// Set up the button which takes you to the camera
		scan = (Button) findViewById(R.id.scan);
		scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = new File(OurUtils.getPath("temp/tmp.png"));
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
		});

		// Set up the button which takes you to playback
		play = (Button) findViewById(R.id.play);
		play.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SightReaderActivity.this,
						FileDialogActivity.class);
				// set user not able to select directories
				intent.putExtra(FileDialogActivity.CAN_SELECT_DIR, false);
				// set user not able to create files
				intent.putExtra(FileDialogActivity.SELECTION_MODE,
						SelectionMode.MODE_OPEN);
				// restrict file types visible
				intent.putExtra(FileDialogActivity.FORMAT_FILTER,
						new String[] { "midi" });
				// set default directory for dialog
				intent.putExtra(FileDialogActivity.START_PATH, OurUtils.getPath("midi/"));
				startActivityForResult(intent, 0);
			}
		});

		// Set up button to test parsing
		findViewById(R.id.parse).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String toTest = "INPUT.png";
				String midi = "baaBaa.midi";
				testImage(toTest, OurUtils.getDestImage(toTest), midi);
				//finish();
			}
		});

	}

	/**
	 * Prints debug information on the given image, and saves a MIDI file of the
	 * piece
	 **/
	private void testImage(String src, String dstImage, String destMid) {
		String srcPath = OurUtils.getPath("input/" + src);
		Mat input = OurUtils.readImage(srcPath);
		Log.d("Guillaume",
				"Original image width before scaling: " + input.cols());
		Mat output = input.clone();
		Imgproc.cvtColor(output, output, Imgproc.COLOR_GRAY2BGR);

		MusicDetector detector = null;
		try {
			detector = new MusicDetector(input,getApplicationContext());
			detector.detect();
			output = detector.print();
			OurUtils.writeImage(output, OurUtils.getPath("output/" + dstImage));

		} catch (NoMusicDetectedException e) {
			Log.d("Guillaume", "No music detected here!");
		}
		try {
			Piece piece = detector.toPiece();
			MidiFile f = Converter.Convert(piece);
			Playback.saveMidiFile(f, destMid);

			Playback.playMidiFile("baaBaa.midi");
		} catch (Exception e) {
			Log.d("Guillaume", "It crashed");
		}

	}

}
