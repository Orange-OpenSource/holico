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

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.hlc.Condition;
import com.francetelecom.rd.hlc.HlcConnector;
import com.francetelecom.rd.hlc.HomeBusException;
import com.francetelecom.rd.hlc.InvalidResourceTypeException;
import com.francetelecom.rd.hlc.Node;
import com.francetelecom.rd.hlc.NodeDiscoveryListener;
import com.francetelecom.rd.hlc.NodeInfo;
import com.francetelecom.rd.hlc.NodeService;
import com.francetelecom.rd.hlc.Resource;
import com.francetelecom.rd.hlc.ResourcePublication;
import com.francetelecom.rd.hlc.Rule;
import com.francetelecom.rd.hlc.RuleDefinitionsListener;
import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;

/**
 * 
 * 
 * @author Enric Guiho (wqjq0154)
 * 
 */

@SuppressWarnings("rawtypes")
public class HlcConnectorImpl implements HlcConnector {

	// ==============================================================================

	/**
	 * Minimum check delay.
	 * <p>
	 * Node Availability will be checked at least every MIN_CHECK_DELAY seconds.
	 */
	protected static final int MIN_CHECK_DELAY = 45;

	/**
	 * Maximum check delay.
	 * <p>
	 * Node Availability will be checked at most every MIN_CHECK_DELAY seconds.
	 */
	protected static final int MAX_CHECK_DELAY = 60;

	private final Logger logger = LoggerFactory
			.getLogger(HlcConnectorImpl.class.getName());

	// TODO : remove sds adapter once the corresponding functionnalities have
	// been implemented in SDS
	final private SdsAdapter sdsAdapter;

	/** Root of the Home state tree. */
	final private Directory hsRoot;

	/** Node this connector have been given for. */
	final private NodeImpl node;

	/** Listeners for Node arrival and departure. */
	private NodeDiscoveryListener[] nodesListeners;

	/** Listeners for Rule creation and suppression. */
	private RuleDefinitionsListener[] rulesListeners;

	/**
	 * map <ruleid, condition resource listener>
	 * used for the rule add/update, 
	 * condition resource listener kept for the condition update, 
	 * 		when the listener has to be replaced because of new condition parameters  
	 */
	private Hashtable<String, SdsAdapterListener> listServiceForRuleInitialized;

	/**
	 * map <ruleid, resource path>
	 * used when we need to remove a resource listener, 
	 * because the rule's condition has changed
	 */
	private Hashtable<String, String> listResourcePathForRuleListened;

	/** 
	 * Timer for periodic Nodes availability check.
	 */
	private Timer periodicNodeAvailabilityTimer;

	// ==============================================================================

	public HlcConnectorImpl(final NodeImpl node, SdsAdapter sdsAdapter) {

		this.sdsAdapter = sdsAdapter;
		this.node = node;
		this.hsRoot = sdsAdapter.getRoot();
		this.listServiceForRuleInitialized = new Hashtable<String, SdsAdapterListener>();
		this.listResourcePathForRuleListened = new Hashtable<String, String>();

		// Initialize NodeListeners list.
		this.nodesListeners = new NodeDiscoveryListener[0];
		// Initialize RulesListeners list.
		this.rulesListeners = new RuleDefinitionsListener[0];

		// Subscribe to rules event.
		final String rulesPath = HomeBusPathDefinitions.CONFIG + "."
				+ HomeBusPathDefinitions.RULE;
		this.sdsAdapter.addSdsAdapterListener(rulesPath,
				new SdsAdapterListener() {

			public void onResourceArrived(Resource resource) {

				final String rulesPath = HomeBusPathDefinitions.CONFIG
						+ "." + HomeBusPathDefinitions.RULE;

				// We have a listener on HLC.Config.Rule, but all event
				// on this branch will be notified => we have to check
				// if the notif fired on a Rule and not a child.
				if (resource.getParentPath().equals(rulesPath)) {

					try {
						final Rule rule = getRule(resource.getName());
						//logger.info("---- onResourceArrived calls initServiceForRule");
						initializeServiceForRule(rule);
					} catch (HomeBusException e) {
						logger.error("Exception on rule registration ",
								e);
					} catch (IllegalArgumentException e) {
						logger.error("Exception on rule registration ",
								e);
					}
				}
			}

			public void onResourceChanged(Resource resource) {
				// CBE
				// We have a listener on HLC.Config.Rule
				final String rulesPath = HomeBusPathDefinitions.CONFIG
						+ "." + HomeBusPathDefinitions.RULE;
				if (resource.getPath().contains(rulesPath)){
					// modification to a parameter under root.Config.Rule
					try {
						int beginIndex = resource.getPath().indexOf(HomeBusPathDefinitions.CONFIG + "." 
								+ HomeBusPathDefinitions.RULE) 
								+ (HomeBusPathDefinitions.CONFIG + "." 
										+ HomeBusPathDefinitions.RULE).length()
										+ 1; // last "["
						int endIndex = beginIndex + 40;
						//logger.info("TEST " + resource.getPath());
						String ruleId = resource.getPath().substring(beginIndex, endIndex);
						final Rule rule = getRule(ruleId);
						//logger.info("---- onResourceChanged calls initServiceForRule");
						// call initializeServiceForRule which will decide if the 
						// SdsListener should be updated, given the current modification
						initializeServiceForRule(rule);
					} catch (HomeBusException e) {
						logger.error("Exception on rule modification ",
								e);
					} catch (IllegalArgumentException e) {
						logger.error("Exception on rule modification ",
								e);
					}
				}
			}

			public void onResourceLeft(Resource resource) {
				// CBE
				// We have a listener on HLC.Config.Rule
				final String rulesPath = HomeBusPathDefinitions.CONFIG
						+ "." + HomeBusPathDefinitions.RULE;
				if (resource.getPath().contains(rulesPath)){
					// modification to a parameter under root.Config.Rule
					try {
						//logger.info("---- onResourceChanged calls initServiceForRule");
						// call initializeServiceForRule which will decide if the 
						// SdsListener should be updated, given the current modification
						int beginIndex = resource.getPath().indexOf(HomeBusPathDefinitions.CONFIG + "." 
								+ HomeBusPathDefinitions.RULE) 
								+ (HomeBusPathDefinitions.CONFIG + "." 
										+ HomeBusPathDefinitions.RULE).length()
										+ 1; // last "["
						int endIndex = beginIndex + 40;
						//logger.info("TEST " + resource.getPath());
						String ruleId = resource.getPath().substring(beginIndex, endIndex);
						disableServiceForRule(ruleId);
					} catch (IllegalArgumentException e) {
						logger.error("Exception on rule removal ",
								e);
					}
				}
			}
		});

		this.periodicNodeAvailabilityTimer = new Timer();

		// generate a random delay for first check
		int delaySec = MIN_CHECK_DELAY
				+ (int) (Math.random() * ((MAX_CHECK_DELAY - MIN_CHECK_DELAY) + 1));
		periodicNodeAvailabilityTimer.schedule(new PeriodicAvailabilityCheckTask(), 
				delaySec * 1000);

	}

