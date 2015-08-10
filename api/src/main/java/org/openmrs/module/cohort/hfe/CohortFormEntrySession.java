package org.openmrs.module.cohort.hfe;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortEncounter;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortObs;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.htmlformentry.BadFormDesignException;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionController;
import org.openmrs.module.htmlformentry.HtmlForm;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.InvalidActionException;
import org.springframework.util.StringUtils;

public class CohortFormEntrySession {

	protected final Log log = LogFactory.getLog(getClass());

	private CohortEncounter encounter;

	public final static String[] COHORT_TAGS = {"cohort"};
	
	public final static String[] ENCOUNTER_TAGS={"encounterDatetime", "location", "encounterProvider"};
	
    private String htmlToDisplay;
	 
	//private long encounterModifiedTimestamp;

	public String getHtmlToDisplay() throws Exception {
		return formEntrySession.getHtmlToDisplay();
	}

	private CohortM cohort;

	public void setCohort(CohortM cohort) {
		this.cohort = cohort;
	}

	//private long formModifiedTimestamp;

	private CohortFormEntryContext context;
	
	private String hasChangedInd;
	
	private String returnUrl;
	
	private FormEntrySession formEntrySession;
	
	private CohortFormSubmissionController controller;

	private CohortFormSubmissionActions submissionActions;

	private String xmlDefinition;
	
	//private HtmlForm htmlForm;
	
	private long encounterModifiedTimestamp;

	//private VelocityEngine velocityEngine;

	//private VelocityContext velocityContext;

	private boolean voidEncounter = false;
	
	private CohortFormEntrySession(
			CohortM cohort,Mode mode,
			Location defaultLocation, HttpSession httpSession, HtmlForm htmlForm) throws Exception {
		formEntrySession=new FormEntrySession(new Patient(),htmlForm,mode,httpSession);
		context = new CohortFormEntryContext(mode);
		context.setDefaultLocation(defaultLocation);
		context.setHttpSession(httpSession);
		//this.httpSession = httpSession;
		this.cohort = cohort;

		context.setupExistingData(cohort);

		/*velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(
				RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
				"org.apache.velocity.runtime.log.CommonsLogLogChute");
		velocityEngine.setProperty(
				CommonsLogLogChute.LOGCHUTE_COMMONS_LOG_NAME,
				"htmlformentry_velocity");

		try {
			velocityEngine.init();
		} catch (Exception e) {
			log.error("Error initializing Velocity engine", e);
		}
		velocityContext = new VelocityContext();
		velocityContext.put("locale", Context.getLocale());
		velocityContext.put("cohort", cohort);
		velocityContext.put("fn",new CohortVelocityFunctions(this));
		velocityContext.put("user", Context.getAuthenticatedUser());
		velocityContext.put("session", this);
		velocityContext.put("context", context);
		velocityContext.put("formGeneratedDatetime", new Date());
		velocityContext.put("visit", context.getVisit());

		// finally allow modules to provide content to the velocity context
		/*
		 * for (VelocityContextContentProvider provider :
		 * Context.getRegisteredComponents
		 * (VelocityContextContentProvider.class)) {
		 * provider.populateContext(this, velocityContext); }
		 */

		//htmlGenerator = new CohortFormEntryGenerator();
	}

	public CohortFormEntrySession(CohortM cohort, String xml,
			HttpSession httpSession) throws Exception {
		this(cohort, Mode.ENTER, null, httpSession,null);
		controller = new CohortFormSubmissionController();
		this.xmlDefinition = xml;
	}

	public CohortFormEntrySession(CohortM cohort, HtmlForm htmlForm,
			HttpSession httpSession) throws Exception {
		this(cohort, htmlForm, Mode.ENTER, httpSession);
	}

	
	public CohortFormEntrySession(CohortM cohort, HtmlForm htmlForm, org.openmrs.module.htmlformentry.FormEntryContext.Mode mode,
			HttpSession httpSession) throws Exception {
		this(cohort, htmlForm, mode, null, httpSession, true, false);
	}

