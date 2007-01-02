package cytoscape.bubbleRouter;

import giny.model.Edge;
import giny.model.Node;
import giny.view.EdgeView;
import giny.view.NodeView;

import java.awt.event.ActionEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JOptionPane;
import javax.swing.undo.AbstractUndoableEdit;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;

/**
 * Rearranges the set of selected nodes in a way that minimizes edge crossings.
 * Uses a very simple method. 1. randomly order the list of nodes 2. select the
 * first node in the list. 3. for each subsequent node in the list, calculate
 * number of edge crossings before and after a possible permutation, as follows
 * as follows: a. for each edge incident to each node, (1) for each edge in the
 * network, see if the two edges intersect b. total the intersections for all
 * incident edges, before and after permutation 4. permute the node with the
 * node in the list that has resulted in largest drop between before and after
 * calculation of edge crossings. 5. increment iterations counter. 6. if
 * iterations counter > threshold, then done. repeat at 3 using the node that
 * has just been switched into first position
 * 
 * @author Allan Kuchinsky, Agilent Technologies
 * 
 */

class UnCrossAction extends CytoscapeAction {



	private static List _selectedNodes;

	/**
	 * Array of NodeViews
	 */
	private static NodeView[] _nodeViews;

	/**
	 * Array of EdgeViews
	 */
	private static EdgeView[] _edgeViews;

	/**
	 * Data structures containing totals for before/after edge crossings should
	 * be same size and parallel to the NodeViews array
	 */

	/**
	 * edge crossings for all nodes before swap
	 */
	private static int[] _beforeEdgeCrossings;

	/**
	 * edge crossings for all nodes after swap
	 */
	private static int[] _afterEdgeCrossings;

	/**
	 * totals of edge crossings after shift
	 */
	private static int[] _totalEdgeCrossings;

	/**
	 * for undo/redo of uncrossing
	 */
	private static Point2D[] _undoOffsets;

	private static Point2D[] _redoOffsets;

	/**
	 * index position of node to swap with candidate
	 */
	private static int _index_of_other;

	/**
	 * index position of candidate node to swap
	 */
	private static int _index_of_candidate;

	/**
	 * Nodes for candidate and other node, for swapping
	 */
	private static Node _candidate_node = null;

	private static Node _other_node = null;

	/**
	 * number of edge crossings for best fit so far
	 */
	private static int _nbr_crossings_for_best_fit;

	private static int _index_for_best_fit;

	/**
	 * before and after positions for candidate and other node
	 */
	static Point2D _candidate_before_locn = null;

	static Point2D _candidate_after_locn = null;

	static Point2D _other_before_locn = null;

	static Point2D _other_after_locn = null;

	static Point2D _tmpPoint;

	/**
	 * 
	 */

	/**
	 * Cytoscape working objects
	 */
	private static CyNetwork _net;

	private static CyNetworkView _view;

	/**
	 * number of iterations before quitting
	 */
	// for debugging set to 1
	private static final int ITERATION_LIMIT = 10; // play with this via

	// heuristics on performance

	private static int iteration = 0;

	public UnCrossAction() {
		super("Minimize Edge Crossings for Selected Nodes");
		setPreferredMenu("Layout");
		setAcceleratorCombo(java.awt.event.KeyEvent.VK_U,
				ActionEvent.ALT_MASK);
	}

	public UnCrossAction(boolean label) {
		super();
	}

	public void actionPerformed(ActionEvent e) {
		unCross(Cytoscape.getCurrentNetworkView().getSelectedNodes());
	}

	public List get_selectedNodes() {
		return _selectedNodes;
	}

	public void set_selectedNodes(List nodes) {
		_selectedNodes = nodes;
	}

	public void unCross(List nodes) {

		Task unCrossTask = new UnCrossTask(nodes);
		JTaskConfig jTaskConfig = new JTaskConfig();
		jTaskConfig.setOwner(Cytoscape.getDesktop());
		jTaskConfig.displayCloseButton(true);
		jTaskConfig.displayStatus(true);
		jTaskConfig.displayTimeElapsed(true);

		jTaskConfig.setAutoDispose(true);

		// Execute Task in New Thread; pops open JTask Dialog Box.
		TaskManager.executeTask(unCrossTask, jTaskConfig);

	}

	public class UnCrossTask implements Task {
		// // MLC 12/05/05:
		// static private final int NODE_THRESHOLD = 200;
		boolean interrupted = false;

		private TaskMonitor taskMonitor = null;

		private List nodes;

		public UnCrossTask(List nodeList) {
			nodes = nodeList;
		}

