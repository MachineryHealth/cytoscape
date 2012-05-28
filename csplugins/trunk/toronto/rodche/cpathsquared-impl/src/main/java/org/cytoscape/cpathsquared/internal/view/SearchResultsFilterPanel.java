package org.cytoscape.cpathsquared.internal.view;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.cytoscape.cpathsquared.internal.filters.ChainedFilter;
import org.cytoscape.cpathsquared.internal.filters.DataSourceFilter;
import org.cytoscape.cpathsquared.internal.filters.EntityTypeFilter;
import org.cytoscape.cpathsquared.internal.filters.OrganismFilter;

import cpath.service.jaxb.SearchHit;


public class SearchResultsFilterPanel extends JPanel implements Observer {
    private final JLabel matchingItemsLabel;
    private final ResultsModel model;
    private final JList hitsJList;
    private final CheckNode rootNode;
    private final CheckNode typeFilterNode;
    private final CheckNode dataSourceFilterNode;
    private final CheckNode organismFilterNode;
    private final JTreeWithCheckNodes tree;
    private final CollapsablePanel filterTreePanel;
    private JButton downlodButton;
	
	public SearchResultsFilterPanel(ResultsModel resultsModel, JList hitsJList) {
        this.model = resultsModel;
        this.hitsJList = hitsJList;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        matchingItemsLabel = new JLabel("Matching entities:  N/A");
        matchingItemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        Font font = matchingItemsLabel.getFont();
        Font newFont = new Font(font.getFamily(), Font.BOLD, font.getSize());
        matchingItemsLabel.setFont(newFont);
        matchingItemsLabel.setBorder(new EmptyBorder(5, 10, 5, 5));
        add(matchingItemsLabel);

        // create an empty filter tree (barebone)
        rootNode = new CheckNode("All Filters");
        typeFilterNode = new CheckNode("BioPAX Type");
        rootNode.add(typeFilterNode);
        organismFilterNode = new CheckNode("and Organism");
        rootNode.add(organismFilterNode);
        dataSourceFilterNode = new CheckNode("and Datasource");
        rootNode.add(dataSourceFilterNode);
        tree = new JTreeWithCheckNodes(rootNode);
        tree.setOpaque(false);
        filterTreePanel = new CollapsablePanel("BioPAX Filters");
        filterTreePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterTreePanel.getContentPane().add(tree);

        JScrollPane scrollPane = new JScrollPane(filterTreePanel);
        add(scrollPane);
        
        model.addObserver(this);
        
//        createDownloadButton();
    }

