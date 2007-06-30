//ActiveModulesUI.java
//------------------------------------------------------------------------------
package csplugins.jActiveModules;
//------------------------------------------------------------------------------

import java.awt.event.ActionEvent;

import javax.swing.SwingConstants;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.beans.*;

import csplugins.jActiveModules.data.ActivePathFinderParameters;
import csplugins.jActiveModules.dialogs.ActivePathsParametersPopupDialog;
import cytoscape.Cytoscape;
import cytoscape.CyNetwork;
import cytoscape.CytoscapeInit;
import cytoscape.data.CyAttributes;
import cytoscape.data.attr.MultiHashMapDefinitionListener;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.plugin.PluginInfo;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;

//------------------------------------------------------------------------------
/**
 * UI for Active Modules. Manages the various menu items
 */
public class ActiveModulesUI extends CytoscapePlugin {

  protected ActivePaths activePaths;
  protected ActivePathFinderParameters apfParams;
  protected ThreadExceptionHandler xHandler;

  public ActiveModulesUI () {
    System.err.println("Starting jActiveModules plugin!\n");
    /* initialize variables */
    //JMenu topMenu = new JMenu("jActiveModules");
    JMenuItem jActiveModulesMenuItem = new JMenuItem(new SetParametersAction());
    Cytoscape.getDesktop().getCyMenus().getOperationsMenu().add(jActiveModulesMenuItem);

    /* Add function calls to Cytoscape menus */
    //topMenu.add ( new SetParametersAction() );
    //topMenu.add ( new FindActivePathsAction () );
    //topMenu.add ( new ScoreSubComponentAction () );
    //topMenu.add ( new RandomizeAndRunAction () );

    //cytoscapeWindow.getCyMenus().getOperationsMenu().add ( new ScoreSubComponentAction () );
    //cytoscapeWindow.getCyMenus().getOperationsMenu().add ( new RandomizeAndRunAction () );

    /* check for command line arguments to run right away */
    //String [] args = CytoscapeInit.getArgs();
    ActivePathsCommandLineParser parser = new ActivePathsCommandLineParser(null);
    apfParams = parser.getActivePathFinderParameters();
	apfParams.reloadExpressionAttributes();
    AttrChangeListener acl = new AttrChangeListener();
    Cytoscape.getPropertyChangeSupport().addPropertyChangeListener( Cytoscape.ATTRIBUTES_CHANGED, acl );
    Cytoscape.getNodeAttributes().getMultiHashMapDefinition().addDataDefinitionListener( acl );
    xHandler = new ThreadExceptionHandler();
    if (apfParams.getRun()) {
      activePaths = new ActivePaths(Cytoscape.getCurrentNetwork(),apfParams, this);
      Thread t = new Thread(activePaths);
      // Since this is cmdline, there is no sense in using the ThreadExceptionHandler.
      t.start();
    }
  }

  /**
   * Action to allow the user to change the current options
   * for running jActiveModules, wiht a gui interface
   */
  protected class SetParametersAction extends AbstractAction {
    private ActivePathsParametersPopupDialog paramsDialog = null;
    
    public SetParametersAction(){
      super("jActiveModules");
    }

    public void actionPerformed(ActionEvent e){
      if (apfParams.getPossibleExpressionAttributes().size() == 0)
      {
        JOptionPane.showMessageDialog(
	  Cytoscape.getDesktop(),
	  "Cannot start jActiveModules:\n" +
	  "There are no node attributes of type float.",
	  "jActiveModules",
	  JOptionPane.ERROR_MESSAGE
	);
	return;
      }
      CytoPanel cytoPanel = Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST);
      if (paramsDialog == null)
      {
        paramsDialog = new ActivePathsParametersPopupDialog 
	  (Cytoscape.getDesktop(), "Find Active Modules Parameters", apfParams, ActiveModulesUI.this);
        paramsDialog.setVisible (true);
      }
      int index = cytoPanel.indexOfComponent(paramsDialog);
      if (index < 0)
      {
        cytoPanel.add("jActiveModules", paramsDialog);
        index = cytoPanel.indexOfComponent(paramsDialog);
      }
      cytoPanel.setSelectedIndex(index);
      cytoPanel.setState(CytoPanelState.DOCK);
    }
  }

  public void startFindActivePaths(CyNetwork network)
  {
        try {
	activePaths = new ActivePaths(network,apfParams,this);  
	} catch (Exception e) {
		JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Error running jActiveModules!  " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		return;
	}
	Thread t = new Thread(activePaths);
	t.setUncaughtExceptionHandler( xHandler );
	t.start();
  }

  /**
   * This action will run activePaths with the current parameters
   */
  protected class FindActivePathsAction extends AbstractAction{  
    
    FindActivePathsAction () { super ("Active Modules: Find Modules"); }
	
    public void actionPerformed (ActionEvent ae) {
      startFindActivePaths(Cytoscape.getCurrentNetwork());
    }
  } 

  protected class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
	public void uncaughtException(Thread t, Throwable e) {
		System.out.println("Non-fatal exception in Thread " + t.getName() + ":");
		e.printStackTrace();
		System.out.println("The previous exception was non-fatal - Don't panic!");
		JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Error running jActiveModules!  " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	}
  }
     
  /**
   * This action will generate a score for the currently selected
   * nodes in the view
   */
  protected class ScoreSubComponentAction extends AbstractAction {
	
     ScoreSubComponentAction () { super ("Active Modules: Score Selected Nodes"); }
     public void actionPerformed (ActionEvent e) {
       activePaths = new ActivePaths(Cytoscape.getCurrentNetwork(),apfParams,ActiveModulesUI.this);  
       activePaths.scoreActivePath ();
     } 
   }

   public void startRandomizeAndRun(CyNetwork network)
   {
       activePaths = new ActivePaths(network,apfParams,ActiveModulesUI.this);
       Thread t = new ScoreDistributionThread(network,activePaths,apfParams);
       t.setUncaughtExceptionHandler( xHandler );
       t.start();	
   }


   public class RandomizeAndRunAction extends AbstractAction{  

     public RandomizeAndRunAction () { super ("Active Modules: Score Distribution"); }
     
     public void actionPerformed (ActionEvent e) {
       startRandomizeAndRun(Cytoscape.getCurrentNetwork());
     }
   }

   /**
    * This is used to update the expression attributes in the params object so that
    * they match those that exist in CyAttributes.
    */
   protected class AttrChangeListener implements PropertyChangeListener, 
                                                 MultiHashMapDefinitionListener {

   	public void propertyChange(PropertyChangeEvent e) {
		if ( e.getPropertyName().equals(Cytoscape.ATTRIBUTES_CHANGED) ) 
			apfParams.reloadExpressionAttributes();     
	}

	/**
	 * There is no point in listening to attributeDefined events because
	 * this only defines the attr and when this is fired, no attr values
	 * actually exist.
	 */
	public void attributeDefined(String attributeName) { }

	public void attributeUndefined(String attributeName) {
		apfParams.reloadExpressionAttributes();     
	}
   }
}
