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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.sds.DataAccessException;

/**
 * @author goul5436
 *
 */
class DataMessage
{
   // ---------------------------------------------------------------------
   // init logger

   private static final Logger logger = LoggerFactory
         .getLogger(DataMessage.class.getName());

   // ---------------------------------------------------------------------

   private DataImpl data = null;

   private int deviceId = 0;
   private long timestamp = 0;

   private String pathname = null;
   private int expectedRevision = -1; // -1 pour les messages SDS, -2 pour les acquittements
   private int floorRevision = 0;

   // ---------------------------------------------------------------------


   /**
    * @param data
    */
   public DataMessage(DataImpl data, int floorRev)
   {
      this.data = data;
      this.floorRevision = floorRev;
      this.timestamp = System.currentTimeMillis();
      this.deviceId = HomeSharedDataImpl.getDeviceId();
   }

   /**
    * @param data
    */
   private DataMessage(DataImpl data, int id, long ts)
   {
      this.data = data;
      this.timestamp = ts;
      this.deviceId = id;
   }

   /**
    * @param data
    */
   public DataMessage(String pathname, int expectedRev, int floorRev)
   {
      this.pathname = pathname;
      this.timestamp = System.currentTimeMillis();
      this.deviceId = HomeSharedDataImpl.getDeviceId();
      this.expectedRevision = expectedRev;
      this.floorRevision = floorRev;
   }

   /**
    * @param data
    */
   private DataMessage(String pathname, int id, long ts, int expectedRev, int floorRev)
   {
      this.pathname = pathname;
      this.timestamp = ts;
      this.deviceId = id;
      this.expectedRevision = expectedRev;
      this.floorRevision = floorRev;
   }

   /**
    * @return Returns the data.
    */
   public DataImpl getData()
   {
      return data;
   }

   /**
    * @return the deviceId
    */
   public int getDeviceId()
   {
      return deviceId;
   }

   /**
    * @return Returns the timestamp.
    */
   public long getTimestamp()
   {
      return timestamp;
   }


   /**
    * @return the pathname
    */
   public String getPathname()
   {
      return (expectedRevision == -1 ? data.getPathname() : pathname);
   }

   /**
    * @return the expectedRevision
    */
   public int getExpectedRevision()
   {
      return expectedRevision;
   }

   /**
    * @return the floorRevision
    */
   public int getFloorRevision()
   {
      return floorRevision;
   }

   /**
    * @return if the message is a request
    */
   public boolean isRequest()
   {
      return (expectedRevision >= 0);
   }

   /**
    * @return if the message is a ACK message
    */
   public boolean isAck()
   {
      return (expectedRevision == -2);
   }

   public byte[] getBytes()
   {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      try
      {
         DataOutputStream dos = new DataOutputStream(bos);

         dos.write(deviceId);
         dos.writeLong(timestamp);
         if (expectedRevision == -1)
         {
            dos.write(1);
            data.writeTo(dos, floorRevision);
         }
         else
         {
            dos.write(2);
            dos.writeUTF(pathname == null ? "" : pathname);
            dos.writeInt(expectedRevision);
            dos.writeInt(floorRevision);
         }
         dos.write(-1);

         dos.close();
      }
      catch (IOException e)
      {
         logger.error("getBytes failed : " + e.getMessage());
         e.printStackTrace();
      }
      return bos.toByteArray();
   }

   public static DataMessage bytesToData(byte[] bytes)
   {
      DataMessage res = null;
      try
      {
         ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
         DataInputStream dis = new DataInputStream(bis);

         int devId = dis.read();
         long ts = dis.readLong();
         if (dis.read() == 1)
         {
            DataImpl dir = DataImpl.readFrom(dis);
            res = new DataMessage(dir, devId, ts);
         }
         else
         {
            String pathname = dis.readUTF();
            int expectedRev = dis.readInt();
            int floorRev = dis.readInt();
            res = new DataMessage(pathname, devId, ts, expectedRev, floorRev);
         }
         dis.close();
      }
      catch (DataAccessException e)
      {
         logger.error("bytesToData failed : " + e.getMessage());			
         e.printStackTrace();
      }
      catch (IOException e)
      {
         logger.error("bytesToData failed : " + e.getMessage());			
         e.printStackTrace();
      }
      return res;
   }
}
