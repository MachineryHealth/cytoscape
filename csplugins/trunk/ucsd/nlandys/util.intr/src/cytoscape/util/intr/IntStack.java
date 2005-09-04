package cytoscape.util.intr;

/**
 * A first-in, last-out container of 32 bit integers.  In the underlying
 * implementation, the memory consumed by an instance of this class may
 * increase, but does never decrease.
 */
public final class IntStack
{

  // This must be a non-negative integer.
  private static final int DEFAULT_CAPACITY = 11;

  private int[] m_stack;
  private int m_currentSize;

  /**
   * Creates a new stack of integers.
   */
  public IntStack()
  {
    m_stack = new int[DEFAULT_CAPACITY];
    empty();
  }

  /**
   * Removes all integers from this stack.  This operation has constant time
   * complexity.
   */
  public final void empty()
  {
    m_currentSize = 0;
  }

  /**
   * Returns the number of integers that are currently on this stack.
   */
  public final int size()
  {
    return m_currentSize;
  }

  /**
   * Pushes a new integer onto this stack.  A successive peek() or pop()
   * call will return the specified value.
   */
  public final void push(int value)
  {
    try { m_stack[m_currentSize++] = value; }
    catch (ArrayIndexOutOfBoundsException e) {
      m_currentSize--;
      checkSize();
      m_stack[m_currentSize++] = value; }
  }

  /**
   * A non-mutating operation that retrieves the next integer on this stack.<p>
   * It is considered an error to call this method if there are no integers
   * currently on this stack.  If size() returns zero immediately before this
   * method is called, the results of this operation are undefined.
   */
  public final int peek()
  {
    return m_stack[m_currentSize - 1];
  }

  /**
   * Removes and returns the next integer on this stack.<p>
   * It is considered an error to call this method if there are no integers
   * currently on this stack.  If size() returns zero immediately before this
   * method is called, the results of this operation are undefined.
   */
  public final int pop()
  {
    try { return m_stack[--m_currentSize]; }
    catch (ArrayIndexOutOfBoundsException e) {
      m_currentSize++;
      throw e; }
  }

  /**
   * Returns an enumeration of all elements currently on this stack.
   * The order of elements in the returned enumeration is based on the
   * elements' popping order on the stack; the first element returned is
   * the element waiting to be popped off the stack.<p>
   * No operation on this stack will have an effect on the returned
   * enumeration except for push().  If calls to push() are mingled with
   * calls to iterate over the returned enumeration, the contents of this
   * enumeration become undefined.  Iterating over the returned enumeration
   * never has an affect on this stack.
   */
  public final IntEnumerator elements()
  {
    final int[] stack = m_stack;
    final int size = m_currentSize;
    return new IntEnumerator() {
        private int inx = size;
        public final int numRemaining() { return inx; }
        public final int nextInt() { return stack[--inx]; } };
  }

  private final void checkSize()
  {
    if (m_currentSize < m_stack.length) return;
    final int newStackSize = (int) Math.min((long) Integer.MAX_VALUE,
                                            ((long) m_stack.length) * 2l + 1l);
    if (newStackSize == m_stack.length)
      throw new IllegalStateException("cannot allocate large enough array");
    final int[] newStack = new int[newStackSize];
    System.arraycopy(m_stack, 0, newStack, 0, m_stack.length);
    m_stack = newStack;
  }

}
