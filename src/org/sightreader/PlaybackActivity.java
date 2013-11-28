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
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class PlaybackActivity extends Activity {

	public static final String TAG = "SRPlaybackActivity";
	public final static long startTime = System.currentTimeMillis();
	private MediaPlayer player;
	private boolean playing = false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "playback creating");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_playback_view);
		
		initialiseButtons();
		
		player = Playback.getMidiFile("temp/", "output.midi");
	}

	private void initialiseButtons() {
		findViewById(R.id.playbackButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (playing) {
					playing=false;
					player.stop();
					findViewById(R.id.playbackButton).setBackgroundResource(R.drawable.media_play);
				} else {
					playing=true;
					player.start();
					findViewById(R.id.playbackButton).setBackgroundResource(R.drawable.media_pause);
				}
			}
		});
		
		findViewById(R.id.playback_resetApp).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(PlaybackActivity.this,
						SightReaderActivity.class);
				startActivity(i);
			}
		});
	}
}
