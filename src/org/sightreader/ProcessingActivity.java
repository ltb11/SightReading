package org.sightreader;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import midiconversion.Converter;
import musicdetection.MusicDetector;
import musicdetection.NoMusicDetectedException;
import musicrepresentation.Piece;

import org.opencv.core.Mat;

import playback.Playback;
import utils.OurUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.leff.midi.MidiFile;

public class ProcessingActivity extends Activity {

	public static final String TAG = "SRCameraActivity";
	public static EditText currentFileName;
	public final static long startTime = System.currentTimeMillis();

	private AlertDialog noMusic;
	private AlertDialog noFile;
	private AlertDialog noFinal;
	private AlertDialog unknown;
	private static int imageNum = 0;
	private static int pageNum;

	public static void SetPageNum(int pageNum) {
		ProcessingActivity.pageNum = pageNum;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onProcCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_processing_view);

		initialiseButtons();
		initialiseAlerts();
		// noMusic.show();

		new ProcessingTask().execute("");

	}

	private void initialiseAlerts() {
		noMusic = new AlertDialog.Builder(ProcessingActivity.this)
				.setTitle("No music was detected")
				.setMessage("Please try taking a clearer photo")
				.setPositiveButton("Back",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								ProcessingActivity.super.onBackPressed();
							}
						}).create();

		noFile = new AlertDialog.Builder(ProcessingActivity.this)
				.setTitle("File Not found exeption")
				.setMessage("Please try again")
				.setPositiveButton("Back",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								ProcessingActivity.super.onBackPressed();
							}
						}).create();

		noFinal = new AlertDialog.Builder(ProcessingActivity.this)
				.setTitle("Final peice was null")
				.setMessage("Please try again")
				.setPositiveButton("Back",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								ProcessingActivity.super.onBackPressed();
							}
						}).create();

		unknown = new AlertDialog.Builder(ProcessingActivity.this)
				.setTitle("Unknown Exeption")
				.setMessage("Please try again")
				.setPositiveButton("Back",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								ProcessingActivity.super.onBackPressed();
							}
						}).create();

	}

	private void initialiseButtons() {

	}

	private class ProcessingTask extends AsyncTask<String, String, String> {

		private boolean success = true;
		private boolean noMusicDet = false;
		private boolean fileNF = false;
		private boolean finalPieceNull = false;

		@Override
		protected String doInBackground(String... arg0) {
			List<Piece> pieces = new LinkedList<Piece>();
			Log.i("PROC", "start processing " + pageNum + " pages");

			while (nextImageExists()) {
				Log.i("PROC", "next image");

				try {
					Mat input = loadImage();
					// OurUtils.writeImage(input,OurUtils.getPath("output/")+"test.png");
					if (input == null) {
						Log.e("PROC", "image loaded but null");
					}

					MusicDetector detector = new MusicDetector(input,getApplicationContext());
					detector.detect();
					
					// TODO:
					Mat output = detector.print();
					OurUtils.writeImage(output, OurUtils.getPath("output/done.png")); 
					
					Piece piece = detector.toPiece();
					pieces.add(piece);

				} catch (FileNotFoundException e) {
					Log.e("PROC", "page " + imageNum + " is missing");
					success = false;
					fileNF = true;
					return "Failed";

				} catch (NoMusicDetectedException e) {
					e.printStackTrace();
					success = false;
					noMusicDet = true;
					return "Failed";
				}

				imageNum++;
			}

			Piece finalPiece = concatPieces(pieces);

			if (finalPiece == null) {
				success = false;
				finalPieceNull = true;
				return "Failed";
			}

			MidiFile midi = Converter.Convert(finalPiece);
			Playback.saveMidiFile(midi, "temp/", "output.midi");

			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {
			if (!success)
				if (noMusicDet) {
					noMusic.show();
				} else if (fileNF) {
					noFile.show();
				} else if (finalPieceNull) {
					noFinal.show();
				} else {
					unknown.show();
				}
			else {
				Intent i = new Intent(ProcessingActivity.this,
						PlaybackActivity.class);
				startActivity(i);
			}
		}

		private Mat loadImage() throws FileNotFoundException {
			Mat mat = OurUtils.loadTempMat(imageNum);
			return mat;
		}

		private boolean nextImageExists() {
			return imageNum < pageNum;
		}

		private Piece concatPieces(List<Piece> pieces) {
			if (pieces.isEmpty())
				return null;
			return pieces.get(0);
		}

	}
}
