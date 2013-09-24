/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.holico.hlc-connector-interface
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
package com.francetelecom.rd.hlc;

/**
 * 
 * Interface NodeInfo. A NodeInfo gives a read-only view of an Node with its
 * properties.
 * 
 * 
 * 
 * @author Pierre Rust (tksh1670)
 * 
 */
public interface NodeInfo {

	/**
	 * Home Bus Node Unique Id.
	 * <p>
	 * Each Node has a nodeId, this id is unique on the bus and MUST be
	 * persisted by the application in order to be stable across reboots.
	 * 
	 * @return the nodeId
	 */
	String getNodeId();

	/**
	 * Identifier of the device which embeds this node.
	 * <p>
	 * A device is a physical connected object on the LAN. One or several Nodes
	 * may run on a single (physical) device. The deviceId identifies uniquely a
	 * device on the LAN and MUST be persisted on order to be stable across
	 * reboots.
	 * 
	 * @return the deviceId
	 */
	String getDeviceId();

	/**
	 * Name of the node (human readable string).
	 * <p>
	 * The node name is a human readable (and understandable ) string of
	 * characters that is used represents the Node on Graphical User Interfaces.
	 * It should be reasonably unique (but this is not enforced by the system)
	 * and allow a user to recognize which equipments and services he is dealing
	 * with.
	 * 
	 * @return the Node Name
	 */
	String getName();

	/**
	 * The manufacturer/provider of the node (human readable string).
	 * 
	 * @return the manufacturer
	 */
	String getManufacturer();

	/**
	 * A string identifying the version of the application, bundle or firmware.
	 * 
	 * @return
	 */
	String getVersion();

	/**
	 * Get the timestamp corresponding to the last time this Node was alive.
	 * 
	 * @return a timestamp
	 */
	String getKeepAlive();

	/**
	 * get the current availability of the node.
	 * <p>
	 * The value can be one of :
	 * <ul>
	 * <li>NODE_AVAILABILITY_AVAILABLE</li>
	 * <li>NODE_AVAILABILITY_NOTAVAILABLE</li>
	 * </ul> 
	 * 
	 * 
	 * @return
	 */
	Integer getAvailability();
	
	
	/**
	 * Each Node may publish one or several NodeServices on the bus.
	 * 
	 * @see NodeService
	 * 
	 * @return the list of NodeService for this Node
	 */
	NodeService[] getNodeServices();

	/**
	 * Each Node may publish data (as ResourceInstance) on one or several
	 * Resource in the HLC data-tree.
	 * 
	 * @see ResourcePublication, Resource and ResourceInstance
	 * 
	 * @return the list of publication declarations for this Node
	 */
	ResourcePublication[] getResourcePublications();

}
