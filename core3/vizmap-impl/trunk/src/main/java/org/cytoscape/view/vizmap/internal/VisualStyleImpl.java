/*
 Copyright (c) 2008, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.view.vizmap.internal;

import static org.cytoscape.model.CyTableEntry.EDGE;
import static org.cytoscape.model.CyTableEntry.NETWORK;
import static org.cytoscape.model.CyTableEntry.NODE;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.RootVisualLexicon;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 */
public class VisualStyleImpl implements VisualStyle {
	
	private static final Logger logger = LoggerFactory.getLogger(VisualStyleImpl.class);
	
	private static final String DEFAULT_TITLE = "?";
	
	private final Map<VisualProperty<?>, VisualMappingFunction<?, ?>> mappings;
	private final Map<VisualProperty<?>, Object> perVSDefaults;
	private final RootVisualLexicon rootLexicon;
	
	private String title;

	/**
	 * Creates a new VisualStyleImpl object.
	 *
	 * @param rootLexicon  DOCUMENT ME!
	 */
	public VisualStyleImpl(final RootVisualLexicon rootLexicon) {
		this(rootLexicon, null);
	}

	/**
	 * Creates a new VisualStyleImpl object.
	 *
	 * @param eventHelper  DOCUMENT ME!
	 * @param rootLexicon  DOCUMENT ME!
	 */
	public VisualStyleImpl(final RootVisualLexicon rootLexicon, final String title) {
		if (rootLexicon == null)
			throw new NullPointerException("rootLexicon is null");

		if (title == null)
			this.title = DEFAULT_TITLE;
		else
			this.title = title;

		this.rootLexicon = rootLexicon;
		mappings = new HashMap<VisualProperty<?>, VisualMappingFunction<?, ?>>();
		perVSDefaults = new HashMap<VisualProperty<?>, Object>();
		
		// Copy immutable defaults from each VP
		for(VisualProperty<?> vp: this.rootLexicon.getAllVisualProperties())
			perVSDefaults.put(vp, vp.getDefault());
		
		logger.info("New Visual Style Created: Style Name = " + this.title);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param c DOCUMENT ME!
	 */
	public void addVisualMappingFunction(final VisualMappingFunction<?, ?> mapping) {
		mappings.put(mapping.getVisualProperty(), mapping);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param <V> DOCUMENT ME!
	 * @param t DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	@SuppressWarnings("unchecked")
	public <V> VisualMappingFunction<?, V> getVisualMappingFunction(VisualProperty<V> t) {
		return (VisualMappingFunction<?, V>) mappings.get(t);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param <V> DOCUMENT ME!
	 * @param t DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	@SuppressWarnings("unchecked")
	public <V> VisualMappingFunction<?, V> removeVisualMappingFunction(VisualProperty<V> t) {
		return (VisualMappingFunction<?, V>) mappings.remove(t);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param <T> DOCUMENT ME!
	 * @param vp DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	@SuppressWarnings("unchecked")
	public <V> V getDefaultValue(final VisualProperty<? extends V> vp) {
		// Since setter checks type, this cast is always legal.
		return (V) perVSDefaults.get(vp);
	}

	/**
	 *  Set the default value for a Visual Property
	 *
	 * @param <T> Default value data type.
	 * @param vp DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public <T> void setDefaultValue(final VisualProperty<? extends T> vp, final T value) {
		perVSDefaults.put(vp, value);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param networkView DOCUMENT ME!
	 */
	public void apply(final CyNetworkView networkView) {
		
		logger.debug("Visual Style Apply method called: " + this.title);
		
		final Collection<View<CyNode>> nodeviews = networkView.getNodeViews();
		final Collection<View<CyEdge>> edgeviews = networkView.getEdgeViews();
		final Collection<View<CyNetwork>> networkviews = new HashSet<View<CyNetwork>>();
		networkviews.add(networkView);

		applyImpl(networkView, nodeviews,
		          rootLexicon.getVisualProperties(nodeviews, NODE));
		applyImpl(networkView, edgeviews,
		          rootLexicon.getVisualProperties(edgeviews, EDGE));
		applyImpl(networkView, networkviews,
		          rootLexicon.getVisualProperties(NETWORK));
		
		logger.debug("Visual Style applied: " + this.title + "\n");
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param <T> DOCUMENT ME!
	 * @param views DOCUMENT ME!
	 * @param visualProperties DOCUMENT ME!
	 */
	public <G extends CyTableEntry> void applyImpl(final CyNetworkView view,
	                                              final Collection<View<G>> views,
	                                              final Collection<?extends VisualProperty<?>> visualProperties) {
		
		for (VisualProperty<?> vp : visualProperties)
			applyImpl(view, views, vp);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param <T> DOCUMENT ME!
	 * @param views DOCUMENT ME!
	 * @param visualProperties DOCUMENT ME!
	 */
	public <V, G extends CyTableEntry> void applyImpl(final CyNetworkView view,
	                                                 final Collection<View<G>> views,
	                                                 final VisualProperty<V> vp) {
		
		final VisualMappingFunction<?, V> mapping = getVisualMappingFunction(vp);
		final V defaultValue = getDefaultValue(vp);
		
		// If mapping is available for this VP, apply the mapping.
		if (mapping != null) {
			mapping.apply(views);
		} else if(!vp.isIgnoreDefault()) { // Check ignore flag first.
			// reset all rows to allow usage of default value:
			for(final View<G> viewModel: views) {
				if(viewModel.getVisualProperty(vp).equals(defaultValue))
					continue;
				
				viewModel.setVisualProperty(vp, defaultValue);
				//logger.debug(vp.getDisplayName() + " updated: " + defaultValue);
			}
		} else
			logger.debug(vp.getDisplayName() + " is set to ignore defaults.  Skipping...");
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getTitle() {
		return title;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param title DOCUMENT ME!
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 *  toString method returns title of this Visual Style.
	 *
	 * @return  DOCUMENT ME!
	 */
	@Override
	public String toString() {
		return this.title;
	}

	public Collection<VisualMappingFunction<?,?>> getAllVisualMappingFunctions() {
		return mappings.values();
	}

	//TODO Is this the right set of lexicon?
	public VisualLexicon getVisualLexicon() {
		return rootLexicon;
	}

}
