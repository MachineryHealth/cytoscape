/*
 File: LoadNetworkFileTask.java

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

package org.cytoscape.task.internal.loadnetwork;

import static org.cytoscape.io.DataCategory.NETWORK;

import java.io.File;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkViewProducerManager;
import org.cytoscape.view.layout.CyLayouts;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.Tunable.Param;

import org.cytoscape.session.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;

/**
 * Specific instance of AbstractLoadNetworkTask that loads a File.
 */
public class LoadNetworkFileTask extends AbstractLoadNetworkTask {

	@Tunable(description = "Network file to load",flags = {Param.network})
	public File file;

	public LoadNetworkFileTask(CyNetworkViewProducerManager mgr, CyNetworkViewFactory gvf,
			CyLayouts cyl, CyNetworkManager netmgr, Properties props, CyNetworkNaming namingUtil) {
		super(mgr, gvf, cyl, netmgr, props, namingUtil);
	}

	/**
	 * Executes Task.
	 */
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		
		reader = mgr.getProducer((file.toURI()));

		uri = file.toURI();
		name = file.getName();
		
		if (reader == null) {
			uri = null;
		}
		
		loadNetwork(reader);
		System.out.println("\n\nNetwork " + file.getAbsolutePath() + " is LOADED !!!\n\n");
	}
}
