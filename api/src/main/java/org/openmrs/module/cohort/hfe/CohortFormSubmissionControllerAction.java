package org.openmrs.module.cohort.hfe;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import org.openmrs.module.htmlformentry.FormSubmissionError;

public interface CohortFormSubmissionControllerAction {
	
	public Collection<FormSubmissionError> validateSubmission(CohortFormEntryContext context, HttpServletRequest submission);

    /**
     * Handles the submission of this element in the form. Assumes that validateSubmission has been called and returned no errors.
     * 
     * @param session
     * @param submission
     */
    public void handleSubmission(CohortFormEntrySession session, HttpServletRequest submission);

}
