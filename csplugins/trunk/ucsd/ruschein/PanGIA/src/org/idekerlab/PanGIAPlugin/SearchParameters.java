package org.idekerlab.PanGIAPlugin;


import cytoscape.CyNetwork;
import cytoscape.util.ScalingMethod;


public final class SearchParameters {
	private CyNetwork physicalNetwork;
	private CyNetwork geneticNetwork;
	private String physicalEdgeAttrName;
	private String geneticEdgeAttrName;
	private String physicalScalingMethod;
	private String geneticScalingMethod;
		
	private double alpha;
	private double alphaMultiplier;
	private double physicalNetworkFilterDegree;
	
	private double pValueThreshold;
	private int numberOfSamples;
	
	public SearchParameters() {
	}

	public void setPhysicalNetwork(CyNetwork network) {
		this.physicalNetwork = network;
	}

	public CyNetwork getPhysicalNetwork() {
		return physicalNetwork;
	}

	public void setGeneticNetwork(CyNetwork network) {
		this.geneticNetwork = network;
	}

	public CyNetwork getGeneticNetwork() {
		return geneticNetwork;
	}

	public void setPhysicalEdgeAttrName(String physicalEdgeAttrName) {
		this.physicalEdgeAttrName = physicalEdgeAttrName;
	}

	public String getPhysicalEdgeAttrName() {
		return physicalEdgeAttrName;
	}

	public void setGeneticEdgeAttrName(String geneticEdgeAttrName) {
		this.geneticEdgeAttrName = geneticEdgeAttrName;
	}

	public String getGeneticEdgeAttrName() {
		return geneticEdgeAttrName;
	}

	public void setPhysicalScalingMethod(final String physicalScalingMethod) {
		this.physicalScalingMethod = physicalScalingMethod;
	}

	public ScalingMethod getPhysicalScalingMethod() {
		return ScalingMethod.getEnumValue(physicalScalingMethod);
	}

	public void setGeneticScalingMethod(final String geneticScalingMethod) {
		this.geneticScalingMethod = geneticScalingMethod;
	}

	public ScalingMethod getGeneticScalingMethod() {
		return ScalingMethod.getEnumValue(geneticScalingMethod);
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlphaMultiplier(final double alphaMultiplier) {
		this.alphaMultiplier = alphaMultiplier;
	}

	public double getAlphaMultiplier() {
		return alphaMultiplier;
	}

	public void setPhysicalNetworkFilterDegree(final double physicalNetworkFilterDegree) {
		this.physicalNetworkFilterDegree = physicalNetworkFilterDegree;
	}

	public double getPhysicalNetworkFilterDegree() {
		return physicalNetworkFilterDegree;
	}

	public void setPValueThreshold(final double pValueThreshold) {
		this.pValueThreshold = pValueThreshold;
	}

	public double getPValueThreshold() {
		return pValueThreshold;
	}

	public void setNumberOfSamples(final int numberOfSamples) {
		this.numberOfSamples = numberOfSamples;
	}

	public int getNumberOfSamples() {
		return numberOfSamples;
	}
}
