package org.sightreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import playback.Playback;
import utils.OurUtils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import android.os.Handler;

import com.lamerman.SelectionMode;

public class PlaybackActivity extends Activity {

	public static final String FILE_PATH = "FILEPATH";
	public static final String TAG = "SRPlaybackActivity";
	private MediaPlayer player;
    private SeekBar seekBar;
	private String filePath;
	private Handler mHandler;
    private Runnable mRunnable;
    public static final int FILE_DIALOG_REQUEST = 1066;

	private Button accept;
	private Button discard;
	private Button playbackButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "playback creating");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_playback_view);
        
        Intent intent = getIntent();
        filePath = intent.getStringExtra("FILEPATH");
		player = Playback.getMidiFile(filePath);
		
		// String path = OurUtils.getPath(folderName);
		// midiFile = new File(path, midiFileName);
		
        initialiseButtons();
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if(player != null){
                    int mCurrentPosition = player.getCurrentPosition();
                    seekBar.setProgress(mCurrentPosition);
                }
                mHandler.postDelayed(mRunnable,10);
            }
        };

	}

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode ==  FILE_DIALOG_REQUEST && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FileDialogActivity.RESULT_PATH);
            player.release();
            player = Playback.getMidiFile(filePath);
            seekBar.setMax(player.getDuration());
        }
    }
    @Override
	public void onDestroy() {
        player.stop();
        mHandler.removeCallbacks(mRunnable);
        player.release();
        super.onDestroy();
	}

	private void initialiseButtons() {
        playbackButton = (Button) findViewById(R.id.playbackButton);
		playbackButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (player.isPlaying()) {
                            pause();
						} else {
                            play();
						}
					}
				});
        ImageView imageView = (ImageView) findViewById(R.id.sheetmusic);
        Bitmap bmp = BitmapFactory.decodeFile(OurUtils.getPath("output/done.png"));
        imageView.setImageBitmap(bmp);

		findViewById(R.id.playback_resetApp).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
                        pause();
                        player.seekTo(0);
                        seekBar.setProgress(0);
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
		findViewById(R.id.changeTrack).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        seekBar.setProgress(0);
                        pause();
                        Intent intent = new Intent(PlaybackActivity.this, FileDialogActivity.class);
                        intent.putExtra(FileDialogActivity.CAN_SELECT_DIR, false);
                        intent.putExtra(FileDialogActivity.SELECTION_MODE, SelectionMode.MODE_OPEN);
                        intent.putExtra(FileDialogActivity.FORMAT_FILTER, new String[] { "midi" });
                        intent.putExtra(FileDialogActivity.START_PATH, OurUtils.getPath("midi/"));
                        startActivityForResult(intent, FILE_DIALOG_REQUEST);
                    }
                });



        seekBar = (SeekBar) findViewById(R.id.seekBar);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer song){
                seekBar.setMax(song.getDuration()); 
                mRunnable.run();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(player != null && b) player.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
	}

    private void play() {
        mHandler.post(mRunnable);
        player.start();
        playbackButton.setText(R.string.pause);
    }

    private void pause() {
        mHandler.removeCallbacks(mRunnable);
        player.pause();
        playbackButton.setText(R.string.play);
    }

}
