/** Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 **
 ** Code written by: Gary Bader
 ** Authors: Gary Bader, Ethan Cerami, Chris Sander
 ** Date: Jan.20.2003
 ** Description: Cytoscape Plug In that clusters a graph according to the MCODE
 ** algorithm.
 **
 ** Based on the csplugins.tutorial written by Ethan Cerami and GINY plugin
 ** written by Andrew Markiel
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
 **/

package csplugins.mcode;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;

import javax.swing.*;

/**
 * MCODE Network Clustering Plug In.
 * Clusters a graph.
 *
 * @author Gary Bader
 */
public class MCODEPlugin extends CytoscapePlugin {
	/**
	 * Constructor for the MCODE plugin.
	 */
	public MCODEPlugin() {
		//set-up menu options in plugins menu
		JMenu menu = Cytoscape.getDesktop().getCyMenus().getOperationsMenu();
        JMenuItem item;
        //MCODE submenu
		JMenu submenu = new JMenu("MCODE");
        item = new JMenuItem("Run MCODE on current network");
        item.addActionListener(new MCODEScoreAndFindAction());
        submenu.add(item);
        item = new JMenuItem("Set parameters");
        item.addActionListener(new MCODEParameterChangeAction());
        submenu.add(item);
        menu.add(submenu);
        //Advanced sub-sub menu
        JMenu subsubmenu = new JMenu("Advanced");
		item = new JMenuItem("Step 1: Score Network");
		item.addActionListener(new MCODEScoreAction());
        subsubmenu.add(item);
		item = new JMenuItem("Step 2: Find Complexes");
		item.addActionListener(new MCODEFindAction());
        subsubmenu.add(item);
        submenu.add(subsubmenu);
        //About box
        item = new JMenuItem("About MCODE");
        item.addActionListener(new MCODEAboutAction());
        submenu.add(item);
        menu.add(submenu);
	}

	/**
	 * Describes the plug in.
	 * @return short plug in description.
	 */
	public String describe() {
		return new String("Clusters a network using the MCODE algorithm.");
	}
}
