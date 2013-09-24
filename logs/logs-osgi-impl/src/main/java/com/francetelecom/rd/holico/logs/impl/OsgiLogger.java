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

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import com.francetelecom.rd.holico.logs.Logger;

/**
 * This is a Osgi implementation of the logger interface 
 * 
 * @author Pierre Rust
 *
 */
public class OsgiLogger implements Logger {

	private final LogService logService;
	private final ServiceReference serviceRef;
	 private boolean detailed = true;
	
	
	public OsgiLogger(LogService logService, String source) {
		if (source == null  || logService == null) {
			throw new IllegalArgumentException("Logger source and LogService cannot be null");
		}
		this.logService = logService;
		this.serviceRef = null;
	}

	public OsgiLogger(LogService logService, ServiceReference serviceRef, String source) {
		if (source == null  || logService == null) {
			throw new IllegalArgumentException("Logger source and LogService cannot be null");
		}
		this.logService = logService;
		this.serviceRef = serviceRef;
	}	
	
	public void debug(String message) {
		internalLog(LogService.LOG_DEBUG, message);
	}

	public void info(String message) {
		internalLog(LogService.LOG_INFO, message);

	}

	public void warn(String message) {
		internalLog(LogService.LOG_WARNING, message);
	}

	public void warn(String message, Throwable t) {
		internalLog(LogService.LOG_WARNING, message, t);

	}

	public void error(String message) {
		internalLog(LogService.LOG_ERROR, message);
	}

	public void error(String message, Throwable t) {
		internalLog(LogService.LOG_ERROR, message, t);
	}
	

	
	 private void internalLog(int level, String message) {
		 internalLog(level, message, null);
	 }
    /**
     * Check the availability of the OSGi logging service, and use it is available.
     * Does nothing otherwise.
     *
     * @param level   Log level, one of LogLevel.LOG_...
     * @param message Log message text
     * @param t       Throwable to log or null
     */
    private void internalLog(int level, String message, Throwable t) {


        StackTraceElement callerInfo = null;
        if (detailed) {
            callerInfo = new Exception().getStackTrace()[2];
        }
        if (logService != null) {
            try {
            	if (serviceRef != null) {
	                if (t != null) {
	                    logService.log(serviceRef, level, createMessagePart(level, callerInfo, message + ""), t);
	                } else {
	                    logService.log(serviceRef, level, createMessagePart(level, callerInfo, message + ""));
	                }
            	} else 
            	{
	                if (t != null) {
	                    logService.log( level, createMessagePart(level, callerInfo, message + ""), t);
	                } else {
	                    logService.log( level, createMessagePart(level, callerInfo, message + ""));
	                }
            	}
            } catch (Exception ignore) {
                // Service may have become invalid, ignore any error until the log service reference is
                // updated by the log factory.
            }
        } 
    }


    private static String createMessagePart(int logLevel, StackTraceElement stackTraceElement, String message) {
        if (logLevel == LogService.LOG_INFO) {
            return message;
        }
        if (stackTraceElement != null) {
            return message + " ;; (" + "{" + Thread.currentThread().getName() + "} " + stackTraceElement.getClassName() + "." +
                    stackTraceElement.getMethodName() + "#" +
                    stackTraceElement.getLineNumber() + ") ";
        }
        return message;
    }


}
