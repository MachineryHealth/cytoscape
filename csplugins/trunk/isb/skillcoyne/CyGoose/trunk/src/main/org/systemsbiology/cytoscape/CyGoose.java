package org.systemsbiology.cytoscape;

import org.systemsbiology.cytoscape.GagglePlugin;
import org.systemsbiology.cytoscape.CyBroadcast;
import org.systemsbiology.cytoscape.dialog.GooseDialog;
import org.systemsbiology.cytoscape.visual.SeedMappings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;

import java.util.*;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.systemsbiology.gaggle.experiment.datamatrix.DataMatrix;
import org.systemsbiology.gaggle.network.*;
import org.systemsbiology.gaggle.boss.Boss;
import org.systemsbiology.gaggle.geese.Goose;

import cytoscape.*;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.calculators.GenericNodeFillColorCalculator;
import cytoscape.visual.mappings.*;
import cytoscape.visual.ui.VizMapUI;
import cytoscape.data.Semantics;
import cytoscape.data.CyAttributes;

import giny.view.NodeView;
import giny.view.EdgeView;
import giny.view.GraphView;

import giny.model.Node;
import giny.model.Edge;


/**
 * @author skillcoy
 * 
 * This goose is going to make Cytoscape act like there's a goose for each
 * network, so other networks outght to be able to direct broadcasts to specific
 * networks rather than just the selected one.
 * 
 */
