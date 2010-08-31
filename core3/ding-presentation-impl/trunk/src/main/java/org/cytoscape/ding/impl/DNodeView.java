/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.ding.impl;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.GraphViewChangeListener;
import org.cytoscape.ding.Label;
import org.cytoscape.ding.NodeShape;
import org.cytoscape.ding.NodeView;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.CustomGraphic;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.TwoDVisualLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ding implementation of node presentation.
 * 
 * @author $author$
 */
public class DNodeView implements NodeView, Label {
	
	static final float DEFAULT_WIDTH = 20.0f;
	static final float DEFAULT_HEIGHT = 20.0f;
	static final int DEFAULT_SHAPE = GraphGraphics.SHAPE_ELLIPSE;
	static final Paint DEFAULT_BORDER_PAINT = Color.black;
	static final String DEFAULT_LABEL_TEXT = "";
	static final Font DEFAULT_LABEL_FONT = new Font(null, Font.PLAIN, 1);
	static final Paint DEFAULT_LABEL_PAINT = Color.black;
	static final float DEFAULT_TRANSPARENCY = 255;
	
	final int m_inx; // The FixedGraph index (non-negative).
	boolean m_selected;
	Paint m_unselectedPaint;
	Paint m_selectedPaint;
	Paint m_borderPaint;

	float transparency;

	/**
	 * Stores the position of a nodeView when it's hidden so that when the
	 * nodeView is retored we can restore the view into the same position.
	 */
	float m_hiddenXMin;
	float m_hiddenYMin;
	float m_hiddenXMax;
	float m_hiddenYMax;

	ArrayList<Shape> m_graphicShapes;
	ArrayList<Paint> m_graphicPaints;

	// AJK: 04/26/06 for tooltip
	String m_toolTipText = null;

	// A LinkedHashSet of the custom graphics associated with this
	// DNodeView. We need the HashSet linked since the ordering of
	// custom graphics is important. For space considerations, we
	// keep _customGraphics null when there are no custom
	// graphics--event though this is a bit more complicated:
	private LinkedHashSet<CustomGraphic> _customGraphics;
	// CG_LOCK is used for synchronizing custom graphics operations on this
	// DNodeView.
	// Arrays are objects like any other and can be used for synchronization. We
	// use an array
	// object assuming it takes up the least amount of memory:
	private final Object[] CG_LOCK = new Object[0];
	private final static HashSet<CustomGraphic> EMPTY_CUSTOM_GRAPHICS = new LinkedHashSet<CustomGraphic>(
			0);

	// Parent network.
	DGraphView dGraphView;
	
	// View Model for this presentation.
	private final View<CyNode> nodeViewModel;

