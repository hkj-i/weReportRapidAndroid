package org.rapidandroid.activity;

import org.rapidandroid.data.RapidSmsDBConstants;

import org.rapidandroid.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class CreateProject extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_project);
	}
	
	public void saveProject(View view) {
		String projectName = ((EditText) findViewById(R.id.project_name)).getText().toString();
		String projectId = ((EditText) findViewById(R.id.project_id)).getText().toString();
		String projectLocation = ((EditText) findViewById(R.id.project_location)).getText().toString();
		String projectDescription = ((EditText) findViewById(R.id.project_description)).getText().toString();
		
		ContentValues cv = new ContentValues();
		cv.put("name", projectName);
		cv.put("number", projectId);
		cv.put("location", projectLocation);
		cv.put("description", projectDescription);
		
		ContentResolver resolver = this.getContentResolver();
		resolver.insert(RapidSmsDBConstants.Project.CONTENT_URI, cv);
		
		Intent intent = new Intent(this, ProjectChooser.class);
		startActivity(intent);
	}
	
}
