package utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import musicdetection.Line;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class ReducedSlice {
	
	private final Mat mat;
	private List<Section> sections;
	private static final int THRESHOLD = 190;
	private final Point topLeft;
	private final Point bottomRight;

	public ReducedSlice(final Mat mat, Point topLeftCorner, Point bottomRightCorner) {
		this.mat = mat;
		this.topLeft = topLeftCorner;
		this.bottomRight = bottomRightCorner;
		
		if (mat.height()<=0 || mat.width()!=1) 
			throw new RuntimeException("ReducedSlice must take in mat of width 1, height>1");
		
		CalculateSections();
	}

	private void CalculateSections() {
		sections = new LinkedList<Section>();
		boolean inSection = false;
		int gapStart = 0;
		
		for(int y=0; y<mat.height();y++) {
			int v = (int) mat.get(y, 0)[0];
			
			if (inSection) {
				if (v > THRESHOLD) {
					// end of gap detected
					sections.add(new Section(gapStart,y));
					inSection = false;
				}
			} else {
				if (v <= THRESHOLD) {
					// start of gap detected
					inSection = true;
				}
			}
		}
	}
	
	public void join(ReducedSlice nextSlice) {
		for (Section section : sections) {
			for (Section nextSection : nextSlice.sections) {
				section.AttemptJoin(nextSection);
			}			
		}
	}

	public List<Line> GetLines() {
		List<Line> lines = new LinkedList<Line>();
		for (Section section : sections) {
			if (section.IsStartOfLine()) {
				Section end = section;
				while (end.HasNext()) end=end.next;
				lines.add(new Line(
						new Point(topLeft.x,topLeft.y+section.y()), 
						new Point(bottomRight.x,topLeft.y+end.y())));
			}
		}
		return lines;
	}

	private class Section {

		private final int start;
		private final int end;
		
		private Section next = null;
		private boolean hasPrevious = false;
		
		public Section(int start, int end) {
			this.start=start;
			this.end=end;
		}

		public double y() {
			return(start+end)/2;
		}

		public boolean IsStartOfLine() {
			return !hasPrevious;
		}
		
		public boolean HasNext() {
			return next!=null;
		}
		
		public Section Next() {
			return next;
		}

		public void AttemptJoin(Section nextSection) {
			if (this.joinsTo(nextSection)) {
				next = nextSection;
				nextSection.hasPrevious = true;
			}
		}

		private boolean joinsTo(Section nextSection) {
			int x1 = start, x2 = end, 
				y1 = nextSection.start, y2 = nextSection.end;
			
			return (x1 >= y1 && x1 <= y2) ||
					(x2 >= y1 && x2 <= y2) ||
					(y1 >= x1 && y1 <= x2) ||
					(y2 >= x1 && y2 <= x2);
		}
	}
}
