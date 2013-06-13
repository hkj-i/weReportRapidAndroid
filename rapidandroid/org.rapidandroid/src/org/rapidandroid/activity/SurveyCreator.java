package org.rapidandroid.activity;
import org.rapidandroid.data.RapidSmsDBConstants;

import org.rapidandroid.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class SurveyCreator extends Activity {
//TODO i think a lot of my oncreates are public
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.survey_creator);
		
		EditText location = (EditText) findViewById(R.id.survey_location);
		if (getIntent().getExtras().containsKey("location")) {
			location.setText(getIntent().getExtras().getString("location"));
		}
	}
	
	public void createSurvey(View view) {
		Log.i("hi!", "in createSurvey");
		String surveyName  = ((EditText) findViewById(R.id.survey_name)).getText().toString();
		String description  = ((EditText) findViewById(R.id.survey_description)).getText().toString();
		Log.i("SurveyCreator", "survey name "+ surveyName);
		
		ContentResolver resolver = getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put("surveyname", surveyName);
		//TODO need to add field for description
		cv.put("description", description);
		
		String projectName = (String) getIntent().getExtras().get("project_name");
		Log.i("SurveyCreator", "getting project name " + projectName);
		
		
		if (!projectName.equals("No Project ID")) {
			
			cv.put("project_id", projectName);
			 
		}
		
		cv.put("phase", getIntent().getExtras().getInt("phase"));
		
		String loc = ((EditText) findViewById(R.id.survey_location)).getText().toString();
		
		
		//resolver.insert(RapidSmsDBConstants.Survey.CONTENT_URI, cv);
		Intent intent = new Intent(this, QuestionChooser.class);
		
		if (description != null && !description.equals("")) {
			intent.putExtra("surveydescription", description);
		}
		if (loc == null || loc.equals("") || loc.equals("Location")) {
			Toast toast = Toast.makeText(this, "Please enter a location.", Toast.LENGTH_LONG);
			toast.show();
		} else {
			cv.put("location", loc);
			intent.putExtra("surveylocation", loc);
			intent.putExtras(getIntent().getExtras());
			intent.putExtra("surveyname", surveyName);
			//Log.i("SurveyCreator", "starting intent");
			startActivity(intent);
		}
			
	}
	
}
