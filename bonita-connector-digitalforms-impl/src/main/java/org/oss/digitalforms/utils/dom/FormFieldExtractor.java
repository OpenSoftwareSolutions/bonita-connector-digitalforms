/*******************************************************************************
 * Copyright (c) 2015 Open Software Solutions GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 *
 * Contributors:
 *     Open Software Solutions GmbH
 ******************************************************************************/
package org.oss.digitalforms.utils.dom;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Magnus Karlsson
 *
 */
public class FormFieldExtractor {
	private final Document inputForm;

	public FormFieldExtractor(File inputFormFile) throws FileNotFoundException, Exception {
		this(new FileInputStream(inputFormFile));
	}
	public FormFieldExtractor(InputStream inputFormStream) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		this.inputForm = dBuilder.parse(inputFormStream);
		inputFormStream.close();
	}

	public FormFieldExtractor(Document inputForm) {
		this.inputForm = inputForm;
	}

	public String getRootAttributeValue(String attributeName) {
		return inputForm.getDocumentElement().getAttribute(attributeName);
	}

	public Document getDocument() {
		return inputForm;
	}

	public String getDocumentAsXml() throws Exception {
		return DocumentMarshaller.toXml(getDocument());
	}


	public InputStream getDocumentAsInputStream() throws Exception {
		return new ByteArrayInputStream(getDocumentAsXml().getBytes());
	}


	public Map<String,String> extractFields() throws Exception {
		inputForm.getDocumentElement().normalize();
		NodeList nList = inputForm.getChildNodes();
		return extractFields(nList);
	}

	private Map<String,String>  extractFields(NodeList nList) {
		Map<String,String> result = new HashMap<String,String>();
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node node = nList.item(temp);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element element = (Element) node;
				if (element.hasAttribute("xsi:type") && element.getAttribute("xsi:type").equals("AttributeValue")) {
					result.put(element.getAttribute("name"), element.getAttribute("value"));
				}
				if (node.hasChildNodes()) {
					result.putAll(extractFields(node.getChildNodes()));
				}

			}
		}
		return result;
	}


}
