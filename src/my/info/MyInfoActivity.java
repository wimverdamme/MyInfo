package my.info;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import my.info.Poi;

public class MyInfoActivity extends Activity {

	private boolean convertionDone = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	private final String backupFilename = "store.bak";

	public void onButtonSaveClick(View view) {
		File backupFile = new File(getCacheDir(), backupFilename);
		if (backupFile.exists())
			backupFile.delete();
		try {
			backupFile.createNewFile();
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(backupFile));
			os.writeObject(PoiList.toArray(new Poi[0]));
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onButtonLoadClick(View view) {
		File backupFile = new File(getCacheDir(), backupFilename);
		if (backupFile.exists())		
			try {
				ObjectInputStream is=new ObjectInputStream(new FileInputStream(backupFile));
				
				PoiList = new ArrayList<Poi>(Arrays.asList((Poi[])is.readObject()));
				

				if (PoiList.size() > 0)
				{
					convertionDone=true;
					((TextView) findViewById(R.id.NrOfPoisFound)).setText(""+PoiList.size());
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}



	}

	public void onButtonNoClick(View view) {
		Toast.makeText(this, "Button No clicked!", Toast.LENGTH_SHORT).show();
		finish();
	}

	public void onButtonYesClick(View view) {
		if (!convertionDone)
			return;
		Intent intent = new Intent(this, RunningActivity.class);
		intent.putExtra("Extended", false);
		startActivity(intent);
	}

	public void onButtonAllClick(View view) {
		if (!convertionDone)
			return;
		Intent intent = new Intent(this, RunningActivity.class);
		intent.putExtra("Extended", true);
		startActivity(intent);
	}

	public static ArrayList<Poi> PoiList = new ArrayList<Poi>();
	public static boolean Converting = false;

	public void onButtonConvertClick(View view) {

		if (!Converting) {
			PoiList.clear();
			Converting = true;

			new Thread(new Runnable() {
				public void run() {
					String list[] = {};

					String path = "";
					AssetManager mgr = getAssets();
					final TextView tv = ((TextView) findViewById(R.id.SelectedFile));
					final TextView tv2 = ((TextView) findViewById(R.id.NrOfPoisFound));
					try {
						list = mgr.list(path);
						for (String f : list)
							if (f.toLowerCase().endsWith(".csv")) 
							{

								// ((TextView)
								// findViewById(R.id.SelectedFile)).setText(f);

								final String text = f;
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
						;
					}

					tv.post(new Runnable() {
						public void run() {
							tv.setText("");
						}
					});
					if (PoiList.size() > 0)
						convertionDone = true;
					Converting = false;
				}
			}).start();
		}

	}
	final int DebugModeMenuItemId=0;
	final int ExitMenuItemId=1;
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
		MenuItem DebugModeItem=menu.add(Menu.NONE,DebugModeMenuItemId,Menu.NONE,R.string.Debugmode);
		DebugModeItem.setCheckable(true);
		DebugModeItem.setChecked(DebugMode);
		
		menu.add(Menu.NONE,ExitMenuItemId,Menu.NONE,R.string.Exit);
	    return true;
    }
	private boolean DebugMode=false;
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		switch (item.getItemId())
		{
			case DebugModeMenuItemId:				
				DebugMode=!item.isChecked();
				item.setChecked(DebugMode);
				break;
			case ExitMenuItemId:
				System.exit(0);
				break;
		}

			
		return super.onOptionsItemSelected(item);
    }

}