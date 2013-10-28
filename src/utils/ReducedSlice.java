package utils;

import java.util.LinkedList;
import java.util.List;

import musicdetection.Line;
import musicdetection.StaveLine;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class ReducedSlice {
	
	private final Mat mat;
	private List<Section> sections;
	private static final int THRESHOLD = 90;
	private final Point topLeft;
	private final Point bottomRight;

	public ReducedSlice(final Mat mat, Point topLeftCorner, Point bottomRightCorner) {
		this.mat = mat;
		this.topLeft = topLeftCorner;
		this.bottomRight = bottomRightCorner;
		
		//if (mat.height()<=0 || mat.width()!=1) 
		if (mat.width()!=1) 
			throw new RuntimeException("ReducedSlice must take in mat of width 1, height>0. Has dimensions: "+mat.width()+"x"+mat.height());
		
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
					sections.add(new Section((int)topLeft.x,(int)bottomRight.x,gapStart,y));
					inSection = false;
				}
			} else {
				if (v <= THRESHOLD) {
					// start of gap detected
					inSection = true;
					gapStart=y;
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

	public List<StaveLine> GetLines() {
		List<StaveLine> staveLines = new LinkedList<StaveLine>();
		for (Section section : sections) {
			if (section.IsStartOfLine()) {
				// get first section
				List<Line> lines = new LinkedList<Line>();
				Section end = section;
				lines.add(end.toLine(this));
				// iterate to end of sections
				while (end.HasNext()) {
					end = end.getNext();
					lines.add(end.toLine(this));
				}
				
				staveLines.add(new StaveLine(lines));
			}
		}
		return staveLines;
	}

	public List<Line> GetSectionLines() {
		List<Line> lines = new LinkedList<Line>();
		for (Section section : sections) {

				lines.add(new Line(
						new Point(topLeft.x,topLeft.y+section.y()), 
						new Point(bottomRight.x,topLeft.y+section.y())));
			
		}
		return lines;
	}

	private class Section {

		private final int startY;
		private final int endY;
		private final int startX;
		private final int endX;
		
		private Section next = null;
		private boolean hasPrevious = false;
		
		public Section(int startX, int endX, int startY, int endY) {
			this.startX=startX;
			this.endX=endX;
			this.startY=startY;
			this.endY=endY;
		}

		public Line toLine(ReducedSlice slice) {
			double y = slice.topLeft.y+y();
			return new Line(new Point(startX,y), new Point(endX,y));
		}

		public double y() {
			return(startY+endY)/2;
		}

		public boolean IsStartOfLine() {
			return !hasPrevious;
		}
		
		public boolean HasNext() {
			return next!=null;
		}
		
		public Section getNext() {
			return next;
		}

		public void AttemptJoin(Section nextSection) {
			if (this.joinsTo(nextSection)) {
				next = nextSection;
				nextSection.hasPrevious = true;
			}
		}

		private boolean joinsTo(Section nextSection) {
			int topA = startY, bottomA = endY, 
				topB = nextSection.startY, bottomB = nextSection.endY;
			
			int tol = 3;
			return (topA >= topB-tol && topA <= bottomB+tol) ||
					(bottomA >= topB-tol && bottomA <= bottomB+tol) ||
					(topB >= topA-tol && topB <= bottomA+tol) ||
					(bottomB >= topA-tol && bottomB <= bottomA+tol);

			
			/*return (x1 >= y1 && x1 <= y2) ||
					(x2 >= y1 && x2 <= y2) ||
					(y1 >= x1 && y1 <= x2) ||
					(y2 >= x1 && y2 <= x2);*/
		}
	}
}
