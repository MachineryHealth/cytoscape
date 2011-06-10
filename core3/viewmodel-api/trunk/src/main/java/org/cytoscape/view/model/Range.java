package org.cytoscape.view.model;

public interface Range<T> {
	

	/**
	 * Type of object used in this range.
	 * 
	 * @return object type.
	 */
	Class<T> getType();
	
	
	/**
	 * If this range is a set of discrete values, return true.
	 * 
	 * @return If discrete, return true.
	 * 
	 */
	boolean isDiscrete();
	
	
	/**
	 * Return true if the given value is in the range defined in this class.
	 * 
	 * @param value any value to be tested.
	 * 
	 * @return true if the given value is in the range.
	 */
	boolean validate(final T value);
}
