package org.numerateweb.math.util.stax;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * This is an abstract implementation for a StAX-based XML transformer.
 */
public abstract class AbstractXMLTransformer extends AbstractXMLParser {
	protected XMLStreamWriter writer;

	public AbstractXMLTransformer(XMLStreamReader reader, XMLStreamWriter writer) {
		super(reader);
		this.writer = writer;
	}

	protected void writeElement() throws XMLStreamException {
		switch (reader.getEventType()) {
		case XMLStreamConstants.START_ELEMENT:
			writer.writeStartElement(reader.getPrefix(), reader.getLocalName(),
					reader.getNamespaceURI());
			break;
		case XMLStreamConstants.END_ELEMENT:
			writer.writeEndElement();
			break;
		case XMLStreamConstants.COMMENT:
			writer.writeComment(reader.getText());
			break;
		case XMLStreamConstants.CDATA:
			writer.writeCData(reader.getText());
			break;
		case XMLStreamConstants.CHARACTERS:
			writer.writeCharacters(reader.getText());
			break;
		case XMLStreamConstants.SPACE:
			break;
		case XMLStreamConstants.END_DOCUMENT:
			break;
		case XMLStreamConstants.ENTITY_REFERENCE:
			break;
		}

		writer.flush();
	}
}
