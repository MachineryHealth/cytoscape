
//============================================================================
// 
//  file: BasicEdge.java 
// 
//  Copyright (c) 2006, University of California, San Diego
//  All rights reverved.
// 
//============================================================================


package nct.graph.basic;

import nct.graph.Edge;

/**
 * A generic implementation of the Edge interface.
 */
public class BasicEdge<NodeType extends Comparable<NodeType>,
                       WeightType extends Comparable<WeightType>> 
	implements Edge<NodeType,WeightType> {

	/**
	 * The source node of the edge.
	 */
	protected NodeType sourceNode;

	/**
	 * The target node of the edge.
	 */
	protected NodeType targetNode;

	/**
	 * The edge weight.
	 */
	protected WeightType weight;

	/**
	 * The edge description.
	 */
	protected String description;

	/**
	 * @param sourceNode The source node of the edge.
	 * @param targetNode The target node of the edge.
	 * @param weight The edge weight.
	 * @param desc The edge description.
	 */
	public BasicEdge(NodeType sourceNode, NodeType targetNode, WeightType weight, String desc) {
		
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.weight = weight;
		this.description = desc;
	}

	/**
	 * @param sourceNode The source node of the edge.
	 * @param targetNode The target node of the edge.
	 * @param weight The edge weight.
	 */
	public BasicEdge(NodeType sourceNode, NodeType targetNode, WeightType weight) {
		this(sourceNode,targetNode,weight,weight.toString());
	}

        /**
         * @return The weight of this edge.
         */
	public WeightType getWeight() {
		return weight;
	}

        /**
         * @param desc The description of the edge.
         */
	public void setDescription(String desc) {
		description = desc;	
	}

        /**
         * @return The description of the edge.
         */
	public String getDescription() {
		return description;
	}

	/**
	 * First compares whether the source and target nodes are the same and if
	 * so assumes the edge is the same. If not, then compares the edge weights.
	 * @param e The other edge to compare against this one.
	 */
	public int compareTo(Edge<NodeType,WeightType> e) {
		
		if ( sourceNode.equals( e.getSourceNode() ) && 
                     targetNode.equals( e.getTargetNode() ) )
			return 0;

		return weight.compareTo( e.getWeight() );
	}

	/**
	 * @param o The edge to compare against this one.
	 * @return True if nodes, description, and weigt are all equal.
	 */
	public boolean equals(Object o) {
		Edge<NodeType,WeightType> e = (Edge<NodeType,WeightType>)o; 
		if ( sourceNode.equals( e.getSourceNode() ) && 
		     targetNode.equals( e.getTargetNode() ) &&
		     description.equals( e.getDescription() ) &&
		     weight.equals( e.getWeight() ) )
		     	return true;
		else
			return false;
	}

	/**
	 * @return A unique hashcode of this instantiation.  
	 */
	public int hashCode() {
		String s = sourceNode.toString() +  weight.toString() + 
		           description + targetNode.toString();
		return s.hashCode();
	}

        /**
         * @return The source node of this edge.
         */
	public NodeType getSourceNode() {
		return sourceNode;
	}

        /**
         * @return The target node of this edge.
         */
	public NodeType getTargetNode() {
		return targetNode;
	}

	/**
	 * @return A string listing all elements of the edge.
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("source: ");
		s.append(sourceNode.toString());
		s.append("  target: ");
		s.append(targetNode.toString());
		s.append("  weight: ");
		s.append(weight.toString());
		s.append("  desc: ");
		s.append(description);

		return s.toString();
	}
}
