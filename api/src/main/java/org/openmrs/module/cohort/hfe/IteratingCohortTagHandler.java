package org.openmrs.module.cohort.hfe;

import java.io.PrintWriter;
import org.w3c.dom.Node;

public interface IteratingCohortTagHandler extends CohortTagHandler {

	public boolean shouldRunAgain(CohortFormEntrySession session, PrintWriter out, Node parent, Node node);
}
