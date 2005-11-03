/**
 *
 */
package org.isb.bionet.gui.wizard;

import java.awt.event.*;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;
import org.isb.bionet.datasource.interactions.*;
import org.isb.bionet.datasource.synonyms.*;
import org.isb.bionet.gui.*;

import java.util.*;

import cytoscape.*;

/**
 * 
 * @author iavila
 * 
 */
public class EdgeSourcesPanel extends JPanel {

    /**
     * The fully specified edge source class to its human friendly name for
     * buttons
     */
    protected Map sourceToName;

    /**
     * If a data source has been selected, it's fully specified calss will be in
     * this Map with a Vector of species as a value
     */
    protected Map sourceToSpecies;

    /**
     * A Map from an edge data source's fully described class to the dialog that
     * contains its parameters
     */
    protected Map sourceToDialog;

    /**
     * The client to which to make requests
     */
    protected InteractionDataClient interactionsClient;

    /**
     * JButton to JTextField to display number of edges
     */
    protected Map buttonToTextField;

    /**
     * JButton to JCheckBox to select data sources
     */
    protected Map buttonToCheckBox;
    
    /**
     * A map from fully specified edge source class to its check box
     */
    protected Map sourceToCheckBox;

    /**
     * JButton to String describing fully specified class of data source
     */
    protected Map buttonToSourceClass;

    /**
     * Possibly null
     */
    protected Vector nodes;

    /**
     * Used to display Cytoscape loaded networks
     */
    protected CyNetworksDialog netsDialog;
    
    /**
     * The client for gene synonyms
     */
    protected SynonymsClient synonymsClient;
    
    /**
     * The first neighbors checkbox
     */
    protected JCheckBox fnCB;
    
    /**
     * 
     * @param interactions_client
     * @param sourceToSelectedSpecies
     *            Map from fully specified data source class to a Vector of its
     *            selected species (Vector of Strings)
     * @param nodes
     *            a Vector of Strings representing nodes, possibly null, used to
     *            estimate number of edges when starting nodes are set
     */
    public EdgeSourcesPanel(
            InteractionDataClient interactions_client,
            SynonymsClient synonyms_client,
            Map sourceToSelectedSpecies,
            Vector nodeIds) {
        
        this.interactionsClient = interactions_client;
        this.synonymsClient = synonyms_client;
        this.sourceToSpecies = sourceToSelectedSpecies;
        this.sourceToDialog = new Hashtable();
        this.buttonToTextField = new Hashtable();
        this.buttonToCheckBox = new Hashtable();
        this.sourceToCheckBox = new Hashtable();
        this.nodes = nodeIds;
        create();
    }
    
    /**
     * 
     * @param nodeIds Vector of Strings
     */
    public void setNodes (Vector nodeIds){
        this.nodes = nodeIds;
        if(this.nodes.size() > 0)
            this.fnCB.setEnabled(true);
    }
    
    
    /**
     * @return whether or not the user selected the first neighbors method
     * (this method is only available if there are starting nodes)
     */
    public boolean isFirstNeighborsSelected (){
        return this.fnCB.isSelected();
    }
    
    
    /**
     * 
     * @param sourceToSelectedSpecies a Map from fully specified data source's classes to Vectors of Strings representing species for the data sources
     */
    public void setSourcesToSpecies (Map sourceToSelectedSpecies){
        this.sourceToSpecies = sourceToSelectedSpecies;
    }

    /**
     * @return A Map from an edge data source's fully described class to the
     *         dialog that contains its parameters
     */
    public Map getSourcesDialogs() {
        return this.sourceToDialog;
    }
    
    /**
     * 
     * @param source_class the fully specified class of the source
     * @return 
     */
    public boolean isSourceSelected (String source_class){
        JCheckBox cb = (JCheckBox)this.sourceToCheckBox.get(source_class);
        if(cb != null) return cb.isSelected();
        return false;
    }

    /**
     * @param buttonName
     *            the human friendly name of the button to enable
     * @param enabled
     *            true or false
     */
    public void setSourceButtonEnabled(String buttonName, boolean enabled) {
        Iterator it = this.buttonToSourceClass.keySet().iterator();
        while (it.hasNext()) {
            JButton button = (JButton) it.next();
            String actualName = button.getText();
            if (actualName.startsWith(buttonName)) {
                button.setEnabled(enabled);
                JCheckBox checkBox = (JCheckBox) this.buttonToCheckBox
                        .get(button);
                checkBox.setSelected(true);
                return;
            }
        }// while it.hasNext
    }

    /**
     * 
     * @return the CyNetworks to be used as sources of edges
     */
    public CyNetwork[] getSelectedNetworks() {
        return this.netsDialog.getSelectedNetworks();
    }

