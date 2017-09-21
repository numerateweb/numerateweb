package org.numerateweb.math.om.cd;

import static org.numerateweb.math.om.cd.OMCD.CD;
import static org.numerateweb.math.om.cd.OMCD.CDBASE;
import static org.numerateweb.math.om.cd.OMCD.CDCOMMENT;
import static org.numerateweb.math.om.cd.OMCD.CDDATE;
import static org.numerateweb.math.om.cd.OMCD.CDDEFINITION;
import static org.numerateweb.math.om.cd.OMCD.CDNAME;
import static org.numerateweb.math.om.cd.OMCD.CDREVIEWDATE;
import static org.numerateweb.math.om.cd.OMCD.CDREVISION;
import static org.numerateweb.math.om.cd.OMCD.CDSTATUS;
import static org.numerateweb.math.om.cd.OMCD.CDURL;
import static org.numerateweb.math.om.cd.OMCD.CDUSES;
import static org.numerateweb.math.om.cd.OMCD.CDVERSION;
import static org.numerateweb.math.om.cd.OMCD.CMP;
import static org.numerateweb.math.om.cd.OMCD.DESCRIPTION;
import static org.numerateweb.math.om.cd.OMCD.EXAMPLE;
import static org.numerateweb.math.om.cd.OMCD.FMP;
import static org.numerateweb.math.om.cd.OMCD.NAME;
import static org.numerateweb.math.om.cd.OMCD.ROLE;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;

import org.numerateweb.math.model.Builder;
import org.numerateweb.math.model.Builder.SeqBuilder;
import org.numerateweb.math.util.stax.AbstractXMLParser;
import org.numerateweb.math.util.stax.ParseException;
import org.numerateweb.math.xml.IOMXmlParser;
import org.numerateweb.math.xml.OM;
import org.numerateweb.math.xml.OMXmlParser;

public class OMCDParser extends AbstractXMLParser implements IOMXmlParser {
	protected URI base;

	public OMCDParser(XMLStreamReader reader) {
		this(reader, null);
	}

	public OMCDParser(XMLStreamReader reader, URI base) {
		super(reader);
		this.base = base;
	}

	public <T> T parse(Builder<T> builder) throws XMLStreamException,
			ParseException {
		nextOrFail(CD);
		T result = parseCD(builder);
		end();
		return result;
	}

	protected <T> T createFromString(QName tagName, Builder<T> builder)
			throws XMLStreamException, ParseException {
		return createFromString(tagName, parseStringValue(), builder);
	}

	protected <T> T createFromString(QName tagName, String value,
			Builder<T> builder) throws XMLStreamException, ParseException {
		return builder.apply()
				.s(META.NS.appendLocalPart(tagName.getLocalPart())).str(value)
				.end();
	}

	protected <T> T parseCD(Builder<T> builder) throws XMLStreamException,
			ParseException {
		String cdBase = "http://www.openmath.org/cd", cdName = "";
		SeqBuilder<T> cdBuilder = builder.apply().s(META.CD);

		// (CDComment* & Description? & CDName & CDURL? & CDBase? &
		// CDReviewDate? & CDDate & CDStatus & CDUses? & CDVersion & CDRevision)
		Set<QName> requiredElements = new HashSet<QName>(Arrays.asList(CDNAME,
				CDDATE, CDSTATUS, CDVERSION, CDREVISION));
		Set<QName> allowedElements = new HashSet<QName>(Arrays.asList(CDBASE,
				CDCOMMENT, CDDATE, CDNAME, CDREVIEWDATE, CDREVISION, CDSTATUS,
				CDURL, CDUSES, CDVERSION, DESCRIPTION));
		do {
			QName tagName = next(allowedElements
					.toArray(new QName[allowedElements.size()]));
			if (tagName == null) {
				break;
			}
			requiredElements.remove(tagName);
			if (!CDCOMMENT.equals(tagName)) {
				allowedElements.remove(tagName);
			}
			if (CDUSES.equals(tagName)) {
				parseCDUses(cdBuilder);
			} else if (CDBASE.equals(tagName)) {
				createFromString(tagName, cdBase = parseStringValue(),
						cdBuilder);
			} else if (CDNAME.equals(tagName)) {
				createFromString(tagName, cdName = parseStringValue(),
						cdBuilder);
			} else {
				createFromString(tagName, cdBuilder);
			}
			end();
		} while (reader.hasNext());

		if (!requiredElements.isEmpty()) {
			expected(requiredElements
					.toArray(new QName[requiredElements.size()]));
		}

		// initialize base with correct value
		if (base == null) {
			base = URIs.createURI(cdBase).appendSegment(cdName);
		}

		// (CDDefinition, CDComment*)+
		requiredElements.clear();
		requiredElements.add(CDDEFINITION);
		QName tagName;
		while ((tagName = next(CDDEFINITION, CDCOMMENT)) != null) {
			if (CDCOMMENT.equals(tagName)) {
				createFromString(tagName, cdBuilder);
			} else {
				parseCDDefinition(cdBuilder);
			}
			end();
		}
		return cdBuilder.end();
	}

