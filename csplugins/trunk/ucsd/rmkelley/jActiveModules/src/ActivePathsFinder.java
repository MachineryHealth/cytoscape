package csplugins.jActiveModules;
//------------------------------------------------------------------------------

import java.io.*;
import java.util.*;
import giny.model.*;
import giny.view.*;
import cytoscape.view.*;
import cytoscape.data.*;
import cytoscape.data.servers.*;
import cytoscape.data.readers.*;
//import cytoscape.undo.*;
import csplugins.jActiveModules.data.*;
import csplugins.jActiveModules.dialogs.*;
//import cytoscape.vizmap.*;
//import cytoscape.layout.*;
import cytoscape.*;
import javax.swing.*;

/**
 *This class contains the main logic for finding Active Paths
 *The  important function is findActivePaths() which calls
 *the simulated annealing subroutine to find the active paths
 */
public class ActivePathsFinder{
  /**
   * See constructor
   */ 
  private String [] attrNames;
  /**
   * See constructor
   */ 
  private CyNetwork cyNetwork;
  /**
   *parameters for path finding
   */
  private ActivePathFinderParameters apfParams;
  /**
   *an array containing all of the nodes initially in the graph
   */
  private Node [] nodes;
  /**
   *This is a hashmap which maps from nodes to an array of edges
   *(Edge []). This is used to determine which edges belonged to
   *which nodes before any changes were made to the graph. This hash
   *map is then used to recover edges when reinserting nodes into a graph
   *it is initialized in setupScoring() and used in toggleNode()
   */
  HashMap node2edges;
  //Global Variables for the Greedy Search
  /**
   * Maps from a node to the best component found for that node
   */
  HashMap node2BestComponent;
  /**
   * The neighborhood for the current best component
   */
  //HashSet bestNeighborhood;
  ExpressionData expressionData;
  JFrame parentFrame;
  protected boolean convertToPval;
  protected static int DISPLAY_STEP = 50;
  protected static double MIN_SIG = 0.0000000000001;
  protected static double MAX_SIG = 1-MIN_SIG;

  /**
   * This is the only constructor for ActivePathsFinder. In order to find the paths, we need certain information.
   * @param attrNames The names of hte attributes which correspond to significance
   * @param cyNetwork The cyNetwork which contains our graph, should divorce this from the window before running
   * @param apfp The object specifying the parameters for this run
   * @param parentFrame The JFrame which is our parent window, if this is null, then we won't display any progress information
   */     
  public ActivePathsFinder(ExpressionData expressionData , String [] attrNames,CyNetwork cyNetwork, ActivePathFinderParameters apfp,JFrame parentFrame){
    this.expressionData = expressionData;
    this.parentFrame = parentFrame;
    this.attrNames = attrNames;
    this.cyNetwork = cyNetwork;
    apfParams = apfp;
     if(expressionData.getSignificanceType() == ExpressionData.LAMBDA){
       convertToPval = true;
      expressionData.convertLambdasToPvals();
    }else{
      convertToPval = false;
    }
  }
    
  /**
   * This function will determine a score for the nodes currently selected in the graph. It does not try to determine components
   * from these selected nodes, merely assuming they are all in the same connected component.
   * @param nodeList the node list
   * @return The score for the selected nodes
   */
  public double scoreList(List nodeList){
    setupScoring();
    // Vector result = new Vector();
    //     Iterator it = cytoscapeWindow.getView().getSelectedNodes().iterator();
    //     while(it.hasNext()){
    //       result.add(((NodeView)it.next()).getNode());
    //     }
    Component selected = new Component(nodeList);
    return selected.getScore();	
  }

