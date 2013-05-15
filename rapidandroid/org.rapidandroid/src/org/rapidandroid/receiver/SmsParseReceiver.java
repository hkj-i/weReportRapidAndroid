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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;
import org.rapidandroid.ApplicationGlobals;
import org.rapidandroid.content.translation.MessageTranslator;
import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidandroid.content.translation.ParsedDataTranslator;
import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.model.Monitor;
import org.rapidsms.java.core.parser.IParseResult;
import org.rapidsms.java.core.parser.service.ParsingService;

import org.rapidandroid.content.translation.*;

import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
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
			/*if (ApplicationGlobals.doReplyOnParse()) {
				// for debug purposes, we'll just ack every time.
				Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
				broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
				broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseSuccessText());
				context.sendBroadcast(broadcast);
			}*/
			Vector<IParseResult> results = ParsingService.ParseMessage(form, body);
			Log.i("SmsParseReceiver", "made it through parsingservice call");
			ParsedDataTranslator.InsertFormData(context, form, msgid, results);
			Log.i("SmsParseReceiver", "made it through insertformdata");
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
	

}
