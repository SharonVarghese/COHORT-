package org.openmrs.module.cohort.web.resource;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.cohort.rest.v1_0.resource.CohortRest;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name =RestConstants.VERSION_1 +CohortRest.COHORT_NAMESPACE+"/testcohort", supportedClass = CohortM.class, supportedOpenmrsVersions = { "1.8.*", "1.9.*, 1.10.*, 1.11.*","1.12.*" })
public class CohortRequestResource extends DataDelegatingCrudResource<CohortM> {

	@Override
	public DelegatingResourceDescription getRepresentationDescription(
			Representation rep) {

		DelegatingResourceDescription description = null;

		if (Context.isAuthenticated()) {
			description = new DelegatingResourceDescription();
			if (rep instanceof DefaultRepresentation) {
				description.addProperty("cohortId");
				description.addProperty("name");
				description.addProperty("description");
				description.addProperty("startDate");
				description.addProperty("endDate");
				description.addProperty("uuid");
				description.addProperty("cohortType");
			} else if (rep instanceof FullRepresentation) {
				description.addProperty("cohortId");
				description.addProperty("name");
				description.addProperty("description");		
				description.addProperty("startDate");
				description.addProperty("endDate");
				description.addProperty("uuid");
			    description.addProperty("cohortType");
			}
		}
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("name");
		description.addProperty("description");	
		description.addProperty("startDate");
		description.addProperty("endDate");
		description.addRequiredProperty("cohortType");
		return description;
	}
	
	@Override
	public CohortM save(CohortM arg0) {
		return Context.getService(CohortService.class).saveCohort(arg0);
	}

	@Override
	protected void delete(CohortM arg0, String arg1, RequestContext arg2)
			throws ResponseException {
		Context.getService(CohortService.class).purgeCohort(arg0);
		
	}

	@Override
	public void purge(CohortM arg0, RequestContext arg1)
			throws ResponseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CohortM newDelegate() {
		return new CohortM();
	}

	@Override
	public CohortM getByUniqueId(String uuid) {
		return Context.getService(CohortService.class).getCohortUuid(uuid);
	}

}