  /**
   * In order to score the components, we need to set up certain data structures first. Mainly this includes seting up the z
   * scores table and the monte carlo correction. Chris sez it shouldn't be called a monte carlo correction, and I tend
   * to agree, but this has some historical inertia behind it (ie, it would involve changing maybe 6 lines of code, which is
   * simply unthinkable) These data structures are initialized as static data structures in the Component class, where the
   * scoring is actually done
   */
  private void setupScoring(){
    GraphPerspective perspective = cyNetwork.getGraphPerspective();
    //Here we initialize the z table. We use this data structure when we want to get an adjusted z score
    //based on how many conditions we are looking at.
    System.out.println("Initializing Z Table");
    Component.zStats = new ZStatistics(attrNames.length); 
    System.out.println("Done initializing Z Table");
	
    nodes = new Node[1];
    nodes = (Node [])(perspective.getRootGraph().nodesList().toArray(nodes));	

    //Edge [] e_array;
    //This has is used to store all the edges that were connected
    //to a particular node
    node2edges = new HashMap();	
    for(int i = 0;i<nodes.length;i++){
      //EdgeCursor edges = nodes[i].edges();
      //e_array = new Edge[edges.size()];
      //for(int j = 0;j<e_array.length;j++,edges.next()){
      //	e_array[j] = edges.edge();
      //}
      Edge [] temp = new Edge[0];
      node2edges.put(nodes[i],perspective.getAdjacentEdgesList(nodes[i],true,true,true).toArray(temp));
    }
    Component.node2edges = node2edges;
	
    //set up the HashMap which is used to map from nodes
    //to z values. At this point, we are mapping from the
    //p values for expression to z values
    System.out.println("Processing Expression Data into Hash");
    HashMap tempHash = new HashMap();
    //sort the conditionNames so everybody agrees what order the condition names should be in
    Arrays.sort(attrNames);
	
    //Component needs the condition names to return which conditions
    //yield significant scores
    Component.attrNames = attrNames;
    //Determine whether or not we want to correct for the size
    //of active paths
    Component.monteCorrection = apfParams.getMCboolean();
    Component.regionScoring = apfParams.getRegionalBoolean();
	
    //double max_zvalue = Double.NEGATIVE_INFINITY;
    GraphObjAttributes nodeAttributes = cyNetwork.getNodeAttributes();
    for(int i = 0;i<nodes.length;i++){
      double [] tempArray = new double[attrNames.length];
      for(int j = 0;j<attrNames.length;j++){
	mRNAMeasurement tempmRNA = expressionData.getMeasurement(nodeAttributes.getCanonicalName(nodes[i]),attrNames[j]);
	//Double significance = nodeAttributes.getDoubleValue(attrNames[i], nodeAttributes.getCanonicalName(nodes[i]));
	if(tempmRNA == null){
	  //if (significance == null) {
	  //we were unable to find any data for this node, something funny going on here, but we correct
	  //by pretending there was a p-value of 0.5
	  tempArray[j] = Component.zStats.oneMinusNormalCDFInverse(.5);
	}
	else{
	  double sigValue = tempmRNA.getSignificance();
	  if (sigValue < MIN_SIG) {
	    sigValue = MIN_SIG;
	    System.out.println("Warning: value for "+nodeAttributes.getCanonicalName(nodes[i])+" adjusted to "+MIN_SIG); 
	  } // end of if ()
	  if (sigValue > MAX_SIG) {
	    sigValue = MAX_SIG;
	    System.out.println("Warning: value for "+nodeAttributes.getCanonicalName(nodes[i])+" adjusted to "+MAX_SIG); 
	  } // end of if ()
	  //transform the p-value into a z-value and store it in the array of z scores for this particular node
	  tempArray[j] = Component.zStats.oneMinusNormalCDFInverse(sigValue);
	  //tempArray[j] = Component.zStats.oneMinusNormalCDFInverse(significance.doubleValue());
	  //if(tempArray[j] > max_zvalue){
	  //  max_zvalue = tempArray[j];
	  //}
	}
      }
      tempHash.put(nodes[i],tempArray);
    }
    //This hash is used by Component to be able to determine the significance
    //for a particular gene
    Component.exHash = tempHash;
    System.out.println("Done processing into Hash");
    
     
    //Initialize the param statistics object. The pStats object uses randomized methods ot determine the 
    //mean and standard deviation for networks of size 1 through n.
    Component.pStats = new ParamStatistics(new Random(apfParams.getRandomSeed()),Component.zStats);
    //The statistics object is required fro the component scoring function
    //We want to use a monte carlo correction
    if(apfParams.getMCboolean()){
      boolean failed = false;
      //and we want to load the state from a file
      if(apfParams.getToUseMCFile()){
	//read in the monte carlo file, it is stored as a serialized ParamStatistics object
	System.out.println("Trying to read monte carlo file");
	try{
	  FileInputStream fis = new FileInputStream(apfParams.getMcFileName());
	  ObjectInputStream ois = new ObjectInputStream(fis);
	  Component.pStats = (ParamStatistics) ois.readObject();		
	  ois.close();
	  if(Component.pStats.getNodeNumber() != nodes.length){
	    //whoops, the file we loaded doesn't look like it contains the correct information for the set
	    //of nodes we are dealing with, user specified a bad file, I hope he feels shame
	    System.out.println("Monte Carlo file calculated for incorrect number of nodes. Using correct file?");
	    failed = true;
	    throw new Exception("wrong number of nodes");
	  }
	}catch(Exception e){
	  System.out.println("Loading monte carlo file failed"+e);
	  failed = true;
	}
      }
	    
      if(failed || !apfParams.getToUseMCFile()){
	System.out.println("Initializing monte carlo state");
	MyProgressMonitor progressMonitor = null;
	if (parentFrame != null) {
	  progressMonitor = new MyProgressMonitor(parentFrame, "Sampling Mean and Standard Deviation","",0,ParamStatistics.DEFAULT_ITERATIONS);
	} // end of if ()
	
	//Component.pStats.calculateMeanAndStd(nodes,ParamStatistics.DEFAULT_ITERATIONS,apfParams.getMaxThreads(), new MyProgressMonitor(cytoscapeWindow, "Sampling Mean and Standard Deviation","",0,ParamStatistics.DEFAULT_ITERATIONS));
	Component.pStats.calculateMeanAndStd(nodes,ParamStatistics.DEFAULT_ITERATIONS,apfParams.getMaxThreads(),progressMonitor);
	System.out.println("Finished initializing monte carlo state");
	    
	System.out.println("Trying to save monte carlo state");
	try{
	  FileOutputStream fos = new FileOutputStream("last.mc");
	  ObjectOutputStream oos = new ObjectOutputStream(fos);
	  oos.writeObject(Component.pStats);
	  oos.close();
	  System.out.println("Saved monte carlo state to last.mc");
	}
	catch(Exception e){
	  System.out.println("Failed to save monte carlo state"+e);
	}
      }
    }

  }

