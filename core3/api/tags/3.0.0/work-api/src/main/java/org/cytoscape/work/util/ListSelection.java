package org.cytoscape.work.util;

/*
 * #%L
 * Cytoscape Work API (work-api)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;


/**
 * A ListSelection object.
 * 
 * @param <T>  type of item that will be listed.
 */
class ListSelection<T> {

	
	/**
	 * Declares a List of items of type <code>T</code>.
	 */
	protected final List<T> values;

	
	/**
	 * Creates a new ListSelection object.
	 *
	 * @param values List of items of type <code>T</code> that contains the one(s) that is(are) going to be selected.
	 * The list of values my be empty.
	 */
	public ListSelection(final List<T> values) {
		if (values == null)
			throw new NullPointerException("values is null.");

		this.values = values;
	}

	
	/**
	 * To get all the items of the <code>List<T> values</code>.
	 *
	 * @return  an enumeration of all the items.
	 */
	public List<T> getPossibleValues() {
		return new ArrayList<T>(values);
	}
}
