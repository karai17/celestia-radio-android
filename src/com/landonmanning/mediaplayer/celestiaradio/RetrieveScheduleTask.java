package com.landonmanning.mediaplayer.celestiaradio;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
import android.util.Log;

public class RetrieveScheduleTask extends AsyncTask<Void, Void, String> {
	private String readScheduleJSON() {
		StringBuilder buffer = new StringBuilder();
		Log.d("Notice", "Start readScheduleJSON");
	    try {
     		URLConnection con = new URL("http://www.ponify.me/schedule.json.php").openConnection();
     		Reader r = new InputStreamReader(con.getInputStream());
     		
     		//-- Build JSON String --
     		int ch;
     		
     		while (true) {
     			ch = r.read();
     		
     			if (ch < 0)
     				break;
     		
     			buffer.append((char) ch);
     		}
     		r.close();
		} catch (MalformedURLException e) {
			Log.e("ERROR: doInBackground", "Invalid URL!");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("ERROR: doInBackground", "Error reading input stream!");
			e.printStackTrace();
		}
		Log.d("Notice", "End readScheduleJSON");
	    return buffer.toString();
	  }

	@Override
	protected String doInBackground(Void... arg0) {
		return readScheduleJSON();
	}

}
