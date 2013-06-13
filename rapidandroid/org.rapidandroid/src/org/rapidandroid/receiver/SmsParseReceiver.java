/*
 * Copyright (C) 2009 Dimagi Inc., UNICEF
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

/**
 * 
 */
package org.rapidandroid.receiver;

import java.util.Vector;

import org.rapidandroid.ApplicationGlobals;
import org.rapidandroid.content.translation.MessageTranslator;
import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidandroid.content.translation.ParsedDataTranslator;
import org.rapidandroid.content.translation.XMLTranslator;
import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidandroid.data.SurveyCreationConstants;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.model.Monitor;
import org.rapidsms.java.core.parser.IParseResult;
import org.rapidsms.java.core.parser.service.ParsingService;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Second level broadcast receiver. The idea is upon a successful SMS message
 * save, a separate receiver will be triggered to handle the actual parsing and
 * processing of the message.
 * 
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 12, 2009
 * 
 */
public class SmsParseReceiver extends BroadcastReceiver {

	private static String[] prefixes = null;
	private static Form[] forms = null;
	
	
	

	// private Context mContext = null;

	public synchronized static void initFormCache() {
		forms = ModelTranslator.getAllForms();
		prefixes = new String[forms.length];
		for (int i = 0; i < forms.length; i++) {
			prefixes[i] = forms[i].getPrefix();
		}
	}

	private Form determineForm(String message) {
		for (int i = 0; i < forms.length; i++) {
			if (forms[i] == null) {
				Log.i("SmsParseReceiver", "form " + i + " is null");
			} else {
				Log.i("SmsParseReceiver", "form " + i + " is " + forms[i].getFormName());
			}
		}
		
		int len = prefixes.length;
		Log.i("SmsParseReceiver", "num prefixes: " + len);
		for (int i = 0; i < len; i++) {
			Log.i("SmsParseReceiver", "prefix: " + prefixes[i].toLowerCase());
			String prefix = prefixes[i];
			Log.i("SmsParseReceiver", "beginning of message: " + message.toLowerCase().trim().substring(0, prefixes[i].length()));
			// TODO changed this to be any case.
			if (message.toLowerCase().trim().startsWith(prefix.toLowerCase() + " ")) {
				Log.i("SmsParseReceiver", "match! returning form " + i);
				return forms[i];
			}
		}
		return null;
	}

	//TODO don't just disregard messages with malformed prefix
	
