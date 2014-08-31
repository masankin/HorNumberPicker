package com.example.androidscroll;

import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	HorNumberPicker numberPicker;
	int values[] = new int[100];
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		numberPicker = (HorNumberPicker)findViewById(R.id.boundLayout);
		for(int index = 0; index < 100; index ++){
			values[index] = index;
		}
		numberPicker.setValues(values);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
