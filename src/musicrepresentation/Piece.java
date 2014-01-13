package musicrepresentation;

import java.util.Iterator;
import java.util.List;

import musicdetection.Time;

public class Piece implements Iterable<Bar> {
 
    private final String title;
    private final Enum<Time> time_signature;
    private final List<Bar> bars;
    private int bpm;
    
    public Piece(List<Bar> bars, int bpm, String title, Time time){
        this.bars = bars;
        this.bpm = bpm;
        this.title = title;
        this.time_signature = time;
    }
    public Piece(List<Bar> bars){
        this(bars,120,"FantasieImpromptu",Time.FourFour);
    }
    public Piece(){
        this(new ArrayList<Bar>()); 
    }


    public String getTitle(){
        return this.title;
    }
    
    public void addNote(){
    }


    public int getBpm(){
        return this.bpm;
    }

    public Iterator<Bar> iterator(){
        return bars.iterator(); 
    }
}
