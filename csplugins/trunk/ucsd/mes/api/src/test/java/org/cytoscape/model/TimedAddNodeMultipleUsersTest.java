
/*
 Copyright (c) 2008, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

package org.cytoscape.model;

import com.clarkware.junitperf.LoadTest;
import com.clarkware.junitperf.TimedTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;


/**
 * Created by IntelliJ IDEA. User: skillcoy Date: Sep 19, 2008 Time: 4:07:31 PM To change this
 * template use File | Settings | File Templates.
 */
public class TimedAddNodeMultipleUsersTest extends TestCase {
	private CyNetwork net;
	private int totalNodes = 100000;

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public static Test suite() {
		long maxTimeInMillis = 1000;
		int concurrentUsers = 3;
		Test test = new TimedAddNodeMultipleUsersTest("testLoadNetwork");
		Test loadTest = new LoadTest(new TimedTest(test, maxTimeInMillis), concurrentUsers);

		return loadTest;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param args DOCUMENT ME!
	 *
	 * @throws Exception DOCUMENT ME!
	 */
	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * Creates a new TimedAddNodeMultipleUsersTest object.
	 *
	 * @param name  DOCUMENT ME!
	 */
	public TimedAddNodeMultipleUsersTest(String name) {
		super(name);
		net = CyNetworkFactory.getInstance(); 
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void setUp() {
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void tearDown() {
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void testLoadNetwork() {
		int localCount = 0;

		for (int i = 0; i < totalNodes; i++)
			if (net.addNode() != null)
				localCount++;

		assertEquals(localCount, totalNodes);
	}
}
