package musicrepresentation;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.Iterable;

public class Chord implements Iterable<AbstractNote>{
    private final List<AbstractNote> notes;

    public Chord(List<AbstractNote> notes){
        this.notes = notes;
    }

    public Chord(AbstractNote note){
        this(new ArrayList<AbstractNote>());
        addNote(note);
    }

    public Chord(){
        this(new ArrayList<AbstractNote>());
    }

    public void addNote(AbstractNote note){
        notes.add(note);
    }

    public int shortestNote(){
        int shortest = notes.get(0).getDuration();
        for(AbstractNote note: notes){
            int tmp = note.getDuration();
            shortest = tmp < shortest ? tmp : shortest;
        }
        return shortest;
    }

    public Iterator<AbstractNote> iterator(){
        return notes.iterator();
    }
}
