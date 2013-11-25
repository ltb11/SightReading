package org.sightreader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import playback.Playback;
import utils.OurUtils;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class PlaybackActivity extends Activity {

	public static final String TAG = "SRPlaybackActivity";
	public final static long startTime = System.currentTimeMillis();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "playback creating");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_playback_view);
		
		/*String filePath = savedInstanceState.getString("FILEPATH");
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader bufferReader = new BufferedReader(new InputStreamReader(
				inputStream, Charset.forName("UTF-8")));
		String midiPath = null;
		try {
			midiPath = bufferReader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		Playback.playMidiFile("temp/", "output.midi");
	}
}