  /**
   *this function restores all the graphs to the nodes that have
   *been removed. The nodes are removed instead of hidden because it
   *seems like the y files component finder goes wonky if the nodes
   *are only hidden
   */
  private void restoreNodes(){
    //for each node not present in the graph, go through and toggle
    //its state back
    GraphPerspective graph = cyNetwork.getGraphPerspective();
    for(int i=0;i<nodes.length;i++){
      if(!graph.containsNode(nodes[i],false)){
	graph.restoreNode(nodes[i]);
	Edge [] e_array = (Edge[])node2edges.get(nodes[i]);
	for(int j=0;j<e_array.length;j++){
	  Edge e = e_array[j];
	  if(graph.containsNode(e.getSource(),false) && graph.containsNode(e.getTarget(),false)){
	    graph.restoreEdge(e);
	  }
	}
      }
    }
  }
      
  /**
   *This is hte method called to determine the activePaths. Its operation depends
   *on the parameters specified in hte activePathsFinderParameters object passed
   *into the constructor.
   */
  public Component [] findActivePaths(){
    setupScoring();
    Vector comps;
    if(apfParams.getSearchDepth()>0){
      //this will read the parameters out of apfParams and 
      //store the result into bestComponnet
      System.out.println("Starting greedy search");
      runGreedySearch();
      System.out.println("Greedy search finished");
	    
      //after the call to run greedy search, each node is associated
      //with the best scoring component to which it belongs. Need to
      //take the values from this hashmap and put them into a vector
      //so that there are no duplicates.
      comps = new Vector(new HashSet(node2BestComponent.values()));
      Collections.sort(comps);
	   
    }
    else{
      System.out.println("Starting simulated annealing");
      Vector resultPaths = new Vector();
      MyProgressMonitor progress = null;
      if (parentFrame != null) {
	progress = new MyProgressMonitor(parentFrame,"Running Simulated Annealing","",0,(int)Math.ceil(apfParams.getTotalIterations()/(double)DISPLAY_STEP));
      } // end of if ()
      Thread thread = new SimulatedAnnealingSearchThread(cyNetwork.getGraphPerspective(),resultPaths,node2edges,nodes,apfParams,progress);
      thread.start();
      try{
	thread.join();
      }catch(Exception e){
	System.out.println("Failed to rejoin simulated annealing search thread");
	System.exit(-1);
      }
      if (progress != null) {
	progress.close();
      } // end of if ()
      System.out.println("Finished simulated annealing run");
      if(apfParams.getToQuench()){
	System.out.println("Starting quenching run");
	SortedVector oldPaths = new SortedVector(resultPaths);
	resultPaths = new Vector();
	thread = new QuenchingSearchThread(cyNetwork.getGraphPerspective(),resultPaths,node2edges,nodes,apfParams,oldPaths);
	thread.start();
	try{
	  thread.join();
	}catch(Exception e){
	  System.out.println("Failed to rejoin Quenching Search Thread");
	  System.exit(-1);
	}
	System.out.println("Quenching run finished");
	
      }
      comps = new Vector(resultPaths);
      restoreNodes();
    }

	
    //the old code liked to deal with these ActivePaths objects which it used
    //to report back the ActivePaths back to the user. I didn't want to deal with
    //them directly, so I have to map back from the Component object to the ActivePath
    //object so the information can be conveyed to the user


    //System.out.println("Mapping component objects into ActivePaths");
    //ActivePath [] activePaths = new ActivePath[apfParams.getNumberOfPaths()];
    //for(int i = 0;i<apfParams.getNumberOfPaths();i++){
    //    Component current = ((Component)comps.get(i));
    //    System.out.println(current);
    //    Vector nodeVector = current.getNodes();
    //    String [] genes = new String[nodeVector.size()];
    //    for(int j = 0;j<nodeVector.size();j++){
    //	genes[j] = (cytoscapeWindow.getCanonicalNodeName((Node)nodeVector.elementAt(j)));
    //	}
    //    activePaths[i] = new ActivePath(current.getScore(),genes,current.getSignificantConditions());
    //    }
    //System.out.println("Done mapping component objects");
    //restoreNodes();
    Component [] temp = new Component[0];
    int size = Math.min(comps.size(),apfParams.getNumberOfPaths());
    return (Component [])comps.subList(0,size).toArray(temp);
  }
    
