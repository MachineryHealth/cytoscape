package fing.model;

import cytoscape.graph.dynamic.DynamicGraph;
import cytoscape.graph.dynamic.util.DynamicGraphFactory;
import cytoscape.util.intr.ArrayIntIterator;
import cytoscape.util.intr.IntArray;
import cytoscape.util.intr.IntEnumerator;
import cytoscape.util.intr.IntIntHash;
import cytoscape.util.intr.IntIterator;
import cytoscape.util.intr.IntHash;
import cytoscape.util.intr.IntIterator;
import cytoscape.util.intr.IntStack;
import cytoscape.util.intr.MinIntHeap;

import giny.model.Edge;
import giny.model.GraphPerspective;
import giny.model.Node;
import giny.model.RootGraph;
import giny.model.RootGraphChangeEvent;
import giny.model.RootGraphChangeListener;

import java.util.Iterator;
import java.util.NoSuchElementException;

// Package visible class.  Use factory to get instance.
// This implementation of giny.model is safe to use with a single thread only.
class FRootGraph implements RootGraph, DynamicGraph
{

  ////////////////////////////////////
  // BEGIN: Implements DynamicGraph //
  ////////////////////////////////////
  public IntEnumerator nodes() { return m_graph.nodes(); }
  public IntEnumerator edges() { return m_graph.edges(); }
  public int nodeCreate() { return ~createNode(); }
  public boolean nodeRemove(int node) { return removeNode(~node) != 0; }
  public int edgeCreate(int sourceNode, int targetNode, boolean directed) {
    return ~createEdge(~sourceNode, ~targetNode, directed); }
  public boolean edgeRemove(int edge) { return removeEdge(~edge) != 0; }
  public boolean nodeExists(int node) { return m_graph.nodeExists(node); }
  public byte edgeType(int edge) { return m_graph.edgeType(edge); }
  public int edgeSource(int edge) { return m_graph.edgeSource(edge); }
  public int edgeTarget(int edge) { return m_graph.edgeTarget(edge); }
  public IntEnumerator edgesAdjacent(int node, boolean outgoing,
                                     boolean incoming, boolean undirected) {
    return m_graph.edgesAdjacent(node, outgoing, incoming, undirected); }
  public IntIterator edgesConnecting(int node0, int node1,
                                     boolean outgoing, boolean incoming,
                                     boolean undirected) {
    return m_graph.edgesConnecting(node0, node1, outgoing,
                                   incoming, undirected); }
  //////////////////////////////////
  // END: Implements DynamicGraph //
  //////////////////////////////////

  // Not specified by giny.model.RootGraph.  GraphPerspective implementation
  // in this package relies on this method.
  // ATTENTION!  Before making this method public you need to change the
  // event implementations to return copied arrays in their methods instead
  // of always returning the same array reference.  Also you need to enable
  // create node and create edge events - currently only remove node and
  // remove edge events are fired.
  void addRootGraphChangeListener(RootGraphChangeListener listener)
  { // This method is not thread safe; synchronize on an object to make it so.
    m_lis = RootGraphChangeListenerChain.add(m_lis, listener);
  }

  // Not specified by giny.model.RootGraph.  GraphPerspective implementation
  // in this package relies on this method.
  // ATTENTION!  Before making this method public you need to change the
  // event implementations to return copied arrays in their methods instead
  // of always returning the same array reference.  Also you need to enable
  // create node and create edge events - currently only remove node and
  // remove edge events are fired.
  void removeRootGraphChangeListener(RootGraphChangeListener listener)
  { // This method is not thread safe; synchronize on an object to make it so.
    m_lis = RootGraphChangeListenerChain.remove(m_lis, listener);
  }

  public GraphPerspective createGraphPerspective(Node[] nodes, Edge[] edges) {
    final Node[] nodeArr = ((nodes != null) ? nodes : new Node[0]);
    final Edge[] edgeArr = ((edges != null) ? edges : new Edge[0]);
    final RootGraph root = this;
    try {
      return new FGraphPerspective
        (this,
         new IntIterator() {
           private int index = 0;
           public boolean hasNext() { return index < nodeArr.length; }
           public int nextInt() {
             if (nodeArr[index] == null ||
                 nodeArr[index].getRootGraph() != root)
               throw new IllegalArgumentException();
             return nodeArr[index++].getRootGraphIndex(); } },
         new IntIterator() {
           private int index = 0;
           public boolean hasNext() { return index < edgeArr.length; }
           public int nextInt() {
             if (edgeArr[index] == null ||
                 edgeArr[index].getRootGraph() != root)
               throw new IllegalArgumentException();
             return edgeArr[index++].getRootGraphIndex(); } }); }
    catch (IllegalArgumentException exc) { return null; } }

  public GraphPerspective createGraphPerspective(int[] nodeInx,
                                                 int[] edgeInx) {
    if (nodeInx == null) nodeInx = new int[0];
    if (edgeInx == null) edgeInx = new int[0];
    try { return new FGraphPerspective
            (this, new ArrayIntIterator(nodeInx, 0, nodeInx.length),
             new ArrayIntIterator(edgeInx, 0, edgeInx.length)); }
    catch (IllegalArgumentException exc) { return null; } }

  public void ensureCapacity(int nodes, int edges) {
    System.out.println("The secret easter egg module has been activated."); }

  public int getNodeCount()
  {
    return m_graph.nodes().numRemaining();
  }

  public int getEdgeCount()
  {
    return m_graph.edges().numRemaining();
  }

  public Iterator nodesIterator()
  {
    final IntEnumerator nodes = m_graph.nodes();
    final FRootGraph rootGraph = this;
    return new Iterator() {
        public void remove() {
          throw new UnsupportedOperationException(); }
        public boolean hasNext() {
          return nodes.numRemaining() > 0; }
        public Object next() {
          if (!hasNext()) throw new NoSuchElementException();
          return rootGraph.getNode(~(nodes.nextInt())); } };
  }

  public java.util.List nodesList() {
    final int nodeCount = getNodeCount();
    final java.util.ArrayList returnThis = new java.util.ArrayList(nodeCount);
    Iterator iter = nodesIterator();
    for (int i = 0; i < nodeCount; i++) returnThis.add(iter.next());
    return returnThis; }

  public int[] getNodeIndicesArray()
  {
    IntEnumerator nodes = m_graph.nodes();
    final int[] returnThis = new int[nodes.numRemaining()];
    for (int i = 0; i < returnThis.length; i++)
      returnThis[i] = ~(nodes.nextInt());
    return returnThis;
  }

