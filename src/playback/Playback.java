package playback;

import android.media.MediaPlayer;
import android.util.Log;

import com.leff.midi.MidiFile;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import utils.OurUtils;

public class Playback {

	public static void playMidiFile(String fileName) {
		playMidiFile("midi/",fileName);
	}

	public static MediaPlayer getMidiFile(String folder, String fileName) {
		String path = OurUtils.getPath(folder);
		File f = new File(path, fileName);

		try {
			FileInputStream fis = new FileInputStream(f);
			FileDescriptor fd = fis.getFD();

			MediaPlayer mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(fd);
			mediaPlayer.prepare();
			return mediaPlayer;
			//fis.close();
		} catch (Exception e) {
			Log.e("Playback", e.toString());
			return null;
		}

	}
	
	public static void playMidiFile(String folder, String fileName) {
		MediaPlayer player = getMidiFile(folder,fileName);
		player.start();
		Log.i("Playback", "Playback started: " + fileName);

	}
	
	public static void saveMidiFile(MidiFile midi, String fileName) {
		saveMidiFile(midi, "midi/", fileName);

	}
	
	public static void saveMidiFile(MidiFile midi, String folder, String fileName) {
		String path = OurUtils.getPath(folder);
		File output = new File(path, fileName);
		try {
			midi.writeToFile(output);
		} catch (IOException e) {
			Log.e("Playback", e.getMessage(), e);
		}

	}
}
