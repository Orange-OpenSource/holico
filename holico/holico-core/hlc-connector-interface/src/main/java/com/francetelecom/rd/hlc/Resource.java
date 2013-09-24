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
 * 
 * @author Pierre Rust (tksh1670)
 * 
 */
public interface Resource {

	public static final int TYPE_SHARED = 0;
	public static final int TYPE_MULTI = 1;
	public static final int TYPE_VALUE_INT = 2;
	public static final int TYPE_VALUE_BOOL = 3;
	public static final int TYPE_VALUE_STRING = 4;

	/**
	 * 
	 * @return the name of this {@link Resource}
	 */
	public String getName();

	/**
	 * 
	 * @return the path of this {@link Resource}.
	 */
	public String getPath();

	/**
	 * 
	 * @return the parent {@link Resource}.
	 */
	public Resource getParent();

	/**
	 * 
	 * @return the path of the parent {@link Resource}.
	 */
	public String getParentPath();

	/**
	 * The type of the resource is one of the <code>TYPE_[xxx]</code> constants
	 * defines in the {@link Resource} class.
	 * <p>
	 * 
	 * @return the type for this Resource
	 */
	public int getResourceType();

	/**
	 * Indicates if this {@link Resource} is a <i>shared</i> resource.
	 * <p>
	 * 
	 * @return true if the {@link Resource} is a shared {@link Resource}.
	 */
	public boolean isSharedResource();

	/**
	 * Indicates if this {@link Resource} is a <i>multi</i> resource.
	 * <p>
	 * 
	 * @return true if the {@link Resource} is a <code>MULTI</code>
	 *         {@link Resource}.
	 */
	public boolean isMultiResource();

	/**
	 * Indicates if this resource has one of the <code>TYPE_VALUE_[xxx]</code>
	 * types
	 * <p>
	 * 
	 * @return true if the {@link Resource} is a Value Resource
	 */
	public boolean isValueResource();

	/**
	 * Return the sub-resources for this resource.
	 * <p>
	 * This method only make sense if the Resource has the SHARED or MULTI. If
	 * it has one of the <code>TYPE_VALUE_[xxx]</code> types, it throws an
	 * {@link InvalidResourceTypeException}.
	 * 
	 * @return the sub-Recources
	 * @throws InvalidResourceTypeException
	 *             if the Resource is not a <i>shared</i> or a <i>multi</i>
	 *             resource.
	 */
	public Resource[] getSubResources() throws InvalidResourceTypeException;

	/**
	 * 
	 * @param path
	 * @return
	 * @throws InvalidResourcePathException
	 * @throws InvalidResourceTypeException
	 */
	public Resource getChildResourceForPath(String path)
			throws InvalidResourcePathException, InvalidResourceTypeException;

	/**
	 * Return the value of this {@link Resource}.
	 * <p>
	 * This method should only be used if the {@link Resource} has one of the
	 * TYPE_VALUE_[xx]. type. In this case the value is converted to
	 * {@link String} and returned. Otherwise, an
	 * {@link InvalidResourceTypeException} is thrown.
	 * 
	 * @return the value of this {@link Resource}, or a {@link String}
	 *         conversion of this value.
	 * @throws InvalidResourceTypeException
	 */
	public String getValueAsString() throws InvalidResourceTypeException;

	/**
	 * Return the value of this {@link Resource}.
	 * <p>
	 * This method should only be used if the {@link Resource} has the type
	 * {@link Resource#TYPE_VALUE_INT}. If the {@link Resource} has any other
	 * type, this method throws an {@link InvalidResourceTypeException}.
	 * 
	 * @return the value of this {@link Resource}.
	 * @throws InvalidResourceTypeException
	 */
	public int getValueAsInt() throws InvalidResourceTypeException;

	/**
	 * Return the value of this {@link Resource}.
	 * <p>
	 * This method should only be used if the {@link Resource} has the type
	 * {@link Resource#TYPE_VALUE_BOOL}. If the {@link Resource} has any other
	 * type, this method will throws {@link InvalidResourceTypeException}.
	 * 
	 * @return the value of this {@link Resource}.
	 * @throws InvalidResourceTypeException
	 *             if the resource does not have the
	 *             {@link Resource#TYPE_VALUE_BOOL} type
	 */
	public boolean getValueAsBoolean() throws InvalidResourceTypeException;

	/**
	 * Return the value of this {@link Resource}.
	 * <p>
	 * This method should only be used if the {@link Resource} has one of the
	 * TYPE_VALUE_[xx]. type. Otherwise, an {@link InvalidResourceTypeException}
	 * is thrown.
	 * 
	 * @return the value of this {@link Resource}.
	 * @throws InvalidResourceTypeException
	 */
	public Object getValue() throws InvalidResourceTypeException;

}
