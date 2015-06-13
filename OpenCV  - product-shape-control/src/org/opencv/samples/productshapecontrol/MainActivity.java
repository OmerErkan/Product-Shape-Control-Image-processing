package org.opencv.samples.productshapecontrol;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	private static Button compareImageBtn;
	private static Button captureImageBtn;
	private MediaPlayer playerBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);
		compareImageBtn = (Button) MainActivity.this
				.findViewById(R.id.idcompareimage);
		captureImageBtn = (Button) MainActivity.this
				.findViewById(R.id.idcapturePatternImage);
		
		run();

	}

	public void run() {

		captureImageBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent iCapture = new Intent(MainActivity.this,
						ImageCapture.class);
				startActivity(iCapture);

			}
		});

		compareImageBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//playerBtn.start();
				 Intent iCompare= new Intent(MainActivity.this,
				 ImageComprator.class);
				 startActivity(iCompare);
				 
			}
		});
	}
}
