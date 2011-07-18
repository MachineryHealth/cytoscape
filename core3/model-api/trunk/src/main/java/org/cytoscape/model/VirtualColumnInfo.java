package org.cytoscape.model;

public interface VirtualColumnInfo {
	/**
	 * @return true if the column is virtual, otherwise false.
	 */
	boolean isVirtual();
	
	/**
	 * Returns the name of the column from the source table which contains the
	 * values this column provides. 
	 */
	String getSourceColumn();
	
	/**
	 * Returns the name of the column from the source table used for the join.
	 */
	String getSourceJoinKey();
	
	/**
	 * Returns the name of the column from the target table used for the join.
	 */
	String getTargetJoinKey();
	
	/**
	 * Returns the originating table for this column if this column is virtual.
	 * @return the actual table that this column originates at if it is virtual, or null if the
	 *         column is not virtual
	 */
	CyTable getSourceTable();
}
