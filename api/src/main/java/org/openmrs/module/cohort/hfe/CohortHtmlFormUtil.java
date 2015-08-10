package org.openmrs.module.cohort.hfe;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.FormField;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortEncounter;
import org.openmrs.module.cohort.CohortObs;
import org.openmrs.module.cohort.api.CohortFormEntryService;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.htmlformentry.FormEntryException;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.HtmlForm;
import org.openmrs.module.htmlformentry.HtmlFormEntryConstants;
import org.openmrs.module.htmlformentry.HtmlFormEntryGlobalProperties;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction;
import org.openmrs.module.htmlformentry.action.ObsGroupAction;
import org.openmrs.module.htmlformentry.element.GettingExistingOrder;
import org.openmrs.module.htmlformentry.element.ObsSubmissionElement;
import org.openmrs.obs.ComplexData;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.DrugEditor;
import org.openmrs.propertyeditor.EncounterTypeEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.PatientEditor;
import org.openmrs.propertyeditor.PersonEditor;
import org.openmrs.propertyeditor.UserEditor;
import org.openmrs.util.OpenmrsUtil;

public class CohortHtmlFormUtil {
	
	static CohortFormEntryService cohortFormEntryService;
	public static CohortObs createObs(Concept concept, Object value, Date datetime, String accessionNumber) {
		CohortObs obs = new CohortObs();
		obs.setConcept(concept);
		ConceptDatatype dt = obs.getConcept().getDatatype();
		if (dt.isNumeric()) {
			obs.setValueNumeric(Double.parseDouble(value.toString()));
		} else if (HtmlFormEntryConstants.COMPLEX_UUID.equals(dt.getUuid())) {
			obs.setComplexData((ComplexData) value);
			obs.setValueComplex(obs.getComplexData().getTitle());
		} else if (dt.isText()) {
			if (value instanceof Location) {
				Location location = (Location) value;
				obs.setValueText(location.getId().toString() + " - " + location.getName());
			} else if (value instanceof Person) {
				Person person = (Person) value;
				obs.setValueText(person.getId().toString() + " - " + person.getPersonName().toString());
			} else {
				obs.setValueText(value.toString());
			}
		} else if (dt.isCoded()) {
             if (value instanceof ConceptName) {
                obs.setValueCodedName((ConceptName) value);
                obs.setValueCoded(obs.getValueCodedName().getConcept());
            } else if (value instanceof Concept) {
				obs.setValueCoded((Concept) value);
            } else {
				obs.setValueCoded((Concept) convertToType(value.toString(), Concept.class));
            }
		} else if (dt.isBoolean()) {
			if (value != null) {
				try {
					obs.setValueAsString(value.toString());
				}
				catch (ParseException e) {
					throw new IllegalArgumentException("Unable to convert " + value + " to a Boolean Obs value", e);
				}
			}
		} else if (ConceptDatatype.DATE.equals(dt.getHl7Abbreviation())
		        || ConceptDatatype.TIME.equals(dt.getHl7Abbreviation())
		        || ConceptDatatype.DATETIME.equals(dt.getHl7Abbreviation())) {
			Date date = (Date) value;
			obs.setValueDatetime(date);
		} else if ("ZZ".equals(dt.getHl7Abbreviation())) {
			// don't set a value
		} else {
			throw new IllegalArgumentException("concept datatype not yet implemented: " + dt.getName()
			        + " with Hl7 Abbreviation: " + dt.getHl7Abbreviation());
		}
		if (datetime != null)
			obs.setObsDateTime(datetime);
		if (accessionNumber != null)
			obs.setAccessionNumber(accessionNumber);
		return obs;
	}
	
