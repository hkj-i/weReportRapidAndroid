package org.rapidandroid.activity;

import org.rapidandroid.R;
import org.rapidandroid.data.SmsDbHelper;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

public class Main extends Activity {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SmsDbHelper mDbHelper = new SmsDbHelper(this);
		SQLiteDatabase mDb = mDbHelper.getReadableDatabase();
		mDbHelper.close();
		mDb.close();
		
		setContentView(R.layout.main_activity);

	
	}
	
	public void createSurvey(View view) {
		Intent intent = new Intent(this, ProjectChooser.class);
		startActivity(intent);
	}
	
	public void viewResponses(View view) {
		Intent intent = new Intent(this, Dashboard.class);
		startActivity(intent);
	}
	
	public void viewSurveys(View view) {
		Intent intent = new Intent(this, SurveyView.class);
		startActivity(intent);
	}
	
}
