package org.sightreader;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;

import com.lamerman.FileDialog;

public class FileDialogActivity extends FileDialog {

	@Override
	public void onBackPressed() {
		super.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			super.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public synchronized void onActivityResult(final int requestCode,
			int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			// user selected file
			String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
			Intent intent = new Intent(getBaseContext(), PlaybackActivity.class);
			intent.putExtra("FILEPATH", filePath);
			startActivity(intent);
		} else if (resultCode == Activity.RESULT_CANCELED) {
			// user selected back button
			// this should go back to start screen
			super.finish();
		}
	}

}
