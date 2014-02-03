/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.holico.hlc-connector-impl
 * Version:     0.4-SNAPSHOT
 *
 * Copyright (C) 2013 Orange
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Orange nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * 	http://opensource.org/licenses/BSD-3-Clause
 */
package com.francetelecom.rd.hlc.impl;

import junit.framework.TestCase;

import com.francetelecom.rd.hlc.Condition;
import com.francetelecom.rd.hlc.InvalidResourceTypeException;
import com.francetelecom.rd.hlc.Node;
import com.francetelecom.rd.hlc.Resource;
import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;

public class ConditionImplTest extends TestCase {

	// ==============================================================================

	int deviceId = 125;
	HomeBusFactory factory;
	Directory hsRoot;

	// ==============================================================================

	protected void setUp() throws Exception {

		System.out
				.println(" Global setup " + ConditionImplTest.class.getName());

		factory = new HomeBusFactory(null);
		hsRoot = factory.getHsRoot();
		recursiveDelete(hsRoot);
	}

	protected void tearDown() throws Exception {

		System.out.println(" Global tearDown "
				+ ConditionImplTest.class.getName());

		// clean all HS tree for real unit tests !
		hsRoot = factory.getHsRoot();
		recursiveDelete(hsRoot);

	}

	private void recursiveDelete(Directory dir) {
		Data[] childs = dir.getChildren();
		for (int i = 0; i < childs.length; ++i) {
			if (childs[i].getType() == Data.TYPE_GEN_DIR
					|| childs[i].getType() == Data.TYPE_SPE_DIR) {
				recursiveDelete((Directory) childs[i]);
				try {
					dir.deleteData(childs[i].getName());
				} catch (DataAccessException e) {
					e.printStackTrace();
				}
			} else {
				try {
					dir.deleteData(childs[i].getName());
				} catch (DataAccessException e) {
					e.printStackTrace();
				}
			}
		}

	}

	// ==============================================================================

	public void test_badConditionCreation() throws Exception {

		hsRoot = factory.getHsRoot();

		// 1. create conditions
		Condition condition = null;

		try {
			condition = null;
			condition = factory.createCondition(Condition.OPERATOR_DIFF,
					new Integer(1), "Topic[Room].Test.NewRef1");
			assertFalse(condition == null);
		} catch (IllegalArgumentException irte) {
			assertTrue("Argument error detected", false);
		}

		try {
			condition = null;
			condition = factory.createCondition(-1, new Integer(1),
					"Topic[Room].Test.NewRef1");
			fail("Must detect an argument error : hsRoot");
		} catch (IllegalArgumentException irte) {
			assertTrue("Argument error detected", true);
		}

		try {
			condition = null;
			condition = factory.createCondition(Condition.OPERATOR_DIFF,
					new Integer(1), "");
			fail("Must detect an argument error : resourcePath");
		} catch (IllegalArgumentException irte) {
			assertTrue("Argument error detected", true);
		}
	}

	// ==============================================================================

