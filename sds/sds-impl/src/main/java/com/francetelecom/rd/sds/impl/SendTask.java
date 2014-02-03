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
package com.francetelecom.rd.sds.impl;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.communicationlayer.DataChannel;

/**
 * @author goul5436
 *
 */
public class SendTask implements Runnable, ExpectedData
{
   // ---------------------------------------------------------------------
   // init logger

   private static final Logger logger = LoggerFactory
         .getLogger(SendTask.class.getName());

   // ---------------------------------------------------------------------

   private static boolean connected = true;
   private static DataChannel channel = TaskManager.getDataChannel();

   public static long RETRANSMISSION_DELAY = 1000; // 1 s
   public static long MAX_RETRANSMISSION_DELAY = 300000; // 5 mn

   private String pathname = null;
   int expectedRevision = -1; // -1 : Message SDS, -2 acquittement à envoyer, >=0 requête
   int floorRevision = 0;
   private long timeout = 0;
   private long delay = 0;

   private int revision = 0; // revision actually sent

   // ---------------------------------------------------------------------

   /**
    * @param pathname
    */
   public SendTask(String pathname, int floorRevision)
   {
      this.pathname = (pathname == null ? "" : pathname);
      this.floorRevision = floorRevision;
   }

   /**
    * @param pathname
    */
   public SendTask(String pathname, int expectedRevision, int floorRevision)
   {
      this.pathname = (pathname == null ? "" : pathname);
      this.expectedRevision = expectedRevision;
      this.floorRevision = floorRevision;
   }

   /**
    * @return the pathname
    */
   public String getPathname()
   {
      return pathname;
   }

   /**
    * @return the expectedRevision
    */
   public int getExpectedRevision()
   {
      return expectedRevision;
   }

   /**
    * @return the sent revision
    */
   public int getRevision()
   {
      return revision;
   }

   /**
    * @return the next timeout to send the message
    */
   public long getTimeout()
   {
      return timeout;
   }

   /**
    * @param d delay for first sending
    */
   public void setDelay(long d)
   {
      this.timeout = System.currentTimeMillis() + d;
      delay = RETRANSMISSION_DELAY / 2;
   }

   /**
    * @return if this message is out of date
    */
   public boolean isOutOfDate()
   {
      return delay >= MAX_RETRANSMISSION_DELAY;
   }

   public static void setConnectionStatus(boolean status)
   {
      if (connected != status)
      {
         connected = status;
         if (connected)
         {
            TaskManager.addTask(new SendTask(null, -1));
         }
      }
   }

   public static boolean isConnected()
   {
      return connected;
   }

   public void run()
   {
      if (connected)
      {
         DirectoryImpl root = (DirectoryImpl)HomeSharedDataImpl.getRootDirectory();
         try
         {
            String logPathName = pathname != "" ? ("'" + pathname + "'") : "root";

            DataImpl data = root.getData(pathname);
            if (data == null) // la donnée a été supprimée entre temps
            {
               logger.debug("data removed on previous step");
               return;
            }

            if (data.revision == 0) // cas de la racine non initialisée
            {
               expectedRevision = 0;
               floorRevision = 0;
            }
            if (expectedRevision == -1)
            {
               // Here is a SDS message

               if (timeout == 0) // 1er envoi
               {
                  logger.info("FIRST SEND ON " + logPathName);
                  delay = RETRANSMISSION_DELAY;
                  TaskManager.registerTaskSent(this);
               }
               else
               {
                  logger.info("RETRANSMISSION ON " + logPathName);
                  if (delay < MAX_RETRANSMISSION_DELAY) { delay *= 2; }
               }
               timeout = System.currentTimeMillis() + delay;
               // send data
               // On peut revérifier que la data est dans la révision attendue ??
               byte[] msg = new DataMessage(data, floorRevision).getBytes();
               if (msg.length > channel.getMaxBytes()) // trop gros, on va y aller progressivement...
               {
                  logger.info("message is too big : " + msg.length);
                  msg = new DataMessage(data, -1).getBytes();
                  if (msg.length > channel.getMaxBytes()) // encore trop gros !
                  {
                     logger.info("message is still too big : " + msg.length);
                     msg = new DataMessage(data, -1, 1).getBytes();
                  }
               }
               revision = data.getRevision();
               channel.send(msg);

               //Console.log("Message envoyé avec révision : " + data.revisionToString() + " Taille : " + msg.length);
               logger.info("SEND (" + msg.length + " octets) : \n" + DataMessage.bytesToData(msg).getData().toStyledString(""));
            }
            else if (expectedRevision == -2) // c'est un acquittement à envoyer
            {
               // Here is an ACK message

               byte[] msg = new DataMessage(pathname, expectedRevision, floorRevision).getBytes();
               channel.send(msg);

               logger.info("SEND ACK FOR (" + DataImpl._revisionToString(floorRevision) + ")");
            }
            else // c'est une requête
            {
               // Here is a REQUEST

               if ((expectedRevision != 0) && (data.revision == expectedRevision) && (data.value != null))
               {
                  logger.info("ALREADY RECEIVED " + logPathName + " " + data.fullRevisionToString());
                  TaskManager.removePendingRequest(logPathName, expectedRevision);
               }
               else
               {
                  if (timeout == 0) // 1er envoi
                  {
                     delay = RETRANSMISSION_DELAY;
                     logger.info("SEND REQUEST ON " + logPathName + " FOR (" + DataImpl._revisionToString(expectedRevision) + ")");
                  }
                  else // retransmission
                  {
                     logger.info("RETRANSMISSION ON " + logPathName + " FOR (" + DataImpl._revisionToString(expectedRevision) + ")");
                     if (delay < MAX_RETRANSMISSION_DELAY) { delay *= 2; }
                  }
                  timeout = System.currentTimeMillis() + delay;
                  DataMessage dataMessage = new DataMessage(pathname, expectedRevision, floorRevision);
                  byte[] msg = dataMessage.getBytes();
                  channel.send(msg);
               }
            }
         }
         catch (DataAccessException e)
         {
         }
      }
   }

   /*
    * this overrides t
    */
   public boolean override(SendTask t)
   {
      return pathname.equals(t.pathname);
   }
}
