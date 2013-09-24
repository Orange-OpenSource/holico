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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;

import com.francetelecom.rd.hlc.Condition;
import com.francetelecom.rd.hlc.HomeBusException;
import com.francetelecom.rd.hlc.NodeServiceCallback;
import com.francetelecom.rd.hlc.Resource;
import com.francetelecom.rd.hlc.Rule;



public class Shutter extends NodeManager implements NodeManagerCallback{

	public final static String RESOURCE_LEVEL_NAME = "level";
	public final static String RESOURCE_LEVEL_PATH = "Confort.Shutter.level";
	
	public final static String RESOURCE_DISENGAGED_NAME  = "disengaged";
	public final static String RESOURCE_DISENGAGED_PATH = "Confort.Shutter.disengaged";
	
	public final static String REFRESH_INTENT = "refresh_shutter";
	
	// ====================================================================
	
	private final static String sManufacturer = "Woodmart";
	private final static String sVersion = "0.5.0";
	
	// ====================================================================
	
	public Shutter(String id, String name, NodeType type, final FragmentActivity activity)	{
					
		super(id, name, sManufacturer, sVersion, type, REFRESH_INTENT, activity);
		
		addResource(RESOURCE_LEVEL_NAME, RESOURCE_LEVEL_PATH, Resource.TYPE_VALUE_INT);
		addResource(RESOURCE_DISENGAGED_NAME, RESOURCE_DISENGAGED_PATH, Resource.TYPE_VALUE_BOOL);
		
		addService("ShutterSetLevel", "set level", false, new NodeServiceCallback() {
			
			@Override
			public void onServiceActivated(Object arg0) {
				
				setResource(RESOURCE_LEVEL_PATH, arg0);								
			}
			
			@Override
			public int getParameterType() {
				return Resource.TYPE_VALUE_INT;
			}
			
			@Override
			public String getParameterName() {
				return "level";
			}
		});
		
		addService("TestCondition", "test a condition", false, new NodeServiceCallback() {
			
			@Override
			public void onServiceActivated(Object arg0) {
				
				activity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						new AlertDialog.Builder(activity)
					    .setTitle("Service")
					    .setMessage("the Service TestCondition was performed")
					    .setPositiveButton("Thanks", new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					        }
					     })
					     .show();						
					}
				});								
			}
			
			@Override
			public int getParameterType() {
				return Resource.TYPE_VALUE_BOOL;
			}
			
			@Override
			public String getParameterName() {
				return "";
			}
		});
	}
	
	@SuppressLint("UseValueOf")
	public void completeNodePublication(){
		
		setResource(RESOURCE_LEVEL_PATH, new Integer(0));
		setResource(RESOURCE_DISENGAGED_PATH, new Boolean(false));
	}
	
	@SuppressLint("UseValueOf")
	public void addDefaultRules() {
		
		try {
			
			Condition condition = DeviceManager.getInstance().getHomeBusFactory().createCondition(Condition.OPERATOR_EQUAL,
					new Integer(100), RESOURCE_LEVEL_PATH);
		
			Rule rule = DeviceManager.getInstance().getHomeBusFactory().createRule("test", condition, "TestCondition", "", false,
					this.id);
			
			connector.addRule(rule);			
				
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (HomeBusException e) {
			e.printStackTrace();
		}
	}
	
	// ====================================================================
	
	@Override
	public void onNodePublished() {
		
		super.onNodePublished();
		completeNodePublication();	
		addDefaultRules() ;
	}
}
