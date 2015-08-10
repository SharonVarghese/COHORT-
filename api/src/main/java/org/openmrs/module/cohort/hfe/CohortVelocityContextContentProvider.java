package org.openmrs.module.cohort.hfe;

import org.apache.velocity.VelocityContext;

public interface CohortVelocityContextContentProvider {
	
	void populateContext(CohortFormEntrySession session, VelocityContext velocityContext);

}
