/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.ui2;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.core.repository.IRepository;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.ui2.ProvUI;

/**
 * Adapter factory for provisioning elements
 * 
 * @since 3.4
 * 
 */

public class ProvUIAdapterFactory implements IAdapterFactory {
	private static final Class[] CLASSES = new Class[] {IInstallableUnit.class, IProfile.class, IRepository.class, IMetadataRepository.class, IArtifactRepository.class};

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		return ProvUI.getAdapter(adaptableObject, adapterType);
	}

	public Class[] getAdapterList() {
		return CLASSES;
	}

}
