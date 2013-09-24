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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.sds.communicationlayer.DataChannel;
import com.francetelecom.rd.sds.communicationlayer.DataChannelFactory;

/**
 * @author goul5436
 *
 */
class TaskManager extends Thread
{
   // ---------------------------------------------------------------------
   // init logger

   private static final Logger logger = LoggerFactory.getLogger(SynchroTask.class.getName());

   // ---------------------------------------------------------------------

   private static final long TRANSITION_TIME = 1000; // 1 second
   private static final int NB_TASKS_MAX = 200; // 20 tasks max

   //private static int discoveryRepeat = 2; // Nb de répétitions du 1er send pour fiabiliser la découverte

   private static TaskManager instance = null;
   private static DataChannel channel = DataChannelFactory.getDataChannel();

   private ReentrantLock lock = new ReentrantLock();
   private Condition condTaskAdded = lock.newCondition();
   private Condition condStableState = lock.newCondition();
   private int nbCallsWaiting = 0;

   private ArrayList tasks = new ArrayList();
   private boolean alive = true;
   private long timeout = 0;

   private ArrayList pendingRequests = new ArrayList(); // contient à la fois des ResponseToRequestForDataTask et des SendTask
   private SendTask rootUpdateSent = null;
   private ArrayList expectedPeersAck = new ArrayList(); // liste des devices dont on attend un ACK sur la dernière maj locale envoyée rootUpdateSent

   private static HashMap connectedPeers = new HashMap();

   // ---------------------------------------------------------------------

   public static TaskManager getInstance()
   {
      if (instance == null)
      {
         instance = new TaskManager();
         channel.registerReceiver(new MessageReceiver());
         instance.start();
      }
      return instance;
   }

   /**
    * Acquires the lock to access to the shared data structure.
    */
   public void lockUnconditionally()
   {
      lock.lock();
   }

   /**
    * Acquires the lock to access to the shared data structure.
    */
   public void lock()
   {
      lock.lock();
      if (!DirectoryImpl.isInStableState())
      {
         nbCallsWaiting++;
         try
         {
            condStableState.awaitNanos(10000000 * TRANSITION_TIME); // 10 s
         }
         catch (InterruptedException e)
         {
         }
         DirectoryImpl.ensureStability();
         nbCallsWaiting--;
      }
   }

   /**
    * Releases the lock.
    */
   public void unlock()
   {
      lock.unlock();
   }

   /**
    * @return the data channel
    */
   public static DataChannel getDataChannel()
   {
      return channel;
   }

   public static void addTask(Runnable t)
   {
      instance._addTask(t);
   }

   private void _addTask(Runnable t)
   {
      lock.lock();
      try
      {
         boolean toAdd = true;
         if (t instanceof ResponseToRequestForDataTask) // si on a déjà la même requête, on ignore
         {
            if (!_addPendingRequest((ResponseToRequestForDataTask)t))
            {
               logger.info("REPEATED REQUEST IGNORED ON '" + ((ResponseToRequestForDataTask)t).getPathname() + "'");
               toAdd = false;
            }
         }
         else if (t instanceof SendTask)
         {
            SendTask st = (SendTask)t;
            Iterator it = tasks.iterator();
            while (it.hasNext())
            {
               Runnable r = (Runnable)it.next();
               if ((r instanceof SendTask) && (st.override((SendTask)r)))
               {
                  it.remove(); // on supprime la tâche send plus ancienne pour n'en garder qu'une par pathname. Comprend aussi les ACK
                  break;
               }
            }
            if (st.expectedRevision >= 0) // c'est une requête, on est dans un état transitoire
            {
               if (_addPendingRequest(st))
               {
                  logger.info("ENTRY INTO TRANSITION STATE");
                  timeout = System.currentTimeMillis() + TRANSITION_TIME;
               }
               else
               {
                  logger.info("REQUEST ALREADY SENT ON " + st.getPathname());
               }
            }
            else if (st.expectedRevision == -2) // c'est un ACK à envoyer, on ignorer donc les ACK à recevoir car on a reçu une rev + récente
            {
               rootUpdateSent = null;
            }
         }
         else if (t instanceof NotifyTask)
         {
            Iterator itn = tasks.iterator();
            while (itn.hasNext())
            {
               Runnable r = (Runnable)itn.next();
               if ((r instanceof NotifyTask) && t.equals(r))
               {
                  //System.out.println("------------ REPEATED NOTIFY TASK IGNORED");
                  toAdd = false;
                  break;
               }
            }
         }
         if (toAdd)
         {
            if (tasks.size() > NB_TASKS_MAX)
            {
               logger.warn("WARNING : Max number of tasks reached");    
               tasks.remove(0); // on supprime une tâche en attente. A voir : suppression des tâches redondantes, SynchroTask, NotifyTask, ... !!
            }
            tasks.add(t);
            condTaskAdded.signal();
         }
      }
      finally
      {
         lock.unlock();
      }
   }

