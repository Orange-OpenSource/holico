/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.sds.sds-impl
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
package com.francetelecom.rd.sds.communicationlayer.impl;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;

import ch.ethz.inf.vs.californium.coap.Message.messageType;
import ch.ethz.inf.vs.californium.coap.registries.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.PUTRequest;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.endpoint.ServerEndpoint;
import ch.ethz.inf.vs.californium.endpoint.resources.LocalResource;
import ch.ethz.inf.vs.californium.util.Properties;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.sds.communicationlayer.DataChannel;
import com.francetelecom.rd.sds.communicationlayer.DataChannelFactory;
import com.francetelecom.rd.sds.communicationlayer.DataReceiver;
import com.google.common.base.Stopwatch;

/**
 * @author GOUL5436
 *
 */
public class DataChannelCoap extends ServerEndpoint implements DataChannel
{
   // ---------------------------------------------------------------------
   // init logger

   private static final Logger logger = LoggerFactory
         .getLogger(DataChannelCoap.class.getName());

   // ---------------------------------------------------------------------

   public static URI SDS_URI = null;
   public static final String IP_MULTICAST_FOR_SYNCHRO = "224.0.0.251";
   public static int PORT_FOR_SYNCHRO = 6790;

   public static final int MAX_BYTES = Properties.std.getInt("RX_BUFFER_SIZE")-1; // possible temporarily max size : 65508, otherwise 4096

   static
   {
      int port = Properties.std.getInt("SYNCHRO_PORT");
      if (port > 0)
      {
         PORT_FOR_SYNCHRO = port;
      }
   }

   private DataReceiver receiver = null;

   private boolean alive = false;

   // ---------------------------------------------------------------------

   /*
    * Constructor for a new SDS server. Here, the resources
    * of the server are initialized.
    */
   public DataChannelCoap() throws SocketException
   {	   	  
      super(IP_MULTICAST_FOR_SYNCHRO, PORT_FOR_SYNCHRO);

      logger.debug("SDS resource added to the coap Endpoint");
      logger.info("SDS coap multicast ip for synchro : " + IP_MULTICAST_FOR_SYNCHRO);
      logger.info("SDS coap port for synchro : " + PORT_FOR_SYNCHRO);
      logger.info("SDS coap max bytes/message : " + MAX_BYTES);

      try
      {
         SDS_URI = new URI("coap://" + IP_MULTICAST_FOR_SYNCHRO + ":" + PORT_FOR_SYNCHRO + "/sds");
         logger.info("SDS coap uri : " + SDS_URI);
      }
      catch (URISyntaxException e)
      {
         logger.error("SDS coap uri creation error : " + e.getMessage());
         e.printStackTrace();
      }

      // provide an instance of a SDS resource
      // this resource handles all the synchronization requests and responses
      // in the future this might be managed using direct coap messages
      // and not request/response mecanism ?
      addResource(new SdsResource());
      logger.debug("SDS resource added to the coap Endpoint");

      // this resource is used for the REST representation of the sds tree
      // it maintains a tree of sub-resources corresponding to all Dierctory
      // Parameters in the sds tree.
      addResource(new SdsTreeResource());
      logger.debug("SDS tree resource added to the coap Endpoint");

      logger.info("SDS coap data channel created");
   }

   /* (non-Javadoc)
    * @see com.francetelecom.rd.sds.communicationlayer.DataChannel#registerReceiver(com.francetelecom.rd.sds.communicationlayer.DataReceiver)
    */
   public void registerReceiver(DataReceiver receiver)
   {

      this.receiver = receiver;
      logger.debug("Receiver registered : " + receiver.toString());

      if (!alive)
      {
         alive = true;
         start();
         logger.debug("Coap Endpoint started");
      }
   }

   /* (non-Javadoc)
    * @see com.francetelecom.rd.sds.communicationlayer.DataChannel#send(byte[])
    */
   public void send(byte[] data)
   {
      // create new request
      Request request = new PUTRequest();
      // specify URI of target endpoint
      request.setURI(SDS_URI);
      request.setType(messageType.NON);
      request.setPayload(data);
      try
      {
         request.execute();					
      }
      catch (IOException e)
      {
         logger.error("Coap request execution failed : " + e.getMessage());
         e.printStackTrace();
      }
   }

   /* (non-Javadoc)
    * @see com.francetelecom.rd.sds.communicationlayer.DataChannel#getMaxBytes()
    */
   public int getMaxBytes()
   {
      return MAX_BYTES;
   }


   public void close()
   {
      alive = false;

      logger.debug("Coap data channel closed");
   }

   // ---------------------------------------------------------------------

   /*
    * Definition of the Hello-World Resource
    */
   class SdsResource extends LocalResource
   {
      public SdsResource()
      {
         // set resource identifier
         super("sds");

         // set display name
         setTitle("SDS Resource");

         logger.debug("SDS resource created");
      }

      public void performPUT(PUTRequest request)
      {
         logger.debug("SDS resource received a PUT request");

         receiver.dataReceived(request.getPayload());

         // respond to the request
         request.respond(CodeRegistry.RESP_CHANGED);
      }

      public void performGET(GETRequest request)
      {					
         // respond to the request
         String pathname = request.getUriQuery().substring(1);

         logger.debug("SDS resource received a GET request for : " + pathname);

         String value = null;
         String response = null;
         int k = pathname.indexOf('=');
         if (k != -1)
         {
            value = pathname.substring(k+1);
            pathname = pathname.substring(0, k);
         }
         if (value == null)
         {
            response = "=> " + pathname + "=" + receiver.getValue(pathname);

         }
         else
         {
            response = "=> SetValue " + (receiver.setValue(pathname, value) ? "OK" : "KO");
         }

         logger.debug("SDS GET request response : " + response);			
         request.respond(CodeRegistry.RESP_CONTENT, response);
      }
   }
}
