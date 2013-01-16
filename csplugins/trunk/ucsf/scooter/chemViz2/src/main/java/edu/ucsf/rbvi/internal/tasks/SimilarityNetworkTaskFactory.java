package edu.ucsf.rbvi.chemViz2.internal.tasks;

import java.util.Collections;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.chemViz2.internal.model.ChemInfoSettings;

public class SimilarityNetworkTaskFactory extends ChemVizAbstractTaskFactory 
                                    implements NetworkViewTaskFactory, NodeViewTaskFactory {
	ChemInfoSettings settings = null;
	Scope scope;
	boolean newNetwork = true;
	CyNetworkViewFactory viewFactory;
	CyNetworkManager networkManager;
	CyNetworkViewManager networkViewManager;
	VisualMappingManager vmm;

	public SimilarityNetworkTaskFactory(ChemInfoSettings settings, CyNetworkViewFactory viewFactory,
	                                    CyNetworkManager networkManager, CyNetworkViewManager viewManager,
	                                    VisualMappingManager vmm, boolean newNetwork, Scope scope) {
		this.settings = settings;
		this.scope = scope;
		this.vmm = vmm;
		this.viewFactory = viewFactory;
		this.newNetwork = newNetwork;
		this.networkManager = networkManager;
		this.networkViewManager = viewManager;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public boolean isReady(CyNetworkView networkView) {
		if (networkView == null) 
			return false;
		if (scope == Scope.ALLNODES && settings.hasNodeCompounds(networkView.getModel().getNodeList()))
			return true;

		if (scope == Scope.SELECTEDNODES)
			return selectedNodesReady(networkView.getModel());

		return false;
	}

	public boolean isReady(View<CyNode> nView, CyNetworkView netView) {
		if (netView == null) 
			return false;
		if (nView != null && settings.hasNodeCompounds(Collections.singletonList(nView.getModel())))
			return true;

		return selectedNodesReady(netView.getModel());
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		List<CyNode> selectedNodes;
		if (scope == Scope.ALLNODES)
			selectedNodes = networkView.getModel().getNodeList();
		else {
			selectedNodes = CyTableUtil.getNodesInState(networkView.getModel(), CyNetwork.SELECTED, true);
		}
		return new TaskIterator(new TanimotoScorerTask(networkView, selectedNodes, viewFactory,
		                                               networkManager, networkViewManager, vmm, settings, newNetwork));
	}

	public TaskIterator createTaskIterator(View<CyNode> nView, CyNetworkView netView) {
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true);
		if (selectedNodes == null || selectedNodes.size() == 0)
			selectedNodes = Collections.singletonList(nView.getModel());

		return new TaskIterator(new TanimotoScorerTask(netView, selectedNodes, viewFactory, 
		                                               networkManager, networkViewManager, vmm, settings, newNetwork));
	}

	private boolean selectedNodesReady(CyNetwork network) {
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		if (selectedNodes != null && selectedNodes.size() > 0) {
			if (settings.hasNodeCompounds(selectedNodes))
				return true;
		}
		return false;
	}
}