   public void terminate()
   {
      channel.close();
      alive = false;
   }

   public void run()
   {
      while (alive)
      {
         lock.lock();
         try
         {
            Runnable toSend = null;
            boolean toSignal = false;
            long waitTime = 0; // différent de 0 si état transitoire
            if (timeout != 0)
            {
               waitTime = timeout - System.currentTimeMillis();
               if (waitTime <= 0)
               {
                  waitTime = 0;
                  timeout = 0;
               }
            }
            if (!tasks.isEmpty())
            {
               int iTask = -1;
               if (waitTime == 0)
               {
                  int level = 0; // 0: initial, 1: notify trouvé, 2: send trouvé, 3: synchro ou responseToReq trouvé
                  if (nbCallsWaiting > 0)
                  {
                     toSignal = true;
                     level = 1;
                  }
                  for (int i=0; i<tasks.size(); i++)
                  {
                     Runnable task = (Runnable)tasks.get(i);
                     if (task instanceof SendTask) // Priorité 2
                     {
                        if (level < 2)
                        {
                           iTask = i;
                           level = 2;
                        }
                     }
                     else if (task instanceof NotifyTask) // Priorité 3
                     {
                        if (level < 1)
                        {
                           iTask = i;
                           level = 1;
                        }
                     }
                     else // SynchroTask or ResponseToRequestForDataTask. Priorité 1
                     {
                        iTask = i;
                        level = 3;
                        break;
                     }
                  }
               }
               else // en état transitoire
               {
                  for (int i=0; i<tasks.size(); i++)
                  {
                     Runnable task = (Runnable)tasks.get(i);
                     if ((task instanceof SynchroTask) && ((SynchroTask)task).isExpected()) // c'est une donnée attendue
                     {
                        iTask = i;
                        break;
                     }
                     else if ((task instanceof SendTask) && (((SendTask)task).expectedRevision != -1)) // c'est une requête
                     {
                        iTask = i;
                        break;
                     }
                  }
               }
               if (iTask >= 0)
               {
                  toSend = (Runnable)tasks.remove(iTask);
               }
            }
            if ((toSend == null) && !toSignal)
            {
               // on recherche les éventuelles retransmissions nécessaires (possibles en état transitoires)
               long now = System.currentTimeMillis();
               Iterator it = pendingRequests.iterator();
               while (it.hasNext())
               {
                  ExpectedData req = (ExpectedData)it.next();
                  if ((req instanceof SendTask) && (now >= ((SendTask)req).getTimeout()))
                  {
                     toSend = (SendTask)req;
                     break;
                  }
               }
               if ((toSend == null) && (rootUpdateSent != null) && (waitTime == 0)) // retransmissions de messages de données
               {
                  if (now >= rootUpdateSent.getTimeout())
                  {
                     toSend = rootUpdateSent;
                     if (expectedPeersAck.isEmpty())
                     {
                        rootUpdateSent = null;
                     }
                     else if (rootUpdateSent.isOutOfDate())
                     {
                        Integer iDevId = (Integer)expectedPeersAck.get(0); // 1er device de la liste
                        if (now - ((Long)connectedPeers.get(iDevId)).longValue() > SendTask.MAX_RETRANSMISSION_DELAY) // alors on le considère comme déconnecté
                        {
                           expectedPeersAck.remove(0);
                           connectedPeers.remove(iDevId);
                        }
                     }
                  }
               }
            }
            if (toSend != null)
            {
               if ( !(toSend instanceof SynchroTask) // stabilité traitée précisément dans synchronize()
                 && !((toSend instanceof SendTask) && (((SendTask)toSend).expectedRevision != -1)) ) // requête, état transitoire possible
               {
                  DirectoryImpl.ensureStability();
                  timeout = 0;
               }
               toSend.run();
               if ((timeout !=0) && DirectoryImpl.isInStableState()) // on sort d'un état transitoire
               {
                  logger.info("NORMAL EXIT OF TRANSITION STATE (2)");
                  timeout = 0;
               }
            }
            else if (toSignal)
            {
               DirectoryImpl.ensureStability();
               timeout = 0;
               condStableState.signal();
            }
            else
            {
               if (waitTime == 0) // on est en état stable ou tout juste sorti d'état transitoire
               {
                  DirectoryImpl.ensureStability(); // si on est en état transitoire alors on force le passage en état stable
                  if (tasks.isEmpty()) // faux, à moins que la sortie d'état transitoire n'est ajouté une NotifyTask
                  {
                     waitTime = SendTask.RETRANSMISSION_DELAY;
                  }
               }
               if (waitTime > 0)
               {
                  // attente
                  try
                  {
                     condTaskAdded.awaitNanos(1000000*waitTime);
                  }
                  catch (InterruptedException e)
                  {
                     terminate();
                  }
               }
            }
         }
         finally
         {
            lock.unlock();
         }
      }
   }

