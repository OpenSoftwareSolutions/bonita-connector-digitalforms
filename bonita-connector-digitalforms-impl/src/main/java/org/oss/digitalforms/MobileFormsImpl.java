/*******************************************************************************
 * Copyright (c) 2015 Open Software Solutions GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 *
 * Contributors:
 *     Open Software Solutions GmbH
 ******************************************************************************/
package org.oss.digitalforms;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.oss.digitalforms.repository.CloudAccessor;
import org.oss.digitalforms.repository.ftp.FtpRepository;
import org.oss.digitalforms.utils.dom.FormTemplateFiller;
import org.oss.digitalforms.utils.map.CaseInsensitiveMap;
import org.oss.digitalforms.utils.zip.ZipWriter;


/**
 * @author Donat Müller, Magnus Karlsson
 */
public class MobileFormsImpl extends AbstractConnector {

	// input parameters
	private static final String FTP_HOST = "ftpHost";

	private static final String USER = "user";

	private static final String PASSWORD = "password";

	private static final String PARAMETERS = "parameters";

	private static final String FORMTEMPLATEL_DOC = "formName";

	public final static String PROTOCOLKIND = "protocolKind";

	private ParameterData pd = new ParameterData();


	private Logger LOGGER = Logger.getLogger(this.getClass().getName());




	@Override
	public void setInputParameters(Map<String, Object> parameters) {
		for (String key : parameters.keySet()) {

			LOGGER.info("Param: " + key + ", Defaultvalue: " + parameters.get(key));
		}
		super.setInputParameters(parameters);
	}

	@SuppressWarnings("unchecked")
	private void initInputs() {

		pd.ftpHost = (String) getInputParameter(FTP_HOST);
		LOGGER.info(FTP_HOST + " " + pd.ftpHost);

		pd.user = (String) getInputParameter(USER);
		LOGGER.info(USER + " " + pd.user);

		pd.password = (String) getInputParameter(PASSWORD);
		LOGGER.info(PASSWORD + " ******");

		pd.templateDocument = (String) getInputParameter(FORMTEMPLATEL_DOC);
		LOGGER.info(FORMTEMPLATEL_DOC + " " + pd.templateDocument);


		final List<List<Object>> parametersList = (List<List<Object>>) getInputParameter(PARAMETERS);
		pd.parameters = new CaseInsensitiveMap();
		if (parametersList != null) {
			LOGGER.info("initInputs - parameters list :" + parametersList.toString());
			for (List<Object> rows : parametersList) {
				if (rows.size() == 2) {
					Object keyContent = rows.get(0);
					Object valueContent = rows.get(1);
					LOGGER.info("Parameter " + keyContent + " " + valueContent);
					if (keyContent != null && valueContent != null) {
						final String key = keyContent.toString();
						final String value = valueContent.toString();
						pd.parameters.put(key, value);
					}
				}
			}
		}


	}

