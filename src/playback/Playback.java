package playback;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;

import utils.OurUtils;
import android.media.MediaPlayer;
import android.util.Log;

public class Playback {

	public static void playMidiFile(String fileName) {
		playMidiFile("midi/",fileName);
	}

	public static void playMidiFile(String folder, String fileName) {
		String path = OurUtils.getPath(folder);
		File f = new File(path, fileName);

		try {
			FileInputStream fis = new FileInputStream(f);
			FileDescriptor fd = fis.getFD();

			MediaPlayer mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(fd);
			mediaPlayer.prepare();
			mediaPlayer.start();
			Log.i("Playback", "Playback started: " + fileName);
			//fis.close();
		} catch (Exception e) {
			Log.e("Playback", e.toString());
		}

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

	public static void test() {
		MidiFile midi = CreateTestMidi();
		saveMidiFile(midi, "test.mid");
		playMidiFile("test.mid");
	}

	private static MidiFile CreateTestMidi() {
		MidiTrack tempoTrack = new MidiTrack();
		MidiTrack noteTrack = new MidiTrack();

		// 2. Add events to the tracks
		// 2a. Track 0 is typically the tempo map

		Tempo t = new Tempo();
		t.setBpm(228);

		tempoTrack.insertEvent(t);

		// 2b. Track 1 will have some notes in it
		int time = 0;
		int spacing = 300;
		int duration = 150;
		for (int i = 40; i < 100; i++) {

			int channel = 0, pitch = i, velocity = 100;
			NoteOn on = new NoteOn(time, channel, pitch, velocity);
			NoteOff off = new NoteOff(time + duration + 120, channel, pitch, 0);

			noteTrack.insertEvent(on);
			noteTrack.insertEvent(off);

			time += spacing;
		}

		// It's best not to manually insert EndOfTrack events; MidiTrack will
		// call closeTrack() on itself before writing itself to a file

		// 3. Create a MidiFile with the tracks we created
		ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
		tracks.add(tempoTrack);
		tracks.add(noteTrack);

		MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
		return midi;
	}
}
