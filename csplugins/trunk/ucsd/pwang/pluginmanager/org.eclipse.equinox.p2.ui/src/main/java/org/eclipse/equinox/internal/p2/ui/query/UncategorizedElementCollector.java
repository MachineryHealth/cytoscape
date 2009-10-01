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
package org.eclipse.equinox.internal.p2.ui.query;

import java.util.*;
import org.eclipse.equinox.internal.p2.ui.model.QueriedElementCollector;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.IRequiredCapability;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.query.IQueryable;

/**
 * A collector that collects everything specified by the query, but
 * removes anything referred to by a category when asked for its
 * contents.  Must be used with a compound query that includes categories
 * and something else.  For example, could be used with a category + group
 * query, and the net result would be all groups that were not referred to
 * by a category.
 * 
 * @since 3.4
 */
public class UncategorizedElementCollector extends QueriedElementCollector {

	private ArrayList categories = new ArrayList();
	private Set allOthers = new HashSet();
	private Collector resultCollector;

	public UncategorizedElementCollector(IQueryable queryable, Object parent, Collector resultCollector) {
		super(queryable, parent);
		this.resultCollector = resultCollector;
	}

	/*
	 * Accepts all IUs on the first pass, separating the categories and
	 * non-categories.  The real work is done once clients try to get the results.
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.internal.provisional.p2.ui.model.QueriedElementCollector#accept(java.lang.Object)
	 */
	public boolean accept(Object match) {
		if (match instanceof IInstallableUnit) {
			IInstallableUnit iu = (IInstallableUnit) match;
			if (Boolean.toString(true).equals(iu.getProperty(IInstallableUnit.PROP_TYPE_CATEGORY)))
				categories.add(iu);
			else
				allOthers.add(iu);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.internal.provisional.p2.query.Collector#iterator()
	 */
	public Iterator iterator() {
		removeReferredIUsAndRecollect();
		return resultCollector.iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.internal.provisional.p2.query.Collector#size()
	 */
	public int size() {
		removeReferredIUsAndRecollect();
		return resultCollector.size();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.internal.provisional.p2.query.Collector#isEmpty()
	 */
	public boolean isEmpty() {
		removeReferredIUsAndRecollect();
		return resultCollector.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.internal.provisional.p2.query.Collector#toArray(java.lang.Class)
	 */
	public Object[] toArray(Class clazz) {
		removeReferredIUsAndRecollect();
		return resultCollector.toArray(clazz);
	}

	private void removeReferredIUsAndRecollect() {
		Iterator iter = categories.iterator();
		while (iter.hasNext()) {
			IInstallableUnit categoryIU = (IInstallableUnit) iter.next();
			IRequiredCapability[] requirements = categoryIU.getRequiredCapabilities();
			for (int i = 0; i < requirements.length; i++) {
				if (requirements[i].getNamespace().equals(IInstallableUnit.NAMESPACE_IU_ID)) {
					IInstallableUnit[] arrayAllOthers = (IInstallableUnit[]) allOthers.toArray(new IInstallableUnit[allOthers.size()]);
					for (int j = 0; j < arrayAllOthers.length; j++)
						if (arrayAllOthers[j].getId().equals(requirements[i].getName()))
							allOthers.remove(arrayAllOthers[j]);
				}
			}
		}
		// Now allOthers has the correct content, so just
		// collect results in the result collector.
		iter = allOthers.iterator();
		while (iter.hasNext())
			resultCollector.accept(iter.next());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.internal.provisional.p2.query.Collector#toCollection()
	 */
	public Collection toCollection() {
		removeReferredIUsAndRecollect();
		return resultCollector.toCollection();
	}
}
