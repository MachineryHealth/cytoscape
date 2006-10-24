package GOlorize;

/**
 * * Copyright (c) 2005 Flanders Interuniversitary Institute for Biotechnology (VIB)
 * *
 * * Authors : Steven Maere, Karel Heymans
 * *
 * * This program is free software; you can redistribute it and/or modify
 * * it under the terms of the GNU General Public License as published by
 * * the Free Software Foundation; either version 2 of the License, or
 * * (at your option) any later version.
 * *
 * * This program is distributed in the hope that it will be useful,
 * * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * * The software and documentation provided hereunder is on an "as is" basis,
 * * and the Flanders Interuniversitary Institute for Biotechnology
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Flanders Interuniversitary Institute for Biotechnology
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * the Flanders Interuniversitary Institute for Biotechnology
 * * has been advised of the possibility of such damage. See the
 * * GNU General Public License for more details.
 * *
 * * You should have received a copy of the GNU General Public License
 * * along with this program; if not, write to the Free Software
 * * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * *
 * * Authors: Steven Maere, Karel Heymans
 * * Date: Mar.25.2005
 * * Description: BiNGO is a Cytoscape plugin for the functional annotation of gene clusters.          
 **/


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import cytoscape.util.CytoscapeAction;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.Cytoscape;


/**
 * GOlorizeplugin.java     Steven Maere & Karel Heymans (c) March 2005
 * ----------------
 * 
 * Main class of the BiNGO plugin ; extends the CytoscapePlugin class from Cytoscape.
 */


public class GOlorizeplugin extends CytoscapePlugin { 




   	/*--------------------------------------------------------------
      FIELDS.
      --------------------------------------------------------------*/

		String bingoDir ;

	/*--------------------------------------------------------------
      CONSTRUCTOR.
      --------------------------------------------------------------*/

    
    public GOlorizeplugin() {
	    //create a new action to respond to menu activation
        BiNGOpluginAction action = new BiNGOpluginAction();
        //set the preferred menu
        action.setPreferredMenu("Plugins");
        //and add it to the menus
        Cytoscape.getDesktop().getCyMenus().addAction(action);
		String tmp = System.getProperty("user.dir") ;
		bingoDir = new File(tmp,"plugins").toString() ;
    }


	/*--------------------------------------------------------------
      METHODS.
      --------------------------------------------------------------*/
		
		
     
     // Gives a description of this plugin.
     
    public String describe() {
        StringBuffer sb = new StringBuffer();
            sb.append("BiNGO is a Cytoscape plugin for functional annotation ");
            sb.append("of gene clusters. It visualises the overrepresentation of Gene Ontology (GO) ");
            sb.append("categories in a set of selected genes.");
        return sb.toString();
    }



	/*--------------------------------------------------------------
      INTERNAL LISTENER-CLASS.
    --------------------------------------------------------------*/

    
    // INTERNAL LISTENER-CLASS : This listener-class gets attached to the menu item.
     
    public class BiNGOpluginAction extends CytoscapeAction {

        
        // The constructor sets the text that should appear on the menu item.
         
    	public BiNGOpluginAction() {super("GOlorize");}

        /**
         * This method opens the BiNGO settingspanel upon selection of the menu item
         * and opens the settingspanel for BiNGO.
         *
         * @param event event triggered when BiNGO menu item clicked.
         */
		
        public void actionPerformed(ActionEvent event) {
/*
			JFrame window = new JFrame("BiNGO Settings");	
            SettingsPanel settingsPanel = new SettingsPanel(bingoDir);
			//window.setJMenuBar(new HelpMenuBar(settingsPanel).getHelpMenuBar());
			window.getContentPane().add(settingsPanel);
			window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			window.pack();
			Dimension screenSize =
		          Toolkit.getDefaultToolkit().getScreenSize();
			// for central position of the settingspanel.
		    window.setLocation(screenSize.width/2 - (window.getWidth()/2),
		                       screenSize.height/2 - (window.getHeight()/2));
			window.setVisible(true);
			window.setResizable(true);
*/
            new GOlorize.GoBin();
	}

    }
}