	// ==============================================================================

	/**
	 * see {@link HlcConnector#getAllNodes(boolean)}
	 */
	public NodeInfo[] getAllNodes(boolean includesDisconnectedNodes)
			throws HomeBusException {

		NodeInfo[] allNodes = null;

		try {
			Directory discoveryDir = hsRoot
					.getDirectory(HomeBusPathDefinitions.DISCOVERY + "."
							+ HomeBusPathDefinitions.NODE);

			ArrayList nodesInfo = new ArrayList();
			Data[] nodes = discoveryDir.getChildren();
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].getType() == Data.TYPE_GEN_DIR) {
					NodeInfo nodeInfo = getNode(nodes[i].getName());
					if (includesDisconnectedNodes
							|| nodeInfo.getAvailability() == HomeBusPathDefinitions.NODE_AVAILABILITY_AVAILABLE) {
						nodesInfo.add(nodeInfo);
					}

				}
			}
			allNodes = new NodeInfo[nodesInfo.size()];
			nodesInfo.toArray(allNodes);

		} catch (DataAccessException e) {
			logger.error("DataException : could not get nodes", e);
			throw new HomeBusException(
					"DataException : could not get nodes on Home bus", e);
		}

		assert allNodes != null;

		return allNodes;
	}

	/**
	 * see {@link HlcConnector#getNode(String)}
	 */
	public NodeInfo getNode(String nodeId) throws HomeBusException,
	IllegalArgumentException {

		// Pre-condition
		if (nodeId == null || nodeId.length() == 0) {
			throw new IllegalArgumentException("Invalid nodeId ");
		}

		NodeInfo nodeInfoResult = null;

		try {
			Directory nodeDir = hsRoot.getDirectory(getNodePath(nodeId));

			String deviceIdLeaf = "";
			if (nodeDir.contains(HomeBusPathDefinitions.NODE_DEVICEID)) {
				deviceIdLeaf = nodeDir
						.getParameterStringValue(HomeBusPathDefinitions.NODE_DEVICEID);
			}

			String nameLeaf = "";
			if (nodeDir.contains(HomeBusPathDefinitions.NODE_NAME)) {
				nameLeaf = nodeDir
						.getParameterStringValue(HomeBusPathDefinitions.NODE_NAME);
			}
			String manufacturerLeaf = "";
			if (nodeDir.contains(HomeBusPathDefinitions.NODE_MANUFACTURER)) {
				manufacturerLeaf = nodeDir
						.getParameterStringValue(HomeBusPathDefinitions.NODE_MANUFACTURER);
			}
			String versionLeaf = "";
			if (nodeDir.contains(HomeBusPathDefinitions.NODE_VERSION)) {
				versionLeaf = nodeDir
						.getParameterStringValue(HomeBusPathDefinitions.NODE_VERSION);
			}
			String keepAliveLeaf = "";
			if (nodeDir.contains(HomeBusPathDefinitions.NODE_KEEPALIVE)) {
				keepAliveLeaf = nodeDir
						.getParameterStringValue(HomeBusPathDefinitions.NODE_KEEPALIVE);
			}
			int availability = HomeBusPathDefinitions.NODE_AVAILABILITY_NOTAVAILABLE;
			if (nodeDir.contains(HomeBusPathDefinitions.NODE_AVAILABILITY)) {
				availability = nodeDir
						.getParameterIntValue(HomeBusPathDefinitions.NODE_AVAILABILITY);
			}

			NodeService[] servicesArray = new NodeService[0];
			if (nodeDir.contains(HomeBusPathDefinitions.SERVICE)) {
				servicesArray = getNodeServices(nodeDir);
			}

			ResourcePublication[] publicationsArray = new ResourcePublication[0];
			if (nodeDir.contains(HomeBusPathDefinitions.PUBLICATION)) {
				publicationsArray = getResourcePublications(nodeDir);
			}

			nodeInfoResult = new NodeInfoImpl(nodeId, deviceIdLeaf, nameLeaf,
					manufacturerLeaf, versionLeaf, keepAliveLeaf, availability,
					servicesArray, publicationsArray);

		} catch (DataAccessException e) {
			logger.error("DataException : could not get node info (" + nodeId
					+ ")", e);
			throw new HomeBusException(
					"DataException : could not get node info (" + nodeId
					+ ") on Home bus ", e);
		}

		// Post-condition
		assert (nodeInfoResult != null);
		assert nodeInfoResult.getNodeId().equals(nodeId);

		return nodeInfoResult;
	}

	/**
	 * see {@link HlcConnector#addNodeDiscoveryListener(NodeDiscoveryListener)}
	 */
	public void addNodeDiscoveryListener(NodeDiscoveryListener listener) {

		if (listener == null) {
			throw new IllegalArgumentException("Listener cannot be null");
		}

		// add the listener to the list of listeners
		int length = this.nodesListeners.length;
		NodeDiscoveryListener newListeners[] = new NodeDiscoveryListener[length + 1];
		System.arraycopy(this.nodesListeners, 0, newListeners, 0, length);
		newListeners[length] = listener;
		this.nodesListeners = newListeners;

		if (this.nodesListeners.length == 1) {
			final String nodesPath = HomeBusPathDefinitions.DISCOVERY + "."
					+ HomeBusPathDefinitions.NODE;

			// add listener on HLC.Discovery.Node
			sdsAdapter.addSdsAdapterListener(nodesPath,
					new SdsAdapterListener() {

				public void onResourceLeft(Resource resource) {
					// we have a listener on HLC.Discovery.Node, but all
					// event
					// on this branch will be notified => we have to
					// check if
					// the notif fired on a Node and not a child
					if (resource.getParentPath().equals(nodesPath)) {
						for (int i = 0; i < nodesListeners.length; i++) {
							nodesListeners[i].onNodeRemoval(resource
									.getName());
						}
					}
				}

				public void onResourceChanged(Resource resource) {
					// we have a listener on HLC.Discovery.Node, but all
					// event
					// on this branch will be notified => we have to
					// check if
					// the notif fired on a Node and not a child

					if (resource.getParentPath().equals(nodesPath)) {

						// CBE we arrive here only if the node ID changes, which never happens normally
						// the resource received here is a data tree ENDPOINT
						// so, for example, if the node's name is modified
						// the parent of the resource received is "nodeID" not the Discovery.Node path
						// so modifications on node's children are never detected
						// sol : verify if the receives resource endpoint is a node's endpoint
						// implemented after the availability check below

						// we are sure to be on nodes level
						// call onNodeModification
						for (int i = 0; i < nodesListeners.length; i++) {
							nodesListeners[i]
									.onNodeModification(resource
											.getName());
						}
					}

					// CBE if the modification concerns the nodes Availability
					// if Availability == 0 => onNodeUnavailable callback
					// if Availability == 1 => onNodeArrival callback
					// if other modifications than availability => onNodeModification

					if (resource.getName().equals(HomeBusPathDefinitions.NODE_AVAILABILITY)){
						// Availability has changed
						try {
							if (resource.getValueAsInt() == HomeBusPathDefinitions.NODE_AVAILABILITY_AVAILABLE) {
								// node becomes available = > onNodeArrival
								for (int i = 0; i < nodesListeners.length; i++) {
									nodesListeners[i]
											.onNodeArrival(resource.getParent().getName());
								}
							}
							else {
								// node becomes unavailable => onNodeUnavailable
								for (int i = 0; i < nodesListeners.length; i++) {
									nodesListeners[i]
											.onNodeUnavailable(resource.getParent().getName());
								}
							}
						} catch (InvalidResourceTypeException e) {
							logger.error("Error type of resource node.availability is not integer!" + e.getMessage());
						}
					}
					else if (resource.getPath().contains(HomeBusPathDefinitions.DISCOVERY + "." 
							+ HomeBusPathDefinitions.NODE)){
						// other parameters than availability has changed
						// this comparison should always be true
						// the resource endpoint received belongs to a node
						// => onNodeModification
						// tricky : find the node id ? 
						// we dont know at which level the resource is
						int beginIndex = resource.getPath().indexOf(HomeBusPathDefinitions.DISCOVERY + "." 
								+ HomeBusPathDefinitions.NODE) 
								+ (HomeBusPathDefinitions.DISCOVERY + "." 
										+ HomeBusPathDefinitions.NODE).length()
										+ 1; // last "["
						int endIndex = beginIndex + 40;
						String nodeId = resource.getPath().substring(beginIndex, endIndex);
						//if (!nodeId.contains(".")){
						for (int i = 0; i < nodesListeners.length; i++) {
							nodesListeners[i]
									.onNodeModification(nodeId);
						}
						//}
					}
				}

				public void onResourceArrived(Resource resource) {
					// we have a listener on HLC.Discovery.Node, but all
					// event
					// on this branch will be notified => we have to
					// check if
					// the notif fired on a Node and not a child
					if (resource.getParentPath().equals(nodesPath)) {
						for (int i = 0; i < nodesListeners.length; i++) {
							nodesListeners[i].onNodeArrival(resource
									.getName());
						}
					}
				}
			});
		}

	}

	/**
	 * see {@link HlcConnector#getAllRules()}
	 */
	public Rule[] getAllRules() throws HomeBusException {

		Rule[] allRules = null;

		try {
			Directory configDir = hsRoot
					.getDirectory(HomeBusPathDefinitions.CONFIG + "."
							+ HomeBusPathDefinitions.RULE);

			ArrayList rulesArray = new ArrayList();
			Data[] rules = configDir.getChildren();
			for (int i = 0; i < rules.length; i++) {
				if (rules[i].getType() == Data.TYPE_GEN_DIR) {
					Rule rule = getRule(rules[i].getName());
					rulesArray.add(rule);
				}
			}
			allRules = new Rule[rulesArray.size()];
			rulesArray.toArray(allRules);

		} catch (DataAccessException e) {
			logger.error("DataException : could not get rules", e);
			throw new HomeBusException(
					"DataException : could not get rules on Home bus", e);
		}

		return allRules;
	}

	/**
	 * see {@link HlcConnector#getRule(String)}
	 */
	public Rule getRule(String ruleId) throws HomeBusException,
	IllegalArgumentException {

		// Pre-condition
		if (ruleId == null || ruleId.length() == 0) {
			throw new IllegalArgumentException("Invalid ruleId ");
		}

		Rule rule = null;

		try {
			Directory ruleDir = hsRoot.getDirectory(getRulePath(ruleId));

			String nameLeaf = "";
			if (ruleDir.contains(HomeBusPathDefinitions.RULE_NAME)) {
				nameLeaf = ruleDir
						.getParameterStringValue(HomeBusPathDefinitions.RULE_NAME);
			}

			String ownerLeaf = "";
			if (ruleDir.contains(HomeBusPathDefinitions.RULE_PERMISSION + "."
					+ HomeBusPathDefinitions.RULE_PERMISSION_OWNER)) {
				ownerLeaf = ruleDir
						.getParameterStringValue(HomeBusPathDefinitions.RULE_PERMISSION
								+ "."
								+ HomeBusPathDefinitions.RULE_PERMISSION_OWNER);
			}
			boolean isPrivateLeaf = false;
			if (ruleDir.contains(HomeBusPathDefinitions.RULE_PERMISSION + "."
					+ HomeBusPathDefinitions.RULE_PERMISSION_ISPRIVATE)) {
				isPrivateLeaf = ruleDir
						.getParameterBooleanValue(HomeBusPathDefinitions.RULE_PERMISSION
								+ "."
								+ HomeBusPathDefinitions.RULE_PERMISSION_ISPRIVATE);
			}
			String conditionResourceLeaf = "";
			if (ruleDir.contains(HomeBusPathDefinitions.RULE_CONDITION + "."
					+ HomeBusPathDefinitions.RULE_CONDITION_RESOURCE)) {
				conditionResourceLeaf = ruleDir
						.getParameterStringValue(HomeBusPathDefinitions.RULE_CONDITION
								+ "."
								+ HomeBusPathDefinitions.RULE_CONDITION_RESOURCE);
			}
			int conditionOperatorLeaf = -1;
			if (ruleDir.contains(HomeBusPathDefinitions.RULE_CONDITION + "."
					+ HomeBusPathDefinitions.RULE_CONDITION_OPERATOR)) {
				conditionOperatorLeaf = ruleDir
						.getParameterIntValue(HomeBusPathDefinitions.RULE_CONDITION
								+ "."
								+ HomeBusPathDefinitions.RULE_CONDITION_OPERATOR);
			}
			int targetValueType = Data.TYPE_BOOL;
			String sConditionTargetValueLeaf = "";
			int iConditionTargetValueLeaf = -1;
			boolean bConditionTargetValueLeaf = false;
			if (ruleDir.contains(HomeBusPathDefinitions.RULE_CONDITION + "."
					+ HomeBusPathDefinitions.RULE_CONDITION_TARGETVALUE)) {
				try {
					sConditionTargetValueLeaf = ruleDir
							.getParameterStringValue(HomeBusPathDefinitions.RULE_CONDITION
									+ "."
									+ HomeBusPathDefinitions.RULE_CONDITION_TARGETVALUE);
					targetValueType = Data.TYPE_STRING;
				} catch (DataAccessException e) {
				}
				try {
					iConditionTargetValueLeaf = ruleDir
							.getParameterIntValue(HomeBusPathDefinitions.RULE_CONDITION
									+ "."
									+ HomeBusPathDefinitions.RULE_CONDITION_TARGETVALUE);
					targetValueType = Data.TYPE_INT;
				} catch (DataAccessException e) {
				}
				try {
					bConditionTargetValueLeaf = ruleDir
							.getParameterBooleanValue(HomeBusPathDefinitions.RULE_CONDITION
									+ "."
									+ HomeBusPathDefinitions.RULE_CONDITION_TARGETVALUE);
					targetValueType = Data.TYPE_BOOL;
				} catch (DataAccessException e) {
				}
			}
			String serviceReferenceLeaf = "";
			if (ruleDir.contains(HomeBusPathDefinitions.RULE_SERVICE + "."
					+ HomeBusPathDefinitions.RULE_SERVICE_REFERENCE)) {
				serviceReferenceLeaf = ruleDir
						.getParameterStringValue(HomeBusPathDefinitions.RULE_SERVICE
								+ "."
								+ HomeBusPathDefinitions.RULE_SERVICE_REFERENCE);
			}
			String serviceArgumentLeaf = "";
			if (ruleDir.contains(HomeBusPathDefinitions.RULE_SERVICE + "."
					+ HomeBusPathDefinitions.RULE_SERVICE_ARGUMENT)) {
				serviceArgumentLeaf = ruleDir
						.getParameterStringValue(HomeBusPathDefinitions.RULE_SERVICE
								+ "."
								+ HomeBusPathDefinitions.RULE_SERVICE_ARGUMENT);
			}

			Condition condition = null;

			switch (targetValueType) {
			case Data.TYPE_BOOL:
				condition = new ConditionImpl(hsRoot, conditionOperatorLeaf,
						bConditionTargetValueLeaf, conditionResourceLeaf);
				break;
			case Data.TYPE_INT:
				condition = new ConditionImpl(hsRoot, conditionOperatorLeaf,
						iConditionTargetValueLeaf, conditionResourceLeaf);
				break;
			case Data.TYPE_STRING:
				condition = new ConditionImpl(hsRoot, conditionOperatorLeaf,
						sConditionTargetValueLeaf, conditionResourceLeaf);
				break;

			}

			rule = new RuleImpl(ruleId, nameLeaf, condition,
					serviceReferenceLeaf, serviceArgumentLeaf, isPrivateLeaf,
					ownerLeaf);

		} catch (DataAccessException e) {
			logger.error("DataException : could not get rule info (" + ruleId
					+ ")", e);
			throw new HomeBusException(
					"DataException : could not get rule info (" + ruleId
					+ ") on Home bus ", e);
		}

		// Post-condition
		assert (rule != null);
		assert rule.getId().equals(ruleId);

		return rule;
	}

	/**
	 * see
	 * {@link HlcConnector#addRuleDefinitionsListener(RuleDefinitionsListener)}
	 */
	public void addRuleDefinitionsListener(RuleDefinitionsListener listener) {
		// add the listener to the list of listeners
		int length = this.rulesListeners.length;
		RuleDefinitionsListener newListeners[] = new RuleDefinitionsListener[length + 1];
		System.arraycopy(this.rulesListeners, 0, newListeners, 0, length);
		newListeners[length] = listener;
		this.rulesListeners = newListeners;

		if (this.rulesListeners.length == 1) {
			final String rulesPath = HomeBusPathDefinitions.CONFIG + "."
					+ HomeBusPathDefinitions.RULE;

			// add listener on HLC.Config.Rule
			sdsAdapter.addSdsAdapterListener(rulesPath,
					new SdsAdapterListener() {

				public void onResourceLeft(Resource resource) {
					// We have a listener on HLC.Config.Rule, but all
					// event on this branch will be notified => we have
					// to check if the notif fired on a Rule and not a
					// child.
					if (resource.getParentPath().equals(rulesPath)) {
						for (int i = 0; i < rulesListeners.length; i++) {
							rulesListeners[i].onRuleRemoved(resource
									.getName());
						}
					}
				}

				public void onResourceChanged(Resource resource) {
					// We have a listener on HLC.Config.Rule, but all
					// event on this branch will be notified => we have
					// to check if the notif fired on a Rule and not a
					// child.
					if (resource.getParentPath().equals(rulesPath)) {
						// CBE we arrive here only if the rule ID changes, which never happens normally
						// the resource received here is a data tree ENDPOINT
						// so, for example, if the rule's name is modified
						// the parent of the resource received is "ruleID" not the Config.Rule path
						// so modifications on node's children are never detected
						// sol : verify if the receives resource endpoint is a rule's endpoint
						// implemented after the availability check below
						for (int i = 0; i < rulesListeners.length; i++) {
							rulesListeners[i].onRuleChanged(resource
									.getName());
						}
					} 
					else if (resource.getPath().contains(HomeBusPathDefinitions.CONFIG + "." 
							+ HomeBusPathDefinitions.RULE)){
						// parameters under Config.Rule[id] changed
						// this comparison should always be true
						// the resource endpoint received belongs to a rule
						// => on
						// tricky : find the rule id ? 
						// we dont know at which level the resource is
						int beginIndex = resource.getPath().indexOf(HomeBusPathDefinitions.CONFIG + "." 
								+ HomeBusPathDefinitions.RULE) 
								+ (HomeBusPathDefinitions.CONFIG + "." 
										+ HomeBusPathDefinitions.RULE).length()
										+ 1; // last "["
						int endIndex = beginIndex + 40; // 40 length of an id generated by tools
						String ruleId = resource.getPath().substring(beginIndex, endIndex);
						for (int i = 0; i < rulesListeners.length; i++) {
							rulesListeners[i]
									.onRuleChanged(ruleId);
						}
					}
				}

				public void onResourceArrived(Resource resource) {
					// We have a listener on HLC.Config.Rule, but all
					// event on this branch will be notified => we have
					// to check if the notif fired on a Rule and not a
					// child.
					if (resource.getParentPath().equals(rulesPath)) {
						for (int i = 0; i < rulesListeners.length; i++) {
							rulesListeners[i].onRuleAdded(resource
									.getName());
						}
					}
				}
			});
		}
	}

	/**
	 * see {@link HlcConnector#addRule(Rule)}
	 */
	public String addRule(final Rule rule) throws IllegalArgumentException,
	HomeBusException {

		// Pre-conditions
		if (rule == null) {
			logger.error("Illegal null rule");
			throw new IllegalArgumentException("Illegal null rule");
		}
		if (rule.getId() == null || rule.getId().isEmpty()) {
			logger.error("Illegal rule with null id");
			throw new IllegalArgumentException("Illegal rule with null id");
		}

		final String configRulePath = HomeBusPathDefinitions.CONFIG + "."
				+ HomeBusPathDefinitions.RULE;
		final String rulePath = configRulePath + "[" + rule.getId() + "]";

		if (hsRoot.contains(rulePath)) {
			// rule already exist
			logger.error("Rule ("
					+ rule.getId()
					+ ") already exist. Maybe you want to call updateRule instead of addRule");
			throw new IllegalArgumentException(
					"Rule ("
							+ rule.getId()
							+ ") already exist. Maybe you want to call updateRule instead of addRule");
		}

		// create Rule structure in HLC
		// lock HSD to be sure to commit a full rule in a
		// single transaction
		sdsAdapter.getHomeSharedData().lock();

		try 
		{
			if (!hsRoot.contains(configRulePath)) {
				// create Config.Rule path
				hsRoot.newData(configRulePath, Data.TYPE_SPE_DIR, true);
			}
			hsRoot.newData(rulePath, Data.TYPE_GEN_DIR, true);
			hsRoot.newData(rulePath + "."
					+ HomeBusPathDefinitions.RULE_PERMISSION,
					Data.TYPE_GEN_DIR, true);
			hsRoot.newData(rulePath + "."
					+ HomeBusPathDefinitions.RULE_CONDITION,
					Data.TYPE_GEN_DIR, true);
			hsRoot.newData(rulePath + "."
					+ HomeBusPathDefinitions.RULE_SERVICE,
					Data.TYPE_GEN_DIR, true);

			// set Rule info
			setRuleInfo(rulePath, rule);

		} 
		catch (DataAccessException e) 
		{

			logger.error("Rule (" + rule.getId()
					+ ") creation failed in HomeLifeContext tree");
			throw new HomeBusException("Rule (" + rule.getId()
					+ ") creation failed in HomeLifeContext tree");
		}
		finally
		{
			sdsAdapter.getHomeSharedData().unlock();
		}

		//logger.info("---- add rule calls initServiceForRule");
		initializeServiceForRule(rule);
		return rule.getId();
	}

	/**
	 * see {@link HlcConnector#updateRule(Rule)}
	 */
	public String updateRule(final Rule rule) throws IllegalArgumentException,
	HomeBusException {

		// Pre-conditions
		if (rule == null) {
			logger.error("Illegal null rule");
			throw new IllegalArgumentException("Illegal null rule");
		}
		if (rule.getId() == null || rule.getId().isEmpty()) {
			logger.error("Illegal rule with null id");
			throw new IllegalArgumentException("Illegal rule with null id");
		}

		try {
			String configRulePath = HomeBusPathDefinitions.CONFIG + "."
					+ HomeBusPathDefinitions.RULE;
			final String rulePath = configRulePath + "[" + rule.getId() + "]";

			if (hsRoot.contains(rulePath)) {

				String currentOwner;

				currentOwner = hsRoot.getParameterStringValue(rulePath + "."
						+ HomeBusPathDefinitions.RULE_PERMISSION + "."
						+ HomeBusPathDefinitions.RULE_PERMISSION_OWNER);

				if (!currentOwner.equals(rule.getNodeOwner())) {
					logger.error("cannot update an existing rule "
							+ rule.getId() + " with a different owner "
							+ rule.getNodeOwner() + " (previous owner : "
							+ currentOwner + ")");
					throw new IllegalArgumentException(
							"cannot update an existing rule " + rule.getId()
							+ " with a different owner "
							+ rule.getNodeOwner()
							+ " (previous owner : " + currentOwner
							+ ")");
				}

			} else {
				// rule does not exist
				logger.error("Rule ("
						+ rule.getId()
						+ ") does not exist. Maybe you want to call addRule instead of updateRule");
				throw new IllegalArgumentException(
						"Rule ("
								+ rule.getId()
								+ ") does not exist. Maybe you want to call addRule instead of updateRule");
			}

			// update Rule structure in HLC
			// lock HSD to be sure to commit a full rule in a
			// single transaction
			sdsAdapter.getHomeSharedData().lock();

			// set Rule info
			try 
			{
				setRuleInfo(rulePath, rule);
			} 
			catch (DataAccessException e) 
			{
				logger.error("Rule (" + rule.getId()
						+ ") update failed in HomeLifeContext tree");
				throw new HomeBusException("Rule (" + rule.getId()
						+ ") update failed in HomeLifeContext tree");
			}
			finally
			{
				sdsAdapter.getHomeSharedData().unlock();
			}


		} catch (DataAccessException e) {
			throw new HomeBusException("Cannot update rule in tree");
		}
		logger.info("---- update rule calls initServiceForRule");
		//initializeServiceForRule(rule);
		return rule.getId();
	}


	/** 
	 * see {@link HlcConnector#removeRule(String)}
	 */
	public void removeRule(String ruleToRemoveId) throws IllegalArgumentException, 
	HomeBusException {
		// CBE
		// Pre-conditions
		if (ruleToRemoveId == null) {
			logger.error("Illegal null rule id");
			throw new IllegalArgumentException("Illegal null rule id");
		}
		if (ruleToRemoveId.isEmpty()) {
			logger.error("Illegal empty rule id");
			throw new IllegalArgumentException("Illegal empty rule id");
		}

		final String ruleToRemovePath = HomeBusPathDefinitions.CONFIG + "."
				+ HomeBusPathDefinitions.RULE + "[" + ruleToRemoveId + "]";

		if (!hsRoot.contains(ruleToRemovePath)) {
			// rule does not exist
			logger.error("Rule ("
					+ ruleToRemoveId
					+ ") doesn't exist. Maybe you want to addRule first, and then remove it");
			throw new IllegalArgumentException(
					"Rule ("
							+ ruleToRemoveId
							+ ") doesn't exist. Maybe you want to addRule first, and then remove it");
		}

		boolean canRemoveRule = true;
		Rule[] allRules = getAllRules();
		int rulesNb = allRules.length;
		for (int i = 0; i < allRules.length; i++){
			if (allRules[i].getId().equals(ruleToRemoveId)){
				// found rule to remove
				if (allRules[i].isPrivate()){
					// rule to remove is private
					if (!allRules[i].getNodeOwner().equals(node.getNodeId())){
						// self node is not rule's owner
						// self node does not have the right to remove the rule
						canRemoveRule = false;
					}
				}
			}
		}
		if (!canRemoveRule){
			logger.error("Rule ("
					+ ruleToRemoveId
					+ ") id private and self node is not the owner. No right to remove a private rule if not owner");
			throw new IllegalArgumentException(
					"Rule ("
							+ ruleToRemoveId
							+ ") id private and self node is not the owner. No right to remove a private rule if not owner");
		}

		try{
			hsRoot.deleteData(ruleToRemovePath);		
		} catch (DataAccessException e) {

			logger.error("Rule (" + ruleToRemoveId
					+ ") deletion failed in HomeLifeContext tree");
			throw new HomeBusException("Rule (" + ruleToRemoveId
					+ ") deletion failed in HomeLifeContext tree");
		}

		disableServiceForRule(ruleToRemoveId);
	}

	/**
	 * see {@link HlcConnector#getHomeLifeContextRoot()}
	 */
	public Resource getHomeLifeContextRoot() {
		return new ResourceImpl(hsRoot);
	}

	/**
	 * see {@link HlcConnector#getNodeService(String)}
	 */
	public NodeService getNodeService(String serviceId)throws HomeBusException,
	IllegalArgumentException {
		// Pre-condition
		if (serviceId == null || serviceId.length() == 0) {
			throw new IllegalArgumentException("Invalid serviceId ");
		}

		//NodeService[] allServices;
		// get all nodes and check for services 
		NodeInfo[] nodes = getAllNodes(true);
		//ArrayList servicesArray = new ArrayList();
		for (int i=0; i < nodes.length; i++){
			NodeInfo n = nodes[i];
			NodeService[] ns = n.getNodeServices();
			for (int j = 0; j < ns.length; j++){
				//servicesArray.add(ns[j]);
				if(ns[j].getNodeServiceId().equals(serviceId)){
					// service found
					return ns[j];
				}
			}
		}
		//allServices = new NodeService[servicesArray.size()];
		//servicesArray.toArray(allServices);
		//return allServices;

		throw new HomeBusException("Service not found on Home bus" +
				" for given id : " + serviceId);

	}

	public NodeInfo getServiceOwner(String serviceId)throws HomeBusException,
	IllegalArgumentException {
		// Pre-condition
		if (serviceId == null || serviceId.length() == 0) {
			throw new IllegalArgumentException("Invalid serviceId ");
		}

		NodeInfo[] nodes = getAllNodes(true);
		for (int i=0; i < nodes.length; i++){
			NodeInfo n = nodes[i];
			NodeService[] ns = n.getNodeServices();
			for (int j = 0; j < ns.length; j++){
				if(ns[j].getNodeServiceId().equals(serviceId)){
					// service found
					return n;
				}
			}
		}

		throw new HomeBusException("Service owner not found on Home bus" +
				" for given service id : " + serviceId);

	}

	// ==============================================================================

	private String getNodePath(String nodeId) {
		return HomeBusPathDefinitions.DISCOVERY + "."
				+ HomeBusPathDefinitions.NODE + "[" + nodeId + "]";
	}

	private String getRulePath(String ruleId) {
		return HomeBusPathDefinitions.CONFIG + "."
				+ HomeBusPathDefinitions.RULE + "[" + ruleId + "]";
	}

	private NodeService[] getNodeServices(Directory nodeDir)
			throws DataAccessException {

		Directory servicesDir = nodeDir
				.getDirectory(HomeBusPathDefinitions.SERVICE);

		ArrayList services = new ArrayList();
		Data[] childServices = servicesDir.getChildren();
		for (int i = 0; i < childServices.length; i++) {
			if (childServices[i].getType() == Data.TYPE_GEN_DIR) {
				Directory nodeServiceDir = (Directory) childServices[i];
				boolean isPrivateLeaf = nodeServiceDir
						.getParameterBooleanValue(HomeBusPathDefinitions.SERVICE_PERMISSION
								+ "."
								+ HomeBusPathDefinitions.SERVICE_PERMISSION_ISPRIVATE);
				String serviceNameLeaf = nodeServiceDir
						.getParameterStringValue(HomeBusPathDefinitions.SERVICE_NAME);

				String paramNameLeaf = null;
				int paramtypeLeaf = -1;
				if (nodeServiceDir
						.contains(HomeBusPathDefinitions.SERVICE_PARAMETERNAME)) {
					paramNameLeaf = nodeServiceDir
							.getParameterStringValue(HomeBusPathDefinitions.SERVICE_PARAMETERNAME);
					paramtypeLeaf = nodeServiceDir
							.getParameterIntValue(HomeBusPathDefinitions.SERVICE_PARAMETERTYPE);
				}
				NodeService service = new NodeService(
						childServices[i].getName(), isPrivateLeaf,
						serviceNameLeaf, paramNameLeaf, paramtypeLeaf);

				services.add(service);
			}
		}
		NodeService[] servicesArray = new NodeService[services.size()];
		services.toArray(servicesArray);

		return servicesArray;

	}

	private ResourcePublication[] getResourcePublications(Directory nodeDir)
			throws DataAccessException {

		Directory resourcePubsDir = nodeDir
				.getDirectory(HomeBusPathDefinitions.PUBLICATION);

		ArrayList resourcePubs = new ArrayList();
		Data[] childResourcePublications = resourcePubsDir.getChildren();
		for (int i = 0; 
				(childResourcePublications != null && i < childResourcePublications.length)
				; i++) {
			if (childResourcePublications[i].getType() == Data.TYPE_GEN_DIR) {
				Directory resourcePubDir = (Directory) childResourcePublications[i];
				String resourcePubPathLeaf = resourcePubDir
						.getParameterStringValue(HomeBusPathDefinitions.PUBLICATION_REFRENCE);
				int resourcePubTypeLeaf = resourcePubDir
						.getParameterIntValue(HomeBusPathDefinitions.PUBLICATION_TYPE);

				ResourcePublication publication = new ResourcePublication(
						childResourcePublications[i].getName(),
						resourcePubPathLeaf, resourcePubTypeLeaf);

				resourcePubs.add(publication);
			}
		}
		ResourcePublication[] publicationsArray = new ResourcePublication[resourcePubs
		                                                                  .size()];
		resourcePubs.toArray(publicationsArray);

		return publicationsArray;
	}

	/**
	 * Performs a availability check for all Nodes.
	 * <p>
	 * Any node that has not updated it's keepalive recently enough will be
	 * marqued as unavailable.
	 * 
	 */
	private void nodesAvailabilityCheck() {
		try {
			// get all connected nodes
			NodeInfo[] nodes = getAllNodes(false);

			for (int i = 0; i < nodes.length; ++i) {

				NodeInfo node = nodes[i];
				long lastKeepAliveTimestamp = Long.parseLong(node
						.getKeepAlive());
				long nowTimestamp = (new Date()).getTime();
				boolean nodeIsAlive = (nowTimestamp - lastKeepAliveTimestamp) <= (NodeImpl.KEEPALIVE_UPDATE_DELAY * NodeImpl.KEEPALIVE_LEFT_FACTOR);

				if (!nodeIsAlive) {
					// We have found a node that has not update it's keepalive
					// recently. Set it as unavailable.
					String availabilityPath = getNodePath(node.getNodeId())
							+ "." + HomeBusPathDefinitions.NODE_AVAILABILITY;
					hsRoot.setParameterValue(
							availabilityPath,
							HomeBusPathDefinitions.NODE_AVAILABILITY_NOTAVAILABLE);
				}
			}

		} catch (HomeBusException e) {
			logger.error("Error when checking nodes availability", e);
		} catch (NumberFormatException e) {
			logger.error("Error when checking nodes availability", e);
		} catch (DataAccessException e) {
			logger.error("Error when checking nodes availability", e);
		}
	}

	/**
	 * Timer task for periodic availability check.
	 * 
	 */
	private class PeriodicAvailabilityCheckTask  extends TimerTask {

		@Override
		public void run() {
			nodesAvailabilityCheck();

			// generate a random delay for next check
			int delaySec = MIN_CHECK_DELAY
					+ (int) (Math.random() * ((MAX_CHECK_DELAY - MIN_CHECK_DELAY) + 1));

			PeriodicAvailabilityCheckTask nextChecktask = new PeriodicAvailabilityCheckTask();
			periodicNodeAvailabilityTimer.schedule(nextChecktask,
					delaySec * 1000);
		}

	};

	/**
	 * 
	 * @param rulePath
	 * @param rule
	 * @throws DataAccessException
	 */
	private void setRuleInfo(String rulePath, Rule rule)
			throws DataAccessException {

		String namePath = rulePath + "." + HomeBusPathDefinitions.RULE_NAME;
		hsRoot.newData(namePath, Data.TYPE_STRING, true);
		hsRoot.setParameterValue(namePath, rule.getName());

		String ownerPath = rulePath + "."
				+ HomeBusPathDefinitions.RULE_PERMISSION + "."
				+ HomeBusPathDefinitions.RULE_PERMISSION_OWNER;
		hsRoot.newData(ownerPath, Data.TYPE_STRING, true);
		hsRoot.setParameterValue(ownerPath, rule.getNodeOwner());

		String isPrivatePath = rulePath + "."
				+ HomeBusPathDefinitions.RULE_PERMISSION + "."
				+ HomeBusPathDefinitions.RULE_PERMISSION_ISPRIVATE;
		hsRoot.newData(isPrivatePath, Data.TYPE_BOOL, true);
		hsRoot.setParameterValue(isPrivatePath, new Boolean(rule.isPrivate()));

		String conditionResourcePath = rulePath + "."
				+ HomeBusPathDefinitions.RULE_CONDITION + "."
				+ HomeBusPathDefinitions.RULE_CONDITION_RESOURCE;
		hsRoot.newData(conditionResourcePath, Data.TYPE_STRING, true);
		hsRoot.setParameterValue(conditionResourcePath, rule.getCondition()
				.getResourcePath());

		String conditionOperatorPath = rulePath + "."
				+ HomeBusPathDefinitions.RULE_CONDITION + "."
				+ HomeBusPathDefinitions.RULE_CONDITION_OPERATOR;
		hsRoot.newData(conditionOperatorPath, Data.TYPE_INT, true);
		hsRoot.setParameterValue(conditionOperatorPath, new Integer(rule
				.getCondition().getOperator()));

		String conditionTargetValPath = rulePath + "."
				+ HomeBusPathDefinitions.RULE_CONDITION + "."
				+ HomeBusPathDefinitions.RULE_CONDITION_TARGETVALUE;
		switch (rule.getCondition().getTargetValueType()) {
		case Data.TYPE_BOOL:
			hsRoot.newData(conditionTargetValPath, Data.TYPE_BOOL, true);
			hsRoot.setParameterValue(conditionTargetValPath, new Boolean(rule
					.getCondition().getTargetBooleanValue()));
			break;
		case Data.TYPE_INT:
			hsRoot.newData(conditionTargetValPath, Data.TYPE_INT, true);
			hsRoot.setParameterValue(conditionTargetValPath, new Integer(rule
					.getCondition().getTargetIntValue()));
			break;
		case Data.TYPE_STRING:
			hsRoot.newData(conditionTargetValPath, Data.TYPE_STRING, true);
			hsRoot.setParameterValue(conditionTargetValPath, rule
					.getCondition().getTargetStringValue());
			break;
		}

		String serviceReferencePath = rulePath + "."
				+ HomeBusPathDefinitions.RULE_SERVICE + "."
				+ HomeBusPathDefinitions.RULE_SERVICE_REFERENCE;
		hsRoot.newData(serviceReferencePath, Data.TYPE_STRING, true);
		hsRoot.setParameterValue(serviceReferencePath,
				rule.getServiceReference());

		String serviceArgumentPath = rulePath + "."
				+ HomeBusPathDefinitions.RULE_SERVICE + "."
				+ HomeBusPathDefinitions.RULE_SERVICE_ARGUMENT;
		hsRoot.newData(serviceArgumentPath, Data.TYPE_STRING, true);
		hsRoot.setParameterValue(serviceArgumentPath, rule.getArgument());
	}

	/**
	 * initialization of a service for a rule means : 
	 * creating an SdsAdapterListener for the resource indicated by the rule's condition. 
	 * - listServiceForRuleInitialized<ruleIs, SdsAdapterListener> (not very correct name) 
	 * memorizes the listeners created for a ruleId
	 * - listResourcePathForRuleListened<ruleId, resourcePath>
	 * @param rule
	 */
	private synchronized void initializeServiceForRule(final Rule rule) {

		// CBE
		// this might be called two times :
		// 1. once by addRule and
		// 2. once by onResourceArrived that detected new rule in
		// Config.Rule
		// so here we must make sure that for a rule the service was not
		// already initialized
		// the check done in sdsAdapter.addSdsAdapterListener to see if the
		// listener for the rule does not already exist
		// will never detect the another listener for the same rule because
		// it does equal (==) on 2 objects SdsAdapterListener
		// created here, so the references will never be the same, even if
		// they are done for the same service/rule (they are created
		// dynamically)
		// solution 1 : maintain here a list of rules that already had their
		// services initialized
		// and check here
		// this solution has to be modified when the rule change will be
		// detected (below onResourceChange will be completed)
		// and the rule service will have to be reinitialized!!!!
		// solution 2 : modify SdsAdapterListener to add the rule id,
		// overwrite the equal method to detect if 2 objects are equal if
		// they have the same rule id
		// and with this solution, we can leave the check in
		// sdsAdapter.addSdsAdapterListener
		// CBE -

		// CBE +
		// i think it is not correct to check if the rule's owner is the self
		// node
		// because a node A can respond to a rule created by another node B,
		// rule that uses node's A service
		// i think we should check if the rule's service reference belongs to
		// the self node
		// Important also : service reference : i think it should be formed out
		// of 2 ids (serviceOwner=nodeId + serviceId)
		// in order to uniquely identify a service and not worry whether 2 nodes
		// use the same serviceId
		// if (rule.getNodeOwner().equals(node.getNodeId())) {

		boolean serviceFound = false;
		for (int i = 0; i < node.getNodeServices().length; i++) {
			if (rule.getServiceReference().equals(
					(node.getNodeServices()[i]).getNodeServiceId())) {
				// the service invoked b the rule belongs to self node
				serviceFound = true;
				// logger.info("will activate service " +
				// (node.getNodeServices()[i]).getNodeServiceId());
			}
		}
		if (serviceFound) {
			//logger.info("---- initialize service that belong to the node");
			// if rule id already exists in the list of initialized services for a rule=>
			// remove the listener from sds adapter, because a new one will be added below
			if (listServiceForRuleInitialized.containsKey(rule.getId())){
				//logger.info("---- will remove old rule resource : " 
				//	+ HomeBusPathDefinitions.HLC + "." +
				//  listResourcePathForRuleListened.get(rule.getId())
				//	+ " listener : " 
				//	+ listServiceForRuleInitialized.get(rule.getId()).getId());
				sdsAdapter.removeSdsAdapterListener(HomeBusPathDefinitions.HLC + "." +
						listResourcePathForRuleListened.get(rule.getId()), // resource path
						listServiceForRuleInitialized.get(rule.getId())); // listener for this resource path
				// we remove listener and resource path because will be added below
				listResourcePathForRuleListened.remove(rule.getId());
				listServiceForRuleInitialized.remove(rule.getId());
			}

			// if arrived here, the rule id is not in the list => initialize the
			// service for this rule :
			// 1. create listener for the resource indicated by the rule's condition,
			// 2. add the listener to sdsAdapter list of listeners,
			// 3. memorize the resource path in the listResourcePathForRuleListened
			// 4. memorize the listener in listServiceForRuleInitialized

			// 1.
			SdsAdapterListener conditionResourceListener = new SdsAdapterListener() {
				public void onResourceLeft(Resource resource) {
					perfomAction();
				}
				public void onResourceChanged(Resource resource) {
					perfomAction();
				}
				public void onResourceArrived(Resource resource) {
					perfomAction();
				}
				private void perfomAction() {
					try {
						if (rule.getCondition().isSatisfied()) {
							node.getServiceCallback(
									rule.getServiceReference())
									.onServiceActivated(
											rule.getArgument());
						}
					} catch (HomeBusException e) {
						logger.error(
								"Exception when performing rule action ",
								e);
					} catch (InvalidResourceTypeException e) {
						logger.error(
								"Exception when performing rule action ",
								e);
					}
				}
			};

			//2.
			sdsAdapter.addSdsAdapterListener(HomeBusPathDefinitions.HLC + "."
					+ rule.getCondition().getResourcePath(), conditionResourceListener);
			//logger.info("---- added new listener : " + conditionResourceListener.getId()
			//		+ " for resource " + HomeBusPathDefinitions.HLC + "."
			//		+ rule.getCondition().getResourcePath());
			//3. 
			listResourcePathForRuleListened.put(rule.getId(), 
					rule.getCondition().getResourcePath());

			//4.
			listServiceForRuleInitialized.put(rule.getId(), conditionResourceListener);
			// CBE -
		}
	}

	/**
	 * when rule removed from tree
	 * remove sds listeners for the resource indicated by the rule's condition
	 * @param removedRuleId
	 */
	private synchronized void disableServiceForRule(String removedRuleId){
		// CBE
		if (listServiceForRuleInitialized.containsKey(removedRuleId)){
			sdsAdapter.removeSdsAdapterListener(HomeBusPathDefinitions.HLC + "." +
					listResourcePathForRuleListened.get(removedRuleId), // resource path
					listServiceForRuleInitialized.get(removedRuleId)); // listener for this resource path
			// we remove listener and resource path 
			listResourcePathForRuleListened.remove(removedRuleId);
			listServiceForRuleInitialized.remove(removedRuleId);
		}
	}

}
