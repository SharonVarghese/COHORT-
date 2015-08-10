package org.openmrs.module.cohort.hfe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortEncounter;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortObs;
import org.openmrs.module.cohort.hfe.CohortCustomFormSubmissionAction;
import org.openmrs.module.cohort.hfe.CohortHtmlFormUtil;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormSubmissionActions;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.InvalidActionException;
import org.openmrs.module.htmlformentry.property.ExitFromCareProperty;
import org.openmrs.util.OpenmrsUtil;
public class CohortFormSubmissionActions {
	
	/** Logger to use with this class */
	protected final Log log = LogFactory.getLog(getClass());

    private Boolean patientUpdateRequired = false;
    
    private FormSubmissionActions formSubmissionActions;

	private List<CohortM> personsToCreate = new Vector<CohortM>();
	
	private List<CohortEncounter> encountersToCreate = new Vector<CohortEncounter>();
	
	private List<CohortEncounter> encountersToEdit = new Vector<CohortEncounter>();
	
	private List<CohortObs> obsToCreate = new Vector<CohortObs>();
	
	private List<CohortObs> obsToVoid = new Vector<CohortObs>();
	
	/*private List<Order> ordersToCreate = new Vector<Order>();
	
	private List<PatientProgram> patientProgramsToCreate = new Vector<PatientProgram>();
	
	private List<PatientProgram> patientProgramsToComplete = new Vector<PatientProgram>();
	
	private List<PatientProgram> patientProgramsToUpdate = new Vector<PatientProgram>();
	
	private List<Relationship> relationshipsToCreate = new Vector<Relationship>();
	
	private List<Relationship> relationshipsToVoid = new Vector<Relationship>();
	
	private List<Relationship> relationshipsToEdit = new Vector<Relationship>();

	private List<PatientIdentifier> identifiersToVoid = new Vector<PatientIdentifier>();

    private ExitFromCareProperty exitFromCareProperty;*/

    private List<CohortCustomFormSubmissionAction> customFormSubmissionActions;

	/** The stack where state is stored */
	private Stack<Object> stack = new Stack<Object>(); // a snapshot might look something like { Patient, Encounter, ObsGroup }
	
	public CohortFormSubmissionActions() {
	}
	
	/**
	 * Add a Person to the submission stack. A Person must be the first object added to the
	 * submission stack.
	 * 
	 * @param Person person to add
	 * @throws InvalidActionException
	 */
	public void beginCohort(CohortM cohort) throws InvalidActionException {
		// person has to be at the top of the stack
		if (stack.size() > 0)
			throw new InvalidActionException("Person can only go on the top of the stack");
		if (cohort.getId() == null && !personsToCreate.contains(cohort))
			personsToCreate.add(cohort);
		stack.push(cohort);
	}
	
	/**
	 * Removes the most recently added Person from the submission stack. All other objects added
	 * after that Person are removed as well.
	 * <p/>
	 * (So, in the current one-person-per-form model, this would empty the entire submission stack)
	 * 
	 * @throws InvalidActionException
	 */
	public void endCohort() throws InvalidActionException {
		if (!stackContains(CohortM.class))
			throw new InvalidActionException("No Person on the stack");
		while (true) {
			Object o = stack.pop();
			if (o instanceof CohortM)
				break;
		}
	}
	
	/**
	 * Adds an Encounter to the submission stack
	 * 
	 * @param encounter the Encounter to add
	 * @throws InvalidActionException
	 */
	public void beginEncounter(CohortEncounter encounter) throws InvalidActionException {
		// there needs to be a Person on the stack before this
		if (!stackContains(CohortM.class))
			throw new InvalidActionException("No Person on the stack");
		if (encounter.getEncounterId() == null && !encountersToCreate.contains(encounter))
			encountersToCreate.add(encounter);
		encounter.setCohort((highestOnStack(CohortM.class)));
		stack.push(encounter);
	}
	
	/**
	 * Removes the most recently added Encounter from the submission stack. All objects added after
	 * that Encounter are removed as well.
	 * 
	 * @throws InvalidActionException
	 */
	public void endEncounter() throws InvalidActionException {
		if (!stackContains(CohortEncounter.class))
			throw new InvalidActionException("No Encounter on the stack");
		while (true) {
			Object o = stack.pop();
			if (o instanceof CohortEncounter)
				break;
		}
	}
	
