// ActivePathFinderParameters.java
//---------------------------------------------------------------------------------------
// $Revision$   
// $Date$ 
// $Author$
//-----------------------------------------------------------------------------------
package csplugins.jActiveModules.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Map;

import cytoscape.Cytoscape;
import cytoscape.CyNode;
import cytoscape.data.CyAttributes;
import cytoscape.data.CyAttributesUtils;

//---------------------------------------------------------------------------------------
public class ActivePathFinderParameters {

	double initialTemperature = 1.0;
	double finalTemperature = 0.01;
	double hubAdjustment = 0;
	int totalIterations = 2500;
	int numberOfPaths = 5;
	int displayInterval = 500;
	int pseudoRandomSeed = Math.abs((int) System.currentTimeMillis());
	int minHubSize = 0;
	int randomSeed = pseudoRandomSeed;
	int searchDepth = 1;
	int maxDepth = 2;
	boolean enableMaxDepth = true;
	double overlapThreshold = 0.8;
	boolean toQuench = true;
	boolean edgesNotNodes = false;
	boolean toUseMCFile = false;
	boolean mcBoolean = true;
	String mcFileName = "";
	boolean isDefault = true;
	boolean regionalBoolean = true;
	boolean searchFromNodes = false;
	boolean exit = false;
	int maxThreads = 1;
	boolean greedySearch = true;
	boolean enableFiltering = true;
	boolean run = false;
	List expressionAttrs = new ArrayList();

	//boolean enableSpokePenalty = false;
	
	// ---------------------------------------------------------------------------------------
	public ActivePathFinderParameters() {
	} // default ctor

	// ---------------------------------------------------------------------------------------
	/*
	 * public ActivePathFinderParameters (double initialTemperature, double
	 * finalTemperature, double hubAdjustment, int totalIterations, int
	 * numberOfPaths, int displayInterval, int minHubSize, int randomSeed, int
	 * searchDepth, int maxDepth, boolean toQuench, boolean edgesNotNodes,
	 * boolean toUseMCFile, boolean mcBoolean, String mcFileName, boolean
	 * regionalBoolean, boolean searchFromNodes, int maxThreads, boolean exit,
	 * boolean greedySearch)
	 *  { this.initialTemperature = initialTemperature; this.finalTemperature =
	 * finalTemperature; this.hubAdjustment = hubAdjustment;
	 * this.totalIterations = totalIterations; this.numberOfPaths =
	 * numberOfPaths; this.displayInterval = displayInterval; this.minHubSize =
	 * minHubSize; this.randomSeed = randomSeed; this.searchDepth = searchDepth;
	 * this.maxDepth = maxDepth; this.toQuench = toQuench; this.edgesNotNodes =
	 * edgesNotNodes; this.toUseMCFile = toUseMCFile; this.mcBoolean =
	 * mcBoolean; this.mcFileName = mcFileName; this.regionalBoolean =
	 * regionalBoolean; this.searchFromNodes = searchFromNodes; this.isDefault =
	 * false; this.maxThreads = maxThreads; this.exit = exit; this.greedySearch =
	 * greedySearch; } // full ctor
	 */
	public ActivePathFinderParameters(ActivePathFinderParameters oldAPFP) {
		setParams(oldAPFP);
	} // copy ctor

