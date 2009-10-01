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
package org.eclipse.equinox.internal.p2.ui.model;

import org.eclipse.equinox.internal.p2.ui.ProvUIMessages;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.ui.operations.ProvisioningUtil;

/**
 * Element class for profile snapshots
 * 
 * @since 3.5
 */
public class ProfileSnapshots extends ProvElement {

	String profileId;

	public ProfileSnapshots(String profileId) {
		super(null);
		this.profileId = profileId;
	}

	public String getProfileId() {
		return profileId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		try {
			long[] timestamps = ProvisioningUtil.getProfileTimestamps(profileId);
			// eliminate the last in the list (latest) because that is the current
			// profile.  
			RollbackProfileElement[] elements = new RollbackProfileElement[timestamps.length - 1];
			boolean skipFirst = false;
			for (int i = 0; i < timestamps.length - 1; i++) {
				elements[i] = new RollbackProfileElement(this, profileId, timestamps[i]);
				// Eliminate the first in the list (earliest) if there was no content at all.
				// This doesn't always happen, but can, and we don't want to offer the user an empty profile to
				// revert to.
				if (i == 0) {
					skipFirst = elements[0].getChildren(elements[0]).length == 0;
				}
			}
			if (skipFirst) {
				RollbackProfileElement[] elementsWithoutFirst = new RollbackProfileElement[elements.length - 1];
				System.arraycopy(elements, 1, elementsWithoutFirst, 0, elements.length - 1);
				return elementsWithoutFirst;
			}
			return elements;
		} catch (ProvisionException e) {
			handleException(e, null);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return ProvUIMessages.ProfileSnapshots_Label;
	}
}
