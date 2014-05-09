package my.info;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import my.info.Poi;

@SuppressLint("DefaultLocale")
public class MyInfoActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mPrefs = getPreferences(MODE_PRIVATE);
		DebugMode = mPrefs.getBoolean("DebugMode", false);
		mContext=this;
		if (PoiList.isEmpty())
			Convert();
	}

	static Context mContext;


	public void onButtonLoadClick(View view) {

	}

	public void onButtonNoClick(View view) {
		Toast.makeText(this, "Button No clicked!", Toast.LENGTH_SHORT).show();
		finish();
	}

	public void onButtonYesClick(View view) {
		if (PoiList.isEmpty())
			return;
		Intent intent = new Intent(this, RunningActivity.class);
		intent.putExtra("Extended", false);
		startActivity(intent);
	}

	public void onButtonAllClick(View view) {
		if (PoiList.isEmpty())
			return;
		Intent intent = new Intent(this, RunningActivity.class);
		intent.putExtra("Extended", true);
		startActivity(intent);
	}

	public static CopyOnWriteArrayList<Poi> ClosePoiList = new CopyOnWriteArrayList<Poi>();
	public static CopyOnWriteArrayList<Poi> PoiList = new CopyOnWriteArrayList<Poi>();
	public static boolean Converting = false;
	
	public static Location LastRecalculationLocation;
	public static void ReCalulateClosePois()
	{
//		String locationProvider = LocationManager.NETWORK_PROVIDER;
		String locationProvider = LocationManager.GPS_PROVIDER;		
		LocationManager locationManager =  (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
		// Or use LocationManager.GPS_PROVIDER		

		Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
		if (lastKnownLocation==null)
			lastKnownLocation=new Location(locationProvider);
		ArrayList<Poi> tempClosePoiList = new ArrayList<Poi>();
		for  (Poi p : PoiList) {		
			float[] results = new float[3];
			Location.distanceBetween(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
					p.getLatitudeE6() / (double) 1E6,
					p.getLongitudeE6() / (double) 1E6, results);
			if (results[0]<100000) //100km
				tempClosePoiList.add(p);
			
		}
		LastRecalculationLocation=lastKnownLocation;
		ClosePoiList = new CopyOnWriteArrayList<Poi>(tempClosePoiList);
		RunningActivity.PoiList = ClosePoiList;
	}
	
	public static boolean Recalculating = false;
	
	public static void Recalculate() {
		if (!Recalculating) {
			Recalculating = true;

			new Thread(new Runnable() {
				public void run() {
					ReCalulateClosePois();			
					Recalculating = false;
				}
			}).start();
		}
	}
	

	public void onButtonConvertClick(View view) {

		Convert();

	}

	private void Convert() {
		if (!Converting) {
			PoiList.clear();
			Converting = true;

			new Thread(new Runnable() {
				public void run() {
					String list[] = {};
					
					ArrayList<Poi> PoiList = new ArrayList<Poi>();

					String path = "";
					AssetManager mgr = getAssets();
					final TextView tv = ((TextView) findViewById(R.id.SelectedFile));
					final TextView tv2 = ((TextView) findViewById(R.id.NrOfPoisFound));
					try {
						list = mgr.list(path);
						int filecount = 1;
						for (String f : list)
							if (f.toLowerCase().endsWith(".csv")) {
								final String text = "" + filecount++;
								tv.post(new Runnable() {
									public void run() {
										tv.setText(text);
									}
								});

								String Type = f.substring(0, f.indexOf("."));
								BufferedReader br = new BufferedReader(
										new InputStreamReader(getAssets().open(
												f)));
								String line;
								int counter = 0;
								while ((line = br.readLine()) != null) {
									counter++;
									if (counter % 100 == 0) {
										final String text3 = ""
												+ PoiList.size();
										tv.post(new Runnable() {
											public void run() {
												tv2.setText(text3);
											}
										});
									}
									int i = line.indexOf(",");
									int j = line.indexOf(",", i + 1);
									double Longitude = 0, Latitude = 0;
									Longitude = Double.parseDouble(line
											.substring(0, i));
									Latitude = Double.parseDouble(line
											.substring(i + 1, j));
									Poi p = new Poi((int) (Latitude * 1E6),
											(int) (Longitude * 1E6), Type);
									PoiList.add(p);
									// if (UseDatabase)
									// m_poiDB.insertPoi(p);
								}
								final String text2 = "" + PoiList.size();
								tv.post(new Runnable() {
									public void run() {
										tv2.setText(text2);
									}
								});
								if (DebugMode)
									break;
							}

					} catch (Exception e) {
						Log.i("MyInfoActivity",
								"onButtonConvertClick() Convert "
										+ e.getMessage());
					}
					MyInfoActivity.PoiList = new  CopyOnWriteArrayList<Poi>(PoiList);
					ReCalulateClosePois();
					tv.post(new Runnable() {
						public void run() {
							tv.setText("");
						}
					});					
					Converting = false;
				}
			}).start();
		}
	}

	final int DebugModeMenuItemId = 0;

	final int AboutItemId = 1;
	final int ExitMenuItemId = 2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem DebugModeItem = menu.add(Menu.NONE, DebugModeMenuItemId,
				Menu.NONE, R.string.Debugmode);
		DebugModeItem.setCheckable(true);
		DebugModeItem.setChecked(DebugMode);

		
		menu.add(Menu.NONE, AboutItemId, Menu.NONE, R.string.About);

		menu.add(Menu.NONE, ExitMenuItemId, Menu.NONE, R.string.Exit);
		return true;
	}

	private boolean DebugMode = false;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DebugModeMenuItemId:
			DebugMode = !item.isChecked();
			item.setChecked(DebugMode);
			break;
		
		case AboutItemId:
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			PackageInfo packageInfo = new PackageInfo();
			try {
				packageInfo = getPackageManager().getPackageInfo(
						getPackageName(), 0);
			} catch (Exception e) {
			}

			long BuildDate = 0;
			ApplicationInfo ai;
			try {
				ai = getPackageManager()
						.getApplicationInfo(getPackageName(), 0);
				ZipFile zf = new ZipFile(ai.sourceDir);
				ZipEntry ze = zf.getEntry("classes.dex");

				BuildDate = ze.getTime();
				zf.close();
			} catch (Exception e) {

			}

			Date d = new Date(BuildDate);
			SimpleDateFormat sdf = new SimpleDateFormat(
					"dd MMM yyyy HH:mm:ss Z z");
			// sdf.setTimeZone(TimeZone.getDefault());

			alertbox.setMessage("Version: " + packageInfo.versionName + " ("
					+ packageInfo.versionCode + ")\n\n" + "Build-Date: "
					+ sdf.format(d));
			alertbox.setNeutralButton("Ok", null);
			alertbox.show();
			break;
		case ExitMenuItemId:
			finish();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private SharedPreferences mPrefs;

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putBoolean("DebugMode", DebugMode);
		ed.commit();
	}

}