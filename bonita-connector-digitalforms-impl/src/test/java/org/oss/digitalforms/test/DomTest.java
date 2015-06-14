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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.oss.digitalforms.repository.CloudAccessor;
import org.oss.digitalforms.repository.ftp.DomainsRegistry;
import org.oss.digitalforms.repository.ftp.FtpRepository;


public class DomTest {
	@Test
	public void parseDomainsXml() throws Exception {
		DomainsRegistry reg = DomainsRegistry.fromFile(new File("src/test/resources/domains.xml"));
		System.out.println(reg.toString());
	}


//	@Test
	public void uploadDomainsXml() throws FileNotFoundException, IOException {
		CloudAccessor ca = new FtpRepository("foo","zip","ftp.digireporting.com","forms@digireporting.com","SuperDemo123");
		ca.putInstance("domains.xml", new FileInputStream("src/test/resources/domains.xml"));
	}

//	@Test
	public void listDomains() throws IOException {
		CloudAccessor ca = new FtpRepository("foo","zip","ftp.digireporting.com","forms@digireporting.com","SuperDemo123");
		for (String domain : ca.queryDomainIds()) {
			System.out.println(domain);
		}
	}

	@Test
	public void testNewRepo() throws IOException {
		CloudAccessor ca = new FtpRepository("bar","zip","ftp.digireporting.com","forms@digireporting.com","SuperDemo123");
		for (String domain : ca.queryDomainIds()) {
			System.out.println(domain);
		}
		ca.putInstance("cool.xml", new FileInputStream("src/test/resources/domains.xml"));


	}
}
