package csplugins.layout;

import org.jgraph.JGraph;
import org.jgraph.graph.*;

import org.jgraph.layout.*;

import giny.model.*;
import giny.view.*;
import giny.util.*;

import javax.swing.*;

import java.util.*;

import java.awt.geom.Rectangle2D;
import java.awt.Color;

import cern.colt.map.*;

import cytoscape.view.*;

public class JGraphLayoutWrapper {

  public static final int ANNEALING       = 0;
  public static final int MOEN            = 1;
  public static final int CIRCLE_GRAPH    = 2;
  public static final int RADIAL_TREE     = 3;
  public static final int GEM             = 4;
  public static final int SPRING_EMBEDDED = 5;
  public static final int SUGIYAMA        = 6;
  public static final int TREE            = 7;

  int layout_type = 0;
  protected GraphView graphView;

  public JGraphLayoutWrapper ( GraphView view, int layout_type) {
    this.graphView = ( GraphView )view;
    this.layout_type = layout_type;
  }

  public void doLayout ( )
  {
    GraphPerspective perspective = graphView.getGraphPerspective();
    Map j_giny_node_map = new HashMap( PrimeFinder.nextPrime( perspective.getNodeCount() ) );
    Map giny_j_node_map = new HashMap( PrimeFinder.nextPrime( perspective.getNodeCount() ) );
    Map j_giny_edge_map = new HashMap( PrimeFinder.nextPrime( perspective.getEdgeCount() ) );

    Iterator node_iterator = perspective.nodesIterator();
    Iterator edge_iterator = perspective.edgesIterator();


    // Construct Model and Graph
    //
    GraphModel model = new DefaultGraphModel();
    JGraph graph = new JGraph(model);
   
    // Create Nested Map (from Cells to Attributes)
    //
    Map attributes = new Hashtable();

   
    Set cells = new HashSet();

    // create Vertices
    while ( node_iterator.hasNext() ) {

      // get the GINY node and node view
      giny.model.Node giny = ( giny.model.Node )node_iterator.next();
      NodeView node_view = graphView.getNodeView( giny );

      DefaultGraphCell jcell = new DefaultGraphCell( giny.getIdentifier() );
      
      // Set bounds
      Rectangle2D bounds = new Rectangle2D.Double( node_view.getXPosition(), 
                                                   node_view.getYPosition(),
                                                   node_view.getWidth(),
                                                   node_view.getHeight() );

      GraphConstants.setBounds( jcell.getAttributes(), bounds);

      j_giny_node_map.put( jcell, giny );
      giny_j_node_map.put( giny, jcell );

      cells.add( jcell );

    }
    
    while ( edge_iterator.hasNext() ) {
      
      giny.model.Edge giny = ( giny.model.Edge )edge_iterator.next();
      
      DefaultGraphCell j_source = ( DefaultGraphCell )giny_j_node_map.get( giny.getSource() );
      DefaultGraphCell j_target = ( DefaultGraphCell )giny_j_node_map.get( giny.getTarget() );

      DefaultPort source_port = new DefaultPort();
      DefaultPort target_port = new DefaultPort();

      j_source.add( source_port );
      j_target.add( target_port );

      source_port.setParent(j_source);
      target_port.setParent(j_target);

      // create the edge
      DefaultEdge jedge = new DefaultEdge();
      j_giny_edge_map.put( jedge, giny );


      // Connect Edge
      //
      ConnectionSet cs = new ConnectionSet( jedge, source_port, target_port);
      Object[] ecells = new Object[] { jedge, j_source, j_target };
      
      // Insert into Model
      //
      model.insert( ecells, attributes, cs, null, null);
      
      cells.add( jedge );

    }

    // now do the layout
    JGraphLayoutAlgorithm layout = null;

    if      ( layout_type == ANNEALING )
      layout = new AnnealingLayoutAlgorithm();
    else if ( layout_type == MOEN )
      layout = new MoenLayoutAlgorithm();
    else if ( layout_type == CIRCLE_GRAPH )
      layout = new CircleGraphLayout();
    else if ( layout_type == RADIAL_TREE )
      layout = new RadialTreeLayoutAlgorithm();
    else if ( layout_type == GEM )
      layout = new GEMLayoutAlgorithm( new AnnealingLayoutAlgorithm() );
    else if ( layout_type == SPRING_EMBEDDED )
      layout = new SpringEmbeddedLayoutAlgorithm();
    else if ( layout_type == SUGIYAMA )
      layout = new SugiyamaLayoutAlgorithm();
    else if ( layout_type == TREE )
      layout = new TreeLayoutAlgorithm();
    
    layout.run( graph, cells.toArray());
    GraphLayoutCache cache = graph.getGraphLayoutCache();

    CellView cellViews[] = graph.getGraphLayoutCache().getAllDescendants(
                             graph.getGraphLayoutCache().getRoots());
    for (int i = 0; i < cellViews.length; i++)
    {
      CellView cell_view = cellViews[i];
      if ( cell_view instanceof VertexView ) {
        // ok, we found a node
        Rectangle2D rect = graph.getCellBounds(cell_view.getCell());
        giny.model.Node giny = (giny.model.Node) j_giny_node_map.get(
	                                           cell_view.getCell());
        NodeView node_view = graphView.getNodeView( giny );
        node_view.setXPosition( rect.getX(), false );
        node_view.setYPosition( rect.getY(), false );
        node_view.setNodePosition( true );
        
       
      }
    }

    // I don't think that any of the current layouts have edge components, 
    // so I won't bother for now.

    model = null;
    graph = null;
    attributes = null;
    cells = null;
    System.gc();
    
  }
}
