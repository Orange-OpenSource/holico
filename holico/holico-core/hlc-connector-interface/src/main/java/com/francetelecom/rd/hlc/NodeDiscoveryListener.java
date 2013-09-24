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
 * This listener interface is used to be notified on any Node Change.
 * <p>
 * It must be registered on the {@link HlcConnector} using 
 * {@link HlcConnector#addNodeDiscoveryListener(NodeDiscoveryListener)}.  
 * 
 * 
 * @author Pierre Rust (tksh1670)
 *
 */
public interface NodeDiscoveryListener {

	/**
	 * called when the node becomes available (Availability = 1) <p>
	 * could be first appearance of the node, or an already existent node which becomes available
	 * @param nodeId
	 */
	public void onNodeArrival(String nodeId);
	
	/**
	 * called when the node (and its parameters) is removed from the data tree 
	 * @param nodeId
	 */
	public void onNodeRemoval(String nodeId);
	
	/**
	 * called when the node's parameter is modified
	 * @param nodeId
	 */
	public void onNodeModification(String nodeId);
	
	/**
	 * called when the node has become unavailable, it has been unpublished from the home bus (Availability = 0)
	 * @param nodeId
	 */
	public void onNodeUnavailable(String nodeId);
	
}
