package ding.view;

import cytoscape.geom.spacial.SpacialEntry2DEnumerator;

import cytoscape.graph.fixed.FixedGraph;

import cytoscape.render.export.ImageImposter;

import cytoscape.render.immed.EdgeAnchors;
import cytoscape.render.immed.GraphGraphics;

import cytoscape.render.stateful.GraphLOD;
import cytoscape.render.stateful.GraphRenderer;

import cytoscape.util.intr.IntEnumerator;
import cytoscape.util.intr.IntHash;
import cytoscape.util.intr.IntStack;

import giny.model.Node;

import giny.view.EdgeView;
import giny.view.GraphViewChangeListener;
import giny.view.NodeView;

// AJK: 04/02/06 BEGIN
import phoebe.*;

import java.awt.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import java.util.*;

// AJK: 04/26/06 BEGIN
//    for tooltips
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolTip;


// AJK: 04/26/06 END
public class InnerCanvas extends DingCanvas
    implements MouseListener, MouseMotionListener,
        java.awt.dnd.DropTargetListener, PhoebeCanvasDroppable, KeyListener {
    final double[] m_ptBuff = new double[2];
    final float[] m_extentsBuff2 = new float[4];
    final float[] m_floatBuff1 = new float[2];
    final float[] m_floatBuff2 = new float[2];
    final Line2D.Float m_line = new Line2D.Float();
    final GeneralPath m_path = new GeneralPath();
    final GeneralPath m_path2 = new GeneralPath();
    final IntStack m_stack = new IntStack();
    final IntStack m_stack2 = new IntStack();
    final Object m_lock;
    DGraphView m_view;
    final GraphLOD[] m_lod = new GraphLOD[1];
    final IntHash m_hash;
    GraphGraphics m_grafx;
    double m_xCenter;
    double m_yCenter;
    double m_scaleFactor;
    private int m_lastRenderDetail = 0;
    private Rectangle m_selectionRect = null;
    final boolean[] m_printingTextAsShape = new boolean[1];

    //AJK: 04/02/06 BEGIN
    private DropTarget dropTarget;
    private String CANVAS_DROP = "CanvasDrop";

    /**
     * DOCUMENT ME!
     */
    public Vector listeners = new Vector();

    //       AJK: 04/02/06 END
    // AJK: 04/27/06 for context menus

    /**
     * DOCUMENT ME!
     */
    public Vector nodeContextMenuListeners = new Vector();

    /**
     * DOCUMENT ME!
     */
    public Vector edgeContextMenuListeners = new Vector();

    InnerCanvas(Object lock, DGraphView view) {
        super();
        m_lock = lock;
        m_view = view;
        m_lod[0] = new GraphLOD(); // Default LOD.
        m_hash = new IntHash();
        m_backgroundColor = Color.white;
		m_isVisible = true;
		m_isOpaque = false;
        m_xCenter = 0.0d;
        m_yCenter = 0.0d;
        m_scaleFactor = 1.0d;
        m_printingTextAsShape[0] = true;
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true);

        // AJK: 04/02/06 BEGIN
        dropTarget = new DropTarget(this, // component
                DnDConstants.ACTION_COPY, // actions
                this); // DropTargetListener

        // AJK: 04/02/06 END
    }

    /**
     * DOCUMENT ME!
     *
     * @param x DOCUMENT ME!
     * @param y DOCUMENT ME!
     * @param width DOCUMENT ME!
     * @param height DOCUMENT ME!
     */
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        if ((width > 0) && (height > 0)) {
            final Image img = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_ARGB);
            GraphGraphics grafx = new GraphGraphics(img, false);

            synchronized (m_lock) {
                m_img = img;
                m_grafx = grafx;
                m_view.m_viewportChanged = true;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param g DOCUMENT ME!
     */
    public void update(Graphics g) {
        if (m_grafx == null)
            return;

        // This is the magical portion of code that transfers what is in the
        // visual data structures into what's on the image.
        boolean contentChanged = false;
        boolean viewportChanged = false;
        double xCenter = 0.0d;
        double yCenter = 0.0d;
        double scaleFactor = 1.0d;

		// set color alpha based on opacity setting
		int alpha = (m_isOpaque) ? 255 : 0;
		Color backgroundColor = new Color(m_backgroundColor.getRed(),
										  m_backgroundColor.getGreen(),
										  m_backgroundColor.getBlue(),
										  alpha);

		// short circuit if we are not visible
		if (!m_isVisible) {
			// clear the background
			m_grafx.clear(backgroundColor, m_xCenter,
						  m_yCenter, m_scaleFactor);
			// update the context canvas
			g.drawImage(m_img, 0, 0, null);
		}

        synchronized (m_lock) {
            if (m_view.m_contentChanged || m_view.m_viewportChanged) {
                m_lastRenderDetail = GraphRenderer.renderGraph((FixedGraph) m_view.m_drawPersp,
                        m_view.m_spacial, m_lod[0], m_view.m_nodeDetails,
                        m_view.m_edgeDetails, m_hash, m_grafx, backgroundColor,
                        m_xCenter, m_yCenter, m_scaleFactor);
                contentChanged = m_view.m_contentChanged;
                m_view.m_contentChanged = false;
                viewportChanged = m_view.m_viewportChanged;
                xCenter = m_xCenter;
                yCenter = m_yCenter;
                scaleFactor = m_scaleFactor;
                m_view.m_viewportChanged = false;
            }
        }

        g.drawImage(m_img, 0, 0, null);

        if (m_selectionRect != null) {
            final Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.red);
            g2.draw(m_selectionRect);
        }

        if (contentChanged) {
            final ContentChangeListener lis = m_view.m_cLis[0];

            if (lis != null)
                lis.contentChanged();
        }

        if (viewportChanged) {
            final ViewportChangeListener lis = m_view.m_vLis[0];

            if (lis != null)
                lis.viewportChanged(
                    getWidth(),
                    getHeight(),
                    xCenter,
                    yCenter,
                    scaleFactor);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param g DOCUMENT ME!
     */
    public void paint(Graphics g) {
        update(g);
    }

    /**
     * DOCUMENT ME!
     *
     * @param g DOCUMENT ME!
     */
    public void print(Graphics g) {
        final ImageImposter img = new ImageImposter(g,
                getWidth(),
                getHeight());

        synchronized (m_lock) {
            GraphRenderer.renderGraph(
                (FixedGraph) m_view.m_drawPersp,
                m_view.m_spacial,
                new GraphLOD() {
                    public byte renderEdges(int visibleNodeCount,
                        int totalNodeCount, int totalEdgeCount) {
                        return m_lod[0].renderEdges(visibleNodeCount,
                            totalNodeCount, totalEdgeCount);
                    }

                    public boolean detail(int renderNodeCount,
                        int renderEdgeCount) {
                        return true;
                    }

                    public boolean nodeBorders(int renderNodeCount,
                        int renderEdgeCount) {
                        return true;
                    }

                    public boolean nodeLabels(int renderNodeCount,
                        int renderEdgeCount) {
                        return true;
                    }

                    public boolean customGraphics(int renderNodeCount,
                        int renderEdgeCount) {
                        return true;
                    }

                    public boolean edgeArrows(int renderNodeCount,
                        int renderEdgeCount) {
                        return true;
                    }

                    public boolean dashedEdges(int renderNodeCount,
                        int renderEdgeCount) {
                        return true;
                    }

                    public boolean edgeAnchors(int renderNodeCount,
                        int renderEdgeCount) {
                        return true;
                    }

                    public boolean edgeLabels(int renderNodeCount,
                        int renderEdgeCount) {
                        return true;
                    }

                    public boolean textAsShape(int renderNodeCount,
                        int renderEdgeCount) {
                        return m_printingTextAsShape[0];
                    }
                },
                m_view.m_nodeDetails,
                m_view.m_edgeDetails,
                m_hash,
                new GraphGraphics(img, false),
                m_backgroundColor,
                m_xCenter,
                m_yCenter,
                m_scaleFactor);
        }
    }

    private int m_currMouseButton = 0;
    private int m_lastXMousePos = 0;
    private int m_lastYMousePos = 0;
    private boolean m_button1NodeDrag = false;

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            // It's too complicated to correctly handle both control and shift
            // simultaneously.
            if (e.isShiftDown() && e.isControlDown())
                return;

            m_currMouseButton = 1;
            m_lastXMousePos = e.getX();
            m_lastYMousePos = e.getY();

            int[] unselectedNodes = null;
            int[] unselectedEdges = null;
            int chosenNode = 0;
            int chosenEdge = 0;
            int chosenAnchor = -1;
            byte chosenNodeSelected = 0;
            byte chosenEdgeSelected = 0;

            synchronized (m_lock) {
                if (m_view.m_nodeSelection) {
                    m_ptBuff[0] = m_lastXMousePos;
                    m_ptBuff[1] = m_lastYMousePos;
                    m_view.xformComponentToNodeCoords(m_ptBuff);
                    m_stack.empty();
                    m_view.getNodesIntersectingRectangle((float) m_ptBuff[0],
                        (float) m_ptBuff[1], (float) m_ptBuff[0],
                        (float) m_ptBuff[1],
                        (m_lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0,
                        m_stack);
                    chosenNode = (m_stack.size() > 0) ? m_stack.peek() : 0;
                }

                if (m_view.m_edgeSelection && (chosenNode == 0) &&
                        ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0)) {
                    m_ptBuff[0] = m_lastXMousePos;
                    m_ptBuff[1] = m_lastYMousePos;
                    m_view.xformComponentToNodeCoords(m_ptBuff);

                    final IntEnumerator hits = m_view.m_spacialA.queryOverlap((float) m_ptBuff[0],
                            (float) m_ptBuff[1], (float) m_ptBuff[0],
                            (float) m_ptBuff[1], null, 0, false);
                    chosenAnchor = (hits.numRemaining() > 0) ? hits.nextInt() : (-1);
                }

                if (m_view.m_edgeSelection && (chosenNode == 0) &&
                        (chosenAnchor < 0)) {
                    computeEdgesIntersecting(m_lastXMousePos - 1,
                        m_lastYMousePos - 1, m_lastXMousePos + 1,
                        m_lastYMousePos + 1, m_stack2);
                    chosenEdge = (m_stack2.size() > 0) ? m_stack2.peek() : 0;
                }

                if ((!e.isShiftDown()) && // If shift is down never unselect.
                        (((chosenNode == 0) && (chosenEdge == 0) &&
                        (chosenAnchor < 0)) || // Mouse missed all.
                        
                    // Not [we hit something but it was already selected].
                    !(((chosenNode != 0) &&
                        m_view.getNodeView(chosenNode)
                                  .isSelected()) || (chosenAnchor >= 0) ||
                        ((chosenEdge != 0) &&
                        m_view.getEdgeView(chosenEdge)
                                  .isSelected())))) {
                    if (m_view.m_nodeSelection) { // Unselect all selected nodes.
                        unselectedNodes = m_view.getSelectedNodeIndices();

                        // Adding this line to speed things up from O(n*log(n)) to O(n).
                        m_view.m_selectedNodes.empty();

                        for (int i = 0; i < unselectedNodes.length; i++)
                            ((DNodeView) m_view.getNodeView(unselectedNodes[i])).unselectInternal();
                    } else
                        unselectedNodes = new int[0];

                    if (m_view.m_edgeSelection) { // Unselect all selected edges.
                        unselectedEdges = m_view.getSelectedEdgeIndices();

                        // Adding this line to speed things up from O(n*log(n)) to O(n).
                        m_view.m_selectedEdges.empty();

                        for (int i = 0; i < unselectedEdges.length; i++)
                            ((DEdgeView) m_view.getEdgeView(unselectedEdges[i])).unselectInternal();
                    } else
                        unselectedEdges = new int[0];

                    if ((unselectedNodes.length > 0) ||
                            (unselectedEdges.length > 0))
                        m_view.m_contentChanged = true;
                }

                if (chosenNode != 0) {
                    final boolean wasSelected = m_view.getNodeView(chosenNode)
                                                      .isSelected();

                    if (wasSelected && e.isShiftDown()) {
                        ((DNodeView) m_view.getNodeView(chosenNode)).unselectInternal();
                        chosenNodeSelected = (byte) -1;
                    } else if (!wasSelected) {
                        ((DNodeView) m_view.getNodeView(chosenNode)).selectInternal();
                        chosenNodeSelected = (byte) 1;
                    }

                    m_button1NodeDrag = true;
                    m_view.m_contentChanged = true;
                }

                if (chosenAnchor >= 0) {
                    if (e.isControlDown()) {
                        final int edge = chosenAnchor >>> 6;
                        final int anchorInx = chosenAnchor & 0x0000003f;
                        ((DEdgeView) m_view.getEdgeView(~edge)).removeHandle(anchorInx);
                        m_button1NodeDrag = false;
                    } else {
                        final boolean wasSelected = m_view.m_selectedAnchors.count(chosenAnchor) > 0;

                        if (wasSelected && e.isShiftDown())
                            m_view.m_selectedAnchors.delete(chosenAnchor);
                        else if (!wasSelected) {
                            if (!e.isShiftDown())
                                m_view.m_selectedAnchors.empty();

                            m_view.m_selectedAnchors.insert(chosenAnchor);
                        }

                        m_button1NodeDrag = true;
                    }

                    m_view.m_contentChanged = true;
                }

                if (chosenEdge != 0) {
                    if (e.isControlDown() &&
                            ((m_lastRenderDetail &
                            GraphRenderer.LOD_EDGE_ANCHORS) != 0)) {
                        m_view.m_selectedAnchors.empty();
                        m_ptBuff[0] = m_lastXMousePos;
                        m_ptBuff[1] = m_lastYMousePos;
                        m_view.xformComponentToNodeCoords(m_ptBuff);

                        final int chosenInx = ((DEdgeView) m_view.getEdgeView(chosenEdge)).addHandleFoo(
                                new Point2D.Float((float) m_ptBuff[0],
                                    (float) m_ptBuff[1]));
                        m_view.m_selectedAnchors.insert(((~chosenEdge) << 6) |
                            chosenInx);
                    }

                    final boolean wasSelected = m_view.getEdgeView(chosenEdge)
                                                      .isSelected();

                    if (wasSelected && e.isShiftDown()) {
                        ((DEdgeView) m_view.getEdgeView(chosenEdge)).unselectInternal();
                        chosenEdgeSelected = (byte) -1;
                    } else if (!wasSelected) {
                        ((DEdgeView) m_view.getEdgeView(chosenEdge)).selectInternal(false);
                        chosenEdgeSelected = (byte) 1;

                        if ((m_lastRenderDetail &
                                GraphRenderer.LOD_EDGE_ANCHORS) != 0) {
                            m_ptBuff[0] = m_lastXMousePos;
                            m_ptBuff[1] = m_lastYMousePos;
                            m_view.xformComponentToNodeCoords(m_ptBuff);

                            final IntEnumerator hits = m_view.m_spacialA.queryOverlap((float) m_ptBuff[0],
                                    (float) m_ptBuff[1], (float) m_ptBuff[0],
                                    (float) m_ptBuff[1], null, 0, false);

                            if (hits.numRemaining() > 0) {
                                final int hit = hits.nextInt();

                                if (m_view.m_selectedAnchors.count(hit) == 0)
                                    m_view.m_selectedAnchors.insert(hit);
                            }
                        }
                    }

                    m_button1NodeDrag = true;
                    m_view.m_contentChanged = true;
                }

                if ((chosenNode == 0) && (chosenEdge == 0) &&
                        (chosenAnchor < 0)) {
                    m_selectionRect = new Rectangle(m_lastXMousePos,
                            m_lastYMousePos, 0, 0);
                    m_button1NodeDrag = false;
                }
            }

            final GraphViewChangeListener listener = m_view.m_lis[0];

            if (listener != null) {
                if ((unselectedNodes != null) && (unselectedNodes.length > 0))
                    listener.graphViewChanged(
                        new GraphViewNodesUnselectedEvent(m_view,
                            unselectedNodes));

                if ((unselectedEdges != null) && (unselectedEdges.length > 0))
                    listener.graphViewChanged(
                        new GraphViewEdgesUnselectedEvent(m_view,
                            unselectedEdges));

                if (chosenNode != 0) {
                    if (chosenNodeSelected > 0)
                        listener.graphViewChanged(
                            new GraphViewNodesSelectedEvent(
                                m_view,
                                new int[] { chosenNode }));
                    else if (chosenNodeSelected < 0)
                        listener.graphViewChanged(
                            new GraphViewNodesUnselectedEvent(
                                m_view,
                                new int[] { chosenNode }));
                }

                if (chosenEdge != 0) {
                    if (chosenEdgeSelected > 0)
                        listener.graphViewChanged(
                            new GraphViewEdgesSelectedEvent(
                                m_view,
                                new int[] { chosenEdge }));
                    else if (chosenEdgeSelected < 0)
                        listener.graphViewChanged(
                            new GraphViewEdgesUnselectedEvent(
                                m_view,
                                new int[] { chosenEdge }));
                }
            }

            // Repaint after listener events are fired because listeners may change
            // something in the graph view.
            repaint();
        } else if (e.getButton() == MouseEvent.BUTTON2) {
            m_currMouseButton = 2;
            m_lastXMousePos = e.getX();
            m_lastYMousePos = e.getY();
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            m_currMouseButton = 3;
            m_lastXMousePos = e.getX();
            m_lastYMousePos = e.getY();

            // AJK 04/27/08: for node context menus
            processNodeContextMenuEvent(e);
            processEdgeContextMenuEvent(e);
        }

        requestFocusInWindow();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (m_currMouseButton == 1) {
                m_currMouseButton = 0;

                if (m_selectionRect != null) {
                    int[] selectedNodes = null;
                    int[] selectedEdges = null;

                    synchronized (m_lock) {
                        if (m_view.m_nodeSelection || m_view.m_edgeSelection) {
                            if (m_view.m_nodeSelection) {
                                m_ptBuff[0] = m_selectionRect.x;
                                m_ptBuff[1] = m_selectionRect.y;
                                m_view.xformComponentToNodeCoords(m_ptBuff);

                                final double xMin = m_ptBuff[0];
                                final double yMin = m_ptBuff[1];
                                m_ptBuff[0] = m_selectionRect.x +
                                    m_selectionRect.width;
                                m_ptBuff[1] = m_selectionRect.y +
                                    m_selectionRect.height;
                                m_view.xformComponentToNodeCoords(m_ptBuff);

                                final double xMax = m_ptBuff[0];
                                final double yMax = m_ptBuff[1];
                                m_stack.empty();
                                m_view.getNodesIntersectingRectangle((float) xMin,
                                    (float) yMin, (float) xMax, (float) yMax,
                                    (m_lastRenderDetail &
                                    GraphRenderer.LOD_HIGH_DETAIL) == 0, m_stack);
                                m_stack2.empty();

                                final IntEnumerator nodesXSect = m_stack.elements();

                                while (nodesXSect.numRemaining() > 0) {
                                    final int nodeXSect = nodesXSect.nextInt();

                                    if (m_view.m_selectedNodes.count(~nodeXSect) == 0)
                                        m_stack2.push(nodeXSect);
                                }

                                selectedNodes = new int[m_stack2.size()];

                                final IntEnumerator nodes = m_stack2.elements();

                                for (int i = 0; i < selectedNodes.length;
                                        i++)
                                    selectedNodes[i] = nodes.nextInt();

                                for (int i = 0; i < selectedNodes.length;
                                        i++)
                                    ((DNodeView) m_view.getNodeView(selectedNodes[i])).selectInternal();

                                if (selectedNodes.length > 0)
                                    m_view.m_contentChanged = true;
                            }

                            if (m_view.m_edgeSelection) {
                                if ((m_lastRenderDetail &
                                        GraphRenderer.LOD_EDGE_ANCHORS) != 0) {
                                    m_ptBuff[0] = m_selectionRect.x;
                                    m_ptBuff[1] = m_selectionRect.y;
                                    m_view.xformComponentToNodeCoords(m_ptBuff);

                                    final double xMin = m_ptBuff[0];
                                    final double yMin = m_ptBuff[1];
                                    m_ptBuff[0] = m_selectionRect.x +
                                        m_selectionRect.width;
                                    m_ptBuff[1] = m_selectionRect.y +
                                        m_selectionRect.height;
                                    m_view.xformComponentToNodeCoords(m_ptBuff);

                                    final double xMax = m_ptBuff[0];
                                    final double yMax = m_ptBuff[1];
                                    final IntEnumerator hits = m_view.m_spacialA.queryOverlap((float) xMin,
                                            (float) yMin, (float) xMax,
                                            (float) yMax, null, 0, false);

                                    if (hits.numRemaining() > 0)
                                        m_view.m_contentChanged = true;

                                    while (hits.numRemaining() > 0) {
                                        final int hit = hits.nextInt();

                                        if (m_view.m_selectedAnchors.count(hit) == 0)
                                            m_view.m_selectedAnchors.insert(hit);
                                    }
                                }

                                computeEdgesIntersecting(m_selectionRect.x,
                                    m_selectionRect.y,
                                    m_selectionRect.x + m_selectionRect.width,
                                    m_selectionRect.y + m_selectionRect.height,
                                    m_stack2);
                                m_stack.empty();

                                final IntEnumerator edgesXSect = m_stack2.elements();

                                while (edgesXSect.numRemaining() > 0) {
                                    final int edgeXSect = edgesXSect.nextInt();

                                    if (m_view.m_selectedEdges.count(~edgeXSect) == 0)
                                        m_stack.push(edgeXSect);
                                }

                                selectedEdges = new int[m_stack.size()];

                                final IntEnumerator edges = m_stack.elements();

                                for (int i = 0; i < selectedEdges.length;
                                        i++)
                                    selectedEdges[i] = edges.nextInt();

                                for (int i = 0; i < selectedEdges.length;
                                        i++)
                                    ((DEdgeView) m_view.getEdgeView(selectedEdges[i])).selectInternal(true);

                                if (selectedEdges.length > 0)
                                    m_view.m_contentChanged = true;
                            }
                        }
                    }

                    m_selectionRect = null;

                    final GraphViewChangeListener listener = m_view.m_lis[0];

                    if (listener != null) {
                        if ((selectedNodes != null) &&
                                (selectedNodes.length > 0))
                            listener.graphViewChanged(
                                new GraphViewNodesSelectedEvent(m_view,
                                    selectedNodes));

                        if ((selectedEdges != null) &&
                                (selectedEdges.length > 0))
                            listener.graphViewChanged(
                                new GraphViewEdgesSelectedEvent(m_view,
                                    selectedEdges));
                    }

                    // Repaint after listener events are fired because listeners may
                    // change something in the graph view.
                    repaint();
                }
            }
        } else if (e.getButton() == MouseEvent.BUTTON2) {
            if (m_currMouseButton == 2)
                m_currMouseButton = 0;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (m_currMouseButton == 3)
                m_currMouseButton = 0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mouseDragged(MouseEvent e) {
        if (m_currMouseButton == 1) {
            if (m_button1NodeDrag) {
                synchronized (m_lock) {
                    m_ptBuff[0] = m_lastXMousePos;
                    m_ptBuff[1] = m_lastYMousePos;
                    m_view.xformComponentToNodeCoords(m_ptBuff);

                    final double oldX = m_ptBuff[0];
                    final double oldY = m_ptBuff[1];
                    m_lastXMousePos = e.getX();
                    m_lastYMousePos = e.getY();
                    m_ptBuff[0] = m_lastXMousePos;
                    m_ptBuff[1] = m_lastYMousePos;
                    m_view.xformComponentToNodeCoords(m_ptBuff);

                    final double newX = m_ptBuff[0];
                    final double newY = m_ptBuff[1];
                    final double deltaX = newX - oldX;
                    final double deltaY = newY - oldY;

                    // TODO: Optimize to not instantiate new array on every call.
                    final int[] selectedNodes = m_view.getSelectedNodeIndices();

                    for (int i = 0; i < selectedNodes.length; i++) {
                        final NodeView nv = m_view.getNodeView(selectedNodes[i]);
                        final double oldXPos = nv.getXPosition();
                        final double oldYPos = nv.getYPosition();
                        nv.setOffset(oldXPos + deltaX, oldYPos + deltaY);
                    }

                    final IntEnumerator anchorsToMove = m_view.m_selectedAnchors.searchRange(Integer.MIN_VALUE,
                            Integer.MAX_VALUE, false);

                    while (anchorsToMove.numRemaining() > 0) {
                        final int edgeAndAnchor = anchorsToMove.nextInt();
                        final int edge = edgeAndAnchor >>> 6;
                        final int anchorInx = edgeAndAnchor & 0x0000003f;
                        final DEdgeView ev = (DEdgeView) m_view.getEdgeView(~edge);
                        ev.getHandleInternal(anchorInx, m_floatBuff1);
                        ev.moveHandleInternal(anchorInx,
                            m_floatBuff1[0] + deltaX, m_floatBuff1[1] + deltaY);
                    }

                    if ((selectedNodes.length > 0) ||
                            (m_view.m_selectedAnchors.size() > 0))
                        m_view.m_contentChanged = true;
                }
            }

            if (m_selectionRect != null) {
                final int x = Math.min(
                        m_lastXMousePos,
                        e.getX());
                final int y = Math.min(
                        m_lastYMousePos,
                        e.getY());
                final int w = Math.abs(m_lastXMousePos - e.getX());
                final int h = Math.abs(m_lastYMousePos - e.getY());
                m_selectionRect.setBounds(x, y, w, h);
            }

            repaint();
        } else if (m_currMouseButton == 2) {
            double deltaX = e.getX() - m_lastXMousePos;
            double deltaY = e.getY() - m_lastYMousePos;
            m_lastXMousePos = e.getX();
            m_lastYMousePos = e.getY();

            synchronized (m_lock) {
                m_xCenter -= (deltaX / m_scaleFactor);
                m_yCenter -= (deltaY / m_scaleFactor);
            }

            m_view.m_viewportChanged = true;
            repaint();
        } else if (m_currMouseButton == 3) {
            double deltaY = e.getY() - m_lastYMousePos;

            synchronized (m_lock) {
                m_lastXMousePos = e.getX();
                m_lastYMousePos = e.getY();
                m_scaleFactor *= Math.pow(2, -deltaY / 300.0d);
            }

            m_view.m_viewportChanged = true;
            repaint();
        }
    }

    // AJK: 05/02/06 BEGIN
    public void mouseMoved(MouseEvent e) {
        NodeView nv = m_view.getPickedNodeView(e.getPoint());
        boolean toolTipSet = false;

        if (nv != null) {
            setToolTipText(((DNodeView) nv).getToolTip());
            toolTipSet = true;
        } else {
            if ((m_lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) != 0) {
                EdgeView ev = m_view.getPickedEdgeView(e.getPoint());

                if (ev != null) {
                    setToolTipText(((DEdgeView) ev).getToolTip());
                    toolTipSet = true;
                }
            }
        }

        if (!toolTipSet)
            setToolTipText(null);
    }

    /**
     * Handles key press events. Currently used with the up/down, left/right arrow
     * keys. Pressing any of the listed keys will move the selected nodes one pixel
     * in that direction.
     *
     * @param k The key event that we're listening for.
     */
    public void keyPressed(KeyEvent k) {
	int code = k.getKeyCode();
	if ( code == KeyEvent.VK_UP ||
	     code == KeyEvent.VK_DOWN ||
	     code == KeyEvent.VK_LEFT ||
	     code == KeyEvent.VK_RIGHT ) {
	     	double move = 1.0;
		if ( k.isShiftDown() )
			move = 10.0;
		if (m_view.m_nodeSelection) {
			int[] selectedNodes = m_view.getSelectedNodeIndices();
			for (int i = 0; i < selectedNodes.length; i++) {
				DNodeView nv = ((DNodeView) m_view.getNodeView(selectedNodes[i]));
				double xPos = nv.getXPosition();
				double yPos = nv.getYPosition();

				if ( code == KeyEvent.VK_UP ) {
					yPos -= move;
				} else if ( code == KeyEvent.VK_DOWN ) {
					yPos += move;
				} else if ( code == KeyEvent.VK_LEFT ) {
					xPos -= move;
				} else if ( code == KeyEvent.VK_RIGHT ) {
					xPos += move;
				}

				nv.setOffset(xPos,yPos);
			}

			repaint();
		}
	} 
    }

    /**
     * Currently not used. 
     *
     * @param k The key event that we're listening for.
     */
    public void keyReleased(KeyEvent k) { }

    /**
     * Currently not used. 
     *
     * @param k The key event that we're listening for.
     */
    public void keyTyped(KeyEvent k) { }

    // AJK: 05/02/06 END
    // Puts [last drawn] edges intersecting onto stack; as RootGraph indices.
    // Depends on the state of several member variables, such as m_hash.
    // Clobbers m_stack and m_ptBuff.
    // The rectangle extents are in component coordinate space.
    // IMPORTANT: Code that calls this method should be holding m_lock.
    final void computeEdgesIntersecting(final int xMini, final int yMini,
        final int xMaxi, final int yMaxi, final IntStack stack) {
        m_ptBuff[0] = xMini;
        m_ptBuff[1] = yMini;
        m_view.xformComponentToNodeCoords(m_ptBuff);

        final double xMin = m_ptBuff[0];
        final double yMin = m_ptBuff[1];
        m_ptBuff[0] = xMaxi;
        m_ptBuff[1] = yMaxi;
        m_view.xformComponentToNodeCoords(m_ptBuff);

        final double xMax = m_ptBuff[0];
        final double yMax = m_ptBuff[1];
        IntEnumerator edgeNodesEnum = m_hash.elements(); // Positive.
        m_stack.empty();

        final int edgeNodesCount = edgeNodesEnum.numRemaining();

        for (int i = 0; i < edgeNodesCount; i++)
            m_stack.push(edgeNodesEnum.nextInt());

        m_hash.empty();
        edgeNodesEnum = m_stack.elements();
        stack.empty();

        final FixedGraph graph = (FixedGraph) m_view.m_drawPersp;

        if ((m_lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0) {
            // We won't need to look up arrows and their sizes.
            for (int i = 0; i < edgeNodesCount; i++) {
                final int node = edgeNodesEnum.nextInt(); // Positive.

                if (!m_view.m_spacial.exists(node, m_view.m_extentsBuff, 0))
                    continue; /* Will happen if e.g. node was removed. */

                final float nodeX = (m_view.m_extentsBuff[0] +
                    m_view.m_extentsBuff[2]) / 2;
                final float nodeY = (m_view.m_extentsBuff[1] +
                    m_view.m_extentsBuff[3]) / 2;
                final IntEnumerator touchingEdges = graph.edgesAdjacent(node,
                        true, true, true);

                while (touchingEdges.numRemaining() > 0) {
                    final int edge = touchingEdges.nextInt(); // Positive.
                    final int otherNode =  // Positive.
                        node ^ graph.edgeSource(edge) ^ graph.edgeTarget(edge);

                    if (m_hash.get(otherNode) < 0) {
                        m_view.m_spacial.exists(otherNode,
                            m_view.m_extentsBuff, 0);

                        final float otherNodeX = (m_view.m_extentsBuff[0] +
                            m_view.m_extentsBuff[2]) / 2;
                        final float otherNodeY = (m_view.m_extentsBuff[1] +
                            m_view.m_extentsBuff[3]) / 2;
                        m_line.setLine(nodeX, nodeY, otherNodeX, otherNodeY);

                        if (m_line.intersects(xMin, yMin, xMax - xMin,
                                    yMax - yMin))
                            stack.push(~edge);
                    }
                }

                m_hash.put(node);
            }
        } else { // Last render high detail.

            for (int i = 0; i < edgeNodesCount; i++) {
                final int node = edgeNodesEnum.nextInt(); // Positive.

                if (!m_view.m_spacial.exists(node, m_view.m_extentsBuff, 0))
                    continue; /* Will happen if e.g. node was removed. */

                final byte nodeShape = m_view.m_nodeDetails.shape(node);
                final IntEnumerator touchingEdges = graph.edgesAdjacent(node,
                        true, true, true);

                while (touchingEdges.numRemaining() > 0) {
                    final int edge = touchingEdges.nextInt(); // Positive.
                    final double segThicknessDiv2 = m_view.m_edgeDetails.segmentThickness(edge) / 2.0d;
                    final int otherNode = node ^ graph.edgeSource(edge) ^
                        graph.edgeTarget(edge);

                    if (m_hash.get(otherNode) < 0) {
                        m_view.m_spacial.exists(otherNode, m_extentsBuff2, 0);

                        final byte otherNodeShape = m_view.m_nodeDetails.shape(otherNode);
                        final byte srcShape;
                        final byte trgShape;
                        final float[] srcExtents;
                        final float[] trgExtents;

                        if (node == graph.edgeSource(edge)) {
                            srcShape = nodeShape;
                            trgShape = otherNodeShape;
                            srcExtents = m_view.m_extentsBuff;
                            trgExtents = m_extentsBuff2;
                        } else { // node == graph.edgeTarget(edge).
                            srcShape = otherNodeShape;
                            trgShape = nodeShape;
                            srcExtents = m_extentsBuff2;
                            trgExtents = m_view.m_extentsBuff;
                        }

                        final byte srcArrow;
                        final byte trgArrow;
                        final float srcArrowSize;
                        final float trgArrowSize;

                        if ((m_lastRenderDetail &
                                GraphRenderer.LOD_EDGE_ARROWS) == 0) {
                            srcArrow = trgArrow = GraphGraphics.ARROW_NONE;
                            srcArrowSize = trgArrowSize = 0.0f;
                        } else {
                            srcArrow = m_view.m_edgeDetails.sourceArrow(edge);
                            trgArrow = m_view.m_edgeDetails.targetArrow(edge);
                            srcArrowSize = ((srcArrow == GraphGraphics.ARROW_NONE)
                                ? 0.0f
                                : m_view.m_edgeDetails.sourceArrowSize(edge));
                            trgArrowSize = (((trgArrow == GraphGraphics.ARROW_NONE) ||
                                (trgArrow == GraphGraphics.ARROW_MONO)) ? 0.0f
                                                                        : m_view.m_edgeDetails.targetArrowSize(edge));
                        }

                        final EdgeAnchors anchors = (((m_lastRenderDetail &
                            GraphRenderer.LOD_EDGE_ANCHORS) == 0) ? null
                                                                  : m_view.m_edgeDetails.anchors(edge));

                        if (!GraphRenderer.computeEdgeEndpoints(m_grafx,
                                    srcExtents, srcShape, srcArrow,
                                    srcArrowSize, anchors, trgExtents,
                                    trgShape, trgArrow, trgArrowSize,
                                    m_floatBuff1, m_floatBuff2))
                            continue;

                        m_grafx.getEdgePath(srcArrow, srcArrowSize, trgArrow,
                            trgArrowSize, m_floatBuff1[0], m_floatBuff1[1],
                            anchors, m_floatBuff2[0], m_floatBuff2[1], m_path);
                        GraphRenderer.computeClosedPath(
                            m_path.getPathIterator(null),
                            m_path2);

                        if (m_path2.intersects(xMin - segThicknessDiv2,
                                    yMin - segThicknessDiv2,
                                    (xMax - xMin) + (segThicknessDiv2 * 2),
                                    (yMax - yMin) + (segThicknessDiv2 * 2)))
                            stack.push(~edge);
                    }
                }

                m_hash.put(node);
            }
        }
    }

    // AJK: 04/02/06 BEGIN

    /**
     * default dragEnter handler.  Accepts the drag.
     * @param dte the DropTargetDragEvent
     *
     */
    public void dragEnter(java.awt.dnd.DropTargetDragEvent dte) {
        dte.acceptDrag(DnDConstants.ACTION_COPY);
    }

    /**
     * default dragExit handler.  Does nothing, can be overridden.
     * @param dte the DropTargetDragEvent
     *
     */
    public void dragExit(java.awt.dnd.DropTargetEvent dte) {
    }

    /**
     * default dropActionChanged handler.  Does nothing, can be overridden.
     * @param dte the DropTargetDragEvent
     *
     */
    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dte) {
    }

    /**
     * default dragOver handler.  Does nothing, can be overridden.
     * @param dte the DropTargetDragEvent
     *
     */
    public void dragOver(java.awt.dnd.DropTargetDragEvent dte) {
    }

    /**
     * default drop handler.  Accepts drop, builds a transferable, creates and
     * fires a PhoebeCanvasDropEvent, then calls dropComplete().
     * @param dte the DropTargetDragEvent
     *
     */
    public void drop(java.awt.dnd.DropTargetDropEvent dte) {
        dte.acceptDrop(DnDConstants.ACTION_COPY);

        Transferable t = dte.getTransferable();

        Point pt = dte.getLocation();

        PhoebeCanvasDropEvent event = new PhoebeCanvasDropEvent(this, // we are the event source
                t, // item dropped
                pt // location
            );
        processPhoebeCanvasDropEvent(event);

        dte.dropComplete(true);
    }

    /**
     * adds a listener to the store of PhoebeCanvasDropTargetListeners
     * @param l the PhoebeCanvasDropTargetListener
     *
     */
    public void addPhoebeCanvasDropListener(PhoebeCanvasDropListener l) {
        listeners.addElement(l);
    }

    /**
     * removes a listener from the store of PhoebeCanvasDropTargetListeners
     * @param l the PhoebeCanvasDropTargetListener
     *
     */
    public void removePhoebeCanvasDropListener(PhoebeCanvasDropListener l) {
        listeners.removeElement(l);
    }

    /**
     * handles a PhoebeCanvasDropEvent.  For each listerner, calls its itemDropped() method
     * @param event the PhoebeCanvasDropEvent
     *
     */
    protected synchronized void processPhoebeCanvasDropEvent(
        PhoebeCanvasDropEvent event) {
        Enumeration e = listeners.elements();

        while (e.hasMoreElements()) {
            PhoebeCanvasDropListener l = (PhoebeCanvasDropListener) e.nextElement();
            l.itemDropped(event);
        }
    }

    // AJK: 04/02/06 END
    // AJK: 04/27/06 BEGIN
    // for node context menus

    /**
     * adds a listener to the store of NodeContextMenuListeners
     * @param l the NodeContextMenuListener
     *
     */
    public void addNodeContextMenuListener(NodeContextMenuListener l) {
        nodeContextMenuListeners.addElement(l);
    }

    /**
     * removes a listener from the store of NodeContextMenuListeners
     * @param l the NodeContextMenuListener
     *
     */
    public void removeNodeContextMenuListener(NodeContextMenuListener l) {
        nodeContextMenuListeners.removeElement(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    public void addEdgeContextMenuListener(EdgeContextMenuListener l) {
        edgeContextMenuListeners.addElement(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    public void removeEdgeContextMenuListener(EdgeContextMenuListener l) {
        edgeContextMenuListeners.removeElement(l);
    }

    /**
     * handles a NodeContextMenuEvent.  For each listerner, calls its itemDropped() method
     * @param event the NodeContextMenuEvent
     *
     */
    protected synchronized void processNodeContextMenuEvent(MouseEvent event) {
        NodeView nv = m_view.getPickedNodeView(event.getPoint());

        //         System.out.println ("   over selected nodeview: " + nv);
        if (nv != null) {
            String nodeLabel = nv.getNode()
                                 .getIdentifier();
            JPopupMenu menu = new JPopupMenu(nodeLabel);
            menu.setLabel(nodeLabel);

            Enumeration e = nodeContextMenuListeners.elements();

            while (e.hasMoreElements()) {
                NodeContextMenuListener l = (NodeContextMenuListener) e.nextElement();
                System.out.println(
                    "Adding context menu items for NodeContextMenuListener: " +
                    l);

                //                              EventListener l = (EventListener) e.nextElement();
                l.addNodeContextMenuItems(nv, menu);
            }

            // Display PopupMenu
            menu.show(
                this,
                event.getX(),
                event.getY());
        }
    }

    protected synchronized void processEdgeContextMenuEvent(MouseEvent event) {
        EdgeView ev = m_view.getPickedEdgeView(event.getPoint());

        if (ev != null) {
            String edgeLabel = ev.getEdge()
                                 .getIdentifier();
            JPopupMenu menu = new JPopupMenu(edgeLabel);
            menu.setLabel(edgeLabel);

            Enumeration e = edgeContextMenuListeners.elements();

            while (e.hasMoreElements()) {
                EdgeContextMenuListener l = (EdgeContextMenuListener) e.nextElement();
                System.out.println(
                    "Adding context menu items for EdgeContextMenuListener: " +
                    l);
                l.addEdgeContextMenuItems(ev, menu);
            }

            // Display PopupMenu
            menu.show(
                this,
                event.getX(),
                event.getY());
        }
    }

    // AJK: 04/27/06 END
    public int getLastRenderDetail() {
        return m_lastRenderDetail;
    }
}
