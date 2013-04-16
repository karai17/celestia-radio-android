package com.landonmanning.mediaplayer.celestiaradio;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class Schedule {
	public ScheduleEntry[] items = new ScheduleEntry[24];
	
	public void ParseSchedule(String day, String jsonstr) {		
		//String[] sched = new String[24];
		try {
			JSONObject json = new JSONObject(jsonstr);
			JSONObject jobj = json.getJSONObject("schedule");
			JSONObject jobjday = jobj.getJSONObject(day);
			Iterator iter = jobjday.keys();
			for(int i = 0; i < 24; i++)
			{
				String Time = (String)iter.next();
				String Name = jobjday.getString(Time);
				items[i] = new ScheduleEntry(Time, Name);
			}
			Arrays.sort(items, new ScheduleComparator());
		}
		catch (JSONException e) {
			e.printStackTrace();
		}		
	}
	
	class ScheduleComparator implements Comparator<ScheduleEntry>  {
	    public int compare(ScheduleEntry entry1, ScheduleEntry entry2) {
	        return entry1.Time.compareTo(entry2.Time);
	    }
	}
}
