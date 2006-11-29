package nct.util;

import nct.util.IntIterator;

/**
 * An insert-only hashtable that has non-negative 32 bit integer keys and
 * non-negative 32 bit integer values.<p>
 * In the underlying implementation, this hashtable increases in size to adapt
 * to key/value pairs being added (the underlying size of the hashtable is
 * invisible to the programmer).  In the underlying implementation, this
 * hashtable never decreases in size.  As a hashtable increases in size,
 * it takes at most four times as much memory as it would take
 * to store the hashtable's keys and values in a perfectly-sized array.
 * Underlying size expansions are implemented such that the operation of
 * expanding in size is amortized over the contstant time complexity needed to
 * insert new elements.<p>
 *
 * @author Nerius Landys
 */
public class IntIntHash
{
  
  protected static int[] PRIMES = { 11, 23, 53, 113, 251, 509, 1019, 2039,
                                        4079, 8179, 16369, 32749, 65521,
                                        131063, 262133, 524269, 1048571,
                                        2097143, 4194287, 8388587, 16777183,
                                        33554393, 67108837, 134217689,
                                        268435399, 536870879, 1073741789,
                                        2147483647 };
  protected static int INITIAL_SIZE = PRIMES[0];
  private static double THRESHOLD_FACTOR = 0.77;

  protected int[] m_keys;
  protected int[] m_vals;
  protected int m_elements;
  protected int m_size;
  protected int m_thresholdSize;

  // These are caching variables.  The idea is that programmers will
  // frequently first do a get(), and based on that result, will perform
  // some other operations and then maybe do a put() operation with the same
  // key as the previous get() operation.
  private int m_prevKey;
  private int m_prevInx;

  /**
   * Creates a new hashtable.
   */
  public IntIntHash()
  {
    m_keys = new int[INITIAL_SIZE];
    m_vals = new int[INITIAL_SIZE];
    empty();
  }

  /**
   * Removes all key/value pairs from this hashtable.  This operation has
   * O(1) time complexity.
   */
  public void empty()
  {
    m_elements = 0;
    m_size = INITIAL_SIZE;
    m_thresholdSize = (int) (THRESHOLD_FACTOR * (double) m_size);
    for (int i = 0; i < m_size; i++)
    {
    	m_keys[i] = -1;
	m_vals[i] = -1;
    }
    m_prevKey = -1;
    m_prevInx = -1;
  }

  /**
   * Returns the number of key/value pairs currently in this hashtable.
   */
  public int size()
  {
    return m_elements;
  }

  /**
   * Puts a new key/value pair into this hashtable, potentially overwriting
   * an existing value whose key is the same as the one specified.
   * Returns the old value associated with the specified key or -1 if no value
   * is associated with specified key at the time of this call.<p>
   * Insertions into the hashtable are performed in [amortized] time
   * complexity O(1).
   *
   * @exception IllegalArgumentException if either key or value is negative.
   */
  public int put(int key, int value)
  {
    if (key < 0) throw new IllegalArgumentException("key is negative");
    if (value < 0) throw new IllegalArgumentException("value is negative");
    if (m_elements == m_thresholdSize) incrSize();
    if (key != m_prevKey) {
      int incr = 0;
      for (m_prevInx = key % m_size;
           m_keys[m_prevInx] >= 0 && m_keys[m_prevInx] != key;
           m_prevInx = (m_prevInx + incr) % m_size)
        if (incr == 0) incr = 1 + (key % (m_size - 1));
      m_prevKey = key; }
    int returnVal = m_vals[m_prevInx];
    m_vals[m_prevInx] = value;
    m_keys[m_prevInx] = key;
    m_elements += (returnVal >>> 31);
    return returnVal;
  }

