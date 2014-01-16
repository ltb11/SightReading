package org.sightreader;

import java.io.File;

import playback.Playback;
import utils.OurUtils;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PlaybackActivity extends Activity {

	public static final String TAG = "SRPlaybackActivity";
	public final static long startTime = System.currentTimeMillis();
	private MediaPlayer player;
	private boolean playing = false;
	private String folderName = "temp/";
	private String midiFileName = "output.midi";
	File midiFile;

	private Button accept;
	private Button discard;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "playback creating");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_playback_view);

		initialiseButtons();

		player = Playback.getMidiFile(folderName, midiFileName);
		String path = OurUtils.getPath(folderName);
		midiFile = new File(path, midiFileName);
	}

	private void initialiseButtons() {
		findViewById(R.id.playbackButton).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (playing) {
							playing = false;
							player.stop();
							findViewById(R.id.playbackButton)
									.setBackgroundResource(
											R.drawable.media_play);
						} else {
							playing = true;
							player.start();
							findViewById(R.id.playbackButton)
									.setBackgroundResource(
											R.drawable.media_pause);
						}
					}
				});

		findViewById(R.id.playback_resetApp).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
						// Intent i = new Intent(PlaybackActivity.this,
						// SightReaderActivity.class);
						// startActivity(i);
					}
				});

		accept = (Button) findViewById(R.id.buttonCameraKeepImage);
		accept.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String path = OurUtils.getPath(folderName);
				File save = new File(path, "new text");
				midiFile.renameTo(save);
			}
		});

		discard = (Button) findViewById(R.id.buttonCameraDiscardImage);
		discard.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				midiFile.delete();
			}
		});

	}
}
