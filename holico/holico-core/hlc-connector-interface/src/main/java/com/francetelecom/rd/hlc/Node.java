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
 * Interface for Node object.
 * <p>
 * The {@link Node} is intended to be used by application developers who wants
 * to connect and Interact with the <b>Home Bus</b>.
 * <p>
 * The Node object is only available to the application that creates it and
 * register it on the Bus and implements the services and publications provided
 * by this Node.
 * <p>
 * It extends {@link NodeInfo} in order to provide access to all properties of
 * the Node and adds action methods to manipulate the Node state and declare new
 * NodServices and ResourcePublications.
 * <p>
 * 
 * 
 * See {@link NodeService} {@link NodeInfo} {@link ResourcePublication}.
 * 
 * 
 * @author Pierre Rust (tksh1670)
 * 
 */
public interface Node extends NodeInfo {

	/**
	 * Publish the current Node on the Home Life Context tree.
	 * <p>
	 * Publishing a Node on the HLC means :
	 * <ul>
	 * <li>declare the {@link Node} (with its id, name, etc.,</li>
	 * <li>declare the {@link NodeService}s provided by this {@link Node},</li>
	 * <li>declare the {@link ResourcePublication}s for this {@link Node}.</li>
	 * </ul>
	 * 
	 * If the Node as already been published, this method does nothing (and
	 * returns false). Once the Node has been published, any modification (new
	 * service or publication) will be immediately and automatically published
	 * on the Hlc.
	 * <p>
	 * 
	 * It is a common case that the Node has not yet been published on the bus
	 * but it's declaration already exists on the Bus. For example, it happens
	 * every time a device reboots : during the reboot, the device is physically
	 * disconnected but the node declaration still lives in the shared data
	 * structure. When the device reconnects, it will re-declare it-self,
	 * resulting in the publication of a node already declared.
	 * <p>
	 * This situation is perfectly correct as long a the new Node publication is
	 * compatible with the already existing declaration. This mostly means that
	 * :
	 * <ul>
	 * <li>
	 * for a fixed <code>nodeId</code>, the <code>deviceId</code> cannot change
	 * across publications.</li>
	 * <li>Existing Service declaration must not be modified (but services may
	 * be removed or added)</li>
	 * <li>Existing publication declaration must not change(but publications may
	 * be removed or added)</li>
	 * </ul>
	 * 
	 * @return true if the Node has been published on the bus, false if it was
	 *         already published on the bus
	 * 
	 * @throws HomeBusException
	 *             if there was an error when publishing the service.
	 * 
	 * @throws InvalidResourceTypeException
	 *             if some publication could not be declared because the path
	 *             and type of the publication conflicts with the existing Home
	 *             Life Context Tree/s
	 */
	public boolean publishOnHomeBus() throws HomeBusException,
			InvalidResourceTypeException, InvalidResourcePathException;

	/**
	 * Indicates if the {@link Node} has already been published on the bus.
	 * 
	 * @return true is the {@link Node} has already been published, false
	 *         otherwise
	 */
	public boolean isPublishedOnHomeBus();

	/**
	 * Un-publish the {@link Node} from the bus.
	 * <p>
	 * Un-publishing means that :
	 * <ul>
	 * <li>The {@link Node} is marked as unavailable;</li>
	 * <li>The keepalive indicator is not updated anymore;</li>
	 * <li>Services offered by this Node are not available anymore;</li>
	 * <li>The definition of the Node is still available in the
	 * <code>Discovery</code> sub-tree;</li>
	 * </ul>
	 * <p>
	 * Un-publication <b>MUST</b> be performed for all <i>"clean shutdown"</i>
	 * of a device or application.
	 * 
	 * @return true if the un-publication was successful, false if the Node was
	 *         not published on the Bus (and thus could not be unpublished).
	 * @throws HomeBusException
	 */
	public boolean unPublishFromHomeBus() throws HomeBusException;

	/**
	 * 
	 * 
	 * @return
	 */
	public boolean removesFromHomeBus() throws HomeBusException;

