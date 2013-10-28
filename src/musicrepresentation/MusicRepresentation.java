package musicrepresentation;

import java.util.LinkedList;
import java.util.List;

public class MusicRepresentation {

	private List<PlayedStave> staves;
	
	public MusicRepresentation(List<PlayedStave> staves) {
		this.staves = staves;
	}
	
	public List<PlayedNote> create() {
		List<PlayedNote> notes = new LinkedList<PlayedNote>();
		for (PlayedStave s : staves) {
			notes.addAll(s.notes());
		}
		return notes;
	}
}
