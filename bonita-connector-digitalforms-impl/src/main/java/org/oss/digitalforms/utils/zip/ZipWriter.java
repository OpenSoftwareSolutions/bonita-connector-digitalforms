/*******************************************************************************
 * Copyright (c) 2015 Open Software Solutions GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 *
 * Contributors:
 *     Open Software Solutions GmbH
 ******************************************************************************/
package org.oss.digitalforms.utils.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Magnus Karlsson, Donat Müller
 *
 */
public class ZipWriter {

	public String getFileNameFromNow(String suffix) {
		return getFoldereNameFromNow() + "." + suffix;
	}

	public String getFoldereNameFromNow() {
		SimpleDateFormat dateformatJava = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-S");
		return dateformatJava.format(System.currentTimeMillis());
	}
	public byte[] getXmlContentAsZipArchive(byte[] xmlContent) throws IOException {
		String xmlFileName = getFileNameFromNow("xml");
		return getXmlContentAsZipArchive(xmlContent,xmlFileName);
	}

	public byte[] getXmlContentAsZipArchive(byte[] xmlContent, String xmlFileName) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(os);
		ZipEntry zipEntry = new ZipEntry(xmlFileName);
		zos.putNextEntry(zipEntry);
		zos.write(xmlContent);
		zos.closeEntry();
		zos.close();
		os.close();
		return os.toByteArray();
	}
}
