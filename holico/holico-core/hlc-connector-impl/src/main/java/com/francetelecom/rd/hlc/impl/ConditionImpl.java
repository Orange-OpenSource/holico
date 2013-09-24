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
import com.francetelecom.rd.hlc.HomeBusException;
import com.francetelecom.rd.hlc.InvalidResourceTypeException;
import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;
import com.francetelecom.rd.sds.Parameter;

public class ConditionImpl implements Condition {

	// ==============================================================================

	private final Directory hsRoot;
	private final int operator;
	private final int targetType;;
	private final String sTargetValue;
	private final int iTargetValue;
	private final boolean bTargetValue;
	private final String resourcePath;

	// ==============================================================================

	public ConditionImpl(Directory hsRoot, final int operator,
			final String sTargetValue, final String resourcePath) {

		if (hsRoot == null) {
			throw new IllegalArgumentException("Invalid hsRoot ");
		}
		if (operator != OPERATOR_DIFF && operator != OPERATOR_EQUAL
				&& operator != OPERATOR_INF && operator != OPERATOR_INFEQUAL
				&& operator != OPERATOR_SUP && operator != OPERATOR_SUPEQUAL) {
			throw new IllegalArgumentException("Invalid operator ");
		}
		if (sTargetValue == null || sTargetValue.length() == 0) {
			throw new IllegalArgumentException("Invalid sTargetValue ");
		}
		if (resourcePath == null || resourcePath.length() == 0) {
			throw new IllegalArgumentException("Invalid resourcePath ");
		}

		this.hsRoot = hsRoot;
		this.operator = operator;
		this.targetType = Data.TYPE_STRING;
		this.sTargetValue = sTargetValue;
		this.iTargetValue = -1;
		this.bTargetValue = false;
		this.resourcePath = resourcePath;
	}

	public ConditionImpl(Directory hsRoot, final int operator,
			final int iTargetValue, final String resourcePath) {

		if (hsRoot == null) {
			throw new IllegalArgumentException("Invalid hsRoot ");
		}
		if (operator != OPERATOR_DIFF && operator != OPERATOR_EQUAL
				&& operator != OPERATOR_INF && operator != OPERATOR_INFEQUAL
				&& operator != OPERATOR_SUP && operator != OPERATOR_SUPEQUAL) {
			throw new IllegalArgumentException("Invalid operator ");
		}
		if (resourcePath == null || resourcePath.length() == 0) {
			throw new IllegalArgumentException("Invalid resourcePath ");
		}

		this.hsRoot = hsRoot;
		this.operator = operator;
		this.targetType = Data.TYPE_INT;
		this.sTargetValue = "";
		this.iTargetValue = iTargetValue;
		this.bTargetValue = false;
		this.resourcePath = resourcePath;
	}

	public ConditionImpl(Directory hsRoot, final int operator,
			final boolean bTargetValue, final String resourcePath) {

		if (hsRoot == null) {
			throw new IllegalArgumentException("Invalid hsRoot ");
		}
		if (operator != OPERATOR_DIFF && operator != OPERATOR_EQUAL
				&& operator != OPERATOR_INF && operator != OPERATOR_INFEQUAL
				&& operator != OPERATOR_SUP && operator != OPERATOR_SUPEQUAL) {
			throw new IllegalArgumentException("Invalid operator " + operator + 
					" for resource path " + resourcePath);
		}
		if (resourcePath == null || resourcePath.length() == 0) {
			throw new IllegalArgumentException("Invalid resourcePath ");
		}

		this.hsRoot = hsRoot;
		this.operator = operator;
		this.targetType = Data.TYPE_BOOL;
		this.sTargetValue = "";
		this.iTargetValue = -1;
		this.bTargetValue = bTargetValue;
		this.resourcePath = resourcePath;
	}

	// ==============================================================================

	public int getOperator() {
		return this.operator;
	}

	public int getTargetValueType() {
		return this.targetType;
	}

	public String getTargetStringValue() {
		return this.sTargetValue;
	}

	public int getTargetIntValue() {
		return this.iTargetValue;
	}

	public boolean getTargetBooleanValue() {
		return this.bTargetValue;
	}

	public String getResourcePath() {
		return this.resourcePath;
	}

	public boolean isSatisfied() throws HomeBusException,
			InvalidResourceTypeException {

		try {
			String resourceFullpath = HomeBusPathDefinitions.HLC + "."
					+ this.resourcePath;
			Parameter param = hsRoot.getParameter(resourceFullpath);

			if (param == null || param.getValue() == null) {
				// invalide parameter => condition could not be satisfied
				return false;
			}
			if (param.getType() != this.targetType) {
				throw new InvalidResourceTypeException(
						"Illegal type comparaison for resource : ("
								+ param.getType() + ") with target ("
								+ this.targetType + ")");
			}

			switch (param.getType()) {
			case Data.TYPE_BOOL:
				boolean bValue = ((Boolean) param.getValue()).booleanValue();
				switch (this.operator) {
				case OPERATOR_EQUAL:
					return (bValue == bTargetValue);
				case OPERATOR_DIFF:
					return (bValue != bTargetValue);
				case OPERATOR_SUP:
				case OPERATOR_INF:
				case OPERATOR_SUPEQUAL:
				case OPERATOR_INFEQUAL:
					// invalide operator => condition could not be satisfied
					return false;
				}
			case Data.TYPE_INT:
				int iValue = ((Integer) param.getValue()).intValue();
				switch (this.operator) {
				case OPERATOR_EQUAL:
					return (iValue == iTargetValue);
				case OPERATOR_SUP:
					return (iValue > iTargetValue);
				case OPERATOR_INF:
					return (iValue < iTargetValue);
				case OPERATOR_SUPEQUAL:
					return (iValue >= iTargetValue);
				case OPERATOR_INFEQUAL:
					return (iValue <= iTargetValue);
				case OPERATOR_DIFF:
					return (iValue != iTargetValue);
				}
			case Data.TYPE_STRING:
				String sValue = (String) param.getValue();
				switch (this.operator) {
				case OPERATOR_EQUAL:
					return (sValue.equals(sTargetValue));
				case OPERATOR_DIFF:
					return (!sValue.equals(sTargetValue));
				case OPERATOR_SUP:
				case OPERATOR_INF:
				case OPERATOR_SUPEQUAL:
				case OPERATOR_INFEQUAL:
					// invalide operator => condition could not be satisfied
					return false;
				}
			}
			return true;
		} catch (DataAccessException e) {
			throw new HomeBusException("Cannot access resource");
		}
	}

}
