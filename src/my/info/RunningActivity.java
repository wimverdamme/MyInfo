package my.info;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

public class RunningActivity extends Activity {

	// Button button;

	private boolean extended = false;
	private boolean gps_enabled = false;
	private LocationManager locManager, dummylocmanager;
	private boolean network_enabled = false;
	private LocationListener locListener = new MyLocationListener(this),
			dummyloclistener = new MyDummyLocationListener();
	private boolean isPlugged = false;
	private ArrayList<Poi> PoiList = MyInfoActivity.PoiList;
	private ArrowDirectionView Arrow;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.running);
		extended = getIntent().getExtras().getBoolean("Extended");
		((TextView) findViewById(R.id.ExtendedValue)).setText(this
				.getResources().getText(
						extended ? R.string.LabelYes : R.string.LabelNo));

		locManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		dummylocmanager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		try {
			gps_enabled = locManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}
		try {
			network_enabled = locManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
		}
		String LocationEnabled = "Off";
		if (gps_enabled)
			if (network_enabled)
				LocationEnabled = "Network/Gps";
			else
				LocationEnabled = "Gps";
		else if (network_enabled)
			LocationEnabled = "Network";
		((TextView) findViewById(R.id.LocationEnabledValue))
				.setText(LocationEnabled);

		if (gps_enabled) {
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
					0, locListener);
			dummylocmanager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 3600000, 100000,
					dummyloclistener);

		} else if (network_enabled) {
			locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					0, 0, locListener);
		}

		this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
	

		Arrow = new ArrowDirectionView(this);
		((TableRow) findViewById(R.id.DirectionRow)).addView(Arrow);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mPrefs = getPreferences(MODE_PRIVATE);
		m_factor = mPrefs.getFloat("SizeFactor", 1);
		changeSize((ViewGroup) findViewById(R.id.Running), m_factor);

		ContinuousMode = mPrefs.getBoolean("ContinuousMode", false);
		WarningSecs = mPrefs.getInt("WarningSecs", 5);
		
		int visible = extended ? View.VISIBLE : View.INVISIBLE;
		Visibility(visible);

	}

	public void Visibility(int visible ) 
	{

		findViewById(R.id.ChargerConnected).setVisibility(visible);
		findViewById(R.id.ChargerConnectedValue).setVisibility(visible);
		findViewById(R.id.Distance).setVisibility(visible);
		findViewById(R.id.DistanceValue).setVisibility(visible);
		findViewById(R.id.DirectionNumeric).setVisibility(visible);
		findViewById(R.id.DirectionNumericValue).setVisibility(visible);
		findViewById(R.id.Separation1).setVisibility(visible);
		findViewById(R.id.Separation2).setVisibility(visible);
		findViewById(R.id.Speed).setVisibility(visible);
		findViewById(R.id.SpeedValue).setVisibility(visible);
		findViewById(R.id.Type).setVisibility(visible);
		findViewById(R.id.TypeValue).setVisibility(visible);
		findViewById(R.id.DirectionArrow).setVisibility(visible);
		Arrow.setVisibility(visible);
		
		
	}

	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			isPlugged = (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB);
			((TextView) findViewById(R.id.ChargerConnectedValue))
					.setText(getResources().getText(
							isPlugged ? R.string.Plugged : R.string.Unplugged)
							+ " " + plugged);
			if (extended && plugged==0)
			{
				extended=false;
				((TextView)findViewById(R.id.ExtendedValue)).setText(R.string.LabelNo);
			}
			int visible = (extended&&plugged!=0) ? View.VISIBLE : View.INVISIBLE;
			Visibility(visible);		
			
		}
	};

	class MyDummyLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	class MyLocationListener implements LocationListener {
		Context parent;

		public MyLocationListener(Context parent) {
			this.parent = parent;
			lastBeep = System.currentTimeMillis() - 6000;
		}

		long lastBeep = 0;
		Poi lastBeepPoi = null;

		public void onLocationChanged(Location location) {
			if (location != null) {
				// This needs to stop getting the location data and save the
				// battery power.
				locManager.removeUpdates(locListener);
				DecimalFormat df = new DecimalFormat("#.#####");
				((TextView) findViewById(R.id.LongitudeValue)).setText(df
						.format(location.getLongitude()));
				((TextView) findViewById(R.id.LatitudeValue)).setText(df
						.format(location.getLatitude()));
				df = new DecimalFormat("#");
				((TextView) findViewById(R.id.AltitudeValue)).setText(df
						.format(location.getAltitude()));
				((TextView) findViewById(R.id.AccuracyValue)).setText(df
						.format(location.getAccuracy()));

				Date date = new Date();
				date.setTime(location.getTime());
				// Time time = new Time();
				// time.set(location.getTime());
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

				((TextView) findViewById(R.id.UpdateTimeValue)).setText(sdf
						.format(date));

				Poi p = findClosedPoi(location.getLatitude(),
						location.getLongitude());
				float[] results = new float[3];
				Location.distanceBetween(location.getLatitude(),
						location.getLongitude(), p.getLatitudeE6()
								/ (double) 1E6, p.getLongitudeE6()
								/ (double) 1E6, results);
				((TextView) findViewById(R.id.DistanceValue)).setText(df
						.format(results[0]));
				((TextView) findViewById(R.id.DirectionNumericValue))
						.setText(df.format(results[2]));

				Arrow.setDirection(results[2] - location.getBearing());
				((TextView) findViewById(R.id.TypeValue)).setText(p.getType());

				float speed = location.getSpeed();
				((TextView) findViewById(R.id.SpeedValue)).setText(df
						.format(speed * 3.6));

				long now = System.currentTimeMillis();
				if ((speed > 0) && (results[0] / speed < WarningSecs)
						&& (lastBeep + 5000 < now || lastBeepPoi != p)) {
					final ToneGenerator tg = new ToneGenerator(
							AudioManager.STREAM_NOTIFICATION, 100);
					tg.startTone(ToneGenerator.TONE_PROP_BEEP2);
					lastBeep = now;
					lastBeepPoi = p;
					((TextView) findViewById(R.id.ExtendedValue)).setText(sdf
							.format(date));
				}

				if (gps_enabled) {
					locManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, ContinuousMode ? 0
									: 15000, ContinuousMode ? 0
									: results[0] / 3, locListener);
				} else if (network_enabled) {
					locManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							ContinuousMode ? 0 : 15000, ContinuousMode ? 0
									: results[0] / 3, locListener);
				}

			}
		}

		public Poi findClosedPoi(double Latitude, double Longitude) {
			Poi closestPoi = PoiList.get(0);
			float[] results = new float[3];
			Location.distanceBetween(Latitude, Longitude,
					closestPoi.getLatitudeE6() / (double) 1E6,
					closestPoi.getLongitudeE6() / (double) 1E6, results);
			double closestDist = results[0];
			for (Poi p : PoiList) {
				Location.distanceBetween(Latitude, Longitude, p.getLatitudeE6()
						/ (double) 1E6, p.getLongitudeE6() / (double) 1E6,
						results);
				if (results[0] < closestDist) {
					closestDist = results[0];
					closestPoi = p;
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

	public enum MenuItemIds {
		ContinuousModeMenuItemId, ResizeText, ExitMenuItemId, Resize, WarningSecs
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem ContinuousModeItem = menu.add(Menu.NONE,
				MenuItemIds.ContinuousModeMenuItemId.ordinal(), Menu.NONE,
				R.string.Continuousmode);
		ContinuousModeItem.setCheckable(true);
		ContinuousModeItem.setChecked(ContinuousMode);

		menu.add(Menu.NONE, MenuItemIds.Resize.ordinal(), Menu.NONE,R.string.Resize);
		menu.add(Menu.NONE, MenuItemIds.WarningSecs.ordinal(), Menu.NONE,R.string.WarningSecs);
		menu.add(Menu.NONE, MenuItemIds.ExitMenuItemId.ordinal(), Menu.NONE,R.string.Exit);
		return true;
	}

	private static boolean ContinuousMode = false;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Button button ;
		EditText et;
		oldfactor=m_factor;
		switch (MenuItemIds.values()[item.getItemId()]) {
		case ContinuousModeMenuItemId:
			ContinuousMode = !item.isChecked();
			item.setChecked(ContinuousMode);
			break;
		case ExitMenuItemId:
			locManager.removeUpdates(locListener);
			dummylocmanager.removeUpdates(dummyloclistener);
			finish();
			break;
		case Resize:
			popUp = new Dialog(RunningActivity.this);
			popUp.setContentView(R.layout.popupdialog);
			popUp.setTitle("Input");
			popUp.setCancelable(true);			
			
			et=(EditText) popUp.findViewById(R.id.Value);
			et.setInputType(android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL );			
			et.setText(""+m_factor);
			
			button = (Button) popUp.findViewById(R.id.CloseButton);
			button.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					popUp.dismiss();
				}
			});
			button = (Button) popUp.findViewById(R.id.OkButton);
			button.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					View parent = (View) v.getParent();

					View bla = parent.findViewById(R.id.Value);
					String Info = ((TextView) parent.findViewById(R.id.Value)).getText().toString();
					try{
						m_factor=Float.parseFloat(Info);
					}catch (NumberFormatException e){}

					popUp.dismiss();
					float newfactor = m_factor/oldfactor;
					changeSize((ViewGroup) findViewById(R.id.Running), newfactor);
					
				}
			});
			
			popUp.show();			
			break;

		case WarningSecs:

			popUp = new Dialog(RunningActivity.this);
			popUp.setContentView(R.layout.popupdialog);
			popUp.setTitle("Input");
			popUp.setCancelable(true);			
			
			et=(EditText) popUp.findViewById(R.id.Value);
			et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);			
			et.setText(""+WarningSecs);
			
			button = (Button) popUp.findViewById(R.id.CloseButton);
			button.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					popUp.dismiss();
				}
			});
			button = (Button) popUp.findViewById(R.id.OkButton);
			button.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					View parent = (View) v.getParent();
					String Info = ((TextView) parent.findViewById(R.id.Value)).getText().toString();
					try{
					WarningSecs=Integer.parseInt(Info);
					}catch (NumberFormatException e){}

					popUp.dismiss();
				}
			});

			popUp.show();

			break;
		}

		return super.onOptionsItemSelected(item);
	}
	Dialog popUp;
	float oldfactor;

	private void changeSize(ViewGroup viewgroup, float factor) {
		for (int i = 0; i < viewgroup.getChildCount(); i++) {
			View v = viewgroup.getChildAt(i);

			Class<? extends View> c = v.getClass();
			if (v instanceof ViewGroup)
				changeSize((ViewGroup) v, factor);
			if (c == TextView.class) {
				float size = ((TextView) v).getTextSize();
				((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_PX, size
						* factor);
			}
		}
	}

	private float m_factor = 1;
	private int WarningSecs = 5;
	private SharedPreferences mPrefs;

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putFloat("SizeFactor", m_factor);
		ed.putBoolean("ContinuousMode", ContinuousMode);
		ed.putInt("WarningSecs", WarningSecs);
		ed.commit();
	}

}