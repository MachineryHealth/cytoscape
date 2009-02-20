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
package org.eclipse.equinox.internal.p2.ui2.query;

import org.eclipse.equinox.internal.p2.ui2.model.QueriedElementCollector;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.query.IQueryable;
import org.eclipse.equinox.internal.provisional.p2.ui2.model.InstalledIUElement;

/**
 * Collectors that accepts the matched IU's and
 * wraps them in an InstalledIUElement.
 * 
 * @since 3.4
 */
public class InstalledIUCollector extends QueriedElementCollector {

	public InstalledIUCollector(IQueryable queryable, Object parent) {
		super(queryable, parent);
	}

	/**
	 * Accepts a result that matches the query criteria.
	 * 
	 * @param match an object matching the query
	 * @return <code>true</code> if the query should continue,
	 * or <code>false</code> to indicate the query should stop.
	 */
	public boolean accept(Object match) {
		if (!(match instanceof IInstallableUnit))
			return true;
		if (queryable instanceof IProfile)
			return super.accept(new InstalledIUElement(parent, ((IProfile) queryable).getProfileId(), (IInstallableUnit) match));
		// Shouldn't happen, the queryable should typically be a profile
		return super.accept(match);
	}

}
