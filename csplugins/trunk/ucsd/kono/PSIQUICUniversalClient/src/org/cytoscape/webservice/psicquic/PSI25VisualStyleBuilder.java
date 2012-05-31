package org.cytoscape.webservice.psicquic;

import static cytoscape.visual.VisualPropertyType.NODE_LABEL;
import giny.view.Justification;
import giny.view.Position;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.webservice.psicquic.mapper.Mitab25Mapper;
import org.omg.CORBA.PRIVATE_MEMBER;

import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.LineStyle;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.visual.mappings.PassThroughMapping;
import ding.view.ObjectPositionImpl;

public class PSI25VisualStyleBuilder {

	// Default visual style name
	private static final String DEF_VS_NAME = "PSI-MI 25 Style";

	// Prefix for all PSI-MI 25 attributes
	public static final String ATTR_PREFIX = "PSI-MI-25.";

	// Top level interaction types
	private static final String[] ITR_TYPE_ROOT_TERMS = { "MI:0208", "MI:0403", "MI:0914" };

	// Presets
	private static final Color OBJECT_COLOR = new Color(0, 128, 128);
	
	
	
	
	private static final Map<String, String> type = new HashMap<String, String>();

	private static VisualStyle defStyle;

	static {
		buildOntologyMap();
		defStyle = defaultVisualStyleBuilder();
	}

	public static VisualStyle getDefVS() {
		return defStyle;
	}

