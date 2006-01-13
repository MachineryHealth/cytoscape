package org.isb.bionet.gui.wizard;

import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import org.apache.xalan.lib.sql.SQLErrorDocument;
import org.isb.bionet.datasource.interactions.*;
import org.isb.bionet.datasource.synonyms.*;
import org.isb.iavila.ontology.xmlrpc.*;
import org.isb.bionet.gui.KeggGui;
import org.isb.bionet.gui.ProlinksGui;
import org.isb.bionet.CyNetUtils;
import cytoscape.*;

/**
 * 
 * @author iavila
 * TODO: "Estimate" buttons to calculate num nodes and edges
 */
public class NetworkBuilderWizard {
    
    protected static final int X_SIZE = 400;
    protected static final int Y_SIZE = 425;
    
    // Clients
    protected SynonymsClient synonymsClient;
    protected InteractionDataClient interactionsClient;
    protected GOClient goClient;
    
    // Panels and dialogs
    protected List dialogs;
    protected SpeciesPanel speciesPanel;
    protected NodeSourcesPanel nodeSourcesPanel;
    protected EdgeSourcesPanel edgeSourcesPanel;
    protected NetworkSettingsPanel networkPanel;
    protected LabelsPanel labelsPanel;
    protected AttributesPanel attsPanel;
    
    // Bookkeeping
    protected int currentStep;
    protected boolean onLastStep = false;
    
    // Actions
    protected AbstractAction DEFAULT_BACK_ACTION = new AbstractAction(){
            
            public void actionPerformed (ActionEvent e){
                displayStep(currentStep-1);
            }
    };
    
    protected AbstractAction DEFAULT_NEXT_ACTION = new AbstractAction(){
                
                public void actionPerformed (ActionEvent e){
                    displayStep(currentStep+1);
                }
        
    };
    
    protected AbstractAction FINISH_ACTION;
    
    /**
     * 
     * @param 
     */
    public NetworkBuilderWizard (SynonymsClient synonyms_client,
                InteractionDataClient interactions_client, GOClient go_client){
        this.synonymsClient = synonyms_client;
        this.interactionsClient = interactions_client;
        this.goClient = go_client;
        FINISH_ACTION = new AbstractAction (){
            public void actionPerformed (ActionEvent event){
                createNetwork();
                JDialog currentDialog = (JDialog)dialogs.get(currentStep);
                currentDialog.setVisible(false);
            }
            
        };
        createDialogs();
    }//constructor
    
    /**
     * Starts the wizard.
     */
    public void startWizard (){
        this.onLastStep = false;
        displayStep(0);
    }//startWizard
    
    /**
     * Displays the dialog at position step in this.dialogs
     * @param step
     */
    protected void displayStep (int step){      
        JDialog prevDialog = (JDialog)this.dialogs.get(this.currentStep);
        this.currentStep = step;
        if(this.currentStep == this.dialogs.size()-1){
            this.onLastStep = true;
        }else{
            this.onLastStep = false;
        }
        JDialog dialog = (JDialog)this.dialogs.get(this.currentStep);
        dialog.setLocationRelativeTo(prevDialog);
        if(prevDialog.isVisible()){
            prevDialog.setVisible(false);
        }
        dialog.setVisible(true);
    }//dsiplayStep
    
