package my.info;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SaveDialog extends Activity {
	public static final String RESULT_SELECTED = "RESULT_SELECTED";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		File[] files = new File(getCacheDir(), "").listFiles();
		int count = 0;
		for (File f : files)
			if (f.getName().toLowerCase().endsWith(".sav"))
				count++;

		String[] values = new String[count];
		count = 0;
		for (File f : files)
			if (f.getName().toLowerCase().endsWith(".sav"))
				values[count++] = f.getName().substring(0,
						f.getName().length() - 4);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, values);
		ListView lv = new ListView(this);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				view.setSelected(!view.isSelected());
			}
		});
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		ll.addView(lv);
		Button b = new Button(this);
		b.setText("Select");
		b.setClickable(true);
		b.setOnClickListener(new MyOnClickListener(lv));

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, lv.getId());
		b.setLayoutParams(params);

		ll.addView(b);

		this.setContentView(ll);

		this.setContentView(ll);
	}

	private class MyOnClickListener implements OnClickListener {
		private ListView lv;

		public MyOnClickListener(ListView lv) {
			super();
			this.lv = lv;
		}

		public void onClick(View v) {
 
		
			SparseBooleanArray checkedItems = lv.getCheckedItemPositions();
			final int checkedItemsCount = checkedItems.size();
			String[] Selected=new String[checkedItemsCount];
			for (int i = 0; i < checkedItemsCount; ++i) {
				final int position = checkedItems.keyAt(i);
				//final boolean isChecked = checkedItems.valueAt(i);				
				Selected[i]=(String)lv.getItemAtPosition(position)+".sav";				
			}			
			
			getIntent().putExtra(RESULT_SELECTED,Selected);
			setResult(RESULT_OK, getIntent());
			finish();

		}
	}

}