  public Iterator edgesIterator()
  {
    final IntEnumerator edges = m_graph.edges();
    final FRootGraph rootGraph = this;
    return new Iterator() {
        public void remove() {
          throw new UnsupportedOperationException(); }
        public boolean hasNext() {
          return edges.numRemaining() > 0; }
        public Object next() {
          if (!hasNext()) throw new NoSuchElementException();
          return rootGraph.getEdge(~(edges.nextInt())); } };
  }

  public java.util.List edgesList() {
    final int edgeCount = getEdgeCount();
    final java.util.ArrayList returnThis = new java.util.ArrayList(edgeCount);
    Iterator iter = edgesIterator();
    for (int i = 0; i < edgeCount; i++) returnThis.add(iter.next());
    return returnThis; }

  public int[] getEdgeIndicesArray()
  {
    IntEnumerator edges = m_graph.edges();
    final int[] returnThis = new int[edges.numRemaining()];
    for (int i = 0; i < returnThis.length; i++)
      returnThis[i] = ~(edges.nextInt());
    return returnThis;
  }

  public Node removeNode(Node node) {
    if (node.getRootGraph() == this &&
        removeNode(node.getRootGraphIndex()) != 0) return node;
    else return null; }

  public int removeNode(final int nodeInx)
  {
    final int nativeNodeInx = ~nodeInx;
    final IntEnumerator nativeEdgeEnum =
      m_graph.edgesAdjacent(nativeNodeInx, true, true, true);
    if (nativeEdgeEnum == null) return 0;
    final Edge[] removedEdgeArr = new Edge[nativeEdgeEnum.numRemaining()];
    for (int i = 0; i < removedEdgeArr.length; i++)
      removedEdgeArr[i] = m_edges.getEdgeAtIndex(nativeEdgeEnum.nextInt());
    for (int i = 0; i < removedEdgeArr.length; i++) {
      final int nativeEdgeInx = ~(removedEdgeArr[i].getRootGraphIndex());
      m_graph.edgeRemove(nativeEdgeInx);
      final FEdge removedEdge = m_edges.getEdgeAtIndex(nativeEdgeInx);
      m_edges.setEdgeAtIndex(null, nativeEdgeInx);
      m_edgeDepot.recycleEdge(removedEdge); }
    final FNode removedNode = m_nodes.getNodeAtIndex(nativeNodeInx);
    m_graph.nodeRemove(nativeNodeInx);
    m_nodes.setNodeAtIndex(null, nativeNodeInx);
    m_nodeDepot.recycleNode(removedNode);
    final int metaNodeInx = m_nativeToMetaNodeInxMap.get(nativeNodeInx);
    if (!(metaNodeInx < 0 || metaNodeInx == Integer.MAX_VALUE)) {
      m_nativeToMetaNodeInxMap.put(nativeNodeInx, Integer.MAX_VALUE);
      m_metaToNativeInxMap.setIntAtIndex(0, metaNodeInx);
      m_metaGraph.nodeRemove(metaNodeInx); }    
    if (removedEdgeArr.length > 0)
      m_lis.rootGraphChanged
        (new RootGraphEdgesRemovedEvent(this, removedEdgeArr));
    m_lis.rootGraphChanged
      (new RootGraphNodesRemovedEvent(this, new Node[] { removedNode }));
    return nodeInx;
  }

  public java.util.List removeNodes(java.util.List nodes) {
    final java.util.ArrayList returnThis = new java.util.ArrayList();
    for (int i = 0; i < nodes.size(); i++)
      if (removeNode((Node) nodes.get(i)) != null)
        returnThis.add(nodes.get(i));
    return returnThis; }

  public int[] removeNodes(int[] nodeIndices) {
    final int[] returnThis = new int[nodeIndices.length];
    for (int i = 0; i < returnThis.length; i++)
      returnThis[i] = removeNode(nodeIndices[i]);
    return returnThis; }

  public int createNode()
  {
    final int nativeNodeInx = m_graph.nodeCreate();
    final int returnThis = ~nativeNodeInx;
    FNode newNode = m_nodeDepot.getNode();
    newNode.m_rootGraph = this;
    newNode.m_rootGraphIndex = returnThis;
    newNode.m_identifier = null;
    m_nodes.setNodeAtIndex(newNode, nativeNodeInx);
    return returnThis;
  }

  public int createNode(Node[] nodes, Edge[] edges) {
    // I don't care how inefficient this is because I don't like meta nodes.
    final GraphPerspective persp = createGraphPerspective(nodes, edges);
    if (persp == null) return 0;
    return createNode(persp); }

  public int createNode(GraphPerspective perspective)
  {
    // Casting to check that we aren't going to get garbage nodes and edges.
    if (((FGraphPerspective) perspective).getRootGraph() != this) return 0;
    final int returnThis = createNode();
    final int nativeParentNodeInx = ~returnThis;
    final int[] perspEdgeInxArr = perspective.getEdgeIndicesArray();
    final int[] perspNodeInxArr = perspective.getNodeIndicesArray();
    if (perspEdgeInxArr.length == 0 && perspNodeInxArr.length == 0)
      return returnThis;
    final int metaParentNodeInx = m_metaGraph.nodeCreate();
    m_metaToNativeInxMap.setIntAtIndex(nativeParentNodeInx + 1,
                                       metaParentNodeInx);
    m_nativeToMetaNodeInxMap.put(nativeParentNodeInx, metaParentNodeInx);
    for (int i = 1; i < perspNodeInxArr.length; i++) {
      final int nativeChildNodeInx = ~perspNodeInxArr[i];
      int metaChildNodeInx = m_nativeToMetaNodeInxMap.get(nativeChildNodeInx);
      if (metaChildNodeInx < 0 || metaChildNodeInx == Integer.MAX_VALUE) {
        metaChildNodeInx = m_metaGraph.nodeCreate();
        m_metaToNativeInxMap.setIntAtIndex(nativeChildNodeInx + 1,
                                           metaChildNodeInx);
        m_nativeToMetaNodeInxMap.put(nativeChildNodeInx, metaChildNodeInx); }
      // This edge can't yet exist because we just created metaParentNodeInx.
      m_metaGraph.edgeCreate(metaParentNodeInx, metaChildNodeInx, true); }
    for (int i = 1; i < perspEdgeInxArr.length; i++) {
      final int nativeChildEdgeInx = ~perspEdgeInxArr[i];
      int metaChildEdgeInx = m_nativeToMetaEdgeInxMap.get(nativeChildEdgeInx);
      if (metaChildEdgeInx < 0 || metaChildEdgeInx == Integer.MAX_VALUE) {
        metaChildEdgeInx = m_metaGraph.nodeCreate();
        m_metaToNativeInxMap.setIntAtIndex(~nativeChildEdgeInx,
                                           metaChildEdgeInx);
        m_nativeToMetaEdgeInxMap.put(nativeChildEdgeInx, metaChildEdgeInx); }
      // This edge can't yet exist because we just created metaParentNodeInx.
      m_metaGraph.edgeCreate(metaParentNodeInx, metaChildEdgeInx, true); }
    return returnThis;
  }