   public static boolean addPendingRequest(SendTask t)
   {
      return instance._addPendingRequest(t);
   }

   private boolean _addPendingRequest(ExpectedData newReq)
   {
      boolean found = false;
      boolean toAdd = true;
      Iterator it = pendingRequests.iterator();
      while (it.hasNext())
      {
         ExpectedData req = (ExpectedData)it.next();
         /*found = newReq.getPathname().equals(req.getPathname()) && (newReq.getExpectedRevision() == req.getExpectedRevision());
         if (found)
         {
            toAdd = (req instanceof ResponseToRequestForDataTask) && (newReq instanceof SendTask); // SendTask remplace ResponseToRequestForDataTask
            if (toAdd) { it.remove(); }
            break;
         }*/
         found = newReq.getPathname().equals(req.getPathname());
         if (found)
         {
            toAdd = (newReq instanceof SendTask) || (req instanceof ResponseToRequestForDataTask); // on ne remplace pas des SendTask par des ResponseToRequest
            if (toAdd) { it.remove(); }
            break;
         }
      }
      if (toAdd)
      {
         pendingRequests.add(newReq);
      }
      return !found;
   }

   public static boolean removePendingRequest(String pathname, int expectedRev)
   {
      return instance._removePendingRequest(pathname, expectedRev);
   }

   private boolean _removePendingRequest(String pathname, int expectedRev)
   {
      int sizeBefore = pendingRequests.size();
      Iterator it = pendingRequests.iterator();
      while (it.hasNext())
      {
         ExpectedData req = (ExpectedData)it.next();
         String rPathname = req.getPathname();
         if ( ((rPathname.isEmpty() && (pathname == null)) || rPathname.equals(pathname))
               && ((expectedRev == 0) || (req.getExpectedRevision() == 0) || (req.getExpectedRevision() == expectedRev)))
         {
            it.remove();
            if ((sizeBefore == 1) && (timeout > 0)) // plus de requête en attente
            {
               logger.info("NORMAL EXIT OF TRANSITION STATE (1)");
               timeout = 0;
            }
            return true; // c'est fait
         }
      }
      return false;
   }

   public static void touchPeer(int devId, long time)
   {
      connectedPeers.put(Integer.valueOf(devId), Long.valueOf(time));
   }

   public static void registerTaskSent(SendTask task)
   {
      removePendingRequest(task.getPathname(), 0); // la requête n'est plus en cours puisque j'envoie ma réponse
      instance._registerTaskSent(task);
   }

   private void _registerTaskSent(SendTask task)
   {
      if (task.getPathname().isEmpty()) // c'est la racine
      {
         rootUpdateSent = task;
         expectedPeersAck.clear();
         expectedPeersAck.addAll(connectedPeers.keySet());
      }
   }

   public static void registerAckMessage(int rev, int devId)
   {
      instance._registerAckMessage(rev, devId);
   }

   private void _registerAckMessage(int rev, int devId)
   {
      if (((rootUpdateSent != null) && (rootUpdateSent.getRevision() == rev)))
      {
         expectedPeersAck.remove(Integer.valueOf(devId));
         if (expectedPeersAck.isEmpty())
         {
            rootUpdateSent = null;
         }
      }
   }
}
