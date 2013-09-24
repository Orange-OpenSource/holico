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

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.hlc.InvalidResourcePathException;
import com.francetelecom.rd.hlc.InvalidResourceTypeException;
import com.francetelecom.rd.hlc.Resource;
import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;
import com.francetelecom.rd.sds.Parameter;

public class ResourceImpl implements Resource {

	// ==============================================================================

	final Logger logger = LoggerFactory.getLogger(ResourceImpl.class.getName());

	private final Data rootData;

	// ==============================================================================

	public ResourceImpl(Data data) {
		this.rootData = data;
	}

	// ==============================================================================

	public String getName() {
		if (this.rootData != null) {
			return this.rootData.getName();
		}
		return null;
	}

	public String getPath() {
		if (this.rootData != null) {
			return this.rootData.getPathname();
		}
		return null;
	}

	public Resource getParent() {
		if (this.rootData != null) {
			return new ResourceImpl(this.rootData.getParent());
		}
		return null;
	}

	public String getParentPath() {
		if (this.rootData != null) {
			return this.rootData.getParent().getPathname();
		}
		return null;
	}

	public int getResourceType() {
		if (this.rootData != null) {
			return Tools.getConnectorTypeFromSdsType(this.rootData.getType());
		}
		return -1;
	}

	public boolean isSharedResource() {
		return (getResourceType() == TYPE_SHARED);
	}

	public boolean isMultiResource() {
		return (getResourceType() == TYPE_MULTI);
	}

	public boolean isValueResource() {
		return (getResourceType() == TYPE_VALUE_BOOL
				|| getResourceType() == TYPE_VALUE_INT || getResourceType() == TYPE_VALUE_STRING);
	}

	public Resource[] getSubResources() throws InvalidResourceTypeException {

		if (this.rootData != null) {

			Resource[] children = new Resource[0];

			if (isSharedResource() || isMultiResource()) {
				Directory resDir = (Directory) this.rootData;
				Data[] resChildren = resDir.getChildren();
				for (int i = 0; i < resChildren.length; i++) {
					Resource child = new ResourceImpl(resChildren[i]);

					// add the resource to the list of resources
					int length = children.length;
					Resource newChildren[] = new Resource[length + 1];
					System.arraycopy(children, 0, newChildren, 0, length);
					newChildren[length] = child;
					children = newChildren;
				}
			} else {
				logger.error("InvalidResourceTypeException : this resource ("
						+ getName() + ") with type : " + getResourceType()
						+ " can't have any child");
				throw new InvalidResourceTypeException(
						"InvalidResourceTypeException : this resource ("
								+ getName() + ") with type : "
								+ getResourceType() + " can't have any child");
			}
			return children;
		}
		return null;
	}

	public Resource getChildResourceForPath(String path)
			throws InvalidResourcePathException, InvalidResourceTypeException {

		if (this.rootData != null) {

			Resource child = null;

			if (isSharedResource() || isMultiResource()) {
				Directory resDir = (Directory) this.rootData;
				try {
					child = new ResourceImpl(resDir.getChild(path));
				} catch (DataAccessException e) {
					logger.error(
							"DataException : could not retrieve child with path : "
									+ path, e);
					throw new InvalidResourcePathException(
							"DataException : could not retrieve child with path : "
									+ path, e);
				}
			} else {
				logger.error("InvalidResourceTypeException : this resource ("
						+ getName() + ") with type : " + getResourceType()
						+ " can't have any child");
				throw new InvalidResourceTypeException(
						"InvalidResourceTypeException : this resource ("
								+ getName() + ") with type : "
								+ getResourceType() + " can't have any child");
			}

			return child;
		}
		return null;
	}

	public String getValueAsString() throws InvalidResourceTypeException {

		if (this.rootData != null) {

			String value = null;

			if (getResourceType() == TYPE_VALUE_STRING) {
				Parameter resParam = (Parameter) this.rootData;
				value = (String) resParam.getValue();
			} else {
				logger.error("InvalidResourceTypeException : this resource ("
						+ getName() + ") with type : " + getResourceType()
						+ " can't return a String value");
				throw new InvalidResourceTypeException(
						"InvalidResourceTypeException : this resource ("
								+ getName() + ") with type : "
								+ getResourceType()
								+ " can't return a String value");
			}

			return value;
		}
		return null;
	}

	public int getValueAsInt() throws InvalidResourceTypeException {

		if (this.rootData != null) {

			int value = 0;

			if (getResourceType() == TYPE_VALUE_INT) {
				Parameter resParam = (Parameter) this.rootData;
				value = ((Integer) resParam.getValue()).intValue();
			} else {
				logger.error("InvalidResourceTypeException : this resource ("
						+ getName() + ") with type : " + getResourceType()
						+ " can't return a int value");
				throw new InvalidResourceTypeException(
						"InvalidResourceTypeException : this resource ("
								+ getName() + ") with type : "
								+ getResourceType()
								+ " can't return a int value");
			}

			return value;
		}
		return -1;
	}

	public boolean getValueAsBoolean() throws InvalidResourceTypeException {

		if (this.rootData != null) {

			boolean value = false;

			if (getResourceType() == TYPE_VALUE_BOOL) {
				Parameter resParam = (Parameter) this.rootData;
				value = ((Boolean) resParam.getValue()).booleanValue();
			} else {
				logger.error("InvalidResourceTypeException : this resource ("
						+ getName() + ") with type : " + getResourceType()
						+ " can't return a boolean value");
				throw new InvalidResourceTypeException(
						"InvalidResourceTypeException : this resource ("
								+ getName() + ") with type : "
								+ getResourceType()
								+ " can't return a boolean value");
			}

			return value;
		}
		return false;
	}

	public Object getValue() throws InvalidResourceTypeException {

		if (this.rootData != null) {

			Object value = null;

			if (getResourceType() == TYPE_VALUE_BOOL
					|| getResourceType() == TYPE_VALUE_INT
					|| getResourceType() == TYPE_VALUE_STRING) {
				Parameter resParam = (Parameter) this.rootData;
				value = resParam.getValue();
			} else {
				logger.error("InvalidResourceTypeException : this resource ("
						+ getName() + ") with type : " + getResourceType()
						+ " can't return a value");
				throw new InvalidResourceTypeException(
						"InvalidResourceTypeException : this resource ("
								+ getName() + ") with type : "
								+ getResourceType() + " can't return a value");
			}

			return value;
		}
		return null;
	}
}
