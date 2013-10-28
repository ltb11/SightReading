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

    public static void Convert(Piece piece){
        MidiTrack tempoTrack = new MidiTrack();
        MidiTrack noteTrack  = new MidiTrack();
    
        TimeSignature ts = new TimeSignature();
        Tempo t = new Tempo();
        t.setBpm(piece.getBpm());

        tempoTrack.insertEvent(ts);
        tempoTrack.insertEvent(t);
        
          
        int nextNote = 0;
        for(Bar bar : piece){
            for(Chord chord: bar){
                int channel = 0; 
                for(Note note: chord){
                    int length = note.getDuration();
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
        File output = new File(piece.getTitle() + ".mid");
        try{
            midi.writeToFile(output);
        } catch(IOException e){
            System.err.println(e);
        }
    }



    
}
