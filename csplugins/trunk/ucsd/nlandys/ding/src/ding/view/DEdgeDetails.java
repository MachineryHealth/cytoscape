package ding.view;

import cytoscape.graph.fixed.FixedGraph;
import cytoscape.render.immed.EdgeAnchors;
import cytoscape.util.intr.IntEnumerator;
import cytoscape.util.intr.IntIterator;
import cytoscape.util.intr.IntObjHash;
import cytoscape.util.intr.MinIntHeap;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.HashMap;

class DEdgeDetails extends IntermediateEdgeDetails
{

  final DGraphView m_view;

  final IntObjHash m_colorsLowDetail = new IntObjHash();
  final Object m_deletedEntry = new Object();

  final HashMap m_segmentThicknesses = new HashMap();
  final HashMap m_sourceArrows = new HashMap();
  final HashMap m_sourceArrowPaints = new HashMap();
  final HashMap m_targetArrows = new HashMap();
  final HashMap m_targetArrowPaints = new HashMap();
  final HashMap m_segmentPaints = new HashMap();
  final HashMap m_segmentDashLengths = new HashMap();
  final HashMap m_labelCounts = new HashMap();
  final HashMap m_labelTexts = new HashMap();
  final HashMap m_labelFonts = new HashMap();
  final HashMap m_labelPaints = new HashMap();

  DEdgeDetails(DGraphView view)
  {
    m_view = view;
  }

  void unregisterEdge(int edge)
  {
    final Object o = m_colorsLowDetail.get(edge);
    if (o != null && o != m_deletedEntry) {
      m_colorsLowDetail.put(edge, m_deletedEntry); }
    final Integer key = new Integer(edge);
    m_segmentThicknesses.remove(key);
    m_sourceArrows.remove(key);
    m_sourceArrowPaints.remove(key);
    m_targetArrows.remove(key);
    m_targetArrowPaints.remove(key);
    m_segmentPaints.remove(key);
    m_segmentDashLengths.remove(key);
    m_labelCounts.remove(key);
    m_labelTexts.remove(key);
    m_labelFonts.remove(key);
    m_labelPaints.remove(key);
  }

  public Color colorLowDetail(int edge)
  {
    final Object o = m_colorsLowDetail.get(edge);
    if (o == null || o == m_deletedEntry) {
      return super.colorLowDetail(edge); }
    return (Color) o;
  }

  /*
   * A null color has the special meaning to remove overridden color.
   */
  void overrideColorLowDetail(int edge, Color color)
  {
    if (color == null ||
        color.equals(super.colorLowDetail(edge))) {
      final Object val = m_colorsLowDetail.get(edge);
      if (val != null && val != m_deletedEntry) {
        m_colorsLowDetail.put(edge, m_deletedEntry); } }
    else {
      m_colorsLowDetail.put(edge, color); }
  }

  public byte sourceArrow(int edge)
  {
    final Object o = m_sourceArrows.get(new Integer(edge));
    if (o == null) { return super.sourceArrow(edge); }
    return ((Byte) o).byteValue();
  }

  /*
   * A non-negative arrowType has the special meaning to remove overridden
   * arrow.
   */
  void overrideSourceArrow(int edge, byte arrowType)
  {
    if (arrowType >= 0 ||
        arrowType == super.sourceArrow(edge)) {
      m_sourceArrows.remove(new Integer(edge)); }
    else { m_sourceArrows.put(new Integer(edge), new Byte(arrowType)); }
  }

  public Paint sourceArrowPaint(int edge)
  {
    final Object o = m_sourceArrowPaints.get(new Integer(edge));
    if (o == null) { return super.sourceArrowPaint(edge); }
    return (Paint) o;
  }

  /*
   * A null paint has the special meaning to remove overridden paint.
   */
  void overrideSourceArrowPaint(int edge, Paint paint)
  {
    if (paint == null ||
        paint.equals(super.sourceArrowPaint(edge))) {
      m_sourceArrowPaints.remove(new Integer(edge)); }
    else { m_sourceArrowPaints.put(new Integer(edge), paint); }
  }