  public int createNode(int[] nodeIndices, int[] edgeIndices) {
    // I don't care how inefficient this is because I don't like meta nodes.
    final GraphPerspective persp =
      createGraphPerspective(nodeIndices, edgeIndices);
    if (persp == null) return 0;
    return createNode(persp); }

  public int[] createNodes(int numNewNodes) {
    final int[] returnThis = new int[numNewNodes];
    for (int i = 0; i < returnThis.length; i++) returnThis[i] = createNode();
    return returnThis; }

  public Edge removeEdge(Edge edge) {
    if (edge.getRootGraph() == this &&
        removeEdge(edge.getRootGraphIndex()) != 0) return edge;
    else return null; }

  public int removeEdge(int edgeInx)
  {
    final int nativeEdgeInx = ~edgeInx;
    if (!(m_graph.edgeRemove(nativeEdgeInx))) return 0;
    final FEdge removedEdge = m_edges.getEdgeAtIndex(nativeEdgeInx);
    m_edges.setEdgeAtIndex(null, nativeEdgeInx);
    m_edgeDepot.recycleEdge(removedEdge);
    final int metaEdgeInx = m_nativeToMetaEdgeInxMap.get(nativeEdgeInx);
    if (!(metaEdgeInx < 0 || metaEdgeInx == Integer.MAX_VALUE)) {
      m_nativeToMetaEdgeInxMap.put(nativeEdgeInx, Integer.MAX_VALUE);
      m_metaToNativeInxMap.setIntAtIndex(0, metaEdgeInx);
      m_metaGraph.nodeRemove(metaEdgeInx); }
    m_lis.rootGraphChanged
      (new RootGraphEdgesRemovedEvent(this, new Edge[] { removedEdge }));
    return edgeInx;
  }

  public java.util.List removeEdges(java.util.List edges) {
    final java.util.ArrayList returnThis = new java.util.ArrayList();
    for (int i = 0; i < edges.size(); i++)
      if (removeEdge((Edge) edges.get(i)) != null)
        returnThis.add(edges.get(i));
    return returnThis; }

  public int[] removeEdges(int[] edgeIndices) {
    final int[] returnThis = new int[edgeIndices.length];
    for (int i = 0; i < returnThis.length; i++)
      returnThis[i] = removeEdge(edgeIndices[i]);
    return returnThis; }

  public int createEdge(Node source, Node target) {
    return createEdge
      (source, target,
       source.getRootGraphIndex() != target.getRootGraphIndex()); }

  public int createEdge(Node source, Node target, boolean directed) {
    if (source.getRootGraph() == this && target.getRootGraph() == this)
      return createEdge(source.getRootGraphIndex(),
                        target.getRootGraphIndex(), directed);
    else return 0; }

  public int createEdge(int sourceNodeIndex, int targetNodeIndex) {
    return createEdge(sourceNodeIndex, targetNodeIndex,
                      sourceNodeIndex != targetNodeIndex); }

  public int createEdge(int sourceNodeIndex, int targetNodeIndex,
                        boolean directed)
  {
    final int nativeEdgeInx =
      m_graph.edgeCreate(~sourceNodeIndex, ~targetNodeIndex, directed);
    if (nativeEdgeInx < 0) return 0;
    final int returnThis = ~nativeEdgeInx;
    FEdge newEdge = m_edgeDepot.getEdge();
    newEdge.m_rootGraph = this;
    newEdge.m_rootGraphIndex = returnThis;
    newEdge.m_identifier = null;
    m_edges.setEdgeAtIndex(newEdge, nativeEdgeInx);
    return returnThis;
  }

  public int[] createEdges(int[] sourceNodeIndices, int[] targetNodeIndices,
                           boolean directed) {
    if (sourceNodeIndices.length != targetNodeIndices.length)
      throw new IllegalArgumentException("input arrays not same length");
    final int[] returnThis = new int[sourceNodeIndices.length];
    for (int i = 0; i < returnThis.length; i++)
      returnThis[i] = createEdge(sourceNodeIndices[i],
                                 targetNodeIndices[i], directed);
    return returnThis; }

  public boolean containsNode(Node node) {
    return node.getRootGraph() == this &&
      getNode(node.getRootGraphIndex()) != null; }

  public boolean containsEdge(Edge edge) {
    return edge.getRootGraph() == this &&
      getEdge(edge.getRootGraphIndex()) != null; }

  public java.util.List neighborsList(Node node) {
    if (node.getRootGraph() == this) {
      final int nodeIndex = node.getRootGraphIndex();
      int[] adjacentEdgeIndices =
        getAdjacentEdgeIndicesArray(nodeIndex, true, true, true);
      if (adjacentEdgeIndices == null) return null;
      m_hash.empty();
      final IntHash neighbors = m_hash;
      for (int i = 0; i < adjacentEdgeIndices.length; i++) {
        int neighborIndex = (nodeIndex ^
                             getEdgeSourceIndex(adjacentEdgeIndices[i]) ^
                             getEdgeTargetIndex(adjacentEdgeIndices[i]));
        neighbors.put(~neighborIndex); }
      IntEnumerator enum = neighbors.elements();
      java.util.ArrayList list = new java.util.ArrayList(enum.numRemaining());
      while (enum.numRemaining() > 0)
        list.add(getNode(~(enum.nextInt())));
      return list; }
    else { return null; } }

  public boolean isNeighbor(Node a, Node b) {
    if (a.getRootGraph() == this && b.getRootGraph() == this)
      return isNeighbor(a.getRootGraphIndex(), b.getRootGraphIndex());
    else return false; }

  public boolean isNeighbor(int nodeInxA, int nodeInxB)
  {
    final IntIterator connectingEdges = m_graph.edgesConnecting
      (~nodeInxA, ~nodeInxB, true, true, true);
    if (connectingEdges == null) return false;
    return connectingEdges.hasNext();
  }

