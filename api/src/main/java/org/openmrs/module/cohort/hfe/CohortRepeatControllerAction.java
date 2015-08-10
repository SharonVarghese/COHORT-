package org.openmrs.module.cohort.hfe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionError;
import org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction;
public class CohortRepeatControllerAction implements CohortFormSubmissionControllerAction {

	 protected List<CohortFormSubmissionControllerAction> repeatingActions = new ArrayList<CohortFormSubmissionControllerAction>();
	 
	 public void addAction(CohortFormSubmissionControllerAction action) {
	        repeatingActions.add(action);
	    }
	 
	 public void beforeHandleSubmission(CohortFormEntrySession session, HttpServletRequest submission) { }
	    
	    /**
	     * Performs any actions that need to happen after handling a submission
	     * 
	     * @param session
	     * @param submission
	     */
	    public void afterHandleSubmission(CohortFormEntrySession session, HttpServletRequest submission) { }
	    
	    /**
	     * Performs any actions that need to happen after handling a submission
	     * 
	     * @param context
	     * @param submission
	     */
	    public void beforeValidateSubmission(CohortFormEntryContext context, HttpServletRequest submission) { }
	    
	    /**
	     * Performs any actions that need to happen after handling a submission
	     * 
	     * @param context
	     * @param submission
	     */
	    public void afterValidateSubmission(CohortFormEntryContext context, HttpServletRequest submission) { }
	   
	    /**
	     * Calls the handleSubmission method for all Form Submission Controller Actions associated with this Repeat Controller
	     * 
	     * @param session
	     * @param submission
	     */
	@Override
	public Collection<FormSubmissionError> validateSubmission(
			CohortFormEntryContext context, HttpServletRequest submission) {
		 beforeValidateSubmission(context, submission);
	        Collection<FormSubmissionError> ret = new ArrayList<FormSubmissionError>();
	        for (CohortFormSubmissionControllerAction action : repeatingActions) {
	            Collection<FormSubmissionError> temp = action.validateSubmission(context, submission);
	            if (temp != null)
	                ret.addAll(temp);
	        }
	        afterValidateSubmission(context, submission);
	        return ret;
		
	}

	@Override
	public void handleSubmission(CohortFormEntrySession session,
			HttpServletRequest submission) {
		beforeHandleSubmission(session, submission);
        for (CohortFormSubmissionControllerAction action : repeatingActions)
            action.handleSubmission(session, submission);
        afterHandleSubmission(session, submission);
	}

}
