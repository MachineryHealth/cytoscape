package filter.cytoscape;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import filter.model.*;
import javax.swing.border.*;
import java.beans.*;
import javax.swing.event.SwingPropertyChangeSupport;

import cytoscape.*;
import cytoscape.data.*;

import cytoscape.CyNetwork;
import giny.model.GraphPerspective;

import filter.view.*;
import filter.model.*;

import ViolinStrings.Strings;

/**
 * This is a Cytoscape specific filter that will pass nodes if
 * a selected attribute matches a specific value.
 */

public class NodeTopologyFilterEditor 
  extends FilterEditor 
  implements ActionListener,FocusListener,ItemListener {

  /**
   * This is the Name that will go in the Tab 
   * and is returned by the "toString" method
   */
  protected String identifier;
  protected NodeTopologyFilter filter;	
  

  protected JTextField nameField;

  protected JComboBox filterBox;
  protected JTextField distanceField;
  protected JTextField countField;
  
  protected String DEFAULT_FILTER_NAME = "NodeTopology: ";
  protected Integer DEFAULT_DISTANCE = new Integer(1);
  protected Integer DEFAULT_COUNT = new Integer(1);
  protected int DEFAULT_FILTER = -1; 
  
  protected Class NODE_CLASS;
  protected Class EDGE_CLASS;
  protected Class NUMBER_CLASS;
  protected Class DEFAULT_CLASS; 
  protected Class filterClass;

  public NodeTopologyFilterEditor () {
    super();
    try{
      filterClass = Class.forName("filter.cytoscape.NodeTopologyFilter");
    }catch(Exception e){
      e.printStackTrace();
    }
    identifier = "Topology Filter";
    setBorder( new TitledBorder( "Node Topology Filter") );
    setLayout(new BorderLayout());
    JPanel namePanel = new JPanel();
    nameField = new JTextField(15);
    nameField.setText(identifier);
    nameField.addActionListener(this);
    nameField.addFocusListener(this);
   
    namePanel.add( new JLabel( "Filter Name" ) );
    namePanel.add( nameField );
    add( namePanel,BorderLayout.NORTH );

    JPanel all_panel = new JPanel();
    all_panel.setLayout(new GridLayout(3,1));
	
    JPanel topPanel = new JPanel();
    topPanel.add(new JLabel("Select nodes with "));
		
    countField = new JTextField(10);
    countField.setEditable(true);
    countField.addActionListener(this);
    countField.addFocusListener(this);
    topPanel.add(countField);

    topPanel.add(new JLabel(" neighbors"));
		
    JPanel middlePanel = new JPanel();
    middlePanel.add(new JLabel("within distance "));

    distanceField = new JTextField(10);
    distanceField.setEditable(true);
    distanceField.addActionListener(this);
    distanceField.addFocusListener(this);
    middlePanel.add(distanceField);


    JPanel bottomPanel = new JPanel();
    bottomPanel.add(new JLabel("that pass the filter "));
    filterBox = new JComboBox();
    filterBox.addItemListener(this);
    filterBox.setModel(FilterManager.defaultManager().getComboBoxModel());
    filterBox.setEditable(false);
    bottomPanel.add(filterBox);

    all_panel.add(topPanel);
    all_panel.add(middlePanel);
    all_panel.add(bottomPanel);

    add(all_panel,BorderLayout.CENTER);


  }

  //----------------------------------------//
  // Implements Filter Editor
  //----------------------------------------//
  
  public Class getFilterClass(){
    return filterClass;
  }

  public String toString () {
    return identifier;
  }

  public String getFilterID () {
    return NodeTopologyFilter.FILTER_ID;
  }

  public String getDescription(){
    return NodeTopologyFilter.FILTER_DESCRIPTION;
  }

  public Filter createDefaultFilter(){
    return new NodeTopologyFilter(DEFAULT_COUNT, DEFAULT_DISTANCE, DEFAULT_FILTER, DEFAULT_FILTER_NAME);
  }

  /**
   * Accepts a Filter for editing
   * Note that this Filter must be a Filter that can be edited
   * by this Filter editor. 
   */
  public void editFilter ( Filter filter ) {
    if ( filter instanceof NodeTopologyFilter ) {
      // good, this Filter is of the right type
      this.filter = (NodeTopologyFilter)filter;
      setFilterName(this.filter.toString());
      setSelectedFilter(this.filter.getFilter());
      setDistance(this.filter.getDistance());
      setCount(this.filter.getCount());
    }
  }

  //----------------------------------------//
  // NodeTopologyFilter Methods
  //----------------------------------------//

  // There should be getter and setter methods for
  // every editable property that the Filter needs to
  // to find out from the Editor. In this case there is only one
  // the search string.

  // Filter Name ///////////////////////////////////////

  public String getFilterName () {
    return filter.toString();
  }

  public void setFilterName ( String name ) {
    filter.setIdentifier(name);
    nameField.setText(name);
  }

  // Search String /////////////////////////////////////

  public int getSelectedFilter(){
    return filter.getFilter();
  }

  public void setSelectedFilter(int newFilter){
    if(filter != null){
      filter.setFilter(newFilter);
      filterBox.removeItemListener(this);
      filterBox.setSelectedItem(FilterManager.defaultManager().getFilter(newFilter));
      filterBox.addItemListener(this);
    }
  }


  public Integer getCount(){
    return filter.getCount();
  }

  public void setCount(Integer count){
    filter.setCount(count);
    countField.setText(count.toString());
  }

  public Integer getDistance(){
    return filter.getDistance();
  }

  public void setDistance(Integer distance){
    filter.setDistance(distance);
    distanceField.setText(distance.toString());
  }

  public void actionPerformed ( ActionEvent e ) {
    handleEvent(e);
  }

  public void focusGained(FocusEvent e){}

  public void focusLost(FocusEvent e){
    handleEvent(e);
  }

  public void itemStateChanged(ItemEvent e){
    handleEvent(e);
  }

  public void handleEvent(AWTEvent e){
    if ( e.getSource() == nameField ) {
      setFilterName(nameField.getText());
    } else if ( e.getSource() == filterBox ) {
      setSelectedFilter(FilterManager.defaultManager().getFilterID((Filter)filterBox.getSelectedItem()));
    } else if( e.getSource() == countField){
      Integer count = null;
      try{
	count = new Integer(countField.getText());
      }catch(NumberFormatException nfe){
	count = DEFAULT_COUNT;
      }
      setCount(count);
    } else if( e.getSource() == distanceField){
      Integer distance = null;
      try{
	distance = new Integer(distanceField.getText());
      }catch(NumberFormatException nfe){
	distance = DEFAULT_DISTANCE;
      }
      setDistance(distance);
    }
  }
}