  /**
   * Runs the greedy search algorithm. This function will run a greedy
   * search iteratively using each node of the graph as a starting point
   */
  private void runGreedySearch(){
    runGreedySearch(cyNetwork.getGraphPerspective().nodesList());
  }

  private void runGreedySearch(List seedList){
    //initialize global best score
    node2BestComponent = new HashMap();
	
    //List seedList = null;
    //initialize the list of nodes we will start searching from
    //if(apfParams.getSearchFromNodes()){
    //search from a subset of nodes that the user has selected
    // seedList = cytoscapeWindow.getView().getSelectedNodes();
    //}
    //else{
    //  seedList = cytoscapeWindow.getView().getGraphPerspective().nodesList();
    //}
	
    //run a greedy search using each node in our starting
    //list in a starting point
    MyProgressMonitor progressMonitor = null;
    if (parentFrame != null) {
      progressMonitor = new MyProgressMonitor(parentFrame,"Performing Greedy Search","",0,seedList.size());
    }
    int number_threads = apfParams.getMaxThreads();
    Vector threadVector = new Vector();
    for(int i=0;i<number_threads;i++){
      GreedySearchThread gst = new GreedySearchThread(cyNetwork.getGraphPerspective(),apfParams.getMaxDepth(),apfParams.getSearchDepth(),seedList,progressMonitor,node2BestComponent,nodes);
      gst.start();
      threadVector.add(gst);
    }
	
    //wait for the threads to finish
    Iterator it = threadVector.iterator();
    while(it.hasNext()){
      try{
	((Thread)it.next()).join();
      }catch(Exception e){
	System.out.println("Failed to join thread");
	System.exit(-1);
      }
    }
    if (progressMonitor!=null) {
      progressMonitor.close();
    }
  }
    
}





