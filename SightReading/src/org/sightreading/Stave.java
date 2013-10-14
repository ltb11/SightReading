package org.sightreading;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class Stave {

	List<Line> lines;
	
	public Stave(List<Line> lines) {
		this.lines = lines;
		if (lines.size() != 5)
			throw new RuntimeException("Stave must have 5 lines!");
	}
	
	public void eraseFromMat(Mat image) {
		/*Scalar c1 = new Scalar(255, 0, 0);
		Scalar c2 = new Scalar(0, 255, 0);
		Scalar c3 = new Scalar(0, 0, 255);
		Scalar c4 = new Scalar(255, 0, 255);
		Scalar c5 = new Scalar(255, 255, 0);
		Scalar[] cs = new Scalar[] { c1, c2, c3, c4, c5};*/
		Scalar col = new Scalar(0,0,0);
		
		for (int i = 0; i < 5; i++) {
			Core.line(image, lines.get(i).start(), lines.get(i).end(), col, 1);
		}
	}
	
}