	public void test_IntVsInt() throws Exception {

		// 1. create node
		Node node = factory.createNode("condNodeId", "condDeviceId",
				"condNodeName");

		// 1.1. publish it
		boolean result = node.publishOnHomeBus();
		assertTrue("publication successful", result);

		// 1.2. add publications
		node.addResourcePublication("pub1", "Topic[Room].Test.NewRef1",
				Resource.TYPE_VALUE_INT);

		// 2. set resource value
		Integer val = new Integer(1);
		node.publishOnResource("Topic[Room].Test.NewRef1", val);

		hsRoot = factory.getHsRoot();

		// 3. create conditions
		Condition condition = null;
		boolean condSatisfied = false;

		condition = factory.createCondition(Condition.OPERATOR_DIFF,
				new Integer(1), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertFalse(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_DIFF,
				new Integer(2), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertTrue(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_EQUAL,
				new Integer(2), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertFalse(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_EQUAL,
				new Integer(1), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertTrue(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_INF,
				new Integer(1), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertFalse(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_INF,
				new Integer(2), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertTrue(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_INFEQUAL,
				new Integer(0), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertFalse(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_INFEQUAL,
				new Integer(1), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertTrue(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_SUP,
				new Integer(1), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertFalse(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_SUP,
				new Integer(0), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertTrue(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_SUPEQUAL,
				new Integer(2), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertFalse(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_SUPEQUAL,
				new Integer(1), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertTrue(condSatisfied);

	}

	public void test_IntVsString() throws Exception {

		// 1. create node
		Node node = factory.createNode("condNodeId", "condDeviceId",
				"condNodeName");

		// 1.1. publish it
		boolean result = node.publishOnHomeBus();
		assertTrue("publication successful", result);

		// 1.2. add publications
		node.addResourcePublication("pub1", "Topic[Room].Test.NewRef1",
				Resource.TYPE_VALUE_STRING);

		// 2. set resource value
		node.publishOnResource("Topic[Room].Test.NewRef1", "test");

		hsRoot = factory.getHsRoot();

		// 3. create conditions
		Condition condition = null;

		condition = factory.createCondition(Condition.OPERATOR_DIFF,
				new Integer(1), "Topic[Room].Test.NewRef1");

		try {
			condition.isSatisfied();
			fail("Must refuse comparaison with different types between resource and target");
		} catch (InvalidResourceTypeException irte) {
			assertTrue("Type error detected", true);
		}
	}

	// ==============================================================================

	public void test_StringVsString() throws Exception {

		// 1. create node
		Node node = factory.createNode("condNodeId", "condDeviceId",
				"condNodeName");

		// 1.1. publish it
		boolean result = node.publishOnHomeBus();
		assertTrue("publication successful", result);

		// 1.2. add publications
		node.addResourcePublication("pub1", "Topic[Room].Test.NewRef1",
				Resource.TYPE_VALUE_STRING);

		// 2. set resource value
		node.publishOnResource("Topic[Room].Test.NewRef1", "test");

		hsRoot = factory.getHsRoot();

		// 3. create conditions
		Condition condition = null;
		boolean condSatisfied = false;

		condition = factory.createCondition(Condition.OPERATOR_DIFF, "test",
				"Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertFalse(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_DIFF, "test2",
				"Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertTrue(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_EQUAL, "test2",
				"Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertFalse(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_EQUAL, "test",
				"Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertTrue(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_INF, "toto",
				"Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertFalse(condSatisfied);
	}

	public void test_StringVsBoolean() throws Exception {

		// 1. create node
		Node node = factory.createNode("condNodeId", "condDeviceId",
				"condNodeName");

		// 1.1. publish it
		boolean result = node.publishOnHomeBus();
		assertTrue("publication successful", result);

		// 1.2. add publications
		node.addResourcePublication("pub1", "Topic[Room].Test.NewRef1",
				Resource.TYPE_VALUE_STRING);

		// 2. set resource value
		node.publishOnResource("Topic[Room].Test.NewRef1", "test");

		hsRoot = factory.getHsRoot();

		// 3. create conditions
		Condition condition = null;

		condition = factory.createCondition(Condition.OPERATOR_DIFF,
				new Boolean(false), "Topic[Room].Test.NewRef1");

		try {
			condition.isSatisfied();
			fail("Must refuse comparaison with different types between resource and target");
		} catch (InvalidResourceTypeException irte) {
			assertTrue("Type error detected", true);
		}
	}

	// ==============================================================================

	public void test_BooleanVsBoolean() throws Exception {

		// 1. create node
		Node node = factory.createNode("condNodeId", "condDeviceId",
				"condNodeName");

		// 1.1. publish it
		boolean result = node.publishOnHomeBus();
		assertTrue("publication successful", result);

		// 1.2. add publications
		node.addResourcePublication("pub1", "Topic[Room].Test.NewRef1",
				Resource.TYPE_VALUE_BOOL);

		// 2. set resource value
		Boolean value = new Boolean(true);
		node.publishOnResource("Topic[Room].Test.NewRef1", value);

		hsRoot = factory.getHsRoot();

		// 3. create conditions
		Condition condition = null;
		boolean condSatisfied = false;

		condition = factory.createCondition(Condition.OPERATOR_DIFF,
				new Boolean(true), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertFalse(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_DIFF,
				new Boolean(false), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertTrue(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_EQUAL,
				new Boolean(false), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertFalse(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_EQUAL,
				new Boolean(true), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertTrue(condSatisfied);

		condition = factory.createCondition(Condition.OPERATOR_INF,
				new Boolean(true), "Topic[Room].Test.NewRef1");
		condSatisfied = condition.isSatisfied();
		assertFalse(condSatisfied);
	}

	public void test_BooleanVsString() throws Exception {

		// 1. create node
		Node node = factory.createNode("condNodeId", "condDeviceId",
				"condNodeName");

		// 1.1. publish it
		boolean result = node.publishOnHomeBus();
		assertTrue("publication successful", result);

		// 1.2. add publications
		node.addResourcePublication("pub1", "Topic[Room].Test.NewRef1",
				Resource.TYPE_VALUE_BOOL);

		// 2. set resource value
		Boolean value = new Boolean(true);
		node.publishOnResource("Topic[Room].Test.NewRef1", value);

		hsRoot = factory.getHsRoot();

		// 3. create conditions
		Condition condition = null;

		condition = factory.createCondition(Condition.OPERATOR_DIFF, "test",
				"Topic[Room].Test.NewRef1");

		try {
			condition.isSatisfied();
			fail("Must refuse comparaison with different types between resource and target");
		} catch (InvalidResourceTypeException irte) {
			assertTrue("Type error detected", true);
		}
	}
}
