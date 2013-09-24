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

import java.security.NoSuchAlgorithmException;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.hlc.Condition;
import com.francetelecom.rd.hlc.HomeBusException;
import com.francetelecom.rd.hlc.Node;
import com.francetelecom.rd.hlc.Rule;
import com.francetelecom.rd.sds.Directory;

/**
 * 
 * @author Pierre Rust (tksh1670)
 * 
 */
public class HomeBusFactory {

	private final SdsAdapter sdsAdapter;

	private final Logger logger = LoggerFactory.getLogger(HomeBusFactory.class
			.getName());

	/**
	 * 
	 * @param deviceId
	 * @throws NoSuchAlgorithmException
	 */
	public HomeBusFactory(int deviceId) throws NoSuchAlgorithmException {

		// Initialize SecureRandom
		// This is a lengthy operation, to be done only upon
		// initialization of the application
		Tools.initializaidGeneration();

		// Initialize SdsAdapter
		sdsAdapter = new SdsAdapter(deviceId);
	}

	/**
	 * Create a new Node. The node will not be published on the Home Bus (and
	 * thus cannot be used) until you call {@link Node#publishOnHomeBus()}.
	 * 
	 * @param nodeId
	 * @param deviceId
	 * @param name
	 * 
	 * @return the Node
	 * 
	 * @throws HomeBusException
	 * @throws IllegalArgumentException
	 */
	public Node createNode(String nodeId, String deviceId, String name)
			throws HomeBusException, IllegalArgumentException {

		// Pre-conditions
		if (nodeId == null || nodeId.length() == 0) {
			logger.error("Illegal nodeId: " + nodeId);
			throw new IllegalArgumentException("Illegal nodeId: " + nodeId);
		}
		if (deviceId == null || deviceId.length() == 0) {
			logger.error("Illegal deviceId: " + deviceId);
			throw new IllegalArgumentException("Illegal deviceId: " + deviceId);
		}
		if (name == null || name.length() == 0) {
			logger.error("Illegal name: " + name);
			throw new IllegalArgumentException("Illegal name: " + name);
		}

		NodeImpl newNode = new NodeImpl(nodeId, deviceId, name, sdsAdapter);

		assert newNode != null;
		return newNode;
	}

	/**
	 * Create a new Condition.
	 * 
	 * @param operator
	 * @param targetValue
	 * @param resourcePath
	 * 
	 * @return the Condition
	 * 
	 * @throws HomeBusException
	 * @throws IllegalArgumentException
	 */
	public Condition createCondition(int operator, Object targetValue,
			String resourcePath) throws HomeBusException,
			IllegalArgumentException {

		// Pre-conditions
		if (operator != Condition.OPERATOR_DIFF
				&& operator != Condition.OPERATOR_EQUAL
				&& operator != Condition.OPERATOR_INF
				&& operator != Condition.OPERATOR_INFEQUAL
				&& operator != Condition.OPERATOR_SUP
				&& operator != Condition.OPERATOR_SUPEQUAL) {
			logger.error("Illegal operator: " + operator);
			throw new IllegalArgumentException("Illegal operator: " + operator);
		}
		if (targetValue == null) {
			logger.error("Illegal targetValue: " + targetValue);
			throw new IllegalArgumentException("Illegal targetValue: "
					+ targetValue);
		}
		if (resourcePath == null || resourcePath.length() == 0) {
			logger.error("Illegal resourcePath: " + resourcePath);
			throw new IllegalArgumentException("Illegal resourcePath: "
					+ resourcePath);
		}

		ConditionImpl condition = null;

		if (targetValue instanceof String) {
			condition = new ConditionImpl(sdsAdapter.getRoot(), operator,
					(String) targetValue, resourcePath);
		} else if (targetValue instanceof Integer) {
			condition = new ConditionImpl(sdsAdapter.getRoot(), operator,
					((Integer) targetValue).intValue(), resourcePath);
		} else if (targetValue instanceof Boolean) {
			condition = new ConditionImpl(sdsAdapter.getRoot(), operator,
					((Boolean) targetValue).booleanValue(), resourcePath);
		}

		assert condition != null;
		return condition;
	}

