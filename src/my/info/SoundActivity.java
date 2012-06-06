package my.info;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class SoundActivity extends Activity {


	private Sound sound;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sound = new Sound();
		List<String> sounds = sound.initSounds(this);

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
			sound.playSound(ID);
		}
	}

}
