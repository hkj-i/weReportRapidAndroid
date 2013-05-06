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
		int len = prefixes.length;
		for (int i = 0; i < len; i++) {
			String prefix = prefixes[i];
			if (message.toLowerCase().trim().startsWith(prefix + " ")) {
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
		// TODO Auto-generated method stub
		String body = intent.getStringExtra("body");

		if (body.startsWith("notifications@dimagi.com /  / ")) {
			body = body.replace("notifications@dimagi.com /  / ", "");
			Log.d("SmsParseReceiver", "Debug, snipping out the email address");
		}

		int msgid = intent.getIntExtra("msgid", 0);

		Form form = determineForm(body);
		if (form == null) {			
			if (ApplicationGlobals.doReplyOnFail()) {
				Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
				broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
				broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseFailText());
				context.sendBroadcast(broadcast);
			}
			return;
		} else {
			Monitor mon = MessageTranslator.GetMonitorAndInsertIfNew(context, intent.getStringExtra("from"));
			// if(mon.getReplyPreference()) {
			if (ApplicationGlobals.doReplyOnParse()) {
				// for debug purposes, we'll just ack every time.
				Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
				broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
				broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseSuccessText());
				context.sendBroadcast(broadcast);
			}
			Vector<IParseResult> results = ParsingService.ParseMessage(form, body);
			ParsedDataTranslator.InsertFormData(context, form, msgid, results);
		}
		
		
		try {
			buildOpenRosaXform(context, intent, msgid, form);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// TODO throws remote exception???????
	private void buildOpenRosaXform(Context context, Intent intent, int msgid, Form f) throws RemoteException {
		

		// In order to construct an XML from a text message, we need:
		//		- Survey ID from Aggregate
		//		- instanceID
		//		- field names (should match Xform)
		//
		//  We'll query the database for already-parsed fields...
		//  not doing anything with them yet.  Not sure where field names are stored,
		//  in the form???

		Cursor row = context.getContentResolver().query(Uri.parse(RapidSmsDBConstants.FormData.CONTENT_URI_PREFIX + f.getFormId()),
											null,
											"message_id = " + msgid,
											null,
											null);
		row.moveToFirst();
		if (row == null) {
			Log.e("DatabaseQuery", "Query results NULL");
		}
		if (row.getColumnCount() == 0) {
			Log.e("DatabaseQuery", "Query results empty");
		}
		for (int i = 0; i < row.getColumnCount(); i++) {
			Log.i("DatabaseQuery", "Column " + i + " " + row.getString(i));
		}
		
		
		
		String xmlString = "<?xml version=\'1.0\' ?>";
		
		// TODO this is hardcoding one specific survey
		xmlString += "<data id=\"build_Texts_1367305507\">";
		xmlString += "<meta>";
		
		// TODO need to generate this
		xmlString += "<instanceID>uuid:" + row.getString(0) + "</instanceID>";
		xmlString += "</meta>";
		
		
		xmlString += "<sender>" + intent.getStringExtra("from") + "</sender>";
		xmlString += "<message>" + intent.getStringExtra("body") + "</message>";
		xmlString += "</data>";
			
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
			FileWriter fw = new FileWriter(externalStorageDir.getAbsoluteFile() + "/odk/instances/Texts_2013-04-30_07-05-21/text" + row.getString(0) + ".xml");
			fw.write(xmlString);
			fw.flush();
			fw.close();

		} catch (IOException e) {
			Log.e("SaveXml","Error writing XML file");
			e.printStackTrace();
		}

		
		// Now we need to add these to the odk db
		ContentValues cv = new ContentValues();
		
		cv.put("instanceFilePath", externalStorageDir.getAbsoluteFile() + "/odk/instances/Texts_2013-04-30_07-05-21/text" + row.getString(0) + ".xml");
		cv.put("jrVersion", 1.0);
		cv.put("jrFormId", row.getString(0));
		cv.put("submissionUri", "build_Texts_1367305507");
		cv.put("displayName", "Text" + row.getString(0));
		
		Uri uri = context.getContentResolver().insert(Uri.parse("content://org.odk.collect.android.provider.odk.instances"), cv);
	}
}
