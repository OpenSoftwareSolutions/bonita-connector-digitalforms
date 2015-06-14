/*******************************************************************************
 * Copyright (c) 2015 Open Software Solutions GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 *
 * Contributors:
 *     Open Software Solutions GmbH
 ******************************************************************************/
package org.oss.digitalforms.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.impl.DocumentImpl;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.io.IOUtil;
import org.junit.Before;
import org.junit.Test;
import org.oss.digitalforms.MobileFormsImpl;


public class MobileFormsTest {

	// input parameters
	private static final String FTP_HOST = "ftpHost";

	private static final String USER = "user";

	private static final String PASSWORD = "password";

	private static final String PARAMETERS = "parameters";

	private static final String FORMTEMPLATEL_DOC = "formName";

	private static final String WRONG_XML_DOC = "wrongXml";

	private static final String WRONG_USERNAME = "wrong_user_name";

	private static final String WRONG_PASSWORD = "wrong_password";

	EngineExecutionContext engineExecutionContext;

	APIAccessor apiAccessor;

	ProcessAPI processAPI;

	@Before
	public void setUp() throws Exception {
		final File root = new File(".");
    	final File file = new File(root, "src/test/resources/digireport-bonita-poc-part1-v0-instance.xml");
    	byte[] fileContent = IOUtil.getAllContentFrom(file);

    	DocumentImpl document = new DocumentImpl();
    	document.setCreationDate(new Date());
    	document.setId(1);
    	document.setProcessInstanceId(1);
    	document.setName("xml");
    	document.setFileName("digireport-bonita-poc-part1-v0-instance.xml");
    	document.setContentMimeType("application/xml");
    	document.setContentStorageId("1L");
    	document.setHasContent(true);

    	engineExecutionContext = mock(EngineExecutionContext.class);
    	apiAccessor = mock(APIAccessor.class);
    	processAPI = mock(ProcessAPI.class);
    	when(apiAccessor.getProcessAPI()).thenReturn(processAPI);
    	when(engineExecutionContext.getProcessInstanceId()).thenReturn(1L);
    	when(processAPI.getLastDocument(1L, "xml")).thenReturn(document);
    	when(processAPI.getLastDocument(1L, WRONG_XML_DOC)).thenThrow(
    			new DocumentNotFoundException(new Throwable("Document not found : " + WRONG_XML_DOC)));
    	when(processAPI.getDocumentContent("1L")).thenReturn(fileContent);
	}

	@Test
	public void testGoodParameters() throws Exception {
	    final Connector connector = getWorkingConnector();
	    connector.validateInputParameters();
	}

	@Test
	public void testExecute() throws Exception {
	  final Connector connector = getWorkingConnector();
	  connector.validateInputParameters();
	  connector.execute();
	}


	@Test(expected = ConnectorValidationException.class)
	public void testWrongDigitalFormsDocument() throws Exception {
		final Connector connector = getWorkingConnector();
		final String wrongJrxmlDoc = WRONG_XML_DOC;
		final Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put(FORMTEMPLATEL_DOC, wrongJrxmlDoc);
		connector.setInputParameters(inputs);
		connector.validateInputParameters();
	}

    @Test(expected = ConnectorValidationException.class)
    public void testNullParameter() throws Exception {
    	final Connector connector = getWorkingConnector();
    	final String wrongFtpHost = null;
    	final Map<String, Object> inputs = new HashMap<String, Object>();
    	inputs.put(FTP_HOST, wrongFtpHost);
    	connector.setInputParameters(inputs);
    	connector.validateInputParameters();
    }


    @Test(expected = ConnectorValidationException.class)
    public void testWrongFtpUser() throws Exception {
    	final Connector connector = getWorkingConnector();
    	connector.validateInputParameters();
    	final String wrongUserName = WRONG_USERNAME;
    	final Map<String, Object> inputs = new HashMap<String, Object>();
    	inputs.put(USER, wrongUserName);
    	connector.setInputParameters(inputs);
    	connector.validateInputParameters();
    }


    @Test(expected = ConnectorValidationException.class)
    public void testWrongFtpPassword() throws Exception {
    	final Connector connector = getWorkingConnector();
    	final String wrongPassword = WRONG_PASSWORD;
    	final Map<String, Object> inputs = new HashMap<String, Object>();
    	inputs.put(PASSWORD, wrongPassword);
    	connector.setInputParameters(inputs);
    	connector.validateInputParameters();
    }

    private MobileFormsImpl getWorkingConnector() throws Exception {

    	final MobileFormsImpl connector = new MobileFormsImpl();
    	final Map<String, Object> inputs = new HashMap<String, Object>();

    	// ftp access
    	inputs.put(FTP_HOST, "ftp.digireporting.com");
    	inputs.put(USER, "forms@digireporting.com");
    	inputs.put(PASSWORD, "SuperDemo123");

    	// DigitalForms settings parameters
    	inputs.put(FORMTEMPLATEL_DOC, "xml");
    	final List<List<String>> parametersList = new ArrayList<List<String>>();
    	final List<String> parameter2List = new ArrayList<String>();
    	parameter2List.add("quelle");
    	parameter2List.add("direkt");
    	parameter2List.add("name");
    	parameter2List.add("Karlsson");
    	parameter2List.add("vorname");
    	parameter2List.add("Magnus");
    	parametersList.add(parameter2List);
    	inputs.put(PARAMETERS, parametersList);

    	connector.setExecutionContext(engineExecutionContext);
    	connector.setAPIAccessor(apiAccessor);
    	connector.setInputParameters(inputs);

    	return connector;
    }

}
