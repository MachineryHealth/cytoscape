//-------------------------------------------------------------------------
// $Revision$
// $Date$
// $Author$
//-------------------------------------------------------------------------
package cytoscape.actions;
//-------------------------------------------------------------------------
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import giny.model.RootGraph;
import giny.model.GraphPerspective;
import giny.view.GraphView;

import cytoscape.data.GraphObjAttributes;
import cytoscape.*;
import cytoscape.view.CyWindow;
//-------------------------------------------------------------------------
public class NewWindowSelectedNodesOnlyAction extends AbstractAction {
    CyWindow cyWindow;

    public NewWindowSelectedNodesOnlyAction(CyWindow cyWindow) {
        super("Selected nodes, All edges");
        this.cyWindow = cyWindow;
    }

    public void actionPerformed(ActionEvent e) {
        //save the vizmapper catalog
        cyWindow.getCytoscapeObj().saveCalculatorCatalog();
        CyNetwork oldNetwork = cyWindow.getNetwork();
        String callerID = "NewWindowSelectedNodesOnlyAction.actionPerformed";
        oldNetwork.beginActivity(callerID);
        GraphView view = cyWindow.getView();
        int [] nodes = view.getSelectedNodeIndices();
       
        CyNetwork newNetwork = Cytoscape.createNetwork( nodes, oldNetwork.getConnectingEdgeIndicesArray( nodes ) );
        newNetwork.setExpressionData( oldNetwork.getExpressionData() );

        String title = " selection";
        try {
            //this call creates a WindowOpened event, which is caught by
            //cytoscape.java, enabling that class to manage the set of windows
            //and quit when the last window is closed
            CyWindow newWindow = new CyWindow(cyWindow.getCytoscapeObj(),
                                              newNetwork, title);
            newWindow.showWindow();
        } catch (Exception e00) {
            System.err.println("exception when creating new window");
            e00.printStackTrace();
        }
        cyWindow.getView().addGraphViewChangeListener(cyWindow.getCyMenus());
        cyWindow.getCyMenus().setNodesRequiredItemsEnabled();
    }
}

