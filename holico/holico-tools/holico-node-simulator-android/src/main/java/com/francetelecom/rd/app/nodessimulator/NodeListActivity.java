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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.francetelecom.rd.app.nodessimulator.devices.DeviceManager;
import com.francetelecom.rd.app.nodessimulator.devices.NodeManager;
import com.francetelecom.rd.app.nodessimulator.R;

/**
 * An activity representing a list of Devices. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link NodeDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link NodeListFragment} and the item details (if present) is a
 * {@link NodeDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link NodeListFragment.Callbacks} interface to listen for item selections.
 */
public class NodeListActivity extends FragmentActivity implements
		NodeListFragment.Callbacks {

	MulticastLock multicastLock;
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	
	private String selectedId = "";
	
	private NodeDetailFragment shutterFragment = null;
	private NodeDetailFragment sprinklerFragment = null;
	private NodeDetailFragment alarmFragment = null;
	private NodeDetailFragment unknwonFragment = null;
	
	@SuppressWarnings("unused")
	private NodesListManager nodeListManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// setup multicast capabilities
		setupMulticast();
		
		/////////////////////////////////////////////////////////
		// ask for device ID
		createIdAlert();
		
	}
	
	private void createIdAlert(){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Please, enter your device ID (1 < id < 127)");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  int id = Integer.parseInt(input.getText().toString());
		  if(id > 0 && id < 128 ){
			  DeviceManager.getInstance().setDeviceId(id);
			  start();
		  }
		  else
		  {
			  Context context = getApplicationContext();
	            CharSequence error = "Please enter an id between 1 and 127";
	            int duration = Toast.LENGTH_LONG;

	            Toast toast = Toast.makeText(context, error, duration);
	            toast.show();
	            createIdAlert();
		  }
		}
		});
			
		alert.show();
	}
	
	private void start(){
		
		setContentView(R.layout.activity_device_list);

		if (findViewById(R.id.device_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((NodeListFragment) getSupportFragmentManager().findFragmentById(
					R.id.device_list)).setActivateOnItemClick(true);
		}

		// TODO: If exposing deep links into your app, handle intents here.
		
		shutterFragment = new NodeDetailFragment_Shutter();
		sprinklerFragment = new NodeDetailFragment_Sprinkler();
		alarmFragment = new NodeDetailFragment_Alarm();
		unknwonFragment = new NodeDetailFragment();
		
		nodeListManager = new NodesListManager(this);
	}
	
	@Override
	protected void onDestroy(){
		if(multicastLock != null)
		{
			multicastLock.release();
		}
		super.onDestroy();
	}

	/**
	 * Callback method from {@link NodeListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if(!id.equals(selectedId)){			
			if (mTwoPane) {
				selectedId = id;
				
				// In two-pane mode, show the detail view in this activity by
				// adding or replacing the detail fragment using a
				// fragment transaction.
				Bundle arguments = new Bundle();
				arguments.putString(NodeDetailFragment.ARG_ITEM_ID, id);
				
				NodeManager node = NodesListManager.ITEM_MAP.get(id);
				
				
				NodeDetailFragment fragment = null;
				
				if(node.type == NodeManager.NodeType.SHUTTER)
				{
					fragment = shutterFragment;
				}
				else if(node.type == NodeManager.NodeType.SPRINKLER)
				{
					fragment = sprinklerFragment;
				}
				else if(node.type == NodeManager.NodeType.ALARM)
				{
					fragment = alarmFragment;
				}
				else if(node.type == NodeManager.NodeType.DATAMODEL)
				{
					fragment = new DatamodelFragment();
				}
				else
				{
					fragment = unknwonFragment;
				}			
				fragment.setArguments(arguments);
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.device_detail_container, fragment).commit();
	
			} else {
				// In single-pane mode, simply start the detail activity
				// for the selected item ID.
				Intent detailIntent = new Intent(this, NodeDetailActivity.class);
				detailIntent.putExtra(NodeDetailFragment.ARG_ITEM_ID, id);
				startActivity(detailIntent);
			}
		}
	}
	
	private void setupMulticast(){
		WifiManager wifiManager = (WifiManager)getSystemService(android.content.Context.WIFI_SERVICE);
		multicastLock = wifiManager.createMulticastLock("com.francetelecom.rd");
		multicastLock.setReferenceCounted(true);
		multicastLock.acquire();
	}
}
