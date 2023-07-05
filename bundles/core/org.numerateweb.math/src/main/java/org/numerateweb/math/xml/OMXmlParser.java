/*
 * Copyright (c) 2023 Numerate Web contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.numerateweb.math.xml;

import static org.numerateweb.math.xml.OM.OMA;
import static org.numerateweb.math.xml.OM.OMATP;
import static org.numerateweb.math.xml.OM.OMATTR;
import static org.numerateweb.math.xml.OM.OMB;
import static org.numerateweb.math.xml.OM.OMBIND;
import static org.numerateweb.math.xml.OM.OMBVAR;
import static org.numerateweb.math.xml.OM.OME;
import static org.numerateweb.math.xml.OM.OMF;
import static org.numerateweb.math.xml.OM.OMFOREIGN;
import static org.numerateweb.math.xml.OM.OMI;
import static org.numerateweb.math.xml.OM.OMOBJ;
import static org.numerateweb.math.xml.OM.OMR;
import static org.numerateweb.math.xml.OM.OMS;
import static org.numerateweb.math.xml.OM.OMSTR;
import static org.numerateweb.math.xml.OM.OMV;
import static org.numerateweb.math.xml.OM.cd;
import static org.numerateweb.math.xml.OM.cdbase;
import static org.numerateweb.math.xml.OM.dec;
import static org.numerateweb.math.xml.OM.encoding;
import static org.numerateweb.math.xml.OM.hex;
import static org.numerateweb.math.xml.OM.href;
import static org.numerateweb.math.xml.OM.id;
import static org.numerateweb.math.xml.OM.name;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.util.StreamReaderDelegate;

import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;

import org.numerateweb.math.model.Builder;
import org.numerateweb.math.model.Builder.BindingBuilder;
import org.numerateweb.math.model.Builder.SeqBuilder;
import org.numerateweb.math.model.Builder.VariablesBuilder;
import org.numerateweb.math.model.OMObjectBuilder;
import org.numerateweb.math.util.stax.AbstractXMLParser;
import org.numerateweb.math.util.stax.ParseException;

public class OMXmlParser extends AbstractXMLParser implements IOMXmlParser {
	protected final static QName[] OM_ELEMENTS = { OMS, OMV, OMI, OMB, OMSTR,
			OMF, OMA, OMBIND, OME, OMATTR, OMR };

	protected final static List<QName> CDBASE_ELEMENTS = Arrays.asList(OMS,
			OMOBJ, OMA, OMBIND, OMATTR, OMATP, OMFOREIGN);

	protected URI base;

	private Stack<String> cdbaseStack = new Stack<String>();

	private XMLOutputFactory xmlOutputFactory;

	private static class Reader extends StreamReaderDelegate {
		private XMLStreamWriter writer;
		private int openTags;

		public Reader(XMLStreamReader reader) {
			super(reader);
		}

		@Override
		public int next() throws XMLStreamException {
			int next = super.next();
			write(next);
			return next;
		}

		void write(int eventType) throws XMLStreamException {
			if (writer != null) {
				switch (eventType) {
				case START_ELEMENT:
					openTags++;
					writer.writeStartElement(getPrefix(), getLocalName(),
							getNamespaceURI());
					for (int i = 0, count = getAttributeCount(); i < count; i++) {
						String ns = getAttributeNamespace(i);
						if (ns == null) {
							writer.writeAttribute(getAttributeLocalName(i),
									getAttributeValue(i));
						} else {
							writer.writeAttribute(getAttributePrefix(i),
									getAttributeNamespace(i),
									getAttributeLocalName(i),
									getAttributeValue(i));
						}
					}
					break;
				case CHARACTERS:
					writer.writeCharacters(getText());
					break;
				case END_ELEMENT:
					if (openTags > 0) {
						writer.writeEndElement();
					}
					openTags--;
					if (openTags == 0) {
						writer.flush();
						writer = null;
					}
					break;
				}
			}
		}

		void setWriter(XMLStreamWriter writer) throws XMLStreamException {
			this.writer = writer;
			this.openTags = 0;
		}

		XMLStreamWriter getWriter() {
			return writer;
		}
	}

	public OMXmlParser(XMLStreamReader reader) {
		this(reader, URIs.createURI("urn:omxml"));
	}

	public OMXmlParser(XMLStreamReader reader, URI base) {
		super(new Reader(reader));
		this.base = base;
		cdbaseStack.push("http://www.openmath.org/cd");
	}

	Reader getReader() {
		return (Reader) reader;
	}

	@Override
	protected void end() throws XMLStreamException, ParseException {
		super.end();
		cdbaseStack.pop();
	}

	@Override
	protected QName next(QName... names) throws XMLStreamException,
			ParseException {
		QName next = super.next(names);
		if (next != null) {
			String cdbaseValue = cdbaseStack.peek();
			if (CDBASE_ELEMENTS.contains(next)) {
				String newCdbaseValue = getAttribute(cdbase);
				if (newCdbaseValue != null
						&& newCdbaseValue.trim().length() > 0) {
					cdbaseValue = newCdbaseValue;
				}
			}
			cdbaseStack.push(cdbaseValue);
		}
		return next;
	}

	public <T> T parse(Builder<T> builder) throws XMLStreamException,
			ParseException {
		nextOrFail(OMOBJ);
		T result = parseOMOBJ(builder);
		end();
		return result;
	}

	public <T> T parseOMOBJ(Builder<T> builder) throws XMLStreamException,
			ParseException {
		nextOrFail(OM_ELEMENTS);
		T result = parseOMElement(builder);
		end();

		return result;
	}

	protected <T> T parseOMElement(Builder<T> builder)
			throws XMLStreamException, ParseException {
		String idValue = getAttribute(id);
		if (idValue != null) {
			builder = builder.id(base.appendLocalPart(idValue));
		}
		T result;
		QName tagName = reader.getName();
		if (OMS.equals(tagName)) {
			result = builder.s(parseOMS());
		} else if (OMV.equals(tagName)) {
			result = builder.var(parseOMV());
		} else if (OMI.equals(tagName)) {
			result = parseOMI(builder);
		} else if (OMB.equals(tagName)) {
			result = parseOMB(builder);
		} else if (OMSTR.equals(tagName)) {
			result = parseOMSTR(builder);
		} else if (OMF.equals(tagName)) {
			result = parseOMF(builder);
		} else if (OMA.equals(tagName)) {
			result = parseOMA(builder);
		} else if (OMBIND.equals(tagName)) {
			result = parseOMBIND(builder);
		} else if (OME.equals(tagName)) {
			result = parseOME(builder);
		} else if (OMATTR.equals(tagName)) {
			result = parseOMATTR(builder);
		} else { // if (OMR.equals(tagName))
			result = parseOMR(builder);
		}
		return result;
	}

	protected <T> T parseOMR(Builder<T> builder) throws XMLStreamException,
			ParseException {
		URI hrefValue = URIs.createURI(getAttribute(href));
		if (hrefValue.isRelative()) {
			hrefValue = hrefValue.resolve(base);
		}
		return builder.ref(hrefValue);
	}

	protected <T> T parseOMATTR(Builder<T> builder) throws XMLStreamException,
			ParseException {
		final Builder<T> builderFinal = builder;
		nextOrFail(OMATP);
		builder = parseOMATP(new AttributeFactory<Builder<T>>() {
			@Override
			public Builder<Builder<T>> attr(URI symbol) {
				return builderFinal.attr(symbol);
			}
		}, builder);
		end();

		nextOrFail(OM_ELEMENTS);
		T target = parseOMElement(builder);
		end();

		return target;
	}

	interface AttributeFactory<T> {
		Builder<T> attr(URI symbol);
	}

	protected <T> T parseOMATP(AttributeFactory<T> attributeFactory, T dflt)
			throws XMLStreamException, ParseException {
		int i = 0;
		T result = dflt;
		while (next(OMS) != null) {
			Builder<T> valueBuilder = attributeFactory.attr(parseOMS());
			end();

			nextOrFail(combine(OM_ELEMENTS, OMFOREIGN));
			if (OMFOREIGN.equals(reader.getName())) {
				result = parseOMFOREIGN(valueBuilder);
			} else {
				result = parseOMElement(valueBuilder);
			}
			end();
			i++;
		}
		if (i == 0) {
			expected(OMS);
		}
		return result;
	}

	protected <T> T parseOMFOREIGN(Builder<T> builder)
			throws XMLStreamException, ParseException {
		StringWriter contents = null;
		XMLStreamWriter writer = null;
		if (getReader().getWriter() == null) {
			contents = new StringWriter();
			if (xmlOutputFactory == null) {
				xmlOutputFactory = XMLOutputFactory.newInstance();
			}
			writer = xmlOutputFactory.createXMLStreamWriter(contents);
			getReader().setWriter(writer);
		}

		String encodingValue = getAttribute(encoding);
		while (parseOMorNotOM())
			;

		// content of nested foreign elements is removed for simplicity
		T foreign = builder.foreign(encodingValue, contents == null ? ""
				: contents.toString());

		return foreign;
	}

	protected boolean parseOMorNotOM() throws XMLStreamException,
			ParseException {
		if (next(OM_ELEMENTS) != null) {
			// an expected OpenMATH object
			parseOMElement(new OMObjectBuilder());
			end();
		} else if (reader.isStartElement()) {
			if (OM.NS.equals(reader.getName().getNamespaceURI())) {
				unexpected(reader.getName());
			}
			reader.next();

			// an unknown start element
			while (parseOMorNotOM())
				;

			if (!reader.isEndElement()) {
				unexpected(reader.getName());
			}
			reader.next();
		} else if (reader.isEndElement()) {
			return false;
		} else {
			reader.next();
		}
		return true;
	}

	protected <T> T parseOME(Builder<T> builder) throws XMLStreamException,
			ParseException {
		nextOrFail(OMS);
		SeqBuilder<T> errorBuilder = builder.error(parseOMS());
		end();

		QName[] elementOrForeign = combine(OM_ELEMENTS, OMFOREIGN);
		while (next(elementOrForeign) != null) {
			if (OMFOREIGN.equals(reader.getName())) {
				parseOMFOREIGN(errorBuilder);
			} else {
				parseOMElement(errorBuilder);
			}
			end();
		}
		return errorBuilder.end();
	}

	protected <T> T parseOMBIND(Builder<T> builder) throws XMLStreamException,
			ParseException {
		BindingBuilder<T> bindBuilder = builder.bind();

		// operator
		nextOrFail(OM_ELEMENTS);
		parseOMElement(bindBuilder.binder());
		end();

		// variable
		nextOrFail(OMBVAR);
		parseOMBVAR(bindBuilder.variables());
		end();

		// expression
		nextOrFail(OM_ELEMENTS);
		parseOMElement(bindBuilder.body());
		end();

		return bindBuilder.end();
	}

	protected void parseOMBVAR(VariablesBuilder<?> varBuilder)
			throws XMLStreamException, ParseException {
		int i = 0;
		while (next(OMV, OMATTR) != null) {
			if (OMV.equals(reader.getName())) {
				varBuilder.var(parseOMV());
			} else { // if OMATTR.equals(reader.getName())
				parseAttrvar(varBuilder);
			}
			end();

			i++;
		}
		if (i == 0) {
			expected(OMV, OMATTR);
		}
	}

	protected <T> void parseAttrvar(VariablesBuilder<T> varBuilder)
			throws XMLStreamException, ParseException {
		final VariablesBuilder<T> varBuilderFinal = varBuilder;
		nextOrFail(OMATP);
		varBuilder = parseOMATP(new AttributeFactory<VariablesBuilder<T>>() {
			@Override
			public Builder<VariablesBuilder<T>> attr(URI symbol) {
				return varBuilderFinal.attrVar(symbol);
			}
		}, varBuilder);
		end();

		nextOrFail(OMV, OMATTR);
		if (OMV.equals(reader.getName())) {
			varBuilder.var(parseOMV());
		} else { // if OMATTR.equals(reader.getName())
			parseAttrvar(varBuilder);
		}
		end();
	}

	protected <T> T parseOMA(Builder<T> builder) throws XMLStreamException,
			ParseException {
		SeqBuilder<T> application = builder.apply();
		int i = 0;
		while (next(OM_ELEMENTS) != null) {
			parseOMElement(application);
			end();
			i++;
		}
		if (i == 0) {
			expected(OM_ELEMENTS);
		}
		return application.end();
	}

	protected <T> T parseOMSTR(Builder<T> builder) throws XMLStreamException,
			ParseException {
		reader.next();
		return builder.str(parseStringValue());
	}

	protected <T> T parseOMF(Builder<T> builder) throws XMLStreamException,
			ParseException {
		String decValue = getAttribute(dec);
		Double value = null;
		if (decValue != null) {
			value = Double.parseDouble(decValue);
		} else {
			String hexValue = getAttribute(hex);
			if (hexValue != null) {
				value = Double.longBitsToDouble(Long.parseLong(hexValue, 16));
			}
		}

		if (value == null) {
			throw newError("Expected double value.");
		}
		return builder.f(value);
	}

	protected <T> T parseOMB(Builder<T> builder) throws XMLStreamException,
			ParseException {
		reader.next();
		String base64BinaryValue = parseStringValue();
		return builder.b(base64BinaryValue);
	}

	protected <T> T parseOMI(Builder<T> builder) throws XMLStreamException,
			ParseException {
		reader.next();
		return builder.i(new BigInteger(parseStringValue()));
	}

	protected String parseOMV() throws XMLStreamException, ParseException {
		return getAttribute(name);
	}

	protected URI parseOMS() throws XMLStreamException, ParseException {
		String cdValue = getAttribute(cd);
		required(cd, cdValue);

		String nameValue = getAttribute(name);
		required(name, nameValue);

		StringBuilder absoluteCd = new StringBuilder(cdbaseStack.peek());
		if (absoluteCd.charAt(absoluteCd.length() - 1) != '/') {
			absoluteCd.append('/');
		}
		absoluteCd.append(cdValue);
		return URIs.createURI(absoluteCd.toString() + '#' + nameValue);
	}
}
