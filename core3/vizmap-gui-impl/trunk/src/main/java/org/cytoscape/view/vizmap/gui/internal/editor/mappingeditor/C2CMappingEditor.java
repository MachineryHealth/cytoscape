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
package org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.ImageIcon;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.VizMapGUI;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.jdesktop.swingx.multislider.TrackRenderer;

/**
 * Continuous-Continuous mapping editor.<br>
 * 
 * <p>
 * This is a editor for continuous values, i.e., numbers.
 * </p>
 * 
 * @version 0.7
 * @since Cytoscape 2.5
 * @author Keiichiro Ono
 * 
 */
public class C2CMappingEditor<K, V extends Number> extends
		ContinuousMappingEditorPanel<K, V> {
	private final static long serialVersionUID = 1213748836613718L;

	// Default value for below and above.
	private final V DEF_BELOW_AND_ABOVE = (V) new Float(1f);
	private final V FIVE = (V) new Float(5f);
	
	private final V FIRST_LOCATION = (V) new Float(10f);
	private final V SECOND_LOCATION = (V) new Float(30f);
	
	/**
	 * Creates a new C2CMappingEditor object.
	 * 
	 * @param type
	 *            DOCUMENT ME!
	 */
	public C2CMappingEditor(final VisualStyle style, final ContinuousMapping<K, V> mapping, final CyTable attr) {
		super(style, mapping, attr);
		abovePanel.setVisible(false);
		belowPanel.setVisible(false);

		//FIXME
//		setSlider();
//
//		// Add two sliders by default.
//		if ((mapping != null) && (mapping.getPointCount() == 0)) {
//			addSlider(0f, FIRST_LOCATION);
//			addSlider(100f, FIRST_LOCATION);
//		}
	}

	// TODO: move this to manager.
	// /**
	// * DOCUMENT ME!
	// *
	// * @param width DOCUMENT ME!
	// * @param height DOCUMENT ME!
	// * @param title DOCUMENT ME!
	// * @param type DOCUMENT ME!
	// */
	// public static Object showDialog(final int width, final int height, final
	// String title,
	// VisualProperty type, Component parentComponent) {
	// editor = new C2CMappingEditor(type);
	// editor.setSize(new Dimension(width, height));
	// editor.setTitle(title);
	// editor.setAlwaysOnTop(true);
	// editor.setLocationRelativeTo(parentComponent);
	// editor.setVisible(true);
	//
	// return editor;
	// }

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public ImageIcon getIcon(final int iconWidth, final int iconHeight) {

		final TrackRenderer rend = slider.getTrackRenderer();

		if (rend instanceof ContinuousTrackRenderer) {
			rend.getRendererComponent(slider);

			return ((ContinuousTrackRenderer<K, V>) rend).getTrackGraphicIcon(
					iconWidth, iconHeight);
		} else {
			return null;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param width
	 *            DOCUMENT ME!
	 * @param height
	 *            DOCUMENT ME!
	 * @param type
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public ImageIcon getLegend(final int width, final int height) {

		final ContinuousTrackRenderer<K, V> rend = (ContinuousTrackRenderer<K, V>) slider
				.getTrackRenderer();
		rend.getRendererComponent(slider);

		return rend.getLegend(width, height);
	}

	//FIXME
//	// Add slider to the editor.
//	private void addSlider(float position, V value) {
//
//		final K maxValue = tracer.getMax(type);
//
//		BoundaryRangeValues<V> newRange;
//
//		if (mapping.getPointCount() == 0) {
//			slider.getModel().addThumb(position, value);
//
//			V five = (V) new Float(5);
//			newRange = new BoundaryRangeValues<V>(below, five, above);
//			mapping.addPoint(maxValue / 2, newRange);
//
//			slider.repaint();
//			repaint();
//
//			return;
//		}
//
//		// Add a new white thumb in the min.
//		slider.getModel().addThumb(position, value);
//
//		// Update continuous mapping
//		final Double newVal = maxValue;
//
//		// Pick Up first point.
//		final ContinuousMappingPoint<V> previousPoint = mapping
//				.getPoint(mapping.getPointCount() - 1);
//
//		final BoundaryRangeValues<V> previousRange = previousPoint.getRange();
//		newRange = new BoundaryRangeValues<V>(previousRange);
//
//		newRange.lesserValue = slider.getModel().getSortedThumbs().get(
//				slider.getModel().getThumbCount() - 1).getObject();
//		System.out.println("EQ color = " + newRange.lesserValue);
//		newRange.equalValue = FIVE;
//		newRange.greaterValue = previousRange.greaterValue;
//		mapping.addPoint(maxValue, newRange);
//
//		updateMap();
//
//		// Cytoscape.redrawGraph(vmm.getNetworkView());
//
//		slider.repaint();
//		repaint();
//	}

	@Override
	protected void addButtonActionPerformed(ActionEvent evt) {
		//FIXME
		//addSlider(100f, FIVE);
	}

	@Override
	protected void deleteButtonActionPerformed(ActionEvent evt) {
		final int selectedIndex = slider.getSelectedIndex();

		if ((0 <= selectedIndex) && (slider.getModel().getThumbCount() > 1)) {
			slider.getModel().removeThumb(selectedIndex);
			mapping.removePoint(selectedIndex);

			updateMap();
			((ContinuousTrackRenderer) slider.getTrackRenderer())
					.removeSquare(selectedIndex);

			// mapping.fireStateChanged();

			// Cytoscape.redrawGraph(vmm.getNetworkView());
			repaint();
		}
	}

	//FIXME
//	private void setSlider() {
//		Dimension dim = new Dimension(600, 100);
//		setPreferredSize(dim);
//		setSize(dim);
//		setMinimumSize(new Dimension(300, 80));
//		slider.updateUI();
//
//		final double minValue = tracer.getMin(type);
//		double actualRange = tracer.getRange(type);
//
//		BoundaryRangeValues<V> bound;
//		Float fraction;
//
//		if (allPoints == null) {
//			return;
//		}
//
//		for (ContinuousMappingPoint<V> point : allPoints) {
//			bound = point.getRange();
//
//			fraction = ((Number) ((point.getValue() - minValue) / actualRange))
//					.floatValue() * 100;
//			slider.getModel().addThumb(fraction, bound.equalValue);
//		}
//
//		if (allPoints.size() != 0) {
//			below = allPoints.get(0).getRange().lesserValue;
//			above = allPoints.get(allPoints.size() - 1).getRange().greaterValue;
//		} else {
//			below = DEF_BELOW_AND_ABOVE;
//			above = DEF_BELOW_AND_ABOVE;
//		}
//
//		/*
//		 * get min and max for the value object
//		 */
//		TriangleThumbRenderer thumbRend = new TriangleThumbRenderer(slider);
//
//		ContinuousTrackRenderer<V> cRend = new ContinuousTrackRenderer<V>(type,
//				below, above, tracer);
//		cRend.addPropertyChangeListener(this);
//
//		slider.setThumbRenderer(thumbRend);
//		slider.setTrackRenderer(cRend);
//		slider.addMouseListener(new ThumbMouseListener());
//	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param evt
	 *            DOCUMENT ME!
	 */
	//TODO: refactor event
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(
				ContinuousMappingEditorPanel.BELOW_VALUE_CHANGED)) {
			below = (V) evt.getNewValue();
		} else if (evt.getPropertyName().equals(
				ContinuousMappingEditorPanel.ABOVE_VALUE_CHANGED)) {
			above = (V) evt.getNewValue();
		}
	}
}
