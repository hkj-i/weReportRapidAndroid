package org.rapidandroid.receiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidsms.java.core.model.Field;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.parser.service.ParsingService.ParserType;
import org.rapidsms.java.core.parser.token.ITokenParser;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;

public class FormCreationFileObserver extends FileObserver {

	
	// TODO this seems really hacky
	private Context context;
	public void addContext(Context context) {
		this.context = context;
	}
	
	public FormCreationFileObserver(String path) {
		super(path);
		Log.i("FormCreationFileObserver", "Created!!!");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onEvent(int event, String path) {
		Log.i("FormCreationFileObserver", "Event triggered: " + event);
		//if (event != CREATE) 
			
		if (event != CLOSE_WRITE) return;

		try {
			File externalStorageDir = Environment.getExternalStorageDirectory();
			String absolutePath = externalStorageDir.getAbsolutePath() + "/odk/forms/" + path;
			FileInputStream inputStream= new FileInputStream(absolutePath);
			
			 BufferedReader reader = new BufferedReader(new FileReader(absolutePath));
			 String         line = null;
			 StringBuilder  stringBuilder = new StringBuilder();
			 String         ls = System.getProperty("line.separator");

			 while( ( line = reader.readLine() ) != null ) {
			     stringBuilder.append( line );
			     stringBuilder.append( ls );
			 }
			String formXml = stringBuilder.toString();
			 
			Log.i("FormCreationFileObserver", "XML String: " + formXml);
			if (formXml.length() == 0) return;
			
			
			
			// First, insert form data into the rapidandroid forms database.
			// We need:
			//			formname - name of the form
			//			prefix - what we're looking for at the beginning of SMS responses
			//			description - optional description of the form
			String formNameTag = "<data id=\"";
			int formNameStartTagIndex = formXml.indexOf(formNameTag);
			int formNameEndTagIndex = formXml.indexOf("\">", formNameStartTagIndex);
			String formName = "";
			for (int i = formNameStartTagIndex + formNameTag.length(); i < formNameEndTagIndex; i++) {
				formName += formXml.charAt(i);
			}
			
			String prefixTag = "<text id=\"/data/sender:label\">";
			String prefixValueTag = "<value>";
			String prefixValueEndTag = "<\\value>";
			int prefixTagStartIndex = formXml.indexOf(prefixTag);
			prefixTagStartIndex = formXml.indexOf(prefixValueTag, prefixTagStartIndex);
			int prefixTagEndIndex = formXml.indexOf(prefixValueEndTag, prefixTagStartIndex);
			String prefix = "";
			for (int i = prefixTagStartIndex + prefixValueTag.length(); i < prefixTagEndIndex; i++) {
				prefix += formXml.charAt(i);
			}
			
			ContentValues cv = new ContentValues();
			cv.put("prefix", path.substring(0, path.indexOf(".")));
			cv.put("formname", formName);
			cv.put("description", formName);
			cv.put("parsemethod", "simpleregex");
			
			/*ContentResolver resolver = context.getContentResolver();
			resolver.insert(RapidSmsDBConstants.Form.CONTENT_URI, cv);
			
			// Now retrieve the form id.
			Cursor formNameRow = resolver.query(RapidSmsDBConstants.Form.CONTENT_URI, 
												null, 
												"formname = \"" + formName + "\"", 
												null, 
												null);
			formNameRow.moveToFirst();
			int formId = formNameRow.getShort(formNameRow.getColumnIndex("_id"));
			*/
			// And insert the fields into the rapidandroid field database.
			Set<String> fieldsToIgnore = new HashSet<String>();
			
			// Order matters.
			LinkedList<String> fields = new LinkedList<String>();
			

			fieldsToIgnore.add("rawtext");
			fieldsToIgnore.add("prefix");
			
			String endMetaTag = "</meta>";
			
			int startOfFields = formXml.indexOf(endMetaTag) + endMetaTag.length();
			int endOfFields = formXml.indexOf("</data>");
			
			int currentIndex = formXml.indexOf('<', startOfFields);
			
			while (currentIndex < endOfFields) {
				
				String fieldName = formXml.substring(currentIndex + 1,
						formXml.indexOf("/>", currentIndex));
				Log.i("FormCreationFileObserver", "Field found: " + fieldName);
				if (!fieldsToIgnore.contains(fieldName)) {
					fields.add(fieldName);
				}
				currentIndex = formXml.indexOf('<', currentIndex + 1);
			}
			/*
			int i = 1;
			for (String field: fields) {
				ContentValues fieldcv = new ContentValues();
				
				// Extract the field type
				int indexOfType = formXml.indexOf("<bind nodeset=\"/data/sender\" type=\"");
				String type = formXml.substring(indexOfType + 1,
						formXml.indexOf('"', indexOfType + 1));
				int typeNumber = 0;
				
				// TODO this only accounts for strings and integers.
				if (type.equals("string")) {
					typeNumber = 1;
				} else {
					typeNumber = 2;
				}
				
				fieldcv.put("form_id", formId);
				fieldcv.put("sequence", i);
				fieldcv.put("name", field);
				fieldcv.put("prompt", field);
				fieldcv.put("fieldtype_id", typeNumber);
				resolver.insert(RapidSmsDBConstants.Field.CONTENT_URI, fieldcv);
				
				i++;
						
			}*/
			
			Form formToSave = new Form();
			formToSave.setFormName(formName);
			formToSave.setPrefix(path.substring(0, path.indexOf('.')));
			formToSave.setDescription(formName);

			// (Message[])parsedMessages.keySet().toArray(new
			// Message[parsedMessages.keySet().size()]);
			Field[] fieldArray = new Field[fields.size()];
			for (int i = 0; i < fields.size(); i++) {
				Field field = new Field();
				field.setName(fields.get(i));
				field.setDescription(fields.get(i));
				field.setSequenceId(i + 1);
				// Extract the field type
				String typeTag = "<bind nodeset=\"/data/" + fields.get(i) + "\" type=\"";
				int indexOfType = formXml.indexOf(typeTag) + typeTag.length();
				String type = formXml.substring(indexOfType,
						formXml.indexOf('"', indexOfType + 1));
				int typeNumber = 0;
				
				// TODO this only accounts for strings and integers.
				Log.i("FormCreationFileObserver", fields.get(i) + ": type " + type);
				if (type.equals("string")) {
					typeNumber = 1;
				} else {
					typeNumber = 2;
				}
				ITokenParser fieldtype = ModelTranslator.getFieldType(typeNumber);
				field.setFieldType(fieldtype);
				
				fieldArray[i] = field;
			}
			formToSave.setFields(fieldArray);

			formToSave.setParserType(ParserType.SIMPLEREGEX);
			try {
				ModelTranslator.addFormToDatabase(formToSave);
			} catch (Exception ex) {
				
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		


		
	}

}
