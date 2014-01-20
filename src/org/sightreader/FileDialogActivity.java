package org.sightreader;

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
    
}
