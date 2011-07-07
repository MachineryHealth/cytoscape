package org.cytoscape.work; 

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.MavenConfiguredJUnit4TestRunner;

import org.cytoscape.work.swing.GUITaskManager;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.swing.GUITunableInterceptor;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.integration.ServiceTestSupport;

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.BookmarksUtil;

import java.util.Properties;

@RunWith(MavenConfiguredJUnit4TestRunner.class)
public class ServiceConfigurationTest extends ServiceTestSupport {

	@Before
	public void setup() {
		Properties coreP = new Properties();
		coreP.setProperty("cyPropertyName","coreSettings");
		
		Properties p = new Properties();
		p.setProperty("cyPropertyName","bookmarks");
		
		registerMockService(CyProperty.class, coreP);
		registerMockService(CyProperty.class, p);
		registerMockService(BookmarksUtil.class);
	}
	
	@Test
	public void testExpectedServices() {
		checkService(GUITaskManager.class);
		checkService(TaskManager.class);
		checkService(GUITunableHandlerFactory.class);
		checkService(TunableInterceptor.class);
		checkService(GUITunableInterceptor.class);
		checkService(UndoSupport.class);
	}
}
