package org.sightreader;

import java.util.List;

import org.opencv.android.JavaCameraView;

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

public class SRCameraView extends JavaCameraView implements PictureCallback {

	private static final String TAG = "SRCameraView";
	private String mPictureFileName;
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

	public void takePicture(final String fileName) {
		Log.i(TAG, "Taking picture");
		this.mPictureFileName = fileName;
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

		Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);
		OurUtils.saveTempImage(bitmap, "test");
		
		Size size = camera.getParameters().getPictureSize();
		
		//Log.i("CAM",""+camera.equals(mCamera));
		Log.i("FINAL_CAMERA_SIZE",size.width+"  "+size.height);
		Log.i("BITMAP_SIZE",bitmap.getWidth()+"  "+bitmap.getHeight());
		
		DisplayPhotoActivity.image=bitmap;
		
		if (callback!=null) {
			Intent i = new Intent(callback,
					DisplayPhotoActivity.class);
			callback.startActivity(i);
		}
		
		// Write the image in a file (in jpeg format)
		/*try {
			FileOutputStream fos = new FileOutputStream(OurUtils.getPath("output/")+mPictureFileName);

			fos.write(data);
			fos.close();

		} catch (java.io.IOException e) {
			Log.e("PictureDemo", "Exception in photoCallback", e);
		}
		
		Log.i("PROC", "saved");*/
		
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
	        
	        if (size.width>1500) break;
	      }
	    }

	    return(result);
	 }
}