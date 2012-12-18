/*
  Copyright (c) 2006, 2007, 2008 The Cytoscape Consortium (www.cytoscape.org)

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

package chemViz.tasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import giny.model.GraphObject;
import giny.view.EdgeView;
import giny.view.NodeView;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;

import chemViz.model.Compound;
import chemViz.model.Compound.AttriType;
import chemViz.ui.CompoundPopup;


/**
 * This class is the common base class for the two compound loader tasks CreatePopupTask
 * and CreateCompoundTableTask
 */
abstract public class AbstractCompoundTask implements Task {
	TaskMonitor monitor;
	boolean canceled = false;
	int maxCompounds = 0;
	int compoundCount = 0;
	List<Compound> threadResultsList = null;

	// These are used for our progress meeter
	int totalObjects = 0;
	int	objectCount = 0;

	public void halt() { canceled = true; }

	public void setTaskMonitor(TaskMonitor monitor) {
		this.monitor = monitor;
	}

	abstract public String getTitle();

	/**
 	 * Runs the task -- this will get all of the compounds, fetching the images (if necessary) and creates the popup.
 	 */
	abstract public void run();
		
	public JTaskConfig getDefaultTaskConfig() {
		JTaskConfig result = new JTaskConfig();

		result.displayCancelButton(true);
		result.displayCloseButton(false);
		result.displayStatus(true);
		result.displayTimeElapsed(false);
		result.setAutoDispose(true);
		result.setModal(false);
		result.setOwner(Cytoscape.getDesktop());

		return result;
	}


	/**
 	 * Returns all of the Compounds for a list of graph objects (Nodes or Edges) based on the SMILES
 	 * and InChI attributes.
 	 *
 	 * @param goSet the Collection of graph objects we're looking at
 	 * @param attributes the appropriate set of attributes (nodeAttributes or edgeAttributes)
 	 * @param sList the list of attributes that contain SMILES strings
 	 * @param iList the list of attributes that contain InChI strings
 	 * @return the list of compounds.  If the compounds have not already been created, they are created
 	 *         as a byproduct of this method.
 	 */
	protected List<Compound> getCompounds(Collection<GraphObject> goSet, CyAttributes attributes, 
	                                      List<String> sList, List<String> iList, int maxThreads) {

		List<GetCompoundTask> threadList = null;
		long startTime = Calendar.getInstance().getTimeInMillis();

		if (maxThreads != 1)
			threadList = new ArrayList<GetCompoundTask>();

		List<Compound> cList = new ArrayList<Compound>();
		for (GraphObject go: goSet) {
			if (done()) break;
			if (maxThreads == 1)
				updateMonitor();
			cList.addAll(getCompounds(go, attributes, sList, iList, threadList));
		}

		if (threadList != null && threadList.size() > 0)
			cList.addAll(GetCompoundTask.runThreads(maxThreads, threadList));

		long endTime = Calendar.getInstance().getTimeInMillis();
		System.out.println("getCompounds took: "+(endTime-startTime)+"ms total");
		System.out.println(" createStructure took: "+Compound.totalTime+"ms");
		System.out.println("  getFingerprint took: "+Compound.totalFPTime+"ms");
		System.out.println("  SMILES parsing took: "+Compound.totalSMILESTime+"ms");
		System.out.println("  creating the fingerprint took: "+Compound.totalGetFPTime+"ms");

		return cList;
	}

	/**
 	 * Returns all of the Compounds for a single graph object (Node or Edge) based on the SMILES
 	 * and InChI attributes.
 	 *
 	 * @param go the graph object we're looking at
 	 * @param attributes the appropriate set of attributes (nodeAttributes or edgeAttributes)
 	 * @param sList the list of attributes that contain SMILES strings
 	 * @param iList the list of attributes that contain InChI strings
 	 * @return the list of compounds.  If the compounds have not already been created, they are created
 	 *         as a byproduct of this method.
 	 */
	protected List<Compound> getCompounds(GraphObject go, CyAttributes attributes, 
	                                      List<String> sList, List<String> iList, 
	                                      List<GetCompoundTask> threadList) {
		if ((sList == null || sList.size() == 0) 
		    && (iList == null || iList.size() == 0))
			return null;
		
		List<Compound> cList = new ArrayList();

		// Get the compound list from each attribute
		for (String attr: sList) {
			if (done()) break;
			cList.addAll(getCompounds(go, attributes, attr, AttriType.smiles, threadList));
		}

		for (String attr: iList) {
			if (done()) break;
			cList.addAll(getCompounds(go, attributes, attr, AttriType.inchi, threadList));
		}

		return cList;
	}

	/**
 	 * Returns all of the Compounds for a single graph object (Node or Edge) based on the designated
 	 * attribute of the specific type
 	 *
 	 * @param go the graph object we're looking at
 	 * @param attributes the appropriate set of attributes (nodeAttributes or edgeAttributes)
 	 * @param attr the attribute that contains the compound descriptor
 	 * @param type the type of the attribute (smiles or inchi)
 	 * @return the list of compounds.  If the compounds have not already been created, they are created
 	 *         as a byproduct of this method.
 	 */
	protected List<Compound> getCompounds(GraphObject go, CyAttributes attributes, 
	                                      String attr, AttriType type,
	                                      List<GetCompoundTask> threadList) {
		byte atype = attributes.getType(attr);
		List<Compound> cList = new ArrayList();
			
		if (!attributes.hasAttribute(go.getIdentifier(), attr)) 
			return cList;
		if (atype == CyAttributes.TYPE_STRING) {
			String cstring = attributes.getStringAttribute(go.getIdentifier(), attr);
			cList.addAll(getCompounds(go, attr, cstring, type, threadList));
		} else if (atype == CyAttributes.TYPE_SIMPLE_LIST) {
			List<String> stringList = attributes.getListAttribute(go.getIdentifier(), attr);
			for (String cstring: stringList) {
				cList.addAll(getCompounds(go, attr, cstring, type, threadList));
				if (done()) break;
			}
		}
		return cList;
	}

	protected List<Compound> getCompounds(GraphObject go, String attr, 
	                                      String compoundString, AttriType type,
	                                      List<GetCompoundTask> threadList) {
		List<Compound> cList = new ArrayList();

		String[] cstrings = null;

		if (type == AttriType.smiles) {
			cstrings = compoundString.split(",");
		} else {
			cstrings = new String[1];
			cstrings[0] = compoundString;
		}

		for (int i = 0; i < cstrings.length; i++) {
			Compound c = Compound.getCompound(go, attr, cstrings[i], type);
			if (c == null) {
				if (threadList != null) {
					threadList.add(new GetCompoundTask(go, attr, cstrings[i], type));
					continue;
				} 

				c = new Compound(go, attr, cstrings[i], type);
			} 

			cList.add(c);
			compoundCount++;
			if (done())
				return cList;
		}

		return cList;
	}

	protected void updateMonitor() {
		if (monitor == null || totalObjects == 0) return;
		monitor.setPercentCompleted((int)(((double)objectCount/(double)totalObjects) * 100.0));
		objectCount++;
	}

	protected void setStatus(String status) {
		if (monitor != null) monitor.setStatus(status);
	}

	private boolean done() {
		if (canceled) return true;
		if ((maxCompounds != 0) && (compoundCount >= maxCompounds)) return true;
		return false;
	}

}
