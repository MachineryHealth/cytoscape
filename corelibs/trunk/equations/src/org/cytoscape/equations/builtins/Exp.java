/*
  File: Exp.java

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
import org.cytoscape.equations.Function;


public class Exp implements Function {
	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "EXP"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns e raised to a specified number."; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of how to use this function
	 */
	public String getUsageDescription() { return "Call this with \"EXP(number)\""; }

	public Class getReturnType() { return Double.class; }

	/**
	 *  @return Double.class or null if there are not 1 or 2 args or the args are not of type Double
	 */
	public Class validateArgTypes(final Class[] argTypes) {
		if (argTypes.length != 1 || argTypes[0] != Double.class)
			return null;

		return Double.class;
	}

	/**
	 *  @param args the function arguments which must be either object of type Double
	 *  @return the result of the function evaluation which is the argument applied to the base e
	 *  @throws ArithmeticException 
	 *  @throws IllegalArgumentException thrown if the argument is not of type Double
	 */
	public Object evaluateFunction(final Object[] args) throws IllegalArgumentException, ArithmeticException {
		final double number = (Double)args[0];
		return  Math.exp(number);
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
		if (leadingArgs.length == 0) {
			final List<Class> possibleNextArgs = new ArrayList<Class>();
			possibleNextArgs.add(Double.class);
			possibleNextArgs.add(Long.class);
			return possibleNextArgs;
		}

		return null;
	}
}
