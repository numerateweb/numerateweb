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

import java.math.BigInteger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

import org.numerateweb.math.model.BuilderUtils;
import org.numerateweb.math.model.LiteralBuilder;
import org.numerateweb.math.ns.INamespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class OMXmlLiteralBuilder implements LiteralBuilder<Node> {
	protected DocumentBuilder docBuilder;
	protected Document document;

	public OMXmlLiteralBuilder(DocumentBuilder docBuilder, Document document) {
		this.docBuilder = docBuilder;
		this.document = document;
	}

	protected void set(Element element, QName attribute, String value) {
		element.setAttributeNS(attribute.getNamespaceURI(),
				attribute.getLocalPart(), value);
	}

	public Element create(QName name) {
		return document.createElementNS(name.getNamespaceURI(),
				name.getLocalPart());
	}

	@Override
	public Node s(URI symbol) {
		Element oms = create(OM.OMS);
		set(oms, OM.cdbase, symbol.trimFragment().trimSegments(1).toString());
		set(oms, OM.cd, symbol.lastSegment());
		set(oms, OM.name, symbol.fragment());
		return oms;
	}

	@Override
	public Node var(String variableName) {
		Element omv = create(OM.OMV);
		set(omv, OM.name, variableName);
		return omv;
	}

	@Override
	public Node i(BigInteger value) {
		Element omi = create(OM.OMI);
		omi.setTextContent(String.valueOf(value));
		return omi;
	}

	@Override
	public Node b(String base64Binary) {
		Element omb = create(OM.OMB);
		omb.setTextContent(base64Binary);
		return omb;
	}

	@Override
	public Node str(String value) {
		Element omstr = create(OM.OMSTR);
		omstr.setTextContent(value);
		return omstr;
	}

	@Override
	public Node f(double value) {
		Element omf = create(OM.OMF);
		set(omf, OM.dec, String.valueOf(value));
		return omf;
	}

	@Override
	public Node ref(IReference reference) {
		Element omr = create(OM.OMR);
		set(omr, OM.href, reference.toString());
		return omr;
	}

	protected Node foreign(String encoding, Node content) {
		Element omforeign = create(OM.OMFOREIGN);
		set(omforeign, OM.encoding, encoding);
		if (content != null) {
			omforeign.appendChild(content);
		}
		return omforeign;
	}

	@Override
	public Node foreign(String encoding, String content) {
		Node contentNode = null;
		try {
			contentNode = docBuilder.parse(content);
		} catch (Exception e) {
			// ignore
		}
		return foreign(encoding, contentNode);
	}

	@Override
	public Node rdfClass(IReference reference, INamespaces ns) {
		return str(BuilderUtils.classAsString(reference, ns));
	}

	@Override
	public LiteralBuilder<Node> id(URI id) {
		// ignore IDs
		return this;
	}
	
}
