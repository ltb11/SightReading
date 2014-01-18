package org.sightreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.lamerman.SelectionMode;
import com.leff.midi.MidiFile;

import playback.Playback;
import utils.OurUtils;

public class PlaybackActivity extends Activity {

	public static final String TAG = "SRPlaybackActivity";

	public static final String FILE_PATH = "FILEPATH";
    public static final String PROCESSING_FLAG = "FROMPROCESS";
    private static final int FILE_DIALOG_REQUEST = 1066;

    private String pathToSave;
    private String imagePath;
	private String filePath;
    
	private MediaPlayer player;
    private MidiFile midi;
    private Bitmap bmp;
	private Handler mHandler;
    private Runnable mRunnable;
    private boolean fromProcessing;

    private SeekBar seekBar;
    private ImageView sheetMusic;
	private Button save;
    private Button changeTrack;
	private Button discard;
	private Button playbackButton;

    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "playback creating");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_playback_view);

        Intent intent = getIntent();
        filePath = intent.getStringExtra(FILE_PATH);
        String fromProcess = intent.getStringExtra(PROCESSING_FLAG);
        fromProcessing = fromProcess != null && fromProcess.equals("true");
        player = Playback.getMidiFile(filePath);

		// String path = OurUtils.getPath(folderName);
		// midiFile = new File(path, midiFileName);
		
        initialiseButtons();
        initialiseSeekBar();
        loadSheetMusic();
	}
    
    private void initialiseSeekBar(){
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
            filePath = data.getStringExtra(FileDialogActivity.RESULT_PATH);
            loadSheetMusic();
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
        reset = (Button) findViewById(R.id.playback_resetApp);
		reset.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
                        pause();
                        player.seekTo(0);
                        seekBar.setProgress(0);
					}
				});

        save = (Button) findViewById(R.id.saveTrack);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(PlaybackActivity.this);
                alert.setTitle("Save");
                alert.setMessage("Filename: ");

                final EditText text = new EditText(PlaybackActivity.this);
                alert.setView(text);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pathToSave = text.getText().toString();
                        OurUtils.mv(OurUtils.getPath("assets/")+imagePath, OurUtils.getPath("assets/") + pathToSave + ".png");
                        OurUtils.mv(filePath, OurUtils.getPath("midi/") + pathToSave + ".midi");
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                alert.show();
            }
        });

		changeTrack = (Button) findViewById(R.id.changeTrack);
        changeTrack.setOnClickListener(
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

        if(fromProcessing){
            changeTrack.setVisibility(View.GONE);
        } else {
            save.setVisibility(View.GONE);
        }


	}

    private void loadSheetMusic() {
        sheetMusic = (ImageView) findViewById(R.id.sheetmusic);
        imagePath = filePath.substring(filePath.lastIndexOf("/")+1,filePath.lastIndexOf(".")) + ".png";
        bmp = BitmapFactory.decodeFile(OurUtils.getPath("assets/" + imagePath));
        sheetMusic.setImageBitmap(bmp);
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
