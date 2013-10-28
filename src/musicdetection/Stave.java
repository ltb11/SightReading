package musicdetection;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import musicrepresentation.AbstractNote;
import musicrepresentation.Bar;
import musicrepresentation.Chord;
import musicrepresentation.NoteName;
import musicrepresentation.PlayedNote;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.util.Log;
import utils.Utils;

public class Stave {

	private List<StaveLine> lines;
	private double staveGap;
	private Map<Point, Clef> clefs;
	private Point originalClef;
	
	private List<Note> notes;
	
	public Stave(List<StaveLine> lines) {
		this.lines = lines;
		if (lines.size() != 5)
			throw new RuntimeException("Stave must have 5 lines!");
		staveGap = (lines.get(4).toLine().start().y - lines.get(0).toLine().start().y) / 4; 
		clefs = new HashMap<Point, Clef>();
		notes = new LinkedList<Note>();
		originalClef = null;
	}
	
	public void eraseFromMat(Mat image) {
		throw new UnsupportedOperationException("obsolete");
		/*Scalar c1 = new Scalar(255, 0, 0);
		Scalar c2 = new Scalar(0, 255, 0);
		Scalar c3 = new Scalar(0, 0, 255);
		Scalar c4 = new Scalar(255, 0, 255);
		Scalar c5 = new Scalar(255, 255, 0);
		Scalar[] cs = new Scalar[] { c1, c2, c3, c4, c5};
		Scalar col = new Scalar(0,0,0);
		
		for (int i = 0; i < 5; i++) {
			Core.line(image, lines.get(i).start(), lines.get(i).end(), col, 1);
		}*/
	}
	
	public void addClef(Clef c, Point p) {
		clefs.put(p, c);
		if (originalClef == null)
			originalClef = p;
	}
	
	public Point originalClef() {
		return originalClef;
	}
	
	public void draw(Mat image) {
		Scalar col = new Scalar(128,0,0);
		
		for (int i = 0; i < 5; i++) {
			Core.line(image, lines.get(i).toLine().start(), lines.get(i).toLine().end(), col, 3);
		}
	}
	
	public void drawDetailed(Mat image) {
		Scalar col = new Scalar(128,0,0);
		
		for (int i = 0; i < 5; i++) {
			for(Line l : lines.get(i).getLines()) {
				Core.line(image, l.start(), l.end(), col, 3);
			}
		}
	}
	
	public double staveGap() {
		return staveGap;
	}

	public Line topLine() {
		return lines.get(0).toLine();
	}

	public Line bottomLine() {
		return lines.get(4).toLine();
	}

	public Clef getClefAtPos(Point p) {
		Clef result = clefs.get(originalClef);
		double lastClef = originalClef.x;
		for (Point point : clefs.keySet()) {
			if (point.x > lastClef && point.x < p.x) {
				result = clefs.get(point);
				lastClef = point.x;
			}
		}
		return result;
	}

	public void addNote(Note n) {
		notes.add(n);
	}

	public void orderNotes() {
		Collections.sort(notes, new Comparator<Note>() {

			@Override
			public int compare(Note lhs, Note rhs) {
				return (int) (lhs.center().x - rhs.center().x);
			}
			
		});
	}

	public void calculateNotePitch() {
		double sy = topLine().start().y;
		for (Note n : notes) {
			double ny = (n.center().y);
			// 0-1 where 0 is top line, 1 is bottom
			double pos = (ny-sy)/(staveGap*4);
			int line = (int) Math.round(8 - pos*4);
			NoteName name = Utils.getName(clefs.get(originalClef),line);
			n.setName(name);
			n.setOctave(Utils.getOctave(clefs.get(originalClef), line));
			Log.v("Guillaume", n.toString());
		}
	}

	private int lineToNote(double line) {
		// TODO Auto-generated method stub
		return ((int)(line*2))/2;
	}

	public List<Bar> toBars() {
		List<Bar> bars = new LinkedList<Bar>();
		Bar currentBar = new Bar();
		bars.add(currentBar);
		
		int duration = 0;
		List<PlayedNote> notes = createPlayedNotes();
		for(PlayedNote n : notes) {
			
			Log.i("NOTE",n.toString());
			
			currentBar.addNote(n);
			
			duration += n.getDuration();
			if (duration>=AbstractNote.TEMP_44LENGTH) {
				duration=0;
				currentBar = new Bar();
				bars.add(currentBar);
			}
		}
		
		return bars;
	}

	private List<PlayedNote> createPlayedNotes() {
		List<PlayedNote> playedNotes = new LinkedList<PlayedNote>();
		for (Note n : notes) {
			playedNotes.add(n.toPlayedNote());
		}
		return playedNotes;
	}

}
