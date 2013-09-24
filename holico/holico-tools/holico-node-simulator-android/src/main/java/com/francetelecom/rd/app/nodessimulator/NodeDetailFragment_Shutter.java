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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.francetelecom.rd.app.nodessimulator.devices.Alarm;
import com.francetelecom.rd.app.nodessimulator.devices.Shutter;
import com.francetelecom.rd.app.nodessimulator.R;

/**
 * A fragment representing a single Device detail screen. This fragment is
 * either contained in a {@link NodeListActivity} in two-pane mode (on
 * tablets) or a {@link NodeDetailActivity} on handsets.
 */
public class NodeDetailFragment_Shutter 	extends NodeDetailFragment 
											implements 	CompoundButton.OnCheckedChangeListener,
											 			SeekBar.OnSeekBarChangeListener
{
	// ====================================================================
	
	private Activity activity;
	
	private Switch m_publicationSwitch;
	private Switch m_disengageSwitch;
	private SeekBar m_levelSeekBar;
	
	private IntentFilter refresh_filter = null; 
	private BroadcastReceiver refreshReceiver = null;
		
	// ====================================================================
	
	private int levelState = 0;
	@SuppressLint("UseValueOf")
	private Boolean disengageState = new Boolean(false);
	
	// ====================================================================
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		nodeView = inflater.inflate(R.layout.fragment_device_detail_shutter,
				container, false);

		this.refreshView();
		
		return nodeView;
	}
	
	@Override
	public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = activity;
        
        refresh_filter = new IntentFilter(Alarm.REFRESH_INTENT); 
		refreshReceiver = new RefreshReceiver();
		this.activity.getBaseContext().registerReceiver(refreshReceiver,refresh_filter);	       
    }
	
	@Override
	public void onDestroyView (){
		
		super.onDestroyView();
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // TODO
        // refreshView();
    }
	
	@SuppressLint({ "DefaultLocale", "UseValueOf" })
	@Override
	public void refreshView()
	{
		if (currentNode != null) {	
			LinearLayout layout = (LinearLayout) nodeView.findViewById(R.id.device_detail);
			
			// TITLE
			((TextView) layout.findViewById(R.id.node_title))
				.setText(currentNode.name.toUpperCase());
			
			// PUBLICATION
			m_publicationSwitch = (Switch) layout.findViewById(R.id.publication_switch);
			m_publicationSwitch.setOnCheckedChangeListener(this);
					
			// INFO / NAME
			((TextView) layout.findViewById(R.id.node_name))
				.setText(currentNode.name);
			
			// INFO / MANUFACTURER
			((TextView) layout.findViewById(R.id.node_manufacturer))
				.setText(currentNode.manufacturer);
					
			// INFO / VERSION
			((TextView) layout.findViewById(R.id.node_version))
				.setText(currentNode.version);
			
			// SERVICE 1 / NAME
			((TextView) layout.findViewById(R.id.service_1_name))
				.setText(currentNode.services.get(0));
			
			// RESOURCE 1 / NAME
			((TextView) layout.findViewById(R.id.resource_1_name))
				.setText(Shutter.RESOURCE_LEVEL_PATH);
			
			m_levelSeekBar = (SeekBar) layout.findViewById(R.id.level_seekBar);
			m_levelSeekBar.setOnSeekBarChangeListener(this);
			if(currentNode.resources.get(Shutter.RESOURCE_LEVEL_PATH) != null) {
				levelState = ((Integer)currentNode.resources.get(Shutter.RESOURCE_LEVEL_PATH)).intValue();
			}
			else {
				levelState = 0;
			}
			m_levelSeekBar.setProgress(levelState);
			
			// RESOURCE 2 / NAME
			((TextView) layout.findViewById(R.id.resource_2_name))
				.setText(Shutter.RESOURCE_DISENGAGED_PATH);
			
			m_disengageSwitch = (Switch) layout.findViewById(R.id.disengage_switch);
			m_disengageSwitch.setOnCheckedChangeListener(this);
			if(currentNode.resources.get(Shutter.RESOURCE_DISENGAGED_PATH) != null) {
				disengageState = (Boolean)currentNode.resources.get(Shutter.RESOURCE_DISENGAGED_PATH);
			}
			else {
				disengageState = new Boolean(false);
			}
			m_disengageSwitch.setChecked(disengageState);
		}
	}
	
	// ====================================================================
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
	{
		if(currentNode != null)
		{
			if(buttonView == m_publicationSwitch)
			{
			    if(isChecked)
			    {			    	
			    	currentNode.publish();			    	
			    }
			    else
			    {
			    	currentNode.unpublish();	
			    }
			}
			
			if(buttonView == m_disengageSwitch)
			{
				if(isChecked != disengageState){
					disengageState = isChecked;
					currentNode.setResource(Shutter.RESOURCE_DISENGAGED_PATH, disengageState);
				}
			}
		}
	}  
	
	// ====================================================================
	    
	class RefreshReceiver extends BroadcastReceiver 
	{		
		@Override
		public void onReceive(Context context, Intent intent) {
	
			String path = intent.getStringExtra("path");
			
			if(path.equals("HomeLifeContext." + Shutter.RESOURCE_LEVEL_PATH))
			{
				int val = intent.getIntExtra("value", 0);
				if(levelState != val)
				{
					levelState = val;
					m_levelSeekBar.setProgress(levelState);
				}
			}
			else if(path.equals("HomeLifeContext." + Shutter.RESOURCE_DISENGAGED_PATH))
			{
				Boolean val = intent.getBooleanExtra("value", false);
				if(disengageState.booleanValue() != val.booleanValue())
				{
					disengageState = val;
					m_disengageSwitch.setChecked(disengageState);
				}
			}			
		}
	}
    		
	
	// ====================================================================
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		if(seekBar == m_levelSeekBar)
		{
			if(seekBar.getProgress() != levelState){
				levelState = seekBar.getProgress();
				currentNode.setResource(Shutter.RESOURCE_LEVEL_PATH, levelState);
			}
		}
	}

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}
}
