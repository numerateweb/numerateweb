package org.numerateweb.math.om.cd;

import javax.xml.namespace.QName;

public interface OMCD {
	final String NS = "http://www.openmath.org/OpenMathCD";

	final QName CD = new QName(NS, "CD");
	final QName CDBASE = new QName(NS, "CDBase");
	final QName CDCOMMENT = new QName(NS, "CDComment");
	final QName CDDATE = new QName(NS, "CDDate");
	final QName CDDEFINITION = new QName(NS, "CDDefinition");
	final QName CDNAME = new QName(NS, "CDName");
	final QName CDREVIEWDATE = new QName(NS, "CDReviewDate");
	final QName CDREVISION = new QName(NS, "CDRevision");
	final QName CDSTATUS = new QName(NS, "CDStatus");
	final QName CDURL = new QName(NS, "CDURL");
	final QName CDUSES = new QName(NS, "CDUses");
	final QName CDVERSION = new QName(NS, "CDVersion");
	final QName DESCRIPTION = new QName(NS, "Description");

	final QName NAME = new QName(NS, "Name");
	final QName ROLE = new QName(NS, "Role");
	final QName CMP = new QName(NS, "CMP");
	final QName FMP = new QName(NS, "FMP");
	final QName EXAMPLE = new QName(NS, "Example");
}
