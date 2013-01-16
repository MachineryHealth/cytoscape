package edu.ucsf.rbvi.chemViz2.internal;

import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import static org.cytoscape.work.ServiceProperties.*;

// Get all of our task factories
import edu.ucsf.rbvi.chemViz2.internal.model.ChemInfoSettings;
import edu.ucsf.rbvi.chemViz2.internal.model.CompoundManager;
import edu.ucsf.rbvi.chemViz2.internal.model.DescriptorManager;
import edu.ucsf.rbvi.chemViz2.internal.tasks.ChemInfoSettingsTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.CalculateEdgeMCSSTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.CalculateNodeMCSSTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.CompoundEdgePopupTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.CompoundNodePopupTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.CompoundEdgeTableTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.CompoundNodeTableTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.CreateEdgeAttributesTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.CreateNodeAttributesTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.PaintNodeStructuresTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.SearchEdgesTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.SearchNodesTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.SimilarityNetworkTaskFactory;
import edu.ucsf.rbvi.chemViz2.internal.tasks.ChemVizAbstractTaskFactory.Scope;
import edu.ucsf.rbvi.chemViz2.internal.view.CustomGraphicsFactory;

public class CyActivator extends AbstractCyActivator {
	private static Logger logger = 
		LoggerFactory.getLogger(edu.ucsf.rbvi.chemViz2.internal.CyActivator.class);
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		// We'll need the CyApplicationManager to get current network, etc.
		CyApplicationManager cyApplicationManagerServiceRef = 
			getService(bc,CyApplicationManager.class);

		// We'll need the CyServiceRegistrar to register listeners
		CyServiceRegistrar cyServiceRegistrar = 
			getService(bc,CyServiceRegistrar.class);

		// Create our managers.  These manage the addition of descriptors and compounds
		CompoundManager compoundManager = new CompoundManager();
		DescriptorManager descriptorManager = new DescriptorManager();
		VisualMappingManager vmm = getService(bc, VisualMappingManager.class);
		VisualMappingFunctionFactory vmff = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
		RenderingEngineManager renderingEngine = getService(bc, RenderingEngineManager.class);
		VisualLexicon lex = renderingEngine.getDefaultVisualLexicon();

		// Create our main context object.  Note that this is actually a 
		// task (so we can use Tunables), but we only ever create one of 
		// them and reuse it.
		ChemInfoSettings settings= 
			new ChemInfoSettings(cyApplicationManagerServiceRef, cyServiceRegistrar,
		                       compoundManager, descriptorManager);
		registerService(bc, settings, SetCurrentNetworkListener.class, new Properties());
		registerService(bc, settings, ColumnCreatedListener.class, new Properties());

		// See if we have a graphics console or not
		boolean haveGUI = true;
		ServiceReference ref = 
			bc.getServiceReference("org.cytoscape.application.swing.CySwingApplication");

		if (ref == null) {
			haveGUI = false;
		}

		settings.setHaveGUI(haveGUI);

		// TODO: Need to figure out how to get CySwingApplication.getJFrame without 
		// depending on org.cytoscape.application.swing...

		// Create our menus

		// CompoundTable -- needsGUI
		if (haveGUI) {
			String menu = "Show Compound Table[1.0]";
			TaskFactory tableTaskFactory = 
				new CompoundNodeTableTaskFactory(settings, Scope.ALLNODES);
			addMenus(bc, tableTaskFactory, menu, "for all nodes", "show compound table", 
			         Scope.ALLNODES, "network", "1.1", false);

			tableTaskFactory = 
				new CompoundNodeTableTaskFactory(settings, Scope.SELECTEDNODES);
			addMenus(bc, tableTaskFactory, menu, "for selected nodes", "show compound table", 
			         Scope.SELECTEDNODES, "network", "1.2", true);

			CompoundEdgeTableTaskFactory tableEdgeTaskFactory = 
				new CompoundEdgeTableTaskFactory(settings, Scope.ALLEDGES);
			addMenus(bc, tableEdgeTaskFactory, menu, "for all edges", "show compound table", 
			         Scope.ALLEDGES, "network", "1.3", false);

			tableEdgeTaskFactory = 
				new CompoundEdgeTableTaskFactory(settings, Scope.SELECTEDEDGES);
			addMenus(bc, tableEdgeTaskFactory, menu, "for selected edges", "show compound table", 
			         Scope.SELECTEDEDGES, "network", "1.4", true);
		}

