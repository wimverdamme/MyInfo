package my.info;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundActivity extends Activity {

	@SuppressWarnings("rawtypes")
	private static HashMap soundPoolMap;
	private static SoundPool soundPool;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<String> initSounds() {
		List<String> retVal = new ArrayList<String>();
		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		soundPoolMap = new HashMap(3);

		Class raw = R.raw.class;
		Field[] fields = raw.getFields();
		int i = 0;
		for (Field field : fields) {
			try {
				soundPoolMap
						.put(i, soundPool.load(this, field.getInt(null), i));
				retVal.add(field.getName());
				i++;
				Log.i("REFLECTION",
						String.format("%s is %d", field.getName(),
								field.getInt(null)));
			} catch (IllegalAccessException e) {
				Log.e("REFLECTION",
						String.format("%s threw IllegalAccessException.",
								field.getName()));
			}
		}
		return retVal;
	}

	public void playSound(int soundID) {
		if (soundPool == null || soundPoolMap == null) {
			initSounds();
		}
		float volume = (float) 1.0;

		soundPool.play((Integer) soundPoolMap.get(soundID), volume, volume, 1,
				0, 1f);
	}

	private EditText et;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		List<String> sounds = this.initSounds();

		ScrollView sv = new ScrollView(this);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		sv.addView(ll);


		int i = 0;
		for (String e : sounds) {
			Button b = new Button(this);
			b.setText(e);
			b.setOnClickListener(new SoundButtonOnClickListenener(i));

			ll.addView(b);
			i++;
		}

		this.setContentView(sv);
	}

	private class SoundButtonOnClickListenener implements View.OnClickListener {
		private int ID;

		public SoundButtonOnClickListenener(int ID) {
			this.ID = ID;
		}

		public void onClick(View v) {
			playSound(ID);
		}
	}

}
