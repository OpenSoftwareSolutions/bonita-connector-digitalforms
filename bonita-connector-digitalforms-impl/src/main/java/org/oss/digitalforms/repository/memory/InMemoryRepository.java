/*******************************************************************************
 * Copyright (c) 2015 Open Software Solutions GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 *
 * Contributors:
 *     Open Software Solutions GmbH
 ******************************************************************************/
package org.oss.digitalforms.repository.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.oss.digitalforms.repository.CloudAccessor;


/**
 * @author Donat Müller
 *
 */
public class InMemoryRepository implements CloudAccessor {


	private final String protocolKind;
	private final Map<String,byte[]> repository;


	public InMemoryRepository(String protocolKind) {
		super();
		this.protocolKind = protocolKind;
		this.repository = new HashMap<String, byte[]>();
	}

	@Override
	public String getProtocolKind() {
		return protocolKind;
	}

	@Override
	public Set<String> queryDomainIds() {
		return repository.keySet();
	}

	@Override
	public InputStream getInstance(String domainId, boolean remove) {
		return new ByteArrayInputStream(repository.get(domainId));
	}

	@Override
	public Set<InputStream> getAllInstances(String domainId, boolean remove) {
		throw new UnsupportedOperationException("This implementation does not support history");
	}

	@Override
	public void putInstance(String domainId, InputStream content) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];
		while ((nRead = content.read(data, 0, data.length)) != -1) {
		  buffer.write(data, 0, nRead);
		}
		buffer.flush();
		repository.put(domainId, buffer.toByteArray());
	}

	@Override
	public void removeInstance(String domainId) {
		repository.remove(domainId);
	}

}
