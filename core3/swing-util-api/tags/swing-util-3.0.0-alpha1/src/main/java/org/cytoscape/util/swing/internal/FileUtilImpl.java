/*
  File: FileUtil.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.util.swing.internal;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Dialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.cytoscape.util.swing.FileUtil;

class FileUtilImpl implements FileUtil {
	
	private File mrud;

	FileUtilImpl() {
		mrud = new File(System.getProperty("user.dir"));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public File getFile(String title, int load_save_custom) {
		return getFile(title, load_save_custom, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public File getFile(String title, int load_save_custom, String start_dir,
			String custom_approve_text) {
		File[] result = getFiles(title, load_save_custom, start_dir,
				custom_approve_text, false);

		return ((result == null) || (result.length <= 0)) ? null : result[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public File[] getFiles(Component parent, String title, int load_save_custom) {
		return getFiles(parent, title, load_save_custom, null, null, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public File[] getFiles(String title, int load_save_custom,
			String start_dir, String custom_approve_text) {
		return getFiles(null, title, load_save_custom, start_dir,
				custom_approve_text, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public File[] getFiles(String title, int load_save_custom,
			String start_dir, String custom_approve_text, boolean multiselect) {
		return getFiles(null, title, load_save_custom, start_dir,
				custom_approve_text, multiselect);
	}

	/**
	 * {@inheritDoc}
	 */
	public File[] getFiles(Component parent, String title,
			int load_save_custom, String start_dir, String custom_approve_text,
			boolean multiselect) {

		if (parent == null)
			throw new NullPointerException("Parent component is null");

		File start = null;

		if (start_dir == null)
			start = getMRUD();
		else
			start = new File(start_dir);

		String osName = System.getProperty("os.name");

		// System.out.println( "Os name: "+osName );
		/* TODO -- FIX THIS!
		if (osName.startsWith("Mac")) {
			// this is a Macintosh, use the AWT style file dialog
			FileDialog chooser; 
			if ( parent instanceof Frame ) 
				chooser = new FileDialog((Frame) parent, title, load_save_custom);
			else 
				chooser = new FileDialog((Dialog) parent, title, load_save_custom);

			// we can only set the one filter; therefore, create a special
			// version of CyFileFilter that contains all extensions
			// TODO fix this so we actually use the filters we're given
			// CyFileFilter fileFilter = new CyFileFilter(new String[]{},new
			// String[]{},"All network files");

			// chooser.setFilenameFilter(fileFilter);

			chooser.setVisible(true);

			if (chooser.getFile() != null) {
				File[] result = new File[1];
				result[0] = new File(chooser.getDirectory() + "/"
						+ chooser.getFile());

				if (chooser.getDirectory() != null) {
					setMRUD(new File(chooser.getDirectory()));
				}

				return result;
			}

			return null;
		} else {
		*/
			// this is not a mac, use the Swing based file dialog
			final JFileChooser chooser = new JFileChooser(start);

			// set multiple selection, if applicable
			chooser.setMultiSelectionEnabled(multiselect);

			// set the dialog title
			chooser.setDialogTitle(title);

			// add filters
			// TODO: fix Filter
			// for (int i = 0; i < filters.length; ++i) {
			// chooser.addChoosableFileFilter(filters[i]);
			// }

			File[] result = null;
			File tmp = null;

			// set the dialog type
			if (load_save_custom == LOAD) {
				if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
					if (multiselect)
						result = chooser.getSelectedFiles();
					else if ((tmp = chooser.getSelectedFile()) != null) {
						result = new File[1];
						result[0] = tmp;
					}
				}
			} else if (load_save_custom == SAVE) {
				if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
					if (multiselect)
						result = chooser.getSelectedFiles();
					else if ((tmp = chooser.getSelectedFile()) != null) {
						result = new File[1];
						result[0] = tmp;
					}
					// FileDialog checks for overwrte, but JFileChooser does
					// not, so we need to do
					// so ourselves
					for (int i = 0; i < result.length; i++) {
						if (result[i].exists()) {
							int answer = JOptionPane
									.showConfirmDialog(
											chooser,
											"The file '"
													+ result[i].getName()
													+ "' already exists, are you sure you want to overwrite it?",
											"File exists",
											JOptionPane.YES_NO_OPTION,
											JOptionPane.WARNING_MESSAGE);
							if (answer == 1)
								return null;
						}
					}
				}
			} else {
				if (chooser.showDialog(parent, custom_approve_text) == JFileChooser.APPROVE_OPTION) {
					if (multiselect)
						result = chooser.getSelectedFiles();
					else if ((tmp = chooser.getSelectedFile()) != null) {
						result = new File[1];
						result[0] = tmp;
					}
				}
			}

			if ((result != null) && (start_dir == null))
				setMRUD(chooser.getCurrentDirectory());

			return result;
		//}
	}

	/**
	 * Get the most recently used directory.
	 */
	public synchronized File getMRUD() {
		return mrud;
	}

	/**
	 * Set the most recently used directory.
	 */
	public synchronized void setMRUD(File mrud_new) {
		if ( mrud_new != null )
			mrud = mrud_new;
	}
}
