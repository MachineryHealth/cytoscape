package ding.view;

import cytoscape.geom.spacial.MutableSpacialIndex2D;
import giny.model.GraphPerspective;
import giny.model.Edge;
import giny.model.Node;
import giny.model.RootGraph;
import giny.view.EdgeView;
import giny.view.GraphView;
import giny.view.GraphViewChangeListener;
import giny.view.NodeView;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Font;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

// Package visible class.
class DGraphView implements GraphView
{

  final Object m_lock = new Object();
  final float[] m_extentsBuff = new float[4];
  GraphPerspective m_perspective;

  // Throughout this code I am assuming that nodes or edges are never
  // removed from the underlying RootGraph.  This assumption was made in the
  // old GraphView implementation.  Removal from the RootGraph is the only
  // thing that can affect m_drawPersp that is beyond our control.
  GraphPerspective m_drawPersp;

  MutableSpacialIndex2D m_spacial;
  DNodeDetails m_nodeDetails;
  DEdgeDetails m_edgeDetails;
  HashMap m_nodeViewMap;
  HashMap m_edgeViewMap;

  private static class InnerCanvas extends Canvas
  {
  }

  DGraphView(GraphPerspective perspective)
  {
    m_perspective = perspective;
    m_drawPersp = m_perspective.getRootGraph().createGraphPerspective
      ((int[]) null, (int[]) null);
    m_nodeViewMap = new HashMap();
    m_edgeViewMap = new HashMap();
  }

  public GraphPerspective getGraphPerspective()
  {
    return m_perspective;
  }

  public boolean nodeSelectionEnabled()
  {
    return false;
  }

  public boolean edgeSelectionEnabled()
  {
    return false;
  }

  public void enableNodeSelection()
  {
  }

  public void disableNodeSelection()
  {
  }

  public void enableEdgeSelection()
  {
  }

  public void disableEdgeSelection()
  {
  }

  public int[] getSelectedNodeIndices()
  {
    return null;
  }

  public List getSelectedNodes()
  {
    return null;
  }

  public int[] getSelectedEdgeIndices()
  {
    return null;
  }

  public List getSelectedEdges()
  {
    return null;
  }

  public void addGraphViewChangeListener(GraphViewChangeListener l)
  {
  }

  public void removeGraphViewChangeListener(GraphViewChangeListener l)
  {
  }

  public void setBackgroundPaint(Paint paint)
  {
  }

  public Paint getBackgroundPaint()
  {
    return null;
  }

  public Component getComponent()
  {
    return null;
  }

  public NodeView addNodeView(int nodeInx)
  {
    synchronized (m_lock) {
      final NodeView oldView =
        (NodeView) m_nodeViewMap.get(new Integer(nodeInx));
      if (oldView != null) { return oldView; }
      if (m_drawPersp.restoreNode(nodeInx) == 0) {
        if (m_drawPersp.getNode(nodeInx) != null) {
          throw new IllegalStateException
            ("something weird is going on - node already existed in graph " +
             "but a view for it did not exist (debug)"); }
        throw new IllegalArgumentException
          ("node index specified does not exist in underlying RootGraph"); }
      final NodeView returnThis = new DNodeView(this, nodeInx);
      m_nodeViewMap.put(new Integer(nodeInx), returnThis);
      m_spacial.insert(~nodeInx, -10.0f, -10.0f, 10.0f, 10.0f);
      return returnThis; }
  }

  public EdgeView addEdgeView(int edgeInx)
  {
    synchronized (m_lock) {
      final EdgeView oldView =
        (EdgeView) m_edgeViewMap.get(new Integer(edgeInx));
      if (oldView != null) { return oldView; }
      final Edge edge = m_drawPersp.getRootGraph().getEdge(edgeInx);
      if (edge == null) {
        throw new IllegalArgumentException
          ("edge index specified does not exist in underlying RootGraph"); }
      addNodeView(edge.getSource().getRootGraphIndex());
      addNodeView(edge.getTarget().getRootGraphIndex());
      if (m_drawPersp.restoreEdge(edgeInx) == 0) {
        if (m_drawPersp.getEdge(edgeInx) != null) {
          throw new IllegalStateException
            ("something weird is going on - edge already existed in graph " +
             "but a view for it did not exist (debug)"); }
        throw new IllegalArgumentException
          ("edge index specified does not exist in underlying RootGraph"); }
      final EdgeView returnThis = new DEdgeView(this, edgeInx);
      m_edgeViewMap.put(new Integer(edgeInx), returnThis);
      return returnThis; }
  }

