/*
  File: InterpreterTest.java

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
package cytoscape.data.eqn_attribs.interpreter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import junit.framework.*;
import cytoscape.data.eqn_attribs.AttribEqnCompiler;
import cytoscape.data.eqn_attribs.builtins.*;


public class InterpreterTest extends TestCase {
	private final AttribEqnCompiler compiler = new AttribEqnCompiler();

	public void testSimpleStringConcatExpr() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("s1", String.class);
		assertTrue(compiler.compile("=\"Fred\"&${s1}", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("s1", new IdentDescriptor("Bob"));
		final Interpreter interpreter = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals("FredBob", interpreter.run());
	}

	public void testSimpleExpr() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("BOB", Double.class);
		assertTrue(compiler.compile("=42 - 12 + 3 * (4 - 2) + ${BOB:12}", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("BOB", new IdentDescriptor(-10.0));
		final Interpreter interpreter = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(26.0), interpreter.run());
	}

	public void testUnaryPlusAndMinus() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("attr1", Double.class);
		attribNameToTypeMap.put("attr2", Double.class);
		assertTrue(compiler.compile("=-17.8E-14", attribNameToTypeMap));
		assertTrue(compiler.compile("=+(${attr1} + ${attr2})", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("attr1", new IdentDescriptor(5.5));
		nameToDescriptorMap.put("attr2", new IdentDescriptor(6.5));
		final Interpreter interpreter = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(12.0), interpreter.run());
	}

	public void testFunctionCall() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertTrue(compiler.compile("=42 + log(4 - 2)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(42.0 + Math.log10(4.0 - 2.0)), interpreter.run());
	}

	public void testExponentiation() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertTrue(compiler.compile("=2^3^4 - 0.0002", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(Math.pow(2.0, Math.pow(3.0, 4.0)) - 0.0002), interpreter.run());
	}

	public void testComparisons() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("x", Double.class);
		attribNameToTypeMap.put("y", Double.class);
		attribNameToTypeMap.put("limit", Double.class);
		assertTrue(compiler.compile("=${x} <= ${y}", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("x", new IdentDescriptor(1.2));
		nameToDescriptorMap.put("y", new IdentDescriptor(-3.8e-12));
		nameToDescriptorMap.put("limit", new IdentDescriptor(-65.23e12));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Boolean(false), interpreter1.run());
		
		assertTrue(compiler.compile("=-15.4^3 > ${limit}", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Boolean(true), interpreter2.run());
	}

	public void testVarargs() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertFalse(compiler.compile("=LOG()", attribNameToTypeMap));
		assertTrue(compiler.compile("=LOG(1)", attribNameToTypeMap));
		assertTrue(compiler.compile("=LOG(1,2)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(Math.log(1.0)/Math.log(2.0)), interpreter.run());
		assertFalse(compiler.compile("=LOG(1,2,3)", attribNameToTypeMap));
	}

	public void testFixedargs() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertFalse(compiler.compile("=ABS()", attribNameToTypeMap));
		assertTrue(compiler.compile("=ABS(-1.5e10)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(1.5e10), interpreter.run());
		assertFalse(compiler.compile("=ABS(1,2)", attribNameToTypeMap));
	}

	public void testABS() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();

		// First we try doubles...
		assertTrue(compiler.compile("=ABS(-1.3)", attribNameToTypeMap));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(1.3), interpreter1.run());
		assertTrue(compiler.compile("=ABS(0.0)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(0.0), interpreter2.run());
		assertTrue(compiler.compile("=ABS(1.3)", attribNameToTypeMap));
		final Interpreter interpreter3 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(1.3), interpreter3.run());

		// ...and then we try integers
		assertTrue(compiler.compile("=ABS(-3)", attribNameToTypeMap));
		final Interpreter interpreter4 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(3.0), interpreter4.run());
		assertTrue(compiler.compile("=ABS(0)", attribNameToTypeMap));
		final Interpreter interpreter5 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(0.0), interpreter5.run());
		assertTrue(compiler.compile("=ABS(3)", attribNameToTypeMap));
		final Interpreter interpreter6 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(3.0), interpreter6.run());
	}

	public void testNOT() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("logical", Boolean.class);
		assertFalse(compiler.compile("=NOT()", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("logical", new IdentDescriptor(true));
		assertTrue(compiler.compile("=NOT(true)", attribNameToTypeMap));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Boolean(false), interpreter1.run());
		assertTrue(compiler.compile("=NOT(false)", attribNameToTypeMap));
		assertTrue(compiler.compile("=NOT(3.2 < 12)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Boolean(false), interpreter2.run());
		assertTrue(compiler.compile("=NOT(${logical})", attribNameToTypeMap));
		assertFalse(compiler.compile("=NOT(true, true)", attribNameToTypeMap));
	}

	public void testUCASEandLCASE() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertTrue(compiler.compile("=UCASE(\"Fred\")", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("FRED"), interpreter1.run());
		assertTrue(compiler.compile("=\"bozo\"&LCASE(\"UPPER\")", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("bozoupper"), interpreter2.run());
	}

	public void testSUBSTITUTE() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertTrue(compiler.compile("=SUBSTITUTE(\"ABABBAABAB\", \"A\", \"X\")", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("XBXBBXXBXB"), interpreter1.run());

		assertTrue(compiler.compile("=Substitute(\"FredBobBillJoeBobHansKarl\", \"Bob\", \"Julie\", 2.4)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("FredBobBillJoeJulieHansKarl"), interpreter2.run());

		assertTrue(compiler.compile("=Substitute(\"FredBobBillJoeBobHansKarl\", \"Bob\", \"Julie\", 3)", attribNameToTypeMap));
		final Interpreter interpreter3 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("FredBobBillJoeBobHansKarl"), interpreter3.run());

		assertTrue(compiler.compile("=Substitute(\"FredBobBillJoeBobHansKarl\", \"Bob2\", \"Julie\")", attribNameToTypeMap));
		final Interpreter interpreter4 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("FredBobBillJoeBobHansKarl"), interpreter4.run());
	}

	public void testIF() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertTrue(compiler.compile("=IF(2.3 >= 1, \"Xx\", \"Yz\")", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("Xx"), interpreter1.run());

		assertTrue(compiler.compile("=IF(FALSE, 12.3, -4)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(-4), interpreter2.run());

		assertTrue(compiler.compile("=IF(true, false, true)", attribNameToTypeMap));
		final Interpreter interpreter3 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Boolean(false), interpreter3.run());

		assertTrue(compiler.compile("=IF(TrUe, 12.3, \"-4\")", attribNameToTypeMap));
		final Interpreter interpreter4 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("12.3"), interpreter4.run());
	}

	public void testLNandEXP() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertTrue(compiler.compile("=LN(2.3)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(Math.log(2.3)), interpreter1.run());

		assertTrue(compiler.compile("=EXP(-4)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(Math.exp(-4.0)), interpreter2.run());

		assertTrue(compiler.compile("=EXP(LN(2.5))", attribNameToTypeMap));
		final Interpreter interpreter3 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(2.5), interpreter3.run());

		boolean succeeded;
		try {
			assertTrue(compiler.compile("=EXP(LN(0.0))", attribNameToTypeMap));
			final Interpreter interpreter4 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
			interpreter4.run();
			succeeded = true;
		} catch (final Exception e) {
			succeeded = false;
		}
		assertFalse(succeeded);
	}

	public void testLEFTandRIGHTandMID() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertTrue(compiler.compile("=LEFT(\"Circus\", 3)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("Cir"), interpreter1.run());

		assertTrue(compiler.compile("=RIGHT(\"Maximus\", 4)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("imus"), interpreter2.run());

		assertTrue(compiler.compile("=MID(\"Gaius Julius Cesar\",7,6)", attribNameToTypeMap));
		final Interpreter interpreter3 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("Julius"), interpreter3.run());

		assertTrue(compiler.compile("=MID(\"Augustus\",7,6000)", attribNameToTypeMap));
		final Interpreter interpreter4 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("us"), interpreter4.run());

		assertTrue(compiler.compile("=LEFT(\"aureus\",6000)", attribNameToTypeMap));
		final Interpreter interpreter5 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("aureus"), interpreter5.run());

		assertTrue(compiler.compile("=RIGHT(\"toga\",33)", attribNameToTypeMap));
		final Interpreter interpreter6 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("toga"), interpreter6.run());
	}

	public void testLEN() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertTrue(compiler.compile("=LEN(\"baboon\")", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(6.0), interpreter1.run());

		assertTrue(compiler.compile("=LEN(\"\")", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(0.0), interpreter2.run());
	}

	public void testDEFINED() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("x", Double.class);
		assertTrue(compiler.compile("=defined(x)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("x", new IdentDescriptor(1.2));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Boolean(true), interpreter1.run());

		assertTrue(compiler.compile("=DEFINED(${limit})", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Boolean(false), interpreter2.run());
	}

	public void testROUND() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertTrue(compiler.compile("=ROUND(2.15, 1)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(2.2), interpreter1.run());

		assertTrue(compiler.compile("=ROUND(2.149,1)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(2.1), interpreter2.run());

		assertTrue(compiler.compile("=ROUND(-1.475,2)", attribNameToTypeMap));
		final Interpreter interpreter3 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(-1.48), interpreter3.run());

		assertTrue(compiler.compile("=ROUND(21.5, -1)", attribNameToTypeMap));
		final Interpreter interpreter4 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(20.0), interpreter4.run());
	}

	public void testTRUNC() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertTrue(compiler.compile("=TRUNC(8.9)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(8.0), interpreter1.run());

		assertTrue(compiler.compile("=TRUNC(-8.9)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(-8.0), interpreter2.run());

		assertTrue(compiler.compile("=TRUNC(PI())", attribNameToTypeMap));
		final Interpreter interpreter3 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(3.0), interpreter3.run());

		assertTrue(compiler.compile("=TRUNC(-1.475,2)", attribNameToTypeMap));
		final Interpreter interpreter4 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(-1.47), interpreter4.run());
	}

	public void testVALUE() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertTrue(compiler.compile("=VALUE(\"-8.9e99\")", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(-8.9e99), interpreter.run());
	}

	public void testAVERAGE() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("list", List.class);
		assertTrue(compiler.compile("=AVERAGE(${list})", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final List<Double> numbers = new ArrayList<Double>();
		numbers.add(1.0);
		numbers.add(2.0);
		numbers.add(3.0);
		numbers.add(4.0);
		numbers.add(5.0);
		nameToDescriptorMap.put("list", new IdentDescriptor(numbers));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(3.0), interpreter1.run());

		assertTrue(compiler.compile("=AVERAGE(1,2.0,3)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(2.0), interpreter2.run());
	}

	public void testMIN() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("list", List.class);
		assertTrue(compiler.compile("=MIN(${list})", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final List<Object> numbers = new ArrayList<Object>();
		numbers.add(1.0);
		numbers.add(new Integer(2));
		numbers.add(3.0);
		numbers.add(new String("4.0"));
		numbers.add(5.0);
		nameToDescriptorMap.put("list", new IdentDescriptor(numbers));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(1.0), interpreter1.run());

		assertTrue(compiler.compile("=MIN(-2,-3,-4.35)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(-4.35), interpreter2.run());
	}

	public void testMAX() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("list", List.class);
		assertTrue(compiler.compile("=MAX(${list})", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final List<Object> numbers = new ArrayList<Object>();
		numbers.add(1.0);
		numbers.add(new Integer(2));
		numbers.add(3.0);
		numbers.add(new String("4.0"));
		numbers.add(5.0);
		nameToDescriptorMap.put("list", new IdentDescriptor(numbers));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(5.0), interpreter1.run());

		assertTrue(compiler.compile("=MAX(-2,-3,-4.35)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(-2.0), interpreter2.run());
	}

	public void testCOUNT() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("list", List.class);
		assertTrue(compiler.compile("=COUNT(${list})", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final List<Object> numbers = new ArrayList<Object>();
		numbers.add(1.0);
		numbers.add(new Integer(2));
		numbers.add(3.0);
		numbers.add(new String("4.0"));
		numbers.add(5.0);
		nameToDescriptorMap.put("list", new IdentDescriptor(numbers));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Long(4L), interpreter1.run());

		assertTrue(compiler.compile("=COUNT(-2,-3,-4.35)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Long(3L), interpreter2.run());
	}

	public void testMEDIAN() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		assertTrue(compiler.compile("=MEDIAN(3,2,5,1,4)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(3.0), interpreter1.run());

		assertTrue(compiler.compile("=MEDIAN(1,2,4,3)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(2.5), interpreter2.run());
	}

	public void testNTH() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("list1", List.class);
		assertTrue(compiler.compile("=NTH(${list1}, 3)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final List<Object> list1 = new ArrayList<Object>();
		list1.add(3.0);
		list1.add(new Integer(2));
		list1.add(5.0);
		list1.add(new String("1"));
		list1.add(4.0);
		nameToDescriptorMap.put("list1", new IdentDescriptor(list1));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("5.0"), interpreter1.run());

		attribNameToTypeMap.put("list2", List.class);
		assertTrue(compiler.compile("=NTH(${list2}, 4)", attribNameToTypeMap));
		final List<Object> list2 = new ArrayList<Object>();
		list2.add(1.0);
		list2.add(new Integer(2));
		list2.add(4.0);
		list2.add(new String("3"));
		nameToDescriptorMap.put("list2", new IdentDescriptor(list2));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("3"), interpreter2.run());

		boolean succeeded;
		try {
			assertTrue(compiler.compile("=NTH(${list2}, 5)", attribNameToTypeMap));
			final Interpreter interpreter3 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
			interpreter3.run();
			succeeded = true;
		} catch (final Exception e) {
			succeeded = false;
		}
		assertFalse(succeeded);

		try {
			assertTrue(compiler.compile("=NTH(${list2}, 0)", attribNameToTypeMap));
			final Interpreter interpreter4 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
			interpreter4.run();
			succeeded = true;
		} catch (final Exception e) {
			succeeded = false;
		}
		assertFalse(succeeded);
	}

	public void testFIRST() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("list1", List.class);
		assertTrue(compiler.compile("=FIRST(${list1})", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final List<Object> list1 = new ArrayList<Object>();
		list1.add(3.0);
		list1.add(new Integer(2));
		list1.add(5.0);
		list1.add(new String("1"));
		list1.add(4.0);
		nameToDescriptorMap.put("list1", new IdentDescriptor(list1));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("3.0"), interpreter1.run());

		attribNameToTypeMap.put("list2", List.class);
		assertTrue(compiler.compile("=FIRST(${list2})", attribNameToTypeMap));
		final List<Object> list2 = new ArrayList<Object>();
		boolean succeeded;
		try {
			assertTrue(compiler.compile("=FIRST(${list2})", attribNameToTypeMap));
			final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
			interpreter2.run();
			succeeded = true;
		} catch (final Exception e) {
			succeeded = false;
		}
		assertFalse(succeeded);
	}

	public void testLAST() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("list1", List.class);
		assertTrue(compiler.compile("=LAST(${list1})", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		final List<Object> list1 = new ArrayList<Object>();
		list1.add(3.0);
		list1.add(new Integer(2));
		list1.add(5.0);
		list1.add(new String("1"));
		list1.add(4.0);
		nameToDescriptorMap.put("list1", new IdentDescriptor(list1));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new String("4.0"), interpreter1.run());

		attribNameToTypeMap.put("list2", List.class);
		assertTrue(compiler.compile("=LAST(${list2})", attribNameToTypeMap));
		final List<Object> list2 = new ArrayList<Object>();
		boolean succeeded;
		try {
			assertTrue(compiler.compile("=LAST(${list2})", attribNameToTypeMap));
			final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
			interpreter2.run();
			succeeded = true;
		} catch (final Exception e) {
			succeeded = false;
		}
		assertFalse(succeeded);
	}

	public void testMOD() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();

		assertTrue(compiler.compile("=MOD(3, 2)", attribNameToTypeMap));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(1.0), interpreter1.run());

		assertTrue(compiler.compile("=MOD(-3, 2)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(1.0), interpreter2.run());

		assertTrue(compiler.compile("=MOD(3, -2)", attribNameToTypeMap));
		final Interpreter interpreter3 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(-1.0), interpreter3.run());

		assertTrue(compiler.compile("=MOD(-3, -2)", attribNameToTypeMap));
		final Interpreter interpreter4 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(-1.0), interpreter4.run());
	}

	public void testSQRT() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();

		assertTrue(compiler.compile("=SQRT(0.0)", attribNameToTypeMap));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(0.0), interpreter1.run());

		assertTrue(compiler.compile("=SQRT(9.0)", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(3.0), interpreter2.run());
	}

	public void testIntegerToFloatingPointConversion() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("BOB", Long.class);

		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("BOB", new IdentDescriptor(new Long(3)));

		assertTrue(compiler.compile("=$BOB > 5.3", attribNameToTypeMap));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Boolean(false), interpreter1.run());

		assertTrue(compiler.compile("=$BOB <= 5.3", attribNameToTypeMap));
		final Interpreter interpreter2 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Boolean(true), interpreter2.run());
	}

	public void testMixedModeArithmetic() throws Exception {
		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
		attribNameToTypeMap.put("x", Long.class);

		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("x", new IdentDescriptor(new Long(3)));

		assertTrue(compiler.compile("=$x + 2.0", attribNameToTypeMap));
		final Interpreter interpreter1 = new Interpreter(compiler.getEquation(), nameToDescriptorMap);
		assertEquals(new Double(5.0), interpreter1.run());
	}
}
