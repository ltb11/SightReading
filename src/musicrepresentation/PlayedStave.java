package musicrepresentation;

import java.util.Collection;
import java.util.List;

public class PlayedStave {

	private List<PlayedNote> notes;
	
	public PlayedStave(List<PlayedNote> notes) {
		this.notes = notes;
	}

	public Collection<? extends PlayedNote> notes() {
		return notes;
	}

}
