package org.rapidandroid.activity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidandroid.data.SurveyCreationConstants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class QuestionVerifier extends Activity {

	ArrayList<String> phoneNumbers = null;
	String[] mphoneNumbers = null;
	ArrayList<String> mSelectedNumbers = null;
	ArrayList<String> mTextMessages = null;


	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		mTextMessages = new ArrayList<String>();

		//Question[] questions = (Question[]) getIntent().getExtras().get("questions");

		ScrollView sv = new ScrollView(this);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);

		TextView verify = new TextView(this);
		verify.setText("Are these the questions you wish to send?");
		ll.addView(verify);

		ContentResolver resolver = getContentResolver();
		Cursor questions = resolver.query(RapidSmsDBConstants.Form.CONTENT_URI, 
				null, "survey_id = " + getIntent().getExtras().getInt("survey_id"), 
				null, null);

		questions.moveToFirst();
		for (int i = 1; i < questions.getCount(); i++) {
			TextView question = new TextView(this);
			question.append(i + ". " + questions.getString(questions.getColumnIndex("description")));


			ll.addView(question);
			questions.moveToNext();
		}
		TextView question = new TextView(this);
		question.append(questions.getCount() + ". " + questions.getString(questions.getColumnIndex("description")));


		ll.addView(question);


		phoneNumbers = new ArrayList<String>();


		Button contactsButton = new Button(this);
		contactsButton.setText("Choose Contacts");
		contactsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("OnClick_loadContact", "clicked contactListener");

				// place to store the items
				mSelectedNumbers = new ArrayList<String>();
		        new AlertDialog.Builder(QuestionVerifier.this)
		        .setTitle("Contact List")
		        .setMultiChoiceItems(mphoneNumbers, null, new DialogInterface.OnMultiChoiceClickListener() {      
		           public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		               if (isChecked) {
		                   // If the user checked the item, add it to the selected items
		                   mSelectedNumbers.add(mphoneNumbers[which]);
		                   Log.i("mSelected Numbers: add number", mphoneNumbers[which] + " added");
		               } else if (mSelectedNumbers.contains(mphoneNumbers[which])) {
		                   // Else, if the item is already in the array, remove it 
		            	   mSelectedNumbers.remove(mphoneNumbers[which]);
		            	   Log.i("mSelected Numbers: remove number", mphoneNumbers[which] + " removed");
		               }
		           }
			     })
		        .setPositiveButton("Select", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) { 
		               // return
		            }
		         })
		        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) { 
		                //clear the selected numbers
		            	mSelectedNumbers = null;
		            	Log.i("mSelected Numbers:cancel", "mSelected Numbers cleared");
		            }
		         })
		         .show();

			}

		});
		ll.addView(contactsButton);

		Button sendButton = new Button(this);
		sendButton.setText("Send");
		sendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ContentResolver resolver = getContentResolver();
				Cursor questions = resolver.query(RapidSmsDBConstants.Form.CONTENT_URI, 
						null, "survey_id = " + getIntent().getExtras().getInt("survey_id"), 
						null, null);


				questions.moveToFirst();

				while (!questions.isAfterLast()) {

					 // Send message
					mTextMessages.add(questions.getString(questions.getColumnIndex("description")).replace("\"", "\\\""));

					String surveyName = questions.getString(questions.getColumnIndex("formname"));

					Cursor answers = resolver.query(RapidSmsDBConstants.Field.CONTENT_URI, 
							null, "form_id = " + questions.getInt(questions.getColumnIndex("_id")), 
							null, null);

					answers.moveToFirst();
					String prompt = answers.getString(answers.getColumnIndex("prompt"));
					String fields = "";	
					int questionType = questions.getInt(questions.getColumnIndex("question_type"));
					if (questionType == SurveyCreationConstants.QuestionTypes.RATING) {
						fields += SurveyCreationConstants.xForm.getNumFieldXml(1, prompt);
					} else if (questionType == SurveyCreationConstants.QuestionTypes.YESNO) {
						String[] labels = {"Yes", "No"};
						fields += SurveyCreationConstants.xForm.getSelectFieldXml(1, prompt, labels);
					}

					String[] selectionArgs = {surveyName};
					Cursor formResult = resolver.query(Uri.parse("content://org.odk.collect.android.provider.odk.forms/forms"), 
							null, 
							"displayName = ?", 
							selectionArgs, 
							null);
					//questions.moveToFirst();
					if (formResult.getCount() == 0) {
						// Now we need to generate the XForms for ODK Collect
						String xForm = SurveyCreationConstants.xForm.xForm;
						xForm = xForm.replace("SURVEY_NAME", surveyName);
						xForm = xForm.replace("FIELDS", fields); // TODO a lot could go wrong w/ this
						// copied from Android dev external storage page
						boolean mExternalStorageAvailable = false;
						boolean mExternalStorageWriteable = false;
						String state = Environment.getExternalStorageState();

						if (Environment.MEDIA_MOUNTED.equals(state)) {
							// We can read and write the media
							mExternalStorageAvailable = mExternalStorageWriteable = true;
						} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
							// We can only read the media
							mExternalStorageAvailable = true;
							mExternalStorageWriteable = false;
						} else {
							// Something else is wrong. It may be one of many other states, but all we need
							//  to know is we can neither read nor write
							mExternalStorageAvailable = mExternalStorageWriteable = false;
						}

						if (!mExternalStorageWriteable) {
							Log.e("SaveXml","External storage not writeable");
						}
						if (!mExternalStorageAvailable) {
							Log.e("SaveXml","External storage not available");
						}

						File externalStorageDir = Environment.getExternalStorageDirectory();
						try {
							// String filename = path + File.separator +
							// path.substring(path.lastIndexOf(File.separator) + 1) + ".xml";

							FileWriter fw = new FileWriter(externalStorageDir.getAbsoluteFile() + 
									"/odk/forms/" + surveyName.replace(" ", "_") + ".xml");
							fw.write(xForm);
							fw.flush();
							fw.close();

						} catch (IOException e) {
							Log.e("SaveXml","Error writing XML file");
							e.printStackTrace();
						}

						// And insert into ODK Collect DB
						ContentValues cv = new ContentValues();
						cv.put("displayName", surveyName);
						cv.put("jrFormId", "capstone_report");

						cv.put("formFilePath", externalStorageDir.getAbsoluteFile() + 
								"/odk/forms/" + surveyName.replace(" ", "_") + ".xml");
						cv.put("submissionUri", "https://capstone-wereport.appspot.com/submission");

						resolver.insert(Uri.parse("content://org.odk.collect.android.provider.odk.forms/forms"), 
								cv);

					}



				    questions.moveToNext();

				}




				//---sends an SMS message to another device---
				// parse through all contacts list and send SMS
				if (mSelectedNumbers != null) {
					for (int i = 0; i < mSelectedNumbers.size(); i++) {
						Log.i("sendSMS", "a total of " + mSelectedNumbers.size() + " contacts");
						try {
							SmsManager smsManager = SmsManager.getDefault();
							//message currently hard coded
							for (int j = 0; j < mTextMessages.size(); j++) {
								smsManager.sendTextMessage(mSelectedNumbers.get(i), null, mTextMessages.get(j), null, null);
								Log.i("Send SMS: Success", "message sent to " + mSelectedNumbers.get(i));
							}
								//Toast.makeText(getApplicationContext(), "SMS Sent!", Toast.LENGTH_LONG).show();
						  } catch (Exception e) {
							//Toast.makeText(getApplicationContext(), "SMS faild, please try again later!",Toast.LENGTH_LONG).show();
							Log.i("Send SMS: Failure", "couldn't send message to " + mSelectedNumbers.get(i));
						  }	    
					}
				}


				Intent intent = new Intent(QuestionVerifier.this, SurveySent.class);
				startActivity(intent);
			}

		});



		// get phone numbers and add them
		Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);


		while (phones.moveToNext())
		{
		  // may be duplicated if a name has multiple numbers
		  String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
		  String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		  phoneNumbers.add(phoneNumber);
		}

		phones.close();

		// make it into an array
		mphoneNumbers = new String[phoneNumbers.size()];
		for (int i = 0; i < phoneNumbers.size(); i++) {
			mphoneNumbers[i] = phoneNumbers.get(i);
			Log.i("phone number", mphoneNumbers[i]);
		}

		Log.i("number of phonenumbers: ", "total: " + mphoneNumbers.length);

		ll.addView(sendButton);
		sv.addView(ll);
		setContentView(sv);
	}


	public void onClick_loadContact(View v) {
		Log.i("OnClick_loadContact", "clicked contactListener");

		// place to store the items
		mSelectedNumbers = new ArrayList<String>();
        new AlertDialog.Builder(this)
        .setTitle("Contact List")
        .setMultiChoiceItems(mphoneNumbers, null, new DialogInterface.OnMultiChoiceClickListener() {      
           public void onClick(DialogInterface dialog, int which, boolean isChecked) {
               if (isChecked) {
                   // If the user checked the item, add it to the selected items
                   mSelectedNumbers.add(mphoneNumbers[which]);
                   Log.i("mSelected Numbers: add number", mphoneNumbers[which] + " added");
               } else if (mSelectedNumbers.contains(mphoneNumbers[which])) {
                   // Else, if the item is already in the array, remove it 
            	   mSelectedNumbers.remove(mphoneNumbers[which]);
            	   Log.i("mSelected Numbers: remove number", mphoneNumbers[which] + " removed");
               }
           }
	     })
        .setPositiveButton("Select", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
               // return
            }
         })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
                //clear the selected numbers
            	mSelectedNumbers = null;
            	Log.i("mSelected Numbers:cancel", "mSelected Numbers cleared");
            }
         })
         .show();
	}

 
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	*/



}