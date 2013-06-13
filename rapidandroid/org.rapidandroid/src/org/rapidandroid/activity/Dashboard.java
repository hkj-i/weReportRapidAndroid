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
package org.rapidandroid.activity;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.rapidandroid.R;
import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidandroid.content.translation.XMLTranslator;
import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidandroid.data.controller.DashboardDataLayer;
import org.rapidandroid.data.controller.MessageDataReporter;
import org.rapidandroid.data.controller.ParsedDataReporter;
import org.rapidandroid.view.SingleRowHeaderView;
import org.rapidandroid.view.adapter.FormDataGridCursorAdapter;
import org.rapidandroid.view.adapter.MessageCursorAdapter;
import org.rapidandroid.view.adapter.SummaryCursorAdapter;
import org.rapidsms.java.core.Constants;
import org.rapidsms.java.core.model.Field;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.model.Message;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

/**
 * Main entry point activity for RapidAndroid. It is a simple view with a
 * pulldown for for form type, and a listview of messages below that pertain to
 * that message.
 * 
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 9, 2009
 * 
 */
public class Dashboard extends Activity {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		//super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setTitle("RapidAndroid :: Dashboard");
		setContentView(R.layout.dashboard);

		this.initFormSpinner();
		// Set the event listeners for the spinner and the listview
		Spinner spin_forms = (Spinner) findViewById(R.id.cbx_forms);
		spin_forms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View theview, int position, long rowid) {
				spinnerItemSelected(position);
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// blow away the listview's items
				mChosenForm = null;
				resetCursor = true;
				loadListViewWithFormData();
			}
		});

		// add some events to the listview
		ListView lsv = (ListView) findViewById(R.id.lsv_dashboardmessages);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels - 8;

		lsv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		// // bind a context menu
		// lsv.setOnCreateContextMenuListener(new
		// View.OnCreateContextMenuListener() {
		// public void onCreateContextMenu(ContextMenu menu, View v,
		// ContextMenuInfo menuInfo) {
		// if (mChosenForm != null) {
		// menu.add(0, CONTEXT_ITEM_SUMMARY_VIEW, 0, "Summary View");
		// menu.add(0, CONTEXT_ITEM_TABLE_VIEW, 0, "Table View");
		// } else {
		// menu.clear();
		// }
		// }
		// });

		lsv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int position, long row) {
				if (adapter.getAdapter().getClass().equals(SummaryCursorAdapter.class)) {
					((SummaryCursorAdapter) adapter.getAdapter()).toggle(position);
				}
			}
		});
		rb100 = (RadioButton) findViewById(R.id.dashboard_rad_100);
		rb100.setOnClickListener(radioClickListener);

		rb500 = (RadioButton) findViewById(R.id.dashboard_rad_500);
		rb500.setOnClickListener(radioClickListener);

		rball = (RadioButton) findViewById(R.id.dashboard_rad_all);
		rball.setOnClickListener(radioClickListener);

		rb100.setChecked(true);

		// by default on startup:
		// mEndDate = new Date();
		// mStartDate = new Date();
		// mStartDate.setDate(mEndDate.getDate() - 7);

		mViewSwitcher = (ViewSwitcher) findViewById(R.id.dashboard_switcher);

		mHeaderTable = (TableLayout) findViewById(R.id.dashboard_headertbl);
		// these animations are too fracking slow
		// Animation in = AnimationUtils.loadAnimation(this,
		// android.R.anim.fade_in);
		// Animation out = AnimationUtils.loadAnimation(this,
		// android.R.anim.fade_out);
		// mViewSwitcher.setInAnimation(in);
		// mViewSwitcher.setOutAnimation(out);
		
		mFormViewMode = LISTVIEW_MODE_TABLE_VIEW;
		this.mBtnViewModeSwitcher = (ImageButton) findViewById(R.id.btn_switch_mode);
		// hee - making summary button go away
		mBtnViewModeSwitcher.setVisibility(View.INVISIBLE);
		mFormViewMode = LISTVIEW_MODE_TABLE_VIEW;
		/*
		 * hee commented it out because we don't use the summary view 
		mBtnViewModeSwitcher.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// this is on click, so we want to toggle it!
				switch (mFormViewMode) {
					case LISTVIEW_MODE_SUMMARY_VIEW:
						mFormViewMode = LISTVIEW_MODE_TABLE_VIEW;

						break;
					case LISTVIEW_MODE_TABLE_VIEW:
						mFormViewMode = LISTVIEW_MODE_SUMMARY_VIEW;

						break;
				}
				resetCursor = false;
				beginListViewReload();
			}
		});
		*/
		
		// TODO I added this code, hope it doesn't break things...
		
		//File externalStorageDir = Environment.getExternalStorageDirectory();
		//String odkFormsPath = externalStorageDir.getAbsoluteFile() + "/odk/forms/";
		//formWatcher = new FormCreationFileObserver(odkFormsPath);
		//formWatcher.addContext(this);
		//formWatcher.startWatching();
		//Log.i("Dashboard", "Called file observer constructer");
		
	}

	private SingleRowHeaderView headerView;
	private SummaryCursorAdapter summaryView;
	private FormDataGridCursorAdapter rowView;
	private MessageCursorAdapter messageCursorAdapter;

	private ViewSwitcher mViewSwitcher;
	private TableLayout mHeaderTable;
	private int traceCount = 0;

	// private ProgressDialog mLoadingDialog;

	private Form mChosenForm = null;
	private boolean mShowAllMessages = false;

	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_FORM_REVIEW = 1;
	// private static final int ACTIVITY_DATERANGE = 2;
	private static final int ACTIVITY_CHARTS = 3; // this and ACTIVITY_CHARTS
	private static final int ACTIVITY_GLOBALSETTINGS = 4;

	private static final int MENU_CREATE_ID = Menu.FIRST;
	private static final int MENU_FORM_REVIEW_ID = Menu.FIRST + 1;
	private static final int MENU_CHANGE_DATERANGE = Menu.FIRST + 2;
	private static final int MENU_CHARTS_ID = Menu.FIRST + 3;
	private static final int MENU_GLOBAL_SETTINGS = Menu.FIRST + 4;
	// private static final int MENU_SHOW_REPORTS = Menu.FIRST + 3;
	// private static final int MENU_EXIT = Menu.FIRST + 3; //waitaminute, we
	// don't want to exit this thing, do we?
	
	// hee hee
	private static final int RESULT_RETRIEVE_FORM = 7;
	private static String PROJECT_NAME = "capstone_report";
	private static String ODK_INSTANCE = "content://org.odk.collect.android.provider.odk.instances/instances/";
	
	// ----

	private static final String STATE_DATE_START = "startdate";
	private static final String STATE_DATE_END = "enddate";
	private static final String STATE_SPINNER_POSITION = "spinneritem";
	private static final String STATE_SELECTED_FORM = "selectedform";
	private static final String STATE_LSV_POSITION = "listposition";
	private static final String STATE_LSV_VIEWMODE = "viewmode";
	private static final String STATE_RAD_INDEX = "radselected";

	// private static final int CONTEXT_ITEM_SUMMARY_VIEW = Menu.FIRST;
	// private static final int CONTEXT_ITEM_TABLE_VIEW = Menu.FIRST + 1;
	// private static final int CONTEXT_ITEM_TEST3 = ContextMenu.FIRST + 2;
	// private static final int CONTEXT_ITEM_TEST4 = ContextMenu.FIRST + 3;

	private static final int LISTVIEW_MODE_SUMMARY_VIEW = 0;
	private static final int LISTVIEW_MODE_TABLE_VIEW = 1;
	// private static final int LISTVIEW_MODE_SUMMARY_VIEW = 0;

	private static final int SHOW_ALL = 5000;
	private static final CharSequence TXT_WAIT = "Please Wait...";

	private int mFormViewMode = LISTVIEW_MODE_TABLE_VIEW;
	private ImageButton mBtnViewModeSwitcher;

	private Form[] mAllForms;
	
	// hee
	private String mChosenMessage = null; // hee
	// hee
		
	boolean mIsInitializing = false;
	boolean resetCursor = true;
	Cursor mListviewCursor = null;

	// private Date mStartDate = Constants.NULLDATE;
	// private Date mEndDate = Constants.NULLDATE;

	private int mScreenWidth;
	private int mListCount = 100;
	private RadioButton rb100;
	private RadioButton rb500;
	private RadioButton rball;

	private OnClickListener radioClickListener = new OnClickListener() {

		public void onClick(View v) {
			RadioButton buttonView = (RadioButton) v;
			if (buttonView.equals(rb100)) {
				mListCount = 100;
				rb100.setChecked(true);
				rb500.setChecked(false);
				rball.setChecked(false);
			} else if (buttonView.equals(rb500)) {
				mListCount = 500;
				rb100.setChecked(false);
				rb500.setChecked(true);
				rball.setChecked(false);

			} else if (buttonView.equals(rball)) {
				mListCount = SHOW_ALL;
				rb100.setChecked(false);
				rb500.setChecked(false);
				rball.setChecked(true);

			}
			if (!mIsInitializing) {
				resetCursor = true;
				beginListViewReload();
			}

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setTitle("RapidAndroid :: Dashboard");
		setContentView(R.layout.dashboard);

		this.initFormSpinner();
		// Set the event listeners for the spinner and the listview
		Spinner spin_forms = (Spinner) findViewById(R.id.cbx_forms);
		spin_forms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View theview, int position, long rowid) {
				spinnerItemSelected(position);
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// blow away the listview's items
				mChosenForm = null;
				resetCursor = true;
				loadListViewWithFormData();
			}
		});

		// add some events to the listview
		ListView lsv = (ListView) findViewById(R.id.lsv_dashboardmessages);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels - 8;

		lsv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		// // bind a context menu
		// lsv.setOnCreateContextMenuListener(new
		// View.OnCreateContextMenuListener() {
		// public void onCreateContextMenu(ContextMenu menu, View v,
		// ContextMenuInfo menuInfo) {
		// if (mChosenForm != null) {
		// menu.add(0, CONTEXT_ITEM_SUMMARY_VIEW, 0, "Summary View");
		// menu.add(0, CONTEXT_ITEM_TABLE_VIEW, 0, "Table View");
		// } else {
		// menu.clear();
		// }
		// }
		// });

		lsv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int position, long row) {
				if (adapter.getAdapter().getClass().equals(SummaryCursorAdapter.class)) {
					((SummaryCursorAdapter) adapter.getAdapter()).toggle(position);
				}
			}
		});
		rb100 = (RadioButton) findViewById(R.id.dashboard_rad_100);
		rb100.setOnClickListener(radioClickListener);

		rb500 = (RadioButton) findViewById(R.id.dashboard_rad_500);
		rb500.setOnClickListener(radioClickListener);

		rball = (RadioButton) findViewById(R.id.dashboard_rad_all);
		rball.setOnClickListener(radioClickListener);

		rb100.setChecked(true);

		// by default on startup:
		// mEndDate = new Date();
		// mStartDate = new Date();
		// mStartDate.setDate(mEndDate.getDate() - 7);

		mViewSwitcher = (ViewSwitcher) findViewById(R.id.dashboard_switcher);

		mHeaderTable = (TableLayout) findViewById(R.id.dashboard_headertbl);
		// these animations are too fracking slow
		// Animation in = AnimationUtils.loadAnimation(this,
		// android.R.anim.fade_in);
		// Animation out = AnimationUtils.loadAnimation(this,
		// android.R.anim.fade_out);
		// mViewSwitcher.setInAnimation(in);
		// mViewSwitcher.setOutAnimation(out);
		
		mFormViewMode = LISTVIEW_MODE_TABLE_VIEW;
		this.mBtnViewModeSwitcher = (ImageButton) findViewById(R.id.btn_switch_mode);
		// hee - making summary button go away
		mBtnViewModeSwitcher.setVisibility(View.INVISIBLE);
		mFormViewMode = LISTVIEW_MODE_TABLE_VIEW;
		/*
		 * hee commented it out because we don't use the summary view 
		mBtnViewModeSwitcher.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// this is on click, so we want to toggle it!
				switch (mFormViewMode) {
					case LISTVIEW_MODE_SUMMARY_VIEW:
						mFormViewMode = LISTVIEW_MODE_TABLE_VIEW;

						break;
					case LISTVIEW_MODE_TABLE_VIEW:
						mFormViewMode = LISTVIEW_MODE_SUMMARY_VIEW;

						break;
				}
				resetCursor = false;
				beginListViewReload();
			}
		});
		*/
		
		// TODO I added this code, hope it doesn't break things...
		
		//File externalStorageDir = Environment.getExternalStorageDirectory();
		//String odkFormsPath = externalStorageDir.getAbsoluteFile() + "/odk/forms/";
		//formWatcher = new FormCreationFileObserver(odkFormsPath);
		//formWatcher.addContext(this);
		//formWatcher.startWatching();
		//Log.i("Dashboard", "Called file observer constructer");
		
	}
	//private FormCreationFileObserver formWatcher;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_SPINNER_POSITION)
					// && savedInstanceState.containsKey(STATE_LSV_POSITION)
					&& savedInstanceState.containsKey(STATE_LSV_VIEWMODE)
					&& savedInstanceState.containsKey(STATE_RAD_INDEX) // savedInstanceState.containsKey(STATE_DATE_START)
			// &&
			// savedInstanceState.containsKey(STATE_DATE_END)
			// STATE_RAD_COUNT
			// && savedInstanceState.containsKey(STATE_SELECTED_FORM)
			) {

				// mStartDate.setTime(savedInstanceState.getLong(STATE_DATE_START));
				// mEndDate.setTime(savedInstanceState.getLong(STATE_DATE_END));

				mIsInitializing = true;
				int chosenRadio = savedInstanceState.getInt(STATE_RAD_INDEX);
				if (chosenRadio == 0) {
					rb100.setChecked(true);
					this.mListCount = 100;
				} else if (chosenRadio == 1) {
					rb500.setChecked(true);
					this.mListCount = 500;
				} else if (chosenRadio == 2) {
					rball.setChecked(true);
					this.mListCount = 5000;
				}

				mIsInitializing = false;
				mFormViewMode = savedInstanceState.getInt(STATE_LSV_VIEWMODE);

				Spinner spin_forms = (Spinner) findViewById(R.id.cbx_forms);
				spin_forms.setSelection(savedInstanceState.getInt(STATE_SPINNER_POSITION));
			}

			// String from = savedInstanceState.getString("from");
			// String body = savedInstanceState.getString("body");
			// //dialogMessage = "SMS :: " + from + " : " + body;
			// //showDialog(160);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		// outState.putLong(STATE_DATE_START, mStartDate.getTime());
		// outState.putLong(STATE_DATE_END, mEndDate.getTime());

		int chosenRadio = 0;

		if (rb100.isChecked()) {
			chosenRadio = 0;
		} else if (rb500.isChecked()) {
			chosenRadio = 1;
		} else if (rball.isChecked()) {
			chosenRadio = 2;
		}
		outState.putInt(STATE_RAD_INDEX, chosenRadio);
		outState.putInt(STATE_LSV_VIEWMODE, mFormViewMode);
		Spinner spin_forms = (Spinner) findViewById(R.id.cbx_forms);
		outState.putInt(STATE_SPINNER_POSITION, spin_forms.getSelectedItemPosition());

	}

	// hee
		private void createInstanceAndCallODK(Uri formData) {
	        AlertDialog.Builder alert = new AlertDialog.Builder(Dashboard.this);
	        Log.i("createInstanceAndCallODK", "alert");
			String path = formData.getPath();
			String folder = "/forms/";
			String formId = path.substring(path.indexOf("/forms/") + folder.length());
			 Log.i("createInstanceAndCallODK", "formid = " + formId);
			ContentResolver resolver = this.getContentResolver();
			Cursor formInfoRow = resolver.query(Uri.parse("content://org.odk.collect.android.provider.odk.forms/forms"),
												null,
												"_id = " + formId,
												null,
												null);

			formInfoRow.moveToFirst();
			//String formName = formInfoRow.getString(formInfoRow.getColumnIndex("jrFormId"));
			// get the display name of the form
			String formName = formInfoRow.getString(formInfoRow.getColumnIndex("displayName"));
			 Log.i("createInstanceAndCallODK", "formname = " + formName);

			// find form 
			// now get message id from chosen message
			// get the message body
			// and save an xml
			// and open collect

			// get the form prefix using the form name
			 Context context = getApplicationContext();
			 String[] stringArgs = {formName};
			Cursor formRow = context.getContentResolver().query(RapidSmsDBConstants.Form.CONTENT_URI, 
						null, 
						"formname = ?", 
						stringArgs, 
						null);
				formRow.moveToFirst();
			String prefix = formRow.getString(formRow.getColumnIndex("prefix"));

			XMLTranslator XMLGenerator = new XMLTranslator();
			XMLGenerator.initFormCache();
			Form f = XMLGenerator.determineForm(prefix + "   " + "randomStuff");
			if (f == null) {
				Log.i("createInstanceAndCallODK", "no form id");
			} else {
				Log.i("createInstanceAndCallODK", "form was returned");
			}
			int msg_id = Integer.parseInt(mChosenMessage);
			 Log.i("createInstanceAndCallODK", "msg_id : " + msg_id);

			 // insert the message into formdata_"---" database
			 ContentValues cv = new ContentValues();
			 cv.put(RapidSmsDBConstants.FormData.MESSAGE, msg_id);
			 Field[] fields = f.getFields();
			 int len = fields.length;
			 Log.i("createInstanceAndCallODK", "form field length :" + len);

			 // insert blank value into every field other than the message id
			for (int i = 0; i < len; i++) {
				Field field = fields[i];
				cv.put(RapidSmsDBConstants.FormData.COLUMN_PREFIX + field.getName(), "");
			}

			 Uri inserted = getApplicationContext().getContentResolver().insert(
						Uri.parse(RapidSmsDBConstants.FormData.CONTENT_URI_PREFIX
								+ f.getFormId()), cv);


				try {
					XMLGenerator.buildOpenRosaXform(getApplicationContext(), msg_id, f);
					 Log.i("createInstanceAndCallODK", "generated xml");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					 Log.i("createInstanceAndCallODK", "failed to generate xml");
				}


				//open collect
				Cursor messageRow = resolver.query(RapidSmsDBConstants.Message.CONTENT_URI, 
						null, 
						"_id = " + msg_id, 
						null, 
						null);
				 Log.i("createInstanceAndCallODK", "get message row");
				messageRow.moveToFirst();
				mChosenMessage = null;
				String form_uri_s = ODK_INSTANCE + messageRow.getString(messageRow.getColumnIndex("form_uri"));
				Log.i("odk call uri: ", form_uri_s);
				// open collect with this

				Uri messageUri = Uri.parse(form_uri_s);

			     Intent intent = new Intent();
			     intent.setComponent(new ComponentName("org.odk.collect.android",
			             "org.odk.collect.android.activities.FormEntryActivity"));
			     intent.setAction(Intent.ACTION_EDIT);
			     intent.setData(messageUri);

			    startActivity(intent);

			}		

			@Override
			protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
				super.onActivityResult(requestCode, resultCode, intent);
				Bundle extras = null;
				if (intent != null) {
					extras = intent.getExtras(); // right now this is a case where we
					// don't do much activity back and
					// forth
				}

				switch (requestCode) {
			    // hee code
			    case RESULT_RETRIEVE_FORM:
			    	if (resultCode == RESULT_OK) {
			    		Log.i("onActivityResult", intent.getData().toString());
			    		createInstanceAndCallODK(intent.getData());
			    	}
			    	break;
			    	//end

				case ACTIVITY_CREATE:
					// we should do an update of the view
					initFormSpinner();
					resetCursor = true;
					beginListViewReload();
					break;
				case ACTIVITY_FORM_REVIEW:
					// dialogMessage = "Activity Done";
					// showDialog(12);
					resetCursor = true;
					beginListViewReload();
					break;
				case ACTIVITY_CHARTS:
					// dialogMessage = "Activity Done";
					// showDialog(13);
					resetCursor = true;
					beginListViewReload();
					break;
				case ACTIVITY_GLOBALSETTINGS:
					resetCursor = true;
					beginListViewReload();
					break;
				// case ACTIVITY_DATERANGE:
				// if (extras != null) {
				// mStartDate = new
				// Date(extras.getLong(DateRange.ResultParams.RESULT_START_DATE));
				// mEndDate = new
				// Date(extras.getLong(DateRange.ResultParams.RESULT_END_DATE));
				// resetCursor = true;
				// beginListViewReload();
				//
				// }
				// break;
			}
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// add images:
		// http://developerlife.com/tutorials/?p=304
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_CREATE_ID, 0, R.string.dashboard_menu_create).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, MENU_FORM_REVIEW_ID, 0, R.string.dashboard_menu_edit).setIcon(android.R.drawable.ic_menu_agenda);
		// menu.add(0, MENU_CHANGE_DATERANGE, 0,
		// R.string.chart_menu_change_parameters.setIcon(android.R.drawable.ic_menu_recent_history);
		menu.add(0, MENU_CHARTS_ID, 0, R.string.dashboard_menu_view).setIcon(android.R.drawable.ic_menu_sort_by_size);
		menu.add(0, MENU_GLOBAL_SETTINGS, 0, "Change Settings").setIcon(android.R.drawable.ic_menu_preferences);
		// menu.add(0, MENU_SHOW_REPORTS, 0,
		// R.string.dashboard_menu_show_reports);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case MENU_CREATE_ID:
				startActivityFormCreate();
				return true;
			case MENU_FORM_REVIEW_ID:
				startActivityFormReview();
				return true;

				// case MENU_CHANGE_DATERANGE:
				// startDateRangeActivity();
				// return true;
			case MENU_CHARTS_ID:
				startActivityChart();
				return true;
			case MENU_GLOBAL_SETTINGS:
				startActivityGlobalSettings();
				return true;
		}
		return true;
	}

	

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Flip the enabled status of menu items depending on selection of a
		// form
		super.onPrepareOptionsMenu(menu);

		boolean formOptionsEnabled = false;
		if (this.mChosenForm != null) {
			formOptionsEnabled = true;
		}

		MenuItem editMenu = menu.findItem(MENU_FORM_REVIEW_ID);
		editMenu.setEnabled(formOptionsEnabled);
		MenuItem viewMenu = menu.findItem(MENU_CHARTS_ID);

		return true;
	}

	// @Override
	// // http://www.anddev.org/tinytutcontextmenu_for_listview-t4019.html
	// // UGH, things changed from .9 to 1.0
	// public boolean onContextItemSelected(MenuItem item) {
	// AdapterView.AdapterContextMenuInfo menuInfo =
	// (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	// switch (item.getItemId()) {
	// case CONTEXT_ITEM_SUMMARY_VIEW:
	// mFormViewMode = LISTVIEW_MODE_SUMMARY_VIEW;
	// break;
	// case CONTEXT_ITEM_TABLE_VIEW:
	// mFormViewMode = LISTVIEW_MODE_TABLE_VIEW;
	// break;
	// default:
	// return super.onContextItemSelected(item);
	// }
	// this.resetCursor = false;
	// beginListViewReload();
	// return true;
	// }

	/**
	 * @deprecated
	 */
	@Deprecated
	private void startActivityDateRange() {
		Intent i = new Intent(this, DateRange.class);
		// Date endDate = java.sql.Date.
		Date endDate = new Date();
		if (mChosenForm != null) {
			endDate = ParsedDataReporter.getOldestMessageDate(this, mChosenForm);
			if (endDate.equals(Constants.NULLDATE)) {
				Builder noDateDialog = new AlertDialog.Builder(this);
				noDateDialog.setPositiveButton("Ok", null);
				noDateDialog.setTitle("Alert");
				noDateDialog.setMessage("This form has no messages or data to chart");
				noDateDialog.show();
				return;
			}
		} else {
			endDate = MessageDataReporter.getOldestMessageDate(this);
		}
		i.putExtra(DateRange.CallParams.ACTIVITY_ARG_STARTDATE, endDate.getTime());
		// startActivityForResult(i, ACTIVITY_DATERANGE);

	}

	/**
	 * 
	 */
	private void startActivityGlobalSettings() {
		Intent i;
		i = new Intent(this, GlobalSettings.class);
		startActivityForResult(i, ACTIVITY_GLOBALSETTINGS);
		
	}
	
	// Start the form edit/create activity
	private void startActivityFormReview() {
		Intent i;
		i = new Intent(this, FormReviewer.class);
		i.putExtra(FormReviewer.CallParams.REVIEW_FORM, mChosenForm.getFormId());
		startActivityForResult(i, ACTIVITY_FORM_REVIEW);
	}

	// Start the form edit/create activity
	private void startActivityFormCreate() {
		Intent i;
		i = new Intent(this, FormCreator.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	private void startActivityChart() {
		// Debug.stopMethodTracing();
		if (mListviewCursor == null) {
			Builder noDataDialog = new AlertDialog.Builder(this);
			noDataDialog.setPositiveButton("Ok", null);
			noDataDialog.setTitle("Alert");
			noDataDialog.setMessage("There is no data to chart.");
			noDataDialog.show();
			return;
		}

		Intent i = new Intent(this, ChartData.class);
		Date now = new Date();
		i.putExtra(ChartData.CallParams.END_DATE, now.getTime());
		// we want to chart for a form
		if (mChosenForm != null && !mShowAllMessages) {
			Date startDate = ParsedDataReporter.getOldestMessageDate(this, mChosenForm);
			if (startDate.equals(Constants.NULLDATE)) {
				Builder noDateDialog = new AlertDialog.Builder(this);
				noDateDialog.setPositiveButton("Ok", null);
				noDateDialog.setTitle("Alert");
				noDateDialog.setMessage("This form has no messages or data to chart");
				noDateDialog.show();
				return;
			}

			if (mListviewCursor.getCount() > 0) {
				mListviewCursor.moveToLast();
				// int msg_id =
				// mListviewCursor.getInt(Message.COL_PARSED_MESSAGE_ID);
				String datestring = mListviewCursor.getString(mListviewCursor.getColumnCount()
						+ Message.COL_JOINED_MESSAGE_TIME);

				try {
					startDate = Message.SQLDateFormatter.parse(datestring);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Message m = MessageTranslator.GetMessage(this, msg_id);

			} else {
				Calendar startCal = Calendar.getInstance();
				startCal.add(Calendar.DATE, -7);
				startDate = startCal.getTime();
			}
			i.putExtra(ChartData.CallParams.START_DATE, startDate.getTime());
			i.putExtra(ChartData.CallParams.CHART_FORM, mChosenForm.getFormId());
		} else if (mShowAllMessages) {
			// Chart for messages
			Date startDate = null;
			boolean setDate = false;
			if (mListviewCursor.getCount() > 0) {
				mListviewCursor.moveToLast();
				try {
					startDate = Message.SQLDateFormatter.parse(mListviewCursor.getString(Message.COL_TIME));
				} catch (ParseException e) {
					setDate = true;
				}
			} else {
				setDate = true;
			}
			if (setDate) {
				Calendar startCal = Calendar.getInstance();
				startCal.add(Calendar.DATE, -7);
				startDate = startCal.getTime();
			}

			mListviewCursor.moveToLast();
			i.putExtra(ChartData.CallParams.START_DATE, startDate.getTime());

			i.putExtra(ChartData.CallParams.CHART_MESSAGES, true);
		}

		// i.putExtra(ChartData.CallParams.START_DATE, mStartDate.getTime());
		// i.putExtra(ChartData.CallParams.END_DATE, mEndDate.getTime());
		startActivityForResult(i, ACTIVITY_CHARTS);
	}

	// This is a call to the DB to get all the forms that this form can support.
	private void initFormSpinner() {
		// The steps:
		// get the spinner control from the layouts
		Spinner spin_forms = (Spinner) findViewById(R.id.cbx_forms);
		// Get an array of forms from the DB
		// in the current iteration, it's mForms
		this.mAllForms = ModelTranslator.getAllForms();

		String[] monitors = new String[mAllForms.length + 1];

		for (int i = 0; i < mAllForms.length; i++) {
			monitors[i] = "Form: " + mAllForms[i].getFormName();
		}

		// add some special selections:
		monitors[monitors.length - 1] = "Show all Messages";
		// monitors[monitors.length - 1] = "Show Monitors";

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, monitors);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// apply it to the spinner
		spin_forms.setAdapter(adapter);
		spin_forms.setSelection(monitors.length - 1); //nicole
	}

	private void spinnerItemSelected(int position) {
		if (position == mAllForms.length) {
			// if it's forms+1, then it's ALL messages
			mChosenForm = null;
			this.mShowAllMessages = true;
			resetCursor = true;
			beginListViewReload();
			// loadListViewWithRawMessages();

		} else {
			this.mShowAllMessages = false;
			mChosenForm = mAllForms[position];
			resetCursor = true;
			beginListViewReload();
		}
	}

	private synchronized void finishListViewReload() {
		if (mListviewCursor == null) {
			return;
		}
		TextView lbl_recents = (TextView) findViewById(R.id.lbl_dashboardmessages);

		lbl_recents.setText(this.mListviewCursor.getCount() + " Messages");

		ListView lsv = (ListView) findViewById(R.id.lsv_dashboardmessages);

		if (mChosenForm != null && !mShowAllMessages) {
			loadListViewWithFormData();
		} else if (mShowAllMessages && mChosenForm == null) {
			this.mBtnViewModeSwitcher.setVisibility(View.INVISIBLE);
			this.messageCursorAdapter = new MessageCursorAdapter(this, mListviewCursor);
			lsv.setAdapter(messageCursorAdapter);
		}
		// lsv.setVisibility(View.VISIBLE);
		
		// hee TODO
		lsv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int position, long row) {
				Log.i("onItemClick ", "clicked");
				Cursor c = mListviewCursor;
				
				c.moveToPosition(position);
				ContentResolver resolver = getContentResolver();

		        // String[] splitMessage = messageRow.getString(messageRow.getColumnIndex("message")).split(" "); 
				
				String messageId = null;
				if (c.getColumnIndex("message_id") == -1
						) { //&& mFormViewMode == Dashboard.LISTVIEW_MODE_TABLE_VIEW) {
					// summary view
					Log.i("entered", "hi");
											
					messageId = c.getString(c.getColumnIndex("_id"));
					
					Log.i("all surveys - id : ", messageId);
					
					
					if (c.getString(c.getColumnIndex("form_uri")) == null) {
						// instance file doesn't exist; create one
						// direct the user to choose a form
						mChosenMessage = messageId;
						
						// choose form
					     ContentValues formvalues = new ContentValues();
					     Intent formintent = new Intent();
					     formintent.setComponent(new ComponentName("org.odk.collect.android",
					             "org.odk.collect.android.activities.FormChooserList"));
					     formintent.setAction(Intent.ACTION_PICK);
					     Log.i("Dashboard", "form chooser list");
					     startActivityForResult(formintent, RESULT_RETRIEVE_FORM); // seven is a random number TODO
					     
					} else {
						// instance file exists
						// grab the uri and open it
						
						Uri messageUri = Uri.parse(ODK_INSTANCE + 
								c.getString(c.getColumnIndex("form_uri")));
						Log.i("form uri URI ",ODK_INSTANCE +  c.getString(c.getColumnIndex("form_uri")));
						
					     Intent intent = new Intent();
					     intent.setComponent(new ComponentName("org.odk.collect.android",
					             "org.odk.collect.android.activities.FormEntryActivity"));
					     intent.setAction(Intent.ACTION_EDIT);
					     intent.setData(messageUri);
					     Log.i("Dashboard", "all messages, instance exits, message uri: " + messageUri);
					    startActivity(intent);							
					}
				
					
				} else if (mFormViewMode == Dashboard.LISTVIEW_MODE_TABLE_VIEW) {
					messageId = c.getString(c.getColumnIndex("message_id"));
					
					// form view
					Log.i("particular survey - id : ", messageId);
					mChosenMessage = null;
					// get the uri
					// and open it on collect
					Cursor messageRow = resolver.query(RapidSmsDBConstants.Message.CONTENT_URI, 
							null, 
							"_id = " + messageId, 
							null, 
							null);
					messageRow.moveToFirst();			
					for (int i = 0; i < messageRow.getColumnCount(); i++) {
					   	Log.i(i + " Colum name: ", messageRow.getColumnName(i));
				
					}
					messageRow.moveToFirst();
					
					if (messageRow.getString(messageRow.getColumnIndex("form_uri")) != null) {
						// form exists
						Log.i("form uri: ", messageRow.getString(messageRow.getColumnIndex("form_uri")));
						Uri messageUri = Uri.parse(ODK_INSTANCE + 
								messageRow.getString(messageRow.getColumnIndex("form_uri")));
						
						// open it
						Intent intent = new Intent();
						Log.i("Dashboard", "single survey, form exists, message uri: " + messageUri);
					     intent.setComponent(new ComponentName("org.odk.collect.android",
					             "org.odk.collect.android.activities.FormEntryActivity"));
					     intent.setAction(Intent.ACTION_EDIT);
					     intent.setData(messageUri);
					     
					    startActivity(intent);
					     // hee TODO uncomment this out
					} else {
						// TODO here too
						// form does not exist
						
						// create instance file
						// call XML translator
						/*
						XMLTranslator XMLGenerator = new XMLTranslator();
						XMLGenerator.initFormCache();
						
						int msg_id = Integer.parseInt(messageId);
						
						try {
							XMLGenerator.buildOpenRosaXform(getApplicationContext(), msg_id, mChosenForm);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						//grab the uri
						Log.i("form uri: ", messageRow.getString(messageRow.getColumnIndex("form_uri")));
						Uri messageUri = Uri.parse(
								messageRow.getString(messageRow.getColumnIndex("form_uri")));
						
						// open it
						Log.i("Dashboard", "single survey, form didn't exists, message uri: " + messageUri);
						Intent intent = new Intent();
					    intent.setComponent(new ComponentName("org.odk.collect.android",
					             "org.odk.collect.android.activities.FormEntryActivity"));
					    intent.setAction(Intent.ACTION_EDIT);
					    intent.setData(messageUri);
					     
					    startActivity(intent);
					    // hee TODO uncomment this out
					     * 
					     */
					}
				} // end hee
			}
		}); // end of onclick listener

		// end of hee code
	}

	final Handler mDashboardHandler = new Handler();
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			// while(!mIsInitializing) {
			finishListViewReload();
			mViewSwitcher.showNext();
			// }
		}
	};

	private synchronized void beginListViewReload() {
		// Debug.startMethodTracing("listview_load" + traceCount++);
		switch (mFormViewMode) {
			case LISTVIEW_MODE_SUMMARY_VIEW:
				mBtnViewModeSwitcher.setImageResource(R.drawable.summaryview);
				break;
			case LISTVIEW_MODE_TABLE_VIEW:
				mBtnViewModeSwitcher.setImageResource(R.drawable.gridview);
				break;
		}

		this.mIsInitializing = true;
		TextView lbl_recents = (TextView) findViewById(R.id.lbl_dashboardmessages);
		lbl_recents.setText(TXT_WAIT);
		mViewSwitcher.showNext();
		resetListAdapters();
		new Thread(new Runnable() {
			public void run() {
				fillCursorInBackground();
				mIsInitializing = false;
				// finishListViewReload();//might puke
				mDashboardHandler.post(mUpdateResults);
			}
		}).start();
	}

	private synchronized void fillCursorInBackground() {
		if (mListviewCursor == null) {
			if (mChosenForm != null && !mShowAllMessages) {
				mListviewCursor = DashboardDataLayer.getCursorForFormData(this, mChosenForm, mListCount);
			} else if (mShowAllMessages && mChosenForm == null) {
				mListviewCursor = DashboardDataLayer.getCursorForRawMessages(this, mListCount);
			}
		}
	}

	// this is a call to the DB to update the ListView with the messages for a
	// selected form
	private void loadListViewWithFormData() {
		ListView lsv = (ListView) findViewById(R.id.lsv_dashboardmessages);
		this.mBtnViewModeSwitcher.setVisibility(View.VISIBLE);
		if (mChosenForm == null) {
			lsv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
													new String[] { "Select an item" }));
		} else {

			
			
			/*  Nicole: A failed attempt at including survey information on the message list
			
			View view = new View(this);
			
			
			int formID = mChosenForm.getFormId();
			Form mForm = ModelTranslator.getFormById(formID);

			TextView txv_formname = new TextView(this);
			
			TextView txv_prefix = new TextView(this);
			TextView txv_description = new TextView(this);

			ListView lsv_fields = (ListView) findViewById(R.id.lsv_fields);

			txv_formname.setText("Form Name: " + mForm.getFormName());
			txv_prefix.setText("Prefix: " + mForm.getPrefix());
			txv_description.setText("Question: " + mForm.getDescription());

			LinearLayout ll = new LinearLayout(this);
			ll.setOrientation(LinearLayout.VERTICAL);
			ll.addView(txv_formname);
			ll.addView(txv_prefix);
			ll.addView(txv_description);
			LayoutParams lps = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			addContentView(ll, lps);
			*/
			if (mListviewCursor.getCount() == 0) {
				lsv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
														new String[] { "No data" }));
				return;
			}

			/*
			 * if we want to get super fancy, we can do a join to make it all
			 * accessible in one cursor instead of having to requery select
			 * formdata_bednets.,
			 * rapidandroid_message.message,rapidandroid_message.time from
			 * formdata_bednets join rapidandroid_message on
			 * (formdata_bednets.message_id = rapidandroid_message._id)
			 */

			if (this.mFormViewMode == Dashboard.LISTVIEW_MODE_SUMMARY_VIEW) {
				this.summaryView = new SummaryCursorAdapter(this, mListviewCursor, mChosenForm);
				lsv.setAdapter(summaryView);
				Log.i("Dashboard", "listview mode summary view");
			} else if (this.mFormViewMode == Dashboard.LISTVIEW_MODE_TABLE_VIEW) {
				if (this.headerView == null) {
					headerView = new SingleRowHeaderView(this, mChosenForm, mScreenWidth);
					mHeaderTable.addView(headerView);
					int colcount = headerView.getColCount();
					for (int i = 0; i < colcount; i++) {
						mHeaderTable.setColumnShrinkable(i, true);
					}
				}
				rowView = new FormDataGridCursorAdapter(this, mChosenForm, mListviewCursor, mScreenWidth);
				lsv.setAdapter(rowView);
				Log.i("Dashboard", "listview mode table view");
			}
			
		} // form view mode
	}

	/**
	 * @param changedforms
	 */
	private void resetListAdapters() {
		ListView lsv = (ListView) findViewById(R.id.lsv_dashboardmessages);

		if (this.headerView != null) {
			mHeaderTable.removeAllViews();
			headerView = null;
		}

		if (rowView != null) {

			rowView = null;
		}
		if (summaryView != null) {
			summaryView = null;
		}
		if (messageCursorAdapter != null) {
			messageCursorAdapter = null;
		}
		// monitorCursorAdapter

		if (resetCursor) {
			// need to reset the cursor
			if (mListviewCursor != null) {
				mListviewCursor.close();
				mListviewCursor = null;
			}
			resetCursor = false;
		}
	}

}