  /**
   * Returns the value bound to the specified key or -1 if no value is
   * currently bound to the specified key.<p>
   * Searches in this hashtable are performed in [amortized] time
   * complexity O(1).
   *
   * @exception IllegalArgumentException if key is negative.
   */
  public int get(int key)
  {
    if (key < 0) throw new IllegalArgumentException("key is negative");
    if (key != m_prevKey) {
      int incr = 0;
      for (m_prevInx = key % m_size;
           m_keys[m_prevInx] >= 0 && m_keys[m_prevInx] != key;
           m_prevInx = (m_prevInx + incr) % m_size)
        if (incr == 0) incr = 1 + (key % (m_size - 1));
      m_prevKey = key; }
    return m_vals[m_prevInx];
  }

  /**
   * Returns an iterator of keys in this hashtable, ordered
   * arbitrarily.<p>
   * The returned iterator becomes invalid as soon as put(int, int) or
   * empty() is called on this hashtable; calling methods on an invalid
   * iteration will cause undefined behavior in the iterator.
   * The returned iterator has absolutely no effect on the underlying
   * hashtable.<p>
   * This method returns in constant time.  The returned enumerator
   * returns successive keys in [amortized] time complexity O(1).<p>
   * It is possible to get the keys() and values() enumerations at the same
   * time and iterate over them simultaneously; then, the
   * i<sup>th</sup> element of the keys() enumeration is the key into the
   * i<sup>th</sup> element of the values() enumeration.
   */
  public IntIterator keys()
  {
    return iteration(m_keys);
  }

  /**
   * Returns an iteration of values in this hashtable, ordered
   * arbitrarily.<p>
   * The returned iterator becomes invalid as soon as put(int, int) or
   * empty() is called on this hashtable; calling methods on an invalid
   * iteration will cause undefined behavior in the iterator.
   * The returned iterator has absolutely no effect on the underlying
   * hashtable.<p>
   * This method returns in constant time.  The returned enumerator
   * returns successive values in [amortized] time complexity O(1).<p>
   * It is possible to get the keys() and values() enumerations at the same
   * time and iterate over them simultaneously; then, the
   * i<sup>th</sup> element of the keys() enumeration is the key into the
   * i<sup>th</sup> element of the values() enumeration.
   */
  public IntIterator values()
  {
    return iteration(m_vals);
  }

  private IntIterator iteration(final int[] arr)
  {
    final int numElements = m_elements;
    return new IntIterator()
    {
      int elements = numElements;
      int index = -1;

      public boolean hasNext()
      {
        return (elements != 0);
      }
      
      public int numRemaining()
      {
        return elements;
      }
      
      public int next()
      {
        while (arr[++index] < 0);
        elements--;
        return arr[index];
      }
    };
  }

  private int[] m_keyDump = null;
  private int[] m_valDump = null;

  private void incrSize()
  {
    int newSize;
    try {
      int primesInx = 0;
      while (m_size != PRIMES[primesInx++]);
      newSize = PRIMES[primesInx]; }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new IllegalStateException
        ("too many elements in this hashtable"); }
    if (m_keys.length < newSize) {
      m_keyDump = m_keys; m_valDump = m_vals;
      m_keys = new int[newSize]; m_vals = new int[newSize]; }
    else {
      System.arraycopy(m_keys, 0, m_keyDump, 0, m_size);
      System.arraycopy(m_vals, 0, m_valDump, 0, m_size); }
    for (int i = 0; i < newSize; i++) { m_keys[i] = -1; m_vals[i] = -1; }
    m_size = newSize;
    m_thresholdSize = (int) (THRESHOLD_FACTOR * (double) m_size);
    int incr;
    int newIndex;
    int oldIndex = -1;
    for (int i = 0; i < m_elements; i++) {
      while (m_keyDump[++oldIndex] < 0);
      incr = 0;
      for (newIndex = m_keyDump[oldIndex] % m_size;
           m_keys[newIndex] >= 0;
           newIndex = (newIndex + incr) % m_size)
        if (incr == 0) incr = 1 + (m_keyDump[oldIndex] % (m_size - 1));
      m_keys[newIndex] = m_keyDump[oldIndex];
      m_vals[newIndex] = m_valDump[oldIndex]; }
    m_prevKey = -1;
    m_prevInx = -1;
  }

}
