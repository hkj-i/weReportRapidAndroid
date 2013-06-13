package org.rapidandroid.activity;

/**
 * weReport
 * Choose a project from a list of projects
 * or create one
 */
import org.rapidandroid.R;
import org.rapidandroid.data.RapidSmsDBConstants;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class ProjectChooser extends Activity implements OnItemSelectedListener {
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
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_chooser);
		
		// set up spinner
		Spinner spinner = (Spinner) findViewById(R.id.projects_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ContentResolver resolver = this.getContentResolver();
		String[] projection = {"name"};
		Cursor projectList = resolver.query(RapidSmsDBConstants.Project.CONTENT_URI, 
											projection, 
											null, 
											null, 
											null);
		projectList.moveToFirst();
		String[] projects = new String[projectList.getCount() + 1];
		projects[0] = "No Project ID";
		for (int i = 1; i < projects.length; i++) {
			projects[i] = projectList.getString(0);
			projectList.moveToNext();
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, projects);
		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(this);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		selected = parent.getItemAtPosition(pos).toString();
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
		
	}
	
	public void createProject(View view) {
		Intent intent = new Intent(this, CreateProject.class);
		startActivity(intent);
	}
	private String selected;
	public void choosePhase(View view) {
		Intent intent = new Intent(this, PhaseChooser.class);
		intent.putExtra("project_name", selected);
		
		if (!selected.equals("No Project ID")) {
			ContentResolver resolver = getContentResolver();
			Cursor projectRow = resolver.query(RapidSmsDBConstants.Project.CONTENT_URI, 
												null, 
												"name = \"" + selected + "\"", null, null);
			projectRow.moveToFirst();
			intent.putExtra("location", projectRow.getString(projectRow.getColumnIndex("location")));
		}
		startActivity(intent);
	}
	
}