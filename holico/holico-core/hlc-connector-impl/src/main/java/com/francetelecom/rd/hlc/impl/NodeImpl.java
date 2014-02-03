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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.hlc.HlcConnector;
import com.francetelecom.rd.hlc.HomeBusException;
import com.francetelecom.rd.hlc.InvalidResourcePathException;
import com.francetelecom.rd.hlc.InvalidResourceTypeException;
import com.francetelecom.rd.hlc.Node;
import com.francetelecom.rd.hlc.NodeService;
import com.francetelecom.rd.hlc.NodeServiceCallback;
import com.francetelecom.rd.hlc.ResourcePublication;
import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;
import com.francetelecom.rd.sds.HomeSharedData;

/**
 * 
 * 
 * @author Pierre Rust (tksh1670)
 * 
 */
public class NodeImpl implements Node {

	final Logger logger = LoggerFactory.getLogger(NodeImpl.class.getName());

	/**
	 * Delay between 2 keepalive parameter updates when a node is alive.
	 */
	static int KEEPALIVE_UPDATE_DELAY = 1000 * 60; // 1 minute

	/**
	 * factor to check if a node is still alive
	 * 
	 * if : (now - keepalive) > KEEPALIVE_LEFT_FACTOR * KEEPALIVE_UPDATE_DELAY
	 * then : node has left
	 */
	static final double KEEPALIVE_LEFT_FACTOR = 2.5;

	/**
	 * Indicates if this node is already published on the home bus.
	 */
	private boolean isPublishedOnHomeBus;

	/**
	 * HomeSharedData
	 */
	private final HomeSharedData hsData;

	/**
	 * Root of the SDS data tree
	 */
	private final Directory hsRoot;

	/**
	 * The nodeId for this Node.
	 */
	private final String nodeId;

	/**
	 * The deviceId for this Node.
	 */
	private final String deviceId;

	/**
	 * The name of the Node.
	 */
	private String name;

	/**
	 * 
	 */
	private String manufacturer;

	/**
	 * 
	 */
	private String version;

	/**
	 * 
	 */
	private String keepAlive;

	/***
	 * 
	 */
	private int availability;

	/**
	 * The services provided by this node.
	 */
	private NodeService[] nodeServices;

	/**
	 * The publications of this node.
	 */
	private ResourcePublication[] resourcePublications;

	/**
	 * The services callbacks map.
	 */
	private Map servicesCallbacks;

	/**
	 * The update keepalive timer.
	 */
	private Timer updateKeepaliveTimer;


	/** Bus Connector. */
	private HlcConnectorImpl connector;

	/**
	 * The constructor is protected because the NodeBuilder MUST always be used
	 * when building a Node.
	 * 
	 * @param nodeId
	 */
	protected NodeImpl(String nodeId, String deviceId, String name,
	      HomeSharedData hsData) {
		this.nodeId = nodeId;
		this.deviceId = deviceId;
		this.name = name;
		this.hsData = hsData;
		this.hsRoot = hsData.getRootDirectory(false, null, null);
		this.nodeServices = new NodeService[0];
		this.resourcePublications = new ResourcePublication[0];
		this.isPublishedOnHomeBus = false;
		this.version = "";
		this.manufacturer = "";
		this.keepAlive = "";
		this.availability = HomeBusPathDefinitions.NODE_AVAILABILITY_NOTAVAILABLE;

		this.servicesCallbacks = new HashMap();
	}

	/**
	 * see {@link NodeInfo#getNodeId()}
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * see {@link NodeInfo#getDeviceId()}
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * see {@link NodeInfo#getName()}
	 */
	public String getName() {
		return name;
	}

	/**
	 * see {@link NodeInfo#getVersion()}
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * see {@link NodeInfo#getManufacturer()}
	 */
	public String getManufacturer() {
		return this.manufacturer;
	}

	/**
	 * see {@link NodeInfo#getKeepAlive()}
	 */
	public String getKeepAlive() {
		return this.keepAlive;
	}

	/**
	 * see {@link NodeInfo#getAvailability()}
	 */
	public Integer getAvailability() {
		return new Integer(availability);
	}	

	/**
	 * see {@link NodeInfo#getNodeServices()}
	 */
	public NodeService[] getNodeServices() {
		return nodeServices;
	}

	/**
	 * see {@link NodeInfo#getResourcePublications()}
	 */
	public ResourcePublication[] getResourcePublications() {
		return resourcePublications;
	}

	/**
	 * see {@link Node#setName(String)}
	 */
	public void setName(String name) throws HomeBusException {

		// Pre-condition
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Invalid name ");
		}

