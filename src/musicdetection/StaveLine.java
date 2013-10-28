package musicdetection;

import java.util.List;

public class StaveLine {

	private Line bestFitLine;
	private List<Line> lines;
	
	public StaveLine(List<Line> lines) {
		Line start = lines.get(0);
		Line end = lines.get(lines.size()-1);
		
		this.bestFitLine = new Line(start.start(), end.end());
		this.lines=lines;
	}
	
	public List<Line> getLines() {
		return lines;
	}
	
	public Line toLine() {
		return bestFitLine;
	}
}
