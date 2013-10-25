package musicrepresentation;

public abstract class Note {
   private final Duration duration;
   private final int dots;
   private final boolean rest;

   public Note(Duration duration, int dots, boolean rest){
        this.duration = duration;
        this.dots = dots;
        this.rest = rest;
   }

    public Duration getDuration(){
        return this.duration;
    }
    public int getDots(){
        return this.dots;
    }
    public boolean getRest(){
        return this.rest;
    }
}
