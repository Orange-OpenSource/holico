/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.holico.hlc-connector-impl
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
package com.francetelecom.rd.hlc.impl;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.hlc.HomeBusException;
import com.francetelecom.rd.hlc.InvalidResourceTypeException;
import com.francetelecom.rd.hlc.Resource;
import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;
import com.francetelecom.rd.sds.HomeSharedData;
import com.francetelecom.rd.sds.Parameter;
import com.francetelecom.rd.sds.ValueChangeListener;
import com.francetelecom.rd.sds.impl.DataImpl;
import com.francetelecom.rd.sds.impl.DirectoryImpl;
import com.francetelecom.rd.sds.impl.HomeSharedDataImpl;
import com.francetelecom.rd.sds.impl.ParameterImpl;

/**
 * @author hjpl6323
 *
 */
public class SdsAdapter {

	// ==============================================================================

	final int deviceId;
	final Directory hsRoot;
	final HomeSharedData hsData;
	/**
	 * Previous tree
	 * <p>
	 * As sds does not gives (yet) the type of modification, we keep the previous 
	 * tree  to compare it with the current tree and compute the kind of 
	 * modification that happened.
	 */
	Directory previousHlcTree;
	Directory currentHlcTree;

	ValueChangeListener valueChangeListener;

	/**
	 * Map of listener for change on the sds tree.
	 * pathListener -> array of corresponding listeners 
	 */
	private Map listeners;

	final Logger logger = LoggerFactory.getLogger(HomeBusFactory.class
			.getName());

	// ==============================================================================

	public SdsAdapter(int deviceId) {

		hsData = HomeSharedDataImpl.getInstance();

		this.deviceId = deviceId;
		this.listeners = new ConcurrentHashMap();

		// FIXME deviceId : relation with HLC nodeId or device ID ?
		// 0-255 and not GUID ?
		boolean forceReinit = true;
		String filename = null;
		hsRoot = hsData.getRootDirectory(forceReinit, filename, deviceId);
	}

	// ==============================================================================

	public Directory getRoot() {
		return hsRoot;
	}

	public Resource getCurrentTree() {
		return new ResourceImpl(currentHlcTree);
	}

	public HomeSharedData getHomeSharedData() {
		return hsData;
	}

	/**
	 * Add a listener that will be notified for any change happening in the 
	 * sub-tree corresponding to the specified path.
	 * 
	 * @param dataPath
	 * @param listener
	 */
	public void addSdsAdapterListener(String dataPath,
			SdsAdapterListener listener) {

		if (this.listeners.containsKey(dataPath)) {

			SdsAdapterListener[] listenersForDesiredPath = (SdsAdapterListener[]) this.listeners
					.get(dataPath);
			boolean listenerAlreadyKnown = false;
			for (int i = 0; i < listenersForDesiredPath.length; i++) {
				if (listenersForDesiredPath[i].equals(listener)) {
					listenerAlreadyKnown = true;
					break;
				}
			}
			if (!listenerAlreadyKnown) {

				int length = listenersForDesiredPath.length;
				SdsAdapterListener newListeners[] = new SdsAdapterListener[length + 1];
				System.arraycopy(listenersForDesiredPath, 0, newListeners, 0,
						length);
				newListeners[length] = listener;
				listenersForDesiredPath = newListeners;

				this.listeners.put(dataPath, listenersForDesiredPath);
			}
		} 
		else {
			SdsAdapterListener listenersForDesiredPath[] = new SdsAdapterListener[1];
			listenersForDesiredPath[0] = listener;
			this.listeners.put(dataPath, listenersForDesiredPath);
		}

		if (this.listeners.size() == 1) {
			// Save the current HLC tree
			previousHlcTree = (Directory) ((DirectoryImpl) hsRoot).clone();

			// CBE +
			// problem : at start up, the previousHlcTree is retrieved 
			// from existing nodes on the home bus.
			// if the tree contains rules that concern the node 
			// that is currently declaring itself,
			// these rules are not taken into consideration by the node
			// at least until they change and are detected in the valueChange sds call
			// not taken into consideration : means a that listener for 
			// the rule's resource has not been created
			// solution : when getting for the first time the tree (here), 
			// check in root.Config branch for possible rules that might concern this node
			try {
				Data[] rule; // Rule[]
				Data[] ruleChildren; // Rule["rueId"]
				rule = ((Directory) previousHlcTree.getChild(HomeBusPathDefinitions.CONFIG)).getChildren();
				// the tree already contains rules
				if (rule.length == 1){
					if (rule[0].getName().equals(HomeBusPathDefinitions.RULE)){
						// we have Rule[] in rule, get each rule below
						ruleChildren = ((Directory)rule[0]).getChildren();

						for (int j = 0; j < ruleChildren.length; j++){
							// for each rule initialize service
							// if the service belongs to the node ???
							dataArrived(rule[0], ruleChildren[j]);
						}
					}
				}
			} catch (DataAccessException e) {
				// ok : if here this means there are no rules in the home bus tree 
			} catch (NullPointerException e) {
				// ok : if here this means there are no rules in the home bus tree 
			}
			// CBE -

			// Initialize sds listener on root to get all HLC tree changes
			valueChangeListener = new ValueChangeListener() {

				@Override
				public void valueChange(EventObject evt) {

					currentHlcTree = (Directory) ((DirectoryImpl) evt.getSource()).clone();
					computeModifications(currentHlcTree);

					// update current tree with new one
					previousHlcTree = (Directory) ((DirectoryImpl) currentHlcTree)
							.clone();
				}
			};
			hsRoot.addValueChangeListener(valueChangeListener);
		}
	}

