package cytoscape.data.attr;

import java.util.Enumeration;

/**
 * This interface consists of the API specification to bind attribute
 * values to nodes.
 */
public interface CyNodeData
{

  /**
   * @param nodeKey the node to which to bind a new attribute value.
   * @param attributeName the attribute definition in which to assign an
   *   attribute value.
   * @param attributeValue the attribute value to bind;
   *   the type of this object must be of the appropriate type based on
   *   the value type of specified attribute definition.
   * @param keyIntoValue an array of length equal to the dimensionality of
   *   the key space of specified attribute definition; entry at index i
   *   is a "representative" from dimension i + 1 of the key space; if
   *   specified attribute definition has a zero-dimensional key space (this
   *   is perhaps the most common scenario) then
   *   this array may either be null or the empty array.
   * @return previous attribute value bound at specified key sequence, or
   *   null if no attribute value was previously bound.
   * @exception IllegalStateException if attributeName is not an existing
   *   node attribute definition; see CyNodeDataDefinition.
   */
  public Object setNodeAttributeValue(String nodeKey, String attributeName,
                                      Object attributeValue,
                                      Object[] keyIntoValue);

  /**
   * @param nodeKey the node from which to retrieve a bound attribute
   *   value.
   * @param attributeName the attribute definition in which to assign an
   *   attribute value.
   * @param keyIntoValue an array of length equal to the dimensionality of
   *   the key space of specified attribute definition; entry at index i
   *   is a "representative" from dimension i + 1 of the key space; if
   *   specified attribute definition has a zero-dimensional key space (this
   *   is perhaps the most commen scenario) then this array may either
   *   be null or the empty array.
   * @return the same value that was set with setNodeAttributeValue() with
   *   parameters specified or null if no such value is bound.
   * @exception IllegalStateException if attributeName is not an existing
   *   node attribute definition; see CyNodeDataDefinition.
   */
  public Object getNodeAttributeValue(String nodeKey, String attributeName,
                                      Object[] keyIntoValue);


  /**
   * This method is the same as getNodeAttributeValue(), only the retrieved
   * attribute value is also deleted.
   * @see #getNodeAttributeValue(String, String, Object[])
   */
  public Object removeNodeAttributeValue(String nodeKey, String attributeName,
                                         Object[] keyIntoValue);

  /**
   * For all bound attribute values on nodeKey in the specified attribute
   * definition, returns the number of representatives in the key space in
   * dimension keyPrefix.length + 1, along specified prefix.
   * @param nodeKey the node to query.
   * @param attributeName the attribute definition to query.
   * @param keyPrefix an array of length K where K is strictly less than N,
   *   the dimensionality of key space of specified attribute definition;
   *   entry at index i contains a "representative" from dimension i + 1 of
   *   the key space of specified attribute definition; this parameter may
   *   be either null or the empty array, in which case the count returned
   *   is the number of representatives in the first dimension of
   *   key space.
   * @return the number of keys in key space dimension K + 1 along specified
   *   keyPrefix.
   * @exception IllegalStateException if attributeName is not an existing
   *   node attribute definition; see CyNodeDataDefinition.
   */
  public int getNodeAttributeKeyspanCount(String nodeKey, String attributeName,
                                          Object[] keyPrefix);

  /**
   * This method is the same as getNodeAttributeKeyspanCount(), only
   * the actual representatives are returned, and not their count.
   * @return a non-null enumeration of key representatives.
   * @see #getNodeAttributeKeyspanCount(String, String, Object[])
   */
  public Enumeration getNodeAttributeKeyspan(String nodeKey,
                                             String attributeName,
                                             Object[] keyPrefix);

  /*
   * I'm not including this method in the API for good reasons.
   * For one, removal is the opposite of insertion, and even though
   * we could potentially remove many entries at once for free, because
   * we inserted them one-by-one, the time complexity for a complete
   * insert-remove cycle of many elements is governed by the insertion
   * time complexity.  Therefore we don't lose in overall time complexity if
   * we omit this removal optimization.  Second, I want listeners to hear
   * all removals that happen.  If I include this method, I either have
   * to iterate over all values that have been removed to fire appropriate
   * listener events (which defeats the purpose of this optimization)
   * or I have to define a separate listener method that
   * says "hey I've removed a keyspan", but then the listener would not
   * get a list of values (corresponding to all keys, recursively, in
   * keyspan) that were removed.
   */
//   public int removeNodeAttributeKeyspan(String nodeKey,
//                                         String attributeName,
//                                         Object[] keyPrefix);

  public void addNodeDataListener(CyNodeDataListener listener);

  public void removeNodeDataListener(CyNodeDataListener listener);

}