	/**
	 * Adds an Obs Group to the submission stack
	 * 
	 * @param group the Obs Group to add
	 * @throws InvalidActionException
	 */
	/*public void beginObsGroup(Obs group) throws InvalidActionException {
		// there needs to be a Person on the stack before this
		if (!stackContains(Person.class))
			throw new InvalidActionException("No Person on the stack");
		if (group.getObsId() == null && !obsToCreate.contains(group)) {
			obsToCreate.add(group);
		}
		Person person = highestOnStack(Person.class);
		CohortEncounter encounter = highestOnStack(CohortEncounter.class);
		group.setPerson(person);
		if (encounter != null) {
			addObsToEncounterIfNotAlreadyThere(encounter, group);
		}
		//this is for obs groups within obs groups
		Object o = stack.peek();
		if (o instanceof CohortObs) {
			Obs oParent = (Obs) o;
			oParent.addGroupMember(group);
		}
		stack.push(group);
		
	}*/
	
	/**
	 * Utility function that adds a set of Obs to an Encounter, skipping Obs that are already part
	 * of the Encounter
	 * 
	 * @param encounter
	 * @param group
	 */
	private void addObsToEncounterIfNotAlreadyThere(CohortEncounter encounter, CohortObs group) {
		for (CohortObs obs : encounter.getObsAtTopLevel(true)) {
			if (obs.equals(group))
				return;
		}
		encounter.addObs(group);
	}
	
	/**
	 * Removes the most recently added ObsGroup from the submission stack. All objects added after
	 * that ObsGroup are removed as well.
	 * 
	 * @throws InvalidActionException
	 */
	public void endObsGroup() throws InvalidActionException {
		// there needs to be an Obs on the stack before this
		if (!stackContains(CohortObs.class))
			throw new InvalidActionException("No Obs on the stack");
		while (true) {
			Object o = stack.pop();
			if (o instanceof CohortObs)
				break;
		}
	}
	
	/**
	 * Returns the Person that was most recently added to the stack
	 * 
	 * @return the Person most recently added to the stack
	 */
	public CohortM getCurrentPerson() {
		return highestOnStack(CohortM.class);
	}
	
	/**
	 * Returns the Encounter that was most recently added to the stack
	 * 
	 * @return the Encounter most recently added to the stack
	 */
	public CohortEncounter getCurrentEncounter() {
		return highestOnStack(CohortEncounter.class);
	}
	
	/**
	 * Utility method that returns the object of a specified class that was most recently added to
	 * the stack
	 */
	@SuppressWarnings("unchecked")
	private <T> T highestOnStack(Class<T> clazz) {
		for (ListIterator<Object> iter = stack.listIterator(stack.size()); iter.hasPrevious();) {
			Object o = iter.previous();
			if (clazz.isAssignableFrom(o.getClass()))
				return (T) o;
		}
		return null;
	}
	
	/**
	 * Utility method that tests whether there is an object of the specified type on the stack
	 */
	private boolean stackContains(Class<?> clazz) {
		for (Object o : stack) {
			if (clazz.isAssignableFrom(o.getClass()))
				return true;
		}
		return false;
	}
	
	/**
	 * Creates an new Obs and associates with the most recently added Person, Encounter, and
	 * ObsGroup (if applicable) on the stack.
	 * <p/>
	 * Note that this method does not actually commit the Obs to the database, but instead adds the
	 * Obs to a list of Obs to be added. The changes are applied elsewhere in the framework.
	 * 
	 * @param concept concept associated with the Obs
	 * @param value value for the Obs
	 * @param datetime date information for the Obs
	 * @param accessionNumber accession number for the Obs
	 * @param comment comment for the obs
	 * @return the Obs to create
	 */
	public CohortObs createObs(Concept concept, Object value, Date datetime, String accessionNumber, String comment) {
		if (value == null || "".equals(value))
			throw new IllegalArgumentException("Cannot create Obs with null or blank value");
		CohortObs obs = CohortHtmlFormUtil.createObs(concept, value, datetime, accessionNumber);
		
		CohortM person = highestOnStack(CohortM.class);
		if (person == null)
			throw new IllegalArgumentException("Cannot create an Obs outside of a Person.");
		CohortEncounter encounter = highestOnStack(CohortEncounter.class);
		//CohortObs obsGroup = highestOnStack(CohortObs.class);
		
		if (person != null)
			obs.setCohort(person);
		
		if(StringUtils.isNotBlank(comment))
			obs.setComment(comment);

		if (encounter != null)
			encounter.addObs(obs);
		/*if (obsGroup != null) {
			obsGroup.addGroupMember(obs);
		}*/ else {
			obsToCreate.add(obs);
		}
		return obs;
	}

