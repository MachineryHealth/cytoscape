package cytoscape.graph.dynamic.util;

import cytoscape.graph.dynamic.DynamicGraph;
import cytoscape.util.intr.IntEnumerator;
import cytoscape.util.intr.IntIterator;
import cytoscape.util.intr.IntStack;

final class DynamicGraphRepresentation implements DynamicGraph
{

  private int m_nodeCount;
  private Node m_firstNode;
  private int m_maxNode;
  private int m_edgeCount;
  private int m_maxEdge;
  private final NodeArray m_nodes;
  private final EdgeArray m_edges;
  private final IntStack m_freeNodes;
  private final IntStack m_freeEdges;
  private final EdgeDepot m_edgeDepot;
  private final NodeDepot m_nodeDepot;

  // Use this as a bag of integers in various operations.  Don't forget to
  // empty() it before using it.
  private final IntStack m_stack;

  DynamicGraphRepresentation()
  {
    m_nodeCount = 0;
    m_firstNode = null;
    m_maxNode = -1;
    m_edgeCount = 0;
    m_maxEdge = -1;
    m_nodes = new NodeArray();
    m_edges = new EdgeArray();
    m_freeNodes = new IntStack();
    m_freeEdges = new IntStack();
    m_edgeDepot = new EdgeDepot();
    m_nodeDepot = new NodeDepot();
    m_stack = new IntStack();
  }

  public IntEnumerator nodes()
  {
    final int nodeCount = m_nodeCount;
    final Node firstNode = m_firstNode;
    return new IntEnumerator() {
        private int numRemaining = nodeCount;
        private Node node = firstNode;
        public int numRemaining() { return numRemaining; }
        public int nextInt() {
          final int returnThis = node.nodeId;
          node = node.nextNode;
          numRemaining--;
          return returnThis; } };
  }

  public IntEnumerator edges()
  {
    final int edgeCount = m_edgeCount;
    final Node firstNode = m_firstNode;
    return new IntEnumerator() {
        private int numRemaining = edgeCount;
        private Node node = firstNode;
        private Edge edge = null;
        public int numRemaining() { return numRemaining; }
        public int nextInt() {
          final int returnThis;
          if (edge != null) returnThis = edge.edgeId;
          else {
            for (edge = node.firstOutEdge;
                 edge == null;
                 node = node.nextNode, edge = node.firstOutEdge) { }
            node = node.nextNode;
            returnThis = edge.edgeId; }
          edge = edge.nextOutEdge;
          numRemaining--;
          return returnThis; } };
  }

  public int createNode()
  {
    final Node n = m_nodeDepot.getNode();
    final int returnThis;
    if (m_freeNodes.size() > 0) returnThis = m_freeNodes.pop();
    else returnThis = ++m_maxNode;
    m_nodes.setNodeAtIndex(n, returnThis);
    m_nodeCount++;
    n.nextNode = m_firstNode;
    if (m_firstNode != null) m_firstNode.prevNode = n;
    m_firstNode = n;
    n.nodeId = returnThis;
    n.outDegree = 0; n.inDegree = 0; n.undDegree = 0; n.selfEdges = 0;
    return returnThis;
  }

  public int removeNode(final int node)
  {
    final IntEnumerator edges = adjacentEdges(node, true, true, true);
    if (edges == null) return -1;
    m_stack.empty();
    while (edges.numRemaining() > 0) m_stack.push(edges.nextInt());
    while (m_stack.size() > 0) removeEdge(m_stack.pop());
    final Node n = m_nodes.getNodeAtIndex(node);
    if (n.prevNode != null) n.prevNode.nextNode = n.nextNode;
    else m_firstNode = n.nextNode;
    if (n.nextNode != null) n.nextNode.prevNode = n.prevNode;
    m_nodes.setNodeAtIndex(null, node);
    m_freeNodes.push(node);
    n.prevNode = null; n.firstOutEdge = null; n.firstInEdge = null;
    m_nodeDepot.recycleNode(n);
    m_nodeCount--;
    return node;
  }

