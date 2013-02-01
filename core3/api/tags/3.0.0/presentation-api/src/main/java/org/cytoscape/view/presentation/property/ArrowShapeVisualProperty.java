package org.cytoscape.view.presentation.property;

/*
 * #%L
 * Cytoscape Presentation API (presentation-api)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.AbstractVisualProperty;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.presentation.property.values.AbstractVisualPropertyValue;
import org.cytoscape.view.presentation.property.values.ArrowShape;

/**
 * Visual Property for {@link ArrowShape} values.
 * This implementation provides basic default shapes.  Rendering
 * Engines can provide others.
 * 
 * @CyAPI.Final.Class
 * @CyAPI.InModule presentation-api
 */
public final class ArrowShapeVisualProperty extends AbstractVisualProperty<ArrowShape> {
	

	/** No arrow */
	public static final ArrowShape NONE = new ArrowShapeImpl("None", "NONE");
	/** Diamond shaped arrow */
	public static final ArrowShape DIAMOND = new ArrowShapeImpl("Diamond", "DIAMOND");
	/** Triangle shaped arrow */
	public static final ArrowShape DELTA = new ArrowShapeImpl("Delta", "DELTA");
	/** Pointy triangle shaped arrow */
	public static final ArrowShape ARROW = new ArrowShapeImpl("Arrow", "ARROW");
	/** T shaped arrow */
	public static final ArrowShape T = new ArrowShapeImpl("T", "T");
	/** Circle shaped arrow */
	public static final ArrowShape CIRCLE = new ArrowShapeImpl("Circle", "CIRCLE");
	/** Top Half of a triangle shaped arrow */
	public static final ArrowShape HALF_TOP = new ArrowShapeImpl("Half Top", "HALF_TOP");
	/** Bottom Half of a triangle shaped arrow */
	public static final ArrowShape HALF_BOTTOM = new ArrowShapeImpl("Half Bottom", "HALF_BOTTOM");

	private static final DiscreteRange<ArrowShape> ARROW_SHAPE_RANGE;
	private static final Map<String, ArrowShape> DEFAULT_SHAPES;
	
	static {
		DEFAULT_SHAPES = new HashMap<String, ArrowShape>();
		DEFAULT_SHAPES.put(NONE.getSerializableString().toUpperCase(), NONE);
		DEFAULT_SHAPES.put(DIAMOND.getSerializableString().toUpperCase(), DIAMOND);
		DEFAULT_SHAPES.put(DELTA.getSerializableString().toUpperCase(), DELTA);
		DEFAULT_SHAPES.put(ARROW.getSerializableString().toUpperCase(), ARROW);
		DEFAULT_SHAPES.put(T.getSerializableString().toUpperCase(), T);
		DEFAULT_SHAPES.put(CIRCLE.getSerializableString().toUpperCase(), CIRCLE);
		DEFAULT_SHAPES.put(HALF_TOP.getSerializableString().toUpperCase(), HALF_TOP);
		DEFAULT_SHAPES.put(HALF_BOTTOM.getSerializableString().toUpperCase(), HALF_BOTTOM);
		
		ARROW_SHAPE_RANGE = new DiscreteRange<ArrowShape>(ArrowShape.class, new HashSet<ArrowShape>(DEFAULT_SHAPES.values()));
	}

	/**
	 * Constructor.
	 * @param defaultValue The default arrow shape.
	 * @param id A machine readable string identifying this visual property used for XML serialization. 
	 * @param displayName A human readable string used for displays and user interfaces. 
	 * @param modelDataType The model data type associated with this visual property, e.g. CyNode, CyEdge, or CyNetwork. 
	 */
	public ArrowShapeVisualProperty(ArrowShape defaultValue, String id, String displayName,
			Class<? extends CyIdentifiable> modelDataType) {
		super(defaultValue, ARROW_SHAPE_RANGE, id, displayName, modelDataType);
	}

	@Override
	public String toSerializableString(ArrowShape value) {
		return value.getSerializableString();
	}

	@Override
	public ArrowShape parseSerializableString(String value) {
		ArrowShape shape = null;
		
		if (value != null)
			shape = DEFAULT_SHAPES.get(value.toUpperCase());
		
		if (shape == null) {
			// Try to find the shape in the range (it might have been added by a private visual lexicon)
			for (final ArrowShape as : ARROW_SHAPE_RANGE.values()) {
				if (as.getSerializableString().equalsIgnoreCase(value)) {
					shape = as;
					break;
				}
			}
		}
		
		return shape;
	}
	
	public static boolean isDefaultShape(final ArrowShape shape) {
		return DEFAULT_SHAPES.containsValue(shape);
	}

	private static final class ArrowShapeImpl extends AbstractVisualPropertyValue implements ArrowShape {
		public ArrowShapeImpl(final String displayName, final String serializableString) {
			super(displayName, serializableString);
		}
	}
}
