package org.rapidandroid.activity;

/**
 * Pulls up the question bank and displays the questions on the screen
 * in text views
 */
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
import android.widget.Toast;

public class QuestionChooser extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.question_chooser);

		Bundle extras = getIntent().getExtras();


	    ScrollView sv = new ScrollView(this);
	    
	    // ll is the main linear layout that contains all questions, next button, etc.
	    final LinearLayout ll = new LinearLayout(this);
	    ll.setOrientation(LinearLayout.VERTICAL);

	    TextView intro = new TextView(this);
	    String introString = "Please choose at least one question from the following.  " +
	    				"For the questions selected, please fill in the blanks with " +
	    				"the desired information.  ";
	    
	    int phase = -1;
	    if (extras.containsKey("phase")) {
	    	phase = extras.getInt("phase");
	    } else {
	    	return;
	    }
	    TextView exampleTV = new TextView(this);
	    String example = "";
	    if (phase == SurveyCreationConstants.ANALYSIS) {
	    	example = "For example, \"Rate your participation in building the well.\"";
	    } else if (phase == SurveyCreationConstants.SCOPING) {
	    	introString += "For SELECT questions, please fill in at least two options.";
	    	example = "For example, \"Select the most important community project from " +
					"the following options: 1. well, 2. latrine.\"";
	    }
	    
	    intro.setText(introString);
	    intro.setTextSize(20);
	    intro.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	    intro.setPadding(0,0,0,20);
	    ll.addView(intro);
	    
	    if (!example.equals("")) {
	    	exampleTV.setText(example);
	    	exampleTV.setTextSize(20);
	    	exampleTV.setPadding(0,0,0,20);
	    	ll.addView(exampleTV);
	    }
	    // initialize the question bank based on the phase
		QuestionBank questionBank;
		questionBank = new QuestionBank(phase);


		// We're on question 0 to start with
		int questionNumber = 0;
		
		// For each question
		for (Question question: questionBank.getQuestions()) {
			
			// The whole block is the vertical linear layout that contains the entire question
			LinearLayout wholeBlock = new LinearLayout(this);
			wholeBlock.setId(questionNumber);
			
			wholeBlock.setOrientation(LinearLayout.VERTICAL);
			
			// vg is the viewgroup that contains the first bit of text, usually the bulk of the question.
			LinearLayout vg = new LinearLayout(this);

			// checkbox
			CheckBox checkBox = new CheckBox(this);
			vg.addView(checkBox);

			// Split the question on it's blanks
			String[] questionParts = question.getQuestionText().split(Question.PROJECT_INPUT_TAG);
			
			// Add the first bit of text
			if (questionParts.length > 0) {
				TextView text = new TextView(this);
				text.setText(questionParts[0]);
				//text.setTextSize(20);
				vg.addView(text);
			}
			 wholeBlock.addView(vg);
			 
			int number = 1;
			for (int i = 1; i < questionParts.length; i++) {

				Log.i("QuestionChooser", "processing question part " + i +": " + questionParts[i]);
				
				EditText projectEntry = new EditText(this);
				TextView text = new TextView(this);
				TextView text2 = new TextView(this);
				//text.setTextSize(20);
				//text2.setTextSize(20);
				if (questionParts[i].length() > 40) {
					
					
					
					text.setText(questionParts[i].substring(0, questionParts[i].indexOf(" ", 32)));
					text2.setText(questionParts[i].substring(questionParts[i].indexOf(" ", 32)));
				} else {
					text.setText(questionParts[i]);
				}
				projectEntry.setHint("project");

				
				
				
				if (question.getQuestionType() == SurveyCreationConstants.QuestionTypes.MULTIPLECHOICE) {
					LinearLayout line = new LinearLayout(this);
					TextView numberText = new TextView(this);
					numberText.setText(" " + number + ". ");
					line.addView(numberText);
					line.addView(projectEntry);
					
					if (number == 4) {
						TextView period = new TextView(this);
						period.setText(".");
						//period.setTextSize(20);
						line.addView(period);
					} else {
						TextView space = new TextView(this);
						space.setText(", ");
						//space.setTextSize(20);
						line.addView(space);
					}
					
					wholeBlock.addView(line);
					vg = line;
					number++;

				} else if (questionParts[i-1].length() > 25 ) {
				
					LinearLayout line = new LinearLayout(this);
					
					line.addView(projectEntry);

					line.addView(text);
					
					wholeBlock.addView(line);
					vg = line;
					
					if (questionParts[i].length() > 40) {
						LinearLayout line2 = new LinearLayout(this);
						line2.addView(text2);
						wholeBlock.addView(line2);
					}
					
				} else {
					Log.i("QuestionChooser", "adding project entry and text to vg");
					vg.addView(projectEntry);
					vg.addView(text);
				}
				
				

			}

			
			ll.addView(wholeBlock);

			questionNumber++;
		}

		Button nextButton = new Button(this);
		nextButton.setText("Next");
		nextButton.setPadding(0, 20, 0, 20);
		nextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				QuestionBank questionBank2 = new QuestionBank(getIntent().getExtras().getInt("phase"));

				Intent intent = new Intent(QuestionChooser.this, QuestionVerifier.class);
				Question[] questions = new Question[questionBank2.getQuestions().size()];
				Log.i("QuestionChooser", "questions[] length " + questions.length);
				int questionNumber = 0;


				boolean atLeastOneQuestionChecked = false;
				int surveyId = -1;

				// Save our survey now.
				ContentResolver resolver = getContentResolver();
				ContentValues cv1 = new ContentValues();
				cv1.put("location", getIntent().getExtras().getString("surveylocation"));
				cv1.put("surveyname", getIntent().getExtras().getString("surveyname"));
				cv1.put("phase", getIntent().getExtras().getInt("phase"));
				if (getIntent().getExtras().containsKey("description")) {
					cv1.put("description", getIntent().getExtras().getString("description"));
				}
				resolver.insert(RapidSmsDBConstants.Survey.CONTENT_URI, cv1);
				
				
				
				
				Log.i("QuestionChooser", "ll child count " + ll.getChildCount());
				for (int i = 0; i < questionBank2.getQuestions().size(); i++) {
					//if (ll.getChildAt(i).getClass().equals(LinearLayout.class)) {
						//ViewGroup vg = (ViewGroup) ll.getChildAt(i);

						/*for (int k = 0; k < vg.getChildCount(); k++) {
							Log.i("QuestionChooser", "child " + k  + " id " + vg.getChildAt(k).getId());
						}*/
						ViewGroup v = (ViewGroup) findViewById(i);
						Log.i("QuestionChooser", "finding id " + i + " " + v);


						//Log.i("QuestionChooser", "vg child count " + vg.getChildCount());




						if (((CheckBox) ((ViewGroup) v.getChildAt(0)).getChildAt(0)).isChecked()) {

							atLeastOneQuestionChecked = true;

							Log.i("QuestionChooser", "got through checkbox clicked");
							String newQuestionText = "";
							int j;
							boolean innerLoopBroken = false;
							for (j = 0; j < v.getChildCount(); j++) {
								for (int k = 0; k < ((ViewGroup) v.getChildAt(j)).getChildCount(); k++) {
									
									if (k+1 < ((ViewGroup) v.getChildAt(j)).getChildCount())
										Log.i("QuestionChooser", "k+1 valid");
									
									//if (((TextView) ((ViewGroup) v.getChildAt(j)).getChildAt(k+1)).getText() == null)
										//Log.i("QuestionChooser", "text null");
									/*int z = k + 1;
									if (z < ((ViewGroup) v.getChildAt(j)).getChildCount() &&
											((TextView) ((ViewGroup) v.getChildAt(j)).getChildAt(z)).getText() == null) {
										innerLoopBroken = true;
										Log.i("QuestionChooser", "breaking inner loop");
										break;
									}*/
									//if (k+1 < ((ViewGroup) v.getChildAt(i)).getChildCount())
										//Log.i("QuestionChooser", (String) ((TextView) ((ViewGroup) v.getChildAt(j)).getChildAt(k+1)).getText());
									
									if (!(j == 0 && k == 0)) {
										Log.i("QuestionChooser", "j = " + j + " k = " + k);
										newQuestionText += ((TextView) ((ViewGroup) v.getChildAt(j)).getChildAt(k)).getText();
										
										if (("" + ((TextView) ((ViewGroup) v.getChildAt(j)).getChildAt(k)).getText()).equals("")) {
											Log.i("QuestionChooser", "evaluated to true!!!!!!!!");
											newQuestionText = newQuestionText.substring(0, newQuestionText.length() - 4);
											innerLoopBroken = true;
											break;
										}
										
										if (((TextView) ((ViewGroup) v.getChildAt(j)).getChildAt(k)).getText() != null)
											Log.i("QuestionChooser", ""+ ((TextView) ((ViewGroup) v.getChildAt(j)).getChildAt(k)).getText());
									}
								}
								if (innerLoopBroken)
									break;
							}
							
							if (newQuestionText.contains("1.") && !newQuestionText.contains("4.") ) {
								char[] newQuestionChar = newQuestionText.toCharArray();
								newQuestionChar[newQuestionText.lastIndexOf(",")] = '.';
								newQuestionText = new String(newQuestionChar);
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
							
							Log.i("QuestionChooser", "querying surveyname " + getIntent().getExtras().getString("surveyname"));

							String[] selectionArgs = {getIntent().getExtras().getString("surveyname") };
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

							String description = newQuestionText + " Reply " + prefix + " ";
							if (question.getQuestionType() == SurveyCreationConstants.QuestionTypes.YESNO) {
								description += "[yes/no]";
							} else if (question.getQuestionType() == SurveyCreationConstants.QuestionTypes.RATING) {
								description += "[1-10]";
							} else if (question.getQuestionType() == SurveyCreationConstants.QuestionTypes.MULTIPLECHOICE) {
								j--;
								description += "[1-" + j + "]";
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
						//}
					}
				}
				if (atLeastOneQuestionChecked) {
					intent.putExtras(getIntent().getExtras());
					intent.putExtra("survey_id", surveyId);
					startActivity(intent);
				} else {
					Toast toast = Toast.makeText(QuestionChooser.this, "Please choose at least one question.", Toast.LENGTH_LONG);
					toast.show();
				}
			}

		});
		ll.addView(nextButton);
		sv.addView(ll);
		this.setContentView(sv);
	}



}