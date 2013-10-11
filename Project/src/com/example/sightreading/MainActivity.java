package com.example.sightreading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.*;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	private static final String  TAG              = "SightReading::Activity";

	private BaseLoaderCallback  mLoaderCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");
				//mOpenCvCameraView.enableView();
				//mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.loadLibrary("opencv_java");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mLoaderCallBack)) { 
		if (!OpenCVLoader.initDebug()) { 
			Log.e(TAG, "Cannot connect to OpenCV Manager"); 
		} else {
			//
		}

		File imgFile = new  File("/Pictures/test.jpg");
		Mat houghMat = new Mat();
		if(imgFile.exists())
		{ 
			houghMat = Highgui.imread(imgFile.getAbsolutePath());
		}

		//Mat src = Highgui.imread(filename, 0);
		//Mat dst, cdst;
		//Imgproc.Canny(src, dst, 50, 200, 3, false);
		//cvtColor(dst, cdst, CV_GRAY2BGR);

		//Mat houghMat = Highgui.imread(filename, 0);
		Mat lines = new Mat();

		Imgproc.HoughLines(houghMat, lines, 1, Math.PI/180, 1);

		double[] data;
		double rho, theta;
		Point pt1 = new Point();
		Point pt2 = new Point();
		double a, b;
		double x0, y0;
		Scalar color = new Scalar(0, 0, 255);

		for( int i = 0; i < lines.cols(); i++ )
		{
			data = lines.get(0, i);
			rho = data[0];
			theta = data[1];
			a = Math.cos(theta);
			b = Math.sin(theta);
			x0 = a*rho;
			y0 = b*rho;
			pt1.x = Math.round(x0 + 1000*(-b));
			pt1.y = Math.round(y0 + 1000*a);
			pt2.x = Math.round(x0 - 1000*(-b));
			pt2.y = Math.round(y0 - 1000 *a);
			Core.line(houghMat, pt1, pt2, color, 3);
		}

		Mat imageMat = new Mat(); 
		Imgproc.cvtColor(houghMat, imageMat, Imgproc.COLOR_GRAY2BGRA, 4);
		Bitmap bmp = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);

		File file = new File("assets/", "test.png");
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