    /**
     * Expands all Nodes.
     */
    public void expandAllNodes() {
    	TreePath path;
    	filterTreePanel.setCollapsed(false);
        
       	typeFilterNode.setSelected(true);
       	path = new TreePath(typeFilterNode.getPath());
       	tree.expandPath(path);
        
       	dataSourceFilterNode.setSelected(true);
       	path = new TreePath(dataSourceFilterNode.getPath());
       	tree.expandPath(path);
        
       	organismFilterNode.setSelected(true);
       	path = new TreePath(organismFilterNode.getPath());
       	tree.expandPath(path);
    }

    
    private List<SearchHit> executeFilter() {
        ChainedFilter chainedFilter = new ChainedFilter();
        
		Set<String> entityTypeSet = new HashSet<String>();
		for (int i = 0; i < typeFilterNode.getChildCount(); i++) {
			CheckNode checkNode = (CheckNode) typeFilterNode.getChildAt(i);
			CategoryCount categoryCount = (CategoryCount) checkNode.getUserObject();
			String entityType = categoryCount.getCategoryName();
			if (checkNode.isSelected()) {
				entityTypeSet.add(entityType);
			}
		}
		EntityTypeFilter entityTypeFilter = new EntityTypeFilter(entityTypeSet);
		chainedFilter.addFilter(entityTypeFilter);
		
		Set<String> entityOrganismSet = new HashSet<String>();
		for (int i = 0; i < organismFilterNode.getChildCount(); i++) {
			CheckNode checkNode = (CheckNode) organismFilterNode.getChildAt(i);
			CategoryCount categoryCount = (CategoryCount) checkNode.getUserObject();
			String entityType = categoryCount.getCategoryName();
			if (checkNode.isSelected()) {
				entityOrganismSet.add(entityType);
			}
		}
		OrganismFilter organismFilter = new OrganismFilter(entityOrganismSet);
		chainedFilter.addFilter(organismFilter);
		
		Set<String> entityDataSourceSet = new HashSet<String>();
		for (int i = 0; i < dataSourceFilterNode.getChildCount(); i++) {
			CheckNode checkNode = (CheckNode) dataSourceFilterNode.getChildAt(i);
			CategoryCount categoryCount = (CategoryCount) checkNode.getUserObject();
			String entityType = categoryCount.getCategoryName();
			if (checkNode.isSelected()) {
				entityDataSourceSet.add(entityType);
			}
		}
		DataSourceFilter dataSourceFilter = new DataSourceFilter(entityDataSourceSet);
		chainedFilter.addFilter(dataSourceFilter);
		
        return chainedFilter.filter(model.getSearchResponse().getSearchHit());
    }

    
	private void applyFilter() {
        List<SearchHit> passedRecordList = executeFilter();
        matchingItemsLabel.setText("Matching entities:  "
        		+ passedRecordList.size());
		
   		DefaultListModel listModel = (DefaultListModel) hitsJList.getModel();
   		listModel.clear();
   		listModel.setSize(passedRecordList.size());
   		int i = 0;
   		for (SearchHit searchHit : passedRecordList) {
   			listModel.setElementAt(searchHit, i++);
   		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Observable o, Object arg) {
        matchingItemsLabel.setText("Matching entities:  "
                + model.getNumRecords());

        if (model.getNumRecords() == 0) {
            filterTreePanel.setVisible(false);
        } else {
            filterTreePanel.setVisible(true);
        }
        
        //  Remove all children
        typeFilterNode.removeAllChildren();
        // Create Filter Nodes
        for (String key : model.numHitsByTypeMap.keySet()) {
            CategoryCount categoryCount = new CategoryCount(key, model.numHitsByTypeMap.get(key));
            CheckNode typeNode = new CheckNode(categoryCount, false, true);
            typeFilterNode.add(typeNode);
        }
        
        organismFilterNode.removeAllChildren();
        for (String key : model.numHitsByOrganismMap.keySet()) {
            CategoryCount categoryCount = new CategoryCount(key, model.numHitsByOrganismMap.get(key));
            CheckNode organismNode = new CheckNode(categoryCount, false, true);
            organismFilterNode.add(organismNode);
        }
        
        dataSourceFilterNode.removeAllChildren();
        for (String key : model.numHitsByDatasourceMap.keySet()) {
            CategoryCount categoryCount = new CategoryCount(key, model.numHitsByDatasourceMap.get(key));
            CheckNode dataSourceNode = new CheckNode(categoryCount, false, true);
            dataSourceFilterNode.add(dataSourceNode);
        }
            
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        tree.setModel(treeModel);
        treeModel.addTreeModelListener(new TreeModelListener() {
            /**
             * Respond to user check node selections.
             *
             * @param treeModelEvent Tree Model Event Object.
             */
            public void treeNodesChanged(TreeModelEvent treeModelEvent) {
            	applyFilter();
            	filterTreePanel.repaint();
            }

            public void treeNodesInserted(TreeModelEvent treeModelEvent) {
                //  no-op
            }

            public void treeNodesRemoved(TreeModelEvent treeModelEvent) {
                //  no-op
            }

            public void treeStructureChanged(TreeModelEvent treeModelEvent) {
                //  no-op
            }
        });
        
        expandAllNodes();
	}
	
	
    
    //TODO 
    private final void createDownloadButton() {
        downlodButton = new JButton("Download");
        downlodButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {               	
//                    DownloadDetails detailsFrame = factory.createDownloadDetails(passedRecordList);
//                    if (dialog != null) {
//                            SwingUtilities.invokeLater(new Runnable() {
//                                public void run() {
//                                    dialog.dispose();
//                                }
//                            });
//                    }
//                    detailsFrame.setVisible(true);
            }
        });
        
        add(downlodButton);
    }
}
