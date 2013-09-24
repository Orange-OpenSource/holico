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
 * 
 * Conditions are used for {@link Rule} definition : they express a conditional
 * statement based on the value of a {@link Resource} in the HomeLifeContext
 * Tree.
 * <p>
 * A Rule is made of :
 * <ul>
 * <li>an operator (<,>,==, != , ...)</li>
 * <li>a ResourcePath, which identifies the resource (in the HLC tree) that will
 * be used in the comparison</li>
 * <li>a target value, which is compared to the value identified by the
 * ResourcePath</li>
 * </ul>
 * 
 * 
 * @author Pierre Rust (tksh1670)
 * 
 */
public interface Condition {

	public static final int OPERATOR_EQUAL = 0;
	public static final int OPERATOR_SUP = 1;
	public static final int OPERATOR_INF = 2;
	public static final int OPERATOR_SUPEQUAL = 3;
	public static final int OPERATOR_INFEQUAL = 4;
	public static final int OPERATOR_DIFF = 5;

	/**
	 * Returns the operator for the condition. Always equal to one of the
	 * OPERATOR_xxx constant.
	 * 
	 * @return The operator for the condition
	 */
	public int getOperator();

	/**
	 * Returns the type of the target value.
	 * 
	 * @return the target value type for this condition
	 */
	public int getTargetValueType();

	/**
	 * Returns the target value as String.
	 * 
	 * @return the target value for this condition as String
	 */
	public String getTargetStringValue();

	/**
	 * Returns the target value as int.
	 * 
	 * @return the target value for this condition as int
	 */
	public int getTargetIntValue();

	/**
	 * Returns the target value as boolean.
	 * 
	 * @return the target value for this condition as boolean
	 */
	public boolean getTargetBooleanValue();

	/**
	 * Returns the resource path.
	 * 
	 * @return the ResourcePath, which identifies the resource (in the HLC tree)
	 *         that will be used in the comparison
	 */
	public String getResourcePath();

	/**
	 * Indicates if the {@link Condition} is satisfied.
	 * <p>
	 * If the resource does not exist, the condition is not yet active. It
	 * always returns false as the resource will not be published
	 * <p>
	 * If the type of the target value is different from the type of the
	 * resource, this method throws an InvalidResourceTypeException exception
	 * 
	 * @return true is the {@link Condition} is satisfied
	 * 
	 * @throws HomeBusException
	 *             if there was an error when accessing to the resource.
	 * 
	 * @throws InvalidResourceTypeException
	 *             if the type of the resource and the targetValue type of the
	 *             condition are different.
	 */
	boolean isSatisfied() throws HomeBusException, InvalidResourceTypeException;

}
