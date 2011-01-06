package org.cytoscape.view.vizmap.gui;

import java.util.Set;

import org.cytoscape.view.model.VisualProperty;

/**
 * Defines the dependency
 * 
 */
public interface VisualPropertyDependency {
	
	/**
	 * Provide text for the GUI check box.
	 * 
	 * @return Check box name as string.
	 */
	String getDisplayName();
	
	VisualProperty<?> getParent();
	
	Set<VisualProperty<?>> getChildren();

}
