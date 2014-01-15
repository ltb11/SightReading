package musicrepresentation;

import android.util.Log;

public abstract class AbstractNote {
   private final Duration duration;
   private final int dots;
   private final int velocity;
   public static final int STANDARD_VELOCITY = 60;
   public static final int CROTCHET_DURATION = 512;

   public static final int TEMP_44LENGTH = 512*4;
   
   public AbstractNote(Duration duration, int dots, int velocity){
        this.duration = duration;
        this.dots = dots;
        this.velocity=velocity;
   }
   /**
    *  Returns a value of the duration in terms of multiples of hemidemisemi something quavers <br>
    *  1024 = minim <br>
    *  512 = crotchet <br>
    *  256 = quaver <br>
    *  128 = semi-quaver <br>
    */
   public int getDuration(){
        int val = value(duration);
        for(int i = dots; i != 0; --i){
            val *= 1.5;
        }
        return val;
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

