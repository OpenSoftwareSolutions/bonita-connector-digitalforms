/*******************************************************************************
 * Copyright (c) 2015 Open Software Solutions GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 *
 * Contributors:
 *     Open Software Solutions GmbH
 ******************************************************************************/
package org.oss.digitalforms.repository.ftp;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.oss.digitalforms.utils.dom.DocumentMarshaller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Reads and writes DomainsXML files and holds contents in Memory.
 * @author Donat Müller, Magnus Karlsson
 *
 */
public class DomainsRegistry {
	private static final String PROTOCOL_INSTANCE = "protocolInstance";
	private static final String TITLE = "title";
	private static final String DOMAIN_ID = "domainId";
	private static final String DOMAIN = "domain";
	private static final String PROTOCOL_KIND = "protocolKind";

	private final Map<String,String> entries;
	private final String protocolKind;

	public DomainsRegistry(String protocolKind) {
		this.entries = new HashMap<String, String>();
		this.protocolKind = protocolKind;
	}

	public void addEntry(String domainId, String title) {
		entries.put(domainId, title);
	}

	public Set<String> domainIds() {
		return entries.keySet();
	}

	public String getTitle(String domainId) {
		return entries.get(domainId);
	}

	public void removeDomain(String domainId) {
		entries.remove(domainId);
	}
	public String getProtocolKind() {
		return protocolKind;
	}

	public String toXML() throws Exception {
		return DocumentMarshaller.toXml(toDocument());
	}

	private Document toDocument() throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.newDocument();
		Element root = doc.createElement(PROTOCOL_INSTANCE);
		doc.appendChild(root);
		root.setAttribute(PROTOCOL_KIND, getProtocolKind());
		for (Map.Entry<String, String> entry : entries.entrySet()) {
			Element domain = doc.createElement(DOMAIN);
			domain.setAttribute(DOMAIN_ID, entry.getKey());
			domain.setAttribute(TITLE, entry.getValue());
			root.appendChild(domain);
		}
		return doc;
	}



	public static DomainsRegistry fromFile(File domainsXmlFile) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(domainsXmlFile);
		Element root = doc.getDocumentElement();
		String protocolKind = root.getAttribute(PROTOCOL_KIND);
		DomainsRegistry registry = new DomainsRegistry(protocolKind);
		NodeList domains = root.getElementsByTagName(DOMAIN);
		for (int i=0; i<domains.getLength();i++) {
			Element domain = (Element) domains.item(i);
			registry.addEntry(domain.getAttribute(DOMAIN_ID), domain.getAttribute(TITLE));
		}
		return registry;
	}
}
