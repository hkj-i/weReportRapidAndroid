package org.rapidandroid.content.translation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.model.Field;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

public class XMLTranslator {

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

	// TODO this is where we would need to "guess" based on the prefix
	// Also in SMSReceiver so the message actually reaches here
	// Currently, this should never return null, since we only
	// grab text messages with prefixes we exactly recognize.
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

	// TODO throws remote exception???????
	// Returns a string of XML and an Integer describing the state of the XML.  Returns 
	// 0 for incomplete XML generated, and 1 for complete XML generated.
	public void buildOpenRosaXform(Context context, int msgid, Form f) throws RemoteException {
		
		String XML = "";
		Integer wellFormed = 1;
		
		
		// In order to construct an XML from a text message, we need:
		//		- Survey ID from Aggregate
		//		- instanceID
		//		- field names (should match Xform)
		//
		//  We'll query the database for already-parsed fields...
		//  not doing anything with them yet.  Not sure where field names are stored,
		//  in the form???

	/*	Cursor row = context.getContentResolver().query(Uri.parse(RapidSmsDBConstants.FormData.CONTENT_URI_PREFIX + f.getFormId()),
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
		
		*/
		/*
		// Required Header
		XML += "<?xml version=\'1.0\' ?>";
		
		// TODO this is hardcoding one specific survey
		//xmlString += "<data id=\"build_Texts_1367305507\">";
		XML += "<data id=\"" + processSpecialCharacters(f.getFormName()) + "\">";
		
		XML += "<meta>";
		
		// TODO need to generate this
		XML += "<instanceID>uuid:" + msgid + "</instanceID>";
		XML += "</meta>";
		
		// TODO need to generate another uuid for the sender so we don't actually
		// send up the real number.  Also, can we quarantee this won't have special
		// characters?
		XML += "<sender>" + sender + "</sender>";
		XML += "<message>" + processSpecialCharacters(msgBody) + "</message>";
		
		// Process the message here.
		Field[] fields = f.getFields();
		
		
		
		
		
		XML += "</data>";
			*/
		
		Cursor messageRow = context.getContentResolver().query(RapidSmsDBConstants.Message.CONTENT_URI, 
																null, 
																"_id = " + msgid, 
																null, 
																null);
		
		Cursor parsedDataRow = context.getContentResolver().query(Uri.parse(RapidSmsDBConstants.FormData.CONTENT_URI_PREFIX + f.getFormId()),
																		null,
																		"message_id = " + msgid,
																		null,
																		null);
		
		Cursor parsedDataFieldNamesRows = context.getContentResolver().query(RapidSmsDBConstants.Field.CONTENT_URI,
																			null,
																			"form_id = " + f.getFormId(),
																			null,
																			null);
		
		
		messageRow.moveToFirst();
		parsedDataRow.moveToFirst();
		parsedDataFieldNamesRows.moveToFirst();
		
		if (messageRow == null) {
			Log.e("DatabaseQuery", "messageRow NULL");
		}
		if (messageRow.getColumnCount() == 0) {
			Log.e("DatabaseQuery", "messageRow empty");
		}
		for (int i = 0; i < messageRow.getColumnCount(); i++) {
			Log.i("DatabaseQuery", "Column " + i + " " + messageRow.getString(i));
		}
		
		if (parsedDataRow == null) {
			Log.e("DatabaseQuery", "parsedDataRow NULL");
		}
		if (parsedDataRow.getColumnCount() == 0) {
			Log.e("DatabaseQuery", "parsedDataRow empty");
		}
		String[] columnNames = parsedDataRow.getColumnNames();
		for (int i = 0; i < parsedDataRow.getColumnCount(); i++) {
			Log.i("DatabaseQuery", "Column " + i + " " + parsedDataRow.getString(i));
			Log.i("DatabaseQuery", "Column name " + columnNames[i]);
		}
		
		if (parsedDataFieldNamesRows == null) {
			Log.e("DatabaseQuery", "messageRow NULL");
		}
		if (parsedDataFieldNamesRows.getColumnCount() == 0) {
			Log.e("DatabaseQuery", "messageRow empty");
		}
		
		for (int i = 0; i < parsedDataFieldNamesRows.getColumnCount(); i++) {
			Log.i("DatabaseQuery", "Column " + i + " " + parsedDataFieldNamesRows.getString(i));
		}
		
		Field[] fields = f.getFields();
		
		// generate xml string
		XML += "<?xml version=\'1.0\' ?>";
		String processedFormName = processSpecialCharacters(f.getFormName());
		XML += "<" + processedFormName + " id=\"" + processedFormName + "\">";
		XML += "<meta>";
		
		// TODO need to generate this
		XML += "<instanceID>uuid:" + msgid + "</instanceID>";
		XML += "</meta>";
		
		XML += "<rawtext>" + processSpecialCharacters(messageRow.getString(messageRow.getColumnIndex("message"))) + "</rawtext>";
		
		for (int i = 0; i < columnNames.length; i++) {
			if (columnNames[i].startsWith("col_")) {
				String fieldName = columnNames[i].substring(4);
				XML += "<" + fieldName + ">" + parsedDataRow.getString(i) + "</" + fieldName + ">";
				
				// If any of the fields aren't filled in, it's a malformed response
				if (parsedDataRow.getString(i) == null) {
					Log.i("DatabaseQuery", "XML malformed: field " + fieldName + " null");
					wellFormed = 0;
				}
			}
		}
		
		for (int i = 0; i < f.getFields().length; i++) {
			Log.i("DatabaseQuery", "Field " + i + ": " + f.getFields()[i].getName());
		}
		String[] splitMessage = messageRow.getString(messageRow.getColumnIndex("message")).split(" ");
		for (int i = 0; i < splitMessage.length; i++) {
			Log.i("DatabaseQuery", "Item " + i + " of message: " + splitMessage[i]);
		}
		
		// TODO this is super hacky and terrible coding
		// If the response didn't include exactly the number of items we expected, it's malformed
		if (messageRow.getString(messageRow.getColumnIndex("message")).split(" ").length != f.getFields().length + 2) {
			Log.i("DatabaseQuery", "XML malformed: number of fields in response " + messageRow.getString(messageRow.getColumnIndex("message")).split(" ").length + ", number of fields expected " + f.getFields().length);
			wellFormed = 0;
		}
		XML += "</" + processedFormName + ">";
		
		Log.i("DatabaseQuery", "XML string: " + XML);
		
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
			String instanceName = f.getFormName() + "_" + messageRow.getString(messageRow.getColumnIndex("time")) + "_" + msgid;
			instanceName = instanceName.replace(" ", "_");
			instanceName = instanceName.replace(":", "_");
			File newFolder = new File(externalStorageDir.getAbsoluteFile() + 
									"/odk/instances/" + instanceName + "/");
			newFolder.mkdirs();

		try {
			// String filename = path + File.separator +
			// path.substring(path.lastIndexOf(File.separator) + 1) + ".xml";

			FileWriter fw = new FileWriter(newFolder + "/" + instanceName + ".xml");
			fw.write(XML);
			fw.flush();
			fw.close();

		} catch (IOException e) {
			Log.e("SaveXml","Error writing XML file");
			e.printStackTrace();
		}

		// Now we need to add these to the odk db

		
		ContentResolver resolver = context.getContentResolver();
	     ContentValues values = new ContentValues();
	     values.put("displayName", f.getFormName() + " " + msgid); // make your own display name~
	     values.put("status", "incomplete"); // you can mark it as complete
	     values.put("canEditWhenComplete", Boolean.toString(true));
	     values.put("instanceFilePath", 
	    		 newFolder + "/" + instanceName + ".xml"); // file path
	     values.put("jrFormId", f.getFormName()); // the form id; the form I used happened to have id = "st2"
	     // only add the version if it exists (ie not null)
	     // now we want to get the uri for the insertion.
	     Uri uriOfForm = resolver.insert(
	    		 Uri.parse("content://org.odk.collect.android.provider.odk.instances/instances"),
	    		 values);
	     
	     String[] projection = {"_id"};
	     
	     Cursor row = (resolver.query(Uri.parse("content://org.odk.collect.android.provider.odk.instances/instances"), 
	    		 					projection, 
	    		 					"instanceFilePath = \"" + newFolder + "/" + instanceName + ".xml\"", 
	    		 					null, null));
		row.moveToFirst();
		int rowId = row.getShort(0);
		
		// Update RapidAndroid DB with uri and well-formed
		ContentResolver rapidResolver = context.getContentResolver();
		ContentValues rapidValues = new ContentValues();
		rapidValues.put("form_uri", (newFolder + "/" + rowId).toString());
		rapidValues.put("is_sent", wellFormed);
	     int colsChanged = rapidResolver.update(
	    		 RapidSmsDBConstants.Message.CONTENT_URI,
	    		 rapidValues,
	    		 "_id = " + msgid,
	    		 null);
		Log.i("DatabaseQuery", "Updated " + colsChanged + " columns");
	     Cursor messageRow1 = context.getContentResolver().query(RapidSmsDBConstants.Message.CONTENT_URI, 
					null, 
					"_id = " + msgid, 
					null, 
					null);
	     messageRow1.moveToFirst();
	     if (messageRow1 == null) {
				Log.e("DatabaseQuery", "messageRow NULL");
			}
			if (messageRow1.getColumnCount() == 0) {
				Log.e("DatabaseQuery", "messageRow empty");
			}
			for (int i = 0; i < messageRow1.getColumnCount(); i++) {
				Log.i("DatabaseQuery", "Column " + i + " " + messageRow1.getString(i));
			}
		

	}
	
	private String processSpecialCharacters(String string) {
		string.replace("<", "&lt");
		string.replace(">", "&gt");
		string.replace("&", "&amp");
		string.replace("\'", "&amp");
		string.replace("\"", "&quot");
		return string;
	}
	
	
	
	
}
