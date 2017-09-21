package org.numerateweb.math.xml;

import javax.xml.namespace.QName;

public interface OM {
	final String NS = "http://www.openmath.org/OpenMath";

	final QName OMOBJ = new QName(NS, "OMOBJ");

	final QName OMS = new QName(NS, "OMS");
	final QName OMV = new QName(NS, "OMV");
	final QName OMI = new QName(NS, "OMI");
	final QName OMB = new QName(NS, "OMB");
	final QName OMSTR = new QName(NS, "OMSTR");
	final QName OMF = new QName(NS, "OMF");
	final QName OMA = new QName(NS, "OMA");
	final QName OMBIND = new QName(NS, "OMBIND");
	final QName OME = new QName(NS, "OME");
	final QName OMATTR = new QName(NS, "OMATTR");
	final QName OMR = new QName(NS, "OMR");

	final QName OMATP = new QName(NS, "OMATP");
	final QName OMFOREIGN = new QName(NS, "OMFOREIGN");
	final QName OMBVAR = new QName(NS, "OMBVAR");

	final QName id = new QName("href");
	final QName href = new QName("href");
	final QName name = new QName("name");
	final QName cd = new QName("cd");
	final QName encoding = new QName("encoding");
	final QName cdbase = new QName("cdbase");

	final QName dec = new QName("dec");
	final QName hex = new QName("hex");
}
