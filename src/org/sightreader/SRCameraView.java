package org.sightreader;

import java.util.List;

import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import utils.OurUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

public class SRCameraView extends JavaCameraView implements PictureCallback {

	private static final String TAG = "SRCameraView";
	private String mPictureFileName;
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

	public void takePicture(final String fileName, int rotation) {
		Log.i(TAG, "Taking picture");
		mPictureFileName = fileName;
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
		Log.i("TEST_CAMERA_SIZE",size.width+"  "+size.height);
		
		mCamera.takePicture(null, null, this);
	}

	private void configure() {
		configured=true;
		Parameters params = mCamera.getParameters();
		Size pictureSize = getBestPictureSize(params);
		
		params.setPictureSize(pictureSize.width,
                pictureSize.height);
		params.setPreviewSize(pictureSize.width,
                pictureSize.height);
		
		Size test = params.getPictureSize();
		
		Log.i("FINAL_SIZE",pictureSize.width+"  "+pictureSize.height);
		Log.i("ACTUAL_SIZE",test.width+"  "+test.height);
		
		mCamera.setParameters(params);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.i(TAG, "Saving a bitmap to file");
		// The camera preview was automatically stopped. Start it again.
		mCamera.startPreview();
		mCamera.setPreviewCallback(this);

		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		Mat tmp = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);

		Utils.bitmapToMat(bitmap, tmp);
		OurUtils.saveTempImage(bitmap, "INPUT");
		
		//Log.i("CAM",""+camera.equals(mCamera));
		//Log.i("FINAL_CAMERA_SIZE",size.width+"  "+size.height);
		Log.i("BITMAP_SIZE",bitmap.getWidth()+"  "+bitmap.getHeight());
		

		switch (mRotation) {
		case Surface.ROTATION_0:
			Mat mRgbaT = tmp.t();
			Core.flip(tmp.t(), mRgbaT, 1);
			//Imgproc.resize(mRgbaT, mRgbaT, tmp.size());
			tmp = mRgbaT;
			break;
		case Surface.ROTATION_90:
			break;
		case Surface.ROTATION_180:
			Mat mRgbaT1 = tmp.t();
			Core.flip(tmp.t(), mRgbaT1, 0);
			//Imgproc.resize(mRgbaT1, mRgbaT1, tmp.size());
			tmp = mRgbaT1;
			break;
		case Surface.ROTATION_270:
			Mat mRgbaT11 = tmp;
			Core.flip(tmp, mRgbaT11, 1);
			Core.flip(mRgbaT11, mRgbaT11, 0);
			//Imgproc.resize(mRgbaT11, mRgbaT11, tmp.size());
			tmp = mRgbaT11;
			break;
		}

		Utils.matToBitmap(tmp, bitmap);

		DisplayPhotoActivity.image = bitmap;

		if (callback != null) {
			Intent i = new Intent(callback, DisplayPhotoActivity.class);
			callback.startActivity(i);
		}
	}

	public void setCallback(CameraActivity cameraActivity) {
		this.callback = cameraActivity;
	}
	
	
	  private Camera.Size getBestPictureSize(Camera.Parameters parameters) {
	    Camera.Size result=null;

	    for (Camera.Size size : parameters.getSupportedPictureSizes()) {
	      Log.i("SIZE",""+size.width+"  "+size.height);
	      if (result == null) {
	        result=size;
	      }
	      else {
	        int resultArea=result.width * result.height;
	        int newArea=size.width * size.height;

	        if (newArea > resultArea) {
	          result=size;
	        }
	        
	        if (size.width>=2000) break;
	      }
	    }

	    return(result);
	 }
}