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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class OMXmlBuilder extends OMXmlBuilderBase<Node> {
	public static OMXmlBuilder create() {
		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			return new OMXmlBuilder(docBuilder, docBuilder.newDocument());
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public OMXmlBuilder(DocumentBuilder docBuilder) {
		this(docBuilder, docBuilder.newDocument());
	}

	public OMXmlBuilder(DocumentBuilder docBuilder, Document document) {
		super(docBuilder, document);
	}

	@Override
	protected Node build(org.w3c.dom.Node node) {
		return node;
	}
}