		public void run() {
			if (taskMonitor == null) {
				throw new IllegalStateException("Task Monitor is not set");
			}
			
			if (nodes.size() <= 0)
			{
				JOptionPane.showMessageDialog(Cytoscape.getDesktop(), 
						"You must first select some nodes in order to minimize edge crossings.");
				return;	
			}

			initialize(nodes);

			for (int j = 0; j < _nodeViews.length; j++) {
				_undoOffsets[j] = _nodeViews[j].getOffset();
			}

			for (iteration = 0; iteration < ITERATION_LIMIT; iteration++) {
				taskMonitor.setStatus("Iteration " + iteration + " of "
						+ ITERATION_LIMIT);
				taskMonitor
						.setPercentCompleted((int) (100 * (1.0 * iteration) / ITERATION_LIMIT));

				for (int i = 0; i < _nodeViews.length; i++) {
					if (taskMonitor != null) {
						if ((i % 5) == 0) {
							taskMonitor
									.setPercentCompleted((int) (100 * (1.0 * iteration) / ITERATION_LIMIT)
											+ ((int) ((i * 1.0 / _nodeViews.length) * (100.0 / ITERATION_LIMIT))));
							taskMonitor.setStatus("Iteration " + iteration
									+ " of " + ITERATION_LIMIT + ", Node " + i
									+ " of " + _nodeViews.length);
						}
					}
					permuteToBestFit(i);
				}
				for (int m = 0; m < _nodeViews.length; m++) {
					_redoOffsets[m] = _nodeViews[m].getOffset();
				}
				CytoscapeDesktop.undo.addEdit(new AbstractUndoableEdit() {

					public String getPresentationName() {
						return "UnCross";
					}

					public String getRedoPresentationName() {

						return "Redo: Edge Minimization";
					}

					public String getUndoPresentationName() {
						return "Undo: Edge Minimization";
					}

					public void redo() {
						for (int m = 0; m < _nodeViews.length; m++) {
							_nodeViews[m].setOffset(_redoOffsets[m].getX(),
									_redoOffsets[m].getY());
						}
					}

					public void undo() {
						for (int m = 0; m < _nodeViews.length; m++) {
							_nodeViews[m].setOffset(_undoOffsets[m].getX(),
									_undoOffsets[m].getY());
						}
					}
				});

				_view.redrawGraph(true, true);
				// _view.updateView();
			}
			// _view.redrawGraph(true, true);
		}

		private void initialize(List nodes) {

			// initialize the data arrays
			_beforeEdgeCrossings = new int[nodes.size()];
			_afterEdgeCrossings = new int[nodes.size()];
			_totalEdgeCrossings = new int[nodes.size()];
			_undoOffsets = new Point2D[nodes.size()];
			_redoOffsets = new Point2D[nodes.size()];

			// start the first iteration
			_selectedNodes = nodes;
			iteration = 0;
			_net = Cytoscape.getCurrentNetwork();
			_view = Cytoscape.getCurrentNetworkView();

			// first initialize array of NodeViews, starting from randomly
			// generated
			// position
			int len = nodes.size();
			int startPosn = (int) (len * Math.random());
			_nodeViews = new NodeView[len];
			ListIterator it = _selectedNodes.listIterator(startPosn);
			int i = 0;
			while (it.hasNext()) {
				_nodeViews[i] = (NodeView) it.next();
				i++;
			}
			it = _selectedNodes.listIterator(startPosn);
			while (it.hasPrevious()) {
				_nodeViews[i] = (NodeView) it.previous();
				i++;
			}

			// now calculate base set of edge crossing values

			// for debugging
			// System.out.println(" ");
			// System.out.println("Node\tEdge Crossings");

			for (int j = 0; j < _nodeViews.length; j++) {
				NodeView nv = _nodeViews[j];
				Point2D locn = nv.getOffset();
				_beforeEdgeCrossings[j] = calculateEdgeCrossings(nv, locn,
						null, null);
				// for debugging
				// System.out.println (nv.getNode().getIdentifier()
				// + "\t" + _beforeEdgeCrossings[j]);
			}
			// for debugging
			// System.out.println(" ");

			int totalCrossings = calculateTotalCrossings(_beforeEdgeCrossings);
			_totalEdgeCrossings[0] = totalCrossings; // we start at first
			// index
		}

		private int calculateTotalCrossings(int[] edgeCrossings) {
			int total = 0;
			for (int i = 0; i < edgeCrossings.length; i++) {
				total += edgeCrossings[i];
			}
			return total;
		}