	public CohortFormEntrySession(CohortM cohort, HtmlForm htmlForm, org.openmrs.module.htmlformentry.FormEntryContext.Mode mode,
			Location defaultLocation, HttpSession httpSession,
			boolean automaticClientSideValidation,
			boolean clientSideValidationHints) throws Exception {
		this(cohort, mode, defaultLocation, httpSession,htmlForm);
		this.context
				.setAutomaticClientSideValidation(automaticClientSideValidation);
		this.context.setClientSideValidationHints(clientSideValidationHints);
		//this.htmlForm = htmlForm;
		//this.formModifiedTimestamp = (htmlForm.getDateChanged() == null ? htmlForm.getDateCreated() : htmlForm.getDateChanged()).getTime();
		//form = htmlForm.getForm();

		//velocityContext.put("form", form);
		controller=new CohortFormSubmissionController();

		// avoid lazy initialization exceptions later
	/*if (form.getEncounterType() != null)
			form.getEncounterType().getName();*/

		xmlDefinition = htmlForm.getXmlData();
	}

	/**
	 * Creates a new HTML Form Entry session (in "Enter" mode) for the specified
	 * patient and using the HTML Form associated with the specified Form
	 * 
	 * @param patient
	 * @param form
	 * @param httpSession
	 * @throws Exception
	 */
	/*public CohortFormEntrySession2(CohortM cohort, Form form,
			HttpSession httpSession) throws Exception {
		this(cohort, Mode.ENTER, null, httpSession);
		this.form = form;

		velocityContext.put("form", form);
		submissionController = new CohortFormSubmissionController();

		HtmlForm temp = HtmlFormEntryUtil.getService().getHtmlFormByForm(form);
		this.formModifiedTimestamp = (temp.getDateChanged() == null ? temp
				.getDateCreated() : temp.getDateChanged()).getTime();
		xmlDefinition = temp.getXmlData();
	}*/

	public CohortFormEntrySession(CohortM cohort, CohortEncounter encounter,
			org.openmrs.module.htmlformentry.FormEntryContext.Mode mode, HtmlForm htmlForm, HttpSession httpSession)
			throws Exception {
		this(cohort, encounter, mode, htmlForm, null, httpSession, true, false);
	}

	/**
	 * Creates a new HTML Form Entry session for the specified patient,
	 * encounter, and {@see Mode}, using the specified HtmlForm
	 * 
	 * @param patient
	 * @param encounter
	 * @param mode
	 * @param htmlForm
	 * @param defaultLocation
	 * @param httpSession
	 * @throws Exception
	 */
	public CohortFormEntrySession(CohortM cohort, CohortEncounter encounter,
			Mode mode, HtmlForm htmlForm, Location defaultLocation,
			HttpSession httpSession, boolean automaticClientSideValidation,
			boolean clientSideValidationHints) throws Exception {
		this(cohort, mode, defaultLocation, httpSession,htmlForm);
		this.context
				.setAutomaticClientSideValidation(automaticClientSideValidation);
		this.context.setClientSideValidationHints(clientSideValidationHints);
		//this.htmlForm = htmlForm;
		/*if (htmlForm != null) {
			if (htmlForm.getId() != null)
				this.formModifiedTimestamp = (htmlForm.getDateChanged() == null ? htmlForm
						.getDateCreated() : htmlForm.getDateChanged())
						.getTime();
			form = htmlForm.getForm();
			velocityContext.put("form", form);
			// avoid lazy initialization exceptions later
			if (form != null && form.getEncounterType() != null)
				form.getEncounterType().getName();
		}*/
		this.encounter = encounter;
		if (encounter != null) {
			formEntrySession.addToVelocityContext("encounter", encounter);
			setEncounterModifiedTimestamp(getEncounterModifiedDate(encounter));
		}

		controller=new CohortFormSubmissionController();
		context.setupExistingData(encounter);
		this.xmlDefinition = htmlForm.getXmlData();
	}
	
	 public static long getEncounterModifiedDate(CohortEncounter encounter) {
	        long ret = encounter.getDateCreated().getTime();
	        if (encounter.getDateVoided() != null)
	            ret = Math.max(ret, encounter.getDateVoided().getTime());
	        for (CohortObs o : encounter.getAllObs(true)) {
	            ret = Math.max(ret, o.getDateCreated().getTime());
	            if (o.getDateVoided() != null)
	                ret = Math.max(ret, o.getDateVoided().getTime());
	        }
	       /* for (Order o : encounter.getOrders()) {
	            ret = Math.max(ret, o.getDateCreated().getTime());
	            if (o.getDateVoided() != null)
	                ret = Math.max(ret, o.getDateVoided().getTime());
	        }*/
	        return ret;
	    }
	 
	 public String getFieldAccessorJavascript() {
	       return formEntrySession.getFieldAccessorJavascript();
	    }

