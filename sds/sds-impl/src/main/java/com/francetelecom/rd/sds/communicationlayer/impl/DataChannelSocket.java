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
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.sds.communicationlayer.DataChannel;
import com.francetelecom.rd.sds.communicationlayer.DataReceiver;

/**
 * @author GOUL5436
 *
 */
public class DataChannelSocket extends Thread implements DataChannel
{
   // ---------------------------------------------------------------------
   // init logger

   private static final Logger logger = LoggerFactory
         .getLogger(DataChannelSocket.class.getName());

   // ---------------------------------------------------------------------

   public static final String IP_MULTICAST_FOR_SYNCHRO = "224.0.0.251";
   public static final int PORT_FOR_SYNCHRO = 6798;

   public static final int MAX_BYTES = 32000; // possible temporarily max size : 65508, target 4096

   private DataReceiver receiver = null;

   private InetAddress group = null;

   // Create a socket to listen on the port and join the group.
   private MulticastSocket socket = null;

   private boolean alive = false;

   // ---------------------------------------------------------------------

   /**
    * 
    */
   public DataChannelSocket()
   {
      logger.info("SDS socket multicast ip for synchro : " + IP_MULTICAST_FOR_SYNCHRO);
      logger.info("SDS socket port for synchro : " + PORT_FOR_SYNCHRO);
      logger.info("SDS socket max bytes/message : " + MAX_BYTES);

      _init();

      logger.info("SDS socket data channel created");
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
         logger.debug("Socket thread started");
      }
   }

   /* (non-Javadoc)
    * @see com.francetelecom.rd.sds.communicationlayer.DataChannel#send(byte[])
    */
   public void send(byte[] data)
   {
      DatagramPacket hi = new DatagramPacket(data, data.length, group, PORT_FOR_SYNCHRO);
      try
      {
         socket.send(hi);
      }
      catch (IOException e)
      {
         logger.error("Data writing on socket failed : " + e.getMessage());
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
      try
      {
         join();
         _dispose();
         logger.debug("Socket data channel closed");
      }
      catch (InterruptedException e)
      {
         logger.error("Socket data channel closure failed : " + e.getMessage());
         e.printStackTrace();
      }
   }

   public void run()
   {
      try
      {
         // Create a buffer to read datagrams into. If a
         // packet is larger than this buffer, the
         // excess will simply be discarded!
         byte[] buffer = new byte[MAX_BYTES];

         // Create a packet to receive data into the buffer
         DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

         // Now loop forever, waiting to receive packets and printing them.
         while (alive)
         {
            // Wait to receive a datagram
            socket.receive(packet);

            logger.debug("A datagram arrived on the Socket");

            receiver.dataReceived(buffer);

            // Reset the length of the packet before reusing it.
            packet.setLength(buffer.length);
         }
      }
      catch (IOException e)
      {
         logger.error("Socket data channel error : " + e.getMessage());
         e.printStackTrace();
      }
   }

   // ---------------------------------------------------------------------

   private void _init()
   {
      try
      {
         group = InetAddress.getByName(IP_MULTICAST_FOR_SYNCHRO);

         // Create a socket to listen on the port and join the group.
         socket = new MulticastSocket(PORT_FOR_SYNCHRO);
         socket.joinGroup(group);
         logger.debug("Multicast socket created");
      }
      catch (UnknownHostException e)
      {
         logger.error("Multicast socket creation failed : " + e.getMessage());
         e.printStackTrace();
      }
      catch (IOException e)
      {
         logger.error("Multicast socket creation failed : " + e.getMessage());
         e.printStackTrace();
      }
   }

   private void _dispose()
   {
      try
      {
         socket.leaveGroup(group);
         socket.close();
         logger.debug("Multicast socket closed");
      }
      catch (IOException e)
      {
         logger.error("Multicast socket closure failed : " + e.getMessage());
         e.printStackTrace();
      }
   }
}
