package utils;

public class Interval {
	
	public int min, max;
	
	public Interval(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	public boolean contains(int number) {
		return number <= max && number >= min;
	}
	
}
