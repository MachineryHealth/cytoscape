package fing.model.test;

import fing.model.FingRootGraphFactory;
import giny.model.Edge;
import giny.model.GraphPerspective;
import giny.model.Node;
import giny.model.RootGraph;

import java.util.Iterator;

public final class AddRemoveTest
{

  // No constructor.
  private AddRemoveTest() { }

  public static final void main(String[] args)
    throws ClassNotFoundException, InstantiationException,
           IllegalAccessException
  {
    final RootGraph root = getRootGraph(args);

    int[] nodeInx;
    final int[] edgeInx = new int[100000];
    final int[] nodeNums = new int[] { 20000, 19990, 20020 };
    final int iterations = 10000;
    for (int foo = 0; foo < iterations; foo++)
    {
      boolean print = true;
      if (!(foo % 10 == 0)) print = false;
      if (print) System.out.println("at add/remove iteration " + (foo + 1) + 
                                    " of " + iterations);
      final int numNodes = nodeNums[foo % nodeNums.length];
      if (print) System.out.println("creating " + numNodes + " nodes");
      nodeInx = root.createNodes(numNodes);
      if (print) System.out.println("creating " + edgeInx.length + " edges");
      for (int i = 0; i < edgeInx.length; i++)
        edgeInx[i] = root.createEdge(nodeInx[i % nodeInx.length],
                                     nodeInx[(i * 3) % nodeInx.length]);
      if (print) System.out.println("creating GraphPerspective");
      GraphPerspective persp = root.createGraphPerspective(nodeInx, edgeInx);
      if (print) printme(root, persp);
      if (print) System.out.println("removing all edges from RootGraph");
      root.removeEdges(edgeInx);
      if (print) System.out.println("removing all nodes from RootGraph");
      root.removeNodes(nodeInx);
      if (print) printme(root, persp);
    }
  }

  private static void printme(RootGraph root,
                              GraphPerspective persp) {
    System.out.println("in RootGraph: " + root.getNodeCount() + " nodes and " +
                       root.getEdgeCount() + " edges");
    System.out.println("in GraphPerspective: " + persp.getNodeCount() +
                       " nodes and " + persp.getEdgeCount() + " edges");
    System.out.println(); }

  private static final RootGraph getRootGraph(String[] mainArgs)
    throws ClassNotFoundException, InstantiationException,
           IllegalAccessException {
    if (mainArgs.length > 0 && mainArgs[0].equalsIgnoreCase("luna"))
      return (RootGraph) Class.forName("luna.LunaRootGraph").newInstance();
    else return FingRootGraphFactory.instantiateRootGraph(); }

}
