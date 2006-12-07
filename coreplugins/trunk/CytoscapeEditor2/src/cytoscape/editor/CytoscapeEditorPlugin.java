/* -*-Java-*-
********************************************************************************
*
* File:         CytoscapeEditorPlugin.java
* RCS:          $Header: $
* Description:
* Author:       Allan Kuchinsky
* Created:      Mon Aug 01 08:42:41 2005
* Modified:     Tue Dec 05 10:57:27 2006 (Michael L. Creech) creech@w235krbza760
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
import cytoscape.editor.editors.MapBioMoleculeEditorToVisualStyle;
import cytoscape.plugin.CytoscapePlugin;


/**
 * core plugin for CytoscapeEditor.
 *
 * @author Allan Kuchinsky, Agilent Technologies
 * @version 1.0
 *
 */
public class CytoscapeEditorPlugin extends CytoscapePlugin {
    public CytoscapeEditorPlugin() {
        // MLC 07/24/06 BEGIN:
        // CytoscapeEditorManager.log("CytoscapeEditor loaded ");
        // MainPluginAction mpa = new MainPluginAction();
        // CytoscapeEditorManager.setRunningEditorFramework(true);
        // CytoscapeEditorManager.log("Setting up CytoscapeEditor");
        // mpa.initializeCytoscapeEditor();
        initializeCytoscapeEditor();

        // MLC 07/24/06 END.
    }

    // MLC 07/24/06 BEGIN:
    /**
     * Overrides CytoscapePlugin.describe():
     */
    public String describe() {
        return "Add nodes and edges to a Cytoscape Network. ";
    }

    /**
     * sets various flags and registers various editors with the CytoscapeEditorManager
     *
     */
    private void initializeCytoscapeEditor() {
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

	CytoscapeEditorManager.register("SimpleBioMoleculeEditor",
	            "cytoscape.editor.event.PaletteNetworkEditEventHandler",
	            CytoscapeEditorManager.NODE_TYPE, CytoscapeEditorManager.EDGE_TYPE,
	            MapBioMoleculeEditorToVisualStyle.BIOMOLECULE_VISUAL_STYLE);
        String editorName = CytoscapeEditorManager.DEFAULT_EDITOR_TYPE;

        try {
            CytoscapeEditor cyEditor = CytoscapeEditorFactory.INSTANCE.getEditor(editorName);
            CytoscapeEditorManager.setCurrentEditor(cyEditor);
            // MLC 08/06/06:
            // CytoscapeEditorManager.setDefaultEditor(cyEditor);
        } catch (InvalidEditorException ex) {
            CytoscapeEditorManager.log("Error: cannot set up Cytoscape Editor: " +
                editorName);
        }

        Cytoscape.getDesktop()
                 .setVisualStyle(Cytoscape.getVisualMappingManager()
                                          .getCalculatorCatalog()
                                          .getVisualStyle(// MLC 08/06/06:
        // CytoscapeInit.getDefaultVisualStyle()));
        // MLC 08/06/06:
        CytoscapeInit.getProperties().getProperty("defaultVisualStyle")));
    }
    // MLC 07/24/06 BEGIN:
    //	public class MainPluginAction extends AbstractAction {
    //		public MainPluginAction() {
    //			super("Cytoscape Editor");
    //		}
    //
    //		/**
    //		 * Gives a description of this plugin.
    //		 */
    //		public String describe() {
    //			StringBuffer sb = new StringBuffer();
    //			sb.append("Add nodes and edges to a Cytoscape Network. ");
    //			return sb.toString();
    //		}
    //
    //		/**
    //		 * This method is called when the user selects the menu item.
    //		 */
    //		public void actionPerformed(ActionEvent ae) {
    //			initializeCytoscapeEditor();
    //		}
    //
    //		/**
    //		 * sets various flags and registers various editors with the CytoscapeEditorManager
    //		 *
    //		 */
    //		public void initializeCytoscapeEditor() {
    //			
    //
    //			CytoscapeEditorManager.setEditingEnabled(false);
    //
    //			CytoscapeEditorManager.initialize();
    //
    //			// add default palette-based editor
    //			CytoscapeEditorManager.register(
    //					CytoscapeEditorManager.DEFAULT_EDITOR_TYPE,
    //					"cytoscape.editor.event.PaletteNetworkEditEventHandler",
    //					// AJK: 02/03/06 have Default editor use current visual
    //					// style
    //					CytoscapeEditorManager.NODE_TYPE,
    //					CytoscapeEditorManager.EDGE_TYPE,
    //					CytoscapeEditorManager.ANY_VISUAL_STYLE);
    //
    //			CytoscapeEditorManager.register("SimpleBioMoleculeEditor",
    //					"cytoscape.editor.event.PaletteNetworkEditEventHandler",
    //					CytoscapeEditorManager.NODE_TYPE,
    //					CytoscapeEditorManager.EDGE_TYPE,
    //					MapBioMoleculeEditorToVisualStyle.BIOMOLECULE_VISUAL_STYLE);
    //
    //			String editorName = CytoscapeEditorManager.DEFAULT_EDITOR_TYPE;
    //			try {
    //				CytoscapeEditor cyEditor = CytoscapeEditorFactory.INSTANCE
    //						.getEditor(editorName);
    //				CytoscapeEditorManager.setCurrentEditor(cyEditor);
    //				CytoscapeEditorManager.setDefaultEditor(cyEditor);
    //			} catch (InvalidEditorException ex) {
    //				CytoscapeEditorManager.log("Error: cannot set up Cytoscape Editor: "
    //						+ editorName);
    //			}
    //
    //			Cytoscape.getDesktop().setVisualStyle(
    //					Cytoscape.getVisualMappingManager()
    //							.getCalculatorCatalog().getVisualStyle(
    //									CytoscapeInit.getDefaultVisualStyle()));
    //		}
    //
    //	}
    // MLC 07/24/06 END.
}
