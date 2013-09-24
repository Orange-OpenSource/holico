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
 * An NodeServiceCallBack is an interface that is implemented on the
 * application side for each action it wants to perform when some
 * specific resourceInstance is published.
 * 
 * NodeServiceCallback are registered on the Node Object. The corresponding
 * NodeService is automatically published on the Bus and the callback will be called
 * if a configuration for this NodeService exists and is triggered.
 * 
 * FIXME : private on service or callback ? 
 * If a NodeServiceCallBack is considered as private, it can not be invoked by any
 * other node and only it's owner may set configuration for this NodeService.
 * 
 * @author Pierre Rust (tksh1670)
 *
 */
public interface NodeServiceCallback 
{
	
	
	/**
	 * The parameter name, only used for GUI
	 * 
	 * @return the parameter name
	 */
	public String getParameterName();
	
	/**
	 * Return the parameter type. 
	 * The parameter types are defines by the <code>TYPE_VALUE_[xx]</code> 
	 * constants in {@link Resource}.
	 *  
	 * @return
	 */
	public int getParameterType();
	
	
	/**
	 * 
	 * @param parameter
	 */
	public void onServiceActivated(Object parameter);
	
	
}
