/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.cohort.web.controller;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortRole;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.web.WebConstants;
import org.openmrs.web.taglib.fieldgen.FieldGenHandlerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * The main controller.
 */
@Controller
public class  AddCohortController {
	
	protected final Log log = LogFactory.getLog(getClass());
	 private SessionStatus status;
	 List<Patient> list1=new ArrayList();
	 Set set1 = new HashSet();
	 
	@RequestMapping(value = "/module/cohort/addcohort", method = RequestMethod.GET)
		public void manage(ModelMap model) {
			model.addAttribute("cohortmodule",new CohortM());
			List<String> cohorttype=new ArrayList<String>();
			LocationService service=Context.getLocationService();
			List<Location> formats=service.getAllLocations();
			model.addAttribute("locations",formats);
			CohortService service1=Context.getService(CohortService.class);
			List<CohortType> list1 = service1.getAllCohortTypes();
			for (int i = 0; i < list1.size(); i++) {
    		    CohortType c = list1.get(i);
    		    cohorttype.add(c.getName());
        	}
			model.addAttribute("formats", cohorttype);
		} 
	 @RequestMapping(value = "module/cohort/addcohort.form", method = RequestMethod.POST)
	    public void onSubmit(WebRequest request, HttpSession httpSession,HttpServletRequest request1,
	                                   @RequestParam(required = false, value = "name") String cohort_name,
	                                   @RequestParam(required = false, value = "description") String description,
	                                   @RequestParam(required = false, value = "startDate") String start_date,
	                                   @RequestParam(required = false, value = "endDate") String end_date,
	                                   @ModelAttribute("cohortmodule") CohortM cohortmodule ,BindingResult errors,ModelMap model)
	       {
		    CohortMember p=new CohortMember();
		    CohortType cohort1=new CohortType();
		    Location loc=new Location();
		    String cohort_type_name=request.getParameter("format");
		    String location=request.getParameter("location");
	        CohortService departmentService = Context.getService(CohortService.class);
	        if (!Context.isAuthenticated()) {
	            errors.reject("Required");
	        } 
	       
	        String voided = request.getParameter("voided");
	        
	        if(cohort_name=="" && description=="" && start_date==null && end_date==null && location.length()==0 && cohort_type_name.length()==0)
	        {
	        	httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,"Values should not be null");
	        	model.addAttribute("cohortmodule",new CohortM());
				LocationService service=Context.getLocationService();
				List<Location> formats=service.getAllLocations();
				model.addAttribute("locations",formats);
	        }
	       

	        else {
	        	 /*try {
	 				java.util.Date start = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH).parse(start_date);
	 				 java.util.Date end = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH).parse(end_date);
	 				 if (start.compareTo(end) < 0 || start.compareTo(end)==0) {*/
	 					//cohortmodule.setLocation(location);
	 		        	List<CohortType> cohorttype1 =departmentService.findCohortType(cohort_type_name);
	 		        	LocationService service=Context.getLocationService();
	 		        	List<Location> formats=service.getLocations(location);
	 		        	for(int j=0;j<formats.size();j++)
	 		        	{
	 		        		loc=formats.get(j);
	 		        	}
	 		        	for (int i = 0; i < cohorttype1.size(); i++) {
	 		    		    cohort1 =cohorttype1.get(i);
	 		        	}
	 		        	cohortmodule.setClocation(loc);
	 		        	cohortmodule.setCohortType(cohort1);
	 		        	departmentService.saveCohort(cohortmodule); 
	 		        	httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR,"insertion success");
	 		        }       
	 			//}
	        	/*catch (ParseException e) {
	 				// TODO Auto-generated catch block
	 				e.printStackTrace();
	 			}  	
	     }*/
 }
	 @RequestMapping(value = "/module/cohort/cpatients.form", method = RequestMethod.GET)
		public void manage1(ModelMap model, HttpSession httpSession,HttpServletRequest request, @RequestParam("cpid") Integer id, @ModelAttribute("cpatient") CohortMember cohort ) {
			model.addAttribute("cpatient",new CohortMember());
			List<String> type=new ArrayList<String>();
			List<String> names=new ArrayList<String>();
			CohortService departmentService = Context.getService(CohortService.class);
			PatientService ps=Context.getPatientService();
			List<Patient> pn=ps.getAllPatients();
			 List<CohortM> cohort1=departmentService.findCohort(id);
	      	  for (int i = 0; i < cohort1.size(); i++) {
	  		    CohortM cohort2 =cohort1.get(i);
	  		    String cname=cohort2.getName();
	  		  List<CohortRole> cr=departmentService.findRoles(cname);
	  		  for(int k=0;k<cr.size();k++)
			  {
				CohortRole c3=cr.get(k);
				type.add(c3.getName());
			  }
	      	  }
			for(int j=0;j<pn.size();j++)
			{
			  Patient p=pn.get(j);
		      names.add(p.getGivenName());
			}
			model.addAttribute("pnames",names);
			model.addAttribute("formats", type);
			
		}
	@RequestMapping(value = "module/cohort/cpatients.form", method = RequestMethod.POST)
	 public void onClick(WebRequest request, HttpSession httpSession, ModelMap model,
             @RequestParam(required = false, value = "type") String type,
             @RequestParam(required = false, value = "startDate") String startDate,
             @RequestParam("cpid") Integer id,@ModelAttribute("cpatient") CohortMember cpatient, @ModelAttribute("patient") Patient patient , BindingResult errors)
	 {
		  CohortM cohort=new CohortM();
		  CohortRole  c2=new CohortRole();
		  List<String> names=new ArrayList<String>();
		  List<String> type1=new ArrayList<String>();
		  CohortService departmentService = Context.getService(CohortService.class);
		  PatientService ps=Context.getPatientService();
		  List<Patient> pn=ps.getAllPatients();
		  String cname;
		  List<CohortM> cohort1=departmentService.findCohort(id);
      	  for (int i = 0; i < cohort1.size(); i++) {
  		    cohort =cohort1.get(i);
  		    cname=cohort.getName();
  		  List<CohortRole> cr=departmentService.findRoles(cname);
  		  for(int k=0;k<cr.size();k++)
		  {
			CohortRole c3=cr.get(k);
			type1.add(c3.getName());
		  }
      	  }
		  //List<CohortRole> cr=departmentService.findRoles(cname);
	      /*for(int k=0;k<cr.size();k++)
		  {
			CohortRole c3=cr.get(k);
			type1.add(c3.getName());
		  }*/
		  for(int b=0;b<pn.size();b++)
		  {
		    	 Patient p1 = pn.get(b);
		    	 names.add(p1.getGivenName());
		   }
			model.addAttribute("pnames",names);
			model.addAttribute("formats",type1);
			 
	        	String patname=request.getParameter("pname");
	        	List<Patient> p2=ps.getPatients(patname);
	        	for(int l=0;l<p2.size();l++)
	        	{
	        		patient=p2.get(l);
	        	}
	        	String rolname=request.getParameter("format");
	        	List<CohortRole> crole =departmentService.findCohortRoles(rolname);
		        	for (int g = 0;g <crole.size(); g++) {
		    		    c2 =crole.get(g);
		        	}
	           cpatient.setPerson(patient);
	           cpatient.setCohort(cohort);
	           cpatient.setRole(c2);
			  departmentService.saveCPatient(cpatient);
			  httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR,"Insertion success");	
	 }
}
	