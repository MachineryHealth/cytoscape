package org.cytoscape.view.vizmap;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.awt.Color;
import java.awt.Paint;
import java.util.Properties;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.property.CyProperty;
import org.cytoscape.test.support.NetworkViewTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractVisualStyleTest {
	
	protected static final String attrName = "Sample attr 1";
	
	
	protected static final Color RED1 = new Color(200, 0, 0);
	protected static final Color GREEN1 = new Color(0, 200, 0);
	protected static final Color RED2 = new Color(100, 0, 0);
	protected static final Color GREEN2 = new Color(0, 100, 0);
	
	protected VisualStyle style;
	
	protected String originalTitle;
	protected String newTitle;
	
	protected VisualLexicon lexicon;
	
	protected DiscreteMapping<String, Paint> colorMapping1;
	protected DiscreteMapping<String, Paint> colorMapping2;
	
	
	// Simple test network
	protected CyNode node1;
	protected CyNode node2;
	protected CyNode node3;
	protected CyEdge edge;
	protected CyNetwork network;
	protected CyNetworkView networkView;
	
	
	@Before
	public void setUp() throws Exception {
		
		final Class<String> type = String.class;
		colorMapping1 = new DiscreteMapping<String, Paint>(attrName, type,
				MinimalVisualLexicon.NODE_PAINT);
		colorMapping2 = new DiscreteMapping<String, Paint>(attrName, type,
				MinimalVisualLexicon.NODE_FILL_COLOR);
		
		colorMapping1.putMapValue("red", RED2);
		colorMapping1.putMapValue("green", GREEN2);
		colorMapping1.putMapValue("blue", Color.BLUE);
		
		colorMapping2.putMapValue("red", RED1);
		colorMapping2.putMapValue("green", GREEN1);
		colorMapping2.putMapValue("blue", Color.BLUE);
		
		CyProperty<Properties> cyProperties = mock(CyProperty.class);
		NetworkViewTestSupport nvts = new NetworkViewTestSupport(cyProperties);
		network = nvts.getNetworkFactory().getInstance();
		
		node1 = network.addNode();
		node2 = network.addNode();
		node3 = network.addNode();
		
		edge = network.addEdge(node1, node2, true);
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(attrName, String.class, true);
		nodeTable.getRow(node1.getSUID()).set(attrName, "red");
		nodeTable.getRow(node2.getSUID()).set(attrName, "green");
		nodeTable.getRow(node3.getSUID()).set(attrName, "foo");
		
		networkView = nvts.getNetworkViewFactory().getNetworkView(network);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testVisualStyle() {
		assertNotNull(style);
		assertNotNull(originalTitle);
		assertNotNull(newTitle);
		assertNotNull(network);
		assertNotNull(networkView);
		assertNotNull(colorMapping1);
		assertNotNull(networkView.getNodeView(node1));
		assertNotNull(networkView.getNodeView(node2));
		assertNotNull(networkView.getNodeView(node3));
		
		assertEquals("red", networkView.getModel().getDefaultNodeTable().getRow(node1.getSUID()).get(attrName, String.class));
		assertEquals("green", networkView.getModel().getDefaultNodeTable().getRow(node2.getSUID()).get(attrName, String.class));
		assertEquals("foo", networkView.getModel().getDefaultNodeTable().getRow(node3.getSUID()).get(attrName, String.class));

		// Test title
		assertEquals(originalTitle, style.getTitle());
		style.setTitle(newTitle);
		assertEquals(newTitle, style.getTitle());
		
		style.addVisualMappingFunction(colorMapping1);
		style.addVisualMappingFunction(colorMapping2);
		style.setDefaultValue(MinimalVisualLexicon.NODE_PAINT, Color.BLACK);
		style.setDefaultValue(MinimalVisualLexicon.NODE_FILL_COLOR, Color.PINK);
		
		final Paint defaultNodeColor = MinimalVisualLexicon.NODE_FILL_COLOR.getDefault();
		final Paint defaultNodePaint = MinimalVisualLexicon.NODE_PAINT.getDefault();
		final View<CyNode> nodeView1 = networkView.getNodeView(node1);
		final View<CyNode> nodeView2 = networkView.getNodeView(node2);
		final View<CyNode> nodeView3 = networkView.getNodeView(node3);
		
		// Before apply call, all node views should have same color (property default).
		assertEquals(defaultNodeColor, nodeView1.getVisualProperty(MinimalVisualLexicon.NODE_FILL_COLOR));
		assertEquals(defaultNodeColor, nodeView2.getVisualProperty(MinimalVisualLexicon.NODE_FILL_COLOR));
		assertEquals(defaultNodeColor, nodeView3.getVisualProperty(MinimalVisualLexicon.NODE_FILL_COLOR));
		assertEquals(defaultNodePaint, nodeView1.getVisualProperty(MinimalVisualLexicon.NODE_PAINT));
		assertEquals(defaultNodePaint, nodeView2.getVisualProperty(MinimalVisualLexicon.NODE_PAINT));
		assertEquals(defaultNodePaint, nodeView3.getVisualProperty(MinimalVisualLexicon.NODE_PAINT));
		
		// Two existing mappings.
		assertEquals(2, style.getAllVisualMappingFunctions().size());
		
		// Apply mappings.
		style.apply(networkView);
		
		// Check defaults
		assertEquals(Color.PINK, style.getDefaultValue(MinimalVisualLexicon.NODE_FILL_COLOR));
		assertEquals(Color.BLACK, style.getDefaultValue(MinimalVisualLexicon.NODE_PAINT));
		
		// Check results.
		assertEquals(RED1, nodeView1.getVisualProperty(MinimalVisualLexicon.NODE_FILL_COLOR));
		assertEquals(GREEN1, nodeView2.getVisualProperty(MinimalVisualLexicon.NODE_FILL_COLOR));
		assertEquals(RED2, nodeView1.getVisualProperty(MinimalVisualLexicon.NODE_PAINT));
		assertEquals(GREEN2, nodeView2.getVisualProperty(MinimalVisualLexicon.NODE_PAINT));
		
		// Check default values.  Leaf node will be applied.
		assertEquals(Color.BLACK, nodeView3.getVisualProperty(MinimalVisualLexicon.NODE_PAINT));
		assertEquals(Color.PINK, nodeView3.getVisualProperty(MinimalVisualLexicon.NODE_FILL_COLOR));
	}
	
}
