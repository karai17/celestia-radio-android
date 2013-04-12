package com.landonmanning.mediaplayer.celestiaradio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Schedule
{
	public String[] items = new String[24];
	
	public void ParseSchedule(String day, String jsonstr)
	{
		JSONObject json;
		try
		{
			json = new JSONObject(jsonstr);
		}
		catch (JSONException e)
		{
			Log.e("ERROR: ParseSchedule", "Error parsing JSON!");
			e.printStackTrace();
			return;
		}
		
		String[] sched = new String[24];
		try
		{
			JSONObject jobj = json.getJSONObject("schedule");
			JSONObject jobjday = jobj.getJSONObject(day);
			Iterator iter = jobjday.keys();
			for(int i = 0; i < 24; i++)
			{
				 items[i] = "   " + jobjday.getString((String) iter.next());
			}
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	}
