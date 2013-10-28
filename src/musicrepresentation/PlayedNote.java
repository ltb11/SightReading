package musicrepresentation;

public class PlayedNote extends AbstractNote {
	private final NoteName name;
	private final int octave;

	public PlayedNote(NoteName name, int octave,
			Duration duration, int dots) {
		this(name,octave,duration,dots,AbstractNote.STANDARD_VELOCITY);
	}

	public PlayedNote(NoteName name, int octave,
			Duration duration, int dots, int velocity) {
		super(duration, dots, velocity);
		this.name = name;
		this.octave = octave;
	}

    @Override
    public int getPitch(){
        int noteVal = 0;
        noteVal += octave * 12 + name.ordinal();
        return noteVal;
    }

	public int getOctave() {
		return this.octave;
	}
	
	@Override
	public String toString() {
		String result = "";
		result += name + " at octave " + octave + ", it's a " + getDuration();
		return result;
	}
}
