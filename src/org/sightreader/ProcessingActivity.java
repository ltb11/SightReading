package org.sightreader;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import musicdetection.MusicDetector;
import musicdetection.NoMusicDetectedException;
import musicrepresentation.Piece;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import utils.OurUtils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class ProcessingActivity extends Activity {

	public static final String TAG = "SRCameraActivity";
	public static EditText currentFileName;
	public final static long startTime = System.currentTimeMillis();

	private static int imageNum = 0;
	//private static int totalPages;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onProcCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_processing_view);
		
		initialiseButtons();
		
		run();
	}
	
	private void run() {
		List<Piece> pieces = new LinkedList<Piece>();
		
		while (nextImageExists()) {
			Log.i("PROC","image proc");
			
			try {
				Mat input = loadImage();
				MusicDetector detector = new MusicDetector(input);
				detector.detect();
				Piece piece = detector.toPiece();
				pieces.add(piece);
				
			} catch (NoMusicDetectedException e) {
				e.printStackTrace();
				
			} catch (FileNotFoundException e) {
				Log.e("PROC","page "+imageNum+" is missing");
			}
			
			imageNum++;
		}
		
		Piece finalPiece = concatPieces(pieces);
	}
	
	private Mat loadImage() throws FileNotFoundException {
		Bitmap bitmap = OurUtils.loadTempImage(imageNum);
		Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), 0);
		Utils.bitmapToMat(bitmap, mat);
		return mat;
	}
	
	private boolean nextImageExists() {
		return imageNum < CameraActivity.totalImagesCaptured();
	}

	private Piece concatPieces(List<Piece> pieces) {
		return null;
	}
	
	private void initialiseButtons() {

	}
	
}