	public void setParams(ActivePathFinderParameters oldAPFP) {
		this.initialTemperature = oldAPFP.getInitialTemperature();
		this.finalTemperature = oldAPFP.getFinalTemperature();
		this.hubAdjustment = oldAPFP.getHubAdjustment();
		this.totalIterations = oldAPFP.getTotalIterations();
		this.numberOfPaths = oldAPFP.getNumberOfPaths();
		this.displayInterval = oldAPFP.getDisplayInterval();
		this.minHubSize = oldAPFP.getMinHubSize();
		this.randomSeed = oldAPFP.getRandomSeed();
		this.searchDepth = oldAPFP.getSearchDepth();
		this.maxDepth = oldAPFP.getMaxDepth();
		this.toQuench = oldAPFP.getToQuench();
		this.edgesNotNodes = oldAPFP.getEdgesNotNodes();
		this.toUseMCFile = oldAPFP.getToUseMCFile();
		this.mcBoolean = oldAPFP.getMCboolean();
		this.mcFileName = oldAPFP.getMcFileName();
		this.regionalBoolean = oldAPFP.getRegionalBoolean();
		this.searchFromNodes = oldAPFP.searchFromNodes;
		this.isDefault = false;
		this.maxThreads = oldAPFP.getMaxThreads();
		this.exit = oldAPFP.getExit();
		this.greedySearch = oldAPFP.getGreedySearch();
		this.overlapThreshold = oldAPFP.getOverlapThreshold();
		this.enableFiltering = oldAPFP.getEnableFiltering();
		this.enableMaxDepth = oldAPFP.getEnableMaxDepth();
		this.run = oldAPFP.getRun();
		setExpressionAttributes(oldAPFP.getExpressionAttributes());
		
	} // copy ctor

	public boolean getRun(){
		return this.run;
	}
	
	public void setRun(boolean newValue){
		this.run = newValue;
	}
	
	public boolean getEnableMaxDepth(){
		return this.enableMaxDepth;
	}
	
	public void setEnableMaxDepth(boolean newValue){
		this.enableMaxDepth = newValue;
	}
	public boolean getEnableFiltering(){
		return this.enableFiltering;
	}
	
	public void setEnableFiltering(boolean newValue){
		this.enableFiltering = newValue;
	}
	
	public void setExit(boolean flag) {
		exit = flag;
	}

	public boolean getExit() {
		return exit;
	}
	public boolean getSearchFromNodes() {
		return searchFromNodes;
	}
	public void setSearchFromNodes(boolean newValue) {
		searchFromNodes = newValue;
		this.isDefault = false;
	}

	public double getOverlapThreshold() {
		return overlapThreshold;
	}

	public void setOverlapThreshold(double newValue) {
		if (newValue < 0 || newValue > 1) {
			throw new IllegalArgumentException("Invalid overlap value: "
					+ newValue);
		}
		this.overlapThreshold = newValue;
	}

	public boolean getRegionalBoolean() {
		return regionalBoolean;
	}
	public void setRegionalBoolean(boolean newValue) {
		regionalBoolean = newValue;
		this.isDefault = false;
	}
	public boolean getToUseMCFile() {
		return toUseMCFile;
	}
	public void setToUseMCFile(boolean newValue) {
		toUseMCFile = newValue;
		this.isDefault = false;
	}
	public boolean getMCboolean() {
		return mcBoolean;
	}
	public void setMCboolean(boolean newValue) {
		mcBoolean = newValue;
		this.isDefault = false;
	}
	public String getMcFileName() {
		return mcFileName;
	}
	public void setMcFileName(String newValue) {
		mcFileName = newValue;
		this.isDefault = false;
	}
	public double getInitialTemperature() {
		return initialTemperature;
	}
	public void setInitialTemperature(double newValue) {
		initialTemperature = newValue;
		this.isDefault = false;
	}
	public double getFinalTemperature() {
		return finalTemperature;
	}
	public void setFinalTemperature(double newValue) {
		finalTemperature = newValue;
		this.isDefault = false;
	}
	public int getTotalIterations() {
		return totalIterations;
	}
	public void setTotalIterations(int newValue) {
		totalIterations = newValue;
		this.isDefault = false;
	}
	public int getNumberOfPaths() {
		return numberOfPaths;
	}
	public void setNumberOfPaths(int newValue) {
		numberOfPaths = newValue;
		this.isDefault = false;
	}
	public int getDisplayInterval() {
		return displayInterval;
	}
	public void setDisplayInterval(int newValue) {
		displayInterval = newValue;
		this.isDefault = false;
	}
	public int getRandomSeed() {
		return randomSeed;
	}
	public void setRandomSeed(int newValue) {
		randomSeed = newValue;
		this.isDefault = false;
	}
	public int getSearchDepth() {
		return searchDepth;
	}
	public void setSearchDepth(int newValue) {
		if (newValue < 0) {
			throw new IllegalArgumentException("Search depth must be > 0");
		}
		searchDepth = newValue;
		this.isDefault = false;
	}
	public int getMaxDepth() {
		return maxDepth;
	}
	public void setMaxDepth(int newValue) {
		maxDepth = newValue;
		this.isDefault = false;
	}
	public int getMinHubSize() {
		return minHubSize;
	}
	public void setMinHubSize(int newValue) {
		minHubSize = newValue;
		this.isDefault = false;
	}
	public double getHubAdjustment() {
		return hubAdjustment;
	}
	public void setHubAdjustment(double newValue) {
		hubAdjustment = newValue;
		this.isDefault = false;
	}
	public boolean getToQuench() {
		return toQuench;
	}
	public void setToQuench(boolean newValue) {
		toQuench = newValue;
		this.isDefault = false;
	}
	public boolean getEdgesNotNodes() {
		return edgesNotNodes;
	}
	

