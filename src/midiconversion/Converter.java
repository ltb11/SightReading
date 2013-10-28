package midiconversion;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;

import musicrepresentation.Piece;
import musicrepresentation.Bar;
import musicrepresentation.Chord;
import musicrepresentation.AbstractNote;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Converter{

    public static MidiFile Convert(Piece piece){
        MidiTrack tempoTrack = new MidiTrack();
        MidiTrack noteTrack  = new MidiTrack();
        int PPQ = 96;

        TimeSignature ts = new TimeSignature();
        Tempo t = new Tempo();
        int bpm = piece.getBpm();
        t.setBpm(bpm);

        tempoTrack.insertEvent(ts);
        tempoTrack.insertEvent(t);
          
        double crotchetLength = 60000.0 / (bpm * PPQ);
        int nextNote = crotchetLength;
        for(Bar bar : piece){
            for(Chord chord: bar){
                int channel = 0; 
                for(AbstractNote note: chord){
                    int duration = note.getDuration();
                    duration /= AbstractNote.CROTCHET_DURATION;
                    int length = (int) crotchetLength * duration;
                    noteTrack.insertNote(channel,note.getPitch(),note.getVelocity(), nextNote,length);
                    channel++; 
                }
                nextNote += chord.shortestNote();
            }
        }
        
        ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
        tracks.add(tempoTrack);
        tracks.add(noteTrack);
    
        MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
        return midi;
    }
}