    /**
     * @param button
     *            the button for the data source for which to estimate edges
     */
    protected void estimateNumEdges (JButton button) {
        
        String sourceClass = (String) this.buttonToSourceClass.get(button);
        if(sourceClass == null){
            return;
        }
        
        if(this.sourceToDialog == null || this.sourceToSpecies == null){
            System.err.println("ERROR: this.sourceToDialog = " + this.sourceToDialog + " this.sourceToSpecies = " 
                        + this.sourceToSpecies);
        }
        
        JDialog dialog = (JDialog) this.sourceToDialog.get(sourceClass);
        List species = (List)this.sourceToSpecies.get(sourceClass);
        
        Hashtable args = new Hashtable();
        if (sourceClass.equals(ProlinksInteractionsSource.class.toString())) {
            ProlinksGui pDialog = (ProlinksGui)dialog;
            Vector types = pDialog.getSelectedInteractionTypes();
            double pval = pDialog.getPval(false);
            args.put(ProlinksInteractionsSource.INTERACTION_TYPE, types);
            args.put(ProlinksInteractionsSource.PVAL, new Double(pval));
            System.out.println("------- Prolinks settings (estimateNumEdges)----------");
            System.out.println("interactionTypes = " + types);
            System.out.println("pval = " + pval);
            System.out.println("species = " + (String)species.get(0));
            System.out.println("------------------------------------------------------");
        }//Prolinks
        
        int numEdges = 0;
        
        try{
            // First neighbors
           
            // If first neighbors is selected, we should first get the first neighbors, and then calculate connecting edges between
            // nodes in this.nodes and their first neighbors.
            // To calculate num of edges, we need to get the actual 1st neighbors, and then getNumConnectingInteractions
            Vector firstNeighbors = null;
            if(this.fnCB.isSelected()){
                     if(args.size() == 0)
                        firstNeighbors = this.interactionsClient.getFirstNeighbors(this.nodes,(String)species.get(0));
                     else
                        firstNeighbors = this.interactionsClient.getFirstNeighbors(this.nodes,(String)species.get(0), args);
            }
            
            // Connecting edges
            
            Vector nodesToConnect = this.nodes;
            if(firstNeighbors != null && firstNeighbors.size() > 0){
                // make sure we don't have repeated nodes in nodedToConnect
                firstNeighbors.removeAll(this.nodes);
                nodesToConnect.addAll(firstNeighbors);
            }    
            if(nodesToConnect.size() > 0){
                
                // NOTE: This number will include already existing edges in the network, so it is not the number of new edges, just total edges
                if(args.size() > 0)
                    numEdges += this.interactionsClient.getNumConnectingInteractions(nodesToConnect,(String)species.get(0), args);
                else
                    numEdges += this.interactionsClient.getNumConnectingInteractions(nodesToConnect,(String)species.get(0));
                
            }else{
                    
                // No starting nodes
                if(args.size() > 0)
                    numEdges += this.interactionsClient.getNumAllInteractions((String)species.get(0), args);
                else
                    numEdges += this.interactionsClient.getNumAllInteractions((String)species.get(0)); 
            
           }
            
       }catch(Exception e){
            e.printStackTrace();
        }finally{
            JTextField tf = (JTextField)this.buttonToTextField.get(button);
            tf.setText(Integer.toString(numEdges));
        }
    } 

