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

package org.rapidandroid.view;

import org.rapidsms.java.core.model.Field;

import android.content.Context;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * @author Daniel Myung dmyung@dimagi.com
 * @created Feb 5, 2009 Summary:
 */
public class SimpleFieldView extends TableLayout {

	private Field mField;
	private TableRow mTopRow;
	private TextView mFieldName;
	private TextView mFieldType;
	private TextView mFieldDesc;

	/**
	 * @param context
	 */
	public SimpleFieldView(Context context, Field field) {
		super(context);
		mField = field;
		// TODO Auto-generated constructor stub
		mTopRow = new TableRow(context);
		mFieldName = new TextView(context);
		mFieldName.setTextSize(18);
		mFieldName.setPadding(4, 4, 4, 4);
		mFieldName.setGravity(Gravity.LEFT);

		mFieldType = new TextView(context);
		mFieldType.setTextSize(16);
		mFieldType.setPadding(4, 4, 4, 8);
		mFieldType.setGravity(Gravity.RIGHT);
		mTopRow.addView(mFieldName);
		mTopRow.addView(mFieldType);

		mFieldDesc = new TextView(context);
		mFieldDesc.setPadding(12, 4, 4, 12);
		mFieldDesc.setTextSize(14);
		this.setColumnStretchable(0, true);
		this.setColumnStretchable(1, true);

		this.addView(mTopRow);
		this.addView(mFieldDesc);
		setField(mField);
	}

	public void setField(Field field) {
		mFieldName.setText(field.getName());
		mFieldType.setText("[" + field.getFieldType().getReadableName() + "]");
		mFieldDesc.setText(field.getDescription());
	}

}