    /**
     * Legacy createObs methods without the comment argument
     */
    public CohortObs createObs(Concept concept, Object value, Date datetime, String accessionNumber) {
        return createObs(concept, value, datetime, accessionNumber, null);
    }


	/**
	 * Modifies an existing Obs.
	 * <p/>
	 * This method works by adding the current Obs to a list of Obs to void, and then adding the new
	 * Obs to a list of Obs to create. Note that this method does not commit the changes to the
	 * database--the changes are applied elsewhere in the framework.
	 * 
	 * @param existingObs the Obs to modify
	 * @param concept concept associated with the Obs
	 * @param newValue the new value of the Obs
	 * @param newDatetime the new date information for the Obs
	 * @param accessionNumber new accession number for the Obs
	 * @param comment comment for the obs
	 */
	public void modifyObs(CohortObs existingObs, Concept concept, Object newValue, Date newDatetime, String accessionNumber, String comment) {
		if (newValue == null || "".equals(newValue)) {
			// we want to delete the existing obs
			if (log.isDebugEnabled())
				log.debug("VOID: " + printObsHelper(existingObs));
			obsToVoid.add(existingObs);
			return;
		}
		if (concept == null) {
			// we want to delete the existing obs
			if (log.isDebugEnabled())
				log.debug("VOID: " + printObsHelper(existingObs));
			obsToVoid.add(existingObs);
			return;
		}
		CohortObs newObs = CohortHtmlFormUtil.createObs(concept, newValue, newDatetime, accessionNumber);
		String oldString = existingObs.getValueAsString(Context.getLocale());
		String newString = newObs.getValueAsString(Context.getLocale());
		if (log.isDebugEnabled() && concept != null) {
			log.debug("For concept " + concept.getBestName(Context.getLocale()) + ": " + oldString + " -> " + newString);
		}
		boolean valueChanged = !newString.equals(oldString);
		// TODO: handle dates that may equal encounter date
		boolean dateChanged = dateChangedHelper(existingObs.getObsDateTime(), newObs.getObsDateTime());
		boolean accessionNumberChanged = accessionNumberChangedHelper(existingObs.getAccessionNumber(),
		    newObs.getAccessionNumber());
		boolean conceptsHaveChanged = false;
		if (!existingObs.getConcept().getConceptId().equals(concept.getConceptId())) {
			conceptsHaveChanged = true;
		}
		if (valueChanged || dateChanged || accessionNumberChanged || conceptsHaveChanged) {
			if (log.isDebugEnabled()) {
				log.debug("CHANGED: " + printObsHelper(existingObs));
			}
			// TODO: really the voided obs should link to the new one, but this is a pain to implement due to the dreaded error: org.hibernate.NonUniqueObjectException: a different object with the same identifier value was already associated with the session
			obsToVoid.add(existingObs);
			createObs(concept, newValue, newDatetime, accessionNumber, comment);
		} else {
			if(existingObs != null && StringUtils.isNotBlank(comment))
				existingObs.setComment(comment);

			if (log.isDebugEnabled()) {
				log.debug("SAME: " + printObsHelper(existingObs));
			}
		}
	}

    /**
     * Legacy modifyObs methods without the comment argument
     */
    public void modifyObs(CohortObs existingObs, Concept concept, Object newValue, Date newDatetime, String accessionNumber) {
        modifyObs(existingObs, concept, newValue, newDatetime, accessionNumber, null);
    }

	/**
	 * Enrolls the Patient most recently added to the stack in the specified Program.
	 * <p/>
	 * Note that this method does not commit the program enrollment to the database but instead adds
	 * the Program to a list of programs to add. The changes are applied elsewhere in the framework
	 * 
	 * @param program Program to enroll the patient in
	 * @see #enrollInProgram(Program, Date, List)
	 */
	/*public void enrollInProgram(Program program) {
		enrollInProgram(program, null, null);
	}*/
	
