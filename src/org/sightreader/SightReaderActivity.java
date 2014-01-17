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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;

import utils.OurUtils;

public class SightReaderActivity extends Activity {
	public static final String TAG = "SightReaderActivity";
    private Button scan;
	private Button play;
    private static final int CAMERA_REQUEST = 1888;
    private static final int FILE_DIALOG_REQUEST = 1066;
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
		if (requestCode ==  FILE_DIALOG_REQUEST && resultCode == RESULT_OK) {
			String filePath = data.getStringExtra(FileDialogActivity.RESULT_PATH);
			Intent intent = new Intent(SightReaderActivity.this, PlaybackActivity.class);
			intent.putExtra(PlaybackActivity.FILE_PATH, filePath);
			startActivity(intent);
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
				startActivityForResult(intent, FILE_DIALOG_REQUEST);
			}
		});

	}

}
