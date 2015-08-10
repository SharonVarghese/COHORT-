package org.openmrs.module.cohort.hfe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortEncounter;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.hfe.CohortHtmlFormUtil;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionController;
import org.openmrs.module.htmlformentry.FormSubmissionError;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction;
import org.openmrs.module.htmlformentry.action.RepeatControllerAction;
import org.openmrs.util.OpenmrsUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CohortFormSubmissionController {

	 private List<CohortFormSubmissionControllerAction> actions = new ArrayList<CohortFormSubmissionControllerAction>();
	    private transient List<FormSubmissionError> lastSubmissionErrors;
	    private transient HttpServletRequest lastSubmission;
	    private CohortRepeatControllerAction repeat = null;

	    private static Log log = LogFactory.getLog(CohortFormSubmissionController.class);
	    
	    /**
	     * Adds a {@see RepeatControllerAction} to the list of submission actions.
	     * 
	     * @param repeat the repeat controller action to add
	     */
	    public void startRepeat(CohortRepeatControllerAction repeat) {
	        if (this.repeat != null)
	            throw new IllegalArgumentException("Nested Repeating elements are not yet implemented");
	        addAction(repeat);
	        this.repeat = repeat;
	    }
	    
	    /**
	     * Marks the end of the a repeat. This has to be specified because nested repeating elements are not yet implemented.
	     */
	    public void endRepeat() {
	        if (this.repeat == null)
	            throw new IllegalArgumentException("No Repeating element is open now");
	        this.repeat = null;
	    }
	    
	    /**
	     * Adds a FormSubmissionControllerAction to the list of submission actions.
	     * 
	     * @param the form submission controller action to add
	     */
	    public void addAction(CohortFormSubmissionControllerAction action) {
	        actions.add(action);
	    }
	    
	    /**
	     * Validates a form submission, given a Form Entry Context.
	     * <p/>
	     * This method cycles through all the FormSubmissionControllerActions and calls their validateSubmission method, 
	     * adding any errors to the error list.
	     * 
	     * @param context the Form Entry Context 
	     * @param submission the submission to validate
	     * @return list of all validation errors
	     */
	    public List<FormSubmissionError> validateSubmission(CohortFormEntryContext context, HttpServletRequest submission) {
	        lastSubmission = submission;
	        lastSubmissionErrors = new ArrayList<FormSubmissionError>();
	        for (CohortFormSubmissionControllerAction element : actions) {
	            Collection<FormSubmissionError> errs = element.validateSubmission(context, submission);
	            if (errs != null) {
	                lastSubmissionErrors.addAll(errs);
	            }
	        }
	        return lastSubmissionErrors;
	    }
	    
	    /**
	     * Processes a form submission, given a Form Entry Session.
	     * <p/>
	     * This method cycles through all the FormSubmissionControllerActions and calls their handleSubmission method,
	     * 
	     * @param session the Form Entry Session
	     * @param submission
	     */
	    public void handleFormSubmission(CohortFormEntrySession session, HttpServletRequest submission) throws Exception{
	        lastSubmission = submission;
	        //Serialize when opted in.
	        String optedIn = Context.getAdministrationService().getGlobalProperty("htmlformentry.archiveHtmlForms","No");
	        if(Boolean.parseBoolean(optedIn)) {
	            //Try to serialize
	            try {
	                CohortM cohort = session.getCohort();
	                CohortEncounter encounter = session.getEncounter();
	                CohortSerializableFormObject formObject = null;
	                if (cohort != null && encounter != null) {
	                    formObject = new CohortSerializableFormObject(session.getXmlDefinition(), submission.getParameterMap(),cohort.getUuid(), encounter.getUuid(),session.getHtmlFormId());
	                } else if(cohort==null || encounter==null) {
	                    if(log.isDebugEnabled()) log.debug("Either patient or encounter or both are null");

	                    //Serialize anyway
	                    formObject = new CohortSerializableFormObject(session.getXmlDefinition(), submission.getParameterMap(),
	                                    session.getHtmlFormId());
	                }
	                if (formObject != null) {
	                    serializeFormData(formObject);
	                }
	            } finally {
	                //Submit even when you are not able to serialize.
	                for (CohortFormSubmissionControllerAction element : actions) {
	                    element.handleSubmission(session, submission);
	                }
	            }
	        } else {  //Just submit
	            for (CohortFormSubmissionControllerAction element : actions) {
	                element.handleSubmission(session, submission);
	            }
	        }
	    }
	    
	    /**
	     * Returns the last submission processed by handleFormSubmission.
	     * 
	     * @return the last submission processed
	     */
	    public HttpServletRequest getLastSubmission() {
	        return lastSubmission;
	    }

	    /**
	     * Returns the last set of submission errors generated by validateSubmission
	     * 
	     * @return the last set of submission errors
	     */
	    public List<FormSubmissionError> getLastSubmissionErrors() {
	        return lastSubmissionErrors;
	    }
	    
	    /**
	     * 
	     * Returns the List of FormSubmissionControllerActions
	     * 
	     * @return the FormSubmissionControllerAction List
	     */
	    public List<CohortFormSubmissionControllerAction> getActions() {
	               return actions;
	    }

	    /**
	     * Serializes an object formed by pairing the HttpServletRequest & FormEntrySession objects necessary for form
	     * submission
	     * @param submittedData  SerializableFormObject
	     * @throws Exception
	     */
	    protected void serializeFormData(final CohortSerializableFormObject submittedData) throws Exception {
	        //Get archive Directory
	        String path = CohortHtmlFormUtil.getArchiveDirPath();

	        //Ignore if no path specified
	        if(path==null)return;

	        File   file = new File(path);
	        if(file.exists()) {
	            if(!file.isDirectory()) {
	                throw new APIException("The specified archive is not a directory, please use a proper directory");
	            }
	            //Proceed if it is a directory
	            if(!file.canWrite()) {
	                throw new APIException("The Archive directory is not writable, check the directory permissions");
	            }
	        }else{
	            //Try to create the directory if it does not exist.
	            if(!file.mkdirs()) {
	                throw new APIException("Failed to create subdirectories. Make sure you have proper write " +
	                        "permission set on the archive directory");
	            }
	        }

	        //Things are fine at this point
	        submittedData.serializeToXml(path);
	    }
}