	/**
	 * Enrolls the Patient most recently added to the stack in the specified Program setting the
	 * enrollment date as the date specified and setting initial states from the specified state
	 * <p/>
	 * Note that this method does not commit the program enrollment to the database but instead adds
	 * the Program to a list of programs to add. The changes are applied elsewhere in the framework
	 * 
	 * @param program Program to enroll the patient in
	 * @param enrollmentDate the date to enroll the patient in the program
	 * @param states list of states to set as initial in their workflows
	 */
    /*public void enrollInProgram(Program program, Date enrollmentDate, List<ProgramWorkflowState> states) {
		if (program == null)
			throw new IllegalArgumentException("Cannot enroll in a blank program");
		
		Patient patient = highestOnStack(Patient.class);
		if (patient == null)
			throw new IllegalArgumentException("Cannot enroll in a program outside of a Patient");
		Encounter encounter = highestOnStack(Encounter.class);
		
		// if an enrollment date has not been specified, enrollment date is the encounter date
		enrollmentDate = (enrollmentDate != null) ? enrollmentDate : (encounter  != null) ? encounter.getEncounterDatetime() : null;
		
		if (enrollmentDate == null)
			throw new IllegalArgumentException("Cannot enroll in a program without specifying an Encounter Date or Enrollment Date");
		
		// only need to do some if the patient is not enrolled in the specified program on the specified date
		if (!HtmlFormEntryUtil.isEnrolledInProgramOnDate(patient, program, enrollmentDate)) {
 			
			// see if the patient is enrolled in this program in the future
			PatientProgram pp = HtmlFormEntryUtil.getClosestFutureProgramEnrollment(patient, program, enrollmentDate);
			
			if (pp != null) {	
				//set the start dates of all states with a start date equal to the enrollment date to the selected date
				for (PatientState patientState : pp.getStates()) {
					if (OpenmrsUtil.nullSafeEquals(patientState.getStartDate(), pp.getDateEnrolled())) {
						patientState.setStartDate(enrollmentDate);
					}
				}
				
				// set the program enrollment date to the newly selected date
				pp.setDateEnrolled(enrollmentDate);
				
				patientProgramsToUpdate.add(pp);
			}
			// otherwise, create the new program
			else {
				pp = new PatientProgram();
				pp.setPatient(patient);
				pp.setProgram(program);
				if (enrollmentDate != null)
					pp.setDateEnrolled(enrollmentDate);
				
				if (states != null) {
					for (ProgramWorkflowState programWorkflowState : states) {
						pp.transitionToState(programWorkflowState, enrollmentDate);
					}
				}
				patientProgramsToCreate.add(pp);
			}
			
		}
	}*/
	
