package org.numerateweb.math.xml;

import javax.xml.stream.XMLStreamException;

import org.numerateweb.math.model.Builder;
import org.numerateweb.math.util.stax.ParseException;

public interface IOMXmlParser {
	<T> T parse(Builder<T> builder) throws XMLStreamException,
	ParseException;
}
