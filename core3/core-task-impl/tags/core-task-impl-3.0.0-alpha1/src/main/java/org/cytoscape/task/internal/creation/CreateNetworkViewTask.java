/*
 File: CreateNetworkViewTask.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.task.internal.creation;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.task.AbstractNetworkTask;

import org.cytoscape.view.model.CyNetworkViewManager;


public class CreateNetworkViewTask extends AbstractNetworkTask {
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkViewFactory gvf;

	public CreateNetworkViewTask(CyNetwork n, CyNetworkViewFactory gvf, CyNetworkViewManager networkViewManager) {
		super(n);
		this.gvf = gvf;
		this.networkViewManager = networkViewManager;
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Creating network view ...");
		taskMonitor.setProgress(-1.0);

		try {
			CyNetworkView view = gvf.getNetworkView(net);
			networkViewManager.addNetworkView(view);
		} catch (Exception e) {
			throw new Exception("Could not create network view for network: "
					+ net.getCyRow().get("name", String.class), e);
		}

		taskMonitor.setProgress(1.0);
		taskMonitor.setStatusMessage("Network view successfully create for:  "
				+ net.getCyRow().get("name", String.class));
	}
}
