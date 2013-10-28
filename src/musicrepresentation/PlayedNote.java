package musicrepresentation;

public class PlayedNote extends Note {
	private final NoteName name;
	private final int octave;

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

}