	// Actual design
	private static VisualStyle defaultVisualStyleBuilder() {

		final VisualStyle defStyle = new VisualStyle(DEF_VS_NAME);

		NodeAppearanceCalculator nac = defStyle.getNodeAppearanceCalculator();
		EdgeAppearanceCalculator eac = defStyle.getEdgeAppearanceCalculator();
		GlobalAppearanceCalculator gac = defStyle.getGlobalAppearanceCalculator();

		// Default values
		final int edgeOp = 110;
		final Color nodeColor = OBJECT_COLOR;
		final Color nodeLineColor = new Color(40, 40, 40);
		final Color nodeLabelColor = new Color(30, 30, 30);

		final Color nodeCompoundColor = new Color(100, 100, 100);
		final Color nodeNestedColor = Color.white;

		final Color edgeColor = OBJECT_COLOR;
		final Font nodeLabelFont = new Font("Helvetica", Font.PLAIN, 14);
		// final Color nodeLabelColor = new Color(105,105,105);

		gac.setDefaultBackgroundColor(Color.white);

		final PassThroughMapping m = new PassThroughMapping(String.class, Mitab25Mapper.PREDICTED_GENE_NAME);

		final Calculator calc = new BasicCalculator(DEF_VS_NAME + "-" + "NodeLabelMapping", m, NODE_LABEL);
		// PassThroughMapping me = new PassThroughMapping("", ATTR_PREFIX +
		// "interaction type");
		//
		// EdgeCalculator calce = new EdgeCalculator(DEF_VS_NAME + "-"
		// + "EdgeLabelMapping", me, null, EDGE_LABEL);
		nac.setCalculator(calc);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_FILL_COLOR, nodeColor);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_SHAPE, NodeShape.RECT);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_OPACITY, 120);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_BORDER_OPACITY, 200);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_LINE_WIDTH, 2);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_BORDER_COLOR, nodeLineColor);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_WIDTH, 25);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_HEIGHT, 15);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_LABEL_COLOR, nodeLabelColor);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_FONT_FACE, nodeLabelFont);

		// TODO: this is for 2.8.0
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_LABEL_POSITION,
				new ObjectPositionImpl(Position.SOUTH, Position.NORTH_WEST, Justification.JUSTIFY_CENTER, -7, 1.0));

		nac.getDefaultAppearance().set(VisualPropertyType.NODE_FONT_SIZE, 14);
		nac.setNodeSizeLocked(false);

		DiscreteMapping nodeColorMapping = new DiscreteMapping(nodeColor, ATTR_PREFIX + "interactor type",
				ObjectMapping.NODE_MAPPING);
		nodeColorMapping.putMapValue("compound", nodeCompoundColor);
		nodeColorMapping.putMapValue("nested", nodeNestedColor);
		final Calculator nodeColorCalc = new BasicCalculator(DEF_VS_NAME + "-" + "NodeColorMapping", nodeColorMapping,
				VisualPropertyType.NODE_FILL_COLOR);
		nac.setCalculator(nodeColorCalc);

		DiscreteMapping nodeShapeMapping = new DiscreteMapping(NodeShape.RECT, ATTR_PREFIX + "interactor type",
				ObjectMapping.NODE_MAPPING);
		nodeShapeMapping.putMapValue("compound", NodeShape.ELLIPSE);
		nodeShapeMapping.putMapValue("nested", NodeShape.ROUND_RECT);
		final Calculator nodeShapeCalc = new BasicCalculator(DEF_VS_NAME + "-" + "NodeShapeMapping", nodeShapeMapping,
				VisualPropertyType.NODE_SHAPE);
		nac.setCalculator(nodeShapeCalc);

		DiscreteMapping nodeWidthMapping = new DiscreteMapping(30, ATTR_PREFIX + "interactor type",
				ObjectMapping.NODE_MAPPING);
		nodeWidthMapping.putMapValue("compound", 15);
		nodeWidthMapping.putMapValue("nested", 100);
		final Calculator nodeWidthCalc = new BasicCalculator(DEF_VS_NAME + "-" + "NodeWidthMapping", nodeWidthMapping,
				VisualPropertyType.NODE_WIDTH);
		nac.setCalculator(nodeWidthCalc);
		DiscreteMapping nodeHeightMapping = new DiscreteMapping(30, ATTR_PREFIX + "interactor type",
				ObjectMapping.NODE_MAPPING);
		nodeHeightMapping.putMapValue("compound", 15);
		nodeHeightMapping.putMapValue("nested", 100);
		final Calculator nodeHeightCalc = new BasicCalculator(DEF_VS_NAME + "-" + "NodeHeightMapping",
				nodeHeightMapping, VisualPropertyType.NODE_HEIGHT);
		nac.setCalculator(nodeHeightCalc);

		// eac.setCalculator(calce);
		eac.getDefaultAppearance().set(VisualPropertyType.EDGE_COLOR, edgeColor);
		eac.getDefaultAppearance().set(VisualPropertyType.EDGE_LABEL_COLOR, Color.red);
		eac.getDefaultAppearance().set(VisualPropertyType.EDGE_FONT_SIZE, 5);

		eac.getDefaultAppearance().set(VisualPropertyType.EDGE_OPACITY, edgeOp);
		eac.getDefaultAppearance().set(VisualPropertyType.EDGE_SRCARROW_OPACITY, edgeOp);
		eac.getDefaultAppearance().set(VisualPropertyType.EDGE_TGTARROW_OPACITY, edgeOp);
		// eac.getDefaultAppearance().set(VisualPropertyType.EDGE_LABEL_OPACITY,
		// 80);
		eac.getDefaultAppearance().set(VisualPropertyType.EDGE_LINE_WIDTH, 1);
		eac.getDefaultAppearance().set(VisualPropertyType.EDGE_LABEL, "");

		// Interaction Type mapping
		DiscreteMapping lineStyle = new DiscreteMapping(LineStyle.SOLID, ATTR_PREFIX + "interaction type",
				ObjectMapping.EDGE_MAPPING);
		DiscreteMapping lineWidth = new DiscreteMapping(1.0, ATTR_PREFIX + "interaction type",
				ObjectMapping.EDGE_MAPPING);
		DiscreteMapping edgeColorMap = new DiscreteMapping(Color.black, ATTR_PREFIX + "interaction type",
				ObjectMapping.EDGE_MAPPING);
		generateInteractionTypeMap(lineStyle, lineWidth, edgeColorMap);

		final Calculator lineStyleCalc = new BasicCalculator(DEF_VS_NAME + "-" + "EdgeLineStyleMapping", lineStyle,
				VisualPropertyType.EDGE_LINE_STYLE);

		final Calculator lineWidthCalc = new BasicCalculator(DEF_VS_NAME + "-" + "EdgeLineWidthMapping", lineWidth,
				VisualPropertyType.EDGE_LINE_WIDTH);

		final Calculator edgeColorCalc = new BasicCalculator(DEF_VS_NAME + "-" + "EdgeColorMapping", edgeColorMap,
				VisualPropertyType.EDGE_COLOR);

		//
		// DiscreteMapping sourceShape = new DiscreteMapping(ArrowShape.NONE,
		// "source experimental role", ObjectMapping.EDGE_MAPPING);
		//
		// sourceShape.putMapValue("bait", ArrowShape.DIAMOND);
		// sourceShape.putMapValue("prey", ArrowShape.CIRCLE);
		//
		// EdgeCalculator sourceShapeCalc = new EdgeCalculator(DEF_VS_NAME + "-"
		// + "EdgeSourceArrowShapeMapping", sourceShape, null,
		// VisualPropertyType.EDGE_SRCARROW_SHAPE);
		//
		// DiscreteMapping targetColor = new DiscreteMapping(Color.black,
		// "target experimental role", ObjectMapping.EDGE_MAPPING);
		//
		// targetColor.putMapValue("bait", Color.red);
		// targetColor.putMapValue("prey", Color.red);
		//
		// EdgeCalculator targetColorCalc = new EdgeCalculator(DEF_VS_NAME + "-"
		// + "EdgeTargetArrowColorMapping", targetColor, null,
		// VisualPropertyType.EDGE_TGTARROW_COLOR);
		//
		// DiscreteMapping sourceColor = new DiscreteMapping(Color.black,
		// "source experimental role", ObjectMapping.EDGE_MAPPING);
		//
		// sourceColor.putMapValue("bait", Color.red);
		// sourceColor.putMapValue("prey", Color.red);
		//
		// EdgeCalculator sourceColorCalc = new EdgeCalculator(DEF_VS_NAME + "-"
		// + "EdgeSourceArrowColorMapping", targetColor, null,
		// VisualPropertyType.EDGE_SRCARROW_COLOR);
		//
		eac.setCalculator(lineStyleCalc);
		eac.setCalculator(lineWidthCalc);
		eac.setCalculator(edgeColorCalc);
		// eac.setCalculator(targetColorCalc);

		return defStyle;
	}

	private static void generateInteractionTypeMap(DiscreteMapping lineStyle, DiscreteMapping lineWidth,
			DiscreteMapping edgeColor) {
		// TODO Auto-generated method stub
		// for (String childTerm : type.keySet()) {
		// if ((type.get(childTerm)).equals("MI:0208")) {
		// lineStyle.putMapValue(childTerm, LineStyle.LONG_DASH);
		// lineWidth.putMapValue(childTerm, 2.0);
		// edgeColor.putMapValue(childTerm, Color.CYAN);
		// } else if ((type.get(childTerm)).equals("MI:0403")) {
		// lineStyle.putMapValue(childTerm, LineStyle.SOLID);
		// lineWidth.putMapValue(childTerm, 2.0);
		// edgeColor.putMapValue(childTerm, Color.green);
		// } else if ((type.get(childTerm)).equals("MI:0914")) {
		// lineStyle.putMapValue(childTerm, LineStyle.SOLID);
		// lineWidth.putMapValue(childTerm, 3.0);
		// edgeColor.putMapValue(childTerm, Color.DARK_GRAY);
		// }
		// }

		lineStyle.putMapValue("MI:0208", LineStyle.LONG_DASH);
		lineWidth.putMapValue("MI:0208", 3.0);
		edgeColor.putMapValue("MI:0208", Color.CYAN);

		lineStyle.putMapValue("MI:0403", LineStyle.SOLID);
		lineWidth.putMapValue("MI:0403", 1.0);
		edgeColor.putMapValue("MI:0403", Color.green);

		lineStyle.putMapValue("MI:0914", LineStyle.SOLID);
		lineWidth.putMapValue("MI:0914", 3.0);
		edgeColor.putMapValue("MI:0914", Color.DARK_GRAY);
	}

	private static void buildOntologyMap() {

		// try {
		// System.out.println("############Ontology Test=============");
		//
		// // Get child terms for each interaction type category.
		// for (String rootTerm : ITR_TYPE_ROOT_TERMS) {
		// final Map<String, String> children = OLSUtil
		// .getAllChildren(rootTerm);
		// for (String childTerm : children.keySet()) {
		// type.put(childTerm, rootTerm);
		// System.out.println(childTerm + ", root = " + rootTerm);
		// }
		// }
		//
		// System.out.println("############Ontology Test DONE!=============");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

	}

}
