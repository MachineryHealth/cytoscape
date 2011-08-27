/* vim: set ts=2: */
/**
 * Copyright (c) 2008 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package metaNodePlugin2.data;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.groups.CyGroup;

import metaNodePlugin2.model.MetaNode;
import metaNodePlugin2.model.MetaNodeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The AttributeHandler class has two major functions.  First, through the use
 * of static methods, it maintains and manages the map of AttributeHandler objects
 * for each attribute being managed.  Second, the AttributeHandler objects
 * themselves handle the aggregation of attributes.
 */
public class AttributeManager {
	static public final String OVERRIDE_ATTRIBUTE = "__MetanodeAggregation";
	static public final String ENABLE_ATTRIBUTE = "__MetanodeAggregationEnabled";
	static public final String CHILDREN_ATTRIBUTE = "NumChildren";
	static public final String DESCENDENTS_ATTRIBUTE = "NumDescendents";

	private AttributeHandlingType[] defaultHandling = new AttributeHandlingType[15];
	private Map<String, AttributeHandler>handlerMap = null;
	private Map<String, AttributeHandler>saveHandlerMap = null;
	private boolean aggregating = false;
	private CyNetwork network;

	static AttributeHandlingType[] stringArray = {AttributeHandlingType.MCV, AttributeHandlingType.CSV, 
	                                              AttributeHandlingType.TSV, AttributeHandlingType.NONE};

	static AttributeHandlingType[] intArray = {AttributeHandlingType.AVG, AttributeHandlingType.SUM, 
	                                           AttributeHandlingType.MIN, AttributeHandlingType.MAX, 
	                                           AttributeHandlingType.MEDIAN, AttributeHandlingType.NONE}; 

	static AttributeHandlingType[] doubleArray = {AttributeHandlingType.AVG, AttributeHandlingType.SUM, 
	                                              AttributeHandlingType.MIN, AttributeHandlingType.MAX, 
	                                              AttributeHandlingType.MEDIAN, AttributeHandlingType.NONE}; 

	static AttributeHandlingType[] listArray = {AttributeHandlingType.CONCAT, AttributeHandlingType.NONE};

	static AttributeHandlingType[] booleanArray = {AttributeHandlingType.OR, AttributeHandlingType.AND, 
	                                               AttributeHandlingType.NONE};

	static AttributeHandlingType[] emptyArray = {AttributeHandlingType.NONE};

	/**************************************************************************
	 * Static (Class) methods for AttributeHandler                            *
	 *************************************************************************/

	/**
 	 * Return the allowable AttributeHandlingTypes for TYPE_STRING attributes
 	 *
 	 * @return array of AttributeHandlingType values
 	 */
	static public AttributeHandlingType[] getStringOptions() { return stringArray; }

	/**
 	 * Return the allowable AttributeHandlingTypes for TYPE_INTEGER attributes
 	 *
 	 * @return array of AttributeHandlingType values
 	 */
	static public AttributeHandlingType[] getIntOptions() { return intArray; }

	/**
 	 * Return the allowable AttributeHandlingTypes for TYPE_FLOATING attributes
 	 *
 	 * @return array of AttributeHandlingType values
 	 */
	static public AttributeHandlingType[] getDoubleOptions() { return doubleArray; }

	/**
 	 * Return the allowable AttributeHandlingTypes for TYPE_LIST attributes
 	 *
 	 * @return array of AttributeHandlingType values
 	 */
	static public AttributeHandlingType[] getListOptions() { return listArray; }

	/**
 	 * Return the allowable AttributeHandlingTypes for TYPE_BOOLEAN attributes
 	 *
 	 * @return array of AttributeHandlingType values
 	 */
	static public AttributeHandlingType[] getBooleanOptions() { return booleanArray; }

	/**
 	 * Return the allowable AttributeHandlingTypes for the attribute type
 	 *
 	 * @param type CyAttribute type
 	 * @return array of AttributeHandlingType values for that type
 	 */
	static public AttributeHandlingType[] getHandlingOptions(byte type) {
		switch(type) {
			case CyAttributes.TYPE_BOOLEAN:
				return getBooleanOptions();
			case CyAttributes.TYPE_INTEGER:
				return getIntOptions();
			case CyAttributes.TYPE_FLOATING:
				return getDoubleOptions();
			case CyAttributes.TYPE_STRING:
				return getStringOptions();
			case CyAttributes.TYPE_SIMPLE_LIST:
				return getListOptions();
			default:
				return emptyArray;
		}
	}