		/**
		 * permute node at index with one of the other nodes such that edge
		 * crossings are minimized
		 * 
		 */
		private void permuteToBestFit(int index) {
			/**
			 * loop through _nodeViews and for each _nodeView a. calculate edge
			 * crossings for the node at index if moved to position of _nodeView
			 * b. calculate edge crossings for the node at current position if
			 * moved to position of _nodeView at index c. sum a and b and store
			 * in total edge crossings for counter position d. permute _nodeView
			 * at index with _nodeView at position
			 * 
			 */

			for (int i = 0; i < _beforeEdgeCrossings.length; i++) {
				NodeView nv = _nodeViews[index];
				if (i != index) // don't permute a node with itself
				{
					_candidate_before_locn = nv.getOffset();
					NodeView otherNv = _nodeViews[i];
					_other_before_locn = otherNv.getOffset();

					// now permute
					nv.setOffset(_other_before_locn.getX(), _other_before_locn
							.getY());
					otherNv.setOffset(_candidate_before_locn.getX(),
							_candidate_before_locn.getY());

					// now calculate edge crossings after permuting
					for (int j = 0; j < _nodeViews.length; j++) {
						NodeView newNv = _nodeViews[j];
						Point2D locn = newNv.getOffset();
						
						// efficiency hack: use cached values for all but 
						// but the candidate and swapped nodeViews
						if ((j == index) || (j == i))
						{
							_afterEdgeCrossings[j] = calculateEdgeCrossings(newNv,
									locn, null, null);
						}
						else
						{
							_afterEdgeCrossings[j] = _beforeEdgeCrossings[j];
						}
						

						// for debugging
						// System.out.println (newNv.getNode().getIdentifier()
						// + "\t" + _afterEdgeCrossings[j]);
					}
					// for debugging
					// System.out.println(" ");
					int totalCrossings = calculateTotalCrossings(_afterEdgeCrossings);
					_totalEdgeCrossings[i] = totalCrossings;

					// now shift back before going on to next potential swap
					nv.setOffset(_candidate_before_locn.getX(),
							_candidate_before_locn.getY());
					otherNv.setOffset(_other_before_locn.getX(),
							_other_before_locn.getY());

				}
			}
			// now go through the totals and find index with minimum edge
			// crossings
			_index_for_best_fit = index;
			_nbr_crossings_for_best_fit = _totalEdgeCrossings[index];
			for (int k = 0; k < _nodeViews.length; k++) {
				if (_totalEdgeCrossings[k] < _nbr_crossings_for_best_fit) {
					_index_for_best_fit = k;
					_nbr_crossings_for_best_fit = _totalEdgeCrossings[k];
				}
			}
			if (_index_for_best_fit != index) // swap for lower edge crossings
			{
				// shift X position by a small random amount so as to indicate
				// movement
				NodeView swapNv = _nodeViews[_index_for_best_fit];
				_tmpPoint = swapNv.getOffset();
				swapNv.setOffset(_nodeViews[index].getXPosition()
						,
//						- (0.1 * swapNv.getWidth())
//						+ (0.2* Math.random() * swapNv.getWidth()),

						_nodeViews[index].getYPosition());
				_nodeViews[index].setOffset(_tmpPoint.getX()
						,

						//						- (0.1 * _nodeViews[index].getWidth())
//						+ (0.2 * Math.random() * _nodeViews[index].getWidth()),
						_tmpPoint.getY());
				_totalEdgeCrossings[_index_for_best_fit] = _totalEdgeCrossings[index];
				_totalEdgeCrossings[index] = _nbr_crossings_for_best_fit;
			}

			// now need to recalculate baseline edge crossing counts after swap
			for (int j = 0; j < _nodeViews.length; j++) {
				NodeView nv = _nodeViews[j];
				Point2D locn = nv.getOffset();
				_beforeEdgeCrossings[j] = calculateEdgeCrossings(nv, locn,
						null, null);
				// for debugging
				// System.out.println (nv.getNode().getIdentifier()
				// + "\t" + _beforeEdgeCrossings[j]);
			}
		}

		/**
		 * calculate edge crossings for edges incident to NodeView at index when
		 * node is at position specified by locn
		 * 
		 * @param nv
		 *            the NodeView for which we are calculating edge crossings
		 * @param nvLocn
		 *            the position for NodeView (may be actual or tentative)
		 * @param swapNV
		 *            the NodeView that we are tentatively swapping locations
		 *            with (may be null)
		 * @param swapLocn
		 *            position for NodeView that we are tentatively swapping
		 *            locations with (may be null)
		 * @return number of edgeCrossings
		 */
		private int calculateEdgeCrossings(NodeView nv, Point2D nvLocn,
				NodeView swapNv, Point2D swapLocn) {
			int crossings = 0;
			Node n = nv.getNode();
			List myEdges = _net.getAdjacentEdgesList(n, true, true, true);
			for (int i = 0; i < myEdges.size(); i++) {
				crossings += calculateCrossingsForEdge((Edge) myEdges.get(i),
						nv, nvLocn, swapNv, swapLocn);
			}

			return crossings;
		}