	/**
	 * Ends a Patient program.
	 * <p/>
	 * Note that this method does not commit the program enrollment change to the database but
	 * instead adds the Program to a list of programs to remove. The changes are applied elsewhere
	 * in the framework
	 * 
	 * @param program Program to end the enrollment for the patient in
	 */
	/*public void completeProgram(Program program) {
		if (program == null)
			throw new IllegalArgumentException("Cannot end a blank program");
		
		Patient patient = highestOnStack(Patient.class);
		if (patient == null)
			throw new IllegalArgumentException("Cannot find program without a patient");
		Encounter encounter = highestOnStack(Encounter.class);
		if (encounter == null)
			throw new IllegalArgumentException("Cannot end enrollment in a program outside of an Encounter");
		
		List<PatientProgram> pp = Context.getProgramWorkflowService().getPatientPrograms(patient, program, null,
		    encounter.getEncounterDatetime(), new Date(), null, false);
		
		patientProgramsToComplete.addAll(pp);
	}
	
	public void transitionToState(ProgramWorkflowState state) {
		if (state == null)
			throw new IllegalArgumentException("Cannot change to a blank state");
		
		Patient patient = highestOnStack(Patient.class);
		if (patient == null)
			throw new IllegalArgumentException("Cannot change state without a patient");
		Encounter encounter = highestOnStack(Encounter.class);
		if (encounter == null)
			throw new IllegalArgumentException("Cannot change state without an Encounter");

        // fetch any existing patient program with a state from this workflow
		PatientProgram patientProgram = HtmlFormEntryUtil.getPatientProgramByWorkflow(patient, state.getProgramWorkflow());

        // if no existing patient program, see if a patient program for this program is already set to be created at part of this submission (HTML-416)
        if (patientProgram == null) {
           patientProgram = HtmlFormEntryUtil.getPatientProgramByProgram(patientProgramsToCreate, state.getProgramWorkflow().getProgram());
        }

        if (patientProgram == null) {
            patientProgram = HtmlFormEntryUtil.getPatientProgramByProgram(patientProgramsToUpdate, state.getProgramWorkflow().getProgram());
        }

        // if patient program is still null, we need to create a new program
		if (patientProgram == null) {
			patientProgram = new PatientProgram();
			patientProgram.setPatient(patient);
			patientProgram.setProgram(state.getProgramWorkflow().getProgram());
			patientProgram.setDateEnrolled(encounter.getEncounterDatetime());
			// HACK: we need to set the date created, creator, and uuid here as a hack around a hibernate flushing issue
			// (should be able to remove this once we move to Hibernate Interceptors instead of Spring AOP to set these parameters)
			patientProgram.setDateCreated(new Date());
			patientProgram.setCreator(Context.getAuthenticatedUser());
			patientProgram.setUuid(UUID.randomUUID().toString());
			
		}
		
		for (PatientState patientState : patientProgram.statesInWorkflow(state.getProgramWorkflow(), false)) {
			if (patientState.getActive(encounter.getEncounterDatetime())) {
				if (patientState.getState().equals(state)) {
					return;
				}
			}
		}
		
		PatientState previousState = null;
		PatientState nextState = null;
		PatientState newState = new PatientState();
		newState.setPatientProgram(patientProgram);
		newState.setState(state);
		newState.setStartDate(encounter.getEncounterDatetime());
		// HACK: we need to set the date created, creator, and uuid here as a hack around a hibernate flushing issue
		// (should be able to remove this once we move to Hibernate Interceptors instead of Spring AOP to set these parameters)
		newState.setDateCreated(new Date());
		newState.setCreator(Context.getAuthenticatedUser());
		newState.setUuid(UUID.randomUUID().toString());
		
		Collection<PatientState> sortedStates = new TreeSet<PatientState>(new Comparator<PatientState>() {
			
			@Override
			public int compare(PatientState o1, PatientState o2) {
				int result = OpenmrsUtil.compareWithNullAsEarliest(o1.getStartDate(), o2.getStartDate());
				if (result == 0) {
					result = OpenmrsUtil.compareWithNullAsLatest(o1.getEndDate(), o2.getEndDate());
				}
				return result;
			}
			
		});
		sortedStates.addAll(patientProgram.statesInWorkflow(state.getProgramWorkflow(), false));
		for (PatientState currentState : sortedStates) {
			
			Date newStartDate = newState.getStartDate();
			Date currentStartDate = currentState.getStartDate();
			Date currentEndDate = currentState.getEndDate();
			
			if (currentEndDate != null) {
				if (currentEndDate.after(newStartDate)) {
					if (currentStartDate.after(newStartDate)) {
						nextState = currentState;
						break;
					} else {
						previousState = currentState;
					}
				} else {
					previousState = currentState;
				}
			} else if (currentStartDate.after(newStartDate)) {
				nextState = currentState;
				break;
			} else {
				previousState = currentState;
				nextState = null;
				break;
			}
		}
		
		if (nextState == null) {
			if (previousState != null) {
				previousState.setEndDate(newState.getStartDate());
			}
		} else {
			if (previousState != null) {
				previousState.setEndDate(newState.getStartDate());
			}
			newState.setEndDate(nextState.getStartDate());
		}
		
		patientProgram.getStates().add(newState);
		
		patientProgramsToUpdate.add(patientProgram);
	}*/

    /**
     * Prepares data to be sent for exiting the given patient from care
     * @param date - the date of exit
     * @param exitReasonConcept - reason the patient is exited from care
     * @param causeOfDeathConcept -the concept that corresponds with the reason the patient died
     * @param otherReason - in case the causeOfDeath is 'other', a place to store more info
     */
   /* public void exitFromCare(Date date, Concept exitReasonConcept, Concept causeOfDeathConcept, String otherReason){

        if (date != null && exitReasonConcept != null){
            this.exitFromCareProperty = new ExitFromCareProperty(date,exitReasonConcept,causeOfDeathConcept,otherReason);
        }else {
            throw new IllegalArgumentException("Exit From Care: date and exitReasonConcept cannot be null");
        }
    }

    public void addCustomFormSubmissionAction(CohortCustomFormSubmissionAction action) {
        if (customFormSubmissionActions == null) {
            customFormSubmissionActions = new ArrayList<CohortCustomFormSubmissionAction>();
        }

        customFormSubmissionActions.add(action);
    }*/

