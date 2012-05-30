package my.info;
 
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableRow;
import android.widget.TextView;
 
public class RunningActivity extends Activity {
 
	//Button button;
	
	private boolean extended=false;
	private boolean gps_enabled = false;
	private LocationManager locManager;
	private boolean network_enabled = false;
	private LocationListener locListener = new MyLocationListener(this);
	private boolean isPlugged=false;
	private ArrayList<Poi> PoiList =MyInfoActivity.PoiList;
	private ArrowDirectionView Arrow;
 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.running);
		extended=getIntent().getExtras().getBoolean("Extended");
		((TextView) findViewById(R.id.ExtendedValue)).setText(this.getResources().getText(extended?R.string.LabelYes:R.string.LabelNo));		
		
		locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		try 
		{
			gps_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {}
		try 
		{
			network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {}
		String LocationEnabled="Off";
		if (gps_enabled)
			if (network_enabled)
				LocationEnabled="Network/Gps"; 
			else
				LocationEnabled="Gps";
		else
			if (network_enabled)
				LocationEnabled="Network";
		((TextView) findViewById(R.id.LocationEnabledValue)).setText(LocationEnabled);
				
		if (gps_enabled) {
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
		}
		else if (network_enabled) {
			locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
		}
		
		this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); 
		
		int visible=extended?View.VISIBLE:View.INVISIBLE;
	
		findViewById(R.id.ChargerConnected).setVisibility(visible);
		findViewById(R.id.ChargerConnectedValue).setVisibility(visible);
		findViewById(R.id.Distance).setVisibility(visible);
		findViewById(R.id.DistanceValue).setVisibility(visible);
		findViewById(R.id.DirectionNumeric).setVisibility(visible);
		findViewById(R.id.DirectionNumericValue).setVisibility(visible);
		findViewById(R.id.Separation1).setVisibility(visible);
		findViewById(R.id.Separation2).setVisibility(visible);
		
		findViewById(R.id.DirectionArrow).setVisibility(visible);
		
		Arrow=new ArrowDirectionView(this);
		((TableRow)findViewById(R.id.DirectionRow)).addView(Arrow);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	}
	
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){  
	    @Override  
	    public void onReceive(Context arg0, Intent intent) {  
	      // TODO Auto-generated method stub  
	      int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);	  
	      isPlugged=(plugged==BatteryManager.BATTERY_PLUGGED_AC ||  plugged==BatteryManager.BATTERY_PLUGGED_USB);
	      ((TextView)findViewById(R.id.ChargerConnectedValue)).setText(getResources().getText(isPlugged?R.string.Plugged:R.string.Unplugged));
	    }  
	  }; 
	
	class MyLocationListener implements LocationListener {
		Context parent;
		public MyLocationListener(Context parent)
		{
			this.parent=parent;
			lastBeep=System.currentTimeMillis()-6000;
		}

		long lastBeep;		

		public void onLocationChanged(Location location) {
			if (location != null) {
				// This needs to stop getting the location data and save the battery power.
				locManager.removeUpdates(locListener); 
				DecimalFormat df = new DecimalFormat("#.#####");
				((TextView) findViewById(R.id.LongitudeValue)).setText(df.format(location.getLongitude()));
				((TextView) findViewById(R.id.LatitudeValue)).setText(df.format(location.getLatitude()));
				df = new DecimalFormat("#");
				((TextView) findViewById(R.id.AltitudeValue)).setText(df.format(location.getAltitude()));
				((TextView) findViewById(R.id.AccuracyValue)).setText(df.format(location.getAccuracy()));	

				Date date=new Date();
				date.setTime(location.getTime());
				//Time time = new Time();
				//time.set(location.getTime());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

				((TextView) findViewById(R.id.UpdateTimeValue)).setText(sdf.format(date));
				
				Poi p=findClosedPoi(location.getLatitude(),location.getLongitude());
				float[] results= new float[3];
				Location.distanceBetween(location.getLatitude(), location.getLongitude(), p.getLatitudeE6()/(double)1E6, p.getLongitudeE6()/(double)1E6, results);
				((TextView) findViewById(R.id.DistanceValue)).setText(df.format(results[0]));
				((TextView) findViewById(R.id.DirectionNumericValue)).setText(df.format(results[2]));
				
				Arrow.setDirection(results[2]-location.getBearing());
				((TextView) findViewById(R.id.TypeValue)).setText(p.getType());
					
				float speed = location.getSpeed();
				((TextView) findViewById(R.id.SpeedValue)).setText(df.format(speed));
				
				long now=System.currentTimeMillis();
				if (speed>0  && results[0]/location.getSpeed()<5 && lastBeep+5000<now)
				{					
					final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
				     tg.startTone(ToneGenerator.TONE_PROP_BEEP);
				     lastBeep=now;
				}
				
				if (gps_enabled) {
					locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, results[0]/3, locListener);
				}
				else if (network_enabled) {
					locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 15000, results[0]/3, locListener);
				}
			
			} 
		}
		
		public Poi findClosedPoi(double Latitude,double Longitude)
		{
			Poi closestPoi=PoiList.get(0);
			float[] results= new float[3];
			Location.distanceBetween(Latitude, Longitude, closestPoi.getLatitudeE6()/(double)1E6, closestPoi.getLongitudeE6()/(double)1E6, results);
			double closestDist=results[0];
			for (Poi p : PoiList)
			{
				Location.distanceBetween(Latitude, Longitude, p.getLatitudeE6()/(double)1E6, p.getLongitudeE6()/(double)1E6, results);
				if (results[0]<closestDist)
				{
					closestDist=results[0];
					closestPoi=p;
				}
			}
			return closestPoi;
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
		
		public String getTimeString(long millisecond) {
		    String timeString = "";

		    int totalSeconds = (int) millisecond / 1000;
		    int hours = totalSeconds / 3600;
		    int remainder = totalSeconds % 3600;
		    int minutes = remainder / 60;
		    int seconds = remainder % 60;

		    if (hours == 0) {
		      timeString = minutes + ":" + seconds;
		    } else {
		      timeString = hours + ":" + minutes + ":" + seconds;
		    }
		    return timeString;
		  }
	}


 
}