		// CompoundPopup -- needsGUI
		if (haveGUI) {
			String menu = "Show Structures[2.0]";
			TaskFactory popupTaskFactory = new CompoundNodePopupTaskFactory(settings, Scope.ALLNODES);
			addMenus(bc, popupTaskFactory, menu, "for all nodes", "show compound structures", 
			         Scope.ALLNODES, "network", "2.1", false);

			popupTaskFactory = new CompoundNodePopupTaskFactory(settings, Scope.SELECTEDNODES);
			addMenus(bc, popupTaskFactory, menu, "for selected nodes", "show compound structures", 
			         Scope.SELECTEDNODES, "network", "2.2", true);

			CompoundEdgePopupTaskFactory popupEdgeTaskFactory = new CompoundEdgePopupTaskFactory(settings, Scope.ALLEDGES);
			addMenus(bc, popupEdgeTaskFactory, menu, "for all edges", "show compound structures", 
			         Scope.ALLEDGES, "network", "2.2", false);

			popupEdgeTaskFactory = new CompoundEdgePopupTaskFactory(settings, Scope.SELECTEDEDGES);
			addMenus(bc, popupEdgeTaskFactory, menu, "for selected edges", "show compound structures", 
			         Scope.SELECTEDEDGES, "network", "2.3", true);
		}

		// Paint Structures on nodes
		// Only relevant if this renderer supports custom graphics
		if (lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1") != null) {
			// Register our customgraphics factory
			CyCustomGraphicsFactory chemVizCustomGraphicsFactory = new CustomGraphicsFactory(settings);
			registerService(bc, chemVizCustomGraphicsFactory, CyCustomGraphicsFactory.class, new Properties());

			String menu = "Paint Structures[3.0]";
			TaskFactory paintTaskFactory = 
				new PaintNodeStructuresTaskFactory(vmm, vmff, lex, settings, Scope.ALLNODES, false);
			addMenus(bc, paintTaskFactory, menu, "on all nodes", "paint structures", 
			         Scope.ALLNODES, "networkAndView", "3.1", false);

			paintTaskFactory = 
				new PaintNodeStructuresTaskFactory(vmm, vmff, lex, settings, Scope.SELECTEDNODES, false);
			addMenus(bc, paintTaskFactory, menu, "on selected nodes", "paint structures", 
			         Scope.SELECTEDNODES, "networkAndView", "3.2", true);
		}

		// Remove Structures from node
		// Only relevant if this renderer supports custom graphics
		if (lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1") != null) {
			String menu = "Remove Structures[4.0]";
			TaskFactory paintTaskFactory = 
				new PaintNodeStructuresTaskFactory(vmm, vmff, lex, settings, Scope.ALLNODES, true);
			addMenus(bc, paintTaskFactory, menu, "from all nodes", "remove structures", 
			         Scope.ALLNODES, "networkAndView", "4.1", false);

			paintTaskFactory = 
				new PaintNodeStructuresTaskFactory(vmm, vmff, lex, settings, Scope.SELECTEDNODES, true);
			addMenus(bc, paintTaskFactory, menu, "from selected nodes", "remove structures", 
			         Scope.SELECTEDNODES, "networkAndView", "4.2", true);
		}

		// Create attributes
		{
			String menu = "Create Attributes from Compound Descriptors[5.0]";

			TaskFactory createAttributesTaskFactory = 
				new CreateNodeAttributesTaskFactory(settings, Scope.ALLNODES);
			addMenus(bc, createAttributesTaskFactory, menu, "for all nodes", "create attributes", 
			         Scope.ALLNODES, "network", "5.1", false);

			createAttributesTaskFactory = 
				new CreateNodeAttributesTaskFactory(settings, Scope.SELECTEDNODES);
			addMenus(bc, createAttributesTaskFactory, menu, "for selected nodes", "create attributes", 
			         Scope.SELECTEDNODES, "network", "5.2", true);

			CreateEdgeAttributesTaskFactory createAttributesEdgeTaskFactory = 
				new CreateEdgeAttributesTaskFactory(settings, Scope.ALLEDGES);
			addMenus(bc, createAttributesEdgeTaskFactory, menu, "for all edges", "create attributes", 
			         Scope.ALLEDGES, "network", "5.3", false);

			createAttributesEdgeTaskFactory = 
				new CreateEdgeAttributesTaskFactory(settings, Scope.SELECTEDEDGES);
			addMenus(bc, createAttributesEdgeTaskFactory, menu, "for selected edges", "create attributes", 
			         Scope.SELECTEDEDGES, "network", "5.4", true);
		}

