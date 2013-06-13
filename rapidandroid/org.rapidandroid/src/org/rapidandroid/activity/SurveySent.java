package org.rapidandroid.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SurveySent extends Activity {


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		TextView text = new TextView(this);
		text.setText("Thank you! Your survey has been successfully sent, and responses are being collected.");
		text.setTextSize(18);
		ll.addView(text);
		Button button = new Button(this);
		button.setText("View Responses");
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SurveySent.this, Dashboard.class);
				startActivity(intent);
			}

		});
		ll.addView(button);

		Button button2 = new Button(this);
		button2.setText("Home");
		button2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SurveySent.this, Main.class);
				startActivity(intent);
			}

		});
		ll.addView(button2);

		setContentView(ll);
	}

}