<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>
<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />

<script type="text/javascript" charset="utf-8"></script>
<h3>Add Cohort Attribute</h3>
<form method="post">
<spring:bind path="cohortatt.cohort">
Cohort Names:<br/>
<select name="names">
<option value=""></option>
<c:forEach var="names" items="${formats1}">
<option value="${names}"<c:if test="${names == status.value}">selected</c:if>>${names}</option>
</c:forEach>
</select>	
<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
</spring:bind>
<br/>
Cohort Attribute Type:<br/>
<select name="format">
<option value=""></option>
<c:forEach var="format" items="${formats}">
<option value="${format}"<c:if test="${format == status.value}">selected</c:if>>${format}</option>
</c:forEach>
<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
</select>
</td>
</tr>
</table>
<spring:bind path="cohortatt.value">
Value:<br/>
<input type= "text" name="value" id="value" size="25" value="${status.value}"/> <br/> <br/>
 <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
</spring:bind>
<tr>
<td>  
<br/>
<br/>                    
<input type="submit" value="Add Cohort Attribute" id="submit"/>				   
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>