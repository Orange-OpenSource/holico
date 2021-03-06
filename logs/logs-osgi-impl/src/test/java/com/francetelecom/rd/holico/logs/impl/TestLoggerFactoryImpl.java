/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.holicologs.logs-osgi-impl
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

import junit.framework.TestCase;

import com.francetelecom.rd.holico.logs.ILoggerFactory;
import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;

/**
 * This is a jdk log implementation of the {@link ILoggerFactory}
 * interface.
 * 
 * @author Pierre Rust
 *
 */
public class TestLoggerFactoryImpl extends TestCase {


	public void testGetfactory() {
		
		// test we actually get a concrete factory 
		ILoggerFactory factory =   LoggerFactory.getILoggerFactory();
		assertNotNull(factory);
			
		// check we actually get the standard factory and not the nop factory
		assertTrue((factory instanceof com.francetelecom.rd.holico.logs.impl.LoggerFactoryImpl ));
		
	}
	
	public void testGetLogger() {
		// this test will not work outside osgi container !
		//Logger logger = LoggerFactory.getLogger("TestLog");
		//assertNotNull(logger);
		
		//assertTrue((logger instanceof OsgiLogger));
		
	}

	public void testLogger() {
		// this test will not work outside osgi container !
		//Logger logger = LoggerFactory.getLogger("TestLog");
		
		//logger.debug("Test Debug");
		//logger.info("Test Info");
		//logger.warn("Test Warn");
		//logger.error("Test Error");
	}	
}
