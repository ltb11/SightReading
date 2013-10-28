package midiconversion;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;

import musicrepresentation.Piece;
import musicrepresentation.Bar;
import musicrepresentation.Chord;
import musicrepresentation.Note;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Converter{

    public static MidiFile Convert(Piece piece){
        MidiTrack tempoTrack = new MidiTrack();
        MidiTrack noteTrack  = new MidiTrack();
    
        TimeSignature ts = new TimeSignature();
        Tempo t = new Tempo();
        int bpm = piece.getBpm();
        t.setBpm(bpm);

        tempoTrack.insertEvent(ts);
        tempoTrack.insertEvent(t);
        
          
        double crotchetLength = 60.0 / bpm;
        int nextNote = 0;
        for(Bar bar : piece){
            for(Chord chord: bar){
                int channel = 0; 
                for(Note note: chord){
                    double duration = note.getDuration();
                    duration /= Note.CROTCHET_DURATION;
                    double length = crotchetLength * duration;
                    noteTrack.insertNote(channel,note.getPitch(),note.getVelocity(), nextNote,length);
                    channel++; 
                }
                nextNote += chord.shortestNote();
            }
        }
        
        ArrayList tracks = new ArrayList<MidiTrack>();
        tracks.add(tempoTrack);
        tracks.add(noteTrack);
    
        MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
        return midi;
    }



    
}