		/**
		 * calculate edge crossings for a specific edge
		 * 
		 * @param edge
		 * @param nv
		 *            the NodeView for which we are calculating edge crossings
		 * @param nvLocn
		 *            the position for NodeView (may be actual or tentative)
		 * @param swapNV
		 *            the NodeView that we are tentatively swapping locations
		 *            with (may be null)
		 * @param swapLocn
		 *            position for NodeView that we are tentatively swapping
		 *            locations with (may be null)
		 * @return number of edges the input edge intersects
		 */
		private int calculateCrossingsForEdge(Edge edge, NodeView nv,
				Point2D nvLocn, NodeView swapNv, Point2D swapLocn) {
			int crossings = 0;
			Iterator allEdges = _net.edgesIterator();
			while (allEdges.hasNext()) {
				Edge otherEdge = (Edge) allEdges.next();
				if (edge != otherEdge) {
					crossings += edgeCrossings(edge, otherEdge, nv, nvLocn,
							swapNv, swapLocn);
				}
			}
			return crossings;
		}

		/**
		 * calculate edge crossings for the two input edges (should be 0 or 1)
		 * 
		 * @param thisEdge
		 *            first edge
		 * @param otherEdge
		 *            secondEdge
		 * @param nv
		 *            the NodeView for which we are calculating edge crossings
		 * @param nvLocn
		 *            the position for NodeView (may be actual or tentative)
		 * @param swapNV
		 *            the NodeView that we are tentatively swapping locations
		 *            with (may be null)
		 * @param swapLocn
		 *            position for NodeView that we are tentatively swapping
		 *            locations with (may be null)
		 * @return number of edge crossings for the two edges (should be 0 or 1
		 */
		private int edgeCrossings(Edge thisEdge, Edge otherEdge, NodeView nv,
				Point2D nvLocn, NodeView swapNv, Point2D swapLocn) {
			// System.out.println(" Comparing edge: " + thisEdge + " to "
			// + otherEdge);
			// System.out.println (" Comparing "
			// + nv.getNode().getIdentifier() + " at " +
			// nv.getXPosition() + "," + nv.getYPosition());
			if (swapNv != null) {
				// System.out.println(" + with "
				// + swapNv.getNode().getIdentifier()+ " at " +
				// swapNv.getXPosition() + "," + swapNv.getYPosition());
			}

			NodeView thisEdgeSourceView = _view.getNodeView(thisEdge
					.getSource());
			NodeView thisEdgeTargetView = _view.getNodeView(thisEdge
					.getTarget());
			NodeView otherEdgeSourceView = _view.getNodeView(otherEdge
					.getSource());
			NodeView otherEdgeTargetView = _view.getNodeView(otherEdge
					.getTarget());

			Line2D thisLine = new Line2D.Double(thisEdgeSourceView.getOffset(),
					thisEdgeTargetView.getOffset());
			Line2D otherLine = new Line2D.Double(otherEdgeSourceView
					.getOffset(), otherEdgeTargetView.getOffset());

			// find the intersection point between the two lines
			Point2D intersectionPt = getIntersectionPoint(thisLine, otherLine);

			// debug
			// System.out.println (" Got intersection point" +
			// intersectionPt.getX() + "," + intersectionPt.getY());

			if (intersectionPt == null) {
				return 0; // no intersection
			}
			// eliminate the case where lines intersect at their end points,
			// i.e.
			// it's a junction at a node, not an edge crossing
			else if ((((int) (intersectionPt.getX()) == ((int) thisLine.getX1())) && ((int) (intersectionPt
					.getY()) == ((int) thisLine.getY1())))
					|| (((int) (intersectionPt.getX()) == ((int) thisLine
							.getX2())) && ((int) (intersectionPt.getY()) == ((int) thisLine
							.getY2()))))
			// we are at the endpoint of one line. Are we at endPoint of the
			// other?
			{
				if ((((int) (intersectionPt.getX()) == ((int) otherLine.getX1())) && ((int) (intersectionPt
						.getY()) == ((int) otherLine.getY1())))
						|| (((int) (intersectionPt.getX()) == ((int) otherLine
								.getX2())) && ((int) (intersectionPt.getY()) == ((int) otherLine
								.getY2())))) {
					// System.out.println ("intersection point is an
					// endpoint.");
					return 0;
				}
			}

			// make sure that point is actually on both lines, not their
			// extensions
			// to infinity
			else if (thisLine.intersectsLine(otherLine))
			// ((thisLine.contains(intersectionPt)) &&
			// (otherLine.contains(intersectionPt)))
			{
				// System.out.println("Lines intersect internally");

				return 1;

			}

			// not an edge crossing if we get to this point
			return 0;

		}

