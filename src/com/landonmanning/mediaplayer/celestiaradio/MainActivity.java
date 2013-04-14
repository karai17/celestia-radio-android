package com.landonmanning.mediaplayer.celestiaradio;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.text.format.Time;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;
/**
 * Celestia Radio Main Activity
 * @author Landon "Karai" Manning
 * @email LManning17@gmail.com
 *
 */



public class MainActivity extends Activity {
    private MediaPlayer player;						// Media player
    private MetaTask metaTask;						// Async Task for continuous updating
    private ImageView logo;							// Company logo
    private TextView artist, title, serverTitle, timetv;	// Artist & Title data
    private ImageButton togglePlay;					// Play/Stop button
    private TableLayout scheduleTable;
    private NotificationCompat.Builder notificationBuilder = null;
    private NotificationManager mNotificationManager;
    private int NotificationId = 123454321;
	private boolean isPlaying;
	private boolean isPlayerLoaded;
	private Time t = new Time();
	private String day = "Splorgday";
	private String scheduleJSON = "";

	/**
	 * Create Activity
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        MakeNotification();
      //Get schedule JSON
        RetrieveScheduleTask scheduleGetter = (RetrieveScheduleTask) new RetrieveScheduleTask().execute((Void)null);
		try {
			scheduleJSON = scheduleGetter.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        

        //-- System Stuff --
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.main);
        
		
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //-- Prepare Variables --
        this.player			= new MediaPlayer();
        this.metaTask		= new MetaTask();
        this.logo			= (ImageView) findViewById(R.id.logo);
        this.artist			= (TextView) findViewById(R.id.artist);
        this.title			= (TextView) findViewById(R.id.title);
        this.serverTitle	= (TextView) findViewById(R.id.serverTitle);
        this.togglePlay		= (ImageButton) findViewById(R.id.togglePlay);
        timetv = (TextView) findViewById(R.id.timeanddate);
        //-- Make Links Clickable --
        this.artist.setMovementMethod(LinkMovementMethod.getInstance());
        this.title.setMovementMethod(LinkMovementMethod.getInstance());
        
        //-- Enable Marquee Effect --
    	this.artist.setSelected(true);
    	this.title.setSelected(true);
    	
    	//-- Prepare Meta Task --
    	this.metaTask.execute(getString(R.string.stats));
    	
        t.timezone = "UTC";
        t.setToNow();
    	
    	//-- Prepare MediaPlayer --
        this.player.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				MainActivity.this.togglePlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.stop));
			}
    	});
        
        //-- Make Logo Link to Website --
        this.logo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getString(R.string.website)));
                startActivity(intent);
            }
        });
    	
    	//-- Toggle Play Button --
        this.togglePlay.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				togglePlay();
			}
		});     
        
    }
	
    public void UpdateNotification()
    {
    	if(notificationBuilder == null)
    	{
    		MakeNotification();
    	}
    	notificationBuilder.setContentTitle(title.getText().toString());
    	notificationBuilder.setContentText(artist.getText().toString());
		mNotificationManager.notify(NotificationId, notificationBuilder.build());
    }
    
    private void ClearNotification()
    {
    	mNotificationManager.cancel(NotificationId);	
    }
    
    private void MakeNotification() {
		 notificationBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher);
		 Intent notificationIntent = new Intent(getApplicationContext(), NotificationActivity.class);
		 TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(notificationIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		notificationBuilder.setContentIntent(resultPendingIntent);

	    mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder.setOngoing(true);	
	}

	/**
     * Change Configuration
     */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	/**
	 * Pause Activity
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		if(isFinishing()) {
			this.metaTask.stop();
			this.ClearNotification();
			this.player.release();
		}
	}
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		this.ClearNotification();
	}
	
	/**
	 * Toggle Play Button
	 */
	private void togglePlay() {
		try {
			if (!this.player.isPlaying()) {
				isPlaying = true;
		    	this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				this.player.setDataSource(getString(R.string.address));
				this.player.prepareAsync();
				isPlayerLoaded = true;
				this.player.start();
				MainActivity.this.togglePlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.stop));
				UpdateNotification();
			} else {
				isPlaying = false;
				//this.player.pause();
				this.player.reset();
				this.togglePlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.play));
				ClearNotification();
			}
		} catch (IllegalArgumentException e) {
			Log.e("ERROR: togglePlay", "Invalid data source!");
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.e("ERROR: togglePlay", "Play state buggered up!");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("ERROR: togglePlay", "Invalid data source!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Set Meta Data
	 * @param json		JSON object from stats page
	 */
	private void setMeta(JSONObject json) {
		try {
			BuildScheduleTable();
			//-- Parse Top Level JSON --
			String currentListeners		= json.getString("CURRENTLISTENERS");
			String serverTitle			= json.getString("SERVERTITLE");
			JSONArray songHistoryArray	= json.getJSONArray("SONGHISTORY");
			String songHistory[][];
			
			songHistory = new String[10][5];
			
			//-- Parse SONGHISTORY --
			for (int i = 0; i < songHistoryArray.length(); i++) {
				JSONObject sh = songHistoryArray.getJSONObject(i);
				String h[] = {
						sh.getString("PLAYEDAT"),
						sh.getString("ARTISTID"),
						sh.getString("ARTIST"),
						sh.getString("SONGID"),
						sh.getString("SONG"),
						sh.getString("TITLE"),
				};
				
				if (!sh.isNull("ARTISTID")) {
					h[1] = "<a href='http://eqbeats.org/user/" + h[1] + "'>";
					h[2] = h[2] + "</a>";
				} else {
					h[1] = "";
				}
				
				if (!sh.isNull("SONGID")) {
					h[3] = "<a href='http://eqbeats.org/track/" + h[3] + "'>";
					h[4] = h[4] + "</a>";
				} else {
					h[3] = "";
				}
				
				songHistory[i] = h;
			}
			
			//-- Set Meta Data --
			this.serverTitle.setText(currentListeners + " ponies tuned in!");
			this.artist.setText(Html.fromHtml(songHistory[0][1] + songHistory[0][2]));
			this.title.setText(Html.fromHtml(songHistory[0][3] + songHistory[0][4]));
			
			t.setToNow();
			String timestr = day + ", " + String.format("%02d", t.hour) + ":" + String.format("%02d", t.minute) + " UTC";
			this.timetv.setText(timestr);

			if(isPlaying)
				UpdateNotification();
		} catch (JSONException e) {
			Log.e("ERROR: setMeta", "Error parsing JSON!");
			e.printStackTrace();
		}
	}
	
	private void BuildScheduleTable()
	{
		 TableLayout tl = (TableLayout)findViewById(R.id.scheduleTable);
		 tl.removeAllViews();
		 LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		 TableRow trdays = new TableRow(this);
	     TextView[] tvdays = new TextView[8];
	     for(int i = 0; i < 8; i++)
	     {
	    	 tvdays[i] = new TextView(this);
	    	 tvdays[i].setLayoutParams(lp);
	    	 tvdays[i].setTextColor(Color.parseColor("#ff8000"));
	     }
	     switch (t.weekDay) {
         case 0:  day = "Sunday";
                  break;
         case 1:  day = "Monday";
                  break;
         case 2:  day = "Tuesday";
                  break;
         case 3:  day = "Wednesday";
                  break;
         case 4:  day = "Thursday";
                  break;
         case 5:  day = "Friday";
                  break;
         case 6:  day = "Saturday";
                  break;
	     }     
	     Schedule schedule = new Schedule();     
		
	     schedule.ParseSchedule(day, scheduleJSON);
	     
	     tl.addView(trdays, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		 for(int i = 0; i < 24; i++)
		 {
			 TableRow tr = new TableRow(this);
		     TextView tvtime = new TextView(this);
		     tvtime.setLayoutParams(lp);
		     tvtime.setText(schedule.items[i].Time);
		     tvtime.setTextColor(Color.parseColor("#ff8000"));
		     tvtime.setShadowLayer(1.0f, 1.0f, 1.0f, Color.parseColor("#555555"));
		     tr.addView(tvtime);
		     TextView tvEntry = new TextView(this);
		     tvEntry.setLayoutParams(lp);
		     tvEntry.setText("   " + schedule.items[i].Name);
		     tvEntry.setTextColor(Color.parseColor("#ff8000"));
		     tvEntry.setShadowLayer(1.0f, 1.0f, 1.0f, Color.parseColor("#555555"));
		     tr.addView(tvEntry);
		     tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		 } 
	     Log.d("Notice", "End BuildScheduleTable");
	     return;
	}
	
	/**
	 * Get Meta Data From SHOUTcast Stats page (http://ponify.me/stats.php)
	 * @author Landon Manning
	 * @email LManning17@gmail.com
	 *
	 */
	private class MetaTask extends AsyncTask<String, String, String> {
		private Boolean running = true;
		
		public void stop() {
			this.running = false;
		}
		
		protected String doInBackground(String... address) {
			while(this.running) {
				try {
	  	       		//-- Connect to and read Meta Data page --
	  	       		URLConnection con = new URL(address[0]).openConnection();
	  	       		Reader r = new InputStreamReader(con.getInputStream());
	  	       		//-- Build JSON String --
	  	       		StringBuilder buffer = new StringBuilder();
	  	       		int ch;
	  	       		
	  	       		while (true) {
	  	       			ch = r.read();
	  	       		
	  	       			if (ch < 0)
	  	       				break;
	  	       		
	  	       			buffer.append((char) ch);
	  	       		}
	  	       		
	  	       		this.publishProgress(buffer.toString());
	  	       		Thread.sleep(5000); // 30 seconds
	  			} catch (MalformedURLException e) {
	  				Log.e("ERROR: doInBackground", "Invalid URL!");
	  				e.printStackTrace();
	  			} catch (IOException e) {
	  				Log.e("ERROR: doInBackground", "Error reading input stream!");
	  				e.printStackTrace();
	  			} catch (InterruptedException e) {
	  				Log.e("ERROR: doInBackground", "Thread sleep interrupted!");
	  				e.printStackTrace();
				}
			}
			
			return null;
		}
		
		protected void onProgressUpdate(String... values) {
			if (!this.running)
				return;
			
			//-- Send JSONObject --
			try {
				JSONObject json = new JSONObject(values[0].toString());
				MainActivity.this.setMeta(json);
				
			} catch (JSONException e) {
  				Log.e("ERROR: onProgressUpdate", "Error parsing JSON!");
				e.printStackTrace();
			}
		}
		
	}
}