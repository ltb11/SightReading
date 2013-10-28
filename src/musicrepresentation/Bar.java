package musicrepresentation;

import java.lang.Iterable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class Bar implements Iterable<Chord>{

    private final List<Chord> chords;

    public Bar(){
        this.chords = new ArrayList<Chord>();
    }

    public void addChord(Chord chord){
        chords.add(chord);
    }
    
    public void addNote(AbstractNote note) {
    	Chord chord = new Chord(note);
    	addChord(chord);
    }

    public void addNote(AbstractNote note){
        Chord chord = new Chord(note);
        addChord(chord);
    }

    public Iterator<Chord> iterator(){
        return chords.iterator();
    }
}
