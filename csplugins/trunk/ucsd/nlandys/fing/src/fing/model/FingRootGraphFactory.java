package fing.model;

import giny.model.RootGraph;

/**
 * This class defines static methods that provide new instances
 * of giny.model.RootGraph objects.
 */
public final class FingRootGraphFactory
{

  // "No constructor".
  private FingRootGraphFactory() { }

  /**
   * Returns a new instance of giny.model.RootGraph.  Obviously, a new
   * RootGraph instance contains no nodes or edges.<p>
   * A secret feature is that the returned object not only implements
   * RootGraph - it also implements cytoscape.graph.dynamic.DynamicGraph.
   * In other words, you can cast the return value to DynamicGraph.  The
   * relationship between RootGraph node/edge indices and DynamicGraph nodes
   * and edges is they are complements of each other.  Complement is '~' in
   * Java.<p>
   * In addition to the RootGraph secretly implementing DyanmicGraph,
   * all of the GraphPerspective objects generated by the returned RootGraph
   * secretly implement cytoscape.graph.fixed.FixedGraph.  In other words,
   * all GraphPerspective objects that are part of this RootGraph system
   * can be cast to FixedGraph.  The relationship between GraphPerspective
   * node/edge indices (which are identical to RootGraph indices) and
   * FixedGraph nodes and edges is they are complements of each other.<p>
   * Below are time complexities of methods implemented:
   * <blockquote><table border=1 cellspacing=0 cellpadding=5>
   * <tr><th colspan=2>RootGraph</th></tr>
   * <tr><th>method</th><th>time complexity</th></tr>
   * <tr>
   * <td>createGraphPerspective(Node[], Edge[])</td>
   * <td></td>
   * </tr><tr>
   * <td>createGraphPerspective(int[], int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>ensureCapacity(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getNodeCount()</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeCount()</td>
   * <td></td>
   * </tr><tr>
   * <td>nodesIterator()</td>
   * <td></td>
   * </tr><tr>
   * <td>nodesList()</td>
   * <td></td>
   * </tr><tr>
   * <td>getNodeIndicesArray()</td>
   * <td></td>
   * </tr><tr>
   * <td>edgesIterator()</td>
   * <td></td>
   * </tr><tr>
   * <td>edgesList()</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeIndicesArray()</td>
   * <td></td>
   * </tr><tr>
   * <td>removeNode(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>removeNode(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>removeNodes(List)</td>
   * <td></td>
   * </tr><tr>
   * <td>removeNodes(int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>createNode()</td>
   * <td></td>
   * </tr><tr>
   * <td>createNode(Node[], Edge[])</td>
   * <td></td>
   * </tr><tr>
   * <td>createNode(GraphPerspective)</td>
   * <td></td>
   * </tr><tr>
   * <td>createNode(int[], int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>createNodes(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>removeEdge(Edge)</td>
   * <td></td>
   * </tr><tr>
   * <td>removeEdge(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>removeEdges(List)</td>
   * <td></td>
   * </tr><tr>
   * <td>removeEdges(int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>createEdge(Node, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>createEdge(Node, Node, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>createEdge(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>createEdge(int, int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>createEdges(int[], int[], boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>containsNode(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>containsEdge(Edge)</td>
   * <td></td>
   * </tr><tr>
   * <td>neighborsList(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>isNeighbor(Node, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>isNeighbor(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgeExists(Node, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgeExists(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeCount(Node, Node, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeCount(int, int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getAdjacentEdgeIndicesArray(int, boolean, boolean, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getConnectingEdgeIndicesArray(int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>getConnectingNodeIndicesArray(int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeIndicesArray(int, int, boolean, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgesList(Node, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgesList(int, int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeIndicesArray(int, int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getInDegree(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getInDegree(Node, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getInDegree(int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getOutDegree(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>getOutDegree(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getOutDegree(Node, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getOutDegree(int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getDegree(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>getDegree(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getIndex(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>getNode(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getIndex(Edge)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdge(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeSourceIndex(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeTargetIndex(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>isEdgeDirected(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>addMetaChild(Node, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>addNodeMetaChild(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>removeNodeMetaChild(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>isMetaParent(Node, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>isNodeMetaParent(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>metaParentsList(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>nodeMetaParentsList(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getNodeMetaParentIndicesArray(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>isMetaChild(Node, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>isNodeMetaChild(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>isNodeMetaChild(int, int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>nodeMetaChildrenList(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>nodeMetaChildrenList(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getNodeMetaChildIndicesArray(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getNodeMetaChildIndicesArray(int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getChildlessMetaDescendants(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>addMetaChild(Node, Edge)</td>
   * <td></td>
   * </tr><tr>
   * <td>addEdgeMetaChild(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>removeEdgeMetaChild(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>isMetaParent(Edge, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>isEdgeMetaParent(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>metaParentsList(Edge)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgeMetaParentsList(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeMetaParentIndicesArray(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>isMetaChild(Node, Edge)</td>
   * <td></td>
   * </tr><tr>
   * <td>isEdgeMetaChild(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgeMetaChildrenList(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgeMetaChildrenList(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeMetaChildIndicesArray(int)</td>
   * <td></td>
   * </tr>
   * </table><br /><table border=1 cellspacing=0 cellpadding=5>
   * <tr><th colspan=2>GraphPerspective</th></tr>
   * <tr><th>method</th><th>time complexity</th></tr>
   * <tr>
   * <td>addGraphPerspectiveChangeListener(GraphPerspectiveChangeListener)</td>
   * <td></td>
   * </tr><tr>
   * <td>removeGraphPerspectiveChangeListener(GraphPerspectiveChangeListener)</td>
   * <td></td>
   * </tr><tr>
   * <td>clone()</td>
   * <td></td>
   * </tr><tr>
   * <td>getRootGraph()</td>
   * <td></td>
   * </tr><tr>
   * <td>getNodeCount()</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeCount()</td>
   * <td></td>
   * </tr><tr>
   * <td>nodesIterator()</td>
   * <td></td>
   * </tr><tr>
   * <td>nodesList()</td>
   * <td></td>
   * </tr><tr>
   * <td>getNodeIndicesArray()</td>
   * <td></td>
   * </tr><tr>
   * <td>edgesIterator()</td>
   * <td></td>
   * </tr><tr>
   * <td>edgesList()</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeIndicesArray()</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeIndicesArray(int, int, boolean, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>hideNode(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>hideNode(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>hideNodes(List)</td>
   * <td></td>
   * </tr><tr>
   * <td>hideNodes(int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>restoreNode(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>restoreNode(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>restoreNodes(List)</td>
   * <td></td>
   * </tr><tr>
   * <td>restoreNodes(List, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>restoreNodes(int[], boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>restoreNodes(int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>hideEdge(Edge)</td>
   * <td></td>
   * </tr><tr>
   * <td>hideEdge(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>hideEdges(List)</td>
   * <td></td>
   * </tr><tr>
   * <td>hideEdges(int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>restoreEdge(Edge)</td>
   * <td></td>
   * </tr><tr>
   * <td>restoreEdge(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>restoreEdges(List)</td>
   * <td></td>
   * </tr><tr>
   * <td>restoreEdges(int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>containsNode(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>containsNode(Node, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>containsEdge(Edge)</td>
   * <td></td>
   * </tr><tr>
   * <td>containsEdge(Edge, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>join(GraphPerspective)</td>
   * <td></td>
   * </tr><tr>
   * <td>createGraphPerspective(Node[], Edge[])</td>
   * <td></td>
   * </tr><tr>
   * <td>createGraphPerspective(int[], int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>createGraphPerspective(Filter)</td>
   * <td></td>
   * </tr><tr>
   * <td>neighborsList(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>neighborsArray(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>isNeighbor(Node, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>isNeighbor(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgeExists(Node, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgeExists(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeCount(Node, Node, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeCount(int, int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgesList(Node, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgesList(int, int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeIndicesArray(int, int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getInDegree(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>getInDegree(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getInDegree(Node, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getInDegree(int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getOutDegree(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>getOutDegree(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getOutDegree(Node, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getOutDegree(int, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getDegree(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>getDegree(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getIndex(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>getNodeIndex(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getRootGraphNodeIndex(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getNode(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getIndex(Edge)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeIndex(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getRootGraphEdgeIndex(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdge(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeSourceIndex(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeTargetIndex(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>isEdgeDirected(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>isMetaParent(Node, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>isNodeMetaParent(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>metaParentsList(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>nodeMetaParentsList(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getNodeMetaParentIndicesArray(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>isMetaChild(Node, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>isNodeMetaChild(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>nodeMetaChildrenList(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>nodeMetaChildrenList(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getNodeMetaChildrenIndicesArray(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>isMetaParent(Edge, Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>isEdgeMetaParent(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>metaParentsList(Edge)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgeMetaParentsList(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeMetaParentIndicesArray(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>isMetaChild(Node, Edge)</td>
   * <td></td>
   * </tr><tr>
   * <td>isEdgeMetaChild(int, int)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgeMetaChildrenList(Node)</td>
   * <td></td>
   * </tr><tr>
   * <td>edgeMetaChildrenList(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getEdgeMetaChildIndicesArray(int)</td>
   * <td></td>
   * </tr><tr>
   * <td>getAdjacentEdgesList(Node, boolean, boolean, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getAdjacentEdgeIndicesArray(int, boolean, boolean, boolean)</td>
   * <td></td>
   * </tr><tr>
   * <td>getConnectingEdges(List)</td>
   * <td></td>
   * </tr><tr>
   * <td>getConnectingEdgeIndicesArray(int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>getConnectingNodeIndicesArray(int[])</td>
   * <td></td>
   * </tr><tr>
   * <td>createGraphPerspective(int[])</td>
   * <td></td>
   * </tr></table><br /><table border=1 cellspacing=0 cellpadding=5>
   * <tr><th colspan=2>Node</th></tr>
   * <tr><th>method</th><th>time complexity</th></tr>
   * <tr>
   * <td>getGraphPerspective()</td>
   * <td></td>
   * </tr><tr>
   * <td>setGraphPerspective(GraphPerspective)</td>
   * <td></td>
   * </tr><tr>
   * <td>getRootGraph()</td>
   * <td></td>
   * </tr><tr>
   * <td>getRootGraphIndex()</td>
   * <td></td>
   * </tr><tr>
   * <td>getIdentifier()</td>
   * <td></td>
   * </tr><tr>
   * <td>setIdentifier(String)</td>
   * <td></td>
   * </tr></table><br /><table border=1 cellspacing=0 cellpadding=5>
   * <tr><th colspan=2>Edge</th></tr>
   * <tr><th>method</th><th>time complexity</th></tr>
   * <tr>
   * <td>getSource()</td>
   * <td></td>
   * </tr><tr>
   * <td>getTarget()</td>
   * <td></td>
   * </tr><tr>
   * <td>isDirected()</td>
   * <td></td>
   * </tr><tr>
   * <td>getRootGraph()</td>
   * <td></td>
   * </tr><tr>
   * <td>getRootGraphIndex()</td>
   * <td></td>
   * </tr><tr>
   * <td>getIdentifier()</td>
   * <td></td>
   * </tr><tr>
   * <td>setIdentifier(String)</td>
   * <td></td>
   * </tr></table><br /><table border=1 cellspacing=0 cellpadding=5>
   * <tr><th colspan=2>GraphPerspectiveChangeEvent</th></tr>
   * <tr><th>method</th><th>time complexity</th></tr>
   * <tr>
   * <td>getSource()</td>
   * <td></td>
   * </tr><tr>
   * <td>getType()</td>
   * <td></td>
   * </tr><tr>
   * <td>isNodesRestoredType()</td>
   * <td></td>
   * </tr><tr>
   * <td>isEdgesRestoredType()</td>
   * <td></td>
   * </tr><tr>
   * <td>isNodesHiddenType()</td>
   * <td></td>
   * </tr><tr>
   * <td>isEdgesHiddenType()</td>
   * <td></td>
   * </tr><tr>
   * <td>isNodesSelectedType()</td>
   * <td></td>
   * </tr><tr>
   * <td>isNodesUnselectedType()</td>
   * <td></td>
   * </tr><tr>
   * <td>isEdgesSelectedType()</td>
   * <td></td>
   * </tr><tr>
   * <td>isEdgesUnselectedType()</td>
   * <td></td>
   * </tr><tr>
   * <td>getRestoredNodes()</td>
   * <td></td>
   * </tr><tr>
   * <td>getRestoredEdges()</td>
   * <td></td>
   * </tr><tr>
   * <td>getHiddenNodes()</td>
   * <td></td>
   * </tr><tr>
   * <td>getHiddenEdges()</td>
   * <td></td>
   * </tr><tr>
   * <td>getSelectedNodes()</td>
   * <td></td>
   * </tr><tr>
   * <td>getUnselectedNodes()</td>
   * <td></td>
   * </tr><tr>
   * <td>getSelectedEdges()</td>
   * <td></td>
   * </tr><tr>
   * <td>getUnselectedEdges()</td>
   * <td></td>
   * </tr><tr>
   * <td>getRestoredNodeIndices()</td>
   * <td></td>
   * </tr><tr>
   * <td>getRestoredEdgeIndices()</td>
   * <td></td>
   * </tr><tr>
   * <td>getHiddenNodeIndices()</td>
   * <td></td>
   * </tr><tr>
   * <td>getHiddenEdgeIndices()</td>
   * <td></td>
   * </tr><tr>
   * <td>getSelectedNodeIndices()</td>
   * <td></td>
   * </tr><tr>
   * <td>getUnselectedNodeIndices()</td>
   * <td></td>
   * </tr><tr>
   * <td>getSelectedEdgeIndices()</td>
   * <td></td>
   * </tr><tr>
   * <td>getUnselectedEdgeIndices()</td>
   * <td></td>
   * </tr></table></blockquote>
   */
  public final static RootGraph instantiateRootGraph()
  {
    return new FRootGraph();
  }

}
