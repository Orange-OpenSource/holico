/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.holico-tools.node-simulator-android
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
package com.francetelecom.rd.app.nodessimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.support.v4.app.FragmentActivity;

import com.francetelecom.rd.app.nodessimulator.devices.Alarm;
import com.francetelecom.rd.app.nodessimulator.devices.NodeManager;
import com.francetelecom.rd.app.nodessimulator.devices.Shutter;
import com.francetelecom.rd.app.nodessimulator.devices.Sprinkler;


public class NodesListManager {

	// ====================================================================
	
	/**
	 * An array of sample (dummy) items.
	 */
	public static List<NodeManager> ITEMS = new ArrayList<NodeManager>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, NodeManager> ITEM_MAP = new HashMap<String, NodeManager>();

	// ====================================================================
	
	public NodesListManager(FragmentActivity activity) {
		ITEMS.clear();
		ITEM_MAP.clear();
		
		// Add Nodes.
		String nodeId = "shutter_1"; //Tools.generateId();
		addItem(new Shutter(nodeId, "shutter", NodeManager.NodeType.SHUTTER, activity));
		nodeId = "sprinkler_1"; //Tools.generateId();
		addItem(new Sprinkler(nodeId, "sprinkler", NodeManager.NodeType.SPRINKLER, activity));
		nodeId = "alarm_1"; //Tools.generateId();
		addItem(new Alarm(nodeId, "alarm", NodeManager.NodeType.ALARM, activity));
		nodeId = "separator"; //Tools.generateId();
		addItem(new NodeManager(nodeId, "...", "", "", NodeManager.NodeType.UNKNOWN, "", activity));
		nodeId = "datamodel"; //Tools.generateId();
		addItem(new NodeManager(nodeId, "data model", "", "", NodeManager.NodeType.DATAMODEL, "", activity));
	}

	// ====================================================================
	
	private void addItem(NodeManager item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}
	
}
