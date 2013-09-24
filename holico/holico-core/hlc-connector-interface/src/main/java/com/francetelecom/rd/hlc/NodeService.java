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
 * A NodeService object represents a service provided by a Node. It is published
 * in the <i>Home Bus Data Tree<i> and can invoked by setting a {@link Rule}.
 * 
 * @author Pierre rust (tksh1670)
 * 
 */
public class NodeService {
	// ==============================================================================

	final private String nodeServiceId;
	final private boolean isPrivate;
	final private String name;
	final private String parameterName;
	final private int parameterType;

	// ==============================================================================

	/**
	 * 
	 * @param nodeServiceId
	 *            the unique identifier of the NodeService
	 * 
	 * @param isPrivate
	 *            true if the NodeService is private
	 * 
	 * @param name
	 *            the human readable (and understandable ) NodeService name
	 * 
	 * @param parameterName
	 *            the NodeService parameter name
	 * 
	 * @param parameterType
	 *            the NodeService parameter type
	 */
	public NodeService(final String nodeServiceId, final boolean isPrivate,
			final String name, final String parameterName,
			final int parameterType) {

		this.nodeServiceId = nodeServiceId;
		this.isPrivate = isPrivate;
		this.name = name;
		this.parameterName = parameterName;
		this.parameterType = parameterType;

	}

	// ==============================================================================
	/**
	 * 
	 * @return the id of this {@link NodeService}
	 */
	public String getNodeServiceId() {

		return this.nodeServiceId;
	}

	/**
	 * Indicate if the {@link NodeService} is private.
	 * <p>
	 * When a {@link NodeService} is private, it can not be invoked by any other
	 * node and only it's owner may set configuration for this
	 * {@link NodeService}.
	 * 
	 * @return
	 */
	public boolean isPrivate() {

		return this.isPrivate;
	}

	/**
	 * The parameter name is used for GUI representation, it should help the
	 * user to understand what this service does.
	 * 
	 * @return
	 */
	public String getName() {

		return this.name;
	}

	/**
	 * The parameter name is used for GUI representation, it should help the
	 * user to understand the kind of input this service requires.
	 * 
	 * @return the name of the parameter
	 */
	public String getParameterName() {

		return this.parameterName;
	}

	/**
	 * The type of the parameter is based on the constants defined in
	 * {@link Data#Type_xx}
	 * 
	 * @return the parameter type
	 */
	public int getParameterType() {

		return this.parameterType;
	}

}
