package midiconversion;

import java.util.ArrayList;

import musicrepresentation.AbstractNote;
import musicrepresentation.Bar;
import musicrepresentation.Chord;
import musicrepresentation.Piece;

import android.util.Log;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;

public class Converter{

    public static MidiFile Convert(Piece piece){
        MidiTrack tempoTrack = new MidiTrack();
        MidiTrack noteTrack  = new MidiTrack();
        int PPQ = 960;

        TimeSignature ts = new TimeSignature();
        Tempo t = new Tempo();
        int bpm = piece.getBpm();
        t.setBpm(bpm);

        tempoTrack.insertEvent(ts);
        tempoTrack.insertEvent(t);
          
        int nextNote = 0;
        for(Bar bar : piece){
            for(Chord chord: bar){
                int channel = 0; 
                int length = 0;
                for(AbstractNote note: chord){
                    double duration = (double) note.getDuration();
                    duration /= AbstractNote.CROTCHET_DURATION ;
                    Log.d("Conrad","duration: " +duration); 

                    length = (int) (duration*PPQ/2);
                    noteTrack.insertNote(channel,note.getPitch(),note.getVelocity(), nextNote,length);
                    channel++; 
                }
                nextNote += length;
                //chord functionality goes back above
            }
        }
        
        ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
        tracks.add(tempoTrack);
        tracks.add(noteTrack);
    
        MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
        return midi;
    }
}
