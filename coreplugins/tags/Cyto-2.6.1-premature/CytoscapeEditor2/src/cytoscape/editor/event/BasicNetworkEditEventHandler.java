/* -*-Java-*-
********************************************************************************
*
* File:         BasicNetworkEditEventHandler.java
* RCS:          $Header: $
* Description:
* Author:       Allan Kuchinsky
* Created:      Fri Jul 31 05:36:07 2005
* Modified:     Thu Jul 10 08:43:53 2008 (Michael L. Creech) creech@w235krbza760
* Language:     Java
* Package:
* Status:       Experimental
*
* (c) Copyright 2006, Agilent Technologies, all rights reserved.
*
********************************************************************************
*
* Revisions:
*
* Wed Jul 09 09:33:32 2008 (Michael L. Creech) creech@w235krbza760
*  Added checks that Editor component is active to mouse and key event processing
*  to avoid handling events when the editor tab isn't the current tab. Fixed
*  "ESC" key to correctly stop drop of an edge.
* Thu May 10 10:02:48 2007 (Michael L. Creech) creech@w235krbza760
*  Commented out various unused variables and removed unused imports.
* Fri Dec 08 05:37:12 2006 (Michael L. Creech) creech@w235krbza760
*  Broke finishEdge() into smaller pieces for subclass usage. Cleaned
*  up mousePressed() and a few protected instance variables that
*  should be private.
********************************************************************************
*/
package cytoscape.editor.event;

import ding.view.DGraphView;
import ding.view.DingCanvas;
import ding.view.InnerCanvas;

import giny.model.Node;
import giny.view.EdgeView;
import giny.view.NodeView;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import cytoscape.CyEdge;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.editor.CytoscapeEditor;
import cytoscape.editor.CytoscapeEditorManager;
import cytoscape.editor.editors.BasicCytoscapeEditor;
import cytoscape.editor.impl.SIF_Interpreter;
import cytoscape.view.CyNetworkView;


// TODO: No instance variables should be protected--only private and
//       all access by subclasses should be thru set/get methods.
//       Also, most of the methods only accessed thru sublasses should
//       be protected, not public.

/**
 *
 * The <b>BasicNetworkEditEventHandler </b> class provides specialized methods
 * defining the behavior of the basic Cytoscape editor provided in Cytoscape
 * 2.2. The behavior is defined in terms of how the event handler responds to
 * mouse events, drag/drop events, and button press events.
 *
 * @author Allan Kuchinsky
 * @version 1.0
 * @see BasicCytoscapeEditor
 *
 * revised: 04/15/2006 to integrate with Cytoscape 2.3 renderer Phase 1: switch
 * underlying node identification and edge drawing code Phase 2: remove
 * dependencies upon Piccolo
 *
 */