	public void removeSdsAdapterListener(String dataPath,
			SdsAdapterListener listener) {
		if (this.listeners.containsKey(dataPath)) {

			SdsAdapterListener[] listenersForDesiredPath = (SdsAdapterListener[]) this.listeners
					.get(dataPath);
			boolean listenerFound = false;
			int i;
			for (i = 0; i < listenersForDesiredPath.length; i++) {
				if (listenersForDesiredPath[i].equals(listener)) {
					listenerFound = true;
					break;
				}
			}
			if (listenerFound) {
				int length = listenersForDesiredPath.length;
				if (length == 1) {
					this.listeners.remove(dataPath);
				} else {
					SdsAdapterListener newListeners[] = new SdsAdapterListener[length - 1];
					// CBE : using t to fix the folowing bug
					// the listener to delete is not the last one(index5) in listenersForDesiredPath(length 6)
					// => length of newListeners is 5, and in for we'll have an error for the last listener to copy
					// newListeners[5] = listenersForDesiredPath[5] ! newListeners[5] does not exist!! max is 4
					int t = 0; 
					for (int j = 0; j < listenersForDesiredPath.length; j++) {
						if (j != i) {
							newListeners[t++] = listenersForDesiredPath[j];
						}
					}
					listenersForDesiredPath = newListeners;
					this.listeners.put(dataPath, listenersForDesiredPath);
				}
			}
		}

		if (this.listeners.size() == 0) {
			// Remove sds listener
			hsRoot.removeValueChangeListener(valueChangeListener);
		}
	}

	// CBE
	/**
	 * removes all listeners of sds adaptor <p>
	 * to be used when the node is unpublished, <p>
	 * and he doesn't need to listen to the home bus anymore
	 */
	public void removeListeners(){
		this.listeners.clear();
		// remove also the sds valueChange listener
		// we don't need to communicate via sds anymore

		// FIXME the removeValuChangeListener makes the application freeze
		// hsRoot.removeValueChangeListener(valueChangeListener);
	}

	// ==============================================================================

	/**
	 * Compute modifications of the HLC tree.
	 * <p>
	 * Try to find recursively modifications on a Directory :
	 * <ul>
	 * <li>child added</li>
	 * <li>child updated</li>
	 * <li>child removed</li>
	 * </ul>
	 * Looking for the new revision branch
	 * </p>
	 */
	private void computeModifications(Directory newHlcTree) {

		//Tools.displayTreeRepresentation(5, currentHlcTree, "", true);
		//Tools.displayTreeRepresentation(5, newHlcTree, "", true);

		// 1. looking for arrival or update in the tree
		Data[] endPoints = lookingForEndPointModifications(newHlcTree);
		resolveEndPointModifications(newHlcTree, endPoints);

		// 2. looking for removal
		resolveRemoval(newHlcTree);
	}

