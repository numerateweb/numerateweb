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