	public static Object convertToType(String val, Class<?> clazz) {
		if (val == null)
			return null;
		if ("".equals(val) && !String.class.equals(clazz))
			return null;
		if (Location.class.isAssignableFrom(clazz)) {
			LocationEditor ed = new LocationEditor();
			ed.setAsText(val);
			return ed.getValue();
		} else if (User.class.isAssignableFrom(clazz)) {
			UserEditor ed = new UserEditor();
			ed.setAsText(val);
			return ed.getValue();
		} else if (Date.class.isAssignableFrom(clazz)) {
			// all HTML Form Entry dates should be submitted as yyyy-mm-dd
			try {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				df.setLenient(false);
				return df.parse(val);
			}
			catch (ParseException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (Double.class.isAssignableFrom(clazz)) {
			return Double.valueOf(val);
		} else if (Integer.class.isAssignableFrom(clazz)) {
			return Integer.valueOf(val);
		} else if (Concept.class.isAssignableFrom(clazz)) {
			ConceptEditor ed = new ConceptEditor();
			ed.setAsText(val);
			return ed.getValue();
        } else if (Drug.class.isAssignableFrom(clazz)) {
            DrugEditor ed = new DrugEditor();
            ed.setAsText(val);
            return ed.getValue();
        } else if (Patient.class.isAssignableFrom(clazz)) {
			PatientEditor ed = new PatientEditor();
			ed.setAsText(val);
			return ed.getValue();
		} else if (Person.class.isAssignableFrom(clazz)) {
			PersonEditor ed = new PersonEditor();
			ed.setAsText(val);
			return ed.getValue();
		} else if (EncounterType.class.isAssignableFrom(clazz)) {
			EncounterTypeEditor ed = new EncounterTypeEditor();
			ed.setAsText(val);
			return ed.getValue();
		} else {
			return val;
		}
	}
	
	public static CohortObs createObs(FormField formField, Object value, Date datetime, String accessionNumber) {
		Concept concept = formField.getField().getConcept();
		if (concept == null)
			throw new FormEntryException("Can't create an Obs for a formField that doesn't represent a Concept");
		return createObs(concept, value, datetime, accessionNumber);
	}
	
	public static void removeEmptyObs(Collection<CohortObs> obsList) {
		if (obsList != null) {
			Set<CohortObs> obsToRemove = new HashSet<CohortObs>();
			for (CohortObs o : obsList) {
				removeEmptyObs(o.getGroupMembers());
				boolean valueEmpty = StringUtils.isEmpty(o.getValueAsString(Context.getLocale()));
				boolean membersEmpty = o.getGroupMembers() == null || o.getGroupMembers().isEmpty();
				if (valueEmpty && membersEmpty) {
					obsToRemove.add(o);
				}
			}
			for (CohortObs o : obsToRemove) {
				if (o.getObsGroup() != null) {
					//o.getObsGroup().removeGroupMember(o);
					o.setObsGroup(null);
				}
				if (o.getEncounterId() != null) {
					o.getEncounterId().removeObs(o);
					o.setEncounterId(null);
				}
				obsList.remove(o);
			}
		}
	}

	/*public static CohortFormEntryService getService() {
		return Context.getService(CohortFormEntryService.class);
	}*/

	public static String getArchiveDirPath() {
		String value = Context.getAdministrationService().getGlobalProperty("htmlformentry.archiveDir");
		if(value != null && org.springframework.util.StringUtils.hasLength(value)) {

			//Replace %Y and %M if any
			Date today = new Date();
			GregorianCalendar gCal = new GregorianCalendar();
			value = value.replace("%Y", String.valueOf(gCal.get(Calendar.YEAR)));
			value = value.replace("%y",String.valueOf(gCal.get(Calendar.YEAR)));


			int month = gCal.get(Calendar.MONTH);
			month++;
			if(month<10) {
				value = value.replace("%M","0"+month);
				value = value.replace("%m","0"+month);
			}
			else {
				value = value.replace("%M",String.valueOf(month));
				value = value.replace("%m",String.valueOf(month));
			}

			//Check if not absolute concatenate with application directory
			File path = new File(value);
			if(!path.isAbsolute()) {
				 return OpenmrsUtil.getApplicationDataDirectory() + File.separator + value;
			}
			return value;
		}
		return null;
	}

	public static CohortFormEntryService getService() {
		return Context.getService(CohortFormEntryService.class);
	}
	public static boolean hasProvider(CohortEncounter e) {
		try {
			Method method = e.getClass().getMethod("getProvidersByRoles");
			// this is a Map<EncounterRole, Set<Provider>>
			Map providersByRoles = (Map) method.invoke(e);
			return providersByRoles != null && providersByRoles.size() > 0;
		}
		catch (Exception ex) {
			return e.getProvider() != null;
		}
	}
	
	/*public static void voidEncounter(CohortEncounter e, HtmlForm htmlform, String voidReason) throws Exception {
		if (voidReason == null) {
			voidReason = "htmlformentry";
		}
		
		if (HtmlFormEntryGlobalProperties.VOID_ENCOUNTER_BY_HTML_FORM_SCHEMA() != null) {
			
			if (HtmlFormEntryGlobalProperties.VOID_ENCOUNTER_BY_HTML_FORM_SCHEMA() == true) {
				voidEncounterByHtmlFormSchema(e, htmlform, voidReason);
			} else {
			  Context.getService(CohortService.class).voidEncounter(e,voidReason);
			}
			
		} else if (HtmlFormEntryGlobalProperties.HTML_FORM_FLOWSHEET_STARTED() != null
		        && HtmlFormEntryGlobalProperties.HTML_FORM_FLOWSHEET_STARTED() == true) {
			voidEncounterByHtmlFormSchema(e, htmlform, voidReason);
		} else {
			Context.getService(CohortService.class).voidEncounter(e,voidReason);
		}
	}
	
	public static void voidEncounterByHtmlFormSchema(CohortEncounter e, HtmlForm htmlform, String voidReason) throws Exception {
		if (e != null && htmlform != null) {
			if (voidReason == null)
				voidReason = "htmlformentry";
			boolean shouldVoidEncounter = true;
			Map<Obs, Obs> replacementObs = new HashMap<Obs, Obs>();//new, then source
			Map<Order, Order> replacementOrders = new HashMap<Order, Order>();//new, then source
			CohortEncounter eTmp = returnEncounterCopy(e, replacementObs, replacementOrders);
			CohortFormEntrySession session = new CohortFormEntrySession(eTmp.getCohort(), eTmp, Mode.VIEW, htmlform, null); // session gets a null HttpSession
            session.getHtmlToDisplay();
			List<CohortFormSubmissionControllerAction> actions = session.getController().getActions();
			for (CohortFormSubmissionControllerAction lfca : actions) {
				if (lfca instanceof ObsSubmissionElement) {
					ObsSubmissionElement ose = (ObsSubmissionElement) lfca;
				}
				if (lfca instanceof ObsGroupAction) {
					ObsGroupAction oga = (ObsGroupAction) lfca;
				}
			}
			
			
			
			for (CohortObs o : e.getAllObs(false)) { //ignore voided obs
				boolean matched = false;
				if (!matched)
					shouldVoidEncounter = false;
			}
			
			if (shouldVoidEncounter) {
				e.setVoided(true);
				e.setVoidedBy(Context.getAuthenticatedUser());
				e.setVoidReason(voidReason);
				e.setDateVoided(new Date());
			}
			eTmp = null;
		}
	}
	
	private static CohortEncounter returnEncounterCopy(CohortEncounter source, Map<Obs, Obs> replacementObs,
            Map<Order, Order> replacementOrders) throws Exception {
if (source != null) {
CohortEncounter encNew = (CohortEncounter) returnCopy(source);
return encNew;
}
return null;
}

private static Object returnCopy(Object source) throws Exception {
	Class<? extends Object> clazz = source.getClass();
	Object ret = clazz.newInstance();
	Set<String> fieldNames = new HashSet<String>();
	List<Field> fields = new ArrayList<Field>();
	addSuperclassFields(fields, clazz);
	for (Field f : fields) {
		fieldNames.add(f.getName());
	}
	for (String root : fieldNames) {
		for (Method getter : clazz.getMethods()) {
			if (getter.getName().toUpperCase().equals("GET" + root.toUpperCase())
			        && getter.getParameterTypes().length == 0) {
				Method setter = getSetter(clazz, getter, "SET" + root.toUpperCase());
				//NOTE: Collection properties are not copied
				if (setter != null && methodsSupportSameArgs(getter, setter)
				        && !(getter.getReturnType().isInstance(Collection.class))) {
					Object o = getter.invoke(source, Collections.EMPTY_LIST.toArray());
					if (o != null) {
						setter.invoke(ret, o);
					}
				}
			}
		}
	}
	return ret;
}

private static void addSuperclassFields(List<Field> fields, Class<? extends Object> clazz) {
	fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
	if (clazz.getSuperclass() != null) {
		addSuperclassFields(fields, clazz.getSuperclass());
	}
	}
	
private static Method getSetter(Class<? extends Object> clazz, Method getter, String methodname) {
		
		List<Method> setterMethods = getMethodCaseInsensitive(clazz, methodname);
		if (setterMethods != null && !setterMethods.isEmpty()) {
			if (setterMethods.size() == 1) {
				return setterMethods.get(0);
			} else if (setterMethods.size() > 1) {
				for (Method m : setterMethods) {
					Class<?>[] parameters = m.getParameterTypes();
					for (Class<?> parameter : parameters) {
						if (getter.getReturnType().equals(parameter)) {
							return m;
						}
					}
				}
			}
		}
		return null;
	}

private static boolean methodsSupportSameArgs(Method getter, Method setter) {
	if (getter != null && setter != null && setter.getParameterTypes() != null && setter.getParameterTypes().length == 1
	        && getter.getReturnType() != null && getter.getReturnType().equals(setter.getParameterTypes()[0]))
		return true;
	return false;
}

private static List<Method> getMethodCaseInsensitive(Class<? extends Object> clazz, String methodName) {
	
	List<Method> methodList = new ArrayList<Method>();
	for (Method m : clazz.getMethods()) {
		if (m.getName().toUpperCase().equals(methodName.toUpperCase())) {
			methodList.add(m);
			
		}
	}
	return methodList;
}*/
}
