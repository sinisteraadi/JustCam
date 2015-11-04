package com.example.cameraapp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity implements SurfaceHolder.Callback, OnClickListener{

	private static final String EXTERNAL_STORAGE_DIR = Environment.getExternalStorageDirectory()+ File.separator  + "NAT";
	private Camera mCamera;
	private boolean mPreviewRunning;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	
	private Button mButtonCapture;
	private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

		public void onPictureTaken(byte[] imageData, Camera c) {

			final Bitmap bm = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
			final File file = new File(EXTERNAL_STORAGE_DIR, "ic_" + System.currentTimeMillis() + ".jpg");
			Log.d("cameradebug", "file");
			
			try {
				final FileOutputStream outstream = new FileOutputStream(file);
				
				bm.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
						
				if (outstream != null) {
					outstream.flush();
					outstream.close();
					
					Log.d("cameradebug", "file written "+file.getAbsolutePath());
				}
				// Update media gallery
				//updateMediaGallery();
				
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 			
			
			// Restart camera preview
			mCamera.startPreview();
		}

	};
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        mButtonCapture = (Button) findViewById(R.id.button_capture);
        mButtonCapture.setOnClickListener(this);
        
        // Create the necessary directories
        File storagePath = new File(EXTERNAL_STORAGE_DIR);
        storagePath.mkdirs();
       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}
		
		final Camera.Parameters params = mCamera.getParameters();
		
		final List<Camera.Size> sizes = params.getSupportedPreviewSizes();
		final Camera.Size size = sizes.get(0);
		
		final List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
		final Camera.Size pictureSize = pictureSizes.get(0);
		
		params.setPreviewSize(size.width, size.height);
		params.setPictureSize(pictureSize.width, pictureSize.height);
		
		mCamera.setParameters(params);
		
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mCamera.startPreview();
		
		mPreviewRunning = true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
		mCamera.stopPreview();
		
		mPreviewRunning = false;
		
		mCamera.release();
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId())
		{
		case R.id.button_capture:
			mCamera.autoFocus(new AutoFocusCallback() {
				
				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					mCamera.takePicture(null, null, mPictureCallback);					
				}
			});
			
			break;
		}
		
	}

	/**
	 * This method is used to notify the media gallery that there is a new image
	 * 
	 */
	private void updateMediaGallery() {
		
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ EXTERNAL_STORAGE_DIR)));

	}
	
    
}
