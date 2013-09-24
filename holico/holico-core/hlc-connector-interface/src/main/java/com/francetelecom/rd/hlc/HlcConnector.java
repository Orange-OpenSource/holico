/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.holico.hlc-connector-interface
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
package com.francetelecom.rd.hlc;

/**
 * The HlcConnector provide access to all information about the current
 * HomeLifeContext.
 * 
 * 
 * @author Pierre Rust (tksh1670)
 * 
 */
public interface HlcConnector {

	/**
	 * Returns a {@link NodeInfo} list for all {@link Node} currently registered
	 * on the HLC.
	 * 
	 * @param includesDisconnectedNodes
	 *            indicates if disconnected Nodes must be returned
	 * 
	 * @return the list of {@link NodeInfo}
	 * 
	 * @throws HomeBusException
	 *             if there was an error when accessing to nodes in
	 *             HomeLifeContext tree.
	 */
	public NodeInfo[] getAllNodes(boolean includesDisconnectedNodes)
			throws HomeBusException;

	/**
	 * Returns {@link NodeInfo} for the {@link Node} corresponding to the given
	 * <code>nodeId<code>.
	 * 
	 * @return the {@link NodeInfo}
	 * 
	 * @throws HomeBusException
	 *             if there was an error when accessing to nodeInfo in
	 *             HomeLifeContext tree for the desired <code>nodeId<code> (no
	 *             {@link Node} with this id of node info missing).
	 * 
	 * @throws IllegalArgumentException
	 *             if the <code>nodeId<code> parameter is not valid
	 */
	public NodeInfo getNode(String nodeId) throws HomeBusException,
			IllegalArgumentException;

	/**
	 * Add a {@link NodeDiscoveryListener}. This listener will be notified for
	 * every Node arrival, removal and modification.
	 * 
	 * @param listener
	 */
	public void addNodeDiscoveryListener(NodeDiscoveryListener listener);

	/**
	 * Returns the list of all {@link Rule}s.
	 * 
	 * @return all {@link Rule}s currently defined
	 * 
	 * @throws HomeBusException
	 *             if there was an error when accessing to rules in
	 *             HomeLifeContext tree.
	 */
	public Rule[] getAllRules() throws HomeBusException;

	/**
	 * Return the {@link Rule} corresponding to the given <code>ruleId</code>.
	 * 
	 * @return the {@link Rule}
	 * 
	 * @throws HomeBusException
	 *             if there was an error when accessing to rule in
	 *             HomeLifeContext tree for the desired
	 *             <code>ruleId<code> (no {@link Rule} with
	 *            this id of node info missing).
	 * 
	 * @throws IllegalArgumentException
	 *             if the <code>ruleId<code> parameter is not valid
	 */
	public Rule getRule(String ruleId) throws HomeBusException,
			IllegalArgumentException;

	/**
	 * Add a {@link RuleDefinitionsListener}. This listener will be notified for
	 * every creation, suppression and modification of the rules.
	 * 
	 * @param listener
	 *            the RuleDefinitionsListener
	 * 
	 * @return true if the opertaion was successfull
	 */
	public void addRuleDefinitionsListener(RuleDefinitionsListener listener);

	/**
	 * Add a Rule
	 * 
	 * @param rule
	 *            the rule to add
	 * 
	 * @return the id of the new rule
	 * 
	 * @throws IllegalArgumentException
	 *             if the
	 *             <code>rule<code> parameter is not valid (null or empty id)
	 *             or if the HomeLifeContext tree already contains a rule with the same id
	 * 
	 * @throws HomeBusException
	 *             if there was an error when adding the rule in HomeLifeContext
	 *             tree
	 */
	public String addRule(Rule rule) throws IllegalArgumentException,
			HomeBusException;

	/**
	 * Update a Rule
	 * 
	 * @param rule
	 *            the rule to update
	 * 
	 * @return the id of the updated rule
	 * 
	 * @throws IllegalArgumentException
	 *             if the
	 *             <code>rule<code> parameter is not valid (null or empty id)
	 *             or if the HomeLifeContext does not contain a rule with the same id
	 *             or if the rule's node owner is different than  the previous one
	 * 
	 * @throws HomeBusException
	 *             if there was an error when updating the rule in
	 *             HomeLifeContext tree
	 */
	public String updateRule(Rule rule) throws IllegalArgumentException,
			HomeBusException;

	/**
	 * Remove a Rule
	 * 
	 * @param ruleId
	 *            of the rule to remove
	 * 
	 * @throws IllegalArgumentException
	 *             if the
	 *             <code>ruleId<code> parameter is not valid (null or empty)
	 *             or if the HomeLifeContext does not contain a rule with the same id
	 *             or if the node does not have the right to remove the rule 
	 *             (because rule is private and the node that wants to remove it is not the rule's owner)
	 * 
	 * @throws HomeBusException
	 *             if there was an error when removing the rule in
	 *             HomeLifeContext tree
	 */
	public void removeRule(String ruleId)throws IllegalArgumentException, 
			HomeBusException;
	
	/**
	 * Returns the root of the Home Life Context.
	 * 
	 * @return the root
	 */
	public Resource getHomeLifeContextRoot();
	
	public NodeService getNodeService(String serviceId) throws HomeBusException,
	IllegalArgumentException;
	
	public NodeInfo getServiceOwner(String serviceId)throws HomeBusException,
	IllegalArgumentException;

}
