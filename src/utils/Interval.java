package utils;

import org.opencv.core.Range;

public class Interval {
	
	private int min, max;
	
	public Interval(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	public Interval (Range r) {
		this.min = r.start;
		this.max = r.end;
	}
	
	public boolean contains(int number) {
		return number <= max && number >= min;
	}
	
	public int min() {
		return min;
	}
	
	public int max() {
		return max;
	}
	
	public Range toRange() {
		return new Range(min, max);
	}
	
}
