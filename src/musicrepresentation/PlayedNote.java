package musicrepresentation;

public class PlayedNote extends Note {
   private final NoteName name;
   private final Shift shift;
   private final int octave;

   public PlayedNote(NoteName name, int octave, Shift shift, Duration duration, int dots){
        super(duration,dots,false);
        this.name   = name;
        this.octave = octave;
        this.shift  = shift;
   }

    public Shift getShift(){
        return this.shift;
    }
    public NoteName getName(){
        return this.name;
    }
    public int getOctave(){
        return this.octave;
    }
}
