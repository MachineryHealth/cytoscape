/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.ui.query;

import java.util.HashMap;
import org.eclipse.equinox.internal.p2.ui.model.IIUElement;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.query.IQueryable;

/**
 * Collector that only accepts categories or the latest version of each
 * IU.
 * 
 * @since 3.4
 */
public class LatestIUVersionElementCollector extends AvailableIUCollector {

	private HashMap uniqueIds = new HashMap();

	public LatestIUVersionElementCollector(IQueryable queryable, Object parent, boolean makeCategories, boolean makeDrillDownChild) {
		super(queryable, parent, makeCategories, makeDrillDownChild);
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
		IInstallableUnit iu = (IInstallableUnit) match;
		// If it's a category, treat it as such if we are to build categories
		if (makeCategory() && isCategory(iu))
			return super.accept(match);
		// Look for the latest element
		Object matchElement = uniqueIds.get(iu.getId());
		if (matchElement == null || iu.getVersion().compareTo(getIU(matchElement).getVersion()) > 0) {
			if (matchElement != null)
				getCollection().remove(matchElement);
			matchElement = makeDefaultElement(iu);
			uniqueIds.put(iu.getId(), matchElement);
			return super.accept(matchElement);
		}
		return true;
	}

	protected IInstallableUnit getIU(Object matchElement) {
		if (matchElement instanceof IInstallableUnit)
			return (IInstallableUnit) matchElement;
		if (matchElement instanceof IIUElement)
			return ((IIUElement) matchElement).getIU();
		return null;
	}
}
