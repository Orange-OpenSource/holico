/**
 * Holico : Proposition d'implementation du HomeBus Agora, repondant aux besoins exprimes dans l'Agora du reseau domiciliaire
 *
 * Module name: com.francetelecom.rd.agoralogs.LogStandardImpl
 * Version:     0.1-SNAPSHOT
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
package com.francetelecom.rd.holico.logs.impl;

import com.francetelecom.rd.holico.logs.Logger;

/**
 * This is a standard implementation of the logger interface 
 * 
 * @author Pierre Rust
 *
 */
public class StandardLogger implements Logger {

	private final java.util.logging.Logger jdkLogger;
	
	public StandardLogger(String source) {
		if (source == null ) {
			throw new IllegalArgumentException("Logger source cannot be null");
		}
		jdkLogger = java.util.logging.Logger.getLogger(source);
	}

	public void debug(String message) {
		jdkLogger.fine(message);
	}

	public void info(String message) {
		jdkLogger.info(message);

	}

	public void warn(String message) {
		jdkLogger.warning(message);
	}

	public void warn(String message, Throwable t) {

		jdkLogger.warning(message);
	}

	public void error(String message) {
		jdkLogger.severe(message);
	}

	public void error(String message, Throwable t) {
		jdkLogger.severe(message);
	}

}