    /**
     * Creates all the dialogs in order of steps
     */
    public void createDialogs (){
        this.dialogs = new ArrayList();
        
        this.currentStep = -1;
        
        // Create the dialog for selecting species
        this.currentStep++;
        JDialog speciesDialog = createSpeciesDialog();
        this.dialogs.add(this.currentStep, speciesDialog);
        
        // Create the dialog to select nodes
        this.currentStep++;
        JDialog nodesDialog = createNodeSourcesDialog();
        this.dialogs.add(this.currentStep, nodesDialog);
        
        // Create the dialog to select edges
        this.currentStep++;
        JDialog edgesDialog = createEdgeSourcesDialog();
        this.dialogs.add(this.currentStep, edgesDialog);
        
        // Create the dialog to prioritize node labels
        this.currentStep++;
        JDialog labelsDialog = createNodeLabelsDialog();
        this.dialogs.add(this.currentStep, labelsDialog);
        
        // Create the dialog for attributes
        this.currentStep++;
        JDialog attsDialog = createAttsDialog();
        this.dialogs.add(this.currentStep,attsDialog);
        
        // Create the dialog for network settings
        this.currentStep++;
        this.onLastStep = true;
        JDialog netDialog = createNetworkSettingsDialog();
        this.dialogs.add(this.currentStep,netDialog);
    }//start
    
    
    /**
     * Creates a dialog with a BorderLayout, the SOUTH portion of the dialog contains wizard buttons (back, next, cancel)
     * @return a JDialog
     */
    protected JDialog createWizardDialog (AbstractAction backAction, AbstractAction nextAction){
        
        JDialog dialog = new JDialog(Cytoscape.getDesktop());
        dialog.setTitle("BioNetwork Builder");
        dialog.setSize(X_SIZE, Y_SIZE);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        JPanel buttons = createWizardButtons(backAction, nextAction);
        
        panel.add(buttons, BorderLayout.SOUTH);
        
        dialog.setContentPane(panel);
        
        return dialog;
        
    }//createWizardDialog
    
    /**
     * Creates Back, Next, and Cancel buttons
     * @return
     */
    protected JPanel createWizardButtons (AbstractAction backAction, AbstractAction nextAction){
        
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        
        JButton back = new JButton("< Back");
        if(backAction != null){
            back.addActionListener(backAction);
        }else{
            back.setEnabled(false);
        }
 
        JButton next = new JButton("Next >");
        next.addActionListener(nextAction);
        
        if(this.onLastStep){
            next.setText("Finish");
        }
        
        
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new AbstractAction(){
            public void actionPerformed (ActionEvent event){
                JDialog dialog = (JDialog)dialogs.get(currentStep);
                dialog.dispose();
            }
        });
        
        panel.add(back);
        panel.add(next);
        panel.add(cancel);
        
