package org.numerateweb.math.om.cd;

import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;


public interface META {
	final URI NS = URIs.createURI("http://www.openmath.org/cd/meta#");
	
	final URI CD = NS.appendLocalPart("CD");
	final URI CDBASE = NS.appendLocalPart("CDBase");
	final URI CDCOMMENT = NS.appendLocalPart("CDComment");
	final URI CDDATE = NS.appendLocalPart("CDDate");
	final URI CDDEFINITION = NS.appendLocalPart("CDDefinition");
	final URI CDNAME = NS.appendLocalPart("CDName");
	final URI CDREVIEWDATE = NS.appendLocalPart("CDReviewDate");
	final URI CDREVISION = NS.appendLocalPart("CDRevision");
	final URI CDSTATUS = NS.appendLocalPart("CDStatus");
	final URI CDURL = NS.appendLocalPart("CDURL");
	final URI CDUSES = NS.appendLocalPart("CDUses");
	final URI CDVERSION = NS.appendLocalPart("CDVersion");
	final URI DESCRIPTION = NS.appendLocalPart("Description");

	final URI NAME = NS.appendLocalPart("Name");
	final URI ROLE = NS.appendLocalPart("Role");
	final URI CMP = NS.appendLocalPart("CMP");
	final URI FMP = NS.appendLocalPart("FMP");
	final URI EXAMPLE = NS.appendLocalPart("Example");
}