	/**
	 * Create a new Rule.
	 * 
	 * @param name
	 * @param condition
	 * @param serviceReference
	 * @param argument
	 * @param isPrivate
	 * @param nodeOwner
	 * 
	 * @return the Rule
	 * 
	 * @throws HomeBusException
	 * @throws IllegalArgumentException
	 */
	public Rule createRule(String name, Condition condition,
			String serviceReference, String argument, boolean isPrivate,
			String nodeOwner) throws HomeBusException, IllegalArgumentException {

		// Pre-conditions
		if (name == null || name.length() == 0) {
			logger.error("Illegal name: " + name);
			throw new IllegalArgumentException("Illegal name: " + name);
		}
		if (condition == null) {
			logger.error("Illegal condition: " + condition);
			throw new IllegalArgumentException("Illegal condition: "
					+ condition);
		}
		if (serviceReference == null || serviceReference.length() == 0) {
			logger.error("Illegal serviceReference: " + serviceReference);
			throw new IllegalArgumentException("Illegal serviceReference: "
					+ serviceReference);
		}
		if (argument == null) {
			logger.error("Illegal argument: " + argument);
			throw new IllegalArgumentException("Illegal argument: " + argument);
		}
		if (nodeOwner == null || nodeOwner.length() == 0) {
			logger.error("Illegal nodeOwner: " + nodeOwner);
			throw new IllegalArgumentException("Illegal nodeOwner: "
					+ nodeOwner);
		}

		RuleImpl newRule = new RuleImpl(name, condition, serviceReference,
				argument, isPrivate, nodeOwner);

		assert newRule != null;
		return newRule;
	}

	/**
	 * Create a new Rule from ruleId (use to update a existing rule)
	 * 
	 * @param ruleId
	 * @param name
	 * @param condition
	 * @param serviceReference
	 * @param argument
	 * @param isPrivate
	 * @param nodeOwner
	 * 
	 * @return the Rule
	 * 
	 * @throws HomeBusException
	 * @throws IllegalArgumentException
	 */
	public Rule createRule(String ruleId, String name, Condition condition,
			String serviceReference, String argument, boolean isPrivate,
			String nodeOwner) throws HomeBusException, IllegalArgumentException {

		// Pre-conditions
		if (ruleId == null || ruleId.length() == 0) {
			logger.error("Illegal ruleId: " + ruleId);
			throw new IllegalArgumentException("Illegal ruleId: " + ruleId);
		}
		if (name == null || name.length() == 0) {
			logger.error("Illegal name: " + name);
			throw new IllegalArgumentException("Illegal name: " + name);
		}
		if (condition == null) {
			logger.error("Illegal condition: " + condition);
			throw new IllegalArgumentException("Illegal condition: "
					+ condition);
		}
		if (serviceReference == null || serviceReference.length() == 0) {
			logger.error("Illegal serviceReference: " + serviceReference);
			throw new IllegalArgumentException("Illegal serviceReference: "
					+ serviceReference);
		}
		if (argument == null) {
			logger.error("Illegal argument: " + argument);
			throw new IllegalArgumentException("Illegal argument: " + argument);
		}
		if (nodeOwner == null || nodeOwner.length() == 0) {
			logger.error("Illegal nodeOwner: " + nodeOwner);
			throw new IllegalArgumentException("Illegal nodeOwner: "
					+ nodeOwner);
		}

		RuleImpl newRule = new RuleImpl(ruleId, name, condition,
				serviceReference, argument, isPrivate, nodeOwner);

		assert newRule != null;
		return newRule;
	}

	/**
	 * <b>WARNING !! </b> This method is only here to allow direct access to the
	 * HS tree for Unit tests, it should <b>NEVER</b> be used outside tests.
	 * 
	 * @return
	 */
	protected Directory getHsRoot() {
		return sdsAdapter.getRoot();

	}


	/**
	 * 
	 * 
	 * @return
	 */
	public String generateNodeId() {

		return Tools.generateId();
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public String generateDeviceId() {

		return Tools.generateId();
	}

	/**
	 * 
	 * @return
	 */
	public String generateServiceId() {
		return Tools.generateId();
	}
}
