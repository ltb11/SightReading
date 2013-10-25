package utils;

import java.util.LinkedList;
import java.util.List;

import musicdetection.Line;
import musicdetection.Stave;

public class SheetStrip {
	
	private Slice[] slices;
	private ReducedSlice[] reducedSlices;
	
	public SheetStrip(Slice[] slices) {
		this.slices=slices;
	}

	public List<Line> FindStave() {
		int totalSlices = slices.length;
		this.reducedSlices = new ReducedSlice[totalSlices];
		
		// reduce all slices
		for(int slice=0;slice<totalSlices;slice++) {
			reducedSlices[slice]=slices[slice].Reduce();
		}
		
		for(int slice=0;slice<totalSlices-1;slice++) {
			reducedSlices[slice].join(reducedSlices[slice+1]);
		}
		
		List<Line> lines = new LinkedList<Line>();
		for (ReducedSlice slice : reducedSlices) {
			lines.addAll(slice.GetLines());
		}
		
		return lines;
	}
}
