package org.sightreading;

import android.app.Activity;
import android.content.Intent;

import com.lamerman.FileDialog;

public class FileDialogActivity extends FileDialog {
	public synchronized void onActivityResult(final int requestCode,
			int resultCode, final Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			// user selected file
			String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
			Intent intent = new Intent(getBaseContext(), PlaybackActivity.class);
			
		} else if (resultCode == Activity.RESULT_CANCELED) {
			// user selected back button
			// this should go back to start screen
		}

	}

}
