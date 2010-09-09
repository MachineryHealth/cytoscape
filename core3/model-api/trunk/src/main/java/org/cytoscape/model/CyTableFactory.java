
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

import java.util.List;


/**
 * An interface describing a factory used for creating 
 * {@link CyTable} objects.  This factory will be
 * provided as a service through Spring/OSGi.
 */
public interface CyTableFactory {
	/**
	 * @param title The name of the CyTable.
	 * @param primaryKey The name primaryKey column for this table. 
	 * @param primaryKeyType The type of the primaryKey column for this table. 
	 * @param pub Whether or not the CyTable should be public.
	 *
	 * @return A new {@link CyTable} with the specified name that is either public or not (see
	 *         {@link CyTable#isPublic}.
	 */
	CyTable createTable(String title, String primaryKey, Class<?> primaryKeyType, boolean pub);

	/**
     * @param includePrivate Whether to include private CyDataTables
     * in the list (i.e. all possible CyDataTables) or not.
     * @return A list containing CyTable SUIDs either
     * including private CyDataTables (i.e. meaning all possible
     * CyDataTables) or just public CyDataTables.
     */
	List<Long> getAllTableSUIDs(boolean includePrivate);

	/**
	 * 
	 * @param suid The SUID identifying the CyTable.
	 *
	 * @return The CyTable identified by the suid. Will return null if a CyTable doesn't
	 *         exist for the  specified SUID.
	 */
	CyTable getTable(long suid);
}
