package org.sightreader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import utils.OurUtils;

public class SRFileBuilder {

	private String filePath;
	private String midiPath;
	private List<String> imagePaths;

	public SRFileBuilder(String saveName) {
		imagePaths = new LinkedList<String>();
		this.filePath = OurUtils.getPath(OurUtils.DATA_FOLDER) + "/" + saveName
				+ ".sr";
		;
	}

	public void build() {
		File dir = new File(OurUtils.getPath(OurUtils.DATA_FOLDER));
		if (!dir.exists())
			dir.mkdirs();

		File saveFile = new File(filePath);

		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(saveFile);
			outputStream.write(midiPath.getBytes());
			for (String imagePath : imagePaths) {
				outputStream.write(System.getProperty("line.seperator")
						.getBytes());
				outputStream.write(imagePath.getBytes());
			}
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setMidiPath(String midiPath) {
		this.midiPath = midiPath;
	}

	public void addImagePath(String ImagePath) {
		imagePaths.add(ImagePath);
	}
}