	/**
	 * Change the node name.
	 * <p>
	 * If the Node has already been published on the bus (with
	 * {@link #publishOnHomeBus()}), the modification is automatically published
	 * on the bus.
	 * 
	 * @param name
	 *            the new name for the node, must be non-null and non-empty.
	 * 
	 * @return true if the operation was successful (i.e. the new NodeService is
	 *         published on the bus)
	 * @throws HomeBusException
	 *             if there was an error when renaming the Node. May only be
	 *             thrown if the Node is already published on the bus.
	 */
	public void setName(String name) throws HomeBusException;

	/**
	 * Change the name manufacturer name.
	 * <p>
	 * See {@link NodeInfo#getManufacturer()}
	 * <p>
	 * Can only be set if the Node has not been published yet (with
	 * {@link #publishOnHomeBus()}).
	 * 
	 * @param manufacturer
	 *            name of the manufacturer, must be non-null and non empty
	 * @throws HomeBusException
	 *             if the Node has already been published on the bus.
	 */
	public void setManufacturer(String manufacturer) throws HomeBusException;

	/**
	 * Change the version for this {@link Node}.
	 * <p>
	 * See {@link NodeInfo#getVersion()}
	 * <p>
	 * Can only be set if the Node has not been published yet (with
	 * {@link #publishOnHomeBus()}).
	 * 
	 * 
	 * @param version
	 *            version of the application, bundle or firmware, must be
	 *            non-null and non empty
	 * @throws HomeBusException
	 *             if the Node has already been published on the bus.
	 */
	public void setVersion(String version) throws HomeBusException;

	/**
	 * Dynamically add a new {@link NodeService} to this Node.
	 * <p>
	 * If the Node has already been published on the bus (with
	 * {@link #publishOnHomeBus()}), the new service is immediately declared on
	 * the bus. *
	 * 
	 * @param serviceId
	 *            the serviceId of this {@link NodeService}
	 * @param name
	 *            human string for this service, used in GUI
	 * @param isPrivate
	 *            indicate if the {@link NodeService} is private.
	 * @param nsCallback
	 *            The callback that will be run when the service is invoked
	 * 
	 * @return true if the operation was successful (i.e. the new NodeService is
	 *         published on the bus)
	 * @throws HomeBusException
	 */
	public boolean addNodeService(String serviceId, String name,
			boolean isPrivate, NodeServiceCallback nsCallback)
			throws HomeBusException;

	/**
	 * Dynamically removes a {@link NodeService} by its serviceId.
	 * <p>
	 * If the Node has already been published on the bus (with
	 * {@link #publishOnHomeBus()}), the service is immediately removed on the
	 * bus.
	 * 
	 * @param serviceId
	 *            the serviceId of the {@link NodeService} that should be
	 *            removed
	 * 
	 * @return true if the service has been is removed from the bus, false if it
	 *         was not yet published (the Node has not been published yet with
	 *         {@link #publishOnHomeBus()}).
	 * @throws HomeBusException
	 *             if there was an error when removing the service. May only be
	 *             thrown if the Node is already published on the bus.
	 */
	public boolean removeNodeService(String serviceId) throws HomeBusException;

	/**
	 * Declare a new {@link ResourcePublication} for this {@link Node}.
	 * <p>
	 * If the Node has already been published on the bus (with
	 * {@link #publishOnHomeBus()}), the new publication is immediately declared
	 * on the bus.
	 * <p>
	 * If a publication is already declared for this path, the call is ignored.
	 * 
	 * @see ResourcePublication and Resource.
	 * 
	 * @param resourcePath
	 *            the path of the resource published
	 * @param type
	 *            the type of data that will be published at this path
	 * 
	 * @return true if the publication is declared on the bus, false if it not
	 *         yet published (the Node has not been published yet with
	 *         {@link #publishOnHomeBus()}) or if if an publication already
	 *         existed for this path..
	 * @throws HomeBusException
	 *             if there was an error when declaring the publication. May
	 *             only be thrown if the Node is already published on the bus.
	 * @throws InvalidResourceTypeException
	 *             if the publication could not be declared because the path and
	 *             type of the publication conflicts with the existing Home Life
	 *             Context Tree or the existing declarations of this Node . This
	 *             Exception may be thrown if the {@link Node} has already been
	 *             published on the Bus or when calling
	 *             {@link #publishOnHomeBus()}
	 */

