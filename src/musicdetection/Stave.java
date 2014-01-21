package musicdetection;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Scalar;

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
import musicrepresentation.Shift;
import utils.OurUtils;

public class Stave {

	private List<StaveLine> lines;
	private double staveGap;
	private Map<Point, Clef> clefs;
	private Map<Point, Time> times;
	private Point originalClef;
	private Point startDetection;
	private Map<NoteName, Shift> keySignature;

	private List<Note> notes;

	public Stave(List<StaveLine> lines) {
		this.lines = lines;
		if (lines.size() != 5)
			throw new RuntimeException("Stave must have 5 lines!");
		staveGap = (lines.get(4).toLine().start().y - lines.get(0).toLine()
				.start().y) / 4;
		clefs = new HashMap<Point, Clef>();
		notes = new LinkedList<Note>();
		times = new HashMap<Point, Time>();
		keySignature = new HashMap<NoteName, Shift>();
		originalClef = null;
		startDetection = null;
	}

	public List<Note> notes() {
		return notes;
	}

	public void addClef(Clef c, Point p, int cols) {
		clefs.put(p, c);
		if (originalClef == null) {
			originalClef = p;
			if (startDetection == null)
				startDetection = new Point(p.x + cols, this.startYRange());
		}
	}

	public void addTime(Time t, Point p, int cols) {
		times.put(p, t);
		if (Math.abs(p.x - originalClef.x) < 200)
			startDetection = new Point(p.x + cols, this.startYRange());
	}

	public boolean isOnStaveLine(Line l) {
		return (Math.abs(topLine().start().y - l.start().y) < staveGap / 3
				&& l.length > topLine().length / 10 || Math.abs(bottomLine()
				.start().y - l.start().y) < staveGap / 3
				&& l.length > bottomLine().length / 10);
	}

	// Only implemented for flats
	public void addToKeySignature(Shift flat) {
		switch (keySignature.keySet().size()) {
		case 0:
			keySignature.put(NoteName.B, flat);
			break;
		case 1:
			keySignature.put(NoteName.E, flat);
			break;
		case 2:
			keySignature.put(NoteName.A, flat);
			break;
		default:
			Log.d("Guillaume",
					"This should not be printed @Stave.addToKeySignature. keySignature size: "
							+ keySignature.keySet().size());
		}
	}

	public Point startDetection() {
		return startDetection;
	}

	public Point originalClef() {
		return originalClef;
	}

	public void draw(Mat image) {
		Scalar col = new Scalar(128, 0, 0);

		for (int i = 0; i < 5; i++) {
			Core.line(image, lines.get(i).toLine().start(), lines.get(i)
					.toLine().end(), col, 3);
		}
	}

	public void drawDetailed(Mat image, Scalar col) {
		for (int i = 0; i < 5; i++) {
			for (Line l : lines.get(i).getLines()) {
				Core.line(image, l.start(), l.end(), col, 3);
			}
		}
	}

	public double staveGap() {
		return staveGap;
	}

	public double startYRange() {
		return topLine().start().y - 4 * staveGap;
	}

	public Range yRange(int maxRows) {
		return new Range((int) Math.max(0, topLine().start().y - 4 * staveGap),
				(int) Math.min(maxRows, bottomLine().start().y + 4 * staveGap));
	}

	public Range closeYRange(int maxRows) {
		return new Range((int) Math.max(0, topLine().start().y),
				(int) Math.min(maxRows, bottomLine().start().y));
	}

	public Range xRange() {
		return new Range((int) startDetection.x, (int) lines.get(0).toLine()
				.end().x);
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
			calculateNotePitchFor(n);
		}
	}

	private void calculateNotePitchFor(Note n) {
		double nx = (n.center().x);
		double ny = (n.center().y);
		double y1 = getY(lines.get(0), nx);
		double y2 = getY(lines.get(4), nx);
		double gap = y2 - y1;

		// 0-1 where 0 is top line, 1 is bottom
		double pos = (ny - y1) / gap;
		int line = (int) Math.round(8 - pos * 8);
		NoteName name = OurUtils.getName(getClefAtPos(n.center()), line);
		n.setName(name);
		n.setOctave(OurUtils.getOctave(getClefAtPos(n.center()), line));
	}

	private double getY(StaveLine line, double x) {
		for (Line l : line.getLines()) {
			if (l.end().x > x) {
				return l.start().y;
			}
		}
		return 0;
	}

	public double staveGapAtPos(Point center) {
		double nx = (center.x);
		double y1 = getY(lines.get(0), nx);
		double y2 = getY(lines.get(4), nx);
		return (y2 - y1) / 4;
	}

	public List<Bar> toBars() {
		List<Bar> bars = new LinkedList<Bar>();
		Bar currentBar = new Bar();
		bars.add(currentBar);

		int duration = 0;
		List<PlayedNote> notes = createPlayedNotes();
		Map<NoteName, Shift> accidentals = new HashMap<NoteName, Shift>(
				keySignature);
		for (PlayedNote n : notes) {

			Log.i("NOTE", n.toString());
			if (n.shift() != Shift.Natural) {
				if (accidentals.containsKey(n.name()))
					accidentals.remove(n.name());
				accidentals.put(n.name(), n.shift());
			}
			currentBar.addNote(n);

			if (accidentals.containsKey(n.name()))
				n.setShift(accidentals.get(n.name()));

			duration += n.getDuration();
			if (duration >= AbstractNote.TEMP_44LENGTH) {
				duration = 0;
				currentBar = new Bar();
				bars.add(currentBar);
				accidentals = new HashMap<NoteName, Shift>(keySignature);
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

	public void removeNote(Note n) {
		notes.remove(n);
	}

}
