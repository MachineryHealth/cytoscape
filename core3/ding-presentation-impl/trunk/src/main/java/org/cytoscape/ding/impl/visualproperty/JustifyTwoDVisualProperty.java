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
package org.cytoscape.ding.impl.visualproperty;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.ding.impl.Justify;
import org.cytoscape.view.model.AbstractVisualProperty;
import org.cytoscape.view.model.DiscreteRangeImpl;
import org.cytoscape.view.model.Range;

public class JustifyTwoDVisualProperty extends AbstractVisualProperty<Justify> {

	private static final Range<Justify> JUSTIFY_RANGE;

	static {
		final Set<Justify> justifySet = new HashSet<Justify>();
		for (final Justify arrow : Justify.values())
			justifySet.add(arrow);
		JUSTIFY_RANGE = new DiscreteRangeImpl<Justify>(Justify.class, justifySet);
	}

	public JustifyTwoDVisualProperty(final Justify def, final String id,
			final String name, final Class<?> targetDataType) {
		super(def, JUSTIFY_RANGE, id, name, targetDataType);
	}

	public String toSerializableString(final Justify value) {
		return value.toString();
	}

	public Justify parseSerializableString(final String text) {
		return Justify.valueOf(text);
	}
}