  public byte targetArrow(int edge)
  {
    final Object o = m_targetArrows.get(new Integer(edge));
    if (o == null) { return super.targetArrow(edge); }
    return ((Byte) o).byteValue();
  }

  /*
   * A non-negative arrowType has the special meaning to remove overridden
   * arrow.
   */
  void overrideTargetArrow(int edge, byte arrowType)
  {
    if (arrowType >= 0 ||
        arrowType == super.targetArrow(edge)) {
      m_targetArrows.remove(new Integer(edge)); }
    else { m_targetArrows.put(new Integer(edge), new Byte(arrowType)); }
  }

  public Paint targetArrowPaint(int edge)
  {
    final Object o = m_targetArrowPaints.get(new Integer(edge));
    if (o == null) { return super.targetArrowPaint(edge); }
    return (Paint) o;
  }

  /*
   * A null paint has the special meaning to remove overridden paint.
   */
  void overrideTargetArrowPaint(int edge, Paint paint)
  {
    if (paint == null ||
        paint.equals(super.targetArrowPaint(edge))) {
      m_targetArrowPaints.remove(new Integer(edge)); }
    else { m_targetArrowPaints.put(new Integer(edge), paint); }
  }

  private final MinIntHeap m_heap = new MinIntHeap();

  public EdgeAnchors anchors(int edge)
  {
    final EdgeAnchors returnThis = (EdgeAnchors) (m_view.getEdgeView(~edge));
    if (returnThis.numAnchors() > 0) { return returnThis; }
    final FixedGraph graph = (FixedGraph) m_view.m_drawPersp;
    if (graph.edgeSource(edge) == graph.edgeTarget(edge)) { // Self-edge.
      final int node = graph.edgeSource(edge);
      m_view.m_spacial.exists(node, m_view.m_extentsBuff, 0);
      final double w = ((double) m_view.m_extentsBuff[2]) -
        m_view.m_extentsBuff[0];
      final double h = ((double) m_view.m_extentsBuff[3]) -
        m_view.m_extentsBuff[1];
      final double x = (((double) m_view.m_extentsBuff[0]) +
                        m_view.m_extentsBuff[2]) / 2.0d;
      final double y = (((double) m_view.m_extentsBuff[1]) +
                        m_view.m_extentsBuff[3]) / 2.0d;
      final double nodeSize = Math.max(w, h);
      int i = 0;
      final IntIterator selfEdges = graph.edgesConnecting
        (node, node, true, true, true);
      while (selfEdges.hasNext()) {
        final int e2 = selfEdges.nextInt();
        if (e2 == edge) { break; }
        if (((EdgeAnchors) m_view.getEdgeView(~e2)).numAnchors() == 0) {
          i++; } }
      final int inx = i;
      return new EdgeAnchors() {
          public int numAnchors() {
            return 2; }
          public void getAnchor(int anchorInx, float[] anchorArr, int offset) {
            if (anchorInx == 0) {
              anchorArr[offset] = (float) (x - (inx + 3) * nodeSize / 2.0d);
              anchorArr[offset + 1] = (float) y; }
            else if (anchorInx == 1) {
              anchorArr[offset] = (float) x;
              anchorArr[offset + 1] = (float) (y - (inx + 3) *
                                               nodeSize / 2.0d); }
          } }; }
    while (true) {
      {
        final IntIterator otherEdges = graph.edgesConnecting
          (graph.edgeSource(edge), graph.edgeTarget(edge),
           true, true, true);
        m_heap.empty();
        while (otherEdges.hasNext()) {
          m_heap.toss(otherEdges.nextInt()); }
      }
      final IntEnumerator otherEdges = m_heap.orderedElements(false);
      int otherEdge = otherEdges.nextInt();
      if (otherEdge == edge) { break; }
      int i =
        ((EdgeAnchors) m_view.getEdgeView(~otherEdge)).numAnchors() == 0 ?
        1 : 0;
      while (true) {
        if (edge == (otherEdge = otherEdges.nextInt())) { break; }
        if (((EdgeAnchors) m_view.getEdgeView(~otherEdge)).numAnchors() == 0) {
          i++; } }
      final int inx = i;
      m_view.m_spacial.exists(graph.edgeSource(edge), m_view.m_extentsBuff, 0);
      final double srcW = ((double) m_view.m_extentsBuff[2]) -
        m_view.m_extentsBuff[0];
      final double srcH = ((double) m_view.m_extentsBuff[3]) -
        m_view.m_extentsBuff[1];
      final double srcX = (((double) m_view.m_extentsBuff[0]) +
                           m_view.m_extentsBuff[2]) / 2.0d;
      final double srcY = (((double) m_view.m_extentsBuff[1]) +
                           m_view.m_extentsBuff[3]) / 2.0d;
      m_view.m_spacial.exists(graph.edgeTarget(edge), m_view.m_extentsBuff, 0);
      final double trgW = ((double) m_view.m_extentsBuff[2]) -
        m_view.m_extentsBuff[0];
      final double trgH = ((double) m_view.m_extentsBuff[3]) -
        m_view.m_extentsBuff[1];
      final double trgX = (((double) m_view.m_extentsBuff[0]) +
                           m_view.m_extentsBuff[2]) / 2.0d;
      final double trgY = (((double) m_view.m_extentsBuff[1]) +
                           m_view.m_extentsBuff[3]) / 2.0d;
      final double nodeSize =
        Math.max(Math.max(Math.max(srcW, srcH), trgW), trgH);
      final double midX = (srcX + trgX) / 2;
      final double midY = (srcY + trgY) / 2;
      final double dx = trgX - srcX;
      final double dy = trgY - srcY;
      final double len = Math.sqrt(dx * dx + dy * dy);
      if (((float) len) == 0.0f) { break; }
      final double normX = dx / len;
      final double normY = dy / len;
      final double factor = ((inx + 1) / 2) *
        (inx % 2 == 0 ? 1 : -1) * nodeSize;
      final double anchorX = midX + factor * normY;
      final double anchorY = midY - factor * normX;
      return new EdgeAnchors() {
          public int numAnchors() { return 1; }
          public void getAnchor(int inx, float[] arr, int off) {
            arr[off] = (float) anchorX;
            arr[off + 1] = (float) anchorY; } }; }
    return returnThis;
  }

