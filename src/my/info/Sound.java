package my.info;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class Sound {
	@SuppressWarnings("rawtypes")
	private static HashMap soundPoolMap=null;
	private static SoundPool soundPool=null;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<String> initSounds(Context context) {
		List<String> retVal = new ArrayList<String>();
		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		soundPoolMap = new HashMap(3);

		Class raw = R.raw.class;
		Field[] fields = raw.getFields();
		int i = 0;
		for (Field field : fields) {
			try {
				soundPoolMap
						.put(i, soundPool.load(context, field.getInt(null), i));
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
			return;
		}
		float volume = (float) 1.0;

		soundPool.play((Integer) soundPoolMap.get(soundID), volume, volume, 1,
				0, 1f);
	}
	
	public Sound()
	{
		
	}

}
