package org.cytoscape.view.vizmap;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import org.cytoscape.view.model.CyNetworkView;
import org.junit.Test;

public abstract class AbstractVisualMappingManagerTest {

	protected VisualMappingManager vmm;
	protected VisualStyle defaultStyle;

	@Test
	public void testVisualMappingManager() {
		assertNotNull(vmm);
	}

	@Test
	public void testGetAndSetVisualStyle() {
		final VisualStyle style1 = mock(VisualStyle.class);
		final VisualStyle style2 = mock(VisualStyle.class);
		final VisualStyle style3 = mock(VisualStyle.class);
		
		final CyNetworkView view1 = mock(CyNetworkView.class);
		final CyNetworkView view2 = mock(CyNetworkView.class);
		final CyNetworkView view3 = mock(CyNetworkView.class);
		
		assertNotNull(vmm.getAllVisualStyles());
		
		//Should contain default style.
		assertEquals(1, vmm.getAllVisualStyles().size());
		assertEquals(defaultStyle, vmm.getDefaultVisualStyle());
		
		vmm.setVisualStyle(style1, view1);
		vmm.addVisualStyle(style2);
		vmm.setVisualStyle(style2, view2);
		final VisualStyle targetStyle = vmm.getVisualStyle(view3);
		
		assertEquals(defaultStyle, targetStyle);
		assertEquals(3, vmm.getAllVisualStyles().size());
		
		vmm.setVisualStyle(style3, view3);
		assertEquals(4, vmm.getAllVisualStyles().size());
		assertEquals(style3, vmm.getVisualStyle(view3));
	}


	@Test
	public void testAddAndRemoveVisualStyle() {
		
		int originalSize = vmm.getAllVisualStyles().size();
		
		final VisualStyle style1 = mock(VisualStyle.class);
		final VisualStyle style2 = mock(VisualStyle.class);
		
		vmm.addVisualStyle(style1);
		vmm.addVisualStyle(style2);
		assertEquals(originalSize + 2, vmm.getAllVisualStyles().size());
		vmm.removeVisualStyle(style2);
		assertEquals(originalSize + 1, vmm.getAllVisualStyles().size());
		
	}
}