  public boolean edgeExists(Node from, Node to) {
    if (from.getRootGraph() == this && to.getRootGraph() == this)
      return edgeExists(from.getRootGraphIndex(),
                        to.getRootGraphIndex());
    else return false; }

  public boolean edgeExists(int fromNodeInx, int toNodeInx)
  {
    final IntIterator connectingEdges =
      m_graph.edgesConnecting(~fromNodeInx, ~toNodeInx, true, false, true);
    if (connectingEdges == null) return false;
    return connectingEdges.hasNext();
  }

  public int getEdgeCount(Node from, Node to, boolean countUndirectedEdges) {
    if (from.getRootGraph() == this && to.getRootGraph() == this)
      return getEdgeCount(from.getRootGraphIndex(),
                          to.getRootGraphIndex(),
                          countUndirectedEdges);
    else return -1; }

  public int getEdgeCount(int fromNodeInx, int toNodeInx,
                          boolean countUndirectedEdges) {
    final int[] connEdges = getEdgeIndicesArray(fromNodeInx, toNodeInx,
                                                countUndirectedEdges);
    if (connEdges == null) return -1;
    else return connEdges.length; }

  public int[] getAdjacentEdgeIndicesArray(int nodeInx,
                                           boolean undirected,
                                           boolean incomingDirected,
                                           boolean outgoingDirected)
  {
    final IntEnumerator adj = m_graph.edgesAdjacent
      (~nodeInx, outgoingDirected, incomingDirected, undirected);
    if (adj == null) return null;
    final int[] returnThis = new int[adj.numRemaining()];
    for (int i = 0; i < returnThis.length; i++) returnThis[i] = ~adj.nextInt();
    return returnThis;
  }

  private final IntHash m_hash2 = new IntHash();

  public int[] getConnectingEdgeIndicesArray(int[] nodeInx)
  {
    m_hash2.empty();
    final IntHash nodeBucket = m_hash2;
    for (int i = 0; i < nodeInx.length; i++) {
      final int positiveNodeIndex = ~nodeInx[i];
      if (m_graph.nodeExists(positiveNodeIndex))
        nodeBucket.put(positiveNodeIndex);
      else return null; }
    m_hash.empty();
    final IntHash edgeBucket = m_hash;
    final IntEnumerator nodeIter = nodeBucket.elements();
    while (nodeIter.numRemaining() > 0) {
      final int thePositiveNode = nodeIter.nextInt();
      final IntEnumerator edgeIter =
        m_graph.edgesAdjacent(thePositiveNode, true, false, true);
      while (edgeIter.numRemaining() > 0) {
        final int candidateEdge = edgeIter.nextInt();
        final int otherEdgeNode = (thePositiveNode ^
                                   m_graph.edgeSource(candidateEdge) ^
                                   m_graph.edgeTarget(candidateEdge));
        if (otherEdgeNode == nodeBucket.get(otherEdgeNode))
          edgeBucket.put(candidateEdge); } }
    final IntEnumerator returnEdges = edgeBucket.elements();
    final int[] returnThis = new int[returnEdges.numRemaining()];
    for (int i = 0; i < returnThis.length; i++)
      returnThis[i] = ~(returnEdges.nextInt());
    return returnThis;
  }

  public int[] getConnectingNodeIndicesArray(int[] edgeInx)
  {
    m_hash.empty();
    final IntHash nodeBucket = m_hash;
    for (int i = 0; i < edgeInx.length; i++) {
      final int positiveEdge = ~edgeInx[i];
      if (m_graph.edgeType(positiveEdge) >= 0) {
        nodeBucket.put(m_graph.edgeSource(positiveEdge));
        nodeBucket.put(m_graph.edgeTarget(positiveEdge)); }
      else { return null; } }
    final IntEnumerator nodes = nodeBucket.elements();
    final int[] returnThis = new int[nodes.numRemaining()];
    for (int i = 0; i < returnThis.length; i++)
      returnThis[i] = ~(nodes.nextInt());
    return returnThis;
  }

  public int[] getEdgeIndicesArray(int fromNodeInx,
                                   int toNodeInx,
                                   boolean undirectedEdges,
                                   boolean bothDirections)
  {
    final IntIterator connectingEdges = m_graph.edgesConnecting
      (~fromNodeInx, ~toNodeInx, true, bothDirections, undirectedEdges);
    if (connectingEdges == null) return null;
    m_heap.empty();
    final MinIntHeap edgeBucket = m_heap;
    while (connectingEdges.hasNext())
      edgeBucket.toss(~connectingEdges.nextInt());
    final int[] returnThis = new int[edgeBucket.size()];
    edgeBucket.copyInto(returnThis, 0);
    return returnThis;
  }

  public java.util.List edgesList(Node from, Node to) {
    if (from.getRootGraph() == this && to.getRootGraph() == this)
      return edgesList(from.getRootGraphIndex(),
                       to.getRootGraphIndex(), true);
    else return null; }

  public java.util.List edgesList(int fromNodeInx, int toNodeInx,
                                  boolean includeUndirectedEdges) {
    final int[] edgeInx = getEdgeIndicesArray(fromNodeInx, toNodeInx,
                                              includeUndirectedEdges);
    if (edgeInx == null) return null;
    java.util.ArrayList returnList = new java.util.ArrayList(edgeInx.length);
    for (int i = 0; i < edgeInx.length; i++)
      returnList.add(getEdge(edgeInx[i]));
    return returnList; }

  public int[] getEdgeIndicesArray(int fromNodeInx, int toNodeInx,
                                   boolean includeUndirectedEdges) {
    return getEdgeIndicesArray(fromNodeInx, toNodeInx,
                               includeUndirectedEdges, false); }

  public int getInDegree(Node node) {
    if (node.getRootGraph() == this)
      return getInDegree(node.getRootGraphIndex());
    else return -1; }

  public int getInDegree(int nodeInx) {
    return getInDegree(nodeInx, true); }

  public int getInDegree(Node node, boolean countUndirectedEdges) {
    if (node.getRootGraph() == this)
      return getInDegree(node.getRootGraphIndex(), countUndirectedEdges);
    else return -1; }

  public int getInDegree(int nodeInx, boolean countUndirectedEdges)
  {
    final IntEnumerator adj =
      m_graph.edgesAdjacent(~nodeInx, false, true, countUndirectedEdges);
    if (adj == null) return -1;
    return adj.numRemaining();
  }