		private Point2D getIntersectionPoint(Line2D thisLine, Line2D otherLine) {
			double e1x1 = thisLine.getX1();
			double e1x2 = thisLine.getX2();
			double e1y1 = thisLine.getY1();
			double e1y2 = thisLine.getY2();

			double e2x1 = otherLine.getX1();
			double e2x2 = otherLine.getX2();
			double e2y1 = otherLine.getY1();
			double e2y2 = otherLine.getY2();

			// debug
			// System.out.println(" Comparing lines "
			// + e1x1 + "," + e1y1 + " ==> " + e1x2 + "," + e1y2);
			// System.out.println(" and "
			// + e2x1 + "," + e2y1 + " ==> " + e2x2 + "," + e2y2);

			double dx1 = e1x2 - e1x1;
			double dx2 = e2x2 - e2x1;

			if ((dx1 == 0.0d) && (dx2 == 0.0d)) {
				return null; // both lines are vertical and parallel (unless
				// the
				// same)
			}

			double m1 = Double.NaN;
			double m2 = Double.NaN;

			if (dx1 != 0.0) {
				m1 = (e1y2 - e1y1) / dx1;
			}

			if (dx2 != 0.0) {
				m2 = (e2y2 - e2y1) / dx2;
			}

			if (((int) dx1) == 0) // first line is vertical
			{
				return new Point2D.Double(e1x1, m2 * (e1x1 - e2x2) + e2y2);
			}
			if (((int) dx2) == 0) // second line is vertical
			{
				return new Point2D.Double(e2x2, m1 * (e2x2 - e1x1) + e1y1);
			}

			Double xInt = (-m2 * e2x2 + e2y2 + m1 * e1x1 - e1y1) / (m1 - m2);
			Double yInt = m1 * (xInt - e1x1) + e1y1;

			return new Point2D.Double(xInt, yInt);
		}

		public void setTaskMonitor(TaskMonitor taskMonitor) {
			if (this.taskMonitor != null) {
				throw new IllegalStateException("Task Monitor is already set");
			}

			this.taskMonitor = taskMonitor;
		}

		/**
		 * 
		 * Sets the task title
		 * 
		 * @return human readable task title
		 */
		public String getTitle() {
			return new String("Minimize Edge Crossings");
		}

		public void halt() {
			this.interrupted = true;
		}
	}

	/**
	 * calculates actual line segment for the edge, taking into count
	 * potentially swapped positions of endpoints
	 * 
	 * @param thisEdge
	 *            first edge
	 * @param nv
	 *            the NodeView for which we are calculating edge crossings
	 * @param nvLocn
	 *            the position for NodeView (may be actual or tentative)
	 * @param swapNV
	 *            the NodeView that we are tentatively swapping locations with
	 *            (may be null)
	 * @param swapLocn
	 *            position for NodeView that we are tentatively swapping
	 *            locations with (may be null)
	 * @return line segment for edge
	 */
	/*
	 * do not need this private static Line2D calculateLineSegment (Edge
	 * thisEdge, NodeView nv, Point2D nvLocn, NodeView swapNv, Point2D swapLocn) {
	 * Node source = thisEdge.getSource(); Node target = thisEdge.getTarget();
	 * 
	 * Point2D sourceLocn, targetLocn; // first determine source position if
	 * (source == nv.getNode()) { sourceLocn = nvLocn; } else if ((swapNv !=
	 * null) && (swapNv == nv.getNode())) { sourceLocn = swapLocn; } else {
	 * sourceLocn = _view.getNodeView(source).getOffset(); } // next determine
	 * target position if (target == nv.getNode()) { targetLocn = nvLocn; } else
	 * if ((swapNv != null) && (swapNv == nv.getNode())) { targetLocn =
	 * swapLocn; } else { targetLocn = _view.getNodeView(target).getOffset(); }
	 * 
	 * return new Line2D.Double (sourceLocn.getX(), sourceLocn.getY(),
	 * targetLocn.getX(), targetLocn.getY()); }
	 */


}
