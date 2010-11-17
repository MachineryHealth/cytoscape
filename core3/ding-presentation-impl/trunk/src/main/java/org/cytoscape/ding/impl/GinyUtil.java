package org.cytoscape.ding.impl;


import static org.cytoscape.graph.render.immed.GraphGraphics.ARROW_DELTA;
import static org.cytoscape.graph.render.immed.GraphGraphics.ARROW_DIAMOND;
import static org.cytoscape.graph.render.immed.GraphGraphics.ARROW_DISC;
import static org.cytoscape.graph.render.immed.GraphGraphics.ARROW_NONE;
import static org.cytoscape.graph.render.immed.GraphGraphics.ARROW_TEE;
import static org.cytoscape.graph.render.immed.GraphGraphics.ARROW_HALF_TOP;
import static org.cytoscape.graph.render.immed.GraphGraphics.ARROW_HALF_BOTTOM;
import static org.cytoscape.graph.render.immed.GraphGraphics.ARROW_ARROWHEAD;
import static org.cytoscape.graph.render.immed.GraphGraphics.SHAPE_DIAMOND;
import static org.cytoscape.graph.render.immed.GraphGraphics.SHAPE_ELLIPSE;
import static org.cytoscape.graph.render.immed.GraphGraphics.SHAPE_HEXAGON;
import static org.cytoscape.graph.render.immed.GraphGraphics.SHAPE_OCTAGON;
import static org.cytoscape.graph.render.immed.GraphGraphics.SHAPE_PARALLELOGRAM;
import static org.cytoscape.graph.render.immed.GraphGraphics.SHAPE_RECTANGLE;
import static org.cytoscape.graph.render.immed.GraphGraphics.SHAPE_ROUNDED_RECTANGLE;
import static org.cytoscape.graph.render.immed.GraphGraphics.SHAPE_TRIANGLE;
import static org.cytoscape.graph.render.immed.GraphGraphics.SHAPE_VEE;
import static org.cytoscape.ding.EdgeView.EDGE_COLOR_CIRCLE;
import static org.cytoscape.ding.EdgeView.EDGE_COLOR_DELTA;
import static org.cytoscape.ding.EdgeView.EDGE_COLOR_ARROW;
import static org.cytoscape.ding.EdgeView.EDGE_COLOR_DIAMOND;
import static org.cytoscape.ding.EdgeView.EDGE_COLOR_T;
import static org.cytoscape.ding.EdgeView.EDGE_HALF_ARROW_TOP;
import static org.cytoscape.ding.EdgeView.EDGE_HALF_ARROW_BOTTOM;
import static org.cytoscape.ding.EdgeView.NO_END;
import static org.cytoscape.ding.NodeView.DIAMOND;
import static org.cytoscape.ding.NodeView.ELLIPSE;
import static org.cytoscape.ding.NodeView.HEXAGON;
import static org.cytoscape.ding.NodeView.OCTAGON;
import static org.cytoscape.ding.NodeView.PARALELLOGRAM;
import static org.cytoscape.ding.NodeView.RECTANGLE;
import static org.cytoscape.ding.NodeView.ROUNDED_RECTANGLE;
import static org.cytoscape.ding.NodeView.TRIANGLE;
import static org.cytoscape.ding.NodeView.VEE;


/**
 *
 * Convert bytes defined in rendering engine into Giny types.
 *
 * @version 0.7
 * @since Cytoscape 2.5
 * @author kono
 *
 */
class GinyUtil {
	static int getGinyNodeType(final byte type) {
		switch (type) {
		case SHAPE_RECTANGLE:
			return RECTANGLE;

		case SHAPE_DIAMOND:
			return DIAMOND;

		case SHAPE_ELLIPSE:
			return ELLIPSE;

		case SHAPE_HEXAGON:
			return HEXAGON;

		case SHAPE_OCTAGON:
			return OCTAGON;

		case SHAPE_PARALLELOGRAM:
			return PARALELLOGRAM;

		case SHAPE_ROUNDED_RECTANGLE:
			return ROUNDED_RECTANGLE;

		case SHAPE_TRIANGLE:
			return TRIANGLE;

		case SHAPE_VEE:
			return VEE;

		default:
			return TRIANGLE;
		}
	}

	static byte getNativeNodeType(final int ginyType) {
		switch (ginyType) {
		case RECTANGLE:
			return SHAPE_RECTANGLE;

		case DIAMOND:
			return SHAPE_DIAMOND;

		case ELLIPSE:
			return SHAPE_ELLIPSE;

		case HEXAGON:
			return SHAPE_HEXAGON;

		case OCTAGON:
			return SHAPE_OCTAGON;

		case PARALELLOGRAM:
			return SHAPE_PARALLELOGRAM;

		case ROUNDED_RECTANGLE:
			return SHAPE_ROUNDED_RECTANGLE;

		case TRIANGLE:
			return SHAPE_TRIANGLE;

		case VEE:
			return SHAPE_VEE;

		default:
			return -1;
		}
	}

	static int getGinyArrowType(final byte type) {
		switch (type) {
		case ARROW_NONE:
			return NO_END;

		case ARROW_DELTA:
			return EDGE_COLOR_DELTA;

		case ARROW_DIAMOND:
			return EDGE_COLOR_DIAMOND;

		case ARROW_DISC:
			return EDGE_COLOR_CIRCLE;

		case ARROW_TEE:
			return EDGE_COLOR_T;

		case ARROW_HALF_TOP:
			return EDGE_HALF_ARROW_TOP;

		case ARROW_HALF_BOTTOM:
			return EDGE_HALF_ARROW_BOTTOM;

		case ARROW_ARROWHEAD:
			return EDGE_COLOR_ARROW;

		default:
			return NO_END;
		}
	}

	static byte getNativeArrowType(final int ginyType) {
		switch (ginyType) {
		case NO_END:
			return ARROW_NONE;

		case EDGE_COLOR_ARROW:
			return ARROW_ARROWHEAD;

		case EDGE_COLOR_DELTA:
			return ARROW_DELTA;

		case EDGE_COLOR_DIAMOND:
			return ARROW_DIAMOND;

		case EDGE_COLOR_CIRCLE:
			return ARROW_DISC;

		case EDGE_COLOR_T:
			return ARROW_TEE;

		case EDGE_HALF_ARROW_TOP:
			return ARROW_HALF_TOP;

		case EDGE_HALF_ARROW_BOTTOM:
			return ARROW_HALF_BOTTOM;

		default:
			return ARROW_NONE;
		}
	}
}