  public float anchorSize(int edge, int anchorInx)
  {
    if (m_view.getEdgeView(~edge).isSelected() &&
        ((DEdgeView) m_view.getEdgeView(~edge)).numAnchors() > 0) {
      return m_view.getAnchorSize(); }
    else {
      return 0.0f; }
  }

  public Paint anchorPaint(int edge, int anchorInx)
  {
    if (((DEdgeView) (m_view.getEdgeView(~edge))).m_lineType ==
        DEdgeView.STRAIGHT_LINES) { anchorInx = anchorInx / 2; }
    if (m_view.m_selectedAnchors.count((edge << 6) | anchorInx) > 0) {
      return m_view.getAnchorSelectedPaint(); }
    else {
      return m_view.getAnchorUnselectedPaint(); }
  }

  public float segmentThickness(int edge)
  {
    final Object o = m_segmentThicknesses.get(new Integer(edge));
    if (o == null) { return super.segmentThickness(edge); }
    return ((Float) o).floatValue();
  }

  /*
   * A negative thickness value has the special meaning to remove overridden
   * thickness.
   */
  void overrideSegmentThickness(int edge, float thickness)
  {
    if (thickness < 0.0f ||
        thickness == super.segmentThickness(edge)) {
      m_segmentThicknesses.remove(new Integer(edge)); }
    else { m_segmentThicknesses.put(new Integer(edge), new Float(thickness)); }
  }

