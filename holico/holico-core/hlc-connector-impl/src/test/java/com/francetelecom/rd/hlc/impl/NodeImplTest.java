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

import com.francetelecom.rd.hlc.HomeBusException;
import com.francetelecom.rd.hlc.InvalidResourcePathException;
import com.francetelecom.rd.hlc.InvalidResourceTypeException;
import com.francetelecom.rd.hlc.Node;
import com.francetelecom.rd.hlc.Resource;
import com.francetelecom.rd.hlc.ResourcePublication;
import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;

public class NodeImplTest extends TestCase {

	HomeBusFactory factory;

	protected void setUp() throws Exception {
		int deviceId = 125;
		factory = new HomeBusFactory(deviceId);
		// Directory hsRoot = factory.getHsRoot();
		// recursiveDelete(hsRoot);

	}

	protected void tearDown() throws Exception {

		// clean all HS tree for real unit tests !
		Directory hsRoot = factory.getHsRoot();
		recursiveDelete(hsRoot);

		System.out.println(" Global tearDown ");
	}

	private void recursiveDelete(Directory dir) throws DataAccessException {
		Data[] childs = dir.getChildren();
		for (int i = 0; i < childs.length; ++i) {
			if (childs[i].getType() == Data.TYPE_GEN_DIR
					|| childs[i].getType() == Data.TYPE_SPE_DIR) {
				recursiveDelete((Directory) childs[i]);
				dir.deleteData(childs[i].getName());
			} else {
				dir.deleteData(childs[i].getName());
			}
		}

	}

	public void test_addOnePublication_unpublished() throws HomeBusException,
			InvalidResourceTypeException, InvalidResourcePathException {

		// normal creation
		Node node = factory.createNode("nodeId", "deviceId", "nodeName");

		boolean result = node.addResourcePublication("pub1",
				"Media[Salon].HomeCinema.Mode", Resource.TYPE_VALUE_STRING);

		assertFalse("Node is not published yet", result);
		ResourcePublication[] pubs = node.getResourcePublications();
		assertEquals("Number of publication ", 1, pubs.length);

		assertEquals(" resourcePath for publication",
				"Media[Salon].HomeCinema.Mode", pubs[0].getResourcePath());
		assertEquals(" type for publication", Resource.TYPE_VALUE_STRING,
				Tools.getConnectorTypeFromSdsType(pubs[0].getType()));
		assertEquals(" index for publication", "" + 0, pubs[0].getId());

	}

	public void test_addTwoPublication_unpublished() throws Exception {

		Node node = factory.createNode("nodeId", "deviceId", "nodeName");

		boolean result1 = node.addResourcePublication("pub1",
				"Media[Salon].HomeCinema.Mode", Resource.TYPE_VALUE_STRING);
		assertFalse("Node is not published yet", result1);

		boolean result2 = node.addResourcePublication("pub2",
				"Telecom.CallStatus", Resource.TYPE_VALUE_STRING);
		assertFalse("Node is not published yet", result2);

		ResourcePublication[] pubs = node.getResourcePublications();
		assertEquals("Number of publication ", 2, pubs.length);

		assertEquals(" resourcePath for publication 1",
				"Media[Salon].HomeCinema.Mode", pubs[0].getResourcePath());
		assertEquals(" type for publication", Resource.TYPE_VALUE_STRING,
				Tools.getConnectorTypeFromSdsType(pubs[0].getType()));
		assertEquals(" index for publication", "" + 0, pubs[0].getId());

		assertEquals(" resourcePath for publication 2", "Telecom.CallStatus",
				pubs[1].getResourcePath());
		assertEquals(" type for publication", Resource.TYPE_VALUE_STRING,
				Tools.getConnectorTypeFromSdsType(pubs[1].getType()));
		assertEquals(" index for publication", "" + 1, pubs[1].getId());
	}

	public void test_addDuplicatePublication_unpublished() throws Exception {

		Node node = factory.createNode("nodeId", "deviceId", "nodeName");

		node.addResourcePublication("pub1", "Media[Salon].HomeCinema.Mode",
				Resource.TYPE_VALUE_STRING);
		boolean result = node.addResourcePublication("pub2",
				"Media[Salon].HomeCinema.Mode", Resource.TYPE_VALUE_STRING);

		assertFalse("Duplicate declaration ", result);
	}

