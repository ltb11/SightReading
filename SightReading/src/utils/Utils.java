package utils;

import org.opencv.core.Scalar;

public class Utils {
	public static Scalar createHsvColor(float hue, float saturation, float value) {
	    int h = (int)(hue % 6);
	    float f = hue * 6 - h;
	    float p = value * (1 - saturation);
	    float q = value * (1 - f * saturation);
	    float t = value * (1 - (1 - f) * saturation);
	    switch (h) {
	      case 0: return new Scalar(value, t, p);
	      case 1: return new Scalar(q, value, p);
	      case 2: return new Scalar(p, value, t);
	      case 3: return new Scalar(p, q, value);
	      case 4: return new Scalar(t, p, value);
	      case 5: return new Scalar(value, p, q);
	      default: throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
	    }
	}
}
