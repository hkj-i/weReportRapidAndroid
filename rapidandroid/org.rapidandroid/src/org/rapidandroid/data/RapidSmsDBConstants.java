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

package org.rapidandroid.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Helper constants for table and querying for the content provider and Sql
 * helper
 * 
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 14, 2009
 * 
 * 
 */

public final class RapidSmsDBConstants {
	public static final String AUTHORITY = "org.rapidandroid.provider.RapidSms";

	private RapidSmsDBConstants() {
	}

	/**
	 * Message table
	 */
	public static final class Message implements BaseColumns {

		public static final String TABLE = "rapidandroid_message";

		public static final String URI_PART = "message";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PART);
		public static final String CONTENT_URI_STRING = "content://" + AUTHORITY + "/" + URI_PART + "/";

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.rapidandroid.data.message";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.rapidandroid.data.message";

		// Section Table columns ##########################################
		/**
		 * 
		 * Phone field is a helper for inserting to the content provider.
		 * columnm is there as legacy, but shouldn't be used.
		 */
		public static final String PHONE = "phone";

		public static final String MESSAGE = "message";
		/**
		 * The monitorID of the sender. theMonitor must exist before a message
		 * can be inserted.
		 */
		public static final String MONITOR = "monitor_id";
		public static final String TIME = "time";
		public static final String IS_OUTGOING = "is_outgoing";
		public static final String IS_VIRTUAL = "is_virtual";
		public static final String RECEIVE_TIME = "receive_time";
	}

	/**
	 * Project table
	 */
	public static final class Project implements BaseColumns {

		public static final String TABLE = "rapidandroid_project";

		public static final String URI_PART = "project";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PART);
		public static final String CONTENT_URI_STRING = "content://" + AUTHORITY + "/" + URI_PART + "/";

		// TODO maybe look into uncommenting these?
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.rapidandroid.data.project";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.rapidandroid.data.project";

		// Section Table columns ##########################################
		/**
		 * 
		 * Phone field is a helper for inserting to the content provider.
		 * columnm is there as legacy, but shouldn't be used.
		 */
		/*public static final String PHONE = "phone";

		public static final String MESSAGE = "message";
		/**
		 * The monitorID of the sender. theMonitor must exist before a message
		 * can be inserted.
		 */
		/*public static final String MONITOR = "monitor_id";
		public static final String TIME = "time";
		public static final String IS_OUTGOING = "is_outgoing";
		public static final String IS_VIRTUAL = "is_virtual";
		public static final String RECEIVE_TIME = "receive_time";*/
	}
	
	/**
	 * Project table
	 */
	public static final class Survey implements BaseColumns {

		public static final String TABLE = "rapidandroid_survey";

		public static final String URI_PART = "survey";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PART);
		public static final String CONTENT_URI_STRING = "content://" + AUTHORITY + "/" + URI_PART + "/";

		// TODO maybe look into uncommenting these?
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.rapidandroid.data.survey";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.rapidandroid.data.survey";

		// Section Table columns ##########################################
		/**
		 * 
		 * Phone field is a helper for inserting to the content provider.
		 * columnm is there as legacy, but shouldn't be used.
		 */
		/*public static final String PHONE = "phone";

		public static final String MESSAGE = "message";
		/**
		 * The monitorID of the sender. theMonitor must exist before a message
		 * can be inserted.
		 */
		/*public static final String MONITOR = "monitor_id";
		public static final String TIME = "time";
		public static final String IS_OUTGOING = "is_outgoing";
		public static final String IS_VIRTUAL = "is_virtual";
		public static final String RECEIVE_TIME = "receive_time";*/
	}
	
	
	/**
	 * Monitor table
	 */
	public static final class Monitor implements BaseColumns {

		// Structural stuffs
		public static final String TABLE = "rapidandroid_monitor";

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.rapidandroid.data.monitor";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.rapidandroid.data.monitor";

		public static final String URI_PART = "mMonitorString";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PART);

		public static final Uri MESSAGE_BY_MONITOR_URI = Uri.parse("content://" + AUTHORITY + "/messagesbymonitor");

		// Section Table columns ##########################################
		public static final String LAST_NAME = "last_name";
		public static final String FIRST_NAME = "first_name";
		public static final String ALIAS = "alias";
		public static final String PHONE = "phone";
		public static final String EMAIL = "email";
		public static final String INCOMING_MESSAGES = "incoming_messages";
		public static final String RECEIVE_REPLY = "receive_reply";

	}

	public static final class Form implements BaseColumns {
		// Structural stuffs
		public static final String TABLE = "rapidandroid_form";

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.rapidandroid.data.form";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.rapidandroid.data.form";

		public static final String URI_PART = "form";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PART);
		public static final String CONTENT_URI_STRING = "content://" + AUTHORITY + "/" + URI_PART + "/";

		// Section Table columns ##########################################
		public static final String FORMNAME = "formname";
		public static final String PREFIX = "prefix";
		public static final String DESCRIPTION = "description";
		public static final String PARSEMETHOD = "parsemethod";

	}

	public static final class Field implements BaseColumns {
		// Structural stuffs
		public static final String TABLE = "rapidandroid_field";

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.rapidandroid.data.field";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.rapidandroid.data.field";

		public static final String URI_PART = "field";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PART);
		public static final String CONTENT_URI_STRING = "content://" + AUTHORITY + "/" + URI_PART + "/";

		// Section Table columns ##########################################
		public static final String FORM = "form_id";
		public static final String SEQUENCE = "sequence";
		public static final String NAME = "name";
		public static final String PROMPT = "prompt";
		public static final String FIELDTYPE = "fieldtype_id";
	}

	public static final class FieldType implements BaseColumns {
		// Structural stuffs
		public static final String TABLE = "rapidandroid_fieldtype";

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.rapidandroid.data.fieldtype";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.rapidandroid.data.fieldtype";

		public static final String URI_PART = "fieldtype";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PART);
		public static final String CONTENT_URI_STRING = "content://" + AUTHORITY + "/" + URI_PART + "/";

		// Section Table columns ##########################################
		public static final String NAME = "name";
		public static final String DATATYPE = "datatype";
		public static final String REGEX = "regex";

	}

	/**
	 * Helper constants for accessing the FormData tables. You must have access
	 * to the Form object for these to be userul
	 * 
	 * @author dmyung
	 * @created Feb 2, 2009
	 */
	public static final class FormData implements BaseColumns {
		// Structural stuffs
		/**
		 * To access the data for a given form, you must access the table by
		 * using this prefix and concat the form's PREFIX. <br>
		 * <br>
		 * This will give you the tablename where formdata is stored. <br>
		 * <br>
		 * The table structure has the following fixed 2 columns at the
		 * beginning (_id, message_id). After that, it's the ordered list of
		 * fields.
		 */
		public static final String TABLE_PREFIX = "formdata_"; // and put the
		// formprefix
		// there!

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.rapidandroid.data.formdata";
		// public static final String CONTENT_ITEM_TYPE =
		// "vnd.android.cursor.item/org.rapidandroid.data.formdata";

		public static final String URI_PART = "formdata";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PART);// hrmm,
																									// this
																									// is
																									// tricky
		public static final String CONTENT_URI_PREFIX = "content://" + AUTHORITY + "/" + URI_PART + "/"; // needs
																											// to
																											// add
																											// the
																											// id

		// Section Table columns ##########################################
		/**
		 * The message_id is column is index 1 of the columns stored
		 */
		public static final String MESSAGE = "message_id";
		// since these tables are dynamically generated, the column prefix is
		// affixed to all columns generated by the form definition. The suffix
		// is the Field Name
		public static final String COLUMN_PREFIX = "col_";

	}

}
