package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Create web browser to hold the about view
		WebView wv = new WebView(this);
		wv.loadUrl("file:///android_asset/about.html");

		// Set activity view
		setContentView(wv);

	}
}
