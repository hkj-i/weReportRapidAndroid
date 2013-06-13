package org.rapidandroid.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
 
public class SurveyCreationConstants {

	public static final int SCOPING = 1;
	public static final int PROJECT = 2;
	public static final int ANALYSIS = 3;

	public static final class QuestionTypes {
		public static final int YESNO = 1;
		public static final int MULTIPLECHOICE = 2;
		public static final int FREEFORM = 3;
		public static final int RANKING = 4;
		public static final int RATING = 5;
	}
	
	public static final class AnswerTypes {
		public static final int YESNO = 7;
		public static final int INTEGER = 2;
		public static final int WORD = 1;
	}
	
	public static final class xForm {
		public static final String xForm =
				"<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:jr=\"http://openrosa.org/javarosa\">"
		  + "<h:head>"
		  + "<h:title>SURVEY_NAME</h:title>"
		  + "<model>"
		  + 	"<instance>"
			  +		"<data id=\"capstone_report\">"
				  +		"<meta>"
				  +			"<instanceID/>"
				  +		"</meta>"
				  +		"<rawtext/>"
				  +		"<volunteer_name/>"
				  +		"<survey_name/>"
				  +		"<phone_number/>"
				  +		"<text1/>"
				  +		"<text2/>"
				  +		"<text3/>"
				  +		"<text4/>"
				  +		"<text5/>"
				  +		"<num1/>"
				  +		"<num2/>"
				  +		"<num3/>"
				  +		"<num4/>"
				  +		"<num5/>"
				  +		"<select1/>"
				  +		"<select2/>"
				  +		"<select3/>"
				  +		"<select4/>"
				  +		"<select5/>"
			  +		"</data>"
		  +		"</instance>"
		  +		"<itext>"
			  +		"<translation lang=\"eng\">"
			  +		"</translation>"
		  +		"</itext>"
		  +		"<bind nodeset=\"/data/meta/instanceID\" type=\"string\" readonly=\"true()\" calculate=\"concat('uuid:', uuid())\"/>"
		  +		"<bind nodeset=\"/data/rawtext\" type=\"string\" readonly=\"true()\" />"
		  +		"<bind nodeset=\"/data/volunteer_name\" type=\"string\" />"
		  +		"<bind nodeset=\"/data/survey_name\" type=\"string\" />"
		  +		"<bind nodeset=\"/data/phone_number\" type=\"string\" />"
		  +		"<bind nodeset=\"/data/text1\" type=\"string\"/>"
		  +		"<bind nodeset=\"/data/text2\" type=\"string\"/>"
		  +		"<bind nodeset=\"/data/text3\" type=\"string\"/>"
		  +		"<bind nodeset=\"/data/text4\" type=\"string\"/>"
		  +		"<bind nodeset=\"/data/text5\" type=\"string\"/>"
		  +		"<bind nodeset=\"/data/num1\" type=\"int\"/>"
		  +		"<bind nodeset=\"/data/num2\" type=\"int\"/>"
		  +		"<bind nodeset=\"/data/num3\" type=\"int\"/>"
		  +		"<bind nodeset=\"/data/num4\" type=\"int\"/>"
		  +		"<bind nodeset=\"/data/num5\" type=\"int\"/>"
		  +		"<bind nodeset=\"/data/select1\" type=\"select1\"/>"
		  +		"<bind nodeset=\"/data/select2\" type=\"select1\"/>"
		  +		"<bind nodeset=\"/data/select3\" type=\"select1\"/>"
		  +		"<bind nodeset=\"/data/select4\" type=\"select1\"/>"
		  +		"<bind nodeset=\"/data/select5\" type=\"select1\"/>"
		  +	"</model>"
	+	"</h:head>"
	+	"<h:body>"
	+	"FIELDS"
	+	"</h:body>"
	+	"</h:html>";
		
		public static String getNumFieldXml(int fieldNumber, String prompt) {
			String xml = "";
			xml += "<group appearance=\"field-list\">";
			xml += "<input ref=\"/data/rawtext\">";
			xml += "<label>Original Message: </label>";
			xml += "</input>";
			xml += "<input ref=\"/data/num" + fieldNumber + "\">";
			xml += "<label>" + prompt + "</label>";
			xml += "</input>";
			xml += "</group>";
			return xml;
		}
		
		public static String getSelectFieldXml(int fieldNumber, String prompt, String[] labels) {
			String xml = "";
			xml += "<group appearance=\"field-list\">";
			xml += "<input ref=\"/data/rawtext\">";
			xml += "<label>Original Message: </label>";
			xml += "</input>";
			xml += "<select1 ref=\"/data/select" + fieldNumber + "\">";
			xml += "<label>" + prompt + "</label>";
			for (int i = 0; i < labels.length; i++) {
				xml += "<item>";
				xml += "<label>" + labels[i] + "</label>";
				
				if (labels[i].contains(" ")) {
					xml += "<value>" + labels[i].substring(labels[i].lastIndexOf(" ")) + "</value>";
				} else {
					xml += "<value>" + labels[i] + "</value>";
				}
				xml += "</item>";
			}
			xml += "</select1>";
			xml += "</group>";
			return xml;
		}
	}
	
	
	public class MultipleChoiceQuestion {
		
	}
	
	public class BinaryQuestion {
		
	}
	
	public class FreeformQuestion {
		
	}
	
	public final class Scoping {
		
	}
	
	public static final class Project {
		
	}
	
	public static final class Analysis {
		
	}
}
