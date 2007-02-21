/* vim: set ts=2: */
/**
 * Copyright (c) 2006 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package SFLDLoader;

import SFLDLoader.ui.SFLDQueryDialog;
import SFLDLoader.model.Superfamily;

// System imports
import javax.swing.JOptionPane;
import java.util.List;
import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

// Cytoscape imports
import cytoscape.*;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.view.CyNetworkView;
import cytoscape.data.CyAttributes;
import cytoscape.util.CytoscapeAction;

/**
 * The SFLDLoader class provides the primary interface to the
 * Cytoscape plugin mechanism
 */
public class SFLDLoader extends CytoscapePlugin {
	final static float VERSION = 0.1f;
	static JDialog sfldQueryDialog = null;
	static List<Superfamily> superFamilies = null;
	public final String URLBase = "http://sfldtest.rbvi.ucsf.edu/cgi-bin/SFLDvm.py";

  /**
   * Create our action and add it to the plugins menu
   */
  public SFLDLoader() {
		JMenu menu = new JMenu("SFLD Loader");
		JMenuItem loader = new JMenuItem("Load network from SFLD");
		loader.addActionListener(new SFLDLoaderMenuListener());
		menu.add(loader);

		JMenu pluginMenu = Cytoscape.getDesktop().getCyMenus().getMenuBar()
																.getMenu("Plugins");
		pluginMenu.add(menu);
		System.out.println("SFLD Loader "+VERSION+" initialized");

		// Load the initial data from the SFLD
		SFLDEnumerator enumerator = new SFLDEnumerator();
		enumerator.start();

  }

	/**
	 * The SFLDLoaderMenuListener launches the loader dialog
	 */
	public class SFLDLoaderMenuListener implements ActionListener {

		/**
		 * Create the menu listener
		 *
		 */
		SFLDLoaderMenuListener() {
		}

		/**
		 * Process the selected menu
		 *
		 * @param e the MenuEvent for the selected menu
		 */
		public void actionPerformed (ActionEvent e)
		{
			// See if the dialog is already created
			if (sfldQueryDialog != null) {
				// Yes, pop it up
				sfldQueryDialog.setVisible(true);
				return;
			}
			// No, create it
			sfldQueryDialog = new SFLDQueryDialog(superFamilies);
			sfldQueryDialog.pack();
			sfldQueryDialog.setLocationRelativeTo(Cytoscape.getDesktop());
			sfldQueryDialog.setVisible(true);
		}
	}

	/**
	 * The SFLDEnumerator is a thread that loads the initial data set, parse
	 * the XML and fill out the model information
	 */
	public class SFLDEnumerator extends Thread {

		public SFLDEnumerator() {
		}

		public void run() {
			System.out.println("Initializing SFLD enumeration");
			DocumentBuilder builder = null;
			Document enumeration = null;
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				builder = factory.newDocumentBuilder();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				return;
			}
			try {
				enumeration = builder.parse(URLBase+"?query=enumerate&level=all&id=0");
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				return;
			}
			NodeList superNodes = enumeration.getElementsByTagName("superfamily");
			superFamilies = new ArrayList(superNodes.getLength());
			for (int i = 0; i < superNodes.getLength(); i++) {
				superFamilies.add(new Superfamily(superNodes.item(i)));
			}
			System.out.println("SFLD enumeration complete");
		}
	}
}