  public EdgeView addEdgeView(String className, int edgeInx)
  {
    throw new UnsupportedOperationException("not implemented");
  }

  public NodeView addNodeView(String className, int nodeInx)
  {
    throw new UnsupportedOperationException("not implemented");
  }

  public NodeView addNodeView(int nodeInx, NodeView replacement)
  {
    throw new UnsupportedOperationException("not implemented");
  }

  public NodeView removeNodeView(NodeView nodeView)
  {
    return removeNodeView(nodeView.getRootGraphIndex());
  }

  public NodeView removeNodeView(Node node)
  {
    return removeNodeView(node.getRootGraphIndex());
  }

  public NodeView removeNodeView(int nodeInx)
  {
    synchronized (m_lock) {
      final int[] edges =
        m_drawPersp.getAdjacentEdgeIndicesArray(nodeInx, true, true, true);
      if (edges == null) { return null; }
      for (int i = 0; i < edges.length; i++) {
        removeEdgeView(edges[i]); }
      final DNodeView returnThis =
        (DNodeView) m_nodeViewMap.remove(new Integer(nodeInx));
      m_drawPersp.hideNode(nodeInx);
      m_nodeDetails.unregisterNode(~nodeInx);
      m_spacial.delete(~nodeInx);
      returnThis.m_view = null;
      return returnThis; }
  }

  public EdgeView removeEdgeView(EdgeView edgeView)
  {
    return removeEdgeView(edgeView.getRootGraphIndex());
  }

  public EdgeView removeEdgeView(Edge edge)
  {
    return removeEdgeView(edge.getRootGraphIndex());
  }

  public EdgeView removeEdgeView(int edgeInx)
  {
    synchronized (m_lock) {
      final DEdgeView returnThis =
        (DEdgeView) m_edgeViewMap.remove(new Integer(edgeInx));
      if (returnThis == null) { return returnThis; }
      m_drawPersp.hideEdge(edgeInx);
      m_edgeDetails.unregisterEdge(~edgeInx);
      returnThis.m_view = null;
      return returnThis; }
  }

  public String getIdentifier()
  {
    return null;
  }

  public void setIdentifier(String id)
  {
  }

  public double getZoom()
  {
    return 0.0d;
  }

  public void setZoom(double zoom)
  {
  }

  public void fitContent()
  {
  }

  public void updateView()
  {
  }

  public RootGraph getRootGraph()
  {
    return m_perspective.getRootGraph();
  }

  public Iterator getNodeViewsIterator()
  {
    synchronized (m_lock) { return m_nodeViewMap.values().iterator(); }
  }

  public int getNodeViewCount()
  {
    synchronized (m_lock) { return m_nodeViewMap.size(); }
  }

  public int getEdgeViewCount()
  {
    synchronized (m_lock) { return m_edgeViewMap.size(); }
  }

  public NodeView getNodeView(Node node)
  {
    return getNodeView(node.getRootGraphIndex());
  }

  public NodeView getNodeView(int nodeInx)
  {
    synchronized (m_lock) {
      return (NodeView) m_nodeViewMap.get(new Integer(nodeInx)); }
  }

  public List getEdgeViewsList()
  {
    synchronized (m_lock) {
      final ArrayList returnThis = new ArrayList(m_edgeViewMap.size());
      final Iterator values = m_edgeViewMap.values().iterator();
      while (values.hasNext()) {
        returnThis.add(values.next()); }
      return returnThis; }
  }

  public List getEdgeViewsList(Node oneNode, Node otherNode)
  {
    synchronized (m_lock) {
      List edges = m_drawPersp.edgesList(oneNode.getRootGraphIndex(),
                                         otherNode.getRootGraphIndex(), true);
      if (edges == null) { return null; }
      final ArrayList returnThis = new ArrayList();
      Iterator it = edges.iterator();
      while (it.hasNext()) {
        Edge e = (Edge) it.next();
        returnThis.add(getEdgeView(e)); }
      return returnThis; }
  }

