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

import com.francetelecom.rd.hlc.NodeInfo;
import com.francetelecom.rd.hlc.NodeService;
import com.francetelecom.rd.hlc.ResourcePublication;

public class NodeInfoImpl implements NodeInfo {

	// ==============================================================================

	final private String nodeId;
	final private String deviceId;
	final private String name;
	final private String manufacturer;
	final private String version;
	final private String keepAlive;
	final private Integer availability;
	final private NodeService[] nodeServices;
	final private ResourcePublication[] resourcePublications;

	// ==============================================================================

	/**
	 * 
	 * @param nodeId
	 *            the unique identifier of the Node
	 * 
	 * @param deviceId
	 *            the unique identifier of the Device
	 * 
	 * @param name
	 *            the human readable (and understandable ) Node name
	 * 
	 * @param manufacturer
	 *            the manufacturer of the Node
	 * 
	 * @param version
	 *            the version of the Device
	 * 
	 * @param keepAlive
	 *            the timestamp corresponding to the last time this Node was
	 *            alive
	 * 
	 * @param nodeServices
	 *            NodeServices published by the Node
	 * 
	 * @param resourcePublications
	 *            ResourcePublications published by the Node
	 */
	public NodeInfoImpl(final String nodeId, final String deviceId,
			final String name, final String manufacturer, final String version,
			final String keepAlive, final Integer availability, final NodeService[] nodeServices,
			final ResourcePublication[] resourcePublications) {

		this.nodeId = nodeId;
		this.deviceId = deviceId;
		this.name = name;
		this.manufacturer = manufacturer;
		this.version = version;
		this.keepAlive = keepAlive;
		this.availability = availability;
		this.nodeServices = nodeServices;
		this.resourcePublications = resourcePublications;

	}

	// ==============================================================================

	public String getNodeId() {
		return this.nodeId;
	}

	public String getDeviceId() {
		return this.deviceId;
	}

	public String getName() {
		return this.name;
	}

	public NodeService[] getNodeServices() {
		return this.nodeServices;
	}

	public ResourcePublication[] getResourcePublications() {
		return this.resourcePublications;
	}

	public String getManufacturer() {
		return this.manufacturer;
	}

	public String getVersion() {
		return this.version;
	}

	public String getKeepAlive() {
		return this.keepAlive;
	}
	
	public Integer getAvailability() {
		return this.availability;
	}


	// ==============================================================================

}