		// Clear cache

		// Calculate MCSS
		// At this point, if we don't have a GUI, all we can do is group.  
		// At some point, when we get observable tasks, we can consider 
		// returning the MCSS string
		{
			String menu = "Calculate Maximum Common SubStructure (MCSS)[6.0]";
			CyGroupManager groupManager = getService(bc, CyGroupManager.class);
			CyGroupFactory groupFactory = getService(bc, CyGroupFactory.class);
			TaskFactory calculateNodeMCSSTaskFactory = 
				new CalculateNodeMCSSTaskFactory(settings, groupManager, 
				                                 groupFactory, haveGUI, false, Scope.ALLNODES);
			addMenus(bc, calculateNodeMCSSTaskFactory, menu, "for all nodes", "calculate mcss", 
			         Scope.ALLNODES, "network", "6.1", false);

			calculateNodeMCSSTaskFactory = 
				new CalculateNodeMCSSTaskFactory(settings, groupManager, groupFactory, 
				                                 haveGUI, false, Scope.SELECTEDNODES);
			addMenus(bc, calculateNodeMCSSTaskFactory, menu, "for selected nodes", "calculate mcss", 
			         Scope.SELECTEDNODES, "network", "6.2", false);

			calculateNodeMCSSTaskFactory = 
				new CalculateNodeMCSSTaskFactory(settings, groupManager, groupFactory, 
				                                 haveGUI, true, Scope.SELECTEDNODES);
			addMenus(bc, calculateNodeMCSSTaskFactory, menu, "and group selected nodes", "calculate mcss", 
			         Scope.SELECTEDNODES, "network", "6.3", false);

			CalculateEdgeMCSSTaskFactory calculateEdgeMCSSTaskFactory = 
				new CalculateEdgeMCSSTaskFactory(settings, groupManager, groupFactory, 
				                                 haveGUI, false, Scope.ALLEDGES);
			addMenus(bc, calculateEdgeMCSSTaskFactory, menu, "for all edges", "calculate mcss", 
			         Scope.ALLEDGES, "network", "6.4", false);

			calculateEdgeMCSSTaskFactory = 
				new CalculateEdgeMCSSTaskFactory(settings, groupManager, groupFactory, 
				                                 haveGUI, false, Scope.SELECTEDEDGES);
			addMenus(bc, calculateEdgeMCSSTaskFactory, menu, "for selected edges", "calculate mcss", 
			         Scope.SELECTEDEDGES, "network", "6.5", true);
		}

		// Search
		{
			String menu = "Search using SMARTS[7.0]";
			TaskFactory searchNodesTaskFactory = 
				new SearchNodesTaskFactory(settings, haveGUI, Scope.ALLNODES);
			addMenus(bc, searchNodesTaskFactory, menu, "through all nodes", "search", 
			         Scope.ALLNODES, "network", "7.1", false);

			searchNodesTaskFactory = 
				new SearchNodesTaskFactory(settings, haveGUI, Scope.SELECTEDNODES);
			addMenus(bc, searchNodesTaskFactory, menu, "through selected nodes", "search", 
			         Scope.SELECTEDNODES, "network", "7.2", true);

			SearchEdgesTaskFactory searchEdgesTaskFactory = 
				new SearchEdgesTaskFactory(settings, haveGUI, Scope.ALLEDGES);
			addMenus(bc, searchEdgesTaskFactory, menu, "through all edges", "search", 
			         Scope.ALLEDGES, "network", "7.3", false);

			searchEdgesTaskFactory = 
				new SearchEdgesTaskFactory(settings, haveGUI, Scope.SELECTEDEDGES);
			addMenus(bc, searchEdgesTaskFactory, menu, "through selected edges", "search", 
			         Scope.SELECTEDEDGES, "network", "7.4", true);
		}

