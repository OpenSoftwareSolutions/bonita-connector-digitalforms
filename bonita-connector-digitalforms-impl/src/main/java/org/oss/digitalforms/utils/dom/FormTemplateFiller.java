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
public class FormTemplateFiller {
	private final Document template;

	public FormTemplateFiller(File templateFile) throws FileNotFoundException, Exception {
		this(new FileInputStream(templateFile));
	}
	public FormTemplateFiller(InputStream templateStream) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		this.template = dBuilder.parse(templateStream);
		templateStream.close();
	}

	public FormTemplateFiller(Document template) {
		this.template = template;
	}

	public String getRootAttributeValue(String attributeName) {
		return template.getDocumentElement().getAttribute(attributeName);
	}

	public Document getDocument() {
		return template;
	}

	public String getDocumentAsXml() throws Exception {
		return DocumentMarshaller.toXml(getDocument());
	}


	public InputStream getDocumentAsInputStream() throws Exception {
		return new ByteArrayInputStream(getDocumentAsXml().getBytes());
	}

	public void fillTemplate(Map<String, String> parameter) throws Exception {
		template.getDocumentElement().normalize();
		NodeList nList = template.getChildNodes();
		modifyChildNodes(nList, parameter);
	}

	private  void modifyChildNodes(NodeList nList, Map<String, String> parameter) {
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node node = nList.item(temp);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element element = (Element) node;
				String attributeName = element.getAttribute("name");
				if (parameter.containsKey(attributeName)) {
					element.setAttribute("value", parameter.get(attributeName));
				}
				if (node.hasChildNodes()) {
					modifyChildNodes(node.getChildNodes(), parameter);
				}
			}
		}
	}

}
