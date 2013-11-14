package utils;

import java.util.LinkedList;
import java.util.List;

import musicdetection.StaveLine;

public class SheetStrip {

	private Slice[] slices;
	private ReducedSlice[] reducedSlices;

	public SheetStrip(Slice[] slices) {
		this.slices = slices;
	}

	public List<StaveLine> FindLines() {
		int totalSlices = slices.length;
		this.reducedSlices = new ReducedSlice[totalSlices];

		// reduce all slices
		for (int slice = 0; slice < totalSlices; slice++) {
			reducedSlices[slice] = slices[slice].Reduce();
		}

		// join them
		for (int slice = 0; slice < totalSlices - 1; slice++) {
			reducedSlices[slice].join(reducedSlices[slice + 1]);
		}

		// get lines
		List<StaveLine> lines = new LinkedList<StaveLine>();
		for (ReducedSlice slice : reducedSlices) {
			lines.addAll(slice.GetLines());
		}

		return lines;
	}
}
