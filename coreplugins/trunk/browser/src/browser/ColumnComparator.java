/*
 =====================================================================

 ColumnComparator.java

 Created by Claude Duguay
 Copyright (c) 2002
 
 Rewrote by Keiichiro Ono 2006

 =====================================================================
 */
package browser;

import java.util.Comparator;
import java.util.Vector;

public class ColumnComparator implements Comparator {

	protected int index;
	protected boolean ascending;

	public ColumnComparator(final int index, final boolean ascending) {
		this.index = index;
		this.ascending = ascending;
	}

	public int compare(final Object obj1, final Object obj2) {

		if (obj1 instanceof Vector && obj2 instanceof Vector) {

			final Object firstObj = ((Vector) obj1).elementAt(index);
			final Object secondObj = ((Vector) obj2).elementAt(index);

			if (firstObj == null && secondObj == null) {
				return 0;
			} else if (firstObj == null) {
				return 1;
			} else if (secondObj == null) {
				return -1;
			} else if (firstObj instanceof Comparable
					&& secondObj instanceof Comparable) {

				final Comparable firstComparableObj = (Comparable) firstObj;
				final Comparable secondComparableObj = (Comparable) secondObj;

				/*
				 * If these values are Strings, treat empty values as null.
				 */
				if (firstComparableObj instanceof String
						&& secondComparableObj instanceof String) {
					final int firstLength = ((String) firstComparableObj)
							.trim().length();
					final int secondLength = ((String) secondComparableObj)
							.trim().length();

					if (firstLength == 0 && secondLength == 0) {
						return 0;
					} else if (firstLength == 0) {
						return 1;
					} else if (secondLength == 0) {
						return -1;
					} else {
						return ascending ? firstComparableObj
								.compareTo(secondComparableObj)
								: secondComparableObj
										.compareTo(firstComparableObj);
					}
				} else {

					return ascending ? firstComparableObj
							.compareTo(secondComparableObj)
							: secondComparableObj.compareTo(firstComparableObj);
				}
			}
		}
		return 1;
	}

	/**
	 * Comparing numbers.
	 * 
	 * @param number1
	 * @param number2
	 * @return
	 */
	public int compare(final Number number1, final Number number2) {

		final double firstNumber = number1.doubleValue();
		final double secondNumber = number2.doubleValue();

		if (firstNumber < secondNumber) {
			return -1;
		} else if (firstNumber > secondNumber) {
			return 1;
		} else {
			return 0;
		}
	}
}
