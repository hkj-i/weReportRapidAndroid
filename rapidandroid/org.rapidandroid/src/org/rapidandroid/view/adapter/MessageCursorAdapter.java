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

package org.rapidandroid.view.adapter;

import java.util.Date;

import org.rapidandroid.content.translation.MessageTranslator;
import org.rapidsms.java.core.model.Message;
import org.rapidsms.java.core.model.Monitor;

import android.content.Context;
import android.database.Cursor;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
			int MonitorID = cursor.getInt(2);
			String timestamp = cursor.getString(3);
			String message = cursor.getString(4);
			boolean isoutgoing = Boolean.parseBoolean(cursor.getString(4));
			Date hackDate = new Date();
			boolean success = false;
			int isProcessed = cursor.getInt(cursor.getColumnIndex("is_sent"));
			try {
				hackDate = Message.SQLDateFormatter.parse(timestamp);
				success = true;
			} catch (Exception ex) {
				success = false;
			}

			SimpleMessageView srv = (SimpleMessageView) view;
			srv.setData(message, hackDate, MonitorID, isoutgoing, isProcessed);
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
		int MonitorID = cursor.getInt(2);
		String timestamp = cursor.getString(3);
		String message = cursor.getString(4);
		boolean isoutgoing = Boolean.parseBoolean(cursor.getString(4));
		Date hackDate = new Date();

		int isProcessed = cursor.getInt(cursor.getColumnIndex("is_sent"));
		
		SimpleMessageView srv = new SimpleMessageView(context, message, hackDate, MonitorID, isoutgoing, isProcessed);
		return srv;
	}

	private class SimpleMessageView extends TableLayout {

		private TableRow mHeaderRow;
		private TextView txvDate;
		private TextView txvFrom;
		private TextView txvMessage;
		private CheckBox txvProcessed;
		
		public SimpleMessageView(Context context, String message, Date timestamp, int monitorID, boolean isOutgoing, int isProcessed) {
			super(context);
			mHeaderRow = new TableRow(context);

			txvDate = new TextView(context);
			txvDate.setTextSize(16);
			txvDate.setPadding(3, 3, 3, 3);
			txvDate.setGravity(Gravity.LEFT);
			txvFrom = new TextView(context);
			txvFrom.setTextSize(16);
			txvFrom.setPadding(3, 3, 8, 3);
			txvFrom.setGravity(Gravity.RIGHT);

			txvProcessed = new CheckBox(context);
			txvProcessed.setGravity(Gravity.RIGHT);
			//txvProcessed.setOnClickListener("return false");
			
			// this.addView(txvHeader, new
			// LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
			// LayoutParams.WRAP_CONTENT));
			mHeaderRow.addView(txvDate);
			mHeaderRow.addView(txvProcessed);
			addView(mHeaderRow);

			txvMessage = new TextView(context);
			txvMessage.setTextSize(12);
			txvMessage.setPadding(8, 2, 8, 2);
			// this.addView(txvMessage, new
			// LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
			// LayoutParams.WRAP_CONTENT));
			this.addView(txvMessage);

			this.setColumnStretchable(0, true);
			this.setColumnStretchable(1, true);

			setData(message, timestamp, monitorID, isOutgoing, isProcessed);
			// TODO Auto-generated constructor stub
		}

		public void setData(String message, Date timestamp, int monitorID, boolean isOutgoing, int isProcessed) {
			txvDate.setText(Message.DisplayDateTimeFormat.format(timestamp));

			if (isProcessed == 0) {
				txvProcessed.setChecked(false);
			} else {
				txvProcessed.setChecked(true);
			}
			
			Monitor m = MessageTranslator.GetMonitor(getContext(), monitorID);
			//txvFrom.setText(m.getPhone());

			txvMessage.setText(message);
		}

	}

}
