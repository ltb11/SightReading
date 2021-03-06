package musicrepresentation;

public class PlayedNote extends AbstractNote {
	private final NoteName name;
    private Shift shift;
	private final int octave;

	public PlayedNote(NoteName name, int octave,Shift shift,
			Duration duration, int dots) {
		this(name,octave,shift,duration,dots,AbstractNote.STANDARD_VELOCITY);
	}

	public PlayedNote(NoteName name, int octave, Shift shift,
			Duration duration, int dots, int velocity) {
		super(duration, dots, velocity);
		this.name = name;
		this.shift = shift;
		this.octave = octave;
	}

    @Override
    public int getPitch(){
        int noteVal = 12;// C0 is MIDI 12
        noteVal += octave * 12 + name.ordinal() + (shift.ordinal() - 2);
        return noteVal;
    }

	public int getOctave() {
		return this.octave;
	}
	
	public NoteName name() {
		return name;
	}
	
	public void setShift(Shift shift) {
		this.shift = shift;
	}

	public Shift shift() {
		return shift;
	}
	
	@Override
	public String toString() {
		String result = "";
		result += name + " at octave " + octave + ", it's a " + getDuration();
		return result;
	}
}
