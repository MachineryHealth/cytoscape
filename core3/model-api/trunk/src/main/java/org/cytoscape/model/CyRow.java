
/*
 Copyright (c) 2008, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.model;

import java.util.Map;


/**
 * This interface represents one row in a CyTable.
 */
public interface CyRow {
	/**
	 * Returns the value found for this row in the specified column
	 * with the specified type.
	 * @param columnName The name identifying the attribute.
	 * @param type The type of the column.
	 * @return the value found for this row in the specified column
	 */
	<T> T get(String columnName, Class<?extends T> type);

	/**
	 * Set the specified column for this row to the specified value.
	 * @param columnName The name identifying the attribute.
	 * @param value The value to assign the specified column in this row 
	 */
	<T> void set(String columnName, T value);

	/**
	 * Returns the Class object that is defined for this column. 
     * @param columnName The name of the column to check.
     * @return The Class object that is defined for this column. Will
     * return null if the column has not been defined. Will always return
     * a base type. 
     */
	Class<?> getType(String columnName);

	/**
	 * Indicates whether the column of the specified type contains
	 * a non-null value.
	 * @param columnName The name identifying the attribute.
	 * @param type The type of the column.
	 * @return true if the value specified in this row at this column
	 * of the specified type is not null.
	 */
	<T> boolean isSet(String columnName, Class<?extends T> type);

	/**
     * Returns a map of column names to Objects that contain the values
     * contained in this Row.
     * @return A map of column names to Objects that contain the values
     * contained in this Row.
     */
	Map<String, Object> getAllValues();

	/**
	 * Don't use this!  This is a hack for the VizMapper and will go away
	 * once the VizMapper is refactored.
	 * @return The row Object that represents the value in a column.
	 */
	Object getRaw(String columnName);

	/**
	 * Returns the {@link CyTable} that this row belongs to.
	 * @return the {@link CyTable} that this row belongs to.
	 */
	CyTable getDataTable();
}