  public int createEdge(int sourceNode, int targetNode, boolean directed)
  {
    if (sourceNode < 0 || sourceNode == Integer.MAX_VALUE) return -1;
    final Node source = m_nodes.getNodeAtIndex(sourceNode);
    if (targetNode < 0 || targetNode == Integer.MAX_VALUE) return -1;
    final Node target = m_nodes.getNodeAtIndex(targetNode);
    if (source == null || target == null) return -1;
    final Edge e = m_edgeDepot.getEdge();
    final int returnThis;
    if (m_freeEdges.size() > 0) returnThis = m_freeEdges.pop();
    else returnThis = ++m_maxEdge;
    m_edges.setEdgeAtIndex(e, returnThis);
    m_edgeCount++;
    if (directed) { source.outDegree++; target.inDegree++; }
    else { source.undDegree++; target.undDegree++; }
    if (source == target) { // Self-edge.
      if (directed) source.selfEdges++;
      else source.undDegree--; }
    e.nextOutEdge = source.firstOutEdge;
    if (source.firstOutEdge != null) source.firstOutEdge.prevOutEdge = e;
    source.firstOutEdge = e;
    e.nextInEdge = target.firstInEdge;
    if (target.firstInEdge != null) target.firstInEdge.prevInEdge = e;
    target.firstInEdge = e;
    e.edgeId = returnThis;
    e.directed = directed;
    e.sourceNode = sourceNode;
    e.targetNode = targetNode;
    return returnThis;
  }

  public int removeEdge(final int edge)
  {
    if (edge < 0 || edge == Integer.MAX_VALUE) return -1;
    final Edge e = m_edges.getEdgeAtIndex(edge);
    if (e == null) return -1;
    final Node source = m_nodes.getNodeAtIndex(e.sourceNode);
    final Node target = m_nodes.getNodeAtIndex(e.targetNode);
    if (e.prevOutEdge != null) e.prevOutEdge.nextOutEdge = e.nextOutEdge;
    else source.firstOutEdge = e.nextOutEdge;
    if (e.nextOutEdge != null) e.nextOutEdge.prevOutEdge = e.prevOutEdge;
    if (e.prevInEdge != null) e.prevInEdge.nextInEdge = e.nextInEdge;
    else target.firstInEdge = e.nextInEdge;
    if (e.nextInEdge != null) e.nextInEdge.prevInEdge = e.prevInEdge;
    if (e.directed) { source.outDegree--; target.inDegree--; }
    else { source.undDegree--; target.undDegree--; }
    if (source == target) { // Self-edge.
      if (e.directed) source.selfEdges--;
      else source.undDegree++; }
    m_edges.setEdgeAtIndex(null, edge);
    m_freeEdges.push(edge);
    e.prevOutEdge = null; e.nextInEdge = null; e.prevInEdge = null;
    m_edgeDepot.recycleEdge(e);
    m_edgeCount--;
    return edge;
  }

  public boolean containsNode(int node)
  {
    if (node < 0 || node == Integer.MAX_VALUE) return false;
    return m_nodes.getNodeAtIndex(node) != null;
  }

  public byte edgeType(int edge)
  {
    if (edge < 0 || edge == Integer.MAX_VALUE) return -1;
    final Edge e = m_edges.getEdgeAtIndex(edge);
    if (e == null) return -1;
    if (e.directed) return 1; return 0;
  }

  public int sourceNode(int edge)
  {
    if (edge < 0 || edge == Integer.MAX_VALUE) return -1;
    final Edge e = m_edges.getEdgeAtIndex(edge);
    if (e == null) return -1;
    return e.sourceNode;
  }

  public int targetNode(int edge)
  {
    if (edge < 0 || edge == Integer.MAX_VALUE) return -1;
    final Edge e = m_edges.getEdgeAtIndex(edge);
    if (e == null) return -1;
    return e.targetNode;
  }

