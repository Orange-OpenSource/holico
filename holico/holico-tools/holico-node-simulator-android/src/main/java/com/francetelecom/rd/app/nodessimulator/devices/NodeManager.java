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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.francetelecom.rd.hlc.HlcConnector;
import com.francetelecom.rd.hlc.HomeBusException;
import com.francetelecom.rd.hlc.InvalidResourcePathException;
import com.francetelecom.rd.hlc.InvalidResourceTypeException;
import com.francetelecom.rd.hlc.Node;
import com.francetelecom.rd.hlc.NodeServiceCallback;
import com.francetelecom.rd.hlc.Resource;
import com.francetelecom.rd.hlc.impl.SdsAdapterListener;



public class NodeManager extends SdsAdapterListener implements NodeManagerCallback  {
	
	
	Node myNode;
		
	// ====================================================================
	
	public enum NodeType {	SHUTTER,
							SPRINKLER,
							ALARM,
							UNKNOWN,
							DATAMODEL};
	
	// ====================================================================

	public boolean isPublished = false;
							
	public String id;
	public String name;
	public String manufacturer;
	public String version;
	public NodeType type;
		
	private final FragmentActivity activity;
	public final String intentFilter;
	
	public HlcConnector connector;
	
	public List<String> services = new ArrayList<String>();
	public Map<String, Object> resources = new HashMap<String, Object>();
	
	
	// ====================================================================
	
			
	public NodeManager(String id, String name, String manufacturer, String version, NodeType type, String intentFilter, FragmentActivity activity) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.manufacturer = manufacturer;
		this.version = version;
		this.activity = activity;
		this.intentFilter = intentFilter;
			
		try {
							
			myNode = DeviceManager.getInstance().getHomeBusFactory().createNode(this.id, "device_id", this.name);
			
			if(this.manufacturer != null && !this.manufacturer.isEmpty())
			{
				myNode.setManufacturer(this.manufacturer);
			}
			if(this.version != null && !this.version.isEmpty())
			{
				myNode.setVersion(this.version);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void publish()
	{	    
		try {
			
			if(myNode.publishOnHomeBus())  {
				this.connector = myNode.getHlcConnector();
				onNodePublished();
			}
			
		} catch (HomeBusException e) {
			e.printStackTrace();
		} catch (InvalidResourceTypeException e) {
			e.printStackTrace();
		} catch (InvalidResourcePathException e) {
			e.printStackTrace();
		}
	}
	
	public void unpublish()
	{
		/*try {
			
			myNode.removesFromHlc();
			
		} catch (HomeBusException e) {
			e.printStackTrace();
		}*/
	}
	
	@SuppressLint("UseValueOf")
	public void addResource(String name, String path, int type)
	{
		try {
			
			myNode.addResourcePublication(name, path, type);	
			
			switch(type){
			case Resource.TYPE_VALUE_BOOL:
				resources.put(path, new Boolean(false));
				break;
			case Resource.TYPE_VALUE_INT:
				resources.put(path, new Integer(-1));
				break;
			case Resource.TYPE_VALUE_STRING:
				resources.put(path, "");
				break;
			}
			
		} catch (HomeBusException e) {
			e.printStackTrace();
		} catch (InvalidResourceTypeException e) {
			e.printStackTrace();
		} catch (InvalidResourcePathException e) {
			e.printStackTrace();		
		}
	}
	
	public void setResource(String path, Object value)
	{
		try {		
			
			if(myNode.isPublishedOnHomeBus()) {
				resources.put(path, value);
				myNode.publishOnResource(path, value);
			}
			
		} catch (HomeBusException e) {
			e.printStackTrace();
		} catch (InvalidResourceTypeException e) {
			e.printStackTrace();
		} catch (InvalidResourcePathException e) {
			e.printStackTrace();
		}
	}
	
	public void addService(String id, String name, boolean isPrivate, NodeServiceCallback callback)
	{
		try {			
			services.add(name);
			myNode.addNodeService(id, name, isPrivate, callback);
			
		} catch (HomeBusException e) {
			e.printStackTrace();
		}
	}
	
	// ====================================================================
	
	private void subscribeForPublicationUpdates(){
		
		Iterator<String> it = this.resources.keySet().iterator();
		while(it.hasNext()) {
		    String resourcePath = (String)it.next();
		    //DeviceManager.getInstance().getHomeBusFactory().getSdsAdaptor().addSdsAdapterListener("HomeLifeContext." + resourcePath, this);			
		}
	}

	// ====================================================================
	
	@Override
	public String toString()
	{
		return this.name;
	}

	// ====================================================================
	
	@Override
	public void onNodePublished() {
		// Have to be Override by extender of this class	
		
		subscribeForPublicationUpdates();	
	}
	
	// ====================================================================
	
	@Override
	public void onResourceArrived(Resource resource) {
		refreshResource(resource);
	}

	@Override
	public void onResourceChanged(Resource resource) {
		refreshResource(resource);
	}

	@Override
	public void onResourceLeft(Resource resource) {
		refreshResource(resource);
	}
	
	private void refreshResource(Resource resource) {				
		RunnableRefresher refresher = new RunnableRefresher();
		refresher.setResource(resource);
		this.activity.runOnUiThread(refresher);				
	}

	// ====================================================================
	
	public class RunnableRefresher implements Runnable {
		private Resource resource;
		  
	  public void setResource(Resource _resource) {
	    this.resource = _resource;
	  }
	  
	  public void run() {
		  
		  try {
			  
			  Intent intent = new Intent(intentFilter);
			  intent.putExtra("path", this.resource.getPath());
			  switch(this.resource.getResourceType()){
			  case Resource.TYPE_VALUE_INT:				  
				  intent.putExtra("value", (Integer)this.resource.getValue());				
				  break;
			  case Resource.TYPE_VALUE_BOOL:
				  intent.putExtra("value", (Boolean)this.resource.getValue());
				  break;
			  case Resource.TYPE_VALUE_STRING:
				  intent.putExtra("value", (String)this.resource.getValue());
				  break;
			  }
			  
	      activity.getBaseContext().getApplicationContext().sendBroadcast(intent);
	      
		  } catch (InvalidResourceTypeException e) {
			e.printStackTrace();
		}
	  }
	}
	
}