  public int getOutDegree(Node node) {
    if (node.getRootGraph() == this)
      return getOutDegree(node.getRootGraphIndex());
    else return -1; }

  public int getOutDegree(int nodeInx) {
    return getOutDegree(nodeInx, true); }

  public int getOutDegree(Node node, boolean countUndirectedEdges) {
    if (node.getRootGraph() == this)
      return getOutDegree(node.getRootGraphIndex(), countUndirectedEdges);
    else return -1; }

  public int getOutDegree(int nodeInx, boolean countUndirectedEdges)
  {
    final IntEnumerator adj =
      m_graph.edgesAdjacent(~nodeInx, true, false, countUndirectedEdges);
    if (adj == null) return -1;
    return adj.numRemaining();
  }

  public int getDegree(Node node) {
    if (node.getRootGraph() == this)
      return getDegree(node.getRootGraphIndex());
    else return -1; }

  public int getDegree(int nodeInx)
  {
    final IntEnumerator adj =
      m_graph.edgesAdjacent(~nodeInx, true, true, true);
    if (adj == null) return -1;
    return adj.numRemaining();
  }

  public int getIndex(Node node) {
    if (node.getRootGraph() == this) return node.getRootGraphIndex();
    else return 0; }

  public Node getNode(int nodeInx) {
    if (nodeInx < 0 && nodeInx != 0x80000000)
      return m_nodes.getNodeAtIndex(~nodeInx);
    else return null; }

  public int getIndex(Edge edge) {
    if (edge.getRootGraph() == this) return edge.getRootGraphIndex();
    else return 0; }

  public Edge getEdge(int edgeInx) {
    if (edgeInx < 0 && edgeInx != 0x80000000)
      return m_edges.getEdgeAtIndex(~edgeInx);
    else return null; }

  public int getEdgeSourceIndex(int edgeInx)
  {
    return ~(m_graph.edgeSource(~edgeInx));
  }

  public int getEdgeTargetIndex(int edgeInx)
  {
    return ~(m_graph.edgeTarget(~edgeInx));
  }

  public boolean isEdgeDirected(int edgeInx)
  {
    return m_graph.edgeType(~edgeInx) == 1;
  }

  public boolean addMetaChild(Node parent, Node child) {
    if (parent.getRootGraph() != this || child.getRootGraph() != this)
      return false;
    return addNodeMetaChild(parent.getRootGraphIndex(),
                            child.getRootGraphIndex()); }

  public boolean addNodeMetaChild(int parentNodeInx, int childNodeInx)
  {
    final int nativeParent = ~parentNodeInx;
    final int nativeChildNode = ~childNodeInx;
    if (!(m_graph.nodeExists(nativeParent) &&
          m_graph.nodeExists(nativeChildNode))) return false;
    int metaParent = m_nativeToMetaNodeInxMap.get(nativeParent);
    if (metaParent < 0 || metaParent == Integer.MAX_VALUE) {
      metaParent = m_metaGraph.nodeCreate();
      m_metaToNativeInxMap.setIntAtIndex(nativeParent + 1, metaParent);
      m_nativeToMetaNodeInxMap.put(nativeParent, metaParent); }
    int metaChildNode = m_nativeToMetaNodeInxMap.get(nativeChildNode);
    if (metaChildNode < 0 || metaChildNode == Integer.MAX_VALUE) {
      metaChildNode = m_metaGraph.nodeCreate();
      m_metaToNativeInxMap.setIntAtIndex(nativeChildNode + 1, metaChildNode);
      m_nativeToMetaNodeInxMap.put(nativeChildNode, metaChildNode); }
    if (m_metaGraph.edgesConnecting(metaParent, metaChildNode, true, false,
                                    false).hasNext()) return false;
    m_metaGraph.edgeCreate(metaParent, metaChildNode, true);
    return true;
  }   

  public boolean removeNodeMetaChild(final int parentNodeInx, int childNodeInx)
  {
    final int nativeParent = ~parentNodeInx;
    final int nativeChildNode = ~childNodeInx;
    if (!(m_graph.nodeExists(nativeParent) &&
          m_graph.nodeExists(nativeChildNode))) return false;
    final IntEnumerator nativeEdgesTouchingChild = m_graph.edgesAdjacent
      (nativeChildNode, true, true, true);
    while (nativeEdgesTouchingChild.numRemaining() > 0)
      removeEdgeMetaChild(parentNodeInx, ~nativeEdgesTouchingChild.nextInt());
    final int metaParent = m_nativeToMetaNodeInxMap.get(nativeParent);
    final int metaChildNode = m_nativeToMetaNodeInxMap.get(nativeChildNode);
    final IntIterator metaRelationships = m_metaGraph.edgesConnecting
      (metaParent, metaChildNode, true, false, false);
    if (metaRelationships == null || !metaRelationships.hasNext())
      return false;
    m_metaGraph.edgeRemove(metaRelationships.nextInt());
    if (m_metaGraph.edgesAdjacent(metaParent, true, true, false).
        numRemaining() == 0) { // Remove disconnected meta-element.
      m_nativeToMetaNodeInxMap.put(nativeParent, Integer.MAX_VALUE);
      m_metaToNativeInxMap.setIntAtIndex(0, metaParent);
      m_metaGraph.nodeRemove(metaParent); }
    if (m_metaGraph.edgesAdjacent(metaChildNode, true, true, false).
        numRemaining() == 0) { // Remove disconnected meta-element.
      m_nativeToMetaNodeInxMap.put(nativeChildNode, Integer.MAX_VALUE);
      m_metaToNativeInxMap.setIntAtIndex(0, metaChildNode);
      m_metaGraph.nodeRemove(metaChildNode); }
    return true;
  }

  public boolean isMetaParent(Node child, Node parent) {
    if (child.getRootGraph() != this || parent.getRootGraph() != this)
      return false;
    return isNodeMetaParent(child.getRootGraphIndex(),
                            parent.getRootGraphIndex()); }

  public boolean isNodeMetaParent(int childNodeInx, int parentNodeInx) {
    return isNodeMetaChild(parentNodeInx, childNodeInx); }

  public java.util.List metaParentsList(Node node) {
    if (node.getRootGraph() != this) return null;
    return nodeMetaParentsList(node.getRootGraphIndex()); }

  public java.util.List nodeMetaParentsList(int nodeInx) {
    final int[] parentInxArr = getNodeMetaParentIndicesArray(nodeInx);
    if (parentInxArr == null) return null;
    final java.util.ArrayList returnThis =
      new java.util.ArrayList(parentInxArr.length);
    for (int i = 0; i < parentInxArr.length; i++)
      returnThis.add(getNode(parentInxArr[i]));
    return returnThis; }

