package utils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class Slice {

	private final Mat mat;
	private final Point topLeft;
	private final Point bottomRight;
	
	public Slice(Mat mat, Point topLeftCorner, Point bottomRightCorner) {
		this.mat=mat;
		this.topLeft = topLeftCorner;
		this.bottomRight = bottomRightCorner;
	}

	public ReducedSlice Reduce() {
		Mat result = mat.clone();
		Core.reduce(mat, result, 1, Core.REDUCE_AVG, CvType.CV_32S);
		return new ReducedSlice(result, topLeft, bottomRight);
	}
}
