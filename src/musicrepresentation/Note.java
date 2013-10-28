package musicrepresentation;

public abstract class Note {
   private final Duration duration;
   private final int dots;
   private final int velocity;

   public Note(Duration duration, int dots, int velocity){
        this.duration = duration;
        this.dots = dots;
        this.velocity=velocity;
   }

    public int getDuration(){
        int val = value(duration);
        for(int i = dots; i != 0; --i){
            val *= 1.5;
        }
        return val;
    }
    
    public void play() {
    	//TODO: implement the method depending on the way it's gonna be called.
    	// Do that for every note so the playback in the end is just 
    	// For (Note n : allNotes) n.play();
    }

    private int value(Duration duration){
        int value = 32 * 512; //smallest possible division of a note is 1/512, ain't nobody got time for fractions...
        int ord = duration.ordinal();
        while(ord > 0){
            value /= 2;
            ord--;
        }
        return value;
    }

    public int getPitch(){
        return 0;
    }
    
    public int getVelocity(){
        return velocity;
    }

}