  public int[] getNodeMetaParentIndicesArray(int nodeInx)
  {
    final int nativeChildNode = ~nodeInx;
    if (!m_graph.nodeExists(nativeChildNode)) return null;
    final int metaChildNode = m_nativeToMetaNodeInxMap.get(nativeChildNode);
    final IntEnumerator metaRelationshipsEnum =
      m_metaGraph.edgesAdjacent(metaChildNode, false, true, false);
    if (metaRelationshipsEnum == null) return new int[0];
    final int[] returnThis = new int[metaRelationshipsEnum.numRemaining()];
    for (int i = 0; i < returnThis.length; i++) {
      final int metaRelationship = metaRelationshipsEnum.nextInt();
      final int metaParent = m_metaGraph.edgeSource(metaRelationship);
      returnThis[i] = ~(m_metaToNativeInxMap.getIntAtIndex(metaParent) - 1); }
    return returnThis;
  }

  public boolean isMetaChild(Node parent, Node child) {
    return isMetaParent(child, parent); }

  public boolean isNodeMetaChild(int parentNodeInx, int childNodeInx)
  {
    final int nativeParent = ~parentNodeInx;
    final int nativeChildNode = ~childNodeInx;
    if (!(m_graph.nodeExists(nativeParent) &&
          m_graph.nodeExists(nativeChildNode))) return false;
    final int metaParent = m_nativeToMetaNodeInxMap.get(nativeParent);
    final int metaChildNode = m_nativeToMetaNodeInxMap.get(nativeChildNode);
    final IntIterator metaRelationshipsIter = m_metaGraph.edgesConnecting
      (metaParent, metaChildNode, true, false, false);
    if (metaRelationshipsIter == null || !metaRelationshipsIter.hasNext())
      return false;
    return true;
  }

  private final IntStack m_stack = new IntStack();

  public boolean isNodeMetaChild(int parentNodeInx, int childNodeInx,
                                 boolean recursive)
  {
    if (!recursive) return isNodeMetaChild(parentNodeInx, childNodeInx);
    final int nativeParent = ~parentNodeInx;
    final int nativeChildNode = ~childNodeInx;
    if (!(m_graph.nodeExists(nativeParent) &&
          m_graph.nodeExists(nativeChildNode))) return false;
    final int metaParent = m_nativeToMetaNodeInxMap.get(nativeParent);
    final int metaChildNode = m_nativeToMetaNodeInxMap.get(nativeChildNode);
    if (metaParent < 0 || metaParent == Integer.MAX_VALUE ||
        metaChildNode < 0 || metaChildNode == Integer.MAX_VALUE) return false;
    // Depth first search.
    m_hash.empty();
    final IntHash metaVisited = m_hash;
    m_stack.empty();
    final IntStack metaPending = m_stack;
    metaVisited.put(metaParent);
    metaPending.push(metaParent);
    while (metaPending.size() > 0) {
      final int currMeta = metaPending.pop();
      final IntEnumerator relationships = m_metaGraph.edgesAdjacent
        (currMeta, true, false, false);
      while (relationships.numRemaining() > 0) {
        final int aChild = m_metaGraph.edgeTarget(relationships.nextInt());
        if (aChild == metaChildNode) return true;
        if (m_metaToNativeInxMap.getIntAtIndex(aChild) > 0 && // A node.
            metaVisited.put(aChild) < 0) metaPending.push(aChild); } }
    return false;
  }

  public java.util.List nodeMetaChildrenList(Node node) {
    if (node.getRootGraph() != this) return null;
    return nodeMetaChildrenList(node.getRootGraphIndex()); }

  public java.util.List nodeMetaChildrenList(int parentNodeInx) {
    final int[] childInxArr = getNodeMetaChildIndicesArray(parentNodeInx);
    if (childInxArr == null) return null;
    final java.util.ArrayList returnThis =
      new java.util.ArrayList(childInxArr.length);
    for (int i = 0; i < childInxArr.length; i++)
      returnThis.add(getNode(childInxArr[i]));
    return returnThis; }

  public int[] getNodeMetaChildIndicesArray(int parentNodeInx)
  {
    final int nativeParent = ~parentNodeInx;
    if (!m_graph.nodeExists(nativeParent)) return null;
    final int metaParent = m_nativeToMetaNodeInxMap.get(nativeParent);
    final IntEnumerator metaRelationshipsEnum =
      m_metaGraph.edgesAdjacent(metaParent, true, false, false);
    if (metaRelationshipsEnum == null) return new int[0];
    m_heap.empty();
    final MinIntHeap childRootNodeBucket = m_heap;
    while (metaRelationshipsEnum.numRemaining() > 0) {
      final int metaChild =
        m_metaGraph.edgeTarget(metaRelationshipsEnum.nextInt());
      if (m_metaToNativeInxMap.getIntAtIndex(metaChild) > 0)
        childRootNodeBucket.toss
          (~(m_metaToNativeInxMap.getIntAtIndex(metaChild) - 1)); }
    final int[] returnThis = new int[childRootNodeBucket.size()];
    childRootNodeBucket.copyInto(returnThis, 0);
    return returnThis;
  }

  public int[] getNodeMetaChildIndicesArray(int parentNodeInx,
                                            boolean recursive)
  {
    if (!recursive) return getNodeMetaChildIndicesArray(parentNodeInx);
    final int nativeParent = ~parentNodeInx;
    if (!m_graph.nodeExists(nativeParent)) return null;
    final int metaParent = m_nativeToMetaNodeInxMap.get(nativeParent);
    if (metaParent < 0 || metaParent == Integer.MAX_VALUE) return new int[0];
    // Depth first search.
    m_hash.empty();
    final IntHash metaVisited = m_hash;
    m_stack.empty();
    final IntStack metaPending = m_stack;
    m_hash2.empty();
    final IntHash nativeChildNodeBucket = m_hash2;
    metaVisited.put(metaParent);
    metaPending.push(metaParent);
    while (metaPending.size() > 0) {
      final int currMeta = metaPending.pop();
      final IntEnumerator relationships = m_metaGraph.edgesAdjacent
        (currMeta, true, false, false);
      while (relationships.numRemaining() > 0) {
        final int aChild = m_metaGraph.edgeTarget(relationships.nextInt());
        if (m_metaToNativeInxMap.getIntAtIndex(aChild) > 0) { // A node.
          nativeChildNodeBucket.put
            (m_metaToNativeInxMap.getIntAtIndex(aChild) - 1);
          if (metaVisited.put(aChild) < 0) metaPending.push(aChild); } } }
    final IntEnumerator returnElements = nativeChildNodeBucket.elements();
    final int[] returnThis = new int[returnElements.numRemaining()];
    for (int i = 0; i < returnThis.length; i++)
      returnThis[i] = ~returnElements.nextInt();
    return returnThis;
  }

