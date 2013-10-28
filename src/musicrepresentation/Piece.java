package musicrepresentation;

import java.util.List;
import java.util.ArrayList;
import java.lang.Iterable;
import java.util.Iterator;

public class Piece implements Iterable<Bar> {
 
    private final String title;
    private final List<Bar> bars;
    private int bpm;
    
    public Piece(){
        this.bars = new ArrayList<Bar>();
        this.bpm = 60;
        this.title = "FantasieImpromptu";
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
