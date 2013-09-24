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

import com.francetelecom.rd.hlc.Condition;
import com.francetelecom.rd.hlc.Node;
import com.francetelecom.rd.hlc.NodeService;
import com.francetelecom.rd.hlc.Rule;

public class RuleImpl implements Rule {

	// ==============================================================================

	/**
	 * Id of the rule
	 */
	private final String ruleId;

	/**
	 * Name of the rule
	 */
	private final String name;

	/**
	 * {@link Condition} of the rule
	 */
	private final Condition condition;

	/**
	 * Service reference of the rule
	 */
	private final String serviceReference;

	/**
	 * Service argument of the rule
	 */
	private final String argument;

	/**
	 * Indicates if the rule is private
	 */
	private final boolean isPrivate;

	/**
	 * Node owner of the rule
	 */
	private final String nodeOwner;

	// ==============================================================================

	/**
	 * {@link Rule} constructor
	 * <p>
	 * Constructor without ruleId : Use it to create a new Rule
	 * 
	 * @param name
	 *            the name of the rule. Usefull for Gui.
	 * 
	 * @param condition
	 *            the {@link Condition} of the rule.
	 * 
	 * @param serviceReference
	 *            the service reference of the rule.
	 * 
	 * @param argument
	 *            the service argument of the rule.
	 * 
	 * @param isPrivate
	 *            the privacy type of the rule.
	 * 
	 * @param nodeOwner
	 *            the node owner of the rule.
	 */
	public RuleImpl(final String name, final Condition condition,
			final String serviceReference, final String argument,
			final boolean isPrivate, final String nodeOwner) {

		// TODO : modifier gestion de l'argument : il faut pouvoir tester le
		// type du paramètre avec ce qui a été publié lors de la création d'un
		// service

		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Invalid ruleId");
		}
		if (condition == null) {
			throw new IllegalArgumentException("Invalid condition");
		}
		if (serviceReference == null || serviceReference.length() == 0) {
			throw new IllegalArgumentException("Invalid nodeService");
		}
		if (nodeOwner == null || nodeOwner.length() == 0) {
			throw new IllegalArgumentException("Invalid nodeOwner");
		}

		this.ruleId = Tools.generateId();
		this.name = name;
		this.condition = condition;
		this.serviceReference = serviceReference;
		this.argument = argument;
		this.isPrivate = isPrivate;
		this.nodeOwner = nodeOwner;
	}

	/**
	 * {@link Rule} constructor
	 * <p>
	 * Constructor wit ruleId : used by connector during rules recovery in
	 * HomeLifeContext tree
	 * 
	 * @param ruleId
	 *            the ruleId.
	 * 
	 * @param name
	 *            the name of the rule. Usefull for Gui.
	 * 
	 * @param condition
	 *            the {@link Condition} of the rule.
	 * 
	 * @param serviceReference
	 *            the service reference of the rule.
	 * 
	 * @param argument
	 *            the service argument of the rule.
	 * 
	 * @param isPrivate
	 *            the privacy type of the rule.
	 * 
	 * @param nodeOwner
	 *            the node owner of the rule.
	 */
	public RuleImpl(final String ruleId, final String name,
			final Condition condition, final String serviceReference,
			final String argument, final boolean isPrivate,
			final String nodeOwner) {

		// TODO : modifier gestion de l'argument : il faut pouvoir tester le
		// type du paramètre avec ce qui a été publié lors de la création d'un
		// service

		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Invalid ruleId");
		}
		if (condition == null) {
			throw new IllegalArgumentException("Invalid condition");
		}
		if (serviceReference == null || serviceReference.length() == 0) {
			throw new IllegalArgumentException("Invalid nodeService");
		}
		if (nodeOwner == null || nodeOwner.length() == 0) {
			throw new IllegalArgumentException("Invalid nodeOwner");
		}

		this.ruleId = ruleId;
		this.name = name;
		this.condition = condition;
		this.serviceReference = serviceReference;
		this.argument = argument;
		this.isPrivate = isPrivate;
		this.nodeOwner = nodeOwner;
	}

	// ==============================================================================

	/**
	 * Returns the id of the Rule, which is necessary when modifying the rule.
	 * <p>
	 * This is the path of the Rule on the tree. Config.Rule[ruleId]
	 * 
	 * @return the id of the rule
	 */
	public String getId() {
		return this.ruleId;
	}

	/**
	 * Returns the name of the Rule. Usefull for Gui.
	 * 
	 * @return the name of the rule
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the condition for this rule. The rule is activated
	 * 
	 * @return the condition of the rule
	 */
	public Condition getCondition() {
		return this.condition;
	}

	/**
	 * Returns the service reference.
	 * <p>
	 * This is the Id of the {@link NodeService} to triggered when the
	 * {@link Condition} is satisfied.
	 * 
	 * @return the service reference
	 */
	public String getServiceReference() {
		return this.serviceReference;
	}

	/**
	 * Returns the service arguments.
	 * <p>
	 * This argument will be passed to the {@link NodeService}.
	 * 
	 * @return the argument
	 */
	public String getArgument() {
		return this.argument;
	}

	/**
	 * Indicates if the {@link Rule} is private.
	 * 
	 * @return true is the {@link Rule} is private
	 */
	public boolean isPrivate() {
		return this.isPrivate;
	}

	/**
	 * Returns the node owner of this {@link Rule}.
	 * <p>
	 * This is the Id of the {@link Node} wich create the {@link Rule}.
	 * 
	 * @return the node owner
	 */
	public String getNodeOwner() {
		return this.nodeOwner;
	}

}
