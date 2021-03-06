package playback;

import android.media.MediaPlayer;
import android.util.Log;

import com.leff.midi.MidiFile;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import utils.OurUtils;
import android.media.MediaPlayer;
import android.util.Log;


public class Playback {

	public static void playMidiFile(String fileName) {
		playMidiFile("midi/", fileName);
	}

	public static MediaPlayer getMidiFile(String filePath) {
		File f = new File(filePath);

		try {
			FileInputStream fis = new FileInputStream(f);
			FileDescriptor fd = fis.getFD();

			MediaPlayer mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(fd);
			mediaPlayer.prepare();

			fis.close();
			return mediaPlayer;
		} catch (Exception e) {
			Log.e("Playback", e.toString());
			return null;
		}

	}

	public static void playMidiFile(String folder, String fileName) {
		MediaPlayer player = getMidiFile(OurUtils.getPath(folder + fileName));
		player.start();
		Log.i("Playback", "Playback started: " + fileName);

	}

	public static void saveMidiFile(MidiFile midi, String fileName) {
		saveMidiFile(midi, "midi/", fileName);

	}

	public static void saveMidiFile(MidiFile midi, String folder,
			String fileName) {
		String path = OurUtils.getPath(folder);
		File output = new File(path, fileName);
		try {
			midi.writeToFile(output);
		} catch (IOException e) {
			Log.e("Playback", e.getMessage(), e);
		}

	}
}