  public Paint segmentPaint(int edge)
  {
    final Object o = m_segmentPaints.get(new Integer(edge));
    if (o == null) { return super.segmentPaint(edge); }
    return (Paint) o;
  }

  /*
   * A null paint has the special meaning to remove overridden paint.
   */
  void overrideSegmentPaint(int edge, Paint paint)
  {
    if (paint == null ||
        paint.equals(super.segmentPaint(edge))) {
      m_segmentPaints.remove(new Integer(edge)); }
    else { m_segmentPaints.put(new Integer(edge), paint); }
  }

  public float segmentDashLength(int edge)
  {
    final Object o = m_segmentDashLengths.get(new Integer(edge));
    if (o == null) { return super.segmentDashLength(edge); }
    return ((Float) o).floatValue();
  }

  /*
   * A negative length value has the special meaning to remove overridden
   * length.
   */
  void overrideSegmentDashLength(int edge, float length)
  {
    if (length < 0.0f ||
        length == super.segmentDashLength(edge)) {
      m_segmentDashLengths.remove(new Integer(edge)); }
    else { m_segmentDashLengths.put(new Integer(edge), new Float(length)); }
  }

  public int labelCount(int edge)
  {
    final Object o = m_labelCounts.get(new Integer(edge));
    if (o == null) { return super.labelCount(edge); }
    return ((Integer) o).intValue();
  }

  /*
   * A negative labelCount has the special meaning to remove overridden count.
   */
  void overrideLabelCount(int edge, int labelCount)
  {
    if (labelCount < 0 ||
        labelCount == super.labelCount(edge)) {
      m_labelCounts.remove(new Integer(edge)); }
    else { m_labelCounts.put(new Integer(edge), new Integer(labelCount)); }
  }

  public String labelText(int edge, int labelInx)
  {
    final long key = (((long) edge) << 32) | ((long) labelInx);
    final Object o = m_labelTexts.get(new Long(key));
    if (o == null) { return super.labelText(edge, labelInx); }
    return (String) o;
  }

  /*
   * A null text has the special meaning to remove overridden text.
   */
  void overrideLabelText(int edge, int labelInx, String text)
  {
    final long key = (((long) edge) << 32) | ((long) labelInx);
    if (text == null ||
        text.equals(super.labelText(edge, labelInx))) {
      m_labelTexts.remove(new Long(key)); }
    else { m_labelTexts.put(new Long(key), text); }
  }

  public Font labelFont(int edge, int labelInx)
  {
    final long key = (((long) edge) << 32) | ((long) labelInx);
    final Object o = m_labelFonts.get(new Long(key));
    if (o == null) { return super.labelFont(edge, labelInx); }
    return (Font) o;
  }

  /*
   * A null font has the special meaning to remove overridden font.
   */
  void overrideLabelFont(int edge, int labelInx, Font font)
  {
    final long key = (((long) edge) << 32) | ((long) labelInx);
    if (font == null ||
        font.equals(super.labelFont(edge, labelInx))) {
      m_labelFonts.remove(new Long(key)); }
    else { m_labelFonts.put(new Long(key), font); }
  }

  public Paint labelPaint(int edge, int labelInx)
  {
    final long key = (((long) edge) << 32) | ((long) labelInx);
    final Object o = m_labelPaints.get(new Long(key));
    if (o == null) { return super.labelPaint(edge, labelInx); }
    return (Paint) o;
  }

  /*
   * A null paint has the special meaning to remove overridden paint.
   */
  void overrideLabelPaint(int edge, int labelInx, Paint paint)
  {
    final long key = (((long) edge) << 32) | ((long) labelInx);
    if (paint == null ||
        paint.equals(super.labelPaint(edge, labelInx))) {
      m_labelPaints.remove(new Long(key)); }
    else { m_labelPaints.put(new Long(key), paint); }
  }

}
