package musicrepresentation;

import java.util.Iterator;
import java.util.List;

import musicdetection.Time;

public class Piece implements Iterable<Bar> {
 
    private final String title;
    private final Enum<Time> time_signature;
    private final List<Bar> bars;
    private int bpm;
    
    public Piece(List<Bar> bars){
        this.bars = bars;
        this.bpm = 120;
        this.title = "FantasieImpromptu";
        this.time_signature = Time.FourFour;
    }

    public String getTitle(){
        return this.title;
    }

    public void addBar(Bar bar){
        bars.add(bar);
    }
    public int getBpm(){
        return this.bpm;
    }

    public Iterator<Bar> iterator(){
        return bars.iterator(); 
    }
}
