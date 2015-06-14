/*******************************************************************************
 * Copyright (c) 2015 Open Software Solutions GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 *
 * Contributors:
 *     Open Software Solutions GmbH
 ******************************************************************************/

package org.oss.digitalforms.utils.map;

import java.util.HashMap;

/**
 * @author Magnus Karlsson
 *
 */
@SuppressWarnings("serial")
public class CaseInsensitiveMap extends HashMap<String, String> {


	@Override
    public String put(String key, String value) {
       return super.put(key.toLowerCase(), value);
    }

    // not @Override because that would require the key parameter to be of type Object
    public String get(String key) {
       return super.get(key.toLowerCase());
    }

}
