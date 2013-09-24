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

import com.francetelecom.rd.hlc.InvalidResourcePathException;
import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;

public class HomeBusPathDefinitions {

	// ==============================================================================

	static final String HLC = "HomeLifeContext";

	// ==============================================================================

	static final String DISCOVERY = "Discovery";

	static final String NODE = "Node";
	static final String NODE_ID = "nodeId";
	static final String NODE_NAME = "name";
	static final String NODE_MANUFACTURER = "manufacturer";
	static final String NODE_VERSION = "version";
	static final String NODE_DEVICEID = "deviceId";
	static final String NODE_KEEPALIVE = "keepAlive";
	static final String NODE_AVAILABILITY = "Availability";
	static final Integer NODE_AVAILABILITY_AVAILABLE = 1;
	static final Integer NODE_AVAILABILITY_NOTAVAILABLE = 0;

	static final String SERVICE = "Service";
	static final String SERVICE_ID = "serviceId";
	static final String SERVICE_NAME = "name";
	static final String SERVICE_PARAMETERNAME = "parameterName";
	static final String SERVICE_PARAMETERTYPE = "parameterType";
	static final String SERVICE_PERMISSION = "Permission";
	static final String SERVICE_PERMISSION_ISPRIVATE = "isPrivate";

	static final String PUBLICATION = "Publication";
	static final String PUBLICATION_ID = "publicationId";
	static final String PUBLICATION_REFRENCE = "reference";
	static final String PUBLICATION_TYPE = "type";

	// ==============================================================================

	static final String CONFIG = "Config";

	static final String RULE = "Rule";
	static final String RULE_ID = "ruleId";
	static final String RULE_NAME = "name";
	static final String RULE_PERMISSION = "Permission";
	static final String RULE_PERMISSION_ISPRIVATE = "isPrivate";
	static final String RULE_PERMISSION_OWNER = "owner";
	static final String RULE_CONDITION = "Condition";
	static final String RULE_CONDITION_RESOURCE = "resource";
	static final String RULE_CONDITION_OPERATOR = "operator";
	static final String RULE_CONDITION_TARGETVALUE = "targetValue";
	static final String RULE_SERVICE = "Service";
	static final String RULE_SERVICE_REFERENCE = "reference";
	static final String RULE_SERVICE_ARGUMENT = "argument";

	// ==============================================================================

	
}