public class CyGoose implements Goose
	{
	private String GooseName;
	private String GooseNetId;	

	private Boss GaggleBoss;
	private GooseDialog GDialog;
	
	private NodeAppearanceCalculator NAC; 
	private SeedMappings VisualMap;
	
	private String BroadcastId;
	private String targetGoose = "Boss";
	
	private boolean IsMovieAttributeMapped;
	private boolean AttributeChecked = false;

	private static void print(String S)
		{ System.out.println(S); }

	public CyGoose(GooseDialog GD, Boss boss)
		{
		GaggleBoss = boss;
		this.GDialog = GD;
		
		// deals with evertying but the broadcast actions
		this.addButtonActions();
		// creates broadcast actions
		CyBroadcast Broadcast = new CyBroadcast(GDialog, GaggleBoss, this);
		
		VisualStyle CurrentStyle = Cytoscape.getVisualMappingManager().getVisualStyle();
		NAC = CurrentStyle.getNodeAppearanceCalculator();
		VisualMap = new SeedMappings(NAC);
		}

	// Deselect all nodes/edges.  
	// TODO: There is no button for this on the gaggle toolbar for cytoscape 
  public void clearSelections() throws RemoteException
		{
		CyNetwork Net = Cytoscape.getNetwork( this.getNetworkId() );
		Net.unselectAllNodes();
		Net.unselectAllEdges();
		}

	// the boss does not call this method, all connections are handled by GagglePlugin
	public void connectToGaggle() throws Exception
		{ print("Use GagglePlugin methods for connection"); }

	// boss does not call this method
	public void doBroadcastList() throws RemoteException
		{ print("doBroadcastList() not implemented"); }

	// exactly what it says
	public void doExit() throws RemoteException, UnmarshalException
		{
		print("Exiting...");
		System.exit(0);
		}

	// hides goose
	public void doHide() throws RemoteException
		{ Cytoscape.getDesktop().setVisible(false); }

	// shows goose
	public void doShow() throws RemoteException
		{
		Cytoscape.getDesktop().setVisible(true);
		Cytoscape.getDesktop().toFront();
		if (!this.getNetworkId().equals("0")) Cytoscape.getDesktop().setFocus(this.getNetworkId());
		}

	/**
	 * @return Name of the goose
	 */
	public String getName() //throws RemoteException
		{ return this.GooseName; }

	/**
	 * @return Network id of goose
	 */
	public String getNetworkId()
		{ return this.GooseNetId; }

	/**
	 * @return Array of selected node ids
	 */
	public String[] getSelection() throws RemoteException
		{
		CyNetwork Net = Cytoscape.getNetwork( this.getNetworkId() );
		
		String[] Selected = new String[ Net.getSelectedNodes().size() ];
		Iterator<CyNode> NodeIter = Net.getSelectedNodes().iterator();
		for(int i=0; (i<Selected.length) && (NodeIter.hasNext()); i++)
			{
			CyNode CurrentNode = NodeIter.next();
			Selected[i] = CurrentNode.getIdentifier(); // this could change based on the broadcastId but currently not implemented
			}
		return Selected;
		}

	/**
	 * @return Total number of selected nodes
	 */
	public int getSelectionCount() throws RemoteException
		{
		CyNetwork Net = Cytoscape.getNetwork( this.getNetworkId() );		
		return Net.getSelectedNodes().size();
		}

	// this is how the 1.1 goose handles clusters, should be updated?
 	// TODO: select all nodes that match geneNames with attributes in conditionNames of species specified should be selected
	public void handleCluster(String species, String clusterName,
			String[] rowNames, String[] columnNames) throws RemoteException
		{ this.handleNameList(species, rowNames); }

	// adds attributes to an existing network 
	// this method is called for the "movies" from the DMV
	// TODO: check that the attribute being used is part of a NodeAppearanceCalculator
	// TODO: setup some default calculators for color/shape/size in cases where an attribute is not matched so a movie
	// will do something regardless
	public void handleMap(String species, String dataTitle, HashMap hashMap)
			throws RemoteException
		{
		GDialog.getMessageArea().setText(dataTitle);

		HashMap<String,ArrayList> AttrMap = hashMap;
		print("********handleMap(String, String, HashMap) \"dataTitle\"***********");
		CyNetwork Net = Cytoscape.getNetwork( this.getNetworkId() );
 		Cytoscape.getDesktop().setFocus(Net.getIdentifier());
 		Cytoscape.getDesktop().toFront();
 		
 		// if a user has anything previously selected it can obscure changes the movie makes
 		Net.unselectAllNodes();
 		Net.unselectAllEdges();
 		
    double UpperValue = 0;
    double LowerValue = 0;

		// iterate over the attribute hash, key=attribute name, value= attribute values ArrayList
    Iterator<String> attrKeyIter = AttrMap.keySet().iterator();
    while (attrKeyIter.hasNext())
      {
      String attrName = attrKeyIter.next();
      
      ArrayList AttrVals = (ArrayList) AttrMap.get(attrName);

      // check the array contains other arrays as expected
      // elements of ArrayLists: [array_of_node_names, array_of_values]
      String[] NodeIds = (String[]) AttrVals.get(0);

      Object nodeVals = AttrVals.get(1);
      Class nodeValsClass = nodeVals.getClass();
      if (!nodeValsClass.isArray())
        {
        System.err.println(this + ".handleMap() error: expecting an array of values!");
        return;
        }

      // determine the data type of attribute in hashMap (should be DOUBLE, STRING, BOOLEAN, or INT)
      String valType = nodeValsClass.getComponentType().getName();

      for (int i=0; i<NodeIds.length; i++)
        {
        CyNode selectNode = Cytoscape.getCyNode(NodeIds[i]);
        CyAttributes NodeAtts = Cytoscape.getNodeAttributes();

        // I can seed mappings currently only for DOUBLE's or INT's as these are continuous mappings
        if (selectNode != null)
          {
					NodeAtts.setAttribute(selectNode.getIdentifier(), Semantics.SPECIES, species);
					
					// set all attributes from the map
          if (valType.equals("double"))
            { // DOUBLE
            double[] Value = (double[]) AttrVals.get(1);
            NodeAtts.setAttribute(selectNode.getIdentifier(), attrName, new Double(Value[i]));
            // first node we'll just set the values for a base
            if (i == 0)
            	{
            	UpperValue = Value[i];
            	LowerValue = Value[i];
            	}
            else
            	{ 
            	if (Value[i] > UpperValue) UpperValue = Value[i];
            	if (Value[i] < LowerValue) LowerValue = Value[i];
            	}
            }
          else if (valType.equals("int"))
            { // INT
            int[] Value = (int[]) AttrVals.get(1);
            NodeAtts.setAttribute(selectNode.getIdentifier(), attrName, new Integer(Value[i]));
            if (i == 0)
            	{
            	UpperValue = Value[i];
            	LowerValue = Value[i];
            	}
            else
            	{ 
            	if (Value[i] > UpperValue) UpperValue = Value[i];
            	if (Value[i] < LowerValue) LowerValue = Value[i];
            	}
            }
          else if (valType.equals("boolean"))
            { // BOOLEAN
            boolean[] Value = (boolean[]) AttrVals.get(1);
            NodeAtts.setAttribute(selectNode.getIdentifier(), attrName, new Boolean(Value[i]));
            }
          else if (valType.equals("java.lang.String"))
            { // STRING
            String[] Value = (String[]) AttrVals.get(1);
            NodeAtts.setAttribute(selectNode.getIdentifier(), attrName, Value[i]);
            }
          else
            {
            System.err.println(this + ".handleMap() error: incompatible attribute data type (" + valType + ")");
            return;
            }
          }
        }
      UpperValue = UpperValue + (UpperValue * 0.2);
      LowerValue = LowerValue - (LowerValue * 0.2);
      
      this.VisualMap.seedMappings(attrName, UpperValue, LowerValue);
      }
    Cytoscape.getNetworkView(Net.getIdentifier()).redrawGraph(true, true);
		}

	public void handleMatrix(DataMatrix matrix) throws RemoteException
		{
    print("***** handleMatrix(DataMatrix) ****** ");
		CyNetwork Net = Cytoscape.getNetwork( this.getNetworkId() );
    Cytoscape.getDesktop().setFocus(Net.getIdentifier());
    
		String[] GeneNames = matrix.getRowTitles();
    String[] ConditionNames = matrix.getColumnTitles();

    for (int row=0; row<GeneNames.length; row++)
      {
      String NodeId = GeneNames[row];
			
      CyNode SelectNode = Cytoscape.getCyNode(NodeId); 

      if (SelectNode != null)
        {
        CyAttributes NodeAtts = Cytoscape.getNodeAttributes();
				Net.setSelectedNodeState( (Node)SelectNode, true );

        // set all experimental conditions as node attributes
        for (int col=0; col<ConditionNames.length; col++)
          {
          Double condVal = new Double(matrix.get(row, col));
          String attributeName = ConditionNames[col];
          if ( ( NodeAtts.hasAttribute(SelectNode.getIdentifier(),  attributeName) ) &&
               ( NodeAtts.getType(attributeName) != CyAttributes.TYPE_FLOATING ) )
            print("handleMatrix() Warning: \"" + attributeName + "\" is not of TYPE_FLOATING");
          else NodeAtts.setAttribute(SelectNode.getIdentifier(), attributeName, condVal);
          }
        }
      }

    // refresh network to flag selected nodes
    Cytoscape.getDesktop().setFocus(Net.getIdentifier());
		}



	// If this is sent to the default boss (Cytoscape, no network) nothing will happen
	// If this is sent to a network boss it will select the appropriate nodes
	public void handleNameList(String species, String[] names) throws RemoteException
		{
		print("**** handleNameList(String, String[]) *****");
		CyNetwork Net = Cytoscape.getNetwork( this.getNetworkId() );

		for ( String CurrentName: names )
			{
			CyNode SelectNode = Cytoscape.getCyNode(CurrentName);
			if ( (SelectNode != null) ) 
				{ Net.setSelectedNodeState( (Node)SelectNode, true ); }
 			}

		if (Net.getSelectedNodes().size() <= 0)
			{ 
			String Msg = "No matching nodes were found, if you think this is incorrect check that your nodes have the ." + 
										Semantics.SPECIES + " attribute set.";
			print(Msg);
			}

		// refresh network to flag selected nodes
		Cytoscape.getDesktop().setFocus(Net.getIdentifier());
		}



	// if broadcast the the generic Cytoscape goose a new network is created, if broadcast
	// to a network goose the interactions are added to the network and selected
	public void handleNetwork(String species, Network network) throws RemoteException
		{
    // create a network if none exists
    // network with ID=0 is the nullNetwork
		if (network.getInteractions().length > 0)
			{
	    if ( this.getNetworkId() == null || this.getNetworkId().equals("0") ) 
	    	{ handleNetwork(species, network, Cytoscape.createNetwork("Gaggle "+species), false); }
			else 
				{ handleNetwork(species, network, Cytoscape.getNetwork(this.getNetworkId()), true); }
			}
		else
			{
			GagglePlugin.showDialogBox("No interactions were broadcast to "+this.getName()+" please select some interactions.", 
																 "No Interactions", 
																	JOptionPane.INFORMATION_MESSAGE);
			}
		}


	public void handleNetwork(String species, Network GaggleNet, CyNetwork CyNet, boolean SelectNodes) 
		throws RemoteException
		{
    print("handleNetwork(String, Network, CyNetwork)");
    Interaction[] GaggleInteractions = GaggleNet.getInteractions();
			
    Collection<Node> srcCollection = new ArrayList<Node>();
    Collection<Node> targetCollection = new ArrayList<Node>();
    Collection<Edge> edgeCollection = new ArrayList<Edge>();

    for(Interaction CurrentInteraction: GaggleInteractions)
      {
      //Interaction CurrentInteraction = GaggleInteractions[i];
      String srcNodeName = CurrentInteraction.getSource();
      String targetNodeName = CurrentInteraction.getTarget();
      String interactionType = CurrentInteraction.getType();

      // flag source node (create new node if it doesn't exist)
      Node srcNode = (Node) Cytoscape.getCyNode(srcNodeName, true);
      CyNet.addNode(srcNode);
      srcCollection.add(srcNode);

      // flag target node (create new node if it doesn't exist)
      Node targetNode = (Node) Cytoscape.getCyNode(targetNodeName, true);
      CyNet.addNode(targetNode);
      targetCollection.add(targetNode);

      // flag edge (create a new edge if it's not found)
      Edge selectEdge = (Edge) Cytoscape.getCyEdge(srcNode, targetNode, Semantics.INTERACTION, interactionType, true);
      // add newly created edge to current network
      if (!CyNet.containsEdge(selectEdge)) CyNet.addEdge(selectEdge);
      edgeCollection.add(selectEdge);
      }

    // flag all selected nodes & edges
		if (SelectNodes)
			{
			CyNet.setSelectedNodeState(srcCollection, true);
			CyNet.setSelectedNodeState(targetCollection, true);
			CyNet.setSelectedEdgeState(edgeCollection, true);
			}

		//basic layout 
		this.layout( (GraphView)Cytoscape.getNetworkView(CyNet.getIdentifier()) );

    // refresh network to flag selected nodes
		Cytoscape.getDesktop().setFocus(CyNet.getIdentifier());
		}

		// provides a basic layout so nodes do not appear all on top of each other, this is the same as the layout
		// for an imported sif file (no layout info given)
		private void layout(GraphView view) 
			{
			double distanceBetweenNodes = 50.0d;
			int columns = (int) Math.sqrt(view.nodeCount());
			Iterator nodeViews = view.getNodeViewsIterator();
			double currX = 0.0d;
			double currY = 0.0d;
			int count = 0;
			while (nodeViews.hasNext()) 
				{
				NodeView nView = (NodeView) nodeViews.next();
				nView.setOffset(currX, currY);
				count++;
				if (count == columns) 
					{
					count = 0;
					currX = 0.0d;
					currY += distanceBetweenNodes;
					} 
				else  currX += distanceBetweenNodes; 
				}
			}
		

	// no point in this one
	public void setGeometry(int x, int y, int width, int height) throws RemoteException
		{ print("setGeometry() not implemented"); }

	// Used to set the goose network id to the cynetwork id
	public void setNetworkId(String Id) 
		{ this.GooseNetId = Id; }

	// sets the name goose is identified by in the boos
	public void setName(String newName) //throws RemoteException
		{ this.GooseName = newName; }

	// I think this is used to choose the identifier to broadcast/handle nodes by, currently not used
	public void setBroadcastId()
		{ this.BroadcastId = "ID"; }

	private void addButtonActions()
		{ 
		// set attribute to broadcast to other geese as the ID 
		/*
		GDialog.getIdButton().addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
					{ setBroadcastId(); }
			});
		*/

		// listen in on the getGooseBox() 
		GDialog.getGooseBox().addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
					{
					JComboBox tempCombo = (JComboBox) event.getSource();
					targetGoose = (String) tempCombo.getSelectedItem();
					}
			});

    // show selected goose 
    GDialog.getShowButton().addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent event)
          {
          try
            { GaggleBoss.show(targetGoose); }
          catch (Exception ex)
            { ex.printStackTrace(); }
          }
      });


		// hide selected goose 
		GDialog.getHideButton().addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
					{
					try
						{ GaggleBoss.hide(targetGoose); }
					catch (Exception ex)
						{ ex.printStackTrace(); }
					}
			});
		}

	}
