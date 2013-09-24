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
 * Rules are used to configure behavior and interaction of various Node on the
 * bus. A Rule bind a {@link NodeService} to a {@link Condition}. Once a rule is
 * set, the Resource specified in the {@link Condition} is automatically
 * monitored and the condition is evaluated for every change on the resource.
 * When the condition is satisfied, the {@link NodeService} is trigerred.
 * 
 * 
 * @author Pierre Rust (tksh1670)
 * 
 */
public interface Rule {

	/**
	 * Returns the id of the Rule, which is necessary when modifying the rule.
	 * <p>
	 * This is the path of the Rule on the tree. Config.Rule[ruleId]
	 * 
	 * @return the id of the rule
	 */
	public String getId();

	/**
	 * Returns the name of the Rule. Usefull for Gui.
	 * 
	 * @return the name of the rule
	 */
	public String getName();

	/**
	 * Returns the condition for this rule. The rule is activated
	 * 
	 * @return the condition of the rule
	 */
	public Condition getCondition();

	/**
	 * Returns the service reference.
	 * <p>
	 * This is the Id of the {@link NodeService} to triggered when the
	 * {@link Condition} is satisfied.
	 * 
	 * @return the service reference
	 */
	public String getServiceReference();

	/**
	 * Returns the service arguments.
	 * <p>
	 * This argument will be passed to the {@link NodeService}.
	 * 
	 * @return the argument
	 */
	public String getArgument();

	/**
	 * Indicates if the {@link Rule} is private.
	 * 
	 * @return true is the {@link Rule} is private
	 */
	public boolean isPrivate();

	/**
	 * Returns the node owner of this {@link Rule}.
	 * <p>
	 * This is the Id of the {@link Node} wich create the {@link Rule}.
	 * 
	 * @return the node owner
	 */
	public String getNodeOwner();

}
