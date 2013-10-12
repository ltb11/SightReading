package utils;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class Utils {

	private static final int minLineDirectionVectorDiff = 10;
	private static final int minLineGap = 5;

	public static Scalar createHsvColor(float hue, float saturation, float value) {
		int h = (int) (hue % 6);
		float f = hue * 6 - h;
		float p = value * (1 - saturation);
		float q = value * (1 - f * saturation);
		float t = value * (1 - (1 - f) * saturation);
		switch (h) {
		case 0:
			return new Scalar(value, t, p);
		case 1:
			return new Scalar(q, value, p);
		case 2:
			return new Scalar(p, value, t);
		case 3:
			return new Scalar(p, q, value);
		case 4:
			return new Scalar(t, p, value);
		case 5:
			return new Scalar(value, p, q);
		default:
			throw new RuntimeException(
					"Something went wrong when converting from HSV to RGB. Input was "
							+ hue + ", " + saturation + ", " + value);
		}
	}

	/*
	 * This works as so: we check the line to test with every line already in
	 * and test: if the vector directing the line to test is close enough to the
	 * reference line (minLineDirectionVectorDiff) and the point on the test
	 * line with the same x as the start of the reference line is close enough
	 * to this start (minLineGap), the line is considered a duplicate and is
	 * dropped. We need to perform this check on both x and y coordinates
	 * because of horizontal/vertical lines
	 */
	public static boolean areTwoLinesDifferent(Point pt1, Point pt2, Mat lines,
			int i) {
		for (int j = 0; j < i; j++) {
			double[] lineToTest = lines.get(0, j);
			Point ptToTest1 = new Point(lineToTest[0], lineToTest[1]);
			Point ptToTest2 = new Point(lineToTest[2], lineToTest[3]);
			double[] v = new double[] { pt2.x - pt1.x, pt2.y - pt1.y };
			double[] vTest = new double[] { ptToTest2.x - ptToTest1.x,
					ptToTest2.y - ptToTest1.y };
			if ((Math.abs(vTest[1] / vTest[0] - v[1] / v[0]) < minLineDirectionVectorDiff && Math
					.abs(ptToTest1.y - pt1.y + (pt1.x - ptToTest1.x) * vTest[1]
							/ vTest[0]) < minLineGap)
					|| (Math.abs(vTest[0] / vTest[1] - v[0] / v[1]) < minLineDirectionVectorDiff && Math
							.abs(ptToTest1.x - pt1.x + (pt1.y - ptToTest1.y)
									* vTest[0] / vTest[1]) < minLineGap)) {
				return false;
			}
		}
		return true;
	}

}
