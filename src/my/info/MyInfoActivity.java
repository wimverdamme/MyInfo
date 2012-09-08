package my.info;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import my.info.Poi;

public class MyInfoActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mPrefs = getPreferences(MODE_PRIVATE);
		DebugMode = mPrefs.getBoolean("DebugMode", false);
		DaysBack = mPrefs.getInt("DaysBack", 0);
		if (PoiList.isEmpty())
			Convert();
	}

	public void onButtonExportSaveClick(View view) {
		Intent intent = new Intent(getBaseContext(), FileDialog.class);
		String PathToSd=Environment.getExternalStorageDirectory().getPath();
		intent.putExtra(FileDialog.START_PATH, PathToSd);

		// can user select directories or not
		intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
		intent.putExtra(FileDialog.SELECT_ONLY_DIR, true);

		if (view.getId() == R.id.ExportButton)
			startActivityForResult(intent, REQUEST_EXPORT);
		else if (view.getId() == R.id.SaveButton)
			startActivityForResult(intent, REQUEST_SAVE);
	}

	static final int REQUEST_EXPORT = 1;
	static final int REQUEST_SAVE = 2;
	Dialog popUp;
	int DaysBack=0;
	public synchronized void onActivityResult(final int requestCode,
			int resultCode, final Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			String Action = "";
			if (requestCode == REQUEST_SAVE) {
				Action = "Saving to: ";
			} else if (requestCode == REQUEST_EXPORT) {
				Action = "Exporting to: ";
			}					

			String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
			Toast.makeText(this, Action + filePath, Toast.LENGTH_SHORT).show();
			if (requestCode == REQUEST_EXPORT) {
				File[] files = new File(getCacheDir(), "").listFiles();
				for (File f : files) {
					String filename = f.getName();
					if (filename.endsWith(".sav") && (DaysBack==0 || f.lastModified()+DaysBack*1000*60*60*24>(new Date()).getTime())) {
						String trackName = filename.substring(0,
								filename.length() - 4);
						try {
							ArrayList<RunningActivity.Point> points = new ArrayList<RunningActivity.Point>();
							ObjectInputStream is = new ObjectInputStream(
									new GZIPInputStream(new FileInputStream(
											getCacheDir() + "/" + filename)));
							while (is.available() > 0) {

								points.add(new RunningActivity.Point(is
										.readInt(), is.readInt(), is
										.readFloat(), is.readFloat(), is
										.readLong()));
							}
							is.close();

							File gpxFile = new File(filePath + "/" + trackName
									+ ".gpx");
							GPXFileWriter.writeGpxFile(trackName, points,
									gpxFile);

						} catch (Exception e) {
							Log.i("MyInfoActivity",
									"onActivityResult() export writeGpxFile "
											+ e.getMessage());
						}
					}
				}
			}
			if (requestCode == REQUEST_SAVE) {
				File[] files = new File(getCacheDir(), "").listFiles();
				for (File f : files) {
					String filename = f.getName();
					if (filename.endsWith(".sav") || filename.endsWith(".log")) {
						InputStream in = null;
						OutputStream out = null;
						try {
							in = new FileInputStream(f);
							out = new FileOutputStream(filePath + "/"
									+ filename);
							byte[] buffer = new byte[1024];
							int read;
							while ((read = in.read(buffer)) != -1) {
								out.write(buffer, 0, read);
							}

							in.close();
							in = null;
							out.flush();
							out.close();
							out = null;
						} catch (Exception e) {
							Log.i("MyInfoActivity",
									"onActivityResult() save writeGpxFile "
											+ e.getMessage());
						}
					}
				}
			}
			Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();

		} else if (resultCode == Activity.RESULT_CANCELED) {

		}

	}

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

	public static CopyOnWriteArrayList<Poi> PoiList = new CopyOnWriteArrayList<Poi>();
	public static boolean Converting = false;

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
	final int HistoryItemId=1;
	final int AboutItemId = 2;
	final int ExitMenuItemId = 3;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem DebugModeItem = menu.add(Menu.NONE, DebugModeMenuItemId,
				Menu.NONE, R.string.Debugmode);
		DebugModeItem.setCheckable(true);
		DebugModeItem.setChecked(DebugMode);

		menu.add(Menu.NONE, HistoryItemId, Menu.NONE, R.string.HistoryDays);
		
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
		case HistoryItemId:
			EditText et;
			Button button;
			popUp = new Dialog(MyInfoActivity.this);
			popUp.setContentView(R.layout.popupdialog);
			popUp.setTitle("How Many days back:");
			popUp.setCancelable(true);

			et = (EditText) popUp.findViewById(R.id.Value);
			et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
			et.setText("0");

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
					String Info = ((TextView) parent.findViewById(R.id.Value))
							.getText().toString();
					try {
						DaysBack = Integer.parseInt(Info);
					} catch (NumberFormatException e) {
						Log.i("RunningActivity", "Warning secs NumberFormatException - "+e.getMessage());
					}

					popUp.dismiss();
				}
			});

			popUp.show();
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
		ed.putInt("DaysBack", DaysBack);
		ed.commit();
	}

}