	/*
	 * @param inx the RootGraph index of node (a negative number).
	 */
	DNodeView(DGraphView view, int inx, View<CyNode> nv) {
		dGraphView = view;
		m_inx = inx;
		nodeViewModel = nv;
		m_selected = false;
		m_unselectedPaint = dGraphView.m_nodeDetails.fillPaint(m_inx);
		m_selectedPaint = Color.yellow;
		m_borderPaint = dGraphView.m_nodeDetails.borderPaint(m_inx);
		m_graphicShapes = null;
		m_graphicPaints = null;
		transparency = DEFAULT_TRANSPARENCY;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public GraphView getGraphView() {
		return dGraphView;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public CyNode getNode() {
		synchronized (dGraphView.m_lock) {
			return dGraphView.networkModel.getNode(m_inx);
		}
	}

	public View<CyNode> getNodeViewModel() {
		return nodeViewModel;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getGraphPerspectiveIndex() {
		return m_inx;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getRootGraphIndex() {
		return m_inx;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param otherNodeView
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public List<EdgeView> getEdgeViewsList(NodeView otherNodeView) {
		synchronized (dGraphView.m_lock) {
			return dGraphView.getEdgeViewsList(getNode(), otherNodeView.getNode());
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getShape() {
		synchronized (dGraphView.m_lock) {
			final int nativeShape = dGraphView.m_nodeDetails.shape(m_inx);

			return nativeShape;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param paint
	 *            DOCUMENT ME!
	 */
	public void setSelectedPaint(Paint paint) {
		synchronized (dGraphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			m_selectedPaint = paint;

			if (isSelected()) {
				dGraphView.m_nodeDetails.overrideFillPaint(m_inx, m_selectedPaint);

				if (m_selectedPaint instanceof Color)
					dGraphView.m_nodeDetails.overrideColorLowDetail(m_inx,
							(Color) m_selectedPaint);

				dGraphView.m_contentChanged = true;
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Paint getSelectedPaint() {
		return m_selectedPaint;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param paint
	 *            DOCUMENT ME!
	 */
	public void setUnselectedPaint(Paint paint) {
		synchronized (dGraphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			m_unselectedPaint = paint;

			if (!isSelected()) {
				dGraphView.m_nodeDetails
						.overrideFillPaint(m_inx, m_unselectedPaint);

				if (m_unselectedPaint instanceof Color)
					dGraphView.m_nodeDetails.overrideColorLowDetail(m_inx,
							(Color) m_unselectedPaint);

				dGraphView.m_contentChanged = true;
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Paint getUnselectedPaint() {
		return m_unselectedPaint;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param paint
	 *            DOCUMENT ME!
	 */
	public void setBorderPaint(Paint paint) {
		synchronized (dGraphView.m_lock) {
			m_borderPaint = paint;
			fixBorder();
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Paint getBorderPaint() {
		return m_borderPaint;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param width
	 *            DOCUMENT ME!
	 */
	public void setBorderWidth(float width) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_nodeDetails.overrideBorderWidth(m_inx, width);
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public float getBorderWidth() {
		synchronized (dGraphView.m_lock) {
			return dGraphView.m_nodeDetails.borderWidth(m_inx);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param stroke
	 *            DOCUMENT ME!
	 */
	public void setBorder(Stroke stroke) {
		if (stroke instanceof BasicStroke) {
			synchronized (dGraphView.m_lock) {
				setBorderWidth(((BasicStroke) stroke).getLineWidth());

				final float[] dashArray = ((BasicStroke) stroke).getDashArray();

				if ((dashArray != null) && (dashArray.length > 1)) {
					m_borderDash = dashArray[0];
					m_borderDash2 = dashArray[1];
				} else {
					m_borderDash = 0.0f;
					m_borderDash2 = 0.0f;
				}

				fixBorder();
			}
		}
	}

	private float m_borderDash = 0.0f;
	private float m_borderDash2 = 0.0f;
	private final static Color s_transparent = new Color(0, 0, 0, 0);

	// Callers of this method must be holding m_view.m_lock.
	private void fixBorder() {
		if ((m_borderDash == 0.0f) && (m_borderDash2 == 0.0f))
			dGraphView.m_nodeDetails.overrideBorderPaint(m_inx, m_borderPaint);
		else {
			final int size = (int) Math.max(1.0f,
					(int) (m_borderDash + m_borderDash2)); // Average times two.

			if ((size == dGraphView.m_lastSize)
					&& (m_borderPaint == dGraphView.m_lastPaint)) {
				/* Use the cached texture paint. */} else {
				final BufferedImage img = new BufferedImage(size, size,
						BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g2 = (Graphics2D) img.getGraphics();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
				g2.setPaint(s_transparent);
				g2.fillRect(0, 0, size, size);
				g2.setPaint(m_borderPaint);
				g2.fillRect(0, 0, size / 2, size / 2);
				g2.fillRect(size / 2, size / 2, size / 2, size / 2);
				dGraphView.m_lastTexturePaint = new TexturePaint(img,
						new Rectangle2D.Double(0, 0, size, size));
				dGraphView.m_lastSize = size;
				dGraphView.m_lastPaint = m_borderPaint;
			}

			dGraphView.m_nodeDetails.overrideBorderPaint(m_inx,
					dGraphView.m_lastTexturePaint);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Stroke getBorder() {
		synchronized (dGraphView.m_lock) {
			if ((m_borderDash == 0.0f) && (m_borderDash2 == 0.0f))
				return new BasicStroke(getBorderWidth());
			else

				return new BasicStroke(getBorderWidth(),
						BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
						new float[] { m_borderDash, m_borderDash2 }, 0.0f);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param trans
	 *            DOCUMENT ME!
	 */
	public void setTransparency(float trans) {
		// TODO: implement this
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public float getTransparency() {
		return 1.0f;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param width
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean setWidth(double width) {
		synchronized (dGraphView.m_lock) {
			if (!dGraphView.m_spacial.exists(m_inx, dGraphView.m_extentsBuff, 0))
				return false;

			final double xCenter = (((double) dGraphView.m_extentsBuff[0]) + dGraphView.m_extentsBuff[2]) / 2.0d;
			final double wDiv2 = width / 2.0d;
			final float xMin = (float) (xCenter - wDiv2);
			final float xMax = (float) (xCenter + wDiv2);

			if (!(xMax > xMin))
				throw new IllegalArgumentException("width is too small");

			dGraphView.m_spacial.delete(m_inx);
			dGraphView.m_spacial.insert(m_inx, xMin, dGraphView.m_extentsBuff[1], xMax,
					dGraphView.m_extentsBuff[3]);

			final double w = ((double) xMax) - xMin;
			final double h = ((double) dGraphView.m_extentsBuff[3])
					- dGraphView.m_extentsBuff[1];

			if (!(Math.max(w, h) < (1.99d * Math.min(w, h)))
					&& (getShape() == GraphGraphics.SHAPE_ROUNDED_RECTANGLE))
				setShape(GraphGraphics.SHAPE_RECTANGLE);

			dGraphView.m_contentChanged = true;

			return true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public double getWidth() {
		synchronized (dGraphView.m_lock) {
			if (!dGraphView.m_spacial.exists(m_inx, dGraphView.m_extentsBuff, 0))
				return -1.0d;

			return ((double) dGraphView.m_extentsBuff[2]) - dGraphView.m_extentsBuff[0];
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param height
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean setHeight(double height) {
		synchronized (dGraphView.m_lock) {
			if (!dGraphView.m_spacial.exists(m_inx, dGraphView.m_extentsBuff, 0))
				return false;

			final double yCenter = (((double) dGraphView.m_extentsBuff[1]) + dGraphView.m_extentsBuff[3]) / 2.0d;
			final double hDiv2 = height / 2.0d;
			final float yMin = (float) (yCenter - hDiv2);
			final float yMax = (float) (yCenter + hDiv2);

			if (!(yMax > yMin))
				throw new IllegalArgumentException("height is too small max:"
						+ yMax + " min:" + yMin + " center:" + yCenter
						+ " height:" + height);

			dGraphView.m_spacial.delete(m_inx);
			dGraphView.m_spacial.insert(m_inx, dGraphView.m_extentsBuff[0], yMin,
					dGraphView.m_extentsBuff[2], yMax);

			final double w = ((double) dGraphView.m_extentsBuff[2])
					- dGraphView.m_extentsBuff[0];
			final double h = ((double) yMax) - yMin;

			if (!(Math.max(w, h) < (1.99d * Math.min(w, h)))
					&& (getShape() == GraphGraphics.SHAPE_ROUNDED_RECTANGLE))
				setShape(GraphGraphics.SHAPE_RECTANGLE);

			dGraphView.m_contentChanged = true;

			return true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public double getHeight() {
		synchronized (dGraphView.m_lock) {
			if (!dGraphView.m_spacial.exists(m_inx, dGraphView.m_extentsBuff, 0))
				return -1.0d;

			return ((double) dGraphView.m_extentsBuff[3]) - dGraphView.m_extentsBuff[1];
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Label getLabel() {
		return this;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getDegree() {
		// This method is totally ridiculous.
		return dGraphView.getNetwork()
				.getAdjacentEdgeList(getNode(), CyEdge.Type.ANY).size();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param x
	 *            DOCUMENT ME!
	 * @param y
	 *            DOCUMENT ME!
	 */
	public void setOffset(double x, double y) {
		synchronized (dGraphView.m_lock) {
			if (!dGraphView.m_spacial.exists(m_inx, dGraphView.m_extentsBuff, 0))
				return;

			final double wDiv2 = (((double) dGraphView.m_extentsBuff[2]) - dGraphView.m_extentsBuff[0]) / 2.0d;
			final double hDiv2 = (((double) dGraphView.m_extentsBuff[3]) - dGraphView.m_extentsBuff[1]) / 2.0d;
			final float xMin = (float) (x - wDiv2);
			final float xMax = (float) (x + wDiv2);
			final float yMin = (float) (y - hDiv2);
			final float yMax = (float) (y + hDiv2);

			if (!(xMax > xMin))
				throw new IllegalStateException(
						"width of node has degenerated to zero after "
								+ "rounding");

			if (!(yMax > yMin))
				throw new IllegalStateException(
						"height of node has degenerated to zero after "
								+ "rounding");

			dGraphView.m_spacial.delete(m_inx);
			dGraphView.m_spacial.insert(m_inx, xMin, yMin, xMax, yMax);
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Point2D getOffset() {
		synchronized (dGraphView.m_lock) {
			if (!dGraphView.m_spacial.exists(m_inx, dGraphView.m_extentsBuff, 0))
				return null;

			final double xCenter = (((double) dGraphView.m_extentsBuff[0]) + dGraphView.m_extentsBuff[2]) / 2.0d;
			final double yCenter = (((double) dGraphView.m_extentsBuff[1]) + dGraphView.m_extentsBuff[3]) / 2.0d;

			return new Point2D.Double(xCenter, yCenter);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param xPos
	 *            DOCUMENT ME!
	 */
	public void setXPosition(double xPos) {
		
		synchronized (dGraphView.m_lock) {
			if (!dGraphView.m_spacial.exists(m_inx, dGraphView.m_extentsBuff, 0)
					|| Double.isNaN(xPos))
				return;

			final double wDiv2 = (((double) dGraphView.m_extentsBuff[2]) - dGraphView.m_extentsBuff[0]) / 2.0d;
			final float xMin = (float) (xPos - wDiv2);
			final float xMax = (float) (xPos + wDiv2);

			if (!(xMax > xMin))
				throw new IllegalStateException(
						"width of node has degenerated to zero after "
								+ "rounding");

			dGraphView.m_spacial.delete(m_inx);
			dGraphView.m_spacial.insert(m_inx, xMin, dGraphView.m_extentsBuff[1], xMax,
					dGraphView.m_extentsBuff[3]);
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param xPos
	 *            DOCUMENT ME!
	 * @param update
	 *            DOCUMENT ME!
	 */
	public void setXPosition(double xPos, boolean update) {
		setXPosition(xPos);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public double getXPosition() {
		synchronized (dGraphView.m_lock) {
			if (!dGraphView.m_spacial.exists(m_inx, dGraphView.m_extentsBuff, 0))
				return Double.NaN;

			return (((double) dGraphView.m_extentsBuff[0]) + dGraphView.m_extentsBuff[2]) / 2.0d;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param yPos
	 *            DOCUMENT ME!
	 */
	public void setYPosition(double yPos) {
		synchronized (dGraphView.m_lock) {
			if (!dGraphView.m_spacial.exists(m_inx, dGraphView.m_extentsBuff, 0)
					|| Double.isNaN(yPos))
				return;

			final double hDiv2 = (((double) dGraphView.m_extentsBuff[3]) - dGraphView.m_extentsBuff[1]) / 2.0d;
			final float yMin = (float) (yPos - hDiv2);
			final float yMax = (float) (yPos + hDiv2);

			if (!(yMax > yMin))
				throw new IllegalStateException(
						"height of node has degenerated to zero after "
								+ "rounding");

			dGraphView.m_spacial.delete(m_inx);
			dGraphView.m_spacial.insert(m_inx, dGraphView.m_extentsBuff[0], yMin,
					dGraphView.m_extentsBuff[2], yMax);
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param yPos
	 *            DOCUMENT ME!
	 * @param update
	 *            DOCUMENT ME!
	 */
	public void setYPosition(double yPos, boolean update) {
		setYPosition(yPos);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public double getYPosition() {
		synchronized (dGraphView.m_lock) {
			if (!dGraphView.m_spacial.exists(m_inx, dGraphView.m_extentsBuff, 0))
				return Double.NaN;

			return (((double) dGraphView.m_extentsBuff[1]) + dGraphView.m_extentsBuff[3]) / 2.0d;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param animate
	 *            DOCUMENT ME!
	 */
	public void setNodePosition(boolean animate) {
	}

	/**
	 * DOCUMENT ME!
	 */
	public void select() {
		final boolean somethingChanged;

		synchronized (dGraphView.m_lock) {
			somethingChanged = selectInternal();

			if (somethingChanged)
				dGraphView.m_contentChanged = true;
		}

		if (somethingChanged) {
			final GraphViewChangeListener listener = dGraphView.m_lis[0];

			if (listener != null)
				listener.graphViewChanged(new GraphViewNodesSelectedEvent(
						dGraphView, DGraphView.makeList(getNode())));
		}
	}

	// Should synchronize around m_view.m_lock.
	boolean selectInternal() {
		if (m_selected)
			return false;

		m_selected = true;
		dGraphView.m_nodeDetails.overrideFillPaint(m_inx, m_selectedPaint);

		if (m_selectedPaint instanceof Color)
			dGraphView.m_nodeDetails.overrideColorLowDetail(m_inx,
					(Color) m_selectedPaint);

		dGraphView.m_selectedNodes.insert(m_inx);

		return true;
	}

	/**
	 * DOCUMENT ME!
	 */
	public void unselect() {
		final boolean somethingChanged;

		synchronized (dGraphView.m_lock) {
			somethingChanged = unselectInternal();

			if (somethingChanged)
				dGraphView.m_contentChanged = true;
		}

		if (somethingChanged) {
			final GraphViewChangeListener listener = dGraphView.m_lis[0];

			if (listener != null)
				listener.graphViewChanged(new GraphViewNodesUnselectedEvent(
						dGraphView, DGraphView.makeList(getNode())));
		}
	}

	// Should synchronize around m_view.m_lock.
	boolean unselectInternal() {
		if (!m_selected)
			return false;

		m_selected = false;
		dGraphView.m_nodeDetails.overrideFillPaint(m_inx, m_unselectedPaint);

		if (m_unselectedPaint instanceof Color)
			dGraphView.m_nodeDetails.overrideColorLowDetail(m_inx,
					(Color) m_unselectedPaint);

		dGraphView.m_selectedNodes.delete(m_inx);

		return true;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isSelected() {
		return m_selected;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param selected
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean setSelected(boolean selected) {
		if (selected)
			select();
		else
			unselect();

		return true;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param shape
	 *            DOCUMENT ME!
	 */
	public void setShape(final int inshape) {
		synchronized (dGraphView.m_lock) {

			int shape = inshape;

			// special case
			if (shape == GraphGraphics.SHAPE_ROUNDED_RECTANGLE) {
				final double width = getWidth();
				final double height = getHeight();

				if (!(Math.max(width, height) < (1.99d * Math
						.min(width, height))))
					shape = GraphGraphics.SHAPE_RECTANGLE;
				else
					shape = GraphGraphics.SHAPE_ROUNDED_RECTANGLE;
			}

			dGraphView.m_nodeDetails.overrideShape(m_inx, shape);
			dGraphView.m_contentChanged = true;
		}
	}

	// AJK: 04/26/06 BEGIN
	/**
	 * DOCUMENT ME!
	 * 
	 * @param tip
	 *            DOCUMENT ME!
	 */
	public void setToolTip(String tip) {
		m_toolTipText = tip;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getToolTip() {
		return m_toolTipText;
	}

	// AJK: 04/26/06 END
	/**
	 * DOCUMENT ME!
	 * 
	 * @param position
	 *            DOCUMENT ME!
	 */
	public void setPositionHint(int position) {
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Paint getTextPaint() {
		synchronized (dGraphView.m_lock) {
			return dGraphView.m_nodeDetails.labelPaint(m_inx, 0);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param textPaint
	 *            DOCUMENT ME!
	 */
	public void setTextPaint(Paint textPaint) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_nodeDetails.overrideLabelPaint(m_inx, 0, textPaint);
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public double getGreekThreshold() {
		return 0.0d;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param threshold
	 *            DOCUMENT ME!
	 */
	public void setGreekThreshold(double threshold) {
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getText() {
		synchronized (dGraphView.m_lock) {
			return dGraphView.m_nodeDetails.labelText(m_inx, 0);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param text
	 *            DOCUMENT ME!
	 */
	public void setText(String text) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_nodeDetails.overrideLabelText(m_inx, 0, text);

			if (DEFAULT_LABEL_TEXT.equals(dGraphView.m_nodeDetails.labelText(m_inx,
					0)))
				dGraphView.m_nodeDetails.overrideLabelCount(m_inx, 0);
			else
				dGraphView.m_nodeDetails.overrideLabelCount(m_inx, 1);

			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Font getFont() {
		synchronized (dGraphView.m_lock) {
			return dGraphView.m_nodeDetails.labelFont(m_inx, 0);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param font
	 *            DOCUMENT ME!
	 */
	public void setFont(Font font) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_nodeDetails.overrideLabelFont(m_inx, 0, font);
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * Adds a custom graphic, <EM>in draw order</EM>, to this DNodeView in a
	 * thread-safe way. This is a convenience method that is equivalent to
	 * calling: <CODE>
	 *   addCustomGraphic (new CustomGraphic (shape,paint,anchor))
	 * </CODE> except the the new CustomGraphic created is returned.
	 * 
	 * @param shape
	 * @param paint
	 * @param anchor
	 *            The int value from NodeDetails, that defines where the graphic
	 *            anchor point lies on this DNodeView's extents rectangle. A
	 *            common anchor is NodeDetails.ANCHOR_CENTER.
	 * @since Cytoscape 2.6
	 * @throws IllegalArgumentException
	 *             if shape or paint are null or anchor is not in the range 0 <=
	 *             anchor <= NodeDetails.MAX_ANCHOR_VAL.
	 * @return The CustomGraphic added to this DNodeView.
	 * @see #addCustomGraphic(CustomGraphic)
	 * @see org.cytoscape.graph.render.stateful.CustomGraphic
	 */
	public CustomGraphic addCustomGraphic(Shape shape, Paint paint, int anchor) {
		CustomGraphic cg = new CustomGraphic(shape, paint, anchor);
		addCustomGraphic(cg);
		return cg;
	}

	/**
	 * Adds a given CustomGraphic, <EM>in draw order</EM>, to this DNodeView in
	 * a thread-safe way. Each CustomGraphic will be drawn in the order is was
	 * added. So, if you care about draw order (as for overlapping graphics),
	 * make sure you add them in the order you desire. Note that since
	 * CustomGraphics may be added by multiple plugins, your additions may be
	 * interleaved with others.
	 * 
	 * <P>
	 * A CustomGraphic can only be associated with a DNodeView once. If you wish
	 * to have a custom graphic, with the same paint and shape information,
	 * occur in multiple places in the draw order, simply create a new
	 * CustomGraphic and add it.
	 * 
	 * @since Cytoscape 2.6
	 * @throws IllegalArgumentException
	 *             if shape or paint are null.
	 * @return true if the CustomGraphic was added to this DNodeView. false if
	 *         this DNodeView already contained this CustomGraphic.
	 * @see org.cytoscape.graph.render.stateful.CustomGraphic
	 */
	public boolean addCustomGraphic(CustomGraphic cg) {
		boolean retVal = false;
		// CG_RW_LOCK.writeLock().lock();
		// if (_customGraphics == null) {
		// _customGraphics = new LinkedHashSet<CustomGraphic>();
		// }
		// retVal = _customGraphics.add (cg);
		// CG_RW_LOCK.writeLock().unlock();
		synchronized (CG_LOCK) {
			if (_customGraphics == null) {
				_customGraphics = new LinkedHashSet<CustomGraphic>();
			}
			retVal = _customGraphics.add(cg);
		}
		ensureContentChanged();
		return retVal;
	}

	/**
	 * A thread-safe way to determine if this DNodeView contains a given custom
	 * graphic.
	 * 
	 * @param cg
	 *            the CustomGraphic for which we are checking containment.
	 * @since Cytoscape 2.6
	 */
	public boolean containsCustomGraphic(CustomGraphic cg) {
		// CG_RW_LOCK.readLock().lock();
		// boolean retVal = false;
		// if (_customGraphics != null) {
		// retVal = _customGraphics.contains (cg);
		// }
		// CG_RW_LOCK.readLock().unlock();
		// return retVal;
		synchronized (CG_LOCK) {
			if (_customGraphics == null) {
				return false;
			}
			return _customGraphics.contains(cg);
		}
	}

	/**
	 * Return a non-null, read-only Iterator over all CustomGraphics contained
	 * in this DNodeView. The Iterator will return each CustomGraphic in draw
	 * order. The Iterator cannot be used to modify the underlying set of
	 * CustomGraphics.
	 * 
	 * @return The CustomGraphics Iterator. If no CustomGraphics are associated
	 *         with this DNOdeView, an empty Iterator is returned.
	 * @throws UnsupportedOperationException
	 *             if an attempt is made to use the Iterator's remove() method.
	 * @since Cytoscape 2.6
	 */
	public Iterator<CustomGraphic> customGraphicIterator() {
		Iterator<CustomGraphic> retVal = null;
		final Iterable<CustomGraphic> toIterate;
		// CG_RW_LOCK.readLock().lock();
		// if (_customGraphics == null) {
		// toIterate = EMPTY_CUSTOM_GRAPHICS;
		// } else {
		// toIterate = _customGraphics;
		// }
		// retVal = new LockingIterator<CustomGraphic>(toIterate);
		// retVal = new Iterator<CustomGraphic>() {
		// Iterator<? extends CustomGraphic> i = toIterate.iterator();
		// public boolean hasNext() {return i.hasNext();}
		// public CustomGraphic next() {return i.next();}
		// public void remove() {
		// throw new UnsupportedOperationException();
		// }
		// };
		// CG_RW_LOCK.readLock().unlock();
		// return retVal;
		synchronized (CG_LOCK) {
			if (_customGraphics == null) {
				toIterate = EMPTY_CUSTOM_GRAPHICS;
			} else {
				toIterate = _customGraphics;
			}
			return new ReadOnlyIterator<CustomGraphic>(toIterate);
		}
	}

	/**
	 * A thread-safe method for removing a given custom graphic from this
	 * DNodeView.
	 * 
	 * @return true if the custom graphic was found an removed. Returns false if
	 *         cg is null or is not a custom graphic associated with this
	 *         DNodeView.
	 * @since Cytoscape 2.6
	 */
	public boolean removeCustomGraphic(CustomGraphic cg) {
		boolean retVal = false;
		// CG_RW_LOCK.writeLock().lock();
		// if (_customGraphics != null) {
		// retVal = _customGraphics.remove (cg);
		// }
		// CG_RW_LOCK.writeLock().unlock();
		synchronized (CG_LOCK) {
			if (_customGraphics != null) {
				retVal = _customGraphics.remove(cg);
			}
		}
		ensureContentChanged();
		return retVal;
	}

	/**
	 * A thread-safe method returning the number of custom graphics associated
	 * with this DNodeView. If none are associated, zero is returned.
	 * 
	 * @since Cytoscape 2.6
	 */
	public int getNumCustomGraphics() {
		// CG_RW_LOCK.readLock().lock();
		// int retVal = 0;
		// if (_customGraphics != null) {
		// retVal = _customGraphics.size();
		// }
		// CG_RW_LOCK.readLock().unlock();
		// return retVal;
		synchronized (CG_LOCK) {
			if (_customGraphics == null) {
				return 0;
			}
			return _customGraphics.size();
		}
	}

	private void ensureContentChanged() {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * Obtain the lock used for reading information about custom graphics. This
	 * is <EM>not</EM> needed for thread-safe custom graphic operations, but
	 * only needed for use with thread-compatible methods, such as
	 * customGraphicIterator(). For example, to iterate over all custom graphics
	 * without fear of the underlying custom graphics being mutated, you could
	 * perform:
	 * 
	 * <PRE>
	 *    DNodeView dnv = ...;
	 *    CustomGraphic cg = null;
	 *    synchronized (dnv.customGraphicLock()) {
	 *       Iterator<CustomGraphic> cgIt = dnv.customGraphicIterator();
	 *       while (cgIt.hasNext()) {
	 *          cg = cgIt.next();
	 *          // PERFORM your operations here.
	 *       }
	 *   }
	 * </PRE>
	 * 
	 * NOTE: A better concurrency approach would be to return the read lock from
	 * a java.util.concurrent.locks.ReentrantReadWriteLock. However, this
	 * requires users to manually lock and unlock blocks of code where many
	 * times try{} finally{} blocks are needed and if any mistake are made, a
	 * DNodeView may be permanently locked. Since concurrency will most likely
	 * be very low, we opt for the simpler approach of having users use
	 * synchronized {} blocks on a standard lock object.
	 * 
	 * @return the lock object used for custom graphics of this DNodeView.
	 */
	public Object customGraphicLock() {
		return CG_LOCK;
	}

	private class ReadOnlyIterator<T> implements Iterator<T> {
		private Iterator<? extends T> _iterator;

		public ReadOnlyIterator(Iterable<T> toIterate) {
			_iterator = toIterate.iterator();
		}

		public boolean hasNext() {
			return _iterator.hasNext();
		}

		public T next() {
			return _iterator.next();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 * DOCUMENT ME!
	 * 
	 * @param position
	 *            DOCUMENT ME!
	 */
	public void setTextAnchor(int position) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_nodeDetails.overrideLabelTextAnchor(m_inx, 0, position);
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getTextAnchor() {
		synchronized (dGraphView.m_lock) {
			return DNodeDetails.convertND2G(dGraphView.m_nodeDetails
					.labelTextAnchor(m_inx, 0));
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param justify
	 *            DOCUMENT ME!
	 */
	public void setJustify(int justify) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_nodeDetails.overrideLabelJustify(m_inx, 0, justify);
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getJustify() {
		synchronized (dGraphView.m_lock) {
			return DNodeDetails.convertND2G(dGraphView.m_nodeDetails.labelJustify(
					m_inx, 0));
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param x
	 *            DOCUMENT ME!
	 */
	public void setLabelOffsetX(double x) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_nodeDetails.overrideLabelOffsetVectorX(m_inx, 0, x);
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public double getLabelOffsetX() {
		synchronized (dGraphView.m_lock) {
			return dGraphView.m_nodeDetails.labelOffsetVectorX(m_inx, 0);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param y
	 *            DOCUMENT ME!
	 */
	public void setLabelOffsetY(double y) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_nodeDetails.overrideLabelOffsetVectorY(m_inx, 0, y);
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public double getLabelOffsetY() {
		synchronized (dGraphView.m_lock) {
			return dGraphView.m_nodeDetails.labelOffsetVectorY(m_inx, 0);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param position
	 *            DOCUMENT ME!
	 */
	public void setNodeLabelAnchor(int position) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_nodeDetails.overrideLabelNodeAnchor(m_inx, 0, position);
			dGraphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getNodeLabelAnchor() {
		synchronized (dGraphView.m_lock) {
			return DNodeDetails.convertND2G(dGraphView.m_nodeDetails
					.labelNodeAnchor(m_inx, 0));
		}
	}

	@Override
	public void setVisualPropertyValue(VisualProperty<?> vp, Object value) {
		
		if (vp == DVisualLexicon.NODE_SHAPE) {
			setShape(((NodeShape) value).getGinyShape());
		} else if (vp == DVisualLexicon.NODE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
		} else if (vp == TwoDVisualLexicon.NODE_SELECTED) {
			setSelected(((Boolean) value).booleanValue());
		} else if (vp == TwoDVisualLexicon.NODE_VISIBLE) {
			if (((Boolean) value).booleanValue())
				dGraphView.showGraphObject(this);
			else
				dGraphView.hideGraphObject(this);
		} else if (vp == TwoDVisualLexicon.NODE_COLOR) { // unselected paint
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_PAINT) {
			setBorderPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_WIDTH) {
			setBorderWidth(((Double) value).floatValue());
		} else if (vp == DVisualLexicon.NODE_BORDER_STROKE) {
			setBorder((Stroke) value);
		} else if (vp == DVisualLexicon.NODE_TRANSPARENCY) {
			setTransparency(((Integer) value).floatValue());
		} else if (vp == TwoDVisualLexicon.NODE_X_SIZE) {
			setWidth(((Double) value).doubleValue());
		} else if (vp == TwoDVisualLexicon.NODE_Y_SIZE) {
			setHeight(((Double) value).doubleValue());
		} else if (vp == TwoDVisualLexicon.NODE_LABEL) {
			setText((String) value);
		} else if (vp == TwoDVisualLexicon.NODE_X_LOCATION) {
			setXPosition(((Double) value).doubleValue());
		} else if (vp == TwoDVisualLexicon.NODE_Y_LOCATION) {
			setYPosition(((Double) value).doubleValue());
		} else if (vp == DVisualLexicon.NODE_TOOLTIP) {
			setToolTip((String) value);
		} else if (vp == TwoDVisualLexicon.NODE_LABEL_COLOR) {
			setTextPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_FACE) {
			setFont((Font) value);
		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_SIZE) {
			setFont(getFont().deriveFont(((Integer) value).floatValue()));
		} else if (vp == DVisualLexicon.NODE_LABEL_TEXT_ANCHOR) {
			setTextAnchor(((Anchor) value).getGinyAnchor());
		} else if (vp == DVisualLexicon.NODE_LABEL_NODE_ANCHOR) {
			setNodeLabelAnchor(((Anchor) value).getGinyAnchor());
		} else if (vp == DVisualLexicon.NODE_LABEL_ANCHOR_X_OFFSET) {
			setLabelOffsetX(((Double) value).doubleValue());
		} else if (vp == DVisualLexicon.NODE_LABEL_ANCHOR_Y_OFFSET) {
			setLabelOffsetY(((Double) value).doubleValue());
		} else if (vp == DVisualLexicon.NODE_LABEL_JUSTIFY) {
			setJustify(((Justify) value).getGinyJustify());
		}
	}

}
