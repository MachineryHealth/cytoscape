package cytoscape.cruft.obo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * The purpose of this class is to convert a 'gene_ontology.obo' file
 * to a crufty old 'go.onto' file.
 * The parameter passed to the constructor is the 'gene_ontology.obo' file and
 * the content read from this reader is the crufty old 'go.onto' file.<p>
 * Please note that this code was not written with performance in mind.
 * Therefore, evaluating this code will not give a good indication of the
 * author's programming ability.
 */
public final class OboOntologyReader extends Reader
{

  private final String NL = System.getProperty("line.separator");
  private BufferedReader m_obo;
  private String m_readString;
  private int m_readInx; // Index into a m_readString character.

  public OboOntologyReader(final Reader oboFile)
  {
    if (oboFile == null) throw new NullPointerException("oboFile is null");
    m_obo = new BufferedReader(oboFile);
    m_readString = "(curator=??) (type=all)" + NL;
    m_readInx = 0;
  }

  public final int read(final char[] cbuf,
                        final int off,
                        final int len) throws IOException
  {
    if (m_obo == null) throw new IOException("this stream is closed");
    if (m_readString == null) return -1;
    final int returnThis;
    if (m_readString.length() - m_readInx >= len) {
      m_readString.getChars(m_readInx, m_readInx + len,
                            cbuf, off);
      returnThis = len;
      m_readInx += len; }
    else { // len is greater than the number of chars left in m_readString.
      m_readString.getChars(m_readInx, m_readString.length(),
                            cbuf, off);
      returnThis = m_readString.length() - m_readInx;
      m_readInx = m_readString.length(); }
    if (m_readInx == m_readString.length()) readMore();
    return returnThis;
  }

  /*
   * The job of this method is to set m_readString and m_readInx.
   */
  private final void readMore() throws IOException
  {
    while (true) // Read until line after '[Term]'.
    {
      final String line = m_obo.readLine();
      if (line == null) { // End of underlying stream.
        m_readString = null;
        return; }
      final String trimmedLine = line.trim();
      if (trimmedLine.equals("[Term]")) {
        break; }
    }

    String id = "";
    String desc = "";
    String isa = "";
    String partof = "";
    while (true) // Parse until blank line.
    {
      final String line = m_obo.readLine().trim();
      if (line.length() == 0) break;
      final int colonInx = line.indexOf(':');
      final String key = line.substring(0, colonInx).trim();
      final String val = line.substring(colonInx + 1).trim();
      if (key.equals("id")) {
        // There's only one id.
        id = val.substring(3); /* Chop away 'GO:'. */ }
      else if (key.equals("name")) {
        // There's only one description.
        desc = val; }
      else if (key.equals("is_a")) {
        // Multiple isa possible.
        final String isaElement = val.substring(3, 10); // Chop 'GO:'.
        isa = isa + isaElement + " "; }
      else if (key.equals("relationship") &&
               val.startsWith("part_of")) {
        // Multiple partof possible.
        final String partofElement = val.substring(11, 18); // 'part_of GO:'.
        partof = partof + partofElement + " "; }
    }

    m_readString = id + " = " + desc;
    if (isa.length() > 0) {
      m_readString = m_readString + " [isa: " + isa + "]"; }
    if (partof.length() > 0) {
      m_readString = m_readString + " [partof: " + partof + "]"; }
    m_readString = m_readString + NL;
    m_readInx = 0;
  }

  /**
   * Closes the underlying obo file stream as well.
   */
  public final void close() throws IOException
  {
    try {
      m_obo.close(); }
    finally {
      m_obo = null; }
  }

}
