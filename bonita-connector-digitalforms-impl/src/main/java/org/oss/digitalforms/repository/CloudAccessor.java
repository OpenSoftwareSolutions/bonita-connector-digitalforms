/*******************************************************************************
 * Copyright (c) 2015 Open Software Solutions GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 *
 * Contributors:
 *     Open Software Solutions GmbH
 ******************************************************************************/
package org.oss.digitalforms.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Accessor to a data repository. A data unit is accessed by the domainId.<br>
 * For storing and retreiving streams are used. A repository implementation has not to<br>
 * know about the structure of the data. Is like a file system where protocolKind is the volume,<br>
 * the domainId's are the filenames and the streams are the files.
 *
 * @author Donat Müller
 *
 */
public interface CloudAccessor {
	/**
	 * Returns the ProtocolKind for this accessor.
	 * @return
	 */
	String getProtocolKind();

	/**
	 * Returns the available domainIds in the repository.
	 * @return
	 */
	Set<String> queryDomainIds() throws IOException;

	/**
	 * Download the most recent protocol instance for the given domainId.
	 * @param domainId
	 * @param remove
	 * @return
	 */
	InputStream getInstance(String domainId, boolean remove) throws IOException;

	/**
	 * Download all instances for the given domainId.
	 * @param domainId
	 * @param remove
	 * @return
	 */
	Set<InputStream> getAllInstances(String domainId, boolean remove) throws IOException;


	/**
	 * Upload an instance for the given domainId in the repository.
	 * @param domainId
	 * @param content
	 */
	void putInstance(String domainId, InputStream content) throws IOException;

	/**
	 * Remove an instance from the repository.
	 * @param domainId
	 */
	void removeInstance(String domainId) throws IOException;

}