	 public String getLastSubmissionErrorJavascript() {
			return formEntrySession.getLastSubmissionErrorJavascript();
		}

	 public String getSetLastSubmissionFieldsJavascript() {
			return formEntrySession.getSetLastSubmissionFieldsJavascript();
		}
	 
	 public CohortM getCohort()
	 {
		 return cohort;
	 }
	 
	 public String getReturnUrlWithParameters() {
		 if (!StringUtils.hasText(returnUrl))
	            return null;
	        String ret = returnUrl;
	        if (!ret.contains("?"))
	            ret += "?";
	        if (!ret.endsWith("?") && !ret.endsWith("&"))
	            ret += "&";
	       ret += "cohortId=" +getCohort().getCohortId();
	       return ret;
	    }
	 public boolean hasEncouterTag()
	 {
		  for (String tag : CohortFormEntrySession.ENCOUNTER_TAGS) {
	            tag = "<" + tag;
	            if (xmlDefinition.contains(tag)) {
	                return true;
	            }
	        }
	        return false;
	 }
	 
	 public boolean hasCohortTag()
	 {
		 for (String tag :CohortFormEntrySession.COHORT_TAGS) {
	            tag = "<" + tag;
	            if (xmlDefinition.contains(tag)) {
	                return true;
	            }
	        }
	        return false;
	 }
	 
	 public void applyActions() throws BadFormDesignException {
			// if any encounter to be created by this form is missing a required
			// field, throw an error
			// (If there's a widget but it was left blank, that would have been
			// caught earlier--this
			// is for when there was no widget in the first place.)

			{
				for (CohortEncounter e : submissionActions.getEncountersToCreate()) {
					if (!CohortHtmlFormUtil.hasProvider(e)||e.getEncounterDateTime() == null
							|| e.getLocation() == null) {
						throw new BadFormDesignException(
								"Please check the design of your form to make sure it has all three tags: <b>&lt;encounterDate/&gt</b>;, <b>&lt;encounterLocation/&gt</b>;, and <b>&lt;encounterProvider/&gt;</b>");
					}
				}
			}

			// if we're un-voiding an existing voided encounter. This won't get hit
			// 99.9% of the time. See EncounterDetailSubmissionElement
			if (!voidEncounter && encounter != null && encounter.isVoided()) {
				encounter.setVoided(false);
				encounter.setVoidedBy(null);
				encounter.setVoidReason(null);
				encounter.setDateVoided(null);
			}

			// remove any obs groups that don't contain children
			CohortHtmlFormUtil.removeEmptyObs(submissionActions.getObsToCreate());

			// propagate encounterDatetime to Obs where necessary
			if (submissionActions.getObsToCreate() != null) {
				List<CohortObs> toCheck = new ArrayList<CohortObs>();
				toCheck.addAll(submissionActions.getObsToCreate());
				while (toCheck.size() > 0) {
					CohortObs o = toCheck.remove(toCheck.size() - 1);
					if (o.getObsDateTime() == null && o.getEncounterId() != null) {
						o.setObsDateTime((o.getEncounterId())
								.getEncounterDateTime());
						if (log.isDebugEnabled())
							log.debug("Set obsDatetime to "
									+ o.getObsDateTime()
									+ " for "
									+ o.getConcept().getBestName(
											Context.getLocale()));
					}
					if (o.getLocation() == null && o.getEncounterId() != null) {
						o.setLocation(o.getEncounterId().getLocation());
					}
					if (o.hasGroupMembers())
						toCheck.addAll(o.getGroupMembers());
				}
			}
			if (submissionActions.getEncountersToCreate() != null) {
	            for (CohortEncounter e : submissionActions.getEncountersToCreate()) {
	                if (formEntrySession.getForm() != null) {
	                    e.setForm(formEntrySession.getForm() );
	                    if (formEntrySession.getForm() .getEncounterType() != null)
	                        e.setEncounterType(formEntrySession.getForm().getEncounterType());
	                }
	                Context.getService(CohortService.class).saveCohortEncounters(encounter);
	            }
	        }
		        
		        if (submissionActions.getObsToVoid() != null) {
		            for (CohortObs o : submissionActions.getObsToVoid()) {
		                if (log.isDebugEnabled())
		                    log.debug("voiding obs: " + o.getObsId());
		                o.setVoided(true);
		                o.setVoidReason("htmlformentry");
		                Context.getService(CohortService.class).saveCohortObs(o);
		                // if o was in a group and it has no obs left, void the group
						voidObsGroupIfAllChildObsVoided(o.getObsGroup());
		            }
		        }

		        // If we're in EDIT mode, we have to save the encounter so that any new obs are created.
		        // This feels a bit like a hack, but actually it's a good thing to update the encounter's dateChanged in this case. (PS- turns out there's no dateChanged on encounter up to 1.5.)
		        // If there is no encounter (impossible at the time of writing this comment) we save the obs manually
		        if (context.getMode() == Mode.EDIT) {
		            if (encounter != null) {
		                if (voidEncounter) {
		                    try {
							//CohortHtmlFormUtil.voidEncounter(encounter,htmlForm, "voided via htmlformentry form submission");
		                    	//TODO CREATE METHOD TO VOID ENCOUNTERS AND OBSERVATIONS IN THIS CLASS
		                    } catch (Exception ex) {
		                        throw new RuntimeException("Unable to void encounter.", ex);
		                    }
		                }
		                Context.getService(CohortService.class).saveCohortEncounters(encounter);
		            } else if (submissionActions.getObsToCreate() != null) {
		                // this may not work right due to savehandlers (similar error to HTML-135) but this branch is
		                // unreachable until html forms are allowed to edit data without an encounter
		                for (CohortObs o : submissionActions.getObsToCreate())
		                    Context.getService(CohortService.class).saveCohortObs(o);
		            }
		        }
		        if (submissionActions.getCustomFormSubmissionActions() != null) {
		            for (CohortCustomFormSubmissionAction customFormSubmissionAction : submissionActions.getCustomFormSubmissionActions()) {
		                customFormSubmissionAction.applyAction(this);
		            }
		        }
}
	 public void prepareForSubmit() {

			submissionActions = new CohortFormSubmissionActions();

			if (hasCohortTag() && !hasEncouterTag()) {
				try {
					submissionActions.beginCohort(cohort);
				} catch (InvalidActionException e) {
					log.error(
							"Programming error: should be no errors starting a patient",
							e);
				}
			} else {
				if (context.getMode() == Mode.EDIT) {
					if (encounter == null)
						throw new RuntimeException(
								"Programming exception: encounter shouldn't be null in EDIT mode");
				} else {
					encounter = new CohortEncounter();
				}
				try {
					submissionActions.beginCohort(cohort);
					submissionActions.beginEncounter(encounter);
				} catch (InvalidActionException e) {
					log.error(
							"Programming error: should be no errors starting a patient and encounter",
							e);
				}
			}

		}
	 
