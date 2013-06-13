package org.rapidandroid.activity;

import org.rapidandroid.data.RapidSmsDBConstants;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SurveyView extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout surveyList = new LinearLayout(this);
		surveyList.setOrientation(LinearLayout.VERTICAL);
		ContentResolver contentResolver = getContentResolver();
		Cursor allSurveys = contentResolver.query(RapidSmsDBConstants.Survey.CONTENT_URI, 
				null, 
				null, 
				null, 
				null);
		if (allSurveys.getCount() == 0) {
			TextView surveyNameText = new TextView(this);
			surveyNameText.setTextSize(24);
			surveyNameText.setText("No surveys have yet been created.");
			surveyNameText.setTypeface(null, Typeface.BOLD);
			surveyList.addView(surveyNameText);
		} else {
			allSurveys.moveToFirst();
			while (!allSurveys.isAfterLast()) {
				
				// Survey name
				String surveyName = allSurveys.getString(allSurveys.getColumnIndex("surveyname"));
				final int surveyId = allSurveys.getInt(allSurveys.getColumnIndex("_id"));
				TextView surveyNameText = new TextView(this);
				surveyNameText.setTextSize(24);
				surveyNameText.setText(surveyId + ". " + surveyName.replace("'", ""));
				surveyNameText.setTypeface(null, Typeface.BOLD);
				surveyList.addView(surveyNameText);
				
				// Questions
				Cursor questions = contentResolver.query(RapidSmsDBConstants.Form.CONTENT_URI, 
						null, "survey_id = " + surveyId, 
						null, null);
	
				questions.moveToFirst();
				for (int i = 1; i < questions.getCount(); i++) {
					TextView question = new TextView(this);
					question.setTextSize(18);
					question.append(i + ". " + questions.getString(questions.getColumnIndex("description")));
					surveyList.addView(question);
					questions.moveToNext();
				}
				TextView question = new TextView(this);
				question.setTextSize(18);
				question.append(questions.getCount() + ". " + questions.getString(questions.getColumnIndex("description")));
				surveyList.addView(question);
				
				Button button = new Button(this);
				button.setText("Send Survey " + surveyId + " to More Contacts");
				button.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(SurveyView.this, QuestionVerifier.class);
						intent.putExtra("survey_id", surveyId);
						startActivity(intent);
					}
					
				});
				button.setPadding(0, 4, 0, 10);
				surveyList.addView(button);
				allSurveys.moveToNext();
			}
		}
		setContentView(surveyList);
	}
	
}
