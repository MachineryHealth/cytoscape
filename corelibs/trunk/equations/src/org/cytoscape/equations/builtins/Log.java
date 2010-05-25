/*
  File: Log.java

  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.equations.builtins;


import java.util.ArrayList;
import java.util.List;
import org.cytoscape.equations.AttribFunction;


public class Log implements AttribFunction {
	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "LOG"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns the logarithm of a number to a specified base."; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of how to use this function
	 */
	public String getUsageDescription() { return "Call this with \"LOG(number [, base])\""; }

	public Class getReturnType() { return Double.class; }

	/**
	 *  @return Double.class or null if there are not 1 or 2 args or the args are not of type Double
	 */
	public Class validateArgTypes(final Class[] argTypes) {
		if (argTypes.length != 1 && argTypes.length != 2)
			return null;

		for (final Class argType : argTypes) {
			if (argType != Double.class && argType != Long.class)
				return null;
		}

		return Double.class;
	}

	/**
	 *  @param args the function arguments which must be either one or two objects of type Double
	 *  @return the result of the function evaluation which is the logarithm of the first argument
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if any of the arguments is not of type Double
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final double number = args[0].getClass() == Double.class ? (Double)args[0] : (Long)args[0];
		final double base;
		if (args.length == 1)
			base = 10.0;
		else
			base = args[1].getClass() == Double.class ? (Double)args[1] : (Long)args[1];

		if (number <= 0.0)
			throw new IllegalArgumentException("LOG() called with a number <= 0.0!");

		if (base <= 0.0)
			throw new IllegalArgumentException("LOG() called with a base <= 0.0!");

		double retval = Math.log10(number);
		if (base != 10.0)
			retval /= Math.log10(base);

		return retval;
	}

	/**
	 *  Used with the equation builder.
	 *
	 *  @param leadingArgs the types of the arguments that have already been selected by the user.
	 *  @return the set of arguments (must be a collection of String.class, Long.class, Double.class,
	 *           Boolean.class and List.class) that are candidates for the next argument.  An empty
	 *           set indicates that no further arguments are valid.
	 */
	public List<Class> getPossibleArgTypes(final Class[] leadingArgs) {
		if (leadingArgs.length < 2) {
			final List<Class> possibleNextArgs = new ArrayList<Class>();
			possibleNextArgs.add(Double.class);
			possibleNextArgs.add(Long.class);
			if (leadingArgs.length == 1)
				possibleNextArgs.add(null);
			return possibleNextArgs;
		}

		return null;
	}
}