	public void test_addConflictingTypeDeclaration_unpublished()
			throws Exception {

		Node node = factory.createNode("nodeId", "deviceId", "nodeName");

		node.addResourcePublication("pub1", "Media[Salon].HomeCinema.Mode",
				Resource.TYPE_VALUE_STRING);
		boolean result = node.addResourcePublication("pub2",
				"Media[Salon].HomeCinema.Mode.pouet", Resource.TYPE_VALUE_INT);
		assertFalse("Cannot detect this kind of conflict without publication",
				result);

	}

	public void test_addConflictingPathDeclaration_unpublished()
			throws Exception {

		Node node = factory.createNode("nodeId", "deviceId", "nodeName");

		node.addResourcePublication("pub1", "Media[Salon].HomeCinema.Mode",
				Resource.TYPE_VALUE_STRING);
		boolean result = false;
		try {
			result = node.addResourcePublication("pub2",
					"Media[Salon].HomeCinema.Mode", Resource.TYPE_VALUE_INT);
			fail("Must refuse conflicting type publications");
		} catch (InvalidResourceTypeException irte) {
			assertFalse("Conflict detected", result);
		}

	}

	public void test_setName_unpublished() throws Exception {
		Node node = factory.createNode("nodeId", "deviceId", "nodeName");
		node.setName("newName");

		assertEquals("newName", node.getName());
	}

	public void test_simplePublish_published() throws Exception {
		Node node = factory.createNode("nodeId", "deviceId", "nodeName");

		boolean result = node.publishOnHomeBus();
		assertTrue("publication successful", result);
	}

	public void test_duplicatePublish_published() throws Exception {
		Node node = factory.createNode("nodeId", "deviceId", "nodeName");

		boolean result = node.publishOnHomeBus();
		assertTrue("publication successful", result);

		// Duplicate publication is actually a very common case.
		// For example, it happens every time a device reboots :
		// during the reboot, the device is physically disconnected but the node
		// declaration still lives in the shared data structure. When the device
		// reconnects, it will re-declare it-self.

		Node node2 = factory.createNode("nodeId", "deviceId", "nodeName");
		boolean result2 = node2.publishOnHomeBus();
		assertTrue("publication successful", result2);

	}

	public void test_duplicatePublishNameChange_published() throws Exception {
		Node node = factory.createNode("nodeId", "deviceId", "nodeName");

		boolean result = node.publishOnHomeBus();
		assertTrue("publication successful", result);

		// name change is legal for republication

		Node node2 = factory.createNode("nodeId", "deviceId", "newNodeName");
		boolean result2 = node2.publishOnHomeBus();
		assertTrue("publication successful", result2);

	}

	// public void test_duplicatePublishWrongDeviceId_published() throws
	// Exception {
	// Node node = factory.createNode("nodeId", "deviceId", "nodeName");
	//
	// boolean result = node.publishOnHomeBus();
	// assertTrue("publication successful", result);
	//
	// // Duplicate publication does not allow the deviceId to change
	//
	// try {
	// Node node2 = factory.createNode("nodeId", "deviceId2", "nodeName");
	// fail("Must detect invalid duplicate Node declaration");
	// } catch (HomeBusException hbe) {
	// assertTrue("Invalid Duplicate declaration detected ", true);
	// }
	// }

	public void test_setName_published() throws Exception {
		Node node = factory.createNode("nodeId", "deviceId", "nodeName");

		boolean result = node.publishOnHomeBus();
		assertTrue("publication successful", result);

		node.setName("newName");
		assertEquals("newName", node.getName());
	}

	public void test_addConflictingPathDeclaration_published() throws Exception {

		Node node = factory.createNode("nodeId", "deviceId", "nodeName");

		node.publishOnHomeBus();

		node.addResourcePublication("pub1", "Media[Salon].HomeCinema.Mode",
				Resource.TYPE_VALUE_STRING);
		boolean result = false;
		try {
			result = node.addResourcePublication("pub2",
					"Media[Salon].HomeCinema.Mode.subType",
					Resource.TYPE_VALUE_STRING);
			fail("Must refuse conflicting type publications");
		} catch (InvalidResourceTypeException irte) {
			assertTrue("Conflict detected", true);
		}
	}

}
