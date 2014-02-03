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

import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;

public class SdsAdapterTest extends TestCase {

	// ==============================================================================

	HomeBusFactory factory;
	Directory hsRoot;

	// ==============================================================================

	protected void setUp() throws Exception {

		System.out.println(" Global setup " + SdsAdapterTest.class.getName());

		factory = new HomeBusFactory(null);
		hsRoot = factory.getHsRoot();
		recursiveDelete(hsRoot);
	}

	protected void tearDown() throws Exception {

		System.out
				.println(" Global tearDown " + SdsAdapterTest.class.getName());

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

	public void test_simple() throws Exception {
		//
		// // 1. create nodes
		// Node node1 = factory.createNode("hlcNodeId1", "hlcDeviceId1",
		// "hlcNodeName1");
		//
		// node1.setManufacturer("hlcManufacturer1");
		// node1.setVersion("hlcVersion1");
		//
		// // 1.1. publish node
		// boolean result = node1.publishOnHomeBus();
		// assertTrue("publication 1 successful", result);
		//
		// // Tools.displayTreeRepresentation(this.deviceId, hsRoot, "", true);
		//
		// // 2. create connector
		// HlcConnector connector = node1.getHlcConnector();
		// connector.addNodeDiscoveryListener(new NodeDiscoveryListener() {
		//
		// public void onNodeRemoval(String nodeId) {
		// // TODO Auto-generated method stub
		// int toto = 0;
		// toto = 1;
		// }
		//
		// public void onNodeModification(String nodeId) {
		// // TODO Auto-generated method stub
		// // TODO Auto-generated method stub
		// int toto = 0;
		// toto = 1;
		//
		// }
		//
		// public void onNodeArrival(String nodeId) {
		// // TODO Auto-generated method stub
		// // TODO Auto-generated method stub
		// int toto = 0;
		// toto = 1;
		//
		// }
		// });
		//
		// // 3. create a new node
		// Node node2 = factory.createNode("hlcNodeId2", "hlcDeviceId2",
		// "hlcNodeName2");
		//
		// node2.setManufacturer("hlcManufacturer2");
		// node2.setVersion("hlcVersion2");
		//
		// // 2.1. publish node
		// result = node2.publishOnHomeBus();
		// assertTrue("publication 2 successful", result);
		//
		// // Tools.displayTreeRepresentation(this.deviceId, hsRoot, "", true);
		//
	}

}
