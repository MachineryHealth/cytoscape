package csplugins.mcode;

import cytoscape.Cytoscape;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 **
 ** Code written by: Gary Bader
 ** Authors: Gary Bader, Ethan Cerami, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and the
 ** Institute for Systems Biology, the University of California at San Diego
 ** and/or Memorial Sloan-Kettering Cancer Center
 ** have no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall the
 ** Institute for Systems Biology, the University of California at San Diego
 ** and/or Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if the
 ** Institute for Systems Biology, the University of California at San
 ** Diego and/or Memorial Sloan-Kettering Cancer Center
 ** have been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 ** User: Gary Bader
 ** Date: Feb 6, 2004
 ** Time: 4:54:53 PM
 ** Description
 **/
public class MCODEParameterChangeAction implements ActionListener {
	/**
	 * This method is called when the user wants to change the MCODE parameters.
	 * @param event Menu Item Selected.
	 */
	public void actionPerformed(ActionEvent event) {
		//display complexes in a new non modal dialog box
		MCODEParameterChangeDialog paramChangeDialog = new MCODEParameterChangeDialog(Cytoscape.getDesktop());
		paramChangeDialog.pack();
		paramChangeDialog.setVisible(true);
	}
}
