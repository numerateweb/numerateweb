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