	/**************************************************************************
	 * Instance methods for AttributeManager                                  *
	 *************************************************************************/
	public AttributeManager () {
	}

	public AttributeManager (AttributeManager source) {
		this.handlerMap = source.handlerMap;
		this.saveHandlerMap = source.saveHandlerMap;
		this.aggregating = source.aggregating;
		for (int index = 0; index < 15; index++)
			this.defaultHandling[index] = source.defaultHandling[index];
	}
	

	/**
 	 * Add a new handler to our internal map for aggregating the designated
 	 * attribute and handlerType
 	 *
 	 * @param metaContext the metanode context for this handler
 	 * @param attribute the attribute this handler is for
 	 * @param handlerType the aggregation method for use by the handler
 	 */
	public void addHandler(MetaNode metaContext, String attribute, AttributeHandlingType handlerType) {
		if (handlerMap == null) handlerMap = new HashMap();

		if (handlerMap.containsKey(attribute)) {
			handlerMap.get(attribute).setHandlerType(handlerType);
		} else {
			handlerMap.put(attribute, new AttributeHandler(attribute, handlerType));
		}

		if (metaContext == null) {
			// Update our list of overrides in the network attributes
			CyAttributes networkAttributes = Cytoscape.getNetworkAttributes();

			// Make sure we have a network to work with
			if (network == null)
				network = Cytoscape.getCurrentNetwork();
			if (networkAttributes.hasAttribute(network.getIdentifier(), OVERRIDE_ATTRIBUTE)) {
				Map<String,String> attrMap = (Map<String,String>)networkAttributes.getMapAttribute(network.getIdentifier(), OVERRIDE_ATTRIBUTE);
				attrMap.put(attribute, handlerType.toString());
				networkAttributes.setMapAttribute(network.getIdentifier(), OVERRIDE_ATTRIBUTE, attrMap);
			}
		} else {
			CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
			String metaNode = metaContext.getGroupNode().getIdentifier();
			if (nodeAttributes.hasAttribute(metaNode, OVERRIDE_ATTRIBUTE)) {
				Map<String,String> attrMap = (Map<String,String>)nodeAttributes.getMapAttribute(metaNode, OVERRIDE_ATTRIBUTE);
				attrMap.put(attribute, handlerType.toString());
				nodeAttributes.setMapAttribute(metaNode, OVERRIDE_ATTRIBUTE, attrMap);
			}
		}
	}

	/**
 	 * Remove a handler for the designated attribute from our internal map of handlers
 	 *
 	 * @param attribute the attribute this handler is for
 	 */
	public void removeHandler(String attribute) {
		if (handlerMap != null && handlerMap.containsKey(attribute)) {
			handlerMap.remove(attribute);
		}
	}

	/**
 	 * Save the current attribute map
 	 */
	public void saveSettings() {
		if (handlerMap == null) return;
		saveHandlerMap = new HashMap();
		for (String attribute: handlerMap.keySet()) {
			AttributeHandler handler = handlerMap.get(attribute);
			saveHandlerMap.put(attribute, new AttributeHandler(handler.getAttribute(),handler.getHandlerType()));
		}
	}

	/**
 	 * Revert the attribute map back to the saved settings.
 	 */
	public void revertSettings() {
		handlerMap = saveHandlerMap;
		saveHandlerMap = null;
	}

	/**
 	 * Clear the attribute handler map
 	 */
	public void clearSettings() {
		handlerMap = null;
		saveHandlerMap = null;
	}

	/**
 	 * Return the attribute handler for a specific attribute
 	 *
 	 * @param attribute the attribute to get the handler for
 	 * @return the AttributeHandler or null if there is no attribute handler
 	 *         for this attribute.
 	 */
	public AttributeHandler getHandler(String attribute) {
		if (handlerMap == null) return null;
		if (handlerMap.containsKey(attribute))
			return handlerMap.get(attribute);
		return null;
	}