	public boolean getGreedySearch() {
		return greedySearch;
	}
	
	public void setGreedySearch(boolean newValue){
		this.greedySearch = newValue;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public void reloadExpressionAttributes() {

		expressionAttrs.clear();

		// find all of the double type parameters
                CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
                String[] names = nodeAttrs.getAttributeNames();
                for ( String name : names ) {
                        if ( nodeAttrs.getType(name) == CyAttributes.TYPE_FLOATING ) {
				Map attrMap = CyAttributesUtils.getAttribute(name,nodeAttrs);

				if ( attrMap == null ) 
					continue; // no values have been defined for the attr yet

				boolean isPValue = true;
				for ( Object value : attrMap.values() ) {
					double d = ((Double)value).doubleValue();
					if ( d < 0 || d > 1 ) {
						isPValue = false;
						break;
					}
				}
					
				if ( isPValue )
					expressionAttrs.add(name);
			}
		}
	}

	public Collection getExpressionAttributes() {
		return expressionAttrs;
	}

	public void setExpressionAttributes(Collection names) {
		expressionAttrs.clear();
		expressionAttrs.addAll(names);
	}

	public void addExpressionAttribute(String name) {
		if ( !expressionAttrs.contains(name) )
			expressionAttrs.add(name);
	}

	public void removeExpressionAttribute(String name) {
		expressionAttrs.remove(name); 
	}

	public void shuffleExpressionAttributes(Random r) {
		Collections.shuffle(expressionAttrs,r);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("      initial temperature: " + initialTemperature + "\n");
		sb.append("        final temperature: " + finalTemperature + "\n");
		sb.append("         total iterations: " + totalIterations + "\n");
		sb.append("          number of paths: " + numberOfPaths + "\n");
		sb.append("         display interval: " + displayInterval + "\n");
		sb.append("         minimum hub size: " + minHubSize + "\n");
		sb.append("       hub penalty factor: " + hubAdjustment + "\n");
		sb.append("              random seed: " + randomSeed + "\n");
		sb.append("                   quench: " + toQuench + "\n");
		sb.append("          edges not nodes: " + edgesNotNodes + "\n");
		sb.append("            use MC at all: " + mcBoolean + "\n");
		sb.append("                 MC file?: " + toUseMCFile + "\n");
		sb.append("              MC filename: " + mcFileName + "\n");
		sb.append("         regional scoring: " + regionalBoolean + "\n");
		sb.append("             search depth: " + searchDepth + "\n");
		sb.append("                max depth: " + maxDepth + "\n");
		sb.append("        search from nodes: " + searchFromNodes + "\n");
		sb.append("              max threads: " + maxThreads + "\n");
		return sb.toString();

	} // toString

} // class ActivePathFinderParameters