	/**
	 * Upon message receipt, determine the form in question, then call the
	 * corresponding parsing logic.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		ApplicationGlobals.initGlobals(context);
	
		if (prefixes == null) {
			initFormCache(); // profiler shows us that this is being called
								// frequently on new messages.
		}
		
		Log.i("SmsParseReciever", "Initialized form cache");
		
		// TODO Auto-generated method stub
		String body = intent.getStringExtra("body");

		if (body.startsWith("notifications@dimagi.com /  / ")) {
			body = body.replace("notifications@dimagi.com /  / ", "");
			Log.d("SmsParseReceiver", "Debug, snipping out the email address");
		}

		int msgid = intent.getIntExtra("msgid", 0);
		Log.i("SmsParseReciever", "msgid " + msgid);
		Form form = determineForm(body);
		Log.i("SmsParseReciever", "form " + form);
		if (form == null) {			
			/*if (ApplicationGlobals.doReplyOnFail()) {
				Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
				broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
				broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseFailText());
				context.sendBroadcast(broadcast);
			}*/
			Log.i("SmsParseReciever", "null form");
			return;
		} else {
			
			Monitor mon = MessageTranslator.GetMonitorAndInsertIfNew(context, intent.getStringExtra("from"));
			
			Log.i("SmsParseReceiver", "made it through messagetranslator call");
			
			// if(mon.getReplyPreference()) {
			//if (ApplicationGlobals.doReplyOnParse()) {
			
			
			//Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
			//broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
			//broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, reply);
			//context.sendBroadcast(broadcast);
			//}
			Vector<IParseResult> results = ParsingService.ParseMessage(form, body);
			Log.i("SmsParseReceiver", "made it through parsingservice call");
			ParsedDataTranslator.InsertFormData(context, form, msgid, results);
			Log.i("SmsParseReceiver", "made it through insertformdata");
			
			
			// Nicole: Code to reply to a received text with well formed prefix
						ContentResolver contentResolver = context.getContentResolver();
						
						Log.i("SmsParseReceiver", "looking for form id " + form.getFormId());
						//String[] args = {form.getFormName()};
						Cursor formRow = contentResolver.query(RapidSmsDBConstants.Form.CONTENT_URI, 
								null,
								"_id = " + form.getFormId(), 
								null, 
								null);
						
						if (formRow == null) {
							Log.i("SmsParseReceiver", "formRow null");
						} else if (formRow.getCount() == 0) {
							Log.i("SmsParseReceiver", "formRow nonexistant");
						}
						formRow.moveToFirst();
						
						Log.i("SmsParseReceiver", "getting question type");
						int questionType = formRow.getInt(formRow.getColumnIndex("question_type"));
						
						Log.i("SmsParseReceiver", "getting description");
						String description = formRow.getString(formRow.getColumnIndex("description"));
						
						Log.i("SmsParseReceiver", "description " + description);
						// Cut off the "Reply ___" part of the text
						int endIndex = description.indexOf("Reply");
						
						Log.i("SmsParseReceiver", "endIndex " + endIndex);
						String question = description.substring(0, endIndex);
						
						//String reply = "Thank you for answering the question: " + question;
						String reply = "Thanks for your response! ";
						reply += "Currently, your community has responded: ";
								
						// Now get the currently collected responses.
						String uriString = RapidSmsDBConstants.FormData.CONTENT_URI_PREFIX
								+ form.getFormId();
						Log.i("SmsParseReceiver", "querying for all form data, uri string: " + uriString);
						Log.i("SmsParseReceiver", "uri string " + RapidSmsDBConstants.Form.CONTENT_URI_STRING);
						Cursor allFormData = contentResolver.query(Uri.parse(uriString), 
								null, 
								null, 
								null, 
								null);
						
						allFormData.moveToFirst();
						if (questionType == SurveyCreationConstants.QuestionTypes.MULTIPLECHOICE) {
							
							// TODO this code is repeated in three places. Here, XMLTranslator, and QuestionVerifier.
							// should modularize.
							
							// This gets the field names we're choosing between for multiple choice.
							String[] selects = new String[4];
							for (int j = 1; j <= 4; j++) {
								if (description.contains(j + ". ")) {
									int k = j + 1;
									if (description.contains(k + ". ")) {
										selects[j-1] = description.substring(description.indexOf("" + j + ". ") + 3, 
																			description.indexOf(",  " + k + ". "));
									} else {
										selects[j-1] = description.substring(description.indexOf("" + j + ". ") + 3, 
												description.indexOf(".", description.indexOf("" + j + ". ") +3));
									}
								}
							}
								
							
							int k = 0;
							while (k < 4 && selects[k] != null) {
								k++;
							}
							Log.i("label",""+ k);
							String[] labels = new String[k];
							for (int i = 0; i < k; i++) {
								
								labels[i] = selects[i];
								Log.i("label", labels[i]);
							}
							
							// Count up all the responses
							int[] tally = new int[k];
							int total = 0;
							while (!allFormData.isAfterLast()) {
								Log.i("SmsParseReceiver", "Iteration of tallying");
								int selection = allFormData.getInt(2);
								if (selection <= k) {
									tally[selection - 1]++;
									total++;
								}
								allFormData.moveToNext();
							}
							
							
							// Build our reply
							int i;
							for (i = 0; i < k - 1; i++) {
								
								int percent = (int) Math.floor(100 * (double) tally[i]/ (double) total);
								reply += percent + "% " + labels[i] + ", ";
							}
							int percent = (int) Math.floor(100 * (double) tally[i]/ (double) total);
							reply += percent + "% " + labels[i] + ".";
							
							
						} else if (questionType == SurveyCreationConstants.QuestionTypes.YESNO) {
							
							int yesTally = 0;
							int noTally = 0;
							int total = 0;
							
							while (!allFormData.isAfterLast()) {
								if (allFormData.getString(2).toLowerCase().equals("true")) {
									yesTally++;
								} else {
									noTally++;
								}
								total++;
								allFormData.moveToNext();
							}
							
							int yesPercent = (int) Math.floor(100 * (double) yesTally/ (double) total);
							int noPercent = (int) Math.floor(100 * (double) noTally/ (double) total);

							reply += yesPercent + "% Yes, " + noPercent + "% No.";
							
						} else if (questionType == SurveyCreationConstants.QuestionTypes.RATING) {
							int tally = 0;
							int total = 0;
							while (!allFormData.isAfterLast()) {
								int rating = allFormData.getInt(2);
								if (rating <= 10 && rating >= 0) {
									tally += rating;
								}
								total++;
								allFormData.moveToNext();
							}
							
							reply += "Average Rating " + String.format("%.2g%n", (double) tally/ (double) total) + ".";
							
						}
						Log.i("SmsParseReceiver", "sending reply text: " + reply);
						
						SmsManager smsManager = SmsManager.getDefault();
						Log.i("SmsParseReceiver", "sending text to: " + intent.getStringExtra("from"));
						smsManager.sendTextMessage(intent.getStringExtra("from"), null, reply, null, null);
						//smsManager.sendTextMessage("5556", null, reply, null, null);

			
			
			
		}
		
		XMLTranslator translator = new XMLTranslator();
		try {
			translator.buildOpenRosaXform(context, msgid, form);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		try {
			//buildOpenRosaXform(context, intent, msgid, form);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
	}

	private ContentResolver getContentResolver() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
