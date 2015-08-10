<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<%@ taglib prefix="wgt" uri="/WEB-INF/view/module/htmlwidgets/resources/htmlwidgets.tld" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<h3>Add Cohort Member</h3>
<form id="form1" method="post">
Patient Name<br/>
<select name="pname">
<option value=""></option>
<c:forEach var="pname" items="${pnames}">
<option value="${pname}"<c:if test="${pname == status.value}">selected</c:if>>${pname}</option>
</c:forEach>
</select>			   
<br/>
<br/>
Type:<br/>
<input type="text" name="type" id="type" size="25" value="${status.value}"/><br/><br/>
Role<br/>
<select name="format">
<option value=""></option>
<c:forEach var="format" items="${formats}">
<option value="${format}"<c:if test="${format == status.value}">selected</c:if>>${format}</option>
</c:forEach>
</select>			   
<br/>
<br/>
<spring:bind path="cpatient.startDate">
<spring:message code="cohort.startdate" /> :<br/>
<input type="text" name="startDate" size="10" onFocus="showCalendar(this,60)"
						   id="startDate" value="${status.value}" /><i style="font-weight: normal; font-size: 0.8em;">(<openmrs:message code="general.format"/>: <openmrs:datePattern />)</i><br/><br/>
						   
<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
</spring:bind>
<spring:bind path="cpatient.endDate">
<spring:message code="cohort.enddate" /> :<br/>
<input type="text" name="endDate" size="10" onFocus="showCalendar(this,60)"
						   id="endDate" value="${status.value}" /><i style="font-weight: normal; font-size: 0.8em;">(<openmrs:message code="general.format"/>: <openmrs:datePattern />)</i><br/><br/>
						   
<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
</spring:bind>
<input type="submit" value="Add Cohort Member" id="add" name="add"/>	
</form>
<%@ include file="/WEB-INF/template/footer.jsp"%>

