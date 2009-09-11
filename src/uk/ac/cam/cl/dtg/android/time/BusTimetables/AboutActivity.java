package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);	
		
		// Create web browser to hold the about view
		WebView wv = new WebView(this);
		
		/*// Where the code will be stored
		String html;
		
		
		// For fetching assets
		AssetManager m = this.getAssets();
		
		try {
			
			// Load about.html into the string
			InputStream is = m.open("about.html");
			html = convertStreamToString(is);
			
		} catch (IOException e) {
			
			// Set html string to a nice error
			html = "<b>Error loading about screen</b><br /><pre>"+e.getMessage()+"</pre>";
		
		}*/
		
		// Render the html
		//wv.loadDataWithBaseURL("file:///android_asset/",html, "text/html", "utf-8", null);

		wv.loadUrl("file:///android_asset/about.html");
		
		// Set activity view
		setContentView(wv);
	
	}
	
	   private String convertStreamToString(InputStream is) {
	        /*
	         * To convert the InputStream to String we use the BufferedReader.readLine()
	         * method. We iterate until the BufferedReader return null which means
	         * there's no more data to read. Each line will appended to a StringBuilder
	         * and returned as String.
	         */
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        StringBuilder sb = new StringBuilder();

	        String line = null;
	        try {
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                is.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }

	        return sb.toString();
	    }


}
