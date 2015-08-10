package org.openmrs.module.cohort.hfe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.anotheria.webutils.servlet.request.HttpServletRequestMockImpl;

import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.htmlformentry.HtmlForm;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.widget.Widget;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.SerializationException;
import org.openmrs.serialization.SimpleXStreamSerializer;

public class CohortSerializableFormObject implements Serializable {

	 private static final long serialVersionUID = 1L;

	    private String cohortUuid;
	    private String encounterUuid;
	    private Map<String,String[]> parameterMap;
	    private String xmlDefinition;
	    private int htmlFormId;

	    private transient CohortFormEntrySession session = null;

	    private class InnerHttpServletRequestMock extends HttpServletRequestMockImpl{
	        private Map<String, String[]> paramMap;
	        InnerHttpServletRequestMock(Map<String,String[]> parameterMap) {
	            super();
	            this.paramMap = parameterMap;
	        }
	        public Map getParamMap() {
	            return paramMap;
	        }

	        public String getParameter(String parameter) {
	            return paramMap.get(parameter)[0];
	        }
	    }

	    public CohortSerializableFormObject() {
	    }

	    public CohortSerializableFormObject(String xmlDefinition, Map<String, String[]> parameterMap,int htmlFormId) {
	        this.parameterMap = parameterMap;
	        this.xmlDefinition = xmlDefinition;
	        this.htmlFormId = htmlFormId;
	    }

	    public CohortSerializableFormObject(String xmlDefinition, Map<String, String[]> parameterMap,String cohortUuid, String encounterUuid, int htmlFormId) {
	        this.xmlDefinition = xmlDefinition;
	        this.encounterUuid = encounterUuid;
	        this.cohortUuid = cohortUuid;
	        this.parameterMap = parameterMap;
	        this.htmlFormId = htmlFormId;
	    }

	    public static long getSerialVersionUID() {
	        return serialVersionUID;
	    }

	    public String getXmlDefinition() {
	        return xmlDefinition;
	    }

	    public void setXmlDefinition(String xmlDefinition) {
	        this.xmlDefinition = xmlDefinition;
	    }

	    public String getEncounterUuid() {
	        return encounterUuid;
	    }

	    public void setEncounterUuid(String encounterUuid) {
	        this.encounterUuid = encounterUuid;
	    }

	    public String getCohortUuid() {
	        return cohortUuid;
	    }

	    public void setCohortUuid(String cohortUuid) {
	        this.cohortUuid = cohortUuid;
	    }

	    public Map<String, String[]> getParameterMap() {
	        return parameterMap;
	    }

	    public void setParameterMap(Map<String, String[]> parameterMap) {
	        this.parameterMap = parameterMap;
	    }

	    public CohortFormEntrySession getSession() throws Exception{
	        if(session==null)createSession();
	        return session;
	    }

	    /**
	     * This method returns a FormEntrySession object using data in the instance object
	     * @return FormEntrySession object
	     * @throws Exception
	     */
	    private void createSession() throws Exception {
	        //TODO: Check for null patientUuid and try to parse the xml to obtain the patient ID instead.
	       CohortM cohort=Context.getService(CohortService.class).getCohortUuid(getCohortUuid());
	        session = new CohortFormEntrySession(cohort,getXmlDefinition(),null);
	        HtmlForm htmlForm = HtmlFormEntryUtil.getService().getHtmlForm(htmlFormId);
	        htmlForm.setXmlData(xmlDefinition);
	        session.setHtmlForm(htmlForm);

	        //getHtmlToDisplay() is called to generate necessary tag handlers and cache the form
	        session.getHtmlToDisplay();

	        //PrepareForSubmit is called to set patient and encounter if specified in tags
	        session.prepareForSubmit();
	    }

	    /**
	     *
	     * @return
	     */
	    private HttpServletRequest createHttpServletRequest() throws Exception{
	        if(getParameterMap()==null) {
	            throw new Exception("Could not create Request without parameters");
	        }
	        HttpServletRequest request = new InnerHttpServletRequestMock(getParameterMap());
	        return request;
	    }

	    public String getFileName() {
	        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
	        String filename =  cohortUuid + "-" + df.format(new Date());
	        return filename;
	    }

	    /**
	     *
	     * @param directoryPath
	     * @throws Exception
	     */
	    public void serializeToXml(String directoryPath) throws Exception {
	        String filename;
	        if(directoryPath.endsWith(File.separator)) {
	            filename = directoryPath.concat(getFileName());
	        } else {
	            filename = directoryPath.concat(File.separator + getFileName());
	        }

	        //Use OpenMRS simpleXStreamSerializer
	        OpenmrsSerializer serializer = Context.getSerializationService().getSerializer(SimpleXStreamSerializer.class);

	        String xmlEquivalent = serializer.serialize(this);

	        BufferedWriter bw = null;
	        try {
	            bw = new BufferedWriter(new FileWriter(filename+".xml"));
	            bw.write(xmlEquivalent);
	        }finally {
	            if(bw != null) bw.close();
	        }
	    }
	    /**
	     * Given argument of the file this method tries to deserialize the contents of the file into SerializableFormObject
	     * @param argument used to pass either a file path or a string representing the archivedData
	     * @param isPath used to indicate whether the first argument is file path (true means it is)
	     * @return  equivalent SerializableFormObject representation
	     * @throws Exception
	     */
	    public static CohortSerializableFormObject deserializeXml(String argument,boolean isPath) throws Exception {
	        if(isPath) {
	            //TODO:Check for existence of the file
	            BufferedReader br = null;
	            try {
	                br = new BufferedReader(new FileReader(argument));
	                char[] buffer = new char[1024];
	                StringBuilder xmlSb = new StringBuilder();
	                int lengthRead;
	                while ((lengthRead = br.read(buffer)) != -1) {
	                    xmlSb.append(buffer, 0, lengthRead);
	                }
	                return deserializeXmlString(xmlSb.toString());
	            } finally {
	                if (br != null) br.close();
	            }
	        } else {
	            return deserializeXmlString(argument);
	        }
	    }

	    public static CohortSerializableFormObject deserializeXml(String path) throws Exception {
	        return deserializeXml(path,true);
	    }

	    public void handleSubmission() throws Exception{
	        //Get the FormEntrySession & HttpServletRequest
	        if(session==null)createSession();
	        HttpServletRequest request = createHttpServletRequest();
	        Map<Widget,String> fields = session.getContext().getFieldNames();

	        //handle submission & save data
	        session.getController().handleFormSubmission(session, request);
	    }

	    private static CohortSerializableFormObject deserializeXmlString(String xml) throws SerializationException{
	        OpenmrsSerializer serializer = Context.getSerializationService().getSerializer(SimpleXStreamSerializer.class);
	        return serializer.deserialize(xml, CohortSerializableFormObject.class);
	    }
}