public class BasicNetworkEditEventHandler extends NetworkEditEventAdapter implements ActionListener, cytoscape.data.attr.MultiHashMapListener //TODO: dont need MultiHashMapListener
 {
	// PNodeLocator locator;

	/**
	 * counter variable used in setting unique names for nodes
	 */
	protected static int counter = 0;

	/**
	 * CytoscapeAttribute: NODE_TYPE
	 */
	public static final String NODE_TYPE = "NODE_TYPE";

	/**
	 * CytoscapeAttribute: EDGE_TYPE
	 *
	 */
	public static final String EDGE_TYPE = "EDGE_TYPE";

	/**
	 * 
	 */
	public static final String DEFAULT_NODE = "DefaultNode";

	/**
	 * 
	 */
	public static final String DEFAULT_EDGE = "DefaultEdge";

	/**
	 * the node that will be dropped
	 */

	// MLC 12/07/06 BEGIN:
	// FIX: Should really change the name--this can easily be shadowed by other
	//      local variables:
	// protected NodeView node;
	private NodeView node;

	// MLC 12/07/06 END.

	/**
	 * the edge that will be dropped
	 */

	// MLC 12/07/06 END.

	/**
	 * flag that indicates whether there is an edge under construction
	 */

	// MLC 12/07/06:
	// protected boolean edgeStarted;
	// MLC 12/07/06:
	private boolean edgeStarted;

	/**
	 * the mouse press location for the drop point
	 */

	// MLC 12/07/06:
	// protected Point2D startPoint;
	// MLC 12/07/06:
	private Point2D startPoint;

	/**
	 * point used in tracking mouse movement
	 */
	protected Point2D nextPoint;

	/**
	 * the canvas that this event handler is listening to
	 */

	// AJK: 04/15/06 go from PCanvas to DING InnerCanvas
	// protected PCanvas canvas;
	protected InnerCanvas canvas;

	/**
	 * the current network view
	 */

	// AJK: 04/15/06 go from PGraphView to DGraphView
	// protected PGraphView view;
	protected DGraphView view;

	/**
	 * attribute used to set NODE_TYPE
	 */
	protected String nodeAttributeName = NODE_TYPE;

	/**
	 * value for attribute used in setting NODE_TYPE
	 */
	protected String nodeAttributeValue = DEFAULT_NODE;

	/**
	 * attribute used to set EDGE_TYPE
	 */
	protected String edgeAttributeName = EDGE_TYPE;

	/**
	 * value for attribute used in setting EDGE_TYPE
	 */
	protected String edgeAttributeValue = DEFAULT_EDGE;

	/**
	 * editor that this event handler is associated with
	 */
	CytoscapeEditor _caller;

	/*
	 * for drawing rubberbanded lines
	 */
	double saveX1 = Double.MIN_VALUE;
	double saveY1 = Double.MIN_VALUE;
	double saveX2 = Double.MIN_VALUE;
	double saveY2 = Double.MIN_VALUE;

	/**
	 * flag that indicates whether we are currently in the process of handling a
	 * dropped edge TODO: handling dropped edges should probably be moved to the
	 * PaletteNetworkEditEventHandler
	 */
	public boolean handlingEdgeDrop = false;
	
	/**
	 * String used to compare against os.name System property -
	 * to determine if we are running on Windows platform.
	 */
	static final String MAC_OS_ID = "mac";

	/**
	 * node or edge which has been highlighted for drop or edge connection
	 * during mouseDrag
	 */
	// MLC 05/10/07:
	// private NodeView _highlightedNodeView = null;
	// MLC 05/10/07:
	// private EdgeView _highlightedEdgeView = null;

	// private float _savedStrokeWidth = Float.NaN;
	// MLC 05/10/07:
	// private Cursor _savedCursor = null;

	/**
	 * Creates a new BasicNetworkEditEventHandler object.
	 */
	public BasicNetworkEditEventHandler() {
	}

	/**
	 *
	 * @param caller
	 */
	public BasicNetworkEditEventHandler(CytoscapeEditor caller) {
		this();
		_caller = caller;
	}

	/**
	 *
	 * @param caller
	 * @param view
	 */
	public BasicNetworkEditEventHandler(CytoscapeEditor caller, CyNetworkView view) {
		this();
		_caller = caller;
		this.setView((DGraphView) view);
	}

	/**
	 *
	 */

	// public PCanvas getCanvas() {
	public InnerCanvas getCanvas() {
		return canvas;
	}
	
	/**
	 * Routine which determines if we are running on mac platform
	 *
	 * @return boolean
	 */
	private boolean isMacPlatform() {
		String os = System.getProperty("os.name");

		return os.regionMatches(true, 0, MAC_OS_ID, 0, MAC_OS_ID.length());
	}

	/**
	 * The <b>mousePressed() </b> method is at the heart of the basic Cytoscape
	 * editor.
	 * <p>
	 * Control-clicking at a position on the canvas creates a node with default
	 * label in that position.
	 * <p>
	 * Control-clicking on a node on the canvas starts an edge with source at
	 * that node. Move the cursor and a rubber-banded line follows the cursor.
	 * As the cursor passes over another node, that node is highlighted and the
	 * rubber-banded line will snap to a connection point on that second node.
	 * Control-click the mouse again and the connection is established.
	 *
	 * @param e
	 *            inputEvent for mouse pressed
	 * @see BasicCytoscapeEditor
	 */
	public void mousePressed(MouseEvent e) {
	    // MLC 07/09/08 BEGIN:
	    // TODO: This check should really be avoided by having the editor remove all mouse and key
	    //       listeners when the editor looses focus (another tab is clicked on).
	    //       Since this is somewhat involved and so is left for when the editor is refactored.
	    if (!CytoscapeEditorManager.isEditorInOperation()) {
		return;
	    }
	    CytoscapeEditorManager.log("CE: mousePressed!");
	    // MLC 07/09/08 END.

		nextPoint = e.getPoint();

		NodeView nv = view.getPickedNodeView(nextPoint);
		boolean onNode = (nv != null);


		// if we have control-clicked on an edge, then just return
		// because the user is adding edge anchors for bending edges in
		// Cytoscape:
		if (e.isControlDown() && !(isMacPlatform())) {
			if (view.getPickedEdgeView(nextPoint) != null) {
				return;
			}
		}

		if (onNode && !edgeStarted && (e.isControlDown()) && !(isMacPlatform())) {
			// begin edge creation
			beginEdge(nextPoint, nv);

		} else if (onNode && edgeStarted) {
			CytoscapeEditorManager.log("calling finishEdge for NodeView " + nv);
			// Finish Edge Creation
			finishEdge(nextPoint, nv);

		} else if (!onNode && edgeStarted) // turn off rubberbanding if clicked
		                                   // on empty area of canvas
		 {
			edgeStarted = false;
			saveX1 = Double.MIN_VALUE;
			saveX2 = Double.MIN_VALUE;
			saveY1 = Double.MIN_VALUE;
			saveY2 = Double.MIN_VALUE;
			this.setHandlingEdgeDrop(false);
		} else if (!onNode && !edgeStarted && (e.isControlDown()) && !(isMacPlatform())) {
			// control-click on a empty place will make a new Node:
			createNode(nextPoint);
		}
		
		//    invoke SIF interpreter for user to enter nodes/edges via text input
		else if ((e.getClickCount() == 2) && (!e.isAltDown()))
		{
			SIF_Interpreter.processInput(e.getPoint(), _caller);
		}
		
		// AJK: 12/06/06 BEGIN
		//    toggle diagnostic logging with alt_triple-click
		else if ((e.getClickCount() > 2) && (e.isAltDown())) {
			CytoscapeEditorManager.setLoggingEnabled(!CytoscapeEditorManager.isLoggingEnabled());
			CytoscapeEditorManager.log("Cytoscape editor logging = "
			                           + CytoscapeEditorManager.isLoggingEnabled());
		}

		else 
		 {
			//			super.mousePressed(e);
		}
	}

	/**
	 * processed keyTypedEvents, in particular use of ESC key to interupt edge drawing
	 */
     // MLC 07/09/08:
     // Actually, keyTyped only returns a char via e.getKeyChar(), keyPressed() is correct for the given code.
	// public void keyTyped(KeyEvent e) // TODO: keyPressed does not seem to be working
     // MLC 07/09/08:
	public void keyPressed(KeyEvent e)
	 {
	    // MLC 07/09/08 BEGIN:
	    // TODO: This check should really be avoided by having the editor remove all mouse and key
	    //       listeners when the editor looses focus (another tab is clicked on).
	    //       Since this is somewhat involved and so is left for when the editor is refactored.
	    if (!CytoscapeEditorManager.isEditorInOperation()) {
		return;
	    }
	    // MLC 07/09/08 END.
		int keyVal = e.getKeyCode();
		CytoscapeEditorManager.log("Key code for typed key = " + keyVal);
		CytoscapeEditorManager.log("VK_ESCAPE = " + KeyEvent.VK_ESCAPE);
		if (keyVal == KeyEvent.VK_ESCAPE) {
			if (edgeStarted) // turn off rubberbanding if clicked
			                 // on empty area of canvas
			 {
				edgeStarted = false;
				saveX1 = Double.MIN_VALUE;
				saveX2 = Double.MIN_VALUE;
				saveY1 = Double.MIN_VALUE;
				saveY2 = Double.MIN_VALUE;
				this.setHandlingEdgeDrop(false);
				// MLC 07/09/08 BEGIN:
				// repaint so that the rubberband line is removed:
				this.getCanvas().repaint();
				// MLC 07/09/08 END.
			}
		}
	}

	/**
	 * begin drawing an edge from the input point
	 *
	 *
	 * @param location   works in Canvas coordinates
	 *
	 */
	public void beginEdge(Point2D location, NodeView nv) {
		edgeStarted = true;
		// node = (NodeView) e.getPickedNode();
		node = nv;
		startPoint = location;
		updateEdge();
		setEdgeStarted(true);
		setStartPoint(startPoint);
	}

	/**
	 * finish edge on node containing input pointf
	 *
	 * @param location works in Canvas coordinates
	 */
	public CyEdge finishEdge(Point2D location, NodeView target) {
		// CytoscapeEditorManager.log("finishEdge in BasicNetworkEventHandler");
		// MLC 12/07/06 BEGIN:
		//        edgeStarted = false;
		//        updateEdge();
		//
		//        saveX1 = Double.MIN_VALUE;
		//        saveX2 = Double.MIN_VALUE;
		//        saveY1 = Double.MIN_VALUE;
		//        saveY2 = Double.MIN_VALUE;
		// MLC 12/07/06 END.
		NodeView source = node;

		Node source_node = source.getNode();
		Node target_node = target.getNode();

		CyEdge myEdge = _caller.addEdge(source_node, target_node,
		                                cytoscape.data.Semantics.INTERACTION,
		                                (this.getEdgeAttributeValue() != null)
		                                ? this.getEdgeAttributeValue()
		                                : BasicNetworkEditEventHandler.DEFAULT_EDGE, true,
		                                (this.getEdgeAttributeValue() != null)
		                                ? this.getEdgeAttributeValue()
		                                : BasicNetworkEditEventHandler.DEFAULT_EDGE);
		// MLC 12/07/06 BEGIN:
		// AJK: 11/19/05 invert selection of target, which will have had its
		// selection inverted upon mouse entry
		// AJK: 12/09/06 comment out the toggling of selection, due to bug caused
		//        target.setSelected(!target.isSelected());
		completeFinishEdge();

		//        edge = null;
		//        node = null;
		//
		//        if (isHandlingEdgeDrop()) {
		//            this.setHandlingEdgeDrop(false);
		//        }
		//
		//        // AJK: 11/19/05 invert selection of target, which will have had its
		//        // selection inverted upon mouse entry
		//        target.setSelected(!target.isSelected());
		//
		//        // AJK: 11/18/2005 invert selection of any nodes/edges that have been highlighted
		//        invertSelections(null);
		//
		//        this.getCanvas().repaint();
		//
		//        // redraw graph so that the correct arrow is shown (but only if network
		//        // is small enough to see the edge...
		//        // NOTE: this is not needed
		//        if (Cytoscape.getCurrentNetwork().getNodeCount() <= 500) {
		//            Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
		//        }
		// MLC 12/07/06 END.
		return myEdge;
	}

	// MLC 12/07/06 BEGIN:
	/**
	 * Perform all cleanup and refresh activities to complete
	 * finishEdge().
	 */
	protected void completeFinishEdge() {
		edgeStarted = false;
		updateEdge();

		saveX1 = Double.MIN_VALUE;
		saveX2 = Double.MIN_VALUE;
		saveY1 = Double.MIN_VALUE;
		saveY2 = Double.MIN_VALUE;

		node = null;

		if (isHandlingEdgeDrop()) {
			this.setHandlingEdgeDrop(false);
		}

		// AJK: 11/18/2005 invert selection of any nodes/edges that have been highlighted
		// AJK: 12/09/06 comment out the toggling of selection, due to bug caused
		//        invertSelections(null);
		this.getCanvas().repaint();

		// redraw graph so that the correct arrow is shown (but only if network
		// is small enough to see the edge...
		// NOTE: this is not needed
		if (Cytoscape.getCurrentNetwork().getNodeCount() <= 500) {
			Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
		}
	}

	// MLC 12/07/06 END.

	/**
	 * create a new node at the point where mouse was pressed
	 *
	 * @param location    point of mouse press (in Canvas coordinates)
	 */
	public CyNode createNode(Point2D location) {
		CyNode cn = null;
		cn = _caller.addNode("node" + counter, this.getNodeAttributeName(),
		                     this.getNodeAttributeValue(), location);
		counter++;

		return cn;
	}

	/**
	 * updates rendering of edge if an edge is under construction
	 */
	public void mouseMoved(MouseEvent e) {
	    // MLC 07/09/08 BEGIN:
	    // TODO: This check should really be avoided by having the editor remove all mouse and key
	    //       listeners when the editor looses focus (another tab is clicked on).
	    //       Since this is somewhat involved and so is left for when the editor is refactored.
	    if (!CytoscapeEditorManager.isEditorInOperation()) {
		return;
	    }
	    // CytoscapeEditorManager.log("CE: mouseMoved!");
	    // MLC 07/09/08 END.
		nextPoint = e.getPoint();

		if (edgeStarted) {
			// we need to update the latest section of the edge
			updateEdge();

			// if over NodeView or EdgeView, then highlight
			// AJK: 12/09/06 comment out the toggling of selection, due to bug caused
			NodeView nv = view.getPickedNodeView(nextPoint);
			EdgeView ev = view.getPickedEdgeView(nextPoint);

			if ((nv != null) || (ev != null)) {
				//                invertSelections(nv);
				//                } else if (ev != null) {
				//                invertSelections(ev);
			} else {
				//                invertSelections(null);
			}
		}
	}

	/**
	 * if hovering over a node, then highlight the node by temporarily
	 * inverting its selection
	 *
	 *
	 */

	// TODO: this doesn't work because we are entering Canvas, NOT nodeview
	public void mouseEntered(MouseEvent e) {
		// AJK: 12/09/06 comment out the toggling of selection, due to bug caused
		//        Point2D  location = e.getPoint();
		//        NodeView nv = view.getPickedNodeView(location);
		//
		//        if (nv != null) {
		//            if (edgeStarted) {
		//                nv.setSelected(!nv.isSelected());
		//            }
		//
		//            this.getCanvas().repaint();
		//        }
	}

	/**
	 * revert temporary node highlighting that was done upon MouseEnter
	 */
	public void mouseExited(MouseEvent e) {
		// AJK: 12/09/06 comment out the toggling of selection, due to bug caused
		//        Point2D  location = e.getPoint();
		//        NodeView nv = view.getPickedNodeView(location);
		//
		//        if (nv != null) {
		//            if (edgeStarted) {
		//                nv.setSelected(!nv.isSelected());
		//            }
		//
		//            this.getCanvas().repaint();
		//        }
	}

	/**
	 * begin or continue drawing an edge as mouse is dragged
	 */
	public void mouseDragged(MouseEvent e) {
	    // MLC 07/09/08 BEGIN:
	    // TODO: This check should really be avoided by having the editor remove all mouse and key
	    //       listeners when the editor looses focus (another tab is clicked on).
	    //       Since this is somewhat involved and so is left for when the editor is refactored.
	    if (!CytoscapeEditorManager.isEditorInOperation()) {
		return;
	    }
	    // MLC 07/09/08 END.
		nextPoint = e.getPoint();

		boolean onNode = false;
		Point2D location = e.getPoint();
		NodeView nv = view.getPickedNodeView(location);
		// MLC 05/10/07:
		// EdgeView ev = view.getPickedEdgeView(location);

		// if over NodeView or EdgeView, then highlight
		// AJK: 12/09/06 comment out the toggling of selection, due to bug caused
		//       if (nv != null) {
		//            invertSelections(nv);
		//        } else if (ev != null) {
		//            invertSelections(ev);
		//        } else {
		//            invertSelections(null);
		//        }
		if (nv != null) {
			onNode = true;
		}

		if (onNode && !edgeStarted && (e.isControlDown() && !(isMacPlatform()))) {
			// begin edge creation
			beginEdge(nextPoint, nv);
		}

		if (!edgeStarted) {
			// super.mouseDragged(e);
		}

		if (edgeStarted) {
			// we need to update the latest section of the edge
			updateEdge();
		}
	}

	// AJK: 12/09/06 comment out the toggling of selection, due to bug caused
	/*   private void invertSelections(Object nodeOrEdgeView) {
	    if (nodeOrEdgeView == null) // we have moved off a node or edge
	     {
	        if (_highlightedEdgeView != null) {
	            _highlightedEdgeView.setSelected(!_highlightedEdgeView.isSelected());
	            //                   if (_savedStrokeWidth != Float.NaN)
	            //                {
	            //                    _highlightedEdgeView.setStrokeWidth(_savedStrokeWidth);
	            //                }
	            _highlightedEdgeView = null;
	        }

	        if (_highlightedNodeView != null) {
	            _highlightedNodeView.setSelected(!_highlightedNodeView.isSelected());
	            _highlightedNodeView = null;
	        }

	        if (_savedCursor != null) {
	            Cytoscape.getDesktop().setCursor(_savedCursor);
	        }
	    } else if (nodeOrEdgeView instanceof NodeView) {
	        NodeView nv = (NodeView) nodeOrEdgeView;

	        if (_highlightedEdgeView != null) {
	            _highlightedEdgeView.setSelected(!_highlightedEdgeView.isSelected());
	            //                   if (_savedStrokeWidth != Float.NaN)
	            //                {
	            //                    _highlightedEdgeView.setStrokeWidth(_savedStrokeWidth);
	            //                    Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
	            //                }
	            //                   _highlightedEdgeView = null;
	        }

	        if (_highlightedNodeView != null) {
	            _highlightedNodeView.setSelected(!_highlightedNodeView.isSelected());
	        }

	        _highlightedNodeView = nv;
	        nv.setSelected(!nv.isSelected());
	        CytoscapeEditorManager.log("Hovering near: " + nv +
	                                   " setting cursor to " +
	                                   Cursor.HAND_CURSOR);
	        _savedCursor = Cytoscape.getDesktop().getCursor();
	        Cytoscape.getDesktop()
	                 .setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    } else if (nodeOrEdgeView instanceof EdgeView) {
	        EdgeView ev = (EdgeView) nodeOrEdgeView;

	        if (_highlightedNodeView != null) {
	            _highlightedNodeView.setSelected(!_highlightedNodeView.isSelected());
	            _highlightedNodeView = null;
	        }

	        if (_highlightedEdgeView != null) {
	            _highlightedEdgeView.setSelected(!_highlightedEdgeView.isSelected());
	            //                 if (_savedStrokeWidth != Float.NaN)
	            //                {
	            //                    _highlightedEdgeView.setStrokeWidth(_savedStrokeWidth);
	            //                    Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
	            //                }
	        }

	        _highlightedEdgeView = ev;
	        ev.setSelected(!ev.isSelected());
	        //            _savedStrokeWidth = ev.getStrokeWidth();
	        //            ev.setStrokeWidth(4.0f);
	        _savedCursor = Cytoscape.getDesktop().getCursor();
	        CytoscapeEditorManager.log("Hovering near: " + ev +
	                                   " setting cursor to " +
	                                   Cursor.HAND_CURSOR);
	        Cytoscape.getDesktop()
	                 .setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    }
	}
	*/

	/**
	     * updates the rubberbanded edge line as the mouse is moved, works in Canvas coordinates
	     */
	public void updateEdge() {
		double x1 = startPoint.getX();
		double y1 = startPoint.getY();
		double x2 = nextPoint.getX();
		double y2 = nextPoint.getY();
		double lineLen = Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
		double offset = 5;

		if (lineLen == 0) {
			lineLen = 1;
		}

		y2 = y2 + (((y1 - y2) / lineLen) * offset);
		x2 = x2 + (((x1 - x2) / lineLen) * offset);

		nextPoint.setLocation(x2, y2);

		Graphics g = canvas.getGraphics();

		Color saveColor = g.getColor();

		if (saveX1 != Double.MIN_VALUE) {
			// AJK: 11/07/06 BEGIN
			//    fix for fanout bug
			//			canvas.getGraphics().setColor(canvas.getBackground());
			DGraphView dnv = (DGraphView) Cytoscape.getCurrentNetworkView();
			DingCanvas backgroundCanvas = dnv.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS);
			g.setColor(backgroundCanvas.getBackground());
			// AJK: 11/04/06 END
			g.drawLine(((int) saveX1) - 1, ((int) saveY1) - 1, ((int) saveX2) + 1,
			           ((int) saveY2) + 1);
		}

		canvas.update(g);
		g.setColor(Color.BLACK);
		g.drawLine(((int) x1) - 1, ((int) y1) - 1, ((int) x2) + 1, ((int) y2) + 1);
		g.setColor(saveColor);

		saveX1 = x1;
		saveX2 = x2;
		saveY1 = y1;
		saveY2 = y2;
	}

	/**
	 *
	 * MultiHashMapListener methods
	 *
	 */
	public void attributeValueAssigned(java.lang.String objectKey, java.lang.String attributeName,
	                                   java.lang.Object[] keyIntoValue,
	                                   java.lang.Object oldAttributeValue,
	                                   java.lang.Object newAttributeValue) {
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param objectKey DOCUMENT ME!
	 * @param attributeName DOCUMENT ME!
	 * @param keyIntoValue DOCUMENT ME!
	 * @param attributeValue DOCUMENT ME!
	 */
	public void attributeValueRemoved(java.lang.String objectKey, java.lang.String attributeName,
	                                  java.lang.Object[] keyIntoValue,
	                                  java.lang.Object attributeValue) {
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param objectKey DOCUMENT ME!
	 * @param attributeName DOCUMENT ME!
	 */
	public void allAttributeValuesRemoved(java.lang.String objectKey, java.lang.String attributeName) {
	}

	/**
	 * @return flag indicating whether an edge is under construction
	 */
	public boolean isEdgeStarted() {
		return edgeStarted;
	}

	/**
	 * set the flag that indicates whether an edge is under construction
	 *
	 * @param edgeStarted
	 *
	 */
	public void setEdgeStarted(boolean edgeStarted) {
		this.edgeStarted = edgeStarted;
	}

	/**
	 * @return Returns the nextPoint.
	 */
	public Point2D getNextPoint() {
		return nextPoint;
	}

	/**
	 * @param nextPoint
	 *            The nextPoint to set
	 *
	 */
	public void setNextPoint(Point2D nextPoint) {
		this.nextPoint = nextPoint;
	}

	/**
	 * @return Returns the node.
	 */
	public NodeView getNode() {
		return node;
	}

	/**
	 * @param node
	 *            The node to set.
	 *
	 */
	public void setNode(NodeView node) {
		this.node = node;
	}

	/**
	 * @return Returns the startPoint.
	 */
	public Point2D getStartPoint() {
		return startPoint;
	}

	/**
	 * @param startPoint
	 *            The startPoint to set.
	 *
	 */
	public void setStartPoint(Point2D startPoint) {
		this.startPoint = startPoint;
	}

	/**
	 * @return Returns the view.
	 */

	// AJK: 04/15/06 for Cytoscape 2.3 renderer
	// public PGraphView getView() {
	public DGraphView getView() {
		return view;
	}

	/**
	 * @param view
	 *            The view to set.
	 *
	 */

	// AJK: 04/15/06 for Cytoscape 2.3 renderer
	// public void setView(PGraphView view) {
	public void setView(DGraphView view) {
		this.view = view;
	}

	/**
	 * @return Returns the flag that indicates whether we are handling the drop
	 *         of an edge onto the canvas TODO: move edge drop handling into
	 *         PaletteNetworkEditEventHandler
	 */
	public boolean isHandlingEdgeDrop() {
		return handlingEdgeDrop;
	}

	/**
	 * @param handlingEdgeDrop
	 *            sets the flag that indicates whether we are handling the drop
	 *            of an edge onto the canvas
	 *
	 *
	 */
	public void setHandlingEdgeDrop(boolean handlingEdgeDrop) {
		this.handlingEdgeDrop = handlingEdgeDrop;
	}

	/**
	 * starts up the event handler on the input network view adds an input event
	 * listener to the view's canvas
	 *
	 * @param view
	 *            a Cytoscape network view
	 */

	// AJK: 04/15/06 for Cytoscape 2.3 renderer
	// public void start(PGraphView view) {
	public void start(DGraphView view) {
		this.view = view;
		this.canvas = view.getCanvas();
		// canvas.addInputEventListener(this);
		CytoscapeEditorManager.log("Started event listener: " + this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addKeyListener(this);
	}

	/**
	 * stops the event handler by removing the input event listener from the
	 * canvas this is called when the user switches between editors
	 *
	 */
	public void stop() {
		if (canvas != null) {
			// AJK: 04/15/06 for Cytoscape 2.3 renderer
			// canvas.removeInputEventListener(this);
			//			CytoscapeEditorManager.log("stopped event listener: " + this);
		    canvas.removeMouseListener(this);
		    canvas.removeMouseMotionListener(this);
		    canvas.removeKeyListener(this);
		    this.view = null;
		    this.canvas = null;
		}
	}

	/**
	 * @return Returns the edgeAttributeValue.
	 */
	public String getEdgeAttributeValue() {
		return edgeAttributeValue;
	}

	/**
	 * @param edgeAttributeValue
	 *            The edgeAttributeValue to set.
	 */
	public void setEdgeAttributeValue(String edgeAttributeValue) {
		this.edgeAttributeValue = edgeAttributeValue;
	}

	/**
	 * @return Returns the nodeAttributeName.
	 */
	public String getNodeAttributeName() {
		return nodeAttributeName;
	}

	/**
	 * @param nodeAttributeName
	 *            The nodeAttributeName to set.
	 */
	public void setNodeAttributeName(String nodeAttributeName) {
		this.nodeAttributeName = nodeAttributeName;
	}

	/**
	 * @return Returns the edgeAttributeName.
	 */
	public String getEdgeAttributeName() {
		return edgeAttributeName;
	}

	/**
	 * @param edgeAttributeName
	 *            The edgeAttributeName to set.
	 */
	public void setEdgeAttributeName(String edgeAttributeName) {
		this.edgeAttributeName = edgeAttributeName;
	}

	/**
	 * @return Returns the nodeAttributeValue.
	 */
	public String getNodeAttributeValue() {
		return nodeAttributeValue;
	}

	/**
	 * @param nodeAttributeValue
	 *            The nodeAttributeValue to set.
	 */
	public void setNodeAttributeValue(String nodeAttributeValue) {
		this.nodeAttributeValue = nodeAttributeValue;
	}

	/**
	 * @return Returns the _caller.
	 */
	public CytoscapeEditor get_caller() {
		return _caller;
	}

	/**
	 * @param _caller
	 *            The _caller to set.
	 */
	public void set_caller(CytoscapeEditor _caller) {
		this._caller = _caller;
	}
}
