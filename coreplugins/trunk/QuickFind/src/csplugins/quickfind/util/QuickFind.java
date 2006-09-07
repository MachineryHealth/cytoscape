package csplugins.quickfind.util;

import csplugins.widgets.autocomplete.index.GenericIndex;
import cytoscape.CyNetwork;
import cytoscape.task.TaskMonitor;

/**
 * Cytoscape Quick Find.
 * <p/>
 * The Cytoscape Quick Find class provides a convenient utility class for
 * quickly searching nodes or edges by any attribute.
 * <p/>
 * The following example illustrates the utility of this class.  To begin,
 * consider that network 1 is defined by the following SIF File:
 * <br/>
 * <pre>
 * YKR026C pp YGL122C
 * YGR218W pp YGL097W
 * YGL097W pp YOR204W
 * YLR249W pp YPR080W
 * YLR249W pp YBR118W
 * YLR293C pp YGL097W
 * </pre>
 * After this network is loaded, it can be automatically added to the quick
 * find index via the
 * {@link QuickFind#addNetwork(cytoscape.CyNetwork, cytoscape.task.TaskMonitor)}
 * method.  By default, this method iterates through each node in the network,
 * and indexes each node by its unique identifier, e.g. node.getIdentifier().
 * <p/>
 * A few minutes later, the end-user enters the String:  "YLR" in the Cytoscape
 * quick search box, and we want to quickly find all matching nodes that begin
 * with this prefix.
 * <p/>
 * To do so, we must first obtain the text index associated with this network
 * via the
 * {@link QuickFind#getIndex(cytoscape.CyNetwork)} method.  For example:
 * <BR/>
 * <PRE>
 * CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
 * TextIndex textIndex = QuickFind.getIndex (currentNetwork);
 * </PRE>
 * We can then retrieve all hits that begin with the prefix:  "YLR" via the
 * {@link csplugins.widgets.autocomplete.index.TextIndex#getHits(String, int)}
 * method.
 * <BR/>
 * <PRE>
 * Hit hits[] = textIndex.getHits ("YLR");
 * </PRE>
 * Technical Details:
 * <UL>
 * <LI>By default, this class will automatically index node objects based on the
 * their unique node identifier, e.g. node.getIdentifier().</LI>
 * <LI>You can index by a different attribute by calling the
 * {@link QuickFind#reindexNetwork(CyNetwork, String,
 * cytoscape.task.TaskMonitor)}.
 * <LI>You can specify any attribute name you like.  However, QuickFind
 * is not yet capable of indexing attributes of type CyAttributes.TYPE_COMPLEX.
 * <LI>QuickFind uses a {@link csplugins.widgets.autocomplete.index.Trie}
 * data structure for very fast look-ups.</LI>
 * </UL>
 * <p/>
 *
 * @author Ethan Cerami.
 */
public interface QuickFind {
    /**
     * Node / Edge Unique Identifier.
     */
    String UNIQUE_IDENTIFIER = "Unique Identifier";

    /**
     * Index all attributes.
     */
    String INDEX_ALL_ATTRIBUTES =
            "[ Index all attributes ]";

    /**
     * Network attribute, used to set default index.
     */
    String DEFAULT_INDEX = "quickfind.default_index";

    /**
     * Adds a new network to the global index, and indexes all nodes
     * described by this network.
     * <P>By default, this class will first determine if the network includes
     * a default index setting.  It does so by determining if the network
     * has a network attribute named:  quickfind.default_index.  For
     * example, if you would like your network to be indexed by
     * "biopax.short_name" by default, you would use the following code:
     *
     * <P>
     * <CODE>
     * CyAttributes networkAttributes = Cytoscape.getNetworkAttributes();
     * <BR>networkAttributes.setAttribute(cyNetwork.getIdentifier(),
     *     "quickfind.default_index", "biopax.short_name");
     * </CODE>
     *
     * <P>If no default index is found, this class will automatically
     * index node objects based on their unique identifier,
     * e.g. node.getIdentifier().
     *
     * @param network     Cytoscape Network.
     * @param taskMonitor TaskMonitor Object.
     */
    void addNetwork(CyNetwork network, TaskMonitor taskMonitor);

    /**
     * Removes the specified network from the global index.
     * <p/>
     * To free up memory, this method should be called whenever a network
     * is destroyed.
     *
     * @param network CyNetwork Object.
     */
    void removeNetwork(CyNetwork network);

    /**
     * Gets the index associated with the specified network.
     *
     * @param network Cytoscape Network.
     * @return Index Object.
     */
    GenericIndex getIndex(CyNetwork network);

    /**
     * Reindexes a network with the specified controlling attribute.
     * <p/>
     * This method will iterate through all noddes within the
     * registered network, and add each node to the text index.
     * For each node, the attribute specified will be used to create the
     * text index.
     * <p/>For example, if you want to index all nodes by their
     * "BIOPAX_NAME" attribute, you would use this code:
     * <br/>
     * <pre>reindexNetwork (cyNetwork, "BIOPAX_NAME", tm);</pre>
     *
     * @param cyNetwork            Cytoscape network.
     * @param controllingAttribute Attribute used to index all nodes.
     * @param taskMonitor          Task Monitor, used to monitor long-term
     *                             progress of task.
     */
    void reindexNetwork(CyNetwork cyNetwork, String controllingAttribute,
                        TaskMonitor taskMonitor);

    /**
     * Adds a new QuickFind Listener.
     *
     * @param listener QuickFindListener Object.
     */
    void addQuickFindListener(QuickFindListener listener);

    /**
     * Removes the specified QuickFind Listener Object.
     *
     * @param listener QuickFindListener Object.
     */
    void removeQuickFindListener(QuickFindListener listener);

    /**
     * Gets an array of all registered QuickFind Listener Objects.
     *
     * @return Array of QuickFindListener Objects.
     */
    QuickFindListener[] getQuickFindListeners();
}