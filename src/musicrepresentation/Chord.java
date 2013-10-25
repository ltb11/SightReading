package musicrepresentation;

import java.util.List;
import java.util.ArrayList;

public class Chord{
    private final List<Note> notes;

    public Chord(List<Note> notes){
        this.notes = notes;
    }

    public Chord(Note note){
        this(new ArrayList<Note>() {{ add(note) }});
    }

    public Chord(){
        this(new ArrayList<Note>());
    }

    public void addNote(Note note){
        notes.add(note);
    }
}