  public List getEdgeViewsList(int oneNodeInx, int otherNodeInx,
                               boolean includeUndirected)
  {
    synchronized (m_lock) {
      List edges = m_drawPersp.edgesList(oneNodeInx, otherNodeInx,
                                         includeUndirected);
      if (edges == null) { return null; }
      final ArrayList returnThis = new ArrayList();
      Iterator it = edges.iterator();
      while (it.hasNext()) {
        Edge e = (Edge) it.next();
        returnThis.add(getEdgeView(e)); }
      return returnThis; }
  }

  public EdgeView getEdgeView(int edgeInx)
  {
    return null;
  }

  public Iterator getEdgeViewsIterator()
  {
    return null;
  }

  public EdgeView getEdgeView(Edge edge)
  {
    return null;
  }

  public int edgeCount()
  {
    return 0;
  }

  public int nodeCount()
  {
    return 0;
  }

  public boolean hideGraphObject(Object obj)
  {
    return false;
  }

  public boolean showGraphObject(Object obj)
  {
    return false;
  }

  public boolean hideGraphObjects(List objects)
  {
    return false;
  }

  public boolean showGraphObjects(List objects)
  {
    return false;
  }

  public Object[] getContextMethods(String className, boolean plusSuperclass)
  {
    return null;
  }

  public Object[] getContextMethods(String className, Object[] methods)
  {
    return null;
  }

  public boolean addContextMethod(String className, String methodClassName,
                                  String methodName, Object[] args,
                                  ClassLoader loader)
  {
    return false;
  }

  public void setAllNodePropertyData(int nodeInx, Object[] data)
  {
  }

  public Object[] getAllNodePropertyData(int nodeInx)
  {
    return null;
  }

  public void setAllEdgePropertyData(int edgeInx, Object[] data)
  {
  }

  public Object[] getAllEdgePropertyData(int edgeInx)
  {
    return null;
  }

  public Object getNodeObjectProperty(int nodeInx, int property)
  {
    return null;
  }

  public boolean setNodeObjectProperty(int nodeInx, int property, Object value)
  {
    return false;
  }

  public Object getEdgeObjectProperty(int edgeInx, int property)
  {
    return null;
  }

  public boolean setEdgeObjectProperty(int edgeInx, int property, Object value)
  {
    return false;
  }

  public double getNodeDoubleProperty(int nodeInx, int property)
  {
    return 0.0d;
  }

  public boolean setNodeDoubleProperty(int nodeInx, int property, double val)
  {
    return false;
  }

  public double getEdgeDoubleProperty(int edgeInx, int property)
  {
    return 0.0d;
  }

  public boolean setEdgeDoubleProperty(int edgeInx, int property, double val)
  {
    return false;
  }

  public float getNodeFloatProperty(int nodeInx, int property)
  {
    return 0.0f;
  }

  public boolean setNodeFloatProperty(int nodeInx, int property, float value)
  {
    return false;
  }

  public float getEdgeFloatProperty(int edgeInx, int property)
  {
    return 0.0f;
  }

  public boolean setEdgeFloatProperty(int edgeInx, int property, float value)
  {
    return false;
  }

  public boolean getNodeBooleanProperty(int nodeInx, int property)
  {
    return false;
  }

  public boolean setNodeBooleanProperty(int nodeInx, int property, boolean val)
  {
    return false;
  }

  public boolean getEdgeBooleanProperty(int edgeInx, int property)
  {
    return false;
  }

  public boolean setEdgeBooleanProperty(int edgeInx, int property, boolean val)
  {
    return false;
  }

  public int getNodeIntProperty(int nodeInx, int property)
  {
    return 0;
  }

  public boolean setNodeIntProperty(int nodeInx, int property, int value)
  {
    return false;
  }

  public int getEdgeIntProperty(int edgeInx, int property)
  {
    return 0;
  }

  public boolean setEdgeIntProperty(int edgeInx, int property, int value)
  {
    return false;
  }

}