		// Create Similarity network
		{
			CyNetworkViewFactory viewFactory = getService(bc, CyNetworkViewFactory.class);
			CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
			CyNetworkViewManager networkViewManager = getService(bc, CyNetworkViewManager.class);

			String menu = "Create Similarity Network[8.0]";
			TaskFactory similarityTaskFactory = 
				new SimilarityNetworkTaskFactory(settings, viewFactory, networkManager, 
				                                 networkViewManager, vmm, true, Scope.ALLNODES);
			addMenus(bc, similarityTaskFactory, menu, "using all nodes", "create smilarity", 
			         Scope.ALLNODES, "networkAndView", "8.1", false);

			similarityTaskFactory = 
				new SimilarityNetworkTaskFactory(settings, viewFactory, networkManager, 
				                                 networkViewManager, vmm, true, Scope.SELECTEDNODES);
			addMenus(bc, similarityTaskFactory, menu, "using selected nodes", "create smilarity", 
			         Scope.SELECTEDNODES, "networkAndView", "8.2", true);
		}

		// Settings Menu
		ChemInfoSettingsTaskFactory settingsTaskFactory = 
			new ChemInfoSettingsTaskFactory(settings);
		Properties settingsMenuProperties = new Properties();
		settingsMenuProperties.setProperty(ID, "settingsMenuTaskFactory");
		settingsMenuProperties.setProperty(PREFERRED_MENU, "Apps.Cheminformatics Tools");
		settingsMenuProperties.setProperty(TITLE, "Settings...");
		settingsMenuProperties.setProperty(COMMAND, "settings");
		settingsMenuProperties.setProperty(COMMAND_NAMESPACE, "chemviz");
		settingsMenuProperties.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		settingsMenuProperties.setProperty(IN_TOOL_BAR, "true");
		settingsMenuProperties.setProperty(MENU_GRAVITY, "110.0");
		registerService(bc, settingsTaskFactory, TaskFactory.class, settingsMenuProperties);
		registerService(bc, settingsTaskFactory, ChemInfoSettingsTaskFactory.class, 
		                settingsMenuProperties);

		logger.info("ChemViz2 started");
	}

	private void addMenus(BundleContext bc, TaskFactory factory, String menu, String title, 
	                      String command, Scope scope, String enable, String gravity, 
	                      boolean exclusive) {
		String baseMenu = "Apps.Cheminformatics Tools";
		Properties properties = new Properties();
		properties.setProperty(PREFERRED_MENU, baseMenu+"."+menu);
		properties.setProperty(TITLE, title);
		properties.setProperty(COMMAND, command);
		properties.setProperty(ENABLE_FOR, enable);
		properties.setProperty(IN_TOOL_BAR, "true");
		properties.setProperty(COMMAND_NAMESPACE, "chemviz");
		properties.setProperty(MENU_GRAVITY, gravity);

		if (enable.equals("networkAndView"))
			registerService(bc, factory, NetworkViewTaskFactory.class, properties);
		else
			registerService(bc, factory, NetworkTaskFactory.class, properties);
		// If exclusive, we need to create a new properties and restructure
		// the menus.  This is to avoid slide of menus with a single choice...
		if (exclusive) {
			properties = new Properties();
			// These are all the same
			properties.setProperty(COMMAND, command);
			properties.setProperty(ENABLE_FOR, enable);
			properties.setProperty(IN_TOOL_BAR, "true");
			properties.setProperty(COMMAND_NAMESPACE, "chemviz");
			properties.setProperty(MENU_GRAVITY, gravity); // We can use the same gravity
			properties.setProperty(PREFERRED_MENU, baseMenu);
			properties.setProperty(TITLE, makeTitle(menu, title));
		}
		if (scope == Scope.SELECTEDNODES)
			registerService(bc, factory, NodeViewTaskFactory.class, properties);
		else if (scope == Scope.SELECTEDEDGES) {
			registerService(bc, factory, EdgeViewTaskFactory.class, properties);
		}
	}

	private String makeTitle(String menu, String title) {
		int offset = menu.indexOf('[');
		return menu.substring(0, offset)+" "+title;
	}
}