	/**
 	 * Load the current mapping of attributes to attribute aggregation options from
 	 * the network attributes for this network.
 	 *
 	 * @param network the CyNetwork we're going to read our options from
 	 * @param metaContext the metaNode we're referring to.  If null, we want the network.
 	 */
	public void loadHandlerMappings(CyNetwork network, MetaNode metaContext) {
		// Load the network defaults
		CyAttributes networkAttributes = Cytoscape.getNetworkAttributes();
		this.network = network;
		if (networkAttributes.hasAttribute(network.getIdentifier(), OVERRIDE_ATTRIBUTE)) {
			Map<String,String> attrMap = (Map<String,String>)networkAttributes.getMapAttribute(network.getIdentifier(), OVERRIDE_ATTRIBUTE);
			for (String attr: attrMap.keySet()) {
				handlerMap.put(attr, new AttributeHandler(attr, stringToType(attrMap.get(attr))));
			}
		}
		if (metaContext == null) return;

		// Now load our specific overrides
		CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
		String metaNode = metaContext.getGroupNode().getIdentifier();
		if (nodeAttributes.hasAttribute(metaNode, OVERRIDE_ATTRIBUTE)) {
			Map<String,String> attrMap = (Map<String,String>)nodeAttributes.getMapAttribute(metaNode, OVERRIDE_ATTRIBUTE);
			for (String attr: attrMap.keySet()) {
				handlerMap.put(attr, new AttributeHandler(attr, stringToType(attrMap.get(attr))));
			}
		}
		this.aggregating = false;
		if (nodeAttributes.hasAttribute(metaNode, ENABLE_ATTRIBUTE)) {
			// System.out.println(ENABLE_ATTRIBUTE+" for "+metaNode+" is "+nodeAttributes.getBooleanAttribute(metaNode, ENABLE_ATTRIBUTE));
			if (nodeAttributes.getBooleanAttribute(metaNode, ENABLE_ATTRIBUTE)) {
				this.aggregating = true;
			}
		}
	}

	/**
 	 * Enable or disable attribute aggregation.
 	 *
 	 * @param enable if 'true' enable aggregation otherwise, disable aggregation
 	 */
	public void setEnable(boolean enable) {
		aggregating = enable;
	}

	/**
 	 * Check to see if attribute aggregation is enabled
 	 *
 	 * @return 'true' if attribute aggregation is enabled otherwise, 'false'
 	 */
	public boolean getEnable() {
		return aggregating;
	}

	public void setDefault(byte attributeType, AttributeHandlingType type) {
		if (attributeType < 0) attributeType += 10;
		defaultHandling[attributeType] = type;
	}

	public AttributeHandlingType getDefault(byte attributeType) {
		if (attributeType < 0) attributeType += 10;
		return defaultHandling[attributeType];
	}

	public AttributeHandler getDefaultHandler(byte attributeType, String attribute) {
		AttributeHandlingType t;
		if (attributeType < 0) attributeType += 10;
		t = defaultHandling[attributeType];
		if (t == null)
			return null;

		// OK, now add it in, but don't update our attributes
		if (handlerMap == null) handlerMap = new HashMap();
		AttributeHandler h = new AttributeHandler(attribute, t);
		handlerMap.put(attribute, h);
		return h;
	}


	public AttributeHandlingType stringToType(String str) {
		for (AttributeHandlingType type: AttributeHandlingType.values()) {
			if (str.equals(type.toString()))
				return type;
		}
		return AttributeHandlingType.NONE;
	}

	/**
 	 * Update all of the attributes for a particular metanode.  This includes the info
 	 * on the number of children and the number of descendents as well as handling all of the 
 	 * attribute aggregation.
 	 *
 	 * @param mNode the metanode we're agregating over
 	 */
	public void updateAttributes(MetaNode mNode) {
		CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
		CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
		CyGroup metaGroup = mNode.getCyGroup();
		String metaNodeName = metaGroup.getGroupNode().getIdentifier();
		int nChildren = metaGroup.getNodes().size();
		int nDescendents = nChildren;
		for (CyNode node: metaGroup.getNodes()) {
			if (MetaNodeManager.getMetaNode(node) != null) {
				// Get the data from this node for aggregation
				if (nodeAttributes.hasAttribute(node.getIdentifier(), DESCENDENTS_ATTRIBUTE)) {
					int descendents = nodeAttributes.getIntegerAttribute(node.getIdentifier(), DESCENDENTS_ATTRIBUTE);
					nDescendents += descendents;
				}
			}
			// Are we aggregating?
			if (getEnable()) {
				// Yes, do it
				if (MetaNodeManager.getMetaNode(node) != null) {
					// This is a metanode -- some algorithm (e.g. median) require recursion...
					aggregateAttributes(nodeAttributes, node.getIdentifier(), 
					                    MetaNodeManager.getMetaNode(node));
				} else {
					aggregateAttributes(nodeAttributes, node.getIdentifier(), null);
				}
			}
		}
		if (getEnable()) {
			// Update all of the node attributes
			assignAttributes(nodeAttributes, metaNodeName);
			nodeAttributes.setAttribute(metaNodeName, DESCENDENTS_ATTRIBUTE, new Integer(nDescendents));
			nodeAttributes.setAttribute(metaNodeName, ENABLE_ATTRIBUTE, Boolean.TRUE);
			// System.out.println("Setting "+ENABLE_ATTRIBUTE+" for "+metaNodeName+" to "+Boolean.TRUE);
		} else {
			// System.out.println("Setting "+ENABLE_ATTRIBUTE+" for "+metaNodeName+" to "+Boolean.FALSE);
			nodeAttributes.setAttribute(metaNodeName, ENABLE_ATTRIBUTE, Boolean.FALSE);
		}

		// Update our special attributes
		nodeAttributes.setAttribute(metaNodeName, DESCENDENTS_ATTRIBUTE, new Integer(nDescendents));
		nodeAttributes.setAttribute(metaNodeName, CHILDREN_ATTRIBUTE, new Integer(nChildren));
		
	}


