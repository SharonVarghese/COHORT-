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
package org.openmrs.module.cohort.api.db;

import java.util.List;

import org.openmrs.Form;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.htmlformentry.HtmlForm;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformentry.element.PersonStub;

/**
 * Defines the methods that must be implemented by the Data Access Object
 */
public interface HtmlFormEntryDAO {

	/**
	 * Retrieves the HtmlForm referenced by id
	 * 
	 * @param id
	 * @return HtmlForm
	 */
    public HtmlForm getHtmlForm(Integer id);
    
	/**
	 * Retrieves the HtmlForm referenced by uuid
	 * 
	 * @param uuid
	 * @return HtmlForm
	 */
	public HtmlForm getHtmlFormByUuid(String uuid) ;
    
    /**
     * Retrieves the HtmlForm associated with a standard OpenMRS Form object
     * 
     * @param form
     * @return HtmlForm
     */
    public HtmlForm getHtmlFormByForm(Form form);

    /**
     * Saves an HtmlForm to the database
     * 
     * @param htmlForm
     * @return
     */
    public HtmlForm saveHtmlForm(HtmlForm htmlForm);

    /**
     * Deletes an HtmlForm from the database
     * 
     * @param htmlForm
     */
    public void deleteHtmlForm(HtmlForm htmlForm);

    /**
     * Returns all HtmlForms in the database
     * @return
     */
    public List<HtmlForm> getAllHtmlForms();

	/**
	 * @see HtmlFormEntryService#needsNameAndDescriptionMigration()
	 */
	public boolean needsNameAndDescriptionMigration();
	
	/**
     * @see HtmlFormEntryService#getProviderStub()
     */
	public List<PersonStub> getUsersAsPersonStubs(String roleName);
	
	/**
	 * Given a uuid and a class, fetch the OpenMRS object associated with that uuid
	 */
	public OpenmrsObject getItemByUuid(Class<? extends OpenmrsObject> type, String uuid);
	 
	/**
	 * Given an id and a class, fetch the OpenMRS object associated with that id
	 */
	public OpenmrsObject getItemById(Class<? extends OpenmrsObject> type, Integer id);
	
	/**
	 * Given a name and a class, fetch the OpenMRS object associated with that name
	 */
	public OpenmrsObject getItemByName(Class<? extends OpenmrsMetadata> type, String name);
	
	/**
     * @see HtmlFormEntryService#getPersonIdsHavingAttributes(String attributeId, String attributeValue)
     */      
	public List<Integer> getPersonIdHavingAttributes(String attributeId, String attributeValue);

}
