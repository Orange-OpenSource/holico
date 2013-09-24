/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.sds.sds-impl
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
package com.francetelecom.rd.sds.communicationlayer.impl;

import java.util.EventObject;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.registries.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.registries.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.endpoint.resources.LocalResource;
import ch.ethz.inf.vs.californium.endpoint.resources.Resource;

import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.Directory;
import com.francetelecom.rd.sds.Parameter;
import com.francetelecom.rd.sds.ValueChangeListener;
import com.francetelecom.rd.sds.impl.HomeSharedDataImpl;

/**
 * This californium (coap) Resource represents the sds tree.
 * <p>
 * It is created form the rot of the sds tree and creates sub-resources for
 * every Directory and parameter in the tree
 * <p>
 * It also registers listeners for the chnages on the sds tree and keep the
 * corresponding resources up-to-date.
 * 
 * @author Pierre Rust (tksh1670)
 */
public class SdsTreeResource extends LocalResource {

	private Timer initTimer;

	private Directory dir = null;
	private Parameter param = null;

	public SdsTreeResource() {
		// FIXME : do to map to root ("") for now as it does not seem to work
		// well
		// with californium
		super("home");
		setTitle("SDS Root ");
		setContentTypeCode(MediaTypeRegistry.APPLICATION_LINK_FORMAT);

		// FIXME : the initialization order makes that the root
		// is generally not initialized at this point :(
		// HACK : wait some time until it's been initialized

		initTimer = new Timer("initRootResourceTimer");
		initTimer.schedule(new InitRootTask(), 2000);
	}

	private class InitRootTask extends TimerTask {

		public void run() {
			Directory root = HomeSharedDataImpl.getRootDirectory();

			if (root == null) {
				initTimer.schedule(new InitRootTask(), 2000);
			} else {
				dir = root;
				initResourceForDirectory();
			}
		}
	}

	private SdsTreeResource(Parameter param) {
		super(param.getName());

		setTitle("Parameter " + param.getName());
		this.param = param;

	}

	private SdsTreeResource(Directory dir) {
		super(dir.getName());

		setTitle("Directory " + dir.getName());
		setContentTypeCode(MediaTypeRegistry.APPLICATION_LINK_FORMAT);

		this.dir = dir;
		initResourceForDirectory();
	}

	/**
	 * Init a directory-backed Resource
	 * 
	 */
	private void initResourceForDirectory() {
		// listen to changes on this Directory to update the corresponding
		// sub-resources.
		dir.addValueChangeListener(new ValueChangeListener() {

			public void valueChange(EventObject evt) {

				updateChildResources();
			}
		});
		updateChildResources();
	}

	/**
	 * Update the list of child resources according to the children of the
	 * Directory backing this Resource.
	 */
	private void updateChildResources() {

		Data[] childs = dir.getChildren();

		if (childs != null)
		{
		   if (subResources != null) {
			  // first, remove resources for deleted children
			  Iterator it = subResources.values().iterator();
			  while (it.hasNext()) {
				 Resource resource = (Resource) it.next();
				 boolean found = false;
				 for (int i = 0; i < childs.length; ++i) {
					if (childs[i].getName().equals(resource.getName())) {
						found = true;
						break;
					}
				 }

				 if (!found) {
					// resourceName is not published anymore, remove it
					it.remove();
				 }
			  }

			  // now add resources for new children
			  for (int i = 0; i < childs.length; ++i) {
				 boolean found = false;
				 Iterator its = subResources.keySet().iterator();
				 while (its.hasNext())
				 {
					String resourceName = (String)its.next();
					if (resourceName.equals(childs[i].getName())) {
						found = true;
						break;
					}
		 		 }

				 if (!found) {
					// new child: create and add corresponding sub-resource

					SdsTreeResource childResources = null;
					if (childs[i] instanceof Parameter)
						childResources = new SdsTreeResource(
								(Parameter) childs[i]);
					else if (childs[i] instanceof Directory)
						childResources = new SdsTreeResource(
								(Directory) childs[i]);
					add(childResources);
				 }
			  }
			
		   } else {
			
			// there is no sub-resource yet, no need to compare anything
			// we can just add them
			for (int i = 0; i < childs.length; ++i) {
			   SdsTreeResource childResources = null;
			   if (childs[i] instanceof Parameter)
				  childResources = new SdsTreeResource(
							(Parameter) childs[i]);
				  else if (childs[i] instanceof Directory)
					 childResources = new SdsTreeResource(
							(Directory) childs[i]);
				  add(childResources);
			    }
			}			
		}
	}

	public void performGET(GETRequest request) {
		Response response = new Response(CodeRegistry.RESP_CONTENT);
		if (dir != null) { // FIXME

			// return resources in link-format
			response.setPayload(LinkFormat.serialize(this, null, true),
					MediaTypeRegistry.APPLICATION_LINK_FORMAT);
			request.respond(response);
		} else if (param != null) {

	        String val = param.getValue().toString();   
			request.respond(CodeRegistry.RESP_CONTENT, val);
		} else {
			// something is wrong !!!
			// FIXME : add trace once sds use the log librairie
		}
	}

}
