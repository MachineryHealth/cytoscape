/* * Modified Date: Jul.27.2010
 * * by : Steven Maere
 * */

/*
 * ResultAndStartPanel.java
 *
 * Created on August 5, 2006, 6:37 PM
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * The software and documentation provided hereunder is on an "as is" basis,
 * and the Pasteur Institut
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall the
 * Pasteur Institut
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * the Pasteur Institut
 * has been advised of the possibility of such damage. See the
 * GNU General Public License for more details: 
 *                http://www.gnu.org/licenses/gpl.txt.
 *
 * Authors: Olivier Garcia
 */

package pingo.GOlorize;

import javax.swing.JTable;

import org.cytoscape.view.model.CyNetworkView;

/**
 * 
 * @author ogarcia
 */
public interface ResultAndStartPanel {

	int getSelectColumn();

	int getGeneColumn();

	int getAliasColumn();

	int getGoIdColumn();

	int getGeneDescriptionColumn();

	int getGoDescriptionColumn();

	int getPvalColumn();

	JTable getJTable();

	GoBin getGoBin();

	boolean isSelected(String term);

	boolean select(String term);

	boolean unselect(String term);

	CyNetworkView getNetworkView();
}
