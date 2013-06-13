package org.rapidandroid.question;

import java.util.ArrayList;
import java.util.List;

import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidandroid.data.SurveyCreationConstants;
import org.rapidsms.java.core.model.Field;
import org.rapidsms.java.core.model.SimpleFieldType;
import org.rapidsms.java.core.parser.token.ITokenParser;

import android.util.Log;

public class QuestionBank {

	private List<Question> questions;
	
	public QuestionBank(int phase) {
		
		questions = new ArrayList<Question>();
		
		ModelTranslator mt = new ModelTranslator();
		//int i = 0;
		ITokenParser[] fieldTypes = ModelTranslator.getFieldTypes();
		for (int i = 0; i < fieldTypes.length; i++) {
			Log.i("QuestionBank", "Field Type " + i);
			Log.i("QuestionBank", "Name " + fieldTypes[i].getReadableName());
			//i++;
		}
		
		if (phase == SurveyCreationConstants.SCOPING) {
			
			Question question1 = new Question();
			question1.setQuestionText("Select the most important community project from the following options: <project> <project> <project> <project>.");
			Field question1answer = new Field();
			question1answer.setDescription("Most Important Project");
			question1answer.setFieldId(SurveyCreationConstants.AnswerTypes.INTEGER);
			question1answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.INTEGER - 1]);
			question1answer.setName("Most_Important_Project");
			question1answer.setSequenceId(1);
			question1.addField(question1answer);
			question1.setQuestionType(SurveyCreationConstants.QuestionTypes.MULTIPLECHOICE);
			
			
			Question question2 = new Question();
			question2.setQuestionText("Rate your potential participation level in <project>.");
			Field question2answer = new Field();
			question2answer.setDescription("Potential Participation Rating");
			question2answer.setFieldId(SurveyCreationConstants.AnswerTypes.INTEGER);
			question2answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.INTEGER - 1]);
			question2answer.setName("Potential_Participation_Rating");
			question2answer.setSequenceId(1);
			question2.addField(question2answer);
			question2.setQuestionType(SurveyCreationConstants.QuestionTypes.RATING);
			
			Question question3 = new Question();
			question3.setQuestionText("Rate your trust of project supervisors.");
			Field question3answer = new Field();
			question3answer.setDescription("Supervisor Trust Rating");
			question3answer.setFieldId(SurveyCreationConstants.AnswerTypes.INTEGER);
			question3answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.INTEGER - 1]);
			question3answer.setName("Supervisor_Trust_Rating");
			question3answer.setSequenceId(1);
			question3.addField(question3answer);
			question3.setQuestionType(SurveyCreationConstants.QuestionTypes.RATING);
			
			Question question4 = new Question();
			question4.setQuestionText("The budget for this project is $1000. Do you approve this amount to be spent on <project>? ");
			Field question4answer = new Field();
			question4answer.setDescription("Project Budget Approval");
			question4answer.setFieldId(SurveyCreationConstants.AnswerTypes.YESNO);
			question4answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.YESNO - 1]);
			question4answer.setName("Project_Budget_Approval");
			question4answer.setSequenceId(1);
			question4.addField(question4answer);
			question4.setQuestionType(SurveyCreationConstants.QuestionTypes.YESNO);
			/*
			Question question5 = new Question();
			question5.setQuestionText("The budget for this project is <budget>. Do you approve this amount to be spent on <project>? ");
			Field question5answer = new Field();
			question5answer.setDescription("Project Budget Approval");
			question5answer.setFieldId(SurveyCreationConstants.AnswerTypes.YESNO);
			question5answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.YESNO - 1]);
			question5answer.setName("Project Budget Approval");
			question5answer.setSequenceId(1);
			question5.addField(question1answer);
			question5.setQuestionType(SurveyCreationConstants.QuestionTypes.YESNO);
			
			*/
			/* TODO this question is buggy when being displayed, need to uncomment eventually
			Question question6 = new Question();
			question6.setQuestionText("The budget for <project> is $1000. Is there another project that would be more useful with $1000?");
			Field question6answer = new Field();
			question6answer.setDescription("Project Budget Approval");
			question6answer.setFieldId(SurveyCreationConstants.AnswerTypes.YESNO);
			question6answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.YESNO - 1]);
			question6answer.setName("Other_Project_Suggested");
			question6answer.setSequenceId(1);
			question6.addField(question6answer);
			question6.setQuestionType(SurveyCreationConstants.QuestionTypes.YESNO);
			*/
			questions.add(question1);
			questions.add(question2);
			questions.add(question3);
			questions.add(question4);
			//questions.add(question5);
			//questions.add(question6);
			
			
		}
		
		if (phase == SurveyCreationConstants.PROJECT) {
			Question question1 = new Question();
			question1.setQuestionText("Is someone from your community stealing or misusing project resources?");
			Field question1answer = new Field();
			question1answer.setDescription("Community Misuse of Funds");
			question1answer.setFieldId(SurveyCreationConstants.AnswerTypes.YESNO);
			question1answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.YESNO - 1]);
			question1answer.setName("Community_Misuse_of_Funds");
			question1answer.setSequenceId(1);
			question1.addField(question1answer);
			
			Question question2 = new Question();
			question2.setQuestionText("Is someone who is in charge of the project stealing or misusing project resources?");
			Field question2answer = new Field();
			question2answer.setDescription("Supervisor Misuse of Funds");
			question2answer.setFieldId(SurveyCreationConstants.AnswerTypes.YESNO);
			question2answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.YESNO - 1]);
			question2answer.setName("Supervisor_Misuse_of_Funds");
			question2answer.setSequenceId(1);
			question2.addField(question2answer);
			
			Question question3 = new Question();
			question3.setQuestionText("Are women being included equally in this project?");
			Field question3answer = new Field();
			question3answer.setDescription("Inclusion of Women");
			question3answer.setFieldId(SurveyCreationConstants.AnswerTypes.YESNO);
			question3answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.YESNO - 1]);
			question3answer.setName("Inclusion_of_Women");
			question3answer.setSequenceId(1);
			question3.addField(question3answer);
			
			Question question4 = new Question();
			question4.setQuestionText("If you are a woman, would you like to be participating more?");
			Field question4answer = new Field();
			question4answer.setDescription("More Participation of Women");
			question4answer.setFieldId(SurveyCreationConstants.AnswerTypes.YESNO);
			question4answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.YESNO - 1]);
			question4answer.setName("More_Participation_of_Women");
			question4answer.setSequenceId(1);
			question4.addField(question4answer);
			
			Question question5 = new Question();
			question5.setQuestionText("If you are a man, would you like that women participate more?");
			Field question5answer = new Field();
			question5answer.setDescription("More Participation of Women");
			question5answer.setFieldId(SurveyCreationConstants.AnswerTypes.YESNO);
			question5answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.YESNO - 1]);
			question5answer.setName("More_Participation_of_Women");
			question5answer.setSequenceId(1);
			question5.addField(question5answer);
			
			questions.add(question1);
			questions.add(question2);
			questions.add(question3);
			questions.add(question4);
			questions.add(question5);
		}
		
		
		if (phase == SurveyCreationConstants.ANALYSIS) {
			Question question1 = new Question();
			question1.setQuestionText("Are you satisfied with <project>?");
			Field question1answer = new Field();
			question1answer.setDescription("Project Satisfaction");
			question1answer.setFieldId(SurveyCreationConstants.AnswerTypes.YESNO);
			question1answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.YESNO - 1]);
			question1answer.setName("Project_Satisfaction");
			question1answer.setSequenceId(1);
			question1.addField(question1answer);
			
			question1.setQuestionType(SurveyCreationConstants.QuestionTypes.YESNO);
			
			Question question2 = new Question();
			question2.setQuestionText("Rate your involvement in <project>.");
			Field question2answer = new Field();
			question2answer.setDescription("Involvement Rating");
			question2answer.setFieldId(SurveyCreationConstants.AnswerTypes.INTEGER);
			question2answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.INTEGER - 1]);

			//question2answer.setFieldType(mt.getFieldTypes()[0]);
			question2answer.setName("Involvement_Rating");
			question2answer.setSequenceId(1);
			question2.addField(question2answer);
			
			question2.setQuestionType(SurveyCreationConstants.QuestionTypes.RATING);
			
			Question question3 = new Question();
			question3.setQuestionText("Rate your trust of project supervisors during <project>.");
			Field question3answer = new Field();
			question3answer.setDescription("Trust Rating");
			question3answer.setFieldId(SurveyCreationConstants.AnswerTypes.INTEGER);
			question3answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.INTEGER - 1]);

			//question2answer.setFieldType(mt.getFieldTypes()[0]);
			question3answer.setName("Trust_Rating");
			question3answer.setSequenceId(1);
			question3.addField(question3answer);
			
			question3.setQuestionType(SurveyCreationConstants.QuestionTypes.RATING);
			
			Question question4 = new Question();
			question4.setQuestionText("Rate your trust of the funding organization for <project>.");
			Field question4answer = new Field();
			question4answer.setDescription("Trust Rating");
			question4answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.INTEGER - 1]);

			question4answer.setFieldId(SurveyCreationConstants.AnswerTypes.INTEGER);
			//question2answer.setFieldType(mt.getFieldTypes()[0]);
			question4answer.setName("Trust_Rating");
			question4answer.setSequenceId(1);
			question4.addField(question4answer);
			
			question4.setQuestionType(SurveyCreationConstants.QuestionTypes.RATING);
			
			Question question5 = new Question();
			question5.setQuestionText("Rate your trust of your community.");
			Field question5answer = new Field();
			question5answer.setDescription("Trust Rating");
			question5answer.setFieldId(SurveyCreationConstants.AnswerTypes.INTEGER);
			question5answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.INTEGER - 1]);

			//question2answer.setFieldType(mt.getFieldTypes()[0]);
			question5answer.setName("Trust_Rating");
			question5answer.setSequenceId(1);
			question5.addField(question5answer);
			
			question5.setQuestionType(SurveyCreationConstants.QuestionTypes.RATING);
			
			
			Question question6 = new Question();
			question6.setQuestionText("Rate how much your knowledge of <project> has improved with the completion of <project>.");
			Field question6answer = new Field();
			question6answer.setDescription("Knowledge Rating");
			question6answer.setFieldId(SurveyCreationConstants.AnswerTypes.INTEGER);
			question6answer.setFieldType((SimpleFieldType) fieldTypes[SurveyCreationConstants.AnswerTypes.INTEGER - 1]);
			question6answer.setName("Knowledge_Rating");
			question6answer.setSequenceId(1);
			question6.addField(question6answer);
			
			question6.setQuestionType(SurveyCreationConstants.QuestionTypes.RATING);
			
				
			questions.add(question1);
			questions.add(question2);
			questions.add(question3);
			questions.add(question4);
			questions.add(question5);
			questions.add(question6);
		}
		
	}
	
	public List<Question> getQuestions() {
		return questions;
	}
	
	
	
}
