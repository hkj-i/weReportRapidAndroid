package org.rapidandroid.activity;

import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidandroid.data.SurveyCreationConstants;
import org.rapidandroid.question.Question;
import org.rapidandroid.question.QuestionBank;
import org.rapidsms.java.core.model.Field;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.parser.service.ParsingService.ParserType;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class QuestionChooser extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.question_chooser);
		
		Bundle extras = getIntent().getExtras();
		
		
	    ScrollView sv = new ScrollView(this);
	    final LinearLayout ll = new LinearLayout(this);
	    ll.setOrientation(LinearLayout.VERTICAL);
	    
		
		
		QuestionBank questionBank;
		if (!extras.containsKey("phase")) {
			// TODO throw some error
			questionBank = new QuestionBank(extras.getInt("phase"));
		} else {

			questionBank = new QuestionBank(extras.getInt("phase"));

		}
		
		int questionNumber = 0;
		for (Question question: questionBank.getQuestions()) {
			LinearLayout vg = new LinearLayout(this);
			LayoutParams lps = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			
			vg.setLayoutParams(lps);
			vg.setId(questionNumber);
			Log.i("ajfkafka", "this is a question!");
			
			CheckBox checkBox = new CheckBox(this);
			checkBox.setId(questionNumber);
			vg.addView(checkBox);
			
			String[] questionParts = question.getQuestionText().split(Question.PROJECT_INPUT_TAG);
			for (int i = 0; i < questionParts.length - 1; i++) {
				
				TextView text = new TextView(this);
				text.setText(questionParts[i]);
				if (i == 0) text.append("\n");
				vg.addView(text);
				
				
				EditText projectEntry = new EditText(this);
				LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				projectEntry.setLayoutParams(params);
				projectEntry.setHint("project");
				
				vg.addView(projectEntry);
				
			}
			
			TextView text = new TextView(this);
			text.setText(questionParts[questionParts.length-1]);
			vg.addView(text);
			ll.addView(vg);
			
			questionNumber++;
		}
		
		Button nextButton = new Button(this);
		nextButton.setText("Next");
		nextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				QuestionBank questionBank2 = new QuestionBank(3);
				
				Intent intent = new Intent(QuestionChooser.this, QuestionVerifier.class);
				Question[] questions = new Question[questionBank2.getQuestions().size()];
				Log.i("QuestionChooser", "questions[] length " + questions.length);
				int questionNumber = 0;
				
				
				boolean atLeastOneQuestionChecked = false;
				int surveyId = -1;
				
				Log.i("QuestionChooser", "ll child count " + ll.getChildCount());
				for (int i = 0; i < ll.getChildCount(); i++) {
					if (ll.getChildAt(i).getClass().equals(LinearLayout.class)) {
						ViewGroup vg = (ViewGroup) ll.getChildAt(i);
						
						for (int k = 0; k < vg.getChildCount(); k++) {
							Log.i("QuestionChooser", "child " + k  + " id " + vg.getChildAt(k).getId());
						}
						View v = vg.findViewById(i);
						Log.i("QuestionChooser", "finding id " + i + " " + v);
						
						
						Log.i("QuestionChooser", "vg child count " + vg.getChildCount());
						
						
						
						
						if (((CheckBox) vg.getChildAt(0)).isChecked()) {
							
							atLeastOneQuestionChecked = true;
							
							Log.i("QuestionChooser", "got through checkbox clicked");
							String newQuestionText = "";
							for (int j = 1; j < vg.getChildCount(); j++) {
								newQuestionText += ((TextView) vg.getChildAt(j)).getText();
							}
							Log.i("QuestionChooser", "new question: " +newQuestionText);
							Log.i("QuestionBankSize", ""+questionBank2.getQuestions().size());
							Log.i("i", ""+i);
							Log.i("qnum", ""+questionNumber);
							Question question = questionBank2.getQuestions().get(i);
							question.setQuestionText(newQuestionText);
							questions[questionNumber] = question;
							questionNumber++;
							
							Log.i("QuestionChooser", "hello!");
							ContentResolver resolver = getContentResolver();
							Log.i("QuestionChooser", "querying surveyname \'" + getIntent().getExtras().getString("surveyname") + "\'");

							String[] selectionArgs = {"\'" + getIntent().getExtras().getString("surveyname") + "\'"};
							Cursor surveyRow = resolver.query(RapidSmsDBConstants.Survey.CONTENT_URI,
																null, 
																"surveyname = ?",
																selectionArgs, null);
							Log.i("QuestionChooser", "querying surveyname \'" + getIntent().getExtras().getString("surveyname") + "\'");
							
							surveyRow.moveToFirst();
							Log.i("moved to first", "");
							if (surveyRow.getCount() == 0) Log.i("oops!", "");
							surveyId = surveyRow.getInt(surveyRow.getColumnIndex("_id"));
							
							String prefix = "@survey" + surveyId + "q" + questionNumber;
							Log.i("QuestionChooser", "prefix " + prefix);
							String formName = getIntent().getExtras().getString("surveyname") + " Question " + questionNumber;
							Log.i("QuestionChooser", "formName " + formName);
							
							String description = newQuestionText + " Reply \"" + prefix + " ";
							if (question.getQuestionType() == SurveyCreationConstants.QuestionTypes.YESNO) {
								description += "[yes/no]\"";
							} else if (question.getQuestionType() == SurveyCreationConstants.QuestionTypes.RATING) {
								description += "[1-10]\"";
							}
							//description += question.getFields().get(0).getFieldType().getReadableName() + "\"";
							
							Log.i("QuestionChooser", "description " + description);
							Form formToSave = new Form();
							formToSave.setFormName(formName);
							formToSave.setPrefix(prefix);
							formToSave.setDescription(description);
							
							Field[] fieldArray = {new Field()};
							
							fieldArray[0].setDescription(newQuestionText);
							fieldArray[0].setFieldId(-1);
						
							Log.i("fieldtype", "" + question.getFields().get(0).getFieldId());
							/*if (question.getFields().get(0).getFieldId() == 7) {
								fieldArray[0].setName("Answer [yes/no]");
							} else if (question.getFields().get(0).getFieldId() == 1) {
								fieldArray[0].setName("Rating [1-10]");
							}
							*/
							fieldArray[0].setName(question.getFields().get(0).getName());
							fieldArray[0].setSequenceId(1); // TODO this is hardcoded, bad!!
							fieldArray[0].setFieldType(question.getFields().get(0).getFieldType());
							formToSave.setFields(fieldArray);

							formToSave.setParserType(ParserType.SIMPLEREGEX);
							try {
								ModelTranslator.addFormToDatabase(formToSave);
							} catch (Exception ex) {
								
							}
							Log.i("QuestionChooser", "updating form db");
							
							ContentValues cv = new ContentValues();
							cv.put("survey_id", surveyId);
							cv.put("sequence", questionNumber);
							cv.put("question_type", question.getQuestionType());
							String[] args = {prefix};
							int affected = resolver.update(RapidSmsDBConstants.Form.CONTENT_URI, 
												cv, 
												"prefix = ?", 
												args);
							Log.i("affected rows", "" +affected);
						}
					}
				}
				if (atLeastOneQuestionChecked) {
					intent.putExtras(getIntent().getExtras());
					intent.putExtra("survey_id", surveyId);
					startActivity(intent);
				} else {
					// TODO error message
				}
			}
			
		});
		ll.addView(nextButton);
		sv.addView(ll);
		this.setContentView(sv);
	}
	
	
	
}