	 private void voidObsGroupIfAllChildObsVoided(CohortObs group) {
			if (group != null) {

				// probably should be able to just tet if group.getGroupMembers() ==
				// 0 since
				// getGroupMembers only returns non-voided members?
				boolean allObsVoided = true;
				for (CohortObs member : group.getGroupMembers()) {
					allObsVoided = allObsVoided && member.isVoided();
				}
				if (allObsVoided) {
					// Context.getObsService().voidObs(group, "htmlformentry");
				}
				voidObsGroupIfAllChildObsVoided(group.getObsGroup());
			}
		}

	public CohortFormSubmissionController getController() {
		return controller;
	}

	public CohortEncounter getEncounter() {
		return encounter;
	}

	public String getXmlDefinition() {
		return xmlDefinition;
	}

	public int getHtmlFormId() {
		return formEntrySession.getHtmlFormId();
	}

	public CohortFormEntryContext getContext() {
		return context;
	}
	public void setHtmlForm(HtmlForm htmlForm) {
	}

	public CohortFormSubmissionActions getSubmissionActions() {
		return submissionActions;
	}

	  public long getEncounterModifiedTimestamp() {
	        return encounterModifiedTimestamp;
	    }
	  
	public void setEncounterModifiedTimestamp(long encounterModifiedTimestamp) {
		this.encounterModifiedTimestamp = encounterModifiedTimestamp;
	}

	public String getHasChangedInd() {
		return hasChangedInd;
	}

	public void setHasChangedInd(String hasChangedInd) {
		this.hasChangedInd = hasChangedInd;
	}

	public void setReturnUrl(String returnUrl2) {
		formEntrySession.setReturnUrl(returnUrl);
	}
	
	 public long getFormModifiedTimestamp() {
	        return formEntrySession.getFormModifiedTimestamp();
 }
}
