/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.equinox.internal.provisional.p2.ui2.policy;

import java.net.URI;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.ui2.ProvUIActivator;
import org.eclipse.equinox.internal.p2.ui2.ProvUIMessages;
import org.eclipse.osgi.util.NLS;

/**
 * 
 * RepositoryLocationValidator can be used to validate a repository URL.  Validation may
 * involve rules known by the validator itself or contact with a repository
 * manager.
 * 
 * @since 3.4
 *
 */
public abstract class RepositoryLocationValidator {
	public static final String FILE_PROTOCOL_PREFIX = "file:"; //$NON-NLS-1$
	public static final String JAR_PATH_PREFIX = "jar:";//$NON-NLS-1$
	public static final String JAR_PATH_SUFFIX = "!/"; //$NON-NLS-1$
	public static final String JAR_EXTENSION = ".jar"; //$NON-NLS-1$
	public static final String ZIP_EXTENSION = ".zip"; //$NON-NLS-1$

	public static final int LOCAL_VALIDATION_ERROR = 3000;
	public static final int ALTERNATE_ACTION_TAKEN = 3001;

	public static IStatus getInvalidLocationStatus(String urlText) {
		return new Status(IStatus.ERROR, ProvUIActivator.PLUGIN_ID, LOCAL_VALIDATION_ERROR, NLS.bind(ProvUIMessages.URLValidator_UnrecognizedURL, urlText), null);
	}

	public static String makeJarURLString(String path) {
		String lowerCase = path.toLowerCase();
		if (lowerCase.endsWith(JAR_EXTENSION) || lowerCase.endsWith(ZIP_EXTENSION))
			return JAR_PATH_PREFIX + FILE_PROTOCOL_PREFIX + path + JAR_PATH_SUFFIX;
		return makeFileURLString(path);
	}

	public static String makeFileURLString(String path) {
		StringBuffer result = new StringBuffer(path.length() + 6);
		result.append(FILE_PROTOCOL_PREFIX);
		//make sure URL has leading slash to indicate a hierarchical URL
		if (path.length() == 0 || path.charAt(0) != '/')
			result.append('/');
		result.append(path);
		return result.toString();
	}

	public abstract IStatus validateRepositoryLocation(URI url, boolean contactRepositories, IProgressMonitor monitor);
}