		if (isPublishedOnHomeBus) {

			try {
				String nodePath = getNodepath();
				hsRoot.newData(nodePath, Data.TYPE_GEN_DIR, true);

				String namePath = nodePath + "."
						+ HomeBusPathDefinitions.NODE_NAME;
				hsRoot.newData(namePath, Data.TYPE_STRING, true);
				hsRoot.setParameterValue(namePath, this.name);

				this.name = name;
			} catch (DataAccessException e) {
				logger.error("DataException : could not set the name (" + name
						+ ") of  the node correctly on Home bus " + nodeId, e);
				throw new HomeBusException(
						"DataException : could not set the name (" + name
						+ ") of  the node correctly on Home bus "
						+ nodeId + " correctly on Home bus ", e);
			}
		} else {
			this.name = name;
		}

		assert name.equals(this.name);
		logger.info("set name " + name + " on node " + nodeId);
	}

	/**
	 * see {@link Node#setManufacturer(String)}
	 */
	public void setManufacturer(String manufacturer) throws HomeBusException {

		// Pre-condition
		if (manufacturer == null || manufacturer.length() == 0) {
			throw new IllegalArgumentException("Invalid manufacturer ");
		}

		if (isPublishedOnHomeBus) {
			throw new HomeBusException(
					"DataException : cannot change manufacturer after node "
							+ "publication ");
		}
		this.manufacturer = manufacturer;

		assert manufacturer.equals(this.manufacturer);
	}

	/**
	 * see {@link Node#setVersion(String)}
	 */
	public void setVersion(String version) throws HomeBusException {

		// Pre-condition
		if (version == null || version.length() == 0) {
			throw new IllegalArgumentException("Invalid version ");
		}

		if (isPublishedOnHomeBus) {
			throw new HomeBusException(
					"DataException : cannot change version after node "
							+ "publication ");
		}
		this.version = version;

		assert version.equals(this.version);
	}

	/**
	 * see {@link Node#publishOnHomeBus()}
	 */
	public boolean publishOnHomeBus() throws HomeBusException,
	InvalidResourceTypeException, InvalidResourcePathException {

		if (isPublishedOnHomeBus) {
			logger.warn("Calling publishOnHomeBus on already published node "
					+ nodeId);
			return false;
		}

		// FIXME : if the publication fail, we may get a partial node in the
		// tree ?

		// lock HSD to be sure to commit a full node in a
		// single transaction
		hsData.lock();

		try 
		{
			String nodePath = getNodepath();
			if (hsRoot.contains(nodePath)) {
				// Node already exist in the HS tree, check if we
				// may update it

				Directory nodeDir = hsRoot.getDirectory(nodePath);

				String treeDeviceId = nodeDir
						.getParameterStringValue(HomeBusPathDefinitions.NODE_DEVICEID);

				if (!deviceId.equals((String) treeDeviceId)) {

					logger.error("An node declartion already exists in the Bus"
							+ " for this nodeId "
							+ nodeId
							+ "but with a different deviceId ");
					throw new InvalidResourceTypeException(
							"An node declartion already exists in the Bus"
									+ " for this nodeId " + nodeId
									+ "but with a different deviceId ");
				}

				// the name, version and manufacturer can be
				// modified on republication
				hsRoot.setParameterValue(nodePath + "."
						+ HomeBusPathDefinitions.NODE_NAME, name);
				hsRoot.setParameterValue(nodePath + "."
						+ HomeBusPathDefinitions.NODE_VERSION, version);
				hsRoot.setParameterValue(nodePath + "."
						+ HomeBusPathDefinitions.NODE_MANUFACTURER,
						manufacturer);

				// set availability
				String availabilityPath = getNodepath() + "."
						+ HomeBusPathDefinitions.NODE_AVAILABILITY;
				hsRoot.setParameterValue(availabilityPath,
						HomeBusPathDefinitions.NODE_AVAILABILITY_AVAILABLE);

				updateKeepAlive(true);

			} else {
				// Node does not exist in the HS tree : create it
				hsRoot.newData(nodePath, Data.TYPE_GEN_DIR, true);

				String namePath = nodePath + "."
						+ HomeBusPathDefinitions.NODE_NAME;
				hsRoot.newData(namePath, Data.TYPE_STRING, true);
				hsRoot.setParameterValue(namePath, name);

				String deviceidPath = nodePath + "."
						+ HomeBusPathDefinitions.NODE_DEVICEID;
				hsRoot.newData(deviceidPath, Data.TYPE_STRING, true);
				hsRoot.setParameterValue(deviceidPath, deviceId);

				String manufacturerPath = nodePath + "."
						+ HomeBusPathDefinitions.NODE_MANUFACTURER;
				hsRoot.newData(manufacturerPath, Data.TYPE_STRING, true);
				hsRoot.setParameterValue(manufacturerPath, manufacturer);

				String versionPath = nodePath + "."
						+ HomeBusPathDefinitions.NODE_VERSION;
				hsRoot.newData(versionPath, Data.TYPE_STRING, true);
				hsRoot.setParameterValue(versionPath, version);

				String keepAlivePath = nodePath + "."
						+ HomeBusPathDefinitions.NODE_KEEPALIVE;
				hsRoot.newData(keepAlivePath, Data.TYPE_STRING, true);

				// set availability
				String availabilityPath = getNodepath() + "."
						+ HomeBusPathDefinitions.NODE_AVAILABILITY;
				hsRoot.newData(availabilityPath, Data.TYPE_INT, true);
				hsRoot.setParameterValue(availabilityPath,
						HomeBusPathDefinitions.NODE_AVAILABILITY_AVAILABLE);

				updateKeepAlive(true);

			}
			// Declare / Update publications
			for (int i = 0; i < resourcePublications.length; ++i) {
				addPublicationDeclaration(resourcePublications[i].getId(),
						resourcePublications[i].getResourcePath(),
						resourcePublications[i].getType());
			}

			// Declare / Update Services
			for (int i = 0; i < nodeServices.length; ++i) {
				addServiceDeclaration(nodeServices[i].getNodeServiceId(),
						nodeServices[i].getName(),
						nodeServices[i].isPrivate(),
						nodeServices[i].getParameterName(),
						nodeServices[i].getParameterType());
			}

			// Create a timer to update keepalive periodically
			TimerTask updateKeepaliveTask = new TimerTask() {
				public void run() {
					try {

						updateKeepAlive(false);

					} catch (InvalidResourcePathException e) {
						logger.error(e.getMessage(), e);
					} catch (DataAccessException e) {
						logger.error(e.getMessage(), e);
					}
				}
			};
			updateKeepaliveTimer.scheduleAtFixedRate(updateKeepaliveTask,
					KEEPALIVE_UPDATE_DELAY, KEEPALIVE_UPDATE_DELAY);

		} 
		catch (DataAccessException e) 
		{
			logger.error(
					"DataException : could not publish the node correctly on Home bus "
							+ nodeId, e);
			throw new HomeBusException(
					"DataException, could not publish the node " + nodeId
					+ " correctly on Home bus ", e);
		} 
		catch (InvalidResourcePathException e) 
		{
			logger.error(
					"An invalid node declaration already exists in the Bus"
							+ " for this nodeId " + nodeId, e);
			throw new InvalidResourceTypeException(
					"An invalid node declaration already exists in the Bus"
							+ " for this nodeId " + nodeId);
		} 
		catch (InvalidResourceTypeException e) 
		{
			logger.error(
					"An invalid node declaration already exists in the Bus"
							+ " for this nodeId " + nodeId, e);
			throw new InvalidResourceTypeException(
					"An invalid node declaration already exists in the Bus"
							+ " for this nodeId " + nodeId);
		}
		finally
		{
			hsData.unlock();
		}

		isPublishedOnHomeBus = true;
		connector = new HlcConnectorImpl(this, hsData);

		return true;
	}

	/**
	 * see {@link Node#isPublishedOnHomeBus()}
	 */
	public boolean isPublishedOnHomeBus() {
		return isPublishedOnHomeBus;
	}

	/**
	 * see {@link Node#unPublishFromHomeBus()}
	 */
	public boolean unPublishFromHomeBus() throws HomeBusException {

		if (!isPublishedOnHomeBus) {
			logger.warn("Trying to unpublished a node taht has not been published before");
			return false;
		}

		updateKeepaliveTimer.cancel();
		updateKeepaliveTimer = null;

		String availabilityPath = getNodepath() + "."
				+ HomeBusPathDefinitions.NODE_AVAILABILITY;
		try {
			hsRoot.setParameterValue(availabilityPath,
					HomeBusPathDefinitions.NODE_AVAILABILITY_NOTAVAILABLE);

		} catch (NumberFormatException e) {
			logger.error("Exception when setting the node as unavailable", e);
		} catch (DataAccessException e) {
			logger.error("Exception when setting the node as unavailable", e);
		}
		isPublishedOnHomeBus = false;
		// CBE : will no longer listen to sds events of data change
		// TODO
		return true;
	}

	public boolean removesFromHomeBus() {

		updateKeepaliveTimer.cancel();
		updateKeepaliveTimer = null;

		String availabilityPath = getNodepath() + "."
				+ HomeBusPathDefinitions.NODE_AVAILABILITY;
		try {
			hsRoot.setParameterValue(availabilityPath,
					HomeBusPathDefinitions.NODE_AVAILABILITY_NOTAVAILABLE);

		} catch (NumberFormatException e) {
			logger.error("Exception when setting the node as unavailable", e);
		} catch (DataAccessException e) {
			logger.error("Exception when setting the node as unavailable", e);
		}

		return false;
	}

	public boolean addNodeService(String serviceId, String name,
			boolean isPrivate, NodeServiceCallback nsCallback)
					throws HomeBusException {

		// Pre-condition
		if (serviceId == null || serviceId.length() == 0) {
			throw new IllegalArgumentException("Invalid serviceId ");
		}
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Invalid name ");
		}
		if (nsCallback == null) {
			throw new IllegalArgumentException(
					"Invalid null NodeServiceCallback ");
		}

		int length = this.nodeServices.length;
		// check if it has not already been declared
		for (int i = 0; i < length; ++i) {
			if (serviceId.equals(this.nodeServices[i].getNodeServiceId())) {
				logger.warn("Ignoring duplicate declaration for service ("
						+ serviceId + ") at path " + name);
				return false;

			}
		}

		// if the service is already published, update publications in the sds
		// tree right now
		if (isPublishedOnHomeBus) {
			try {
				addServiceDeclaration(serviceId, name, isPrivate,
						nsCallback.getParameterName(),
						nsCallback.getParameterType());
			} catch (DataAccessException e) {
				logger.error(" Cannot declare new service on Home bus ", e);
				throw new HomeBusException(
						" Cannot declare new service on Home bus ", e);
			}
		}

		// add the service to the list of services
		NodeService newServices[] = new NodeService[length + 1];
		System.arraycopy(this.nodeServices, 0, newServices, 0, length);
		newServices[length] = new NodeService(serviceId, isPrivate, name,
				nsCallback.getParameterName(), nsCallback.getParameterType());
		this.nodeServices = newServices;

		// save callback
		servicesCallbacks.put(serviceId, nsCallback);

		assert this.nodeServices.length == length + 1;
		return isPublishedOnHomeBus;
	}

	public boolean removeNodeService(String serviceId) throws HomeBusException {
		// Pre-conditions
		if (serviceId == null || serviceId.length() == 0) {
			logger.error("Illegal serviceId: " + serviceId);
			throw new IllegalArgumentException("Illegal serviceId: "
					+ serviceId);
		}

		boolean found = false;
		int length = this.nodeServices.length;
		for (int i = 0; i < length; ++i) {
			if (serviceId.equals(this.nodeServices[i].getNodeServiceId())) {

				// we have found the service, remove it
				found = true;

				if (isPublishedOnHomeBus) {
					Directory servicesDir;
					try {
						servicesDir = getServicesDir();
						servicesDir.deleteData(nodeServices[i]
								.getNodeServiceId());
					} catch (DataAccessException e) {
						logger.error(" Cannot remove service from Home bus ", e);
						throw new HomeBusException(
								" Cannot remove service from Home bus ", e);
					}
				}

				NodeService newSerivces[] = new NodeService[length - 1];
				System.arraycopy(nodeServices, 0, newSerivces, 0, i);

				if (i != length) {
					System.arraycopy(nodeServices, i + 1, newSerivces, i,
							length - i - 1);
				}
				break;
			}
		}
		if (!found) {
			assert nodeServices.length == length;
			logger.warn("Could not find service to remove with id " + serviceId);
			return false;
		}

		assert nodeServices.length == length - 1;
		logger.info("Removed service with id" + serviceId + " from node "
				+ nodeId);
		return isPublishedOnHomeBus;
	}

	public boolean addResourcePublication(String id, String resourcePath,
			int type) throws HomeBusException, InvalidResourceTypeException,
			InvalidResourcePathException {

		// Pre-conditions
		if (resourcePath == null || resourcePath.length() == 0) {
			logger.error("Illegal resourcePath: " + nodeId);
			throw new IllegalArgumentException("Illegal resourcePath: "
					+ resourcePath);
		}
		if (id == null || id.length() == 0) {
			logger.error("Illegal id: " + id);
			throw new IllegalArgumentException("Illegal id: " + id);
		}

		// switch from Connector to Sds Types
		type = Tools.getSdsTypeFromConnectorType(type);

		int length = this.resourcePublications.length;
		// check if it has not already been declared
		for (int i = 0; i < length; ++i) {
			if (resourcePath.equals(this.resourcePublications[i]
					.getResourcePath())) {
				if (type == resourcePublications[i].getType()) {
					logger.warn("Ignoring duplicate declaration for publication ("
							+ id + ") at path " + resourcePath);
					return false;
				} else {
					logger.error("Conflicting declaration for publication ("
							+ id + ") at path " + resourcePath);
					throw new InvalidResourceTypeException(
							"Conflicting declaration for publication (" + id
							+ ") at path " + resourcePath
							+ " ( existing type : "
							+ resourcePublications[i].getType()
							+ " refused new type : " + type);
				}
			}
		}

		// if the node is already published, update publications in the sds tree
		// right now
		if (isPublishedOnHomeBus) {
			try {
				addPublicationDeclaration(id, resourcePath, type);
			} catch (DataAccessException e) {
				logger.error(" Cannot declare new publication on Home bus ", e);
				throw new HomeBusException(
						" Cannot declare new publication on Home bus ", e);
			}
		}

		// add the resource to the list of publications
		ResourcePublication newPubs[] = new ResourcePublication[length + 1];
		System.arraycopy(this.resourcePublications, 0, newPubs, 0, length);
		newPubs[length] = new ResourcePublication("" + length, resourcePath,
				type);
		this.resourcePublications = newPubs;

		assert this.resourcePublications.length == length + 1;
		return isPublishedOnHomeBus;
	}

	public boolean removeResourcePublicationByPath(String resourcePath)
			throws HomeBusException {

		// Pre-conditions
		if (resourcePath == null || resourcePath.length() == 0) {
			logger.error("Illegal resourcePath: " + nodeId);
			throw new IllegalArgumentException("Illegal resourcePath: "
					+ resourcePath);
		}

		boolean found = false;
		int length = this.resourcePublications.length;
		for (int i = 0; i < length; ++i) {
			if (resourcePath.equals(this.resourcePublications[i]
					.getResourcePath())) {

				// we have found the publication, remove it
				found = true;

				if (isPublishedOnHomeBus) {
					Directory pubsDir;
					try {
						pubsDir = getPublicationsDir();
						pubsDir.deleteData(resourcePublications[i].getId());
					} catch (DataAccessException e) {
						logger.error(
								" Cannot remove publication from Home bus ", e);
						throw new HomeBusException(
								" Cannot remove publication from Home bus ", e);
					}
				}

				ResourcePublication newPubs[] = new ResourcePublication[length - 1];
				System.arraycopy(resourcePublications, 0, newPubs, 0, i);

				if (i != length) {
					System.arraycopy(resourcePublications, i + 1, newPubs, i,
							length - i - 1);
				}
				break;
			}
		}
		if (!found) {
			assert resourcePublications.length == length;
			logger.warn("Could not find publication to remove with path "
					+ resourcePath);
			return false;
		}

		assert resourcePublications.length == length - 1;
		logger.info("Removed publication with path" + resourcePath
				+ " from node " + nodeId);
		return isPublishedOnHomeBus;
	}

	public boolean removeResourcePublicationById(String id)
			throws HomeBusException {

		// Pre-conditions
		if (id == null || id.length() == 0) {
			logger.error("Illegal id: " + id);
			throw new IllegalArgumentException("Illegal id: " + id);
		}

		boolean found = false;
		int length = this.resourcePublications.length;
		for (int i = 0; i < length; ++i) {
			if (this.resourcePublications[i].getId().equals(id)) {

				// we have found the publication, remove it
				found = true;

				if (isPublishedOnHomeBus) {
					Directory pubsDir;
					try {
						pubsDir = getPublicationsDir();
						pubsDir.deleteData(id);
					} catch (DataAccessException e) {
						logger.error(
								" Cannot remove publication from Home bus ", e);
						throw new HomeBusException(
								" Cannot remove publication from Home bus ", e);
					}
				}

				ResourcePublication newPubs[] = new ResourcePublication[length - 1];
				System.arraycopy(resourcePublications, 0, newPubs, 0, i);

				if (i != length) {
					System.arraycopy(resourcePublications, i + 1, newPubs, i,
							length - i - 1);
				}
				break;
			}
		}
		if (!found) {
			assert resourcePublications.length == length;
			logger.warn("Could not find publication to remove for id " + id);
			return false;
		}

		assert resourcePublications.length == length - 1;
		logger.info("Removed publication with id " + id + " from node "
				+ nodeId);
		return isPublishedOnHomeBus;
	}

	public void publishOnResource(String resourcePath, Object value)
			throws HomeBusException, InvalidResourceTypeException,
			InvalidResourcePathException {

		// Pre-conditions
		if (resourcePath == null || resourcePath.length() == 0) {
			logger.error("Illegal resourcePath: " + resourcePath);
			throw new IllegalArgumentException("Illegal resourcePath: "
					+ resourcePath);
		}
		if (value == null
				|| (!(value instanceof String) && !(value instanceof Integer) && !(value instanceof Boolean))) {
			logger.error("Illegal value: " + value);
			throw new IllegalArgumentException("Illegal value: " + resourcePath);
		}

		if (!isPublishedOnHomeBus) {
			throw new HomeBusException(
					"Cannot access resource from an unpublished node ");
		}

		boolean found = false;
		int length = this.resourcePublications.length;
		for (int i = 0; i < length; ++i) {
			if (resourcePath.equals(this.resourcePublications[i]
					.getResourcePath())) {

				// we have found the publication, set it
				found = true;

				try {
					String resourceFullpath = HomeBusPathDefinitions.HLC + "."
							+ resourcePath;
					hsRoot.setParameterValue(resourceFullpath, value);
				} catch (DataAccessException e) {
					logger.error(
							" Cannot set publication value from Home bus ", e);
					throw new HomeBusException(
							" Cannot set publication value from Home bus ", e);
				}

				break;
			}
		}
		if (!found) {
			logger.warn("Could not find publication to set with path "
					+ resourcePath);
			throw new IllegalArgumentException(
					"Could not find publication to set with path "
							+ resourcePath);
		}
	}

	public HlcConnector getHlcConnector() throws HomeBusException {

		if (!isPublishedOnHomeBus) {
			throw new HomeBusException(
					"Cannot access connector from an unpublished node ");
		}

		// the connector must habe been created when the node was published 

		assert connector != null;
		return connector;
	}

	public NodeServiceCallback getServiceCallback(String serviceId)
			throws IllegalArgumentException {
		// Pre-conditions
		if (serviceId == null || serviceId.length() == 0) {
			logger.error("Illegal serviceId: " + serviceId);
			throw new IllegalArgumentException("Illegal serviceId: "
					+ serviceId);
		}
		boolean serviceIdFound = false;
		for (Iterator it = this.servicesCallbacks.entrySet().iterator(); it
				.hasNext();) {
			Map.Entry pairs = (Map.Entry) it.next();
			String id = (String) pairs.getKey();
			if (id.equals(serviceId)) {
				serviceIdFound = true;
				break;
			}
		}
		if (!serviceIdFound) {
			logger.error("serviceId unknown in callbacks list: " + serviceId);
			throw new IllegalArgumentException(
					"serviceId unknown in callbacks list:" + serviceId);
		}

		return (NodeServiceCallback) this.servicesCallbacks.get(serviceId);
	}

	// ==============================================================================

	/**
	 * Update the keepAlive info for this Node in the HSD tree.
	 * 
	 * @param force
	 * @throws DataAccessException
	 * @throws InvalidResourcePathException
	 */
	private void updateKeepAlive(boolean force)
			throws InvalidResourcePathException, DataAccessException {

		if (updateKeepaliveTimer == null ) {
			this.updateKeepaliveTimer = new Timer();
		}

		synchronized (this) {

			// Nothing to do is not published on HomeBus
			if (isPublishedOnHomeBus || force) {
				String keepAlivePath = getNodepath() + "."
						+ HomeBusPathDefinitions.NODE_KEEPALIVE;
				Date now = new Date();
				String nowStr = String.valueOf(now.getTime());
				hsRoot.setParameterValue(keepAlivePath, nowStr);
			}
		}
	}

	/**
	 * Declare a new publication in the HSD tree.
	 * 
	 * If the publication does not exists, declares it If the resource of the
	 * publication does not exist, creates it. *
	 * 
	 * @param index
	 * @param resourcePath
	 * @param type
	 * @throws DataAccessException
	 * @throws InvalidResourceTypeException
	 * @throws InvalidResourcePathException
	 * @throws HomeBusException
	 */
	private void addPublicationDeclaration(final String id,
			final String resourcePath, final int type)
					throws DataAccessException, InvalidResourceTypeException,
					InvalidResourcePathException, HomeBusException {

		assert id != null && !id.isEmpty();
		assert resourcePath != null && resourcePath.length() > 0;

		// check if the resource path is coherent with the current tree
		// and create it if it does not exist

		String resourceFullpath = HomeBusPathDefinitions.HLC + "."
				+ resourcePath;
		if (hsRoot.contains(resourceFullpath)) {

			Data targetPubData = hsRoot.getChild(resourceFullpath);
			if (targetPubData.getType() != type) {
				logger.error("cannot declare publication with type " + type
						+ " at path " + resourcePath
						+ " : type conflicts with existing Resource ("
						+ targetPubData.getType() + ")");
				throw new InvalidResourceTypeException(
						"cannot declare publication with type " + type
						+ " at path " + resourcePath
						+ " : type conflicts with existing Resource ("
						+ targetPubData.getType() + ")");
			}

		} else {

			// check if the parent accept child
			int index = -1;
			String parentPath = resourceFullpath;
			while ((index = parentPath.lastIndexOf('.')) >= 0) {
				parentPath = resourceFullpath.substring(0, index);
				if (hsRoot.contains(parentPath)) {

					Data parentData = hsRoot.getChild(parentPath);
					if (parentData.getType() != Data.TYPE_GEN_DIR
							&& parentData.getType() != Data.TYPE_SPE_DIR) {
						logger.error("cannot declare publication with type "
								+ type
								+ " at path "
								+ resourcePath
								+ " : type conflicts with existing Resource branch ("
								+ parentData.getName() + " / "
								+ parentData.getType() + ")");
						throw new InvalidResourceTypeException(
								"cannot declare publication with type "
										+ type
										+ " at path "
										+ resourcePath
										+ " : type conflicts with existing Resource branch ("
										+ parentData.getName() + " / "
										+ parentData.getType() + ")");
					}
				}
			}

			// the HLC path does not exists yet, create it :
			hsRoot.newData(resourceFullpath, type, true);
		}

		// now declare the publication in the tree for this Node

		// lock HSD to be sure to commit a full publication in a
		// single transaction
		hsData.lock();

		try 
		{
			Directory pubsDir;

			pubsDir = getPublicationsDir();

			String pubPath = getPublicationsDir().getPathname() + "[" + id
					+ "]";
			if (pubsDir.contains(id)) {

				// declaration already exists, update it if possible

				Directory pubDir = pubsDir.getDirectory(id);
				hsRoot.setParameterValue(pubPath + "."
						+ HomeBusPathDefinitions.PUBLICATION_REFRENCE,
						resourcePath);

				String existingPath = pubDir
						.getParameterStringValue(HomeBusPathDefinitions.PUBLICATION_REFRENCE);
				if (resourcePath.equals(existingPath)) {
					// resource path is the same as the already existing
					// declaration
					// : type must be the same too
					int existingType = pubDir
							.getParameterIntValue(HomeBusPathDefinitions.PUBLICATION_TYPE);
					if (existingType != type) {
						logger.error("cannot declare publication with type "
								+ type
								+ " at path "
								+ resourcePath
								+ " : type conflicts with existing declaration");
						throw new InvalidResourceTypeException(
								"cannot declare publication with type "
										+ type
										+ " at path "
										+ resourcePath
										+ " : type conflicts with existing declaration");
					}
				} else {
					// TODO !

				}

				String refPath = pubPath + "."
						+ HomeBusPathDefinitions.PUBLICATION_REFRENCE;
				hsRoot.newData(refPath, Data.TYPE_STRING, true);
				hsRoot.setParameterValue(refPath, resourcePath);

				String typePath = pubPath + "."
						+ HomeBusPathDefinitions.PUBLICATION_TYPE;
				hsRoot.newData(typePath, Data.TYPE_INT, true);
				hsRoot.setParameterValue(typePath, new Integer(type));

			} else {

				// publication does not exist : create it
				hsRoot.newData(pubPath, Data.TYPE_GEN_DIR, true);

				String refPath = pubPath + "."
						+ HomeBusPathDefinitions.PUBLICATION_REFRENCE;
				hsRoot.newData(refPath, Data.TYPE_STRING, true);
				hsRoot.setParameterValue(refPath, resourcePath);

				String typePath = pubPath + "."
						+ HomeBusPathDefinitions.PUBLICATION_TYPE;
				hsRoot.newData(typePath, Data.TYPE_INT, true);
				hsRoot.setParameterValue(typePath, new Integer(type));

			}
		} 
		catch (DataAccessException e) 
		{
			logger.error("cannot declare publication with type " + type
					+ " at path " + resourcePath
					+ " : failure during HomeLifeContext tree access");
			throw new HomeBusException(
					"cannot declare publication with type "
							+ type
							+ " at path "
							+ resourcePath
							+ " : failure during HomeLifeContext tree access");
		}
		finally
		{
			hsData.unlock();
		}
	}

	/**
	 * Declare a new Service in the HSD tree
	 * 
	 * @param serviceId
	 *            id of the service in the Node Service directory
	 * @param name
	 *            name of the service
	 * @param isPrivate
	 * @param parameterName
	 * @param parameterType
	 * @throws DataAccessException
	 * @throws HomeBusException
	 */
	private void addServiceDeclaration(final String serviceId,
			final String name, final boolean isPrivate,
			final String parameterName, final int parameterType)
					throws DataAccessException, HomeBusException {

		assert serviceId != null && serviceId.length() > 0;
		assert name != null && name.length() > 0;

		// parameter not mandatory
		// assert parameterName != null && parameterName.length() > 0;

		// lock HSD to be sure to commit a full service in a
		// single transaction
		hsData.lock();

		try 
		{
			String servicepath = getServicesDir().getPathname() + "["
					+ serviceId + "]";
			hsRoot.newData(servicepath, Data.TYPE_GEN_DIR, true);

			String servNamePath = servicepath + "."
					+ HomeBusPathDefinitions.SERVICE_NAME;
			hsRoot.newData(servNamePath, Data.TYPE_STRING, true);
			hsRoot.setParameterValue(servNamePath, name);

			if (parameterName != null && parameterName.length() > 0) {

				String paramNamePath = servicepath + "."
						+ HomeBusPathDefinitions.SERVICE_PARAMETERNAME;
				hsRoot.newData(paramNamePath, Data.TYPE_STRING, true);
				hsRoot.setParameterValue(paramNamePath, parameterName);

				String paramTypePath = servicepath + "."
						+ HomeBusPathDefinitions.SERVICE_PARAMETERTYPE;
				hsRoot.newData(paramTypePath, Data.TYPE_INT, true);
				hsRoot.setParameterValue(paramTypePath, new Integer(
						parameterType));
			}

			hsRoot.newData(servicepath + "."
					+ HomeBusPathDefinitions.SERVICE_PERMISSION,
					Data.TYPE_GEN_DIR, true);

			String isPrivateePath = servicepath + "."
					+ HomeBusPathDefinitions.SERVICE_PERMISSION + "."
					+ HomeBusPathDefinitions.SERVICE_PERMISSION_ISPRIVATE;
			hsRoot.newData(isPrivateePath, Data.TYPE_BOOL, true);
			hsRoot.setParameterValue(isPrivateePath, new Boolean(isPrivate));

		} 
		catch (DataAccessException e) 
		{
			logger.error("cannot declare service with id " + serviceId
					+ " at name " + name
					+ " : failure during HomeLifeContext tree access");
			throw new HomeBusException("cannot declare service with id "
					+ serviceId + " at name " + name
					+ " : failure during HomeLifeContext tree access");
		}
		finally
		{
			hsData.unlock();
		}
	}

	/**
	 * 
	 * @return the Data directory where services are declared
	 * @throws DataAccessException
	 */
	private Directory getServicesDir() throws DataAccessException {
		assert nodeId != null;

		Directory nodeDir = null;
		String nodePath = getNodepath();
		if (hsRoot.contains(nodePath)) {
			nodeDir = hsRoot.getDirectory(nodePath);
		} else {
			nodeDir = (Directory) hsRoot.newData(nodePath, Data.TYPE_GEN_DIR,
					true);
		}
		Directory servicesDir = null;
		if (nodeDir.contains(HomeBusPathDefinitions.SERVICE)) {
			servicesDir = nodeDir.getDirectory(HomeBusPathDefinitions.SERVICE);
		} else {
			servicesDir = (Directory) hsRoot.newData(nodePath + "."
					+ HomeBusPathDefinitions.SERVICE, Data.TYPE_SPE_DIR, true);
		}
		assert servicesDir != null;
		return servicesDir;
	}

	/**
	 * 
	 * @return the Data directory where publications are declared
	 * @throws DataAccessException
	 */
	private Directory getPublicationsDir() throws DataAccessException {
		assert nodeId != null;

		Directory nodeDir = null;
		String nodePath = getNodepath();
		if (hsRoot.contains(getNodepath())) {
			nodeDir = hsRoot.getDirectory(nodePath);
		} else {
			nodeDir = (Directory) hsRoot.newData(nodePath, Data.TYPE_GEN_DIR,
					true);
		}
		Directory pubsDir = null;
		if (nodeDir.contains(HomeBusPathDefinitions.PUBLICATION)) {
			pubsDir = nodeDir.getDirectory(HomeBusPathDefinitions.PUBLICATION);
		} else {
			pubsDir = (Directory) hsRoot.newData(nodePath + "."
					+ HomeBusPathDefinitions.PUBLICATION, Data.TYPE_SPE_DIR,
					true);
		}

		assert pubsDir != null;
		return pubsDir;
	}

	private String getNodepath() {
		return HomeBusPathDefinitions.DISCOVERY + "."
				+ HomeBusPathDefinitions.NODE + "[" + nodeId + "]";
	}

	/**
	 * <b>WARNING !! </b> This method is only here to allow direct access to
	 * reduce KEEPALIVE_UPDATE_DELAY for Unit tests, it should <b>NEVER</b> be
	 * used outside tests.
	 * 
	 * @return
	 */
	protected void setKeepAliveUpdateDelay(int delay) {
		KEEPALIVE_UPDATE_DELAY = delay;
	}



}
