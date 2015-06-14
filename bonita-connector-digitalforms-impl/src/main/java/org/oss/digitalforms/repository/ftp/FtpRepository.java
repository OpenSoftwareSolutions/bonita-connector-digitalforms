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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.apache.commons.net.ftp.FTPFile;
import org.oss.digitalforms.repository.CloudAccessor;
import org.oss.digitalforms.utils.ftp.FtpTransfer;


/**
 * @author Magnus Karlsson, Donat Müller
 *
 */
public class FtpRepository implements CloudAccessor {

	private static final String DOMAINS_XML = "domains.xml";
	private final String protocolKind, fileSuffix, host, user, pwd;

	public FtpRepository(String protocolKind, String fileSuffix, String host,
			String user, String pwd) throws IOException {
		super();
		this.protocolKind = protocolKind;
		this.fileSuffix = fileSuffix;
		this.host = host;
		this.user = user;
		this.pwd = pwd;
		init();
	}

	private void init() throws IOException {
		FtpTransfer ftp = getFtpTransfer();
		FTPFile[] dirlist = ftp.listDirectory("/");
		if (!ftp.folderExists(dirlist, getProtocolKind())) {
			ftp.createFolder(getProtocolKind());
		}
		try {
			File tempFile = ftp.downloadToTempFile(getFolder(), DOMAINS_XML);
			tempFile.delete();
		} catch (Exception e) {
			DomainsRegistry registry = new DomainsRegistry(getProtocolKind());
			try {
				ftp.uploadFile(registry.toXML().getBytes(), DOMAINS_XML,
						getFolder());
			} catch (Exception e1) {
				throw new IOException("Exception while updating domains.xml",
						e1);
			}
		}

		ftp.disconnect();
	}

	@Override
	public String getProtocolKind() {
		return protocolKind;
	}

	private String getFolder() {
		return "/" + getProtocolKind();
	}

	@Override
	public Set<String> queryDomainIds() throws IOException {
		FtpTransfer ftp = getFtpTransfer();
		File tempFile = ftp.downloadToTempFile(getFolder(), DOMAINS_XML);
		try {
			DomainsRegistry registry = DomainsRegistry.fromFile(tempFile);
			return registry.domainIds();
		} catch (Exception e) {
			throw new IOException("Exception while parsing domains.xml", e);
		} finally {
			tempFile.delete();
			ftp.disconnect();
		}
	}

	@Override
	public InputStream getInstance(String domainId, boolean remove)
			throws IOException {
		FtpTransfer ftp = getFtpTransfer();
		File tempFile = ftp.downloadToTempFile(getFolder(), domainId + "."
				+ fileSuffix);
		ftp.disconnect();
		if (remove) {
			return new RemoveRemoteTemporaryFileBasedOutputStream(tempFile,
					domainId);
		} else {
			return new TemporaryFileBasedOutputStream(tempFile);
		}
	}

	@Override
	public Set<InputStream> getAllInstances(String domainId, boolean remove) {
		throw new UnsupportedOperationException(
				"This implementation does not support history.");
	}

	@Override
	public void putInstance(String domainId, InputStream content)
			throws IOException {
		FtpTransfer ftp = getFtpTransfer();
		File tempFile = ftp.downloadToTempFile(getFolder(), DOMAINS_XML);
		try {
			DomainsRegistry registry = DomainsRegistry.fromFile(tempFile);
			registry.addEntry(domainId, "No Title");
			ftp.uploadFile(registry.toXML().getBytes(), DOMAINS_XML,
					getFolder());
			ftp.uploadFile(content, domainId + "." + fileSuffix, getFolder());
		} catch (Exception e) {
			throw new IOException("Exception while updating domains.xml", e);
		} finally {
			tempFile.delete();
			ftp.disconnect();
		}
	}

	@Override
	public void removeInstance(String domainId) throws IOException {
		FtpTransfer ftp = getFtpTransfer();
		try {
			ftp.deleteFile(getProtocolKind(), domainId + "." + fileSuffix);
		} finally {
			ftp.disconnect();
		}
	}

	private FtpTransfer getFtpTransfer() throws IOException {
		return new FtpTransfer(host, user, pwd);
	}

	private static class TemporaryFileBasedOutputStream extends
			FilterInputStream {

		private final File tempFile;

		private TemporaryFileBasedOutputStream(File tempFile)
				throws FileNotFoundException {
			super(new FileInputStream(tempFile));
			this.tempFile = tempFile;
		}

		@Override
		public void close() throws IOException {
			super.close();
			tempFile.delete();
		}
	}

	private class RemoveRemoteTemporaryFileBasedOutputStream extends
			TemporaryFileBasedOutputStream {
		private final String domainId;

		private RemoveRemoteTemporaryFileBasedOutputStream(File tempFile,
				String domainId) throws FileNotFoundException {
			super(tempFile);
			this.domainId = domainId;
		}

		@Override
		public void close() throws IOException {
			super.close();
			FtpTransfer ftp = getFtpTransfer();
			File tempFile = ftp.downloadToTempFile(getFolder(), DOMAINS_XML);
			try {
				DomainsRegistry registry = DomainsRegistry.fromFile(tempFile);
				registry.removeDomain(domainId);
				ftp.uploadFile(registry.toXML().getBytes(), DOMAINS_XML,
						getFolder());
				ftp.deleteFile(getProtocolKind(), domainId + "." + fileSuffix);
			} catch (Exception e) {
				throw new IOException("Exception while updating domains.xml", e);
			} finally {
				tempFile.delete();
				ftp.disconnect();
			}
		}

	}

}