	/**
	 * 
	 * @param newHlcSubTree
	 * @return
	 */
	private Data[] lookingForEndPointModifications(Directory newHlcSubTree) {

		Data[] endPoints = new Data[0];

		// CBE + 
		// comparing entire version, not only the device's revision 
		// because by comparing only revision, modification of a resource was not always detected
		// CBE -
		String fullPreviousRevision = ((DataImpl) previousHlcTree).revisionToString();
		Data[] children = newHlcSubTree.getChildren();
		for (int i = 0; i < children.length; i++) {
			Data child = children[i];

			String fullChildRevision = ((DataImpl) child).revisionToString();
			try
			{
				Data prevChild = ((DirectoryImpl)previousHlcTree).getChild(child.getPathname());
				if (prevChild != null ) {
					fullPreviousRevision = ((DataImpl) prevChild).revisionToString();
				} else {
					// the previous tree did not contain the child : it's a new child
					logger.info("New child found : " + child.getPathname());
					fullPreviousRevision = "";
				}
			}
			catch (DataAccessException e)
			{
				logger.error("DataAccessException while getting child revision in previous HlcTree"+
						" For path " + child.getPathname(),e);
			}

			if (fullChildRevision.compareTo(fullPreviousRevision) != 0) {
				// we are in the updated branch here

				if (child instanceof Parameter) {
					// Parameter = end point of a branch
					// => add it to end point list
					int length = endPoints.length;
					Data newEndPoints[] = new Data[length + 1];
					System.arraycopy(endPoints, 0, newEndPoints, 0, length);
					newEndPoints[length] = child;
					endPoints = newEndPoints;
				} else if (child instanceof Directory) {
					if (previousHlcTree.contains(child.getPathname())
							|| child.getPathname().equals(
									HomeBusPathDefinitions.CONFIG)
									|| child.getPathname().equals(
											HomeBusPathDefinitions.DISCOVERY)
											|| child.getType() == Data.TYPE_SPE_DIR) {
						// looking for next level in tree
						Data[] childEndPoints = lookingForEndPointModifications((Directory) child);
						if (childEndPoints.length > 0) {
							// => add child to root to end point list
							int length = endPoints.length;
							Data newEndPoints[] = new Data[length
							                               + childEndPoints.length];
							System.arraycopy(endPoints, 0, newEndPoints, 0,
									length);
							for (int index = 0; index < childEndPoints.length; index++) {
								newEndPoints[length + index] = childEndPoints[index];
							}
							endPoints = newEndPoints;
						}
					} else {
						// here is an end point of a branch
						// => add it to end point list
						int length = endPoints.length;
						Data newEndPoints[] = new Data[length + 1];
						System.arraycopy(endPoints, 0, newEndPoints, 0, length);
						newEndPoints[length] = child;
						endPoints = newEndPoints;
					}
				}
			}
		}

		return endPoints;
	}

	private void resolveEndPointModifications(Directory root, Data[] endPoints) {

		for (int i = 0; i < endPoints.length; i++) {

			boolean arrived = false;

			if (previousHlcTree.contains(endPoints[i].getPathname())) {
				// update
				arrived = false;
				dataChanged(endPoints[i], endPoints[i]);
			} else {
				// arrival
				arrived = true;
				dataArrived(endPoints[i], endPoints[i]);
			}

			// ascend branch
			Directory parent = getParent(root, endPoints[i]);
			while (parent != null) {

				if (arrived) {
					dataArrived(parent, endPoints[i]);
				} else {
					dataChanged(parent, endPoints[i]);
				}

				parent = getParent(root, parent);
			}
		}
	}

	private void resolveRemoval(Directory root) {

		lookForRemovedData(root, null);

		// CBE 
		// the previous solution below checks if direct children of the root 
		// (Config, discovery, HomeLifeContext)
		// disappeared, which is not enough
		// must also check the children of the direct children, and so on
		// a recursive solution needed => lookForRemovedData(newRoot, childToCheckFromPreviousTree)

		/*Data[] children = previousHlcTree.getChildren();
		for (int i = 0; i < children.length; i++) {
			Data child = children[i];

			if (root.contains(child.getPathname())) {
				// child still present
			} 
			else {
				logger.info("child disappeared " + child.getPathname());
				// child disappear
				dataLeft(child, child);

				// ascend branch
				Directory parent = getParent(root, child);
				while (parent != null) {
					dataLeft(parent, child);
					parent = getParent(root, parent);
				}
			}			
		}*/
	}

