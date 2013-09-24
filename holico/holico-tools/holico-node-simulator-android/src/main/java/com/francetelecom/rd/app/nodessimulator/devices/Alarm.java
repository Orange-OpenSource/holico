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
package com.francetelecom.rd.app.nodessimulator.devices;

import android.annotation.SuppressLint;
import android.support.v4.app.FragmentActivity;

import com.francetelecom.rd.hlc.NodeServiceCallback;
import com.francetelecom.rd.hlc.Resource;

public class Alarm extends NodeManager implements NodeManagerCallback{

	public final static String RESOURCE_ACTIVATION_NAME = "activation";
	public final static String RESOURCE_ACTIVATION_PATH = "Security.Alarm.activated";
	
	public final static String RESOURCE_TRESPASS_NAME  = "trespass";
	public final static String RESOURCE_TRESPASS_PATH = "Security.Alarm.trespass";
	
	public final static String REFRESH_INTENT = "refresh_alarm";
	
	// ====================================================================
		
	private final static String sManufacturer = "Somfy";
	private final static String sVersion = "1.0.0";	
	
	// ====================================================================
	
	public Alarm(String id, String name, NodeType type, FragmentActivity activity)	{
		
		super(id, name, sManufacturer, sVersion, type, REFRESH_INTENT, activity);
		
		addResource(RESOURCE_ACTIVATION_NAME, RESOURCE_ACTIVATION_PATH, Resource.TYPE_VALUE_BOOL);
		addResource(RESOURCE_TRESPASS_NAME, RESOURCE_TRESPASS_PATH, Resource.TYPE_VALUE_BOOL);
		
		addService("AlarmActivation", "activate", false, new NodeServiceCallback() {
			
			@Override
			public void onServiceActivated(Object arg0) {
				
				setResource(RESOURCE_ACTIVATION_PATH, arg0);								
			}
			
			@Override
			public int getParameterType() {
				return Resource.TYPE_VALUE_BOOL;
			}
			
			@Override
			public String getParameterName() {
				return "activated";
			}
		});
	}
	
	@SuppressLint("UseValueOf")
	public void completeNodePublication(){
		
		setResource(RESOURCE_ACTIVATION_PATH, new Boolean(false));
		setResource(RESOURCE_TRESPASS_PATH, new Boolean(false));
	}
	
	public void addDefaultRules() {
				
	}
			
	// ====================================================================
	
	@Override
	public void onNodePublished() {
		
		super.onNodePublished();
		completeNodePublication();		
		addDefaultRules();			
	}
}