	/**
	 * Aggregate the data for all attributes into our map.
	 *
	 * @param attrMap the attributes over which we're aggregating
	 * @param attrType "edge" or "node"
	 * @param source the source (node or edge ID) for the attributes
	 * @param recurse a metanode if we are supposed to recurse (only done for MEDIAN)
	 */
	private void aggregateAttributes(CyAttributes attrMap, 
	                                 String source, MetaNode recurse) {
		String [] attributes = attrMap.getAttributeNames();
		for (int i = 0; i < attributes.length; i++) {
			String attr = attributes[i];
			byte type = attrMap.getType(attr);
			// Also need to add exclusions
			if (!attrMap.getUserVisible(attr) || 
			    type == CyAttributes.TYPE_COMPLEX ||
			    type == CyAttributes.TYPE_SIMPLE_MAP) {
				continue;
			}

			// Do we have a specific handler (override)?
			// AttributeHandler handler = getHandler(attrType+"."+attr);
			AttributeHandler handler = getHandler(attr);
			if (handler == null) {
				// No, create a basic handler
				handler = getDefaultHandler(attrMap.getType(attr), attr);
				aggregateAttribute(attrMap, handler, source, recurse);
			} else {
				// Aggregate
				aggregateAttribute(attrMap, handler, source, recurse);
			}
		}
	}

	/**
	 * Aggregate the data for a particular attribute into our map.
	 *
	 * @param attrMap the attributes over which we're aggregating
	 * @param handler the attribute handler we're working on
	 * @param source the source (node or edge ID) for the attributes
	 * @param recurse a metanode if we are supposed to recurse (only done for MEDIAN)
	 */
	private void aggregateAttribute(CyAttributes attrMap,
	                                AttributeHandler handler,
	                                String source,
	                                MetaNode recurse) {

		if (handler == null) 
			return;

		if (recurse == null) {
			Object value = handler.aggregateAttribute(attrMap, source, 1);
			return;
		}
		if (handler.getHandlerType() != AttributeHandlingType.MEDIAN &&
		    handler.getHandlerType() != AttributeHandlingType.MCV) {
			// Get the descendent count for this metanode
			CyNode node = recurse.getCyGroup().getGroupNode();
			int descendents = attrMap.getIntegerAttribute(node.getIdentifier(), DESCENDENTS_ATTRIBUTE);
			handler.aggregateAttribute(attrMap, source, descendents);
			return;
		}

		for (CyNode node: recurse.getCyGroup().getNodes()) {
			MetaNode mn = MetaNodeManager.getMetaNode(node);
			if (mn != null) {
				aggregateAttribute(attrMap, handler, null, mn);
			} else {
				aggregateAttribute(attrMap, handler, node.getIdentifier(), null);
			}
		}
	}

	/**
 	 * Actually assign the aggregated attributes to our meta (edge,node)
 	 *
 	 * @param attrMap the attributes map we're assigning to
	 * @param attrType "edge" or "node"
 	 * @param target the name of the object
 	 */
	private void	assignAttributes(CyAttributes attrMap,
	                               String target) {

		String [] attributes = attrMap.getAttributeNames();
		for (int i = 0; i < attributes.length; i++) {
			String attr = attributes[i];
			// Get our handler
			// AttributeHandler handler = getHandler(attrType+"."+attr);
			AttributeHandler handler = getHandler(attr);
			if (handler != null)
				handler.assignAttribute(attrMap, target);
		}
	}

}
