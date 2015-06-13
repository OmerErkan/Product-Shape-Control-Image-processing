package org.opencv.samples.productshapecontrol;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.features2d.FeatureDetector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class ImageCapture extends Activity implements CvCameraViewListener2 {
	private static final String TAG = "OCVSample::Activity";

	private ShowJavaCamera mOpenCvCameraView;
	private List<Size> mResolutionList;
	private MenuItem[] mEffectMenuItems;
	private SubMenu mColorEffectsMenu;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;
	private MenuItem mItemPreviewFeatures;
	private static FeatureDetector detector;
	private FeatureDetector featureDetector;

	private static final int VIEW_MODE_FEATURES = 5;
	private Mat mRgba;
	private Mat mGray;
	private int mViewMode;

	private Button captureBtn;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				System.loadLibrary("shape_control");
				// mOpenCvCameraView.takePicture(null, null,
				// new ShowJavaCamera(getApplicationContext(), null));
				// mOpenCvCameraView.setOnTouchListener(ImageCapture.this);
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public ImageCapture() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.captureimage_javacamera);

		mOpenCvCameraView = (ShowJavaCamera) findViewById(R.id.tutorial3_activity_java_surface_view);

		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

		mOpenCvCameraView.setCvCameraViewListener(this);
		captureBtn = (Button) findViewById(R.id.button1);
		captureBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				takeFoto();
//				try {
//				Thread.sleep(3000); // 1000 milliseconds is one second.
//				} catch (InterruptedException ex) {
//					Thread.currentThread().interrupt();
//				}
//				Intent call2 = new Intent(ImageCapture.this, MainActivity.class);
//				startActivity(call2);

			}
		});

	}

	@SuppressLint("SimpleDateFormat")
	public void takeFoto() {
		Log.i(TAG, "onTouch event");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String currentDateandTime = sdf.format(new Date());
		String fileName = Environment.getExternalStorageDirectory().getPath()
				+ "/sample_picture_" + currentDateandTime + ".jpg";
		mOpenCvCameraView.takePicture(fileName);
		Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
		featureDetector = FeatureDetector.create(4);

	}

	public void onCameraViewStopped() {
		mRgba.release();
		mGray.release();
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// return inputFrame.rgba();
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
		return mRgba;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		List<String> effects = mOpenCvCameraView.getEffectList();

		if (effects == null) {
			Log.e(TAG, "Color effects are not supported by device!");
			return true;
		}

		mColorEffectsMenu = menu.addSubMenu("Color Effect");
		mEffectMenuItems = new MenuItem[effects.size()];
		mItemPreviewFeatures = menu.add("Find features");
		int idx = 0;
		ListIterator<String> effectItr = effects.listIterator();
		while (effectItr.hasNext()) {
			String element = effectItr.next();
			mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE,
					element);
			idx++;
		}

		mResolutionMenu = menu.addSubMenu("Resolution");
		mResolutionList = mOpenCvCameraView.getResolutionList();
		mResolutionMenuItems = new MenuItem[mResolutionList.size()];

		ListIterator<Size> resolutionItr = mResolutionList.listIterator();
		idx = 0;
		while (resolutionItr.hasNext()) {
			Size element = resolutionItr.next();
			mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
					Integer.valueOf(element.width).toString() + "x"
							+ Integer.valueOf(element.height).toString());
			idx++;
		}

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
		if (item.getGroupId() == 1) {
			mOpenCvCameraView.setEffect((String) item.getTitle());
			Toast.makeText(this, mOpenCvCameraView.getEffect(),
					Toast.LENGTH_SHORT).show();
		} else if (item.getGroupId() == 2) {
			int id = item.getItemId();
			Size resolution = mResolutionList.get(id);
			mOpenCvCameraView.setResolution(resolution);
			resolution = mOpenCvCameraView.getResolution();
			String caption = Integer.valueOf(resolution.width).toString() + "x"
					+ Integer.valueOf(resolution.height).toString();
			Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
		} else if (item == mItemPreviewFeatures) {
			Toast.makeText(this, " feauture in selected ", Toast.LENGTH_SHORT)
					.show();
			mViewMode = VIEW_MODE_FEATURES;

		}

		return true;
	}

	public native void FindFeatures(long matAddrGr, long matAddrRgba);
}
