package cytoscape.graph.dynamic.util;

// Valid indices: [0, Integer.MAX_VALUE - 1].
class EdgeArray implements java.io.Serializable
{

  private final static int INITIAL_CAPACITY = 0; // Must be non-negative.

  private Edge[] m_edgeArr;

  EdgeArray()
  {
    m_edgeArr = new Edge[INITIAL_CAPACITY];
  }

  // Understand that this method will not increase the size of the underlying
  // array, no matter what.
  // Throws ArrayIndexOutOfBoundsException if index is negative.
  // The package-level agreement for this class is that Integer.MAX_VALUE
  // will never be passed to this method.
  Edge getEdgeAtIndex(int index)
  {
    if (index >= m_edgeArr.length) return null;
    return m_edgeArr[index];
  }

  // Understand that this method will potentially increase the size of the
  // underlying array, but only if two conditions hold:
  //   1. edge is not null and
  //   2. index is greater than or equal to the length of the array.
  // Throws ArrayIndexOutOfBoundsException if index is negative.
  // The package-level agreement for this class is that Integer.MAX_VALUE
  // will never be passed to this method.
  void setEdgeAtIndex(Edge edge, int index)
  {
    if (index >= m_edgeArr.length && edge == null) return;
    try { m_edgeArr[index] = edge; }
    catch (ArrayIndexOutOfBoundsException e)
    {
      if (index < 0) throw e;
      final int newArrSize = (int)
        Math.min((long) Integer.MAX_VALUE,
                 Math.max(((long) m_edgeArr.length) * 2l + 1l,
                          ((long) index) + 1l + (long) INITIAL_CAPACITY));
      Edge[] newArr = new Edge[newArrSize];
      System.arraycopy(m_edgeArr, 0, newArr, 0, m_edgeArr.length);
      m_edgeArr = newArr;
      m_edgeArr[index] = edge;
    }
  }

}