  public int[] getChildlessMetaDescendants(int nodeInx)
  {
    final int nativeParent = ~nodeInx;
    if (!m_graph.nodeExists(nativeParent)) return null;
    final int metaParent = m_nativeToMetaNodeInxMap.get(nativeParent);
    if (metaParent < 0 || metaParent == Integer.MAX_VALUE) return new int[0];
    // Depth first search.
    m_hash.empty();
    final IntHash metaVisited = m_hash;
    m_stack.empty();
    final IntStack metaPending = m_stack;
    m_hash2.empty();
    final IntHash nativeChildlessNodeBucket = m_hash2;
    metaVisited.put(metaParent);
    metaPending.push(metaParent);
    while (metaPending.size() > 0) {
      final int currMeta = metaPending.pop();
      final IntEnumerator relationships = m_metaGraph.edgesAdjacent
        (currMeta, true, false, false);
      while (relationships.numRemaining() > 0) {
        final int aChild = m_metaGraph.edgeTarget(relationships.nextInt());
        if (m_metaToNativeInxMap.getIntAtIndex(aChild) > 0 && // A node.
            metaVisited.put(aChild) < 0) {
          metaPending.push(aChild);
          if (m_metaGraph.edgesAdjacent
              (aChild, true, false, false).numRemaining() == 0)
            nativeChildlessNodeBucket.put
              (m_metaToNativeInxMap.getIntAtIndex(aChild) - 1); } } }
    final IntEnumerator returnElements = nativeChildlessNodeBucket.elements();
    final int[] returnThis = new int[returnElements.numRemaining()];
    for (int i = 0; i < returnThis.length; i++)
      returnThis[i] = ~returnElements.nextInt();
    return returnThis;
  }

  public boolean addMetaChild(Node parent, Edge child) {
    if (parent.getRootGraph() != this || child.getRootGraph() != this)
      return false;
    return addEdgeMetaChild(parent.getRootGraphIndex(),
                            child.getRootGraphIndex()); }

  public boolean addEdgeMetaChild(final int parentNodeInx, int childEdgeInx)
  {
    final int nativeParent = ~parentNodeInx;
    final int nativeChildEdge = ~childEdgeInx;
    if (!(m_graph.nodeExists(nativeParent) &&
          m_graph.edgeType(nativeChildEdge) >= 0)) return false;
    addNodeMetaChild(parentNodeInx, ~m_graph.edgeSource(nativeChildEdge));
    addNodeMetaChild(parentNodeInx, ~m_graph.edgeTarget(nativeChildEdge));
    int metaParent = m_nativeToMetaNodeInxMap.get(nativeParent);
    if (metaParent < 0 || metaParent == Integer.MAX_VALUE) {
      metaParent = m_metaGraph.nodeCreate();
      m_metaToNativeInxMap.setIntAtIndex(nativeParent + 1, metaParent);
      m_nativeToMetaNodeInxMap.put(nativeParent, metaParent); }
    int metaChildEdge = m_nativeToMetaEdgeInxMap.get(nativeChildEdge);
    if (metaChildEdge < 0 || metaChildEdge == Integer.MAX_VALUE) {
      metaChildEdge = m_metaGraph.nodeCreate();
      m_metaToNativeInxMap.setIntAtIndex(~nativeChildEdge, metaChildEdge);
      m_nativeToMetaEdgeInxMap.put(nativeChildEdge, metaChildEdge); }
    if (m_metaGraph.edgesConnecting(metaParent, metaChildEdge, true, false,
                                    false).hasNext()) return false;
    m_metaGraph.edgeCreate(metaParent, metaChildEdge, true);
    return true;
  }

  public boolean removeEdgeMetaChild(int parentNodeInx, int childEdgeInx)
  {
    final int nativeParent = ~parentNodeInx;
    final int nativeChildEdge = ~childEdgeInx;
    if (!(m_graph.nodeExists(nativeParent) &&
          m_graph.edgeType(nativeChildEdge) >= 0)) return false;
    final int metaParent = m_nativeToMetaNodeInxMap.get(nativeParent);
    final int metaChildEdge = m_nativeToMetaEdgeInxMap.get(nativeChildEdge);
    final IntIterator metaRelationships = m_metaGraph.edgesConnecting
      (metaParent, metaChildEdge, true, false, false);
    if (metaRelationships == null || !metaRelationships.hasNext())
      return false;
    m_metaGraph.edgeRemove(metaRelationships.nextInt());
    if (m_metaGraph.edgesAdjacent(metaParent, true, true, false).
        numRemaining() == 0) { // Remove disconnected meta-element.
      m_nativeToMetaNodeInxMap.put(nativeParent, Integer.MAX_VALUE);
      m_metaToNativeInxMap.setIntAtIndex(0, metaParent);
      m_metaGraph.nodeRemove(metaParent); }
    if (m_metaGraph.edgesAdjacent(metaChildEdge, false, true, false).
        numRemaining() == 0) { // Remove disconnected meta-element.
      m_nativeToMetaEdgeInxMap.put(nativeChildEdge, Integer.MAX_VALUE);
      m_metaToNativeInxMap.setIntAtIndex(0, metaChildEdge);
      m_metaGraph.nodeRemove(metaChildEdge); }
    return true;
  }

  public boolean isMetaParent(Edge child, Node parent) {
    if (child.getRootGraph() != this || parent.getRootGraph() != this)
      return false;
    return isEdgeMetaParent(child.getRootGraphIndex(),
                            parent.getRootGraphIndex()); }

  public boolean isEdgeMetaParent(int childEdgeInx, int parentNodeInx) {
    return isEdgeMetaChild(parentNodeInx, childEdgeInx); }

  public java.util.List metaParentsList(Edge edge) {
    if (edge.getRootGraph() != this) return null;
    return edgeMetaParentsList(edge.getRootGraphIndex()); }

