package org.sightreader;

import java.util.List;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

public class SRCameraView extends JavaCameraView implements PictureCallback {

	private static final String TAG = "SRCameraView";
	private int mRotation;
	private CameraActivity callback;
	private boolean configured = false;

	public SRCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public List<String> getEffectList() {
		return mCamera.getParameters().getSupportedColorEffects();
	}

	public boolean isEffectSupported() {
		return (mCamera.getParameters().getColorEffect() != null);
	}

	public String getEffect() {
		return mCamera.getParameters().getColorEffect();
	}

	public void setEffect(String effect) {
		Camera.Parameters params = mCamera.getParameters();
		params.setColorEffect(effect);
		mCamera.setParameters(params);
	}

	public List<Size> getResolutionList() {
		return mCamera.getParameters().getSupportedPreviewSizes();
	}

	public void setResolution(Size resolution) {
		disconnectCamera();
		mMaxHeight = resolution.height;
		mMaxWidth = resolution.width;
		connectCamera(getWidth(), getHeight());

	}

	public Size getResolution() {
		return mCamera.getParameters().getPreviewSize();
	}

	public void takePicture(int rotation) {
		Log.i(TAG, "Taking picture");
		mRotation = rotation;
		// Postview and jpeg are sent in the same buffers if the queue is not
		// empty when performing a capture.
		// Clear up buffers to avoid mCamera.takePicture to be stuck because of
		// a memory issue
		mCamera.setPreviewCallback(null);

		// PictureCallback is implemented by the current class
		if (!configured) {
			configure();
		}

		Size size = mCamera.getParameters().getPictureSize();
		Log.i("TEST_CAMERA_SIZE", size.width + "  " + size.height);

		mCamera.takePicture(null, null, this);
	}

	private void configure() {
		configured = true;
		Parameters params = mCamera.getParameters();
		Size pictureSize = getBestPictureSize(params);
		Size previewSize = getBestPreviewSize(params);

		params.setPictureSize(pictureSize.width, pictureSize.height);
		params.setPreviewSize(previewSize.width, previewSize.height);

		Size test = params.getPictureSize();

		Log.i("FINAL_SIZE", pictureSize.width + "  " + pictureSize.height);
		Log.i("ACTUAL_SIZE", test.width + "  " + test.height);

		mCamera.setParameters(params);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.i(TAG, "Decoding camera data");
		// The camera preview was automatically stopped. Start it again.
		mCamera.startPreview();
		mCamera.setPreviewCallback(this);

		long startTime = System.currentTimeMillis();
		Log.i(TAG, "before decode " + (System.currentTimeMillis() - startTime));
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		Log.i(TAG, "after decode " + (System.currentTimeMillis() - startTime));
		Matrix matrix = new Matrix();

		// Log.i("CAM",""+camera.equals(mCamera));
		// Log.i("FINAL_CAMERA_SIZE",size.width+"  "+size.height);
		// Log.i("BITMAP_SIZE", bitmap.getWidth() + "  " + bitmap.getHeight());

		switch (mRotation) {
		case Surface.ROTATION_0:
			matrix.postRotate(90);
			break;
		case Surface.ROTATION_90:
			break;
		case Surface.ROTATION_180:
			matrix.postRotate(270);
			break;
		case Surface.ROTATION_270:
			matrix.postRotate(180);
			break;
		}

		Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		Log.i(TAG, "after rotate " + (System.currentTimeMillis() - startTime));

		// TODO this maybe should change to a save and load because i think it
		// may cause memory problems?
		DisplayPhotoActivity.image = rotatedBitmap;

		// OurUtils.saveTempImage(bitmap, "INPUTROT");

		if (callback != null) {
			Intent i = new Intent(callback, DisplayPhotoActivity.class);
			callback.startActivity(i);
		}
	}

	public void setCallback(CameraActivity cameraActivity) {
		this.callback = cameraActivity;
	}

	private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			Log.i("SIZE", "" + size.width + "  " + size.height);
			if (result == null) {
				result = size;
			} else {
				int resultArea = result.width * result.height;
				int newArea = size.width * size.height;

				if (newArea > resultArea) {
					result = size;
				}

				if (size.width >= 2000)
					break;
			}
		}

		return (result);
	}

	private Camera.Size getBestPictureSize(Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			Log.i("SIZE", "" + size.width + "  " + size.height);
			if (result == null) {
				result = size;
			} else {
				int resultArea = result.width * result.height;
				int newArea = size.width * size.height;

				if (newArea > resultArea) {
					result = size;
				}

				if (size.width >= 2000)
					break;
			}
		}

		return (result);
	}
}
