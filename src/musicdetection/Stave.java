package musicdetection;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import musicrepresentation.AbstractNote;
import musicrepresentation.Bar;
import musicrepresentation.NoteName;
import musicrepresentation.PlayedNote;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Scalar;

import utils.OurUtils;
import android.util.Log;

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
	
	public List<Note> notes() {
		return notes;
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
	
	public double startYRange() {
		return topLine().start().y - 4*staveGap;
	}

	public Range yRange() {
		return new Range((int) (topLine().start().y - 4 * staveGap),
				(int) (bottomLine().start().y + 4 * staveGap));
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
		for (Note n : notes) {
			double nx = (n.center().x);
			double ny = (n.center().y);
			double y1 = getY(lines.get(0),nx);
			double y2 = getY(lines.get(4),nx);
			double gap = y2-y1;
			
			// 0-1 where 0 is top line, 1 is bottom
			double pos = (ny-y1)/gap;
			int line = (int) Math.round(8 - pos*8);
			NoteName name = OurUtils.getName(clefs.get(originalClef),line);
			n.setName(name);
			n.setOctave(OurUtils.getOctave(clefs.get(originalClef), line));
		}
	}

	private double getY(StaveLine line, double x) {
		for(Line l : line.getLines()) {
			if (l.end().x>x) {
				return l.start().y;
			}
		}
		return 0;
	}
	
	public double staveGapAtPos(Point center) {
		double nx = (center.x);
		double y1 = getY(lines.get(0),nx);
		double y2 = getY(lines.get(4),nx);
		return (y2 - y1) / 4;
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

	public double getTopYAtPos(Point pos) {
		return getY(lines.get(0), pos.x);
	}

}