	/**
	 * This method compares Timestamps to plain Dates by dropping the nanosecond precision
	 */
	private boolean dateChangedHelper(Date oldVal, Date newVal) {
		if (newVal == null)
			return false;
		else
			return oldVal.getTime() != newVal.getTime();
	}
	
	private boolean accessionNumberChangedHelper(String oldVal, String newVal) {
		return !OpenmrsUtil.nullSafeEquals(oldVal, newVal);
	}
	
	private String printObsHelper(CohortObs obs) {
		return obs.getConcept().getBestName(Context.getLocale()) + " = " + obs.getValueAsString(Context.getLocale());
	}
	public List<CohortM> getPersonsToCreate() {
		return personsToCreate;
	}
	
	/**
	 * Sets the list of Persons that need to be created to process form submission
	 * 
	 * @param personsToCreate the list of Persons to create
	 */
	public void setPersonsToCreate(List<CohortM> personsToCreate) {
		this.personsToCreate = personsToCreate;
	}
	
	/**
	 * Returns a list of all the Encounters that need to be created to process form submissions
	 * 
	 * @return a list of Encounters to create
	 */
	public List<CohortEncounter> getEncountersToCreate() {
		return encountersToCreate;
	}
	
	/**
	 * Sets the list of Encounters that need to be created to process form submission
	 * 
	 * @param encountersToCreate the list of Encounters to create
	 */
	public void setEncountersToCreate(List<CohortEncounter> encountersToCreate) {
		this.encountersToCreate = encountersToCreate;
	}
	
	/**
	 * Returns the list of Encounters that need to be edited to process form submission
	 * 
	 * @return the list of Encounters to edit
	 */
	public List<CohortEncounter> getEncountersToEdit() {
		return encountersToEdit;
	}
	
	/**
	 * Sets the list of Encounters that need to be editing to process form submission
	 * 
	 * @param encountersToEdit the list of Encounters to edit
	 */
	public void setEncountersToEdit(List<CohortEncounter> encountersToEdit) {
		this.encountersToEdit = encountersToEdit;
	}
	
	/**
	 * Returns the list of Obs that need to be created to process form submission
	 * 
	 * @return the list of Obs to create
	 */
	public List<CohortObs> getObsToCreate() {
		return obsToCreate;
	}
	
	/**
	 * Sets the list of Obs that need to be created to process form submission
	 * 
	 * @param obsToCreate the list of Obs to create
	 */
	public void setObsToCreate(List<CohortObs> obsToCreate) {
		this.obsToCreate = obsToCreate;
	}
	
	/**
	 * Returns the list of Os that need to be voided to process form submission
	 * 
	 * @return the list of Obs to void
	 */
	public List<CohortObs> getObsToVoid() {
		return obsToVoid;
	}
	
	/**
	 * Sets the list Obs that need to be voided to process form submission
	 * 
	 * @param obsToVoid the list of Obs to void
	 */
	public void setObsToVoid(List<CohortObs> obsToVoid) {
		this.obsToVoid = obsToVoid;
	}
	
	/**
	 * Returns the list of Orders that need to be created to process form submission
	 * 
	 * @return the list of Orders to create
	 */
	/*public List<Order> getOrdersToCreate() {
		return ordersToCreate;
	}
	
	/**
	 * Sets the list of Orders that need to be created to process form submission
	 * 
	 * @param ordersToCreate the list of Orders to create
	 */
	/*public void setOrdersToCreate(List<Order> ordersToCreate) {
		this.ordersToCreate = ordersToCreate;
	}*/
	
	/**
	 * Returns the list of Patient Programs that need to be created to process form submission
	 * 
	 * @return the patientProgramsToCreate the list of Programs to create
	 */
	/*public List<PatientProgram> getPatientProgramsToCreate() {
		return patientProgramsToCreate;
	}*/
	