  public java.util.List edgeMetaParentsList(int edgeInx) {
    final int[] metaParentInx = getEdgeMetaParentIndicesArray(edgeInx);
    if (metaParentInx == null) return null;
    final java.util.ArrayList returnThis =
      new java.util.ArrayList(metaParentInx.length);
    for (int i = 0; i < metaParentInx.length; i++)
      returnThis.add(getNode(metaParentInx[i]));
    return returnThis; }

  public int[] getEdgeMetaParentIndicesArray(int edgeInx)
  {
    final int nativeChildEdge = ~edgeInx;
    if (m_graph.edgeType(nativeChildEdge) < 0) return null;
    final int metaChildEdge = m_nativeToMetaEdgeInxMap.get(nativeChildEdge);
    final IntEnumerator metaRelationshipsEnum =
      m_metaGraph.edgesAdjacent(metaChildEdge, false, true, false);
    if (metaRelationshipsEnum == null) return new int[0];
    final int[] returnThis = new int[metaRelationshipsEnum.numRemaining()];
    for (int i = 0; i < returnThis.length; i++) {
      final int metaRelationship = metaRelationshipsEnum.nextInt();
      final int metaParent = m_metaGraph.edgeSource(metaRelationship);
      returnThis[i] = m_metaToNativeInxMap.getIntAtIndex(metaParent); }
    return returnThis;
  }

  public boolean isMetaChild(Node parent, Edge child) {
    return isMetaParent(child, parent); }

  public boolean isEdgeMetaChild(int parentNodeInx, int childEdgeInx) {
    final int nativeParent = ~parentNodeInx;
    final int nativeChildEdge = ~childEdgeInx;
    if (!(m_graph.nodeExists(nativeParent) &&
          m_graph.edgeType(nativeChildEdge) >= 0)) return false;
    final int metaParent = m_nativeToMetaNodeInxMap.get(nativeParent);
    final int metaChildEdge = m_nativeToMetaEdgeInxMap.get(nativeChildEdge);
    final IntIterator metaRelationshipsIter = m_metaGraph.edgesConnecting
      (metaParent, metaChildEdge, true, false, false);
    if (metaRelationshipsIter == null || !metaRelationshipsIter.hasNext())
      return false;
    return true;
  }

  public java.util.List edgeMetaChildrenList(Node node) {
    if (node.getRootGraph() != this) return null;
    return edgeMetaChildrenList(node.getRootGraphIndex()); }

  public java.util.List edgeMetaChildrenList(int parentNodeInx) {
    final int[] edgeChildrenArr = getEdgeMetaChildIndicesArray(parentNodeInx);
    if (edgeChildrenArr == null) return null;
    final java.util.ArrayList returnThis =
      new java.util.ArrayList(edgeChildrenArr.length);
    for (int i = 0; i < edgeChildrenArr.length; i++)
      returnThis.add(getEdge(edgeChildrenArr[i]));
    return returnThis; }

  public int[] getEdgeMetaChildIndicesArray(int parentNodeInx)
  {
    final int nativeParent = ~parentNodeInx;
    if (!m_graph.nodeExists(nativeParent)) return null;
    final int metaParent = m_nativeToMetaNodeInxMap.get(nativeParent);
    final IntEnumerator metaRelationshipsEnum =
      m_metaGraph.edgesAdjacent(metaParent, true, false, false);
    if (metaRelationshipsEnum == null) return new int[0];
    m_heap.empty();
    final MinIntHeap childRootEdgeBucket = m_heap;
    while (metaRelationshipsEnum.numRemaining() > 0) {
      final int metaChild =
        m_metaGraph.edgeTarget(metaRelationshipsEnum.nextInt());
      if (m_metaToNativeInxMap.getIntAtIndex(metaChild) < 0)
        childRootEdgeBucket.toss
          (m_metaToNativeInxMap.getIntAtIndex(metaChild)); }
    final int[] returnThis = new int[childRootEdgeBucket.size()];
    childRootEdgeBucket.copyInto(returnThis, 0);
    return returnThis;
  }

  // The relationship between indices (both node and edge) in this
  // RootGraph and in the DynamicGraph is "flip the bits":
  // rootGraphIndex == ~(dynamicGraphIndex)
  private final DynamicGraph m_graph =
    DynamicGraphFactory.instantiateDynamicGraph();

  // For the most part, there will always be a listener registered with this
  // RootGraph (all GraphPerspectives will have registered listeners).  So,
  // instead of checking for null, just keep a permanent listener.
  private RootGraphChangeListener m_lis = new RootGraphChangeListener() {
      public void rootGraphChanged(RootGraphChangeEvent event) {} };

  // This hash is re-used by many methods.  Make sure to empty() it before
  // using it.  You can use it as a bag of integers or to filter integer
  // duplicates.  You don't need to empty() it after usage.
  private final IntHash m_hash = new IntHash();

  // This heap is re-used by several methods.  It's used primarily as a bucket
  // of integers; sorting with this heap is [probably] not done at all.
  // Make sure to empty() it before using it.
  private final MinIntHeap m_heap = new MinIntHeap();

  // This is our "node factory" and "node recyclery".
  private final NodeDepository m_nodeDepot = new NodeDepository();

  // This is our "edge factory" and "edge recyclery".
  private final EdgeDepository m_edgeDepot = new EdgeDepository();

  // This is our index-to-node mapping.
  private final NodeArray m_nodes = new NodeArray();

  // This is our index-to-edge mapping.
  private final EdgeArray m_edges = new EdgeArray();

  // This is our meta-relationships graph where nodes in the meta-graph
  // are edges and nodes in the original graph, and directed edges in the
  // meta-graph are parent->child relationships.  Note that only nodes
  // in the original graph can be parents, and both nodes and edges from the
  // original graph can be children.  We're only going to populate the
  // meta-graph with nodes or edges from the original graph that are defined
  // in a parent->child relationship; in other words, we don't add nodes
  // to the meta-graph for every node and edge in the orignal graph,
  // necessarily.
  private final DynamicGraph m_metaGraph =
    DynamicGraphFactory.instantiateDynamicGraph();

  // An index in the array corresponds to a node in the meta graph;
  // the value at that index, if strictly positive, is one plus the native
  // node in the original graph; if strictly negative, is the complement
  // of the native edge in the original graph.
  private final IntArray m_metaToNativeInxMap = new IntArray();

  private final IntIntHash m_nativeToMetaNodeInxMap = new IntIntHash();
  private final IntIntHash m_nativeToMetaEdgeInxMap = new IntIntHash();

  // Package visible constructor.
  FRootGraph() { }

}
