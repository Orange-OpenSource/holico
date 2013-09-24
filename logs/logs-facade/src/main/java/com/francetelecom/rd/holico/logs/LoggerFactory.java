/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.holicologs.logs-facade
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
package com.francetelecom.rd.holico.logs;

import com.francetelecom.rd.holico.logs.nopimpl.NopLoggerFactory;

public final class LoggerFactory {

	private static String LOGGER_FACTORY_CLASS = "com.francetelecom.rd.holico.logs.impl.LoggerFactoryImpl";
	private static boolean isInitialized;

	private static ILoggerFactory loggerFactory;

	/**
	 * prevent instantiation
	 */
	private LoggerFactory() {

	}

	/**
	 * Get a logger for a log source.
	 * 
	 * @param source
	 * @return the logger for this Log source
	 */
	public static Logger getLogger(String source) {
		ILoggerFactory factory = getILoggerFactory();

		return factory.getLogger(source);
	}

	public static synchronized ILoggerFactory getILoggerFactory() {
		if (!isInitialized) {

			// perform init

			try {
				Class factoryClass = LoggerFactory.class.getClassLoader()
						.loadClass(LOGGER_FACTORY_CLASS);

				loggerFactory = (ILoggerFactory) factoryClass.newInstance();
				isInitialized = true;
			} catch (InstantiationException e) {
				System.out
						.println("Cannot get logger : InstantiationException "
								+ e.getMessage());
				System.out
						.println("Using Nop implementation : you will have no logs !");
				loggerFactory = new NopLoggerFactory();
			} catch (IllegalAccessException e) {
				System.out
						.println("Cannot get logger : IllegalAccessException "
								+ e.getMessage());
				System.out
						.println("Using Nop implementation : you will have no logs !");
				loggerFactory = new NopLoggerFactory();

			} catch (ClassNotFoundException e) {
				System.out
						.println("Cannot get logger : no suitable implementation found "
								+ e.getMessage());
				System.out
						.println("Using Nop implementation : you will have no logs !");
				loggerFactory = new NopLoggerFactory();
			}

		}
		return loggerFactory;
	}

}