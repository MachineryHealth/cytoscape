/* -*-Java-*-
********************************************************************************
*
* File:         CytoscapeEditorPlugin.java
* RCS:          $Header: $
* Description:
* Author:       Allan Kuchinsky
* Created:      Mon Aug 01 08:42:41 2005
* Modified:     Sat Jun 30 04:41:38 2007 (Michael L. Creech) creech@w235krbza760
* Language:     Java
* Package:
* Status:       Experimental (Do Not Distribute)
*
* (c) Copyright 2006, Agilent Technologies, all rights reserved.
*
********************************************************************************
*
* Revisions:
*
* Fri Jun 29 09:30:00 2007 (Michael L. Creech) creech@w235krbza760
*  Updated VERSION to 2.51.
* Fri May 11 16:37:23 2007 (Michael L. Creech) creech@w235krbza760
*  Updated VERSION to 2.50 and added getPluginInfoObject() for Cytoscape 2.5.
* Fri Dec 15 10:08:07 2006 (Michael L. Creech) creech@w235krbza760
*  Hacked a fix for not reinitializing the plugin if it is loaded
*  first by another plugin.
* Sat Aug 05 08:14:51 2006 (Michael L. Creech) creech@w235krbza760
*  Removed deprecated call to CytoscapeInit.getDefaultVisualStyle() in
*  initializeCytoscapeEditor().
* Mon Jul 24 08:43:51 2006 (Michael L. Creech) creech@w235krbza760
*  Removed some misleading code--println that editor is loaded and
*  initialization code as though editor is initialized via a menu item, when
*  it is not.
********************************************************************************
*/
package cytoscape.editor;

import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.plugin.CytoscapePlugin;


/**
 * core plugin for CytoscapeEditor.
 *
 * @author Allan Kuchinsky, Agilent Technologies
 * @version 1.0
 *
 */
public class CytoscapeEditorPlugin extends CytoscapePlugin {
    // MLC 12/11/06:
    private static boolean _initialized = false;
    // MLC 05/11/07:
    private static final double VERSION = 2.51;

	/**
	 * Creates a new CytoscapeEditorPlugin object.
	 */
	public CytoscapeEditorPlugin() {
		initializeCytoscapeEditor();
	}

    // MLC 06/30/07 BEGIN:
    // overrides CytoscapePlugin.getPluginInfoObject():
    //    public PluginInfo getPluginInfoObject() {
    //        PluginInfo info = new PluginInfo();
    //        info.setName("CytoscapeEditor");
    //        info.setDescription("Add nodes and edges to a Cytoscape Network.");
    //        info.setCategory(PluginInfo.Category.CORE);
    //        info.setPluginVersion(VERSION);
    //        info.setCytoscapeVersion("2.5");
    //        // info.setProjectUrl("http://www.cytoscape.org/download_agilent_literature_search_v2.5.php?file=litsearch_v2.4");
    //        info.addAuthor("Allan Kuchinsky", "Agilent Labs");
    //        info.addAuthor("Michael Creech", "Blue Oak Software");
    //        return info;
    //    }
    // MLC 06/30/07 END.

	// MLC 07/24/06 BEGIN:

	/**
	 * sets various flags and registers various editors with the CytoscapeEditorManager
	 *
	 */

	// MLC 12/11/06 BEGIN:
	// private void initializeCytoscapeEditor() {
	public static void initializeCytoscapeEditor() {
		if (_initialized) {
			return;
		}

		// MLC 12/11/06 END.
		// MLC 07/24/06:
		CytoscapeEditorManager.setRunningEditorFramework(true);

		CytoscapeEditorManager.setEditingEnabled(false);

		CytoscapeEditorManager.initialize();

		// add default palette-based editor
		CytoscapeEditorManager.register(CytoscapeEditorManager.DEFAULT_EDITOR_TYPE,
		                                "cytoscape.editor.event.PaletteNetworkEditEventHandler",
		                                // AJK: 02/03/06 have Default editor use current visual
		// style
		CytoscapeEditorManager.NODE_TYPE, CytoscapeEditorManager.EDGE_TYPE,
		                                CytoscapeEditorManager.ANY_VISUAL_STYLE);

		String editorName = CytoscapeEditorManager.DEFAULT_EDITOR_TYPE;

		try {
			CytoscapeEditor cyEditor = CytoscapeEditorFactory.INSTANCE.getEditor(editorName);
			CytoscapeEditorManager.setCurrentEditor(cyEditor);

			// MLC 08/06/06:
			// CytoscapeEditorManager.setDefaultEditor(cyEditor);
		} catch (InvalidEditorException ex) {
			CytoscapeEditorManager.log("Error: cannot set up Cytoscape Editor: " + editorName);
		}

		Cytoscape.getDesktop().setVisualStyle(Cytoscape.getVisualMappingManager()
		                                               .getCalculatorCatalog()
		                                               .getVisualStyle( // MLC 08/06/06:
		                                                                // CytoscapeInit.getDefaultVisualStyle()));
		                                                                // MLC 08/06/06:
		CytoscapeInit.getProperties().getProperty("defaultVisualStyle")));
		// MLC 12/11/06:
		_initialized = true;
	}

}