	public boolean addResourcePublication(String id, String resourcePath,
			int type) throws HomeBusException, InvalidResourceTypeException,
			InvalidResourcePathException, HomeBusException;

	/**
	 * Dynamically removes a {@link ResourcePublication} from the Node, by its
	 * index.
	 * <p>
	 * If the Node has already been published on the bus (with
	 * {@link #publishOnHomeBus()}), the publication is immediately removed from
	 * the bus.
	 * 
	 * 
	 * @param id
	 *            the id of this {@link ResourcePublication} (given by
	 *            {@link ResourcePublication#getId()}.
	 * 
	 * @return true if the publication has been removed, false if the Node is
	 *         not yet published on the bus or if no publication could be found
	 *         for this index.
	 * 
	 * @throws HomeBusException
	 *             if there was an error when removing the publication. May only
	 *             be thrown if the Node is already published on the bus.
	 */

	public boolean removeResourcePublicationById(String id)
			throws HomeBusException;

	/**
	 * Dynamically removes a {@link ResourcePublication} from the Node, by its
	 * resourcePath.
	 * <p>
	 * If the Node has already been published on the bus (with
	 * {@link #publishOnHomeBus()}), the publication is immediately removed from
	 * the bus.
	 * 
	 * 
	 * @param resourcePath
	 *            the resourcePath of this {@link ResourcePublication} (given by
	 *            {@link ResourcePublication#getResourcePath()}.
	 * 
	 * @return true if the publication has been removed, false if the Node is
	 *         not yet published on the bus or if no publication could be found
	 *         for this resourcePath.
	 * 
	 * @throws HomeBusException
	 *             if there was an error when removing the publication. May only
	 *             be thrown if the Node is already published on the bus.
	 */
	public boolean removeResourcePublicationByPath(String resourcePath)
			throws HomeBusException;

	/**
	 * Publish a value on a {@link Resource} identified by it's
	 * <code>resourcePath</code>.
	 * <p>
	 * The publication is allowed only if a {@link ResourcePublication} has been
	 * declared for this <code>resourcePath</code> (and this type of value) and
	 * the {@link Node} has already been published on the bus (this
	 * {@link #publishOnHomeBus()}).
	 * 
	 * @param resourcepath
	 *            the resourcePath of the {@link Resource} where this value must
	 *            be published on.
	 * @param value
	 *            the value to publish
	 * 
	 * @throws HomeBusException
	 *             if there was an error when publishing on the {@link Resource}
	 *             or if the Node is not yet published on the bus.
	 * @throws InvalidResourcePathException
	 *             if the <code>resourcePath</code> does not match one of the
	 *             declared publications.
	 * @throws InvalidResourceTypeException
	 *             if the value type does not match the type of the resource for
	 *             the given resourcePath and it could not be converted
	 *             automatically.
	 */
	public void publishOnResource(String resourcePath, Object value)
			throws HomeBusException, InvalidResourceTypeException,
			InvalidResourcePathException;

	/**
	 * Get the HlcConnector, providing access to all data available on the bus.
	 * <p>
	 * As only bus participants can access bus data, the {@link HlcConnector} is
	 * only available on a published Node. Calling this method without calling
	 * (successfully) {@link #publishOnHomeBus()} will raise an error.
	 * 
	 * 
	 * @return the HlcConnector
	 */
	public HlcConnector getHlcConnector() throws HomeBusException;

	/**
	 * Get a {@link NodeServiceCallback} of a {@link NodeService} identified by
	 * it's <code>serviceId</code>.
	 * 
	 * 
	 * @return the NodeServiceCallback
	 */
	public NodeServiceCallback getServiceCallback(String serviceId)
			throws IllegalArgumentException;
}
