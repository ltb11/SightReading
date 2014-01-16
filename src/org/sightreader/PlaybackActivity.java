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
import android.widget.SeekBar;

import android.os.Handler;

public class PlaybackActivity extends Activity {

	public static final String TAG = "SRPlaybackActivity";
	private MediaPlayer player;
    private SeekBar seekBar;
	private boolean playing = false;
	private String folderName = "temp/";
	private String midiFileName = "output.midi";
	File midiFile;
	private Handler mHandler;
        private Runnable mRunnable;

	// private Button accept;
	// private Button discard;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "playback creating");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_playback_view);


		player = Playback.getMidiFile(folderName, midiFileName);
		
		// String path = OurUtils.getPath(folderName);
		// midiFile = new File(path, midiFileName);
		
        initialiseButtons();
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if(player != null){
                    int mCurrentPosition = player.getCurrentPosition() /1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                mHandler.postDelayed(this,1000);
            }
        };
        mRunnable.run();
	}

	private void initialiseButtons() {
		findViewById(R.id.playbackButton).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (player.isPlaying()) {
							playing = false;
							player.pause();
                    ((Button) findViewById(R.id.playbackButton)).setText(R.string.play);
							findViewById(R.id.playbackButton)
									.setBackgroundResource(
											R.drawable.media_play);
						} else {
							playing = true;
							player.start();
                    ((Button)findViewById(R.id.playbackButton)).setText(R.string.pause);
									.setBackgroundResource(
											R.drawable.media_pause);
						}
					}
				});

		findViewById(R.id.playback_resetApp).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						player.pause();
						findViewById(R.id.playbackButton)
								.setBackgroundResource(R.drawable.media_play);
						player.seekTo(0);
						// finish();
					}
				});

		// accept = (Button) findViewById(R.id.buttonCameraKeepImage);
		// accept.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// String path = OurUtils.getPath(folderName);
		// File save = new File(path, "new text");
		// midiFile.renameTo(save);
		// }
		// });
		//
		// discard = (Button) findViewById(R.id.buttonCameraDiscardImage);
		// discard.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// midiFile.delete();
		// }
		// });

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(player.getDuration());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(player != null && b) player.seekTo(i * 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


	}
}
