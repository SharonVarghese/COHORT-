<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<script type="text/javascript" charset="utf-8"></script>
<h3>Delete Cohort Program</h3>
<form method="get">
<spring:bind path="cohortprogram.name">
<spring:message code="cohort.cohorttypename" /> <br/>
<input type= "text" name="name" id="name" size="25" value="${status.value}"/> <br/> <br/>
<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
            </spring:bind>
<input type="submit" value="delete" id="delete" name="delete"/>				   
</form>