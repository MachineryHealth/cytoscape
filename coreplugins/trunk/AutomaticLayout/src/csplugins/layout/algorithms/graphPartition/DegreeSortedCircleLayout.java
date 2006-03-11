package csplugins.layout.algorithms.graphPartition;

import java.util.*;

import cern.colt.list.*;

import cytoscape.*;
import cytoscape.view.*;
import cytoscape.data.*;
import giny.view.*;
import giny.model.*;

import filter.cytoscape.*;

public class DegreeSortedCircleLayout extends AbstractLayout
{
  public DegreeSortedCircleLayout(CyNetwork _network)
  {
    super(_network);
  }

  public void layoutPartion(GraphPerspective _graph)
  {
    // get the array of node indices
    int nodeIndicesArray[] = _graph.getNodeIndicesArray();

    // create a new array that is the Nodes corresponding to the node indices
    Node sortedNodes[] = new Node[nodeIndicesArray.length];
    for (int i = 0; i < nodeIndicesArray.length; i++)
    {
      sortedNodes[i] = _graph.getNode(nodeIndicesArray[i]);
    }

    // sort the Nodes based on the degree
    Arrays.sort(sortedNodes, new Comparator()
      {
        public int compare(Object o1, Object o2)
        {
          Node node1 = (Node) o1;
          Node node2 = (Node) o2;

          return (Cytoscape.getCurrentNetwork().getDegree(
                                    node2.getRootGraphIndex()) -
                  Cytoscape.getCurrentNetwork().getDegree(
                                    node1.getRootGraphIndex()));
        }

        public boolean equals(Object o)
        {
          return false;
        }
      }
    );

    // place each Node in a circle
    int r = 100 * (int) Math.sqrt(sortedNodes.length);
    double phi = 2 * Math.PI / sortedNodes.length;
    for (int i = 0; i < sortedNodes.length; i++)
    {
      Node node = sortedNodes[i];
      layout.setX(node, r + r * Math.sin(i * phi));
      layout.setY(node, r + r * Math.cos(i * phi));
    }
  }
}
