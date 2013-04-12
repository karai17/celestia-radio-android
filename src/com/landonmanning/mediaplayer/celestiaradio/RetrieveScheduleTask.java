package com.landonmanning.mediaplayer.celestiaradio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class RetrieveScheduleTask extends AsyncTask<Void, Void, String>
{
	private String readScheduleJSON()
	{
	    StringBuilder builder = new StringBuilder();
	    HttpClient client = new DefaultHttpClient();
	    HttpGet httpGet = new HttpGet("http://www.ponify.me/schedule.json.php");
	    try
	    {
	      HttpResponse response = client.execute(httpGet);
	      StatusLine statusLine = response.getStatusLine();
	      int statusCode = statusLine.getStatusCode();
	      if (statusCode == 200)
	      {
	        HttpEntity entity = response.getEntity();
	        InputStream content = entity.getContent();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	        String line;
	        while ((line = reader.readLine()) != null)
	        {
	          builder.append(line);
	        }
	      } 
	      else 
	      {
	        Log.e("readScheduleJSON", "Failed to download file");
	      }
	    } catch (ClientProtocolException e) 
	    {
	      e.printStackTrace();
	    } 
	    catch (IOException e) 
	    {
	      e.printStackTrace();
	    }
	    return builder.toString();
	  }

	@Override
	protected String doInBackground(Void... arg0)
	{
		return readScheduleJSON();
	}

}
