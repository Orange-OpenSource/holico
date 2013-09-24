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
package com.francetelecom.rd.app.nodessimulator;

import com.francetelecom.rd.app.nodessimulator.devices.DeviceManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.francetelecom.rd.hlc.InvalidResourceTypeException;
import com.francetelecom.rd.hlc.Resource;
import com.francetelecom.rd.hlc.impl.SdsAdapterListener;
import com.francetelecom.rd.app.nodessimulator.R;

/**
 * A fragment representing a single Device detail screen. This fragment is
 * either contained in a {@link NodeListActivity} in two-pane mode (on
 * tablets) or a {@link NodeDetailActivity} on handsets.
 */
public class DatamodelFragment extends NodeDetailFragment  {

	private View rootView = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.fragment_datamodel,
				container, false);

		// Show the datamodel as text in a TextView.
		LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.data_model);
		
		// TITLE
		((TextView) layout.findViewById(R.id.node_title))
			.setText("CURRENT DATA MODEL");
		
		// DATA MODEL
		EditText datamodelText = ((EditText) layout.findViewById(R.id.data_model_text));
		datamodelText.setText("here is the datamodel representation");	

		 refreshView();		 
		 
		// subscribe for datamodel updates
		/* 
		DeviceManager.getInstance().getHomeBusFactory().getSdsAdaptor().addSdsAdapterListener("HomeLifeContext", this);
		DeviceManager.getInstance().getHomeBusFactory().getSdsAdaptor().addSdsAdapterListener("Config", this);
		DeviceManager.getInstance().getHomeBusFactory().getSdsAdaptor().addSdsAdapterListener("Discovery", this);
		*/
			
		return rootView;
	}
	
	@Override
	public void onDestroyView (){
		
		super.onDestroyView();
		
		// unsubscribe for datamodel updates
		/*
		DeviceManager.getInstance().getHomeBusFactory().getSdsAdaptor().removeSdsAdapterListener("HomeLifeContext", this);	
		DeviceManager.getInstance().getHomeBusFactory().getSdsAdaptor().removeSdsAdapterListener("Config", this);	
		DeviceManager.getInstance().getHomeBusFactory().getSdsAdaptor().removeSdsAdapterListener("Discovery", this);
		*/
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // TODO
        // refreshView();
    }
	
	@Override
	protected void refreshView()
	{	
		if(rootView != null)
		{
			LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.data_model);
			
			/*
			Resource root = DeviceManager.getInstance().getHomeBusFactory().getSdsAdaptor().getCurrentTree();
			String datamodelAsString = getDatamodelAsString(root, "");			
			
			((EditText) layout.findViewById(R.id.data_model_text))
			.setText(datamodelAsString);
			*/
		}
	}
	
	public String getDatamodelAsString(Resource resource, String prefix) {

		String result = "";
		
		if(resource.getName() != null){
			Resource[] children;
			try {
				children = resource.getSubResources();
			
				for (int i = 0; i < children.length; i++) {
					boolean isDir = !children[i].isValueResource();
					String line = prefix + " " + children[i].getName();
					if (isDir) {
						line += (children[i].getResourceType() == Resource.TYPE_SHARED ? "." : "[]");
					} else {
						if (children[i].getValue() != null) {
							line += " = " + children[i].getValue().toString();
						}
		
					}
		
					result += line + "\r\n";
		
					if (isDir) {
						String pr = (prefix.length() == 0 ? " \u2514\u2500\u2500"
								: "    " + prefix);
						result += getDatamodelAsString(children[i], pr);
					}
				}
			} catch (InvalidResourceTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	// ====================================================================
	
	public class RunnableRefresher implements Runnable {
	  @SuppressWarnings("unused")
	  private Resource resource;
	  
	  public void setResource(Resource _resource) {
	    this.resource = _resource;
	  }

	  public void run() {

		  refreshView();
	  }
	}
	
	// ====================================================================
	
	class Listener extends SdsAdapterListener {
	
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
		if(getActivity() != null){
			RunnableRefresher refresher = new RunnableRefresher();
			refresher.setResource(resource);
			getActivity().runOnUiThread(refresher);		
		}
	}
	}
}
