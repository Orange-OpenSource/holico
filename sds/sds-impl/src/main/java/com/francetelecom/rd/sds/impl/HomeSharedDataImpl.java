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

import java.io.*;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.HomeSharedData;
import com.francetelecom.rd.sds.Directory;

/**
 * This class is the entry point of the Home Shared DataImpl Structure
 *  
 * @author goul5436
 */
public class HomeSharedDataImpl implements HomeSharedData
{
   // ---------------------------------------------------------------------
   // init logger

   private static final Logger logger = LoggerFactory.getLogger(HomeSharedDataImpl.class.getName());

   // ---------------------------------------------------------------------

   protected static final int DEV_MASK = 0xFF000000;

   private static HomeSharedDataImpl instance = new HomeSharedDataImpl();
   private static TaskManager taskManager = TaskManager.getInstance();

   private Directory root = null;
   private int lastLocalRevision = 0;
   private int nextLocalRevision = 0;

   // ---------------------------------------------------------------------

   public static HomeSharedDataImpl getInstance()
   {
      return instance;
   }

   /**
    * Acquires the lock to access to the shared data structure.
    */
   public void lock()
   {
      taskManager.lock();
   }

   /**
    * Releases the lock.
    */
   public void unlock()
   {
      taskManager.unlock();
   }

   /**
    * Loads and returns the root directory of the shared data structure.
    * 
    * @param forceReinit true to force the creation of an empty shared data structure
    * @param filename to read the data
    * @param deviceId device identifier. Only used if a new shared data structure needs to be created, in this case,
    * the value must be between 1 and 255 (otherwise null is returned).
    * 
    * @return The root directory of the shared data structure.
    */
   public Directory getRootDirectory(boolean forceReinit, String filename, int deviceId)
   {
      if (forceReinit)
      {
         root = null;
      }
      if (root == null)
      {
         // local shared data structure does not exist yet 
         // => load/create it

         if (!forceReinit)
         {
            try
            {
               // try to load it from a saved file
               FileInputStream fis = new FileInputStream(filename);
               DataInputStream dis = new DataInputStream(fis);

               lastLocalRevision = dis.readInt();
               root = (DirectoryImpl)DataImpl.readFrom(dis);
               dis.close();

               logger.debug("shared data structure loaded from : " + filename);
            }
            catch (FileNotFoundException e)
            {
               //e.printStackTrace();
            }
            catch (DataAccessException e)
            {
               //e.printStackTrace();
            }
            catch (IOException e)
            {
               logger.error("shared data structure loading failed : " + e.getMessage());
               e.printStackTrace();
            }
         }
         if ((root == null) && (deviceId > 0) && (deviceId < 128))
         {
            // local root still does not exist (no file)
            // => create a new one
            lastLocalRevision = deviceId << 24;
            root = new DirectoryImpl(null, null, 0);

            logger.debug("new shared data structure created");
         }
         if (root != null)
         {
            logger.debug("send the new local shared data structure to all node on network");
            // create a SendTask with null pathname => send root
            TaskManager.addTask(new SendTask(null, -1));
         }
      }
      return root;
   }

   /**
    * Saves the shared data structure.
    *
    * @param filename to save the shared data structure
    */
   public void save(String filename)
   {
      if (root != null)
      {
         lock();
         try
         {
            FileOutputStream fos = new FileOutputStream(filename);
            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeInt(lastLocalRevision);
            ((DirectoryImpl)root).writeTo(dos, 0);

            dos.close();

            logger.debug("shared data structure saved in : " + filename);
         }
         catch (FileNotFoundException e)
         {
            logger.error("shared data structure backup failed : " + e.getMessage());
            e.printStackTrace();
         }
         catch (IOException e)
         {
            logger.error("shared data structure backup failed : " + e.getMessage());
            e.printStackTrace();
         }
         finally
         {
            unlock();
         }
      }
   }

   /**
    * Returns the root directory of the shared data structure (which must be previously loaded).
    *
    * @return the root directory
    */
   public static Directory getRootDirectory()
   {
      return instance.root;
   }

   /**
    * @return the local base revision
    */
   public static int getBaseRevision()
   {
      return instance.lastLocalRevision;
   }

   /**
    * @return Set the local base revision.
    */
   static void setBaseRevision(int baseRev)
   {
      instance.lastLocalRevision = baseRev;
   }

   /**
    * @return the device id
    */
   public static int getDeviceId()
   {
      return instance.lastLocalRevision >> 24;
   }

   /**
    * @return Returns the incremented rootRevision.
    */
   static int newLocalRevision(boolean commited)
   {
      instance.nextLocalRevision = (commited ? 0 : instance.lastLocalRevision+1);
      return (commited ? ++instance.lastLocalRevision : instance.nextLocalRevision);
   }

   /**
    * @return Returns the incremented rootRevision.
    */
   static void commitLocalRevision()
   {
      if (instance.nextLocalRevision != 0)
      {
         ++instance.lastLocalRevision;
         instance.nextLocalRevision = 0;
      }
   }
}
