package musicrepresentation;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.Iterable;

public class Chord implements Iterable<Note>{
    private final List<Note> notes;

    public Chord(List<Note> notes){
        this.notes = notes;
    }

    public Chord(Note note){
        this(new ArrayList<Note>());
        addNote(note);
    }

    public Chord(){
        this(new ArrayList<Note>());
    }

    public void addNote(Note note){
        notes.add(note);
    }

    public int shortestNote(){
        int shortest = notes.get(0).getDuration();
        for(Note note: notes){
            int tmp = note.getDuration();
            shortest = tmp < shortest ? tmp : shortest;
        }
        return shortest;
    }

    public Iterator<Note> iterator(){
        return notes.iterator();
    }
}