    protected void create() {

        // Create buttons and select them if the user has selected them as data
        // sources
        try {
            this.sourceToName = this.interactionsClient.getSourcesNames();
        } catch (Exception e) {
            e.printStackTrace();
            this.sourceToName = new Hashtable();
        }

        this.buttonToSourceClass = new HashMap();
        Iterator it = this.sourceToName.keySet().iterator();
        while (it.hasNext()) {
            final String sourceClass = (String) it.next();
            String buttonName = (String) this.sourceToName.get(sourceClass);
            boolean enabled = this.sourceToSpecies.containsKey(sourceClass);
            final JButton button = new JButton(buttonName + "...");
            final  ProlinksGui pDialog = new ProlinksGui();
            this.sourceToDialog.put(sourceClass, pDialog);
            if (buttonName.equals(ProlinksInteractionsSource.NAME)) {
                button.addActionListener(
                 new AbstractAction() {
                    public void actionPerformed(ActionEvent event) {
                        ProlinksGui pDialog = (ProlinksGui) sourceToDialog
                                .get(sourceClass);
                        pDialog.pack();
                        pDialog.setLocationRelativeTo(EdgeSourcesPanel.this);
                        pDialog.setVisible(true);
                        // Dialog is modal, so we get back when the user closes
                        // it:
                        //estimateNumEdges(button);
                    }// actionPerformed
                });// AbstractAction
            }
            button.setEnabled(enabled);
            this.buttonToSourceClass.put(button, sourceClass);
          
        }// while it

        JButton netsButton = new JButton("Loaded Networks...");
        netsButton.addActionListener(
                
                new AbstractAction() {

                public void actionPerformed(ActionEvent event) {
                    if (netsDialog == null) {
                        netsDialog = new CyNetworksDialog();
                    }
                    netsDialog.update();
                    netsDialog.setLocationRelativeTo(EdgeSourcesPanel.this);
                    netsDialog.pack();
                    netsDialog.setVisible(true);
                    // netsDialog is modal
                    CyNetwork[] nets = netsDialog.getSelectedNetworks();
                    int numEdges = 0;
                    for (int i = 0; i < nets.length; i++) {
                        numEdges += nets[i].getEdgeCount();
                    }// for i
                    JButton source = (JButton) event.getSource();
                    ((JTextField) buttonToTextField.get(source)).setText(Integer
                            .toString(numEdges));
                }// actionPerformed
            }// AbstractAction
                );
        
        netsButton.setEnabled(false);
        this.buttonToSourceClass.put(netsButton,null);

       
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
       
        GridBagLayout gridbag = new GridBagLayout();
        JPanel gridLayoutPanel = new JPanel();
        gridLayoutPanel.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 1.0;
        c.ipadx = 5;
        Component emptyBox = Box.createHorizontalGlue();
        gridbag.setConstraints(emptyBox, c);
        gridLayoutPanel.add(emptyBox);

        JLabel sourceLabel = new JLabel("Edge Source");
        gridbag.setConstraints(sourceLabel, c);
        gridLayoutPanel.add(sourceLabel);

        c.gridwidth = GridBagConstraints.REMAINDER; // end row

        JLabel stats = new JLabel("Num Edges");
        gridbag.setConstraints(stats, c);
        gridLayoutPanel.add(stats);

        c.fill = GridBagConstraints.HORIZONTAL;
        it = this.buttonToSourceClass.keySet().iterator();
        while (it.hasNext()) {
            
            final JButton button = (JButton) it.next();
            String sourceClass = (String)this.buttonToSourceClass.get(button);
            
            c.gridwidth = 1; // reset to the default
            JCheckBox cb = new JCheckBox();
            gridbag.setConstraints(cb, c);
            gridLayoutPanel.add(cb);
            if(sourceClass != null) this.sourceToCheckBox.put(sourceClass, cb);
            
            gridbag.setConstraints(button, c);
            gridLayoutPanel.add(button);

            cb.setSelected(button.isEnabled());

            cb.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    JCheckBox source = (JCheckBox) event.getSource();
                    button.setEnabled(source.isSelected());
                }
            });

            c.gridwidth = GridBagConstraints.REMAINDER;
            JTextField edgesNum = new JTextField(4);
            edgesNum.setText("0");

            this.buttonToTextField.put(button, edgesNum);
            this.buttonToCheckBox.put(button, cb);

            edgesNum.setEditable(false);
            gridbag.setConstraints(edgesNum, c);
            gridLayoutPanel.add(edgesNum);
        }// while it buttons
        
        JButton numEdgesButton = new JButton("Calculate number of edges from selected databases");
        numEdgesButton.addActionListener(
                new AbstractAction (){
                    
                    public void actionPerformed (ActionEvent event){
                        estimateNumEdges();
                    }
                    
                }
        );
        c.gridwidth = GridBagConstraints.REMAINDER;
        
        fnCB = new JCheckBox("Add first neighbors of nodes");
        fnCB.setSelected(false);
        if(this.nodes.size() == 0){
            fnCB.setEnabled(false);
        }
        gridbag.setConstraints(fnCB, c);
        gridLayoutPanel.add(fnCB);
        gridbag.setConstraints(numEdgesButton, c);
        gridLayoutPanel.add(numEdgesButton);
        
        // set layout for this panel and add the two main panels
        GridBagLayout gbl = new GridBagLayout();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.ipady = 25;
        setLayout(gbl);
        
        gbl.setConstraints(gridLayoutPanel,c);
        add(gridLayoutPanel);
        
    }// create
    
    /**
     * Estimates the number of edges per selected data source
     */
    public void estimateNumEdges (){
        Iterator it = this.buttonToCheckBox.keySet().iterator();
        while(it.hasNext()){
            JButton button = (JButton)it.next();
            if(button.isEnabled()){
                estimateNumEdges(button);
            }
        }//while it.hasNext
       
    }
}