	/**
	 * Sets the list of Patient Programs that need to be created to process form submission
	 * 
	 * @param patientProgramsToCreate the list of Programs to create
	 */
	/*public void setPatientProgramsToCreate(List<PatientProgram> patientProgramsToCreate) {
		this.patientProgramsToCreate = patientProgramsToCreate;
	}*/
	
	/**
	 * Returns the list of Patient Programs that need to be completed to process form submission
	 * 
	 * @return the patientProgramsToComplete the list of Programs to completed
	 */
	/*public List<PatientProgram> getPatientProgramsToComplete() {
		return patientProgramsToComplete;
	}*/
	
	/**
	 * Sets the list of Patient Programs that need to be completed to process form submission
	 * 
	 * @param patientProgramsToComplete the list of Programs to completed
	 */
	/*public void setPatientProgramsToComplete(List<PatientProgram> patientProgramsToComplete) {
		this.patientProgramsToComplete = patientProgramsToComplete;
	}*/
	
	/**
	 * Returns the list of Relationships that need to be created to process form submission
	 * 
	 * @return the relationshipsToCreate
	 */
	/*public List<Relationship> getRelationshipsToCreate() {
		return relationshipsToCreate;
	}*/
	
	/**
	 * Sets the list of Relationships that need to be creatd to process form submission
	 * 
	 * @param relationshipsToCreate the relationshipsToCreate to set
	 */
	/*public void setRelationshipsToCreate(List<Relationship> relationshipsToCreate) {
		this.relationshipsToCreate = relationshipsToCreate;
	}*/
	
	/**
	 * Returns the list of Relationships that need to be voided to process form submission
	 * 
	 * @return the relationshipsToVoid
	 */
	/*public List<Relationship> getRelationshipsToVoid() {
		return relationshipsToVoid;
	}*/
	
	/**
	 * Sets the list of Relationships that need to be voided to process form submission
	 * 
	 * @param relationshipsToVoid the relationshipsToVoid to set
	 */
	/*public void setRelationshipsToVoid(List<Relationship> relationshipsToVoid) {
		this.relationshipsToVoid = relationshipsToVoid;
	}*/
	
	/**
	 * Returns the list of Relationships that need to be edited to process form submission
	 * 
	 * @return the relationshipsToEdit
	 */
	/*public List<Relationship> getRelationshipsToEdit() {
		return relationshipsToEdit;
	}*/
	
	/**
	 * Sets the list of Relationships that need to be edited to process form submission
	 * 
	 * @param relationshipsToEdit the relationshipsToEdit to set
	 */
	/*public void setRelationshipsToEdit(List<Relationship> relationshipsToEdit) {
		this.relationshipsToEdit = relationshipsToEdit;
	}*/
	
	/**
	 * @return the patientProgramsToUpdate
	 */
	/*public List<PatientProgram> getPatientProgramsToUpdate() {
		return patientProgramsToUpdate;
	}*/
	
	/**
	 * @param patientProgramsToUpdate the patientProgramsToUpdate to set
	 */
	/*public void setPatientProgramsToUpdate(List<PatientProgram> patientProgramsToUpdate) {
		this.patientProgramsToUpdate = patientProgramsToUpdate;
	}*/

	/**
	 * @return the identifiersToVoid
	 */
	/*public List<PatientIdentifier> getIdentifiersToVoid() {
		return identifiersToVoid;
	}*/

	/**
	 * @param identifiersToVoid the identifiersToVoid to set
	 */
	/*public void setIdentifiersToVoid(List<PatientIdentifier> identifiersToVoid) {
		this.identifiersToVoid = identifiersToVoid;
	}*/

    /**
     *
     * @return the exitFromCareProperty
     */
    /*public ExitFromCareProperty getExitFromCareProperty() {
        return exitFromCareProperty;
    }

    public void setExitFromCareProperty(ExitFromCareProperty exitFromCareProperty) {
        this.exitFromCareProperty = exitFromCareProperty;
    }*/
	
	  public List<CohortCustomFormSubmissionAction> getCustomFormSubmissionActions() {
	        return customFormSubmissionActions;
	    }

	    public void setCustomFormSubmissionActions(List<CohortCustomFormSubmissionAction> customFormSubmissionActions) {
	       this.customFormSubmissionActions=customFormSubmissionActions;
	    }
}