	@Override
	public void validateInputParameters() throws ConnectorValidationException {
		try {
			initInputs();
			final List<String> errors = new ArrayList<String>();
			if (pd.templateDocument == null || pd.templateDocument.trim().length() == 0) {
				errors.add("templateDocument cannot be empty!");
			}

			Long processInstanceId = getExecutionContext().getProcessInstanceId();
			Long activityInstanceId = getExecutionContext().getActivityInstanceId();
			try {
				Document document = getAPIAccessor().getProcessAPI().getLastDocument(processInstanceId, pd.templateDocument);
				if (!document.hasContent() || !document.getContentFileName().matches(".*\\.xml")) {
					errors.add("the templateDocument " + document.getName() + " has no content " + document.hasContent() + " or file name " + document.getContentFileName() + " isn't compatible with DigitalForms");
				}
				else {
					pd.templateContent = getAPIAccessor().getProcessAPI().getDocumentContent(document.getContentStorageId());
					LOGGER.info(String.format("Validating Processinstance: %s ActivityInstanceId: %s contentStorageId: %s templateContent: %s",  processInstanceId, activityInstanceId, document.getContentStorageId(), pd.templateContent) );
				}
			} catch (Exception e) {
				errors.add(pd.templateDocument + " is not the name of a document defined in the process");
			}

			if (!errors.isEmpty()) {
				throw new ConnectorValidationException(this, errors);
			}

			// Check FTP  Settings
			// Check that templateFile exists
			// Test ftp connection
			if(pd.ftpHost != null && !pd.ftpHost.isEmpty()){
				try {
					ftpValidations(pd.ftpHost, pd.user, pd.password);
				} catch (Exception e) {
					errors.add("unable to connect to ftp" + e.getMessage());
				}
			} else {
				errors.add("ftp host has not been specified.");
			}
			if (!errors.isEmpty()) {
				LOGGER.warning(String.format("Validating Processinstance: %s ActivityInstanceId: %s ERRORS: %s",  processInstanceId, activityInstanceId, errors) );
				throw new ConnectorValidationException(this, errors);
			}
			LOGGER.info(String.format("Validating Processinstance: %s ActivityInstanceId: %s DONE",  processInstanceId, activityInstanceId) );
		} catch (Exception e) {
			LOGGER.warning(e.getMessage());
			e.printStackTrace();
			new ConnectorException(e);
		}

	}

	private void ftpValidations(String ftpHost, String user, String password) throws Exception {
		FTPClient ftp = new FTPClient();
		ftp.connect(ftpHost);
		int reply = ftp.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			throw new Exception("Exception in connecting to FTP Server");
		}
		ftp.login(user, password);
		ftp.listFiles();
		ftp.disconnect();
	}

	@Override
	protected void executeBusinessLogic() throws ConnectorException {
		String activityInstanceId = String.valueOf(this.getExecutionContext().getActivityInstanceId());
		LOGGER.info("Execute Connector Logic for activity: " + activityInstanceId);
		try {
			//			putMobileFormOnFtp(pd.ftpHost, pd.user, pd.password, pd.templateDocument, pd.templateContent, pd.parameters);
			String folderName = extractFolderName(pd.templateContent);
			LOGGER.info("folderName: " + folderName);
			CloudAccessor ca = new FtpRepository(folderName,"zip",pd.ftpHost, pd.user, pd.password);
			LOGGER.info("new Attributes for the form: " + pd.parameters);
			mergeAndUploadForm(ca, pd.templateContent, pd.parameters,activityInstanceId);
			LOGGER.info("Done executing Logic for activity: " + activityInstanceId);
		} catch (final Exception e) {
			LOGGER.info("Error while executing Logic for activity: " + activityInstanceId);
			throw new ConnectorException(e);
		}
	}



	private String extractFolderName(byte[] templateContent) throws Exception {
		FormTemplateFiller filler = new FormTemplateFiller(new ByteArrayInputStream(templateContent));
		return filler.getRootAttributeValue(PROTOCOLKIND);
	}

	private void mergeAndUploadForm(CloudAccessor cloudAccessor,
									byte[] templateContent,
									Map<String, String> formFillingParameters,
									String activityInstanceId) throws Exception {
		FormTemplateFiller filler = new FormTemplateFiller(new ByteArrayInputStream(templateContent));
		formFillingParameters.put("activityInstanceId", activityInstanceId);
		formFillingParameters.put("formCompleted", "true"); // hack to run without ipad
		LOGGER.info("formFillingParameters : " + formFillingParameters);
		filler.fillTemplate(formFillingParameters);
		LOGGER.info("new Parameters in the form for the tablet: " + filler.getDocumentAsXml());
		ZipWriter zipWriter = new ZipWriter();
		byte[] zipContent = zipWriter.getXmlContentAsZipArchive(filler.getDocumentAsXml().getBytes(),activityInstanceId + ".xml");
		cloudAccessor.putInstance(activityInstanceId, new ByteArrayInputStream(zipContent));
	}



	private static class ParameterData {
		// Ftp base configuration
		private String ftpHost;

		private String user;

		private String password;

		// Form settings
		private String templateDocument;

		private Map<String, String> parameters = null;

		private byte[] templateContent;
	}

}
