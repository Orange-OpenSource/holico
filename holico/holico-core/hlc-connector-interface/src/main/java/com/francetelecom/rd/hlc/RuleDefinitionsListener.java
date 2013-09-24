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
 * This listener interface is ued to be notified on any Rule Change.
 * It must be registered on the {@link HlcConnector} using 
 * {@link HlcConnector#addRuleDefinitionsListener(RuleDefinitionsListener)}. 
 * 
 * 
 * 
 * @author Pierre Rust (tksh1670)
 *
 */
public interface RuleDefinitionsListener {

	
	/**
	 * This method is called for every new {@link Rule} creation.
	 * 
	 * @param ruleId the id of the {@link Rule} that have been created.
	 */
	public void onRuleAdded(String ruleId);
	
	/**
	 * This method is called for every {@link Rule} modification.
	 * 
	 * @param ruleId the id of the {@link Rule} that have been changed.
	 */
	public void onRuleChanged(String ruleId);
	
	/**
	 * This method is called for every {@link Rule} suppression.
	 * 
	 * @param ruleId the id of the {@link Rule} that have been suppressed.
	 */	
	public void onRuleRemoved(String ruleId);
	
	
}
