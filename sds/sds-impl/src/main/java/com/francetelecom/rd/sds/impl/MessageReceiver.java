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
import com.francetelecom.rd.sds.communicationlayer.DataReceiver;

/**
 * @author goul5436
 *
 */
public class MessageReceiver implements DataReceiver
{
   // ---------------------------------------------------------------------
   // init logger

   private static final Logger logger = LoggerFactory.getLogger(MessageReceiver.class.getName());

   // ---------------------------------------------------------------------


   public static final int REDUNDANCY_INTERVAL = 100;

   private static TaskManager taskManager = TaskManager.getInstance();

   // ---------------------------------------------------------------------

   /* (non-Javadoc)
    * @see com.francetelecom.rd.sds.communicationlayer.DataReceiver#dataReceived(byte[])
    */
   public void dataReceived(byte[] data)
   {
      logger.debug("data received");

      if (SendTask.isConnected())
      {
         long time = System.currentTimeMillis();
         //Console.log("Paquet recu de : " + packet.getAddress().getHostName() + " Taille : " + packet.getLength());

         taskManager.lockUnconditionally();
         try
         {
            DataMessage msg = DataMessage.bytesToData(data);
            int devId = msg.getDeviceId();
            if (devId != (HomeSharedDataImpl.getDeviceId())) // on vérifie que c'est pas notre propre message
            {
               if (msg.isRequest())
               {
                  logger.info("RECEIVE A REQUEST FROM " + devId + " ON '" + msg.getPathname() + "'");

                  TaskManager.addTask(new ResponseToRequestForDataTask(msg.getPathname(), msg.getExpectedRevision(), msg.getFloorRevision()));
               }
               else if (msg.isAck())
               {
                  logger.info("RECEIVE AN ACK FROM " + devId + " FOR (" + DataImpl._revisionToString(msg.getFloorRevision()) + ")");

                  TaskManager.registerAckMessage(msg.getFloorRevision(), devId);
               }
               else
               {
                  DataImpl sdsData = msg.getData();
                  logger.info("RECEIVE A SDS MESSAGE FROM " + devId + "\n" + sdsData.toStyledString(""));

                  int compar = 1;
                  if (sdsData.getPathname().isEmpty()) // c'est la racine
                  {
                     logger.debug("received a root sds message");

                     compar = ((DirectoryImpl)HomeSharedDataImpl.getRootDirectory())._revcmp(sdsData);
                     if (compar == 1) // sdsData > root, cas "normal" : on reçoit un sds plus récent
                     {
                        logger.debug("sdsData > root => send an ACK for this message");
                        TaskManager.addTask(new SendTask(null, -2, sdsData.getRevision())); // pour l'envoi de l'acquittement
                     }
                     else if (compar == 2) // Réception d'une révision de root < révision locale => maj distante nécessaire
                     {
                        logger.debug("root < local revision => remote update required");
                        TaskManager.addTask(new SendTask(null, sdsData.getRevision()));
                     }
                  }
                  if ((compar != 0) && (compar != 2)) // sinon sdsData <= root == donnée reçue plus ancienne (ou égale) => on l'ignore
                  {
                     logger.debug("sdsData > root or not comparable => synchro");
                     TaskManager.addTask(new SynchroTask(sdsData, time-msg.getTimestamp()));
                  }
               }
            }
            else
            {
               logger.debug("RECEIVE OUR OWN MESSAGE => ignore it");
            }
         }
         finally
         {
            taskManager.unlock();
         }
      }
   }

   public String getValue(String pathname)
   {
      String value = null;
      if (SendTask.isConnected())
      {
         taskManager.lock();
         try
         {
            DirectoryImpl root = (DirectoryImpl)HomeSharedDataImpl.getRootDirectory();
            DataImpl data = root.getData(pathname);
            if (data != null)
            {
               if (data instanceof ParameterImpl)
               {
                  Object val = ((ParameterImpl)data).getValueImpl();
                  if (val != null)
                  {
                     value = val.toString();
                  }
               }
               else
               {
                  String[] names = root.getChildNames(pathname);
                  value = "";
                  for (int i=0; i<names.length; i++)
                  {
                     if (i>0) { value += "|"; }
                     value += names[i];
                  }
               }
            }
         }
         catch (DataAccessException e)
         {
         }
         finally
         {
            taskManager.unlock();
         }
      }
      return value;
   }

   public boolean setValue(String pathname, String value)
   {
      boolean res = false;
      if (SendTask.isConnected())
      {
         taskManager.lock();
         try
         {
            DirectoryImpl root = (DirectoryImpl)HomeSharedDataImpl.getRootDirectory();
            try
            {
               root.setParameterValue(pathname, value);
               res = true;
            }
            catch (DataAccessException e)
            {
            }
         }
         finally
         {
            taskManager.unlock();
         }
      }
      return res;
   }
}
