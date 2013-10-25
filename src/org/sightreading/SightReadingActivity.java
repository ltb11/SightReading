package org.sightreading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import musicdetection.DetectMusic;
import musicdetection.Line;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import utils.Utils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

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
		setContentView(R.layout.sight_reading_surface_view);

		initialiseButtons();

		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this,
				mOpenCVCallBack)) {
			Log.e("TEST", "Cannot connect to OpenCV Manager");
		}

		new File(Utils.getPath("") + File.separator + "input").mkdirs();
		new File(Utils.getPath("") + File.separator + "output").mkdirs();
		new File(Utils.getPath("") + File.separator + "assets").mkdirs();

	}

	private void initialiseButtons() {
		Button button = (Button) findViewById(R.id.scan);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				scanImage();
			}
		});
		
		ImageButton imageButton = (ImageButton) findViewById(R.id.camera);
		imageButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				takePicture();
			}
		});
		
		EditText currentFileName = (EditText) findViewById(R.id.filePath);
		currentFileName.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
		
	}
	
	private void takePicture() {	    
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 2);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 2) {
			Bitmap photo = (Bitmap) data.getExtras().get("data");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss", Locale.UK);
		    String date = dateFormat.format(new Date());
		    String photoFile = Utils.getPath("pictures/IMG_" + date + ".jpg");
			try {
				FileOutputStream out = new FileOutputStream(photoFile);
				photo.compress(Bitmap.CompressFormat.PNG, 0, out);
				ImageView currentView = ((ImageView) findViewById(R.id.currentImage));
				Bitmap bitmapPic = BitmapFactory.decodeFile(photoFile);
				currentView.setImageBitmap(bitmapPic);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		else
			super.onActivityResult(requestCode, resultCode, data);
	}

	private Mat testProcessing(Mat sheet) {
		Mat mat = sheet.clone();
		Utils.preprocessImage(sheet);
		Mat projection = sheet.clone();
		Mat proj = Utils.horizontalProjection(projection);
		LinkedList<Integer> divisions = Utils.detectDivisions(proj, 190);
		List<Mat> result = Utils.cut(sheet, divisions);
		List<Mat> actualResult = new LinkedList<Mat>();
		Map<Mat, List<Line>> staveMap = new HashMap<Mat, List<Line>>();
		for (Mat m : result) {
			Mat clone = m.clone();
			Utils.invertColors(m);
			Mat lines = new Mat();
			Imgproc.HoughLinesP(m, lines, 1, Math.PI / 180, 100);
			Imgproc.cvtColor(clone, clone, Imgproc.COLOR_GRAY2BGR);
			staveMap.put(clone, Utils.getHoughLinesFromMat(lines));
			actualResult.add(clone);
		}
		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2BGR);
		Utils.rebuildMatrix(actualResult, mat, divisions);
		return mat;
	}

	public void scanImage() {
		String src = ((EditText) findViewById(R.id.filePath)).getText().toString();
		testImage(src, Utils.getDest(src));
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
