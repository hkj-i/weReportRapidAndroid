package org.rapidandroid.question;
import java.util.ArrayList;

import org.rapidsms.java.core.model.Field;


public class Question {

	private String questionText;
	private ArrayList<Field> fields;
	
	// TODO use this
	private int questionType;
	
	public static final String PROJECT_INPUT_TAG = "<project>";
	public static final String BUDGET_TAG = "<budget>";
	
	
	public Question() {
		questionText = "";
		fields = new ArrayList<Field>();
		
	}
	
	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}
	
	public void addField(Field field) {
		fields.add(field);
	}
	
	public void setQuestionType(int questionType) {
		this.questionType = questionType;
	}
	
	public String getQuestionText() {
		return questionText;
	}
	
	public ArrayList<Field> getFields() {
		return fields;
	}

	public int getQuestionType() {
		return questionType;
	}
	
}

