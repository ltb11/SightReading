package musicrepresentation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import musicdetection.Time;

public class Bar implements Iterable<Chord>{

    private final List<Chord> chords;
    private final Enum<Time> time_signature;
    private int fullness;
    private boolean full;

    public Bar(Time time){
        this.chords = new ArrayList<Chord>();
        this.time_signature = time;
        this.fullness = 0;
    }

    public Bar(){
        this.chords = new ArrayList<Chord>();
        this.time_signature = Time.FourFour;
        this.fullness = 0;
    }

    public void addChord(Chord chord){
        chords.add(chord);
        fullness+=chord.shortestNote();
    }
    
    public void addNote(AbstractNote note) {
    	Chord chord = new Chord(note);
    	addChord(chord);
    }

    public boolean isFull(){
        return fullness >= AbstractNote.TEMP_44LENGTH;
    }

    public Iterator<Chord> iterator(){
        return chords.iterator();
    }
}
