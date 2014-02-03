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
 *    http://opensource.org/licenses/BSD-3-Clause
 */
package com.francetelecom.rd.sds.impl;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

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

   private static ArrayList<UUID> UUIDs = new ArrayList<UUID>();
   private static final UUID NO_UUID = new UUID(0,0);

   private Directory root = null;
   private int lastLocalRevision = 1; // Voir possibilité de remettre à 0 !!
   private int lockRevisionState = 0; // 0: non locké, 1: locké non utilisé, 2 locké et utilisé

   // ---------------------------------------------------------------------

   public static HomeSharedDataImpl getInstance()
   {
      return instance;
   }

   public static ArrayList<UUID> getUUIDs()
   {
      return UUIDs;
   }

   public static boolean isPriorTo(int id0, int id1)
   {
      UUID uuid0 = (id0 < UUIDs.size() ? UUIDs.get(id0) : NO_UUID);
      UUID uuid1 = (id1 < UUIDs.size() ? UUIDs.get(id1) : NO_UUID);
      return (uuid0.compareTo(uuid1) == -1);
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
      taskManager.unlock(true);
   }

   /**
    * Loads and returns the root directory of the shared data structure.
    * 
    * @param forceReinit true to force the creation of an empty shared data structure
    * @param filename to read the data
    * @param uuid unique device identifier. If null, a new UUID is randomly created. 
    * Remark : uuid is useful only when a new shared data structure needs to be created.
    *
    * @return The root directory of the shared data structure.
    */
   public Directory getRootDirectory(boolean forceReinit, String filename, UUID uuid)
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

               int nbDev = dis.readInt();
               int oldId = nbDev >> 24;
               if (oldId > 0) // C'est un ancien format, on n'a pas de table d'UUIDs
               {
                  if (uuid == null)
                  {
                     uuid = UUID.randomUUID();
                  }
                  UUIDs.add(uuid);
                  lastLocalRevision = nbDev & ~DEV_MASK;
                  if (lastLocalRevision==0) { lastLocalRevision = 1; }
                  nbDev = 1;
               }
               else
               {
                  oldId = -1;
                  for (int i=0; i<nbDev; i++)
                  {
                     long most = dis.readLong();
                     long least = dis.readLong();
                     UUIDs.add(new UUID(most, least));
                  }
                  uuid = UUIDs.get(0);
                  lastLocalRevision = dis.readInt();
               }
               root = (DirectoryImpl)DataImpl.readFrom(dis, null);
               // recherche des ids utilisés ou non
               boolean[] ids = new boolean[nbDev]; // par défaut tout est à false
               ((DirectoryImpl)root).markUsedIds(ids, oldId);
               for (int i=1; i<nbDev; i++)
               {
                  if (!ids[i]) { UUIDs.set(i, NO_UUID); }
               }
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
         if (root == null)
         {
            // local root still does not exist (no file)
            // => create a new one
            if (uuid == null)
            {
               uuid = UUID.randomUUID();
            }
            UUIDs.add(uuid);
            root = new DirectoryImpl(null, null, 0);

            logger.debug("new shared data structure created");
         }
         if (root != null)
         {
            logger.debug("send the new local shared data structure to all node on network");
            // create a SendTask with null pathname => send root
            TaskManager.addTask(new SendTask(null, -1));
         }
         logger.debug("Local node UUID = "+uuid);
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

            dos.writeInt(UUIDs.size());
            for (UUID id : UUIDs)
            {
               dos.writeLong(id.getMostSignificantBits());
               dos.writeLong(id.getLeastSignificantBits());
            }
            dos.writeInt(lastLocalRevision);
            ((DirectoryImpl)root).writeTo(dos, 0, -1);

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
   public static int getDeviceId(UUID uuid)
   {
      int id = UUIDs.indexOf(uuid);
      if (id == -1)
      {
         id = UUIDs.indexOf(NO_UUID); // Y a-t-il une place libre ?
         if (id >= 0) // si oui, on la prend
         {
            UUIDs.set(id, uuid);
         }
         else // sinon on ajoute un élément
         {
            id = UUIDs.size();
            UUIDs.add(uuid);
         }
      }
      return id;
   }

   /**
    * Prepare to assign a new local revision, if not already done.
    *
    * @return true if not previously locked
    */
   static boolean lockRevision()
   {
      boolean res = (instance.lockRevisionState == 0);
      if (res)
      {
         instance.lockRevisionState = 1;
      }
      return res;
   }

   /**
    * @return The prepared new local revision.
    */
   static int newRevision()
   {
      assert(instance.lockRevisionState != 0);
      instance.lockRevisionState = 2;
      return instance.lastLocalRevision + 1;
   }

   /**
    * Commit the new local revision if used, then unlock.
    */
   static void unlockRevision()
   {
      assert(instance.lockRevisionState != 0);
      if (instance.lockRevisionState == 2) // il y a eu au moins une modification
      {
         ++instance.lastLocalRevision;
         TaskManager.addTask(new SendTask(null, instance.lastLocalRevision-1)); // floorRevision = revision-1 pour n'envoyer qu'un diff
      }
      instance.lockRevisionState = 0;
   }
}
