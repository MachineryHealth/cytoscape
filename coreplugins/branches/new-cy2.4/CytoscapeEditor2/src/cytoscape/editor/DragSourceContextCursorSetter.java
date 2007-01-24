/* -*-Java-*-
********************************************************************************
*
* File:         DragSourceContextCurorSetter.java
* RCS:          $Header: $
* Description:
* Author:       Michael L. Creech
* Created:      Sat Dec 16 14:54:45 2006
* Modified:     Sun Dec 17 05:41:06 2006 (Michael L. Creech) creech@w235krbza760
* Language:     Java
* Package:
* Status:       Experimental (Do Not Distribute)
*
* (c) Copyright 2006, Agilent Technologies, all rights reserved.
*
********************************************************************************
*/
package cytoscape.editor;

import cytoscape.view.CyNetworkView;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.dnd.DragSourceDragEvent;


/**
 * Interface used to allow a general computation to determine 
 * the cursor to show during a drag operation originating
 * from a BasicCytoShapeEntity (palette entry) that is dragged over a
 * CyNetworkView of interest.
 * @author Michael L. Creech
 */

public interface DragSourceContextCursorSetter {
    /**
     * Used to determine the cursor to show during a drag operation originating
     * from a BasicCytoShapeEntity (palette entry) that is dragged over a
     * CyNetworkView of interest. For example, we might want to show that
     * it is illegal to drop on certain objects, such as Edges.
     * @param netView the CyNetworkView that is being dragged over.
     * @param newViewLoc the location within netView of the cursor, in netView coordinates.
     * @param dsde the DragSourceDragEvent that describes this particular
     *        drag operation.
     * @return the Cursor to display during this drag (e.g, DragSource.DefaultCopyNoDrop).
     */
    public Cursor computeCursor(CyNetworkView netView, Point netViewLoc,
                                DragSourceDragEvent dsde);
}