        return panel;
    }//createWizardButtons
    
    
    protected JPanel createExplanationPanel (String explanation){
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(explanation);
        panel.add(label);
        return panel;
    }
    
    //TODO: Each data source should have a dialog that pops-up with the species
    //TODO: Explanation: must choose same species for each data source
    //TODO: In the Synonyms db, have a species table? This would solve the above two items
    protected JDialog createSpeciesDialog (){
        
        AbstractAction back, next;
        if(this.currentStep == 0){
            back = null;
        }else{
            back = DEFAULT_BACK_ACTION;
        }
        
        next = new AbstractAction (){
            public void actionPerformed (ActionEvent event){
                Map table = (Map)speciesPanel.getSourcesSelectedSpecies();
                if(table.size() == 0){
                    JOptionPane.showMessageDialog(speciesPanel,"Please select a species", "Error", JOptionPane.ERROR_MESSAGE);
                }else{
                    // enable edge sources in the edges dialog
                    if(edgeSourcesPanel == null)
                        return;
                    Map sourceToName = (Map)speciesPanel.getSourcesNames();
                    Map sourcesToSpecies = speciesPanel.getSourcesSelectedSpecies();
                    edgeSourcesPanel.setSourcesToSpecies(sourcesToSpecies);
                    Iterator it = table.keySet().iterator();
                    while(it.hasNext()){
                        String name = (String)sourceToName.get(it.next());
                        edgeSourcesPanel.setSourceButtonEnabled(name, true);
                    }//while it
                    
                    // TODO: Disable the ones that are not used!
                    
                    
                    if(onLastStep){
                        FINISH_ACTION.actionPerformed(event);
                    }else{
                        DEFAULT_NEXT_ACTION.actionPerformed(event);
                    }
                  }//else
            }//actionPerformed
        };
        
        JDialog dialog = createWizardDialog(back, next);
        
        JPanel explanation = 
            createExplanationPanel("<html><br>Select a species for your biological network from your<br>desired data sources.<br></html>");
        dialog.getContentPane().add(explanation, BorderLayout.NORTH);
        
        try{
            Hashtable sourceToSp = this.interactionsClient.getSupportedSpeciesForEachSource();
            Hashtable  sourceToName = this.interactionsClient.getSourcesNames();
            this.speciesPanel = new SpeciesPanel(sourceToSp, sourceToName);
        }catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this.speciesPanel,
                    "<html>There was an error while attempting to obtain supported species!<br>"+ e.getMessage() +"<br></html>", 
                    "Error",JOptionPane.ERROR_MESSAGE);
            Hashtable emptyTable = new Hashtable();
            this.speciesPanel = new SpeciesPanel(emptyTable, emptyTable);
        }
       
        dialog.getContentPane().add(this.speciesPanel, BorderLayout.CENTER);
        
        return dialog;
        
    }//createSpeciesDialog
    
    
    protected JDialog createNodeSourcesDialog (){
        
        AbstractAction back, next;
        if(this.currentStep == 0){
            back = null;
        }else{
            back = DEFAULT_BACK_ACTION;
        }
        
        
        next = new AbstractAction (){
            
            public void actionPerformed (ActionEvent event){
                Vector nodes = nodeSourcesPanel.getAllNodes();
               
                if(edgeSourcesPanel != null){
                    edgeSourcesPanel.setNodes(nodes);
                }// edgeSourcesPanel != null
                
                if(onLastStep){
                    FINISH_ACTION.actionPerformed(event);
                }else{
                    DEFAULT_NEXT_ACTION.actionPerformed(event);
                }
                
            }//actionPerformed
            
        };
        
        JDialog dialog = createWizardDialog(back, next);
        
        JPanel explanation = 
            createExplanationPanel("<html><br>Select the sources for the nodes in your biological network.<br>"+
                    "Advanced settings for some sources are available if you"+
                    "<br>press the source's corresponding button.<br><br>"+
                    "If you don't select any node sources, then nodes will be"+
                    "<br>created automatically when edges are created (next step).<br>" +
                    "</html>"); 
        
        dialog.getContentPane().add(explanation,BorderLayout.NORTH);
        
        this.nodeSourcesPanel = new NodeSourcesPanel(this.goClient, this.synonymsClient);
        
        JPanel bigPanel = new JPanel();
        bigPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bigPanel.add(this.nodeSourcesPanel);
       
        dialog.getContentPane().add(bigPanel, BorderLayout.CENTER);
        
        return dialog;
        
    }
    
    protected JDialog createEdgeSourcesDialog (){
        
        AbstractAction back, next;
        if(this.currentStep == 0){
            back = null;
        }else{
            back = DEFAULT_BACK_ACTION;
        }
        
        // Data sources contain default parameters, so even if the user does not
        // change anything here, we are OK
        if(this.onLastStep){
            next =  FINISH_ACTION;
        }else{
            next = DEFAULT_NEXT_ACTION; 
                
        }
        
        JDialog dialog = createWizardDialog(back, next);
        
        JPanel explanation = createExplanationPanel(
                "<html><br>The edge sources that you selected when specifying species<br>"+
                "are available here.<br>"+
                "You can set their parameters by pressing on their<br>corresponding buttons.<br></htlm>"
        ); 
        
        dialog.getContentPane().add(explanation,BorderLayout.NORTH);
        
        Map sourcesToSpecies;
        
        if(this.speciesPanel != null){
            sourcesToSpecies = this.speciesPanel.getSourcesSelectedSpecies();
        }else{
           sourcesToSpecies = new Hashtable();
        }
        
        Vector nodes = new Vector();
        if(this.nodeSourcesPanel != null){
            nodes = this.nodeSourcesPanel.getAllNodes();  
        }
        
        this.edgeSourcesPanel = new EdgeSourcesPanel(this.interactionsClient, this.synonymsClient, sourcesToSpecies, nodes);
        
        JPanel bigPanel = new JPanel();
        bigPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bigPanel.add(this.edgeSourcesPanel);
       
        dialog.getContentPane().add(bigPanel, BorderLayout.CENTER);
        
        return dialog;
        
    }
    
    protected JDialog createNodeLabelsDialog (){
        
        AbstractAction back, next;
        
        if(this.currentStep == 0){
            back = null;
        }else{
            back = DEFAULT_BACK_ACTION;
        }

        next = new AbstractAction (){
            public void actionPerformed (ActionEvent event){
                    if(onLastStep){
                        FINISH_ACTION.actionPerformed(event);
                    }else{
                        DEFAULT_NEXT_ACTION.actionPerformed(event);
                    }
            }//actionPerformed
        };//AbstractAction
        
        JDialog dialog = createWizardDialog(back, next);
        
        JPanel explanation = createExplanationPanel(
                "<html><br>Prioritize the ID types for node labels.<br>Nodes will be labeled with the highest priority ID type available<br>in the list below.</html>"
        );
        
        dialog.getContentPane().add(explanation, BorderLayout.NORTH);
        
        this.labelsPanel = new LabelsPanel();
        
        JPanel bigPanel = new JPanel();
        bigPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bigPanel.add(this.labelsPanel);
       
        dialog.getContentPane().add(bigPanel, BorderLayout.CENTER);
        
        return dialog;
    }
    
    protected JDialog createAttsDialog (){
        
        AbstractAction back, next;
        
        if(this.currentStep == 0){
            back = null;
        }else{
            back = DEFAULT_BACK_ACTION;
        }

        next = new AbstractAction (){
            public void actionPerformed (ActionEvent event){
                    if(onLastStep){
                        FINISH_ACTION.actionPerformed(event);
                    }else{
                        DEFAULT_NEXT_ACTION.actionPerformed(event);
                    }
            }//actionPerformed
        };//AbstractAction
        
        JDialog dialog = createWizardDialog(back, next);
        
        JPanel explanation = createExplanationPanel(
                "<html><br>Select the attribute options for your network.<br></html>"
        );
        
        
        dialog.getContentPane().add(explanation, BorderLayout.NORTH);
        
        this.attsPanel = new AttributesPanel();
        
        JPanel bigPanel = new JPanel();
        bigPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bigPanel.add(this.attsPanel);
       
        dialog.getContentPane().add(bigPanel, BorderLayout.CENTER);
        
        return dialog;
    }
    
    protected JDialog createNetworkSettingsDialog (){
        
        AbstractAction back, next;
        
        if(this.currentStep == 0){
            back = null;
        }else{
            back = DEFAULT_BACK_ACTION;
        }

        next = new AbstractAction (){
            public void actionPerformed (ActionEvent event){
                String name = networkPanel.getNetworkName();
                if(name == null || name.length() == 0){
                    JOptionPane.showMessageDialog(networkPanel,"Please enter a name for your network.", "Error", JOptionPane.ERROR_MESSAGE);
                }else{
                    if(onLastStep){
                        FINISH_ACTION.actionPerformed(event);
                    }else{
                        DEFAULT_NEXT_ACTION.actionPerformed(event);
                    }
                 }//else
            }//actionPerformed
        };//AbstractAction
        
        JDialog dialog = createWizardDialog(back, next);
        
        JPanel explanation = createExplanationPanel(
                "<html><br>Set parameters for your biological network.<br>"+
                          "If you enter the name of an existing network, the new <br>interactions will be added to it.<br></html>"
        );
        
        dialog.getContentPane().add(explanation, BorderLayout.NORTH);
        
        this.networkPanel = new NetworkSettingsPanel();
        
        JPanel bigPanel = new JPanel();
        bigPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bigPanel.add(this.networkPanel);
       
        dialog.getContentPane().add(bigPanel, BorderLayout.CENTER);
        
        return dialog;
    }
    
    
    // TODO: Organize this better, this is quick and dirty, maybe a new class???
    protected void createNetwork (){
        // 1. Get the species for each edge data source
        Map sourceToSpecies = this.speciesPanel.getSourcesSelectedSpecies();
        Map sourceToNames = this.speciesPanel.getSourcesNames();
        
        // 2. Get the starting nodes for the network (if any)
        Vector startingNodes = this.nodeSourcesPanel.getAllNodes();
        
        // 3. Get the edge data source parameter settings
        Map sourceToSettings = this.edgeSourcesPanel.getSourcesDialogs();
        Iterator it = sourceToSettings.keySet().iterator();
        // count the number of selected sources
        int selected = 0;
        while(it.hasNext()){  
            String sourceClass = (String)it.next();
            if(this.edgeSourcesPanel.isSourceSelected(sourceClass)) selected++;
        }
        boolean firstNeighbors = this.edgeSourcesPanel.isFirstNeighborsSelected();
        
        // 4. Get the network name
        String netName = this.networkPanel.getNetworkName();
        
        // 5. See which attributes we should add
        String [] labelOps = this.labelsPanel.getOrderedLabelOptions();
        Hashtable atts = this.attsPanel.getSelectedAttributesTable();
        
        // 6. Iterate over all the edge data sources and accumulate interactions
        
        //TODO: Fix this:
        // Each data source (KEGG, Prolinks, etc) will contribute a set of nodes connected by interactions
        // the following code does not find edges between nodes that come from different sources
        
        HashSet interactions = new HashSet();
        HashSet nodeIDs = null;
        if(selected > 1 && startingNodes.size() > 0)
            nodeIDs = new HashSet();
        
        HashMap sourceNameToArgs = new HashMap();
        HashMap sourceNameToSpecies = new HashMap();
        it = sourceToSettings.keySet().iterator();
        while(it.hasNext()){
            
            String sourceClass = (String)it.next();
            if(!this.edgeSourcesPanel.isSourceSelected(sourceClass)) continue;
          
            List sourceSpecies = (List)sourceToSpecies.get(sourceClass);
            if(sourceSpecies == null || sourceSpecies.size() == 0) continue;
            
            String species = (String)sourceSpecies.get(0);
            String sourceName = (String)sourceToNames.get(sourceClass);
            sourceNameToSpecies.put(sourceName,species);
            
            Hashtable args = new Hashtable();
            if(sourceName.equals(ProlinksInteractionsSource.NAME)){
                
                ProlinksGui prolinksGui = (ProlinksGui)sourceToSettings.get(sourceClass);
                Vector interactionTypes = prolinksGui.getSelectedInteractionTypes();
                double pvalTh = prolinksGui.getPval(false);
                System.out.println("------- Prolinks settings (createNetwork)----------");
                System.out.println("interactionTypes = " + interactionTypes);
                System.out.println("pval = " + pvalTh);
                System.out.println("species = " + sourceSpecies);
                System.out.println("---------------------------------------------------");
                
                if(pvalTh != 1){
                    args.put(ProlinksInteractionsSource.PVAL, new Double(pvalTh));
                }
                
                if(interactionTypes.size() < 4){
                    args.put(ProlinksInteractionsSource.INTERACTION_TYPE, interactionTypes);
                }
              
            
            }else if(sourceName.endsWith(KeggInteractionsSource.NAME)){
            
                KeggGui kDialog = (KeggGui)sourceToSettings.get(sourceClass);
                int threshold = kDialog.getThreshold();
                boolean oneEdge = kDialog.createOneEdgePerCompound();
                args = new Hashtable();
                args.put(KeggInteractionsSource.THRESHOLD_KEY,new Integer(threshold));
                args.put(KeggInteractionsSource.EDGE_PER_CPD_KEY, new Boolean(oneEdge));
                System.out.println("------- KEGG settings (estimateNumEdges)----------");
                System.out.println("threshold = " + threshold);
                System.out.println("oneEdgePerCpd = " + oneEdge);
                System.out.println("species = " + sourceSpecies);
                System.out.println("---------------------------------------------------");
                
            }
              
            sourceNameToArgs.put(sourceName,args);
            
            Vector sourceInteractions = null;
            try{
                if(startingNodes == null || startingNodes.size() == 0){
                    
                    if(args.size() > 0){
                        sourceInteractions = (Vector)this.interactionsClient.getAllInteractions(species, args);
                    }else{
                        sourceInteractions = (Vector)this.interactionsClient.getAllInteractions(species);
                    }
                
                }else{

                    Vector adjacentNodes = null;
                    
                    if(firstNeighbors){ 
                            if(args.size() > 0)
                                adjacentNodes = this.interactionsClient.getFirstNeighbors(startingNodes,species,args);
                            else
                                adjacentNodes = this.interactionsClient.getFirstNeighbors(startingNodes,species);
                    }
                   
                    // If firstNeighbors is selected, and we have startingNodes, then we want to find the edges connecting
                    // the nodes in first neighbors and starting nodes: CAVEAT: The nodes in startingNodes could be a subset of
                    // selected nodes in a network. Connecting edges would only be found for these selected nodes, not for the
                    // whole network.
                    
                    Vector nodesToConnect = startingNodes;
                    if(adjacentNodes != null){
                        // make sure we don't have repeated nodes in nodesToConnect
                        adjacentNodes.removeAll(startingNodes);
                        nodesToConnect.addAll(adjacentNodes);
                    }
                    if(args.size() > 0){
                        sourceInteractions = (Vector)this.interactionsClient.getConnectingInteractions(nodesToConnect, species, args);
                    }else{
                        sourceInteractions = (Vector)this.interactionsClient.getConnectingInteractions(nodesToConnect, species);
                    }
                }//else
                
               
                if(nodeIDs != null){
                    // Accumulate the new nodeIDs:
                    Iterator it2 = sourceInteractions.iterator();
                    while(it2.hasNext()){
                        Hashtable interaction = (Hashtable)it2.next();
                        String id1 = (String)interaction.get(InteractionsDataSource.INTERACTOR_1);
                        String id2 = (String)interaction.get(InteractionsDataSource.INTERACTOR_2);
                        nodeIDs.add(id1);
                        nodeIDs.add(id2);
                    }//while it
                }else{
                    interactions.addAll(sourceInteractions);
                }
                
            }catch (Exception ex){
                ex.printStackTrace();
            }
            
        }//while it
        
        if(nodeIDs != null){
            // Finally, connect nodes from different data sources
            nodeIDs.addAll(startingNodes);
            it = sourceNameToArgs.keySet().iterator();
            while(it.hasNext()){
                String sourceName = (String)it.next();
                Hashtable args= (Hashtable)sourceNameToArgs.get(sourceName);
                String species = (String)sourceNameToSpecies.get(sourceName);
                Vector sourceInteractions = null;
                try{
                    if(args.size() > 0){
                        sourceInteractions = (Vector)this.interactionsClient.getConnectingInteractions(new Vector(nodeIDs), species, args);
                    }else{
                        sourceInteractions = (Vector)this.interactionsClient.getConnectingInteractions(new Vector(nodeIDs), species);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                interactions.addAll(sourceInteractions);
            }//while it
        }
        
        
        // 6. Make a network, or add interactions if the network already exists
   
        Set nets = Cytoscape.getNetworkSet();
        it = nets.iterator();
        CyNetwork net = null;
        boolean found = false;
        while(it.hasNext()){
            net = (CyNetwork)it.next();
            if(net.getTitle().equals(netName)){
                found = true;
                break;
            }
        }
        
        if(found){
            CyNetUtils.addInteractionsToNetwork(net,startingNodes,interactions, this.synonymsClient,labelOps,atts);
        }else{
            net = CyNetUtils.makeNewNetwork(startingNodes,interactions, netName, this.synonymsClient,labelOps,atts);
        }
        
        
    }//createNetwork
    
    /**
     * 
     * @param id an ID
     * @return one of:<br>
     * PROLINKS_ID, KEGG_ID, GI_ID, or ID_NOT_FOUND
     */
    public String getIdType (String id){
        String [] tokens = id.split(":");
        if(tokens.length == 0) return SynonymsSource.ID_NOT_FOUND;
        if(tokens[0].equals(SynonymsSource.PROLINKS_ID)) return SynonymsSource.PROLINKS_ID;
        if(tokens[0].equals(SynonymsSource.KEGG_ID)) return SynonymsSource.KEGG_ID;
        if(tokens[0].equals(SynonymsSource.GI_ID)) return SynonymsSource.GI_ID;
        return SynonymsSource.ID_NOT_FOUND;
    }
    
}//NetworkBuilderWizard