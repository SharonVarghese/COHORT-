<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<script type="text/javascript" charset="utf-8"></script>
<h3>Delete Cohort Member Attribute</h3>
<form method="get">
<spring:bind path="cohortatt.value">
Cohort Member Attribute Value <br/>
<input type= "text" name="value" id="value" size="25" value="${status.value}"/> <br/> <br/>
<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
            </spring:bind>
<input type="submit" value="delete" id="delete" name="delete"/>				   
</form>
<%@ include file="/WEB-INF/template/footer.jsp"%>