  public IntEnumerator adjacentEdges(int node,
                                     final boolean outgoing,
                                     final boolean incoming,
                                     final boolean undirected)
  {
    if (node < 0 || node == Integer.MAX_VALUE) return null;
    final Node n = m_nodes.getNodeAtIndex(node);
    if (n == null) return null;
    final Edge[] edgeLists;
    if (undirected || (outgoing && incoming)) {
      edgeLists = new Edge[] { n.firstOutEdge, n.firstInEdge }; }
    else if (outgoing) { // Cannot also be incoming.
      edgeLists = new Edge[] { n.firstOutEdge, null }; }
    else if (incoming) { // Cannot also be outgoing.
      edgeLists = new Edge[] { null, n.firstInEdge }; }
    else { // All boolean input parameters are false.
      edgeLists = new Edge[] { null, null }; }
    int tentativeEdgeCount = 0;
    if (outgoing) tentativeEdgeCount += n.outDegree;
    if (incoming) tentativeEdgeCount += n.inDegree;
    if (undirected) tentativeEdgeCount += n.undDegree;
    if (outgoing && incoming) tentativeEdgeCount -= n.selfEdges;
    final int edgeCount = tentativeEdgeCount;
    return new IntEnumerator() {
        private int numRemaining = edgeCount;
        private int edgeListIndex = -1;
        private Edge edge = null;
        public int numRemaining() { return numRemaining; }
        public int nextInt() {
          while (edge == null) edge = edgeLists[++edgeListIndex];
          int returnThis = -1;
          if (edgeListIndex == 0) {
            while (edge != null &&
                   !((outgoing && edge.directed) ||
                     (undirected && !edge.directed))) {
              edge = edge.nextOutEdge;
              if (edge == null) {
                edge = edgeLists[++edgeListIndex];
                break; } }
            if (edge != null && edgeListIndex == 0) {
              returnThis = edge.edgeId;
              edge = edge.nextOutEdge; } }
          if (edgeListIndex == 1) {
            while ((edge.sourceNode == edge.targetNode &&
                    ((outgoing && edge.directed) ||
                     (undirected && !edge.directed))) ||
                   !((incoming && edge.directed) ||
                     (undirected && !edge.directed))) {
              edge = edge.nextInEdge; }
            returnThis = edge.edgeId;
            edge = edge.nextInEdge; }
          numRemaining--;
          return returnThis; } };   
  }

  public IntIterator connectingEdges(int node0, int node1,
                                     boolean outgoing, boolean incoming,
                                     boolean undirected)
  {
    final IntEnumerator node0Adj = adjacentEdges(node0, outgoing, incoming,
                                                 undirected);
    final IntEnumerator node1Adj = adjacentEdges(node1, incoming, outgoing,
                                                 undirected);
    if (node0Adj == null || node1Adj == null) return null;
    final DynamicGraph graph = this;
    final IntEnumerator theAdj;
    final int nodeZero;
    final int nodeOne;
    if (node0Adj.numRemaining() <= node1Adj.numRemaining()) {
      theAdj = node0Adj; nodeZero = node0; nodeOne = node1; }
    else {
      theAdj = node1Adj; nodeZero = node1; nodeOne = node0; }
    return new IntIterator() {
        private int nextEdge = -1;
        private void ensureComputeNext() {
          if (nextEdge != -1) return;
          while (theAdj.numRemaining() > 0) {
            final int edge = theAdj.nextInt();
            if (nodeOne == (nodeZero ^ graph.sourceNode(edge) ^
                graph.targetNode(edge))) {
              nextEdge = edge; return; } }
          nextEdge = -2; }
        public boolean hasNext() {
          ensureComputeNext();
          if (nextEdge < 0) return false;
          else return true; }
        public int nextInt() {
          ensureComputeNext();
          final int returnThis = nextEdge;
          nextEdge = -1;
          return returnThis; } };
  }

}