	/**
	 * CBE
	 * @param newRoot the new tree (on root level)
	 * @param previousData children to check from previous tree 
	 */
	private void lookForRemovedData(Directory newRoot, Data previousData){

		try {
			Data[] children;
			if (previousData == null){
				// first call
				children = previousHlcTree.getChildren();
			}
			else{
				// second calls
				children = previousHlcTree.getDirectory(previousData.getPathname()).getChildren();
			}
			if ((children != null) && (children.length != 0)){
				for (int i = 0; i < children.length; i++) {
					Data child = children[i];
					if (newRoot.contains(child.getPathname())) {
						// child still present
					} 
					else {
						// child disappeared
						dataLeft(child, child);
						// ascend branch
						Directory parent = getParent(newRoot, child);
						while (parent != null) {
							dataLeft(parent, child);
							parent = getParent(newRoot, parent);
						}
					}
					// look below each child for removal
					lookForRemovedData(newRoot, child);
				}	
			}
		} catch (DataAccessException e) {
			// added comment until FIXME
			// a lot of warnings when recursion call
			// BECAUSE previousHlcTree.getDirectory(...) does not always 
			// get directories for child arguments
			// e.printStackTrace();
		}
	}

	/**
	 * Return the Directory that would be parent of 'child' in the 'tree'.
	 * R
	 * 
	 * 
	 * @param tree the Directory in which we look for the parent
	 * @param child the data object we want the parent for
	 * @return the parent, or null if it does not exist in 'tree'
	 */
	private Directory getParent(Directory tree, Data child) {

		assert (tree != null);
		assert (child != null);

		Directory parent = null;

		// PRT : 
		// I don't think we need all this to get the parent path.
		//if (childPath.indexOf(".") >= 0 || childPath.indexOf("[") >= 0) {
		//	
		//	String parentPath = childPath.replace("." + child.getName(), "");
		//	parentPath = parentPath.replace("[" + child.getName() + "]", "");

		if (!child.getName().equals("")){
			// is not root, so there is a parent
			try {
				// CBE FIXME if looking for the parent of Config.Rule[id].Condition
				// will have exception Config.Rule[id] not a directory
				// ???
				String parentPath = child.getParent().getPathname();
				parent = tree.getDirectory(parentPath);
			} catch (DataAccessException e) {
				// CBE added comment until FIXME done
				//logger.info("No parent for " + child.getPathname() + 
				//	"in previous tree " );
				//e.printStackTrace();
			}
		}

		return parent;
	}

	// ==============================================================================

	/**
	 * Sends notification for data arrived to all listeners.
	 * 
	 * @param source
	 * @param modifiedData
	 */
	private void dataArrived(Data source, Data modifiedData) {

		assert source != null;
		assert modifiedData != null;

		Iterator it = this.listeners.entrySet().iterator();
		while (it.hasNext()) {
			Entry item = (Entry) it.next();
			if (((String) item.getKey()).equals(source.getPathname())) {
				SdsAdapterListener[] listeners = (SdsAdapterListener[]) item
						.getValue();
				for (int i = 0; i < listeners.length; i++) {
					listeners[i]
							.onResourceArrived(new ResourceImpl(modifiedData));
				}
			}
		}
	}

	/**
	 * Sends notification for data left to all listeners.
	 * 
	 * @param source
	 * @param modifiedData
	 */
	private void dataLeft(Data source, Data modifiedData) {
		assert source != null;
		assert modifiedData != null;

		Iterator it = this.listeners.entrySet().iterator();
		while (it.hasNext()) {
			Entry item = (Entry) it.next();
			if (((String) item.getKey()).equals(source.getPathname())) {
				SdsAdapterListener[] listeners = (SdsAdapterListener[]) item
						.getValue();
				for (int i = 0; i < listeners.length; i++) {
					listeners[i].onResourceLeft(new ResourceImpl(modifiedData));
				}
			}
		}
	}

	/**
	 * Sends notification for data changes to all listeners.
	 * 
	 * @param source
	 * @param modifiedData
	 */
	private void dataChanged(Data source, Data modifiedData) {

		assert source != null;
		assert modifiedData != null;

		Iterator it = this.listeners.entrySet().iterator();
		while (it.hasNext()) {
			Entry item = (Entry) it.next();
			if (((String) item.getKey()).equals(source.getPathname())) {
				SdsAdapterListener[] listeners = (SdsAdapterListener[]) item
						.getValue();
				for (int i = 0; i < listeners.length; i++) {
					listeners[i]
							.onResourceChanged(new ResourceImpl(modifiedData));
				}
			}
		}
	}

	// ==============================================================================


}
