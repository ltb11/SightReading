package utils;

public class ComposedBeamInterval extends Interval {

	int division;

	public ComposedBeamInterval(int min, int max, int division) {
		super(min, max);
		this.division = division;
	}

	@Override
	public int division() {
		return division;
	}

}
