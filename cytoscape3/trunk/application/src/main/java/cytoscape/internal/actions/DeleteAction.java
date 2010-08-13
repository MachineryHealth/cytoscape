/* -*-Java-*-
 ********************************************************************************
 *
 * File:         DeleteAction.java
 * RCS:          $Header: $
 * Description:
 * Author:       Allan Kuchinsky
 * Created:      Tue May 24 06:54:43 2005
 * Modified:     Tue Jan 09 05:16:48 2007 (Michael L. Creech) creech@w235krbza760
 * Language:     Java
 * Package:
 * Status:       Experimental (Do Not Distribute)
 *
 * (c) Copyright 2006, Agilent Technologies, all rights reserved.
 *
 ********************************************************************************
 *
 * Revisions:
 *
* Fri May 11 16:58:47 2007 (Michael L. Creech) creech@w235krbza760
*  Removed unneeded imports and update some generics.
 * Tue Jan 09 05:15:18 2007 (Michael L. Creech) creech@w235krbza760
 *  Added setup of Delete keyboard accelerator to constructors.
 * Wed Dec 27 06:54:56 2006 (Michael L. Creech) creech@w235krbza760
 *  Removed use of _title and removed 2 parameter constructor, removed unused
 *  local variables.
 ********************************************************************************
 */
package cytoscape.internal.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.MenuEvent;

import org.cytoscape.model.CyDataTableUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.GraphObject;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.undo.UndoSupport;

import cytoscape.view.CytoscapeAction;


/**
 *
 * action for deleting selected Cytoscape nodes and edges
 *
 * @author Allan Kuchinsky, Agilent Technologies
 * @version 1.0
 *
 */
public class DeleteAction extends CytoscapeAction {
	private static final long serialVersionUID = -5769255815829787466L;

	private CyRootNetworkFactory cyRootNetworkFactory;
	
	/**
	 * 
	 */
	public static final String ACTION_TITLE = "Delete Selected Nodes and Edges";

	private GraphObject graphObj = null;

	private final UndoSupport undo;

	/**
	 * action for deleting selected Cytoscape nodes and edges
	 */
	public DeleteAction(final UndoSupport undo, final CyRootNetworkFactory root,CyNetworkManager netmgr) {
		this(null,undo,root,netmgr);
	}

	/**
	 * perform deletion on the input object. if object is a Node, then this will
	 * result in also deleting the edges adjacent to the node
	 *
	 * @param obj the object to be deleted
	 */

	public DeleteAction(GraphObject obj, final UndoSupport undo, final CyRootNetworkFactory root, CyNetworkManager netmgr) {
		super(ACTION_TITLE,netmgr);
		setPreferredMenu("Edit");
		setAcceleratorCombo(KeyEvent.VK_DELETE, 0);
		graphObj = obj;
		this.undo = undo;
		cyRootNetworkFactory = root; 
	}


	/**
	 * Delete all selected nodes, edges, edges adjacent to the selected nodes,
	 * and any nodes/edges passed as arguments.
	 */
	public void actionPerformed(ActionEvent ae) {

		CyNetworkView myView = netmgr.getCurrentNetworkView();

		// delete from the base CySubNetwork so that our changes can be undone 
		CySubNetwork cyNet = cyRootNetworkFactory.convert( myView.getModel() ).getBaseNetwork();
		List<CyEdge> selEdges = CyDataTableUtil.getEdgesInState(cyNet,"selected",true); 
		List<CyNode> selNodes = CyDataTableUtil.getNodesInState(cyNet,"selected",true); 
		CyNode cyNode;
		CyEdge cyEdge;

		// if an argument exists, add it to the appropriate list
		if (graphObj != null ) {
			if ( graphObj instanceof CyNode) {
				cyNode = (CyNode) graphObj;
				if ( !selNodes.contains(cyNode) )
					selNodes.add(cyNode);
			} else if ( graphObj instanceof CyEdge) {
				cyEdge = (CyEdge) graphObj;
				if ( !selEdges.contains(cyEdge) )
					selEdges.add(cyEdge);
			}
		}

		Set<CyEdge> edges = new HashSet<CyEdge>();
		Set<CyNode> nodes = new HashSet<CyNode>();

		// add all node indices
		for (int i = 0; i < selNodes.size(); i++) {
			cyNode = selNodes.get(i);
			nodes.add(cyNode);
		}

		// add all selected edge indices
		for (int i = 0; i < selEdges.size(); i++) {
			cyEdge = selEdges.get(i); 
			edges.add( cyEdge );
		}

		undo.getUndoableEditSupport().postEdit( new DeleteEdit(cyNet,nodes,edges,this,netmgr) );
		
		// delete the actual nodes and edges

		for ( CyNode nd : nodes )
			cyNet.removeNode(nd);
		for ( CyEdge ed : edges )
			cyNet.removeEdge(ed);

		//Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null, cyNet);
	}

    public void menuSelected(MenuEvent me) {
        CyNetwork n = netmgr.getCurrentNetwork();
        if ( n == null ) {
            setEnabled(false);
            return;
        }

        List<CyNode> nodes = CyDataTableUtil.getNodesInState(n,"selected",true);
        List<CyEdge> edges = CyDataTableUtil.getEdgesInState(n,"selected",true);

        if ( nodes.size() > 0 || edges.size() > 0 )
            setEnabled(true);
        else
            setEnabled(false);
    }
}
