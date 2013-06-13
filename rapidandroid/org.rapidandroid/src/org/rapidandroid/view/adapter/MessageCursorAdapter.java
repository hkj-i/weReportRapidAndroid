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

//
package org.rapidandroid.view.adapter;

import java.util.Date;

import org.rapidandroid.R;
import org.rapidandroid.content.translation.MessageTranslator;
import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidsms.java.core.model.Message;
import org.rapidsms.java.core.model.Monitor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/*
 * Used for drawing the list when user chooses
 * show all messages
 * 
 * there are three states reflected by the checkmark
 * grey is not well-formed
 * blue is finalized(well-formed) but not sent
 * green is finalized(well-formed) and sent to Aggregate
 */
public class MessageCursorAdapter extends CursorAdapter {

	public MessageCursorAdapter(Context context, Cursor c) {
		super(context, c, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.CursorAdapter#bindView(android.view.View,
	 * android.content.Context, android.database.Cursor)
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (view != null) {
			Log.i("Message Cursor bind view", "hello");
			int MonitorID = cursor.getInt(2);
			String timestamp = cursor.getString(3);
			String message = cursor.getString(4);
			boolean isoutgoing = Boolean.parseBoolean(cursor.getString(4));

			boolean issent = false; // hee
			String issent_s = cursor.getString(7);
			if (issent_s != null && issent_s.equals("1")) {
				issent = true;
			}
			
			boolean isfinalized = false;
			String isfinalized_s = cursor.getString(8);
			if (isfinalized_s != null && isfinalized_s.equals("1")) {
				isfinalized = true;
			}
			
			Date hackDate = new Date();
			boolean success = false;
			try {
				hackDate = Message.SQLDateFormatter.parse(timestamp);
				success = true;
			} catch (Exception ex) {
				success = false;
			}

			SimpleMessageView srv = (SimpleMessageView) view;
			//srv.setData(message, hackDate, MonitorID, isoutgoing);
			srv.setData(message, hackDate, MonitorID, isoutgoing, issent, isfinalized);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.CursorAdapter#newView(android.content.Context,
	 * android.database.Cursor, android.view.ViewGroup)
	 */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		Log.i("Message Cursor new View", "hello");
		int this_id = cursor.getInt(0);
		Log.i("newView: id -", this_id + "");
		int MonitorID = cursor.getInt(2);
		Log.i("newView: monitor id-", MonitorID + "");
		String timestamp = cursor.getString(3);
		Log.i("newView: timestamp -", timestamp);
		String message = cursor.getString(4);
		Log.i("newView: message -", message);
		boolean isoutgoing = Boolean.parseBoolean(cursor.getString(4));
		Log.i("newView: isoutgoing - ", cursor.getString(4));

		boolean issent = false; // hee
		String issent_s = cursor.getString(7);
		if (issent_s != null && issent_s.equals("1")) {
			issent = true;
		}
		
		boolean isfinalized = false;
		String isfinalized_s = cursor.getString(8);
		if (isfinalized_s != null && isfinalized_s.equals("1")) {
			isfinalized = true;
		}
		
		Log.i("newView: issent -", cursor.getString(7));
		Date hackDate = new Date();
		
		// Changed to include issent, isfinalized
		SimpleMessageView srv = new SimpleMessageView(context, message, hackDate, MonitorID, isoutgoing, issent, isfinalized);
		return srv;
	}

	private class SimpleMessageView extends TableLayout {

		private TableRow mHeaderRow;
		private TextView txvDate;
		private TextView txvFrom;
		private TextView txvMessage;
		private ImageView txvIssent;

		public SimpleMessageView(Context context, String message, Date timestamp, 
				int monitorID, boolean isOutgoing, boolean issent, boolean isfinalized) {
			super(context);
			Log.i("MessageCursorViewThing", "hello");
			mHeaderRow = new TableRow(context);

			txvDate = new TextView(context);
			txvDate.setTextSize(16);
			txvDate.setPadding(3, 3, 3, 3);
			txvDate.setGravity(Gravity.LEFT);
			
			mHeaderRow.addView(txvDate);

	        txvIssent = new ImageView(getContext());
	        if (issent && isfinalized) {
	        	txvIssent.setImageResource(R.drawable.checkmark_green);
	        } else if (isfinalized) {
	        	txvIssent.setImageResource(R.drawable.checkmark_blue);
	        } else {
	        	txvIssent.setImageResource(R.drawable.checkmark_grey);
	        }
	        mHeaderRow.addView(txvIssent);
	        
			addView(mHeaderRow);

			txvMessage = new TextView(context);
			txvMessage.setTextSize(12);
			txvMessage.setPadding(8, 2, 8, 2);
			this.addView(txvMessage);

			this.setColumnStretchable(0, true);
			this.setColumnStretchable(1, true);
			
			
			setData(message, timestamp, monitorID, isOutgoing, issent, isfinalized);
			// TODO Auto-generated constructor stub
		}

		public void setData(String message, Date timestamp, int monitorID, boolean isOutgoing, boolean issent, boolean isfinalized) {
			txvDate.setText(Message.DisplayDateTimeFormat.format(timestamp));

			Monitor m = MessageTranslator.GetMonitor(getContext(), monitorID);
			 if (issent && isfinalized) {
		        txvIssent.setImageResource(R.drawable.checkmark_green);
	        } else if (isfinalized) {
	        	txvIssent.setImageResource(R.drawable.checkmark_blue);
	        } else {
	        	txvIssent.setImageResource(R.drawable.checkmark_grey);
	        }
			txvMessage.setText(message);
		}

	}

}
