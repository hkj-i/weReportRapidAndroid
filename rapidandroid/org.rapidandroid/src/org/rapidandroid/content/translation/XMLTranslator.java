package org.rapidandroid.content.translation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidandroid.data.SurveyCreationConstants;
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
	// hee
	private static String PROJECT_NAME = "capstone_report";
	private static String ODK_INSTANCE = "content://org.odk.collect.android.provider.odk.instances/instances/";
	
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
	public Form determineForm(String message) {
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
		
		Cursor formRow = context.getContentResolver().query(Uri.parse(RapidSmsDBConstants.Form.CONTENT_URI_STRING),
				null,
				"_id = " + f.getFormId(), 
				null,
				null);
		
		
		messageRow.moveToFirst();
		parsedDataRow.moveToFirst();
		parsedDataFieldNamesRows.moveToFirst();
		formRow.moveToFirst();
		
		String surveyid = formRow.getString(formRow.getColumnIndex("survey_id"));
		
		Cursor surveyRow = context.getContentResolver().query(RapidSmsDBConstants.Survey.CONTENT_URI,
				null,
				"_id = " + surveyid,
				null,
				null);
		surveyRow.moveToFirst();
		Log.i("Survey id is", "surveyid");
		
		String projectname = surveyRow.getString(surveyRow.getColumnIndex("project_id")); // currently, it is project name
		if (projectname == null) {
			// change to empty string; avoid null
			projectname = "";
		}
		String[] args = {projectname};
		Cursor projectRow = context.getContentResolver().query(RapidSmsDBConstants.Project.CONTENT_URI,
				null,
				"name = ?",
				args,
				null);
		projectRow.moveToFirst();

		if (messageRow == null) {
			Log.e("DatabaseQuery", "messageRow NULL");
		}
		if (messageRow.getColumnCount() == 0) {
			Log.e("DatabaseQuery", "messageRow empty");
		}

		Log.i("message row column count:", messageRow.getColumnCount() + "");
		for (int i = 0; i < messageRow.getColumnCount(); i++) {
			Log.i("DatabaseQuery - get column count", "Column " + i + " " + messageRow.getString(i));
		}

		if (parsedDataRow == null) {
			Log.e("DatabaseQuery", "parsedDataRow NULL");
		}

		Log.i("DatabaseQuery", "parsedDataRow not NULL");

		if (parsedDataRow.getColumnCount() == 0) {
			Log.i("DatabaseQuery", "parsedDataRow empty");
		}

		Log.i("DatabaseQuery", "parsedDataRow not empty");

		String[] columnNames = parsedDataRow.getColumnNames();

		Log.i("DatabaseQuery", "got columnNames");


		Log.i("DatabaseQuery", "got columnCount which is " + parsedDataRow.getColumnCount());;

		for (int i = 0; i < parsedDataRow.getColumnCount(); i++) {
			Log.i("DatabaseQuery", "Column " + i + " " + parsedDataRow.getString(i));
			Log.i("DatabaseQuery", "Column name " + columnNames[i]);
		}

		Log.i("DatabaseQuery", "printed datarow column count");

		if (parsedDataFieldNamesRows == null) {
			Log.e("DatabaseQuery", "messageRow NULL");
		}
		if (parsedDataFieldNamesRows.getColumnCount() == 0) {
			Log.e("DatabaseQuery", "messageRow empty");
		}

		for (int i = 0; i < parsedDataFieldNamesRows.getColumnCount(); i++) {
			Log.i("DatabaseQuery - parse Data Field Names", "Column " + i + " " + parsedDataFieldNamesRows.getString(i));
		}

		Log.i("DatabaseQuery - parse Data Field Names", "finished parsing");
		Field[] fields = f.getFields();
		Log.i("XML Translator", "fields is not null");

		// generate xml string
		XML += "<?xml version=\'1.0\' ?>";
		String processedFormName = processSpecialCharacters(f.getFormName());
		//hee
		//XML += "<data " +" id=\"" + processedFormName + "\">";
		XML += "<data " +" id=\"" + PROJECT_NAME + "\">";
		XML += "<meta>";

		// TODO need to generate this
		XML += "<instanceID>uuid:" + msgid + "</instanceID>";
		XML += "</meta>";

		XML += "<rawtext>" + processSpecialCharacters(messageRow.getString(messageRow.getColumnIndex("message"))) + "</rawtext>";

		Log.i("XML tanslator", "enter the fields of instance file");
		// for producing the instance file
		int text = 1;
		int num = 1;
		int select = 1;
		int countm = 1;
		for (int i = 0; i < columnNames.length; i++) {

			if (columnNames[i].startsWith("col_")) {
				String parsedfield = parsedDataRow.getString(i);

				// grab the fieldtype id corresponding to the sequence

				parsedDataFieldNamesRows = context.getContentResolver().query(RapidSmsDBConstants.Field.CONTENT_URI,
						null,
						"form_id = " + f.getFormId() + " AND " + "sequence = " + countm,
						null,
						null);
				parsedDataFieldNamesRows.moveToFirst();

				String ftype_id = parsedDataFieldNamesRows.getString(parsedDataFieldNamesRows.getColumnIndex("fieldtype_id"));
				Cursor datatyperow = context.getContentResolver().query(RapidSmsDBConstants.FieldType.CONTENT_URI,
						null,
						"_id = " + ftype_id,
						null,
						null);

				Cursor questionType = context.getContentResolver().query(RapidSmsDBConstants.Form.CONTENT_URI, 
						null,
						"_id = " + f.getFormId(),  
						null, 
						null);
				
				questionType.moveToFirst();
				datatyperow.moveToFirst();

				// according to the type, choose num or select or text
				String type =  datatyperow.getString(datatyperow.getColumnIndex("datatype"));
				if (type.equals("word")) {
					XML += "<" + "text" + text +">" + parsedfield + "</" + "text" + text +">";		
					text++;
				} else if (type.equals("boolean")) {
					// yes no
					if (parsedfield.toLowerCase().equals("true")) {
						XML += "<" + "select" + select +">" + "Yes" + "</" + "select" + select +">";
					} else {
						XML += "<" + "select" + select +">" + "No" + "</" + "select" + select +">";
					}
					select++;
					
				} else if (questionType.getInt(questionType.getColumnIndex("question_type")) == SurveyCreationConstants.QuestionTypes.MULTIPLECHOICE) {
					
					String description = questionType.getString(questionType.getColumnIndex("description"));
					String[] selects = new String[4];
					for (int j = 1; j <= 4; j++) {
						if (description.contains(j + ". ")) {
							int k = j + 1;
							if (description.contains(k + ". ")) {
								selects[j-1] = description.substring(description.indexOf("" + j + ". ") +3, 
																	description.indexOf("" + k + ". "));
							} else {
								selects[j-1] = description.substring(description.indexOf("" + j + ". ") +3, 
										Math.max(description.indexOf(".", description.indexOf("" + j + ". ") +3),
												description.indexOf(" ", description.indexOf("" + j + ". ") +3)));
							}
						}
					}
					
					int k = 0;
					while (k < 4 && selects[k] != null) {
						XML += "<" + "select" + select +">" + selects[k] + "</" + "select" + select +">";
						k++;
						select++;
					}
					
				} else {
					// number
					XML += "<" + "num" + num +">" + parsedfield + "</" + "num" + num +">";
					num++;
				}
				countm++;
			}
		}

		// fill out the rest
		for(int i=1; i <= 5; i++) {
			if (text == i) {
				XML +=	"<text" + text + " />";
				text++;
			}

			if (num == i) {
				XML +=	"<num" + num + " />";
				num++;
			}

			if (select == i) {
				XML +=	"<select" + select + " />";
				select++;
			}

		}

		// now get the form name
		String official_formname = processedFormName.replace(" ", "_");

		String phonenumber = messageRow.getString(messageRow.getColumnIndex("phone"));
		
		// now get the project name
		Log.i("XML parse receiver", "Now to get volunteer name");
		
		XML += "<volunteer_name /><survey_name>" + official_formname  +"</survey_name>";
		
		if (!projectname.equals("")) {
			XML += "<project_name>" + projectname + "</project_name>";
		} else {
			XML += "<project_name />";
		}
		
		Log.i("XML translate", "projectname = " + projectname);
		projectRow.moveToFirst();
		if (!projectname.equals("") && projectRow != null && projectRow.getColumnCount() != 0) {
			Log.i("XML", "getLocation");
			Log.i("XML", "column count = " + projectRow.getColumnCount());
			String loc = projectRow.getString(projectRow.getColumnIndex("location"));
			if (loc != null && loc != "") {
				XML += "<location>" + loc + "</location>";
			} else {
				XML += "<location />";
			}
		} else {
			XML += "<location />";
		}
		
		if (phonenumber != null && !phonenumber.equals("")) {
			XML += "<phone_number>" + encryptPhonenumber(phonenumber) +"</phone_number>";
		} else {
			XML += "<phone_number />";
		}
				
		XML+= "<time>" + messageRow.getString(messageRow.getColumnIndex("receive_time")) + "</time>";
		
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
		XML += "</data>";

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
	     values.put("displayName", processedFormName); // make your own display name~
	     values.put("status", "incomplete"); // you can mark it as complete
	     values.put("canEditWhenComplete", Boolean.toString(true));
	     values.put("instanceFilePath", 
	    		 newFolder + "/" + instanceName + ".xml"); // file path
	     values.put("jrFormId", PROJECT_NAME); // the form id; the form I used happened to have id = "st2"
	     values.put("submissionUri", "https://capstone-wereport.appspot.com/submission");
	     // hee
	     // jrFormName
	     values.put("jrFormName", processedFormName);

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
		rapidValues.put("form_uri", uriOfForm.toString().substring(ODK_INSTANCE.length()));
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

	private String encryptPhonenumber(String phonenumber) {
		Integer phoneN = Integer.parseInt(phonenumber);
		return phoneN.toString();
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