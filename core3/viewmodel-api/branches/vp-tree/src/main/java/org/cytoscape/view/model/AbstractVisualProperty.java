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
package org.cytoscape.view.model;

import java.util.Collection;
import java.util.HashSet;

/**
 * An abstract implementation of VisualProperty that omits the methods dealing
 * with serializing data.
 * 
 * @since Cytoscape 3.0
 * 
 */
public abstract class AbstractVisualProperty<T> implements VisualProperty<T> {

	// Default value for this VP.
	protected final T defaultValue;

	// Identifier.
	final protected String id;

	// Human-readable name of VP.
	final protected String name;

	protected boolean isIgnoreDefault = false;

	// Accepts all kinds of data types, so data compatibility checking is
	// developer's responsibility.
	protected final Collection<VisualProperty<?>> children;
	protected VisualProperty<?> parent;

	/**
	 * Constructor with all required immutable field values.
	 * 
	 * @param objectType
	 * @param defaultValue
	 * @param id
	 * @param name
	 */
	public AbstractVisualProperty(final T defaultValue, final String id,
			final String name) {
		this.defaultValue = defaultValue;
		this.id = id;
		this.name = name;
		this.children = new HashSet<VisualProperty<?>>();
	}

	@SuppressWarnings("unchecked")
	public Class<T> getType() {
		if (defaultValue != null)
			return (Class<T>) defaultValue.getClass();
		else
			return null;
	}

	public T getDefault() {
		return defaultValue;
	}

	public String getIdString() {
		return id;
	}

	public String getDisplayName() {
		return name;
	}

	public boolean isIgnoreDefault() {
		return this.isIgnoreDefault;
	}

	public VisualProperty<?> getParent() {
		return this.parent;
	}

	public Collection<VisualProperty<?>> getChildren() {
		return this.children;
	}

	@Override
	public void setParent(VisualProperty<?> parent) {
		this.parent = parent;
	}

}