	protected <T> T parseCDUses(Builder<T> builder) throws XMLStreamException,
			ParseException {
		SeqBuilder<T> cdUsesBuilder = builder.apply().s(META.CDUSES);
		while (next(CDNAME) != null) {
			createFromString(CDNAME, cdUsesBuilder);
			end();
		}
		return cdUsesBuilder.end();
	}

	protected <T> T parseCDDefinition(Builder<T> builder)
			throws XMLStreamException, ParseException {
		SeqBuilder<T> cdDefinitionBuilder = builder.apply()
				.s(META.CDDEFINITION);

		// CDComment*
		while (next(CDCOMMENT) != null) {
			createFromString(CDCOMMENT, cdDefinitionBuilder);
			end();
		}

		// (Name & Role? & Description)
		Set<QName> requiredElements = new HashSet<QName>(Arrays.asList(NAME,
				DESCRIPTION));
		Set<QName> allowedElements = new HashSet<QName>(Arrays.asList(NAME,
				ROLE, DESCRIPTION));
		QName tagName;
		while ((tagName = next(allowedElements
				.toArray(new QName[allowedElements.size()]))) != null) {
			requiredElements.remove(tagName);
			allowedElements.remove(tagName);
			createFromString(tagName, cdDefinitionBuilder);
			end();
		}

		if (!requiredElements.isEmpty()) {
			expected(requiredElements
					.toArray(new QName[requiredElements.size()]));
		}

		// (CDComment | Example | FMP | CMP)*
		while ((tagName = next(CDCOMMENT, EXAMPLE, FMP, CMP)) != null) {
			if (CDCOMMENT.equals(tagName) || CMP.equals(tagName)) {
				createFromString(tagName, cdDefinitionBuilder);
			} else if (EXAMPLE.equals(tagName)) {
				parseExample(cdDefinitionBuilder);
			} else if (FMP.equals(tagName)) {
				parseFMP(cdDefinitionBuilder);
			}
			end();
		}
		return cdDefinitionBuilder.end();
	}

	protected <T> T parseExample(Builder<T> builder) throws XMLStreamException,
			ParseException {
		SeqBuilder<T> exampleBuilder = builder.apply().s(
				META.NS.appendLocalPart(EXAMPLE.getLocalPart()));
		// (text | OMOBJ)*
		reader.next();
		while (reader.hasNext()) {
			if (reader.isCharacters()) {
				String text = parseStringValue();
				if (!text.isEmpty()) {
					exampleBuilder.str(text);
				}
			} else if (reader.isStartElement()) {
				if (OM.OMOBJ.equals(reader.getName())) {
					// an OpenMath object
					start(reader.getName());
					new OMXmlParser(reader, base).parseOMOBJ(exampleBuilder);
					end();
				} else {
					throw newError("Unexpected element.");
				}
			} else if (reader.isEndElement()) {
				break;
			} else {
				// skip comments etc.
				reader.next();
			}
		}
		return exampleBuilder.end();
	}

	// attribute kind {xsd:string}?, OMOBJ
	protected <T> T parseFMP(Builder<T> builder) throws XMLStreamException,
			ParseException {
		SeqBuilder<T> fmpBuilder = builder.apply().s(
				META.NS.appendLocalPart(FMP.getLocalPart()));
		return new OMXmlParser(reader, base).parse(fmpBuilder).end();
	}
}