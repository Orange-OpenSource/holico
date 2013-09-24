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

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.HashMap;

import com.francetelecom.rd.sds.*;

public abstract class DataImpl implements Data
{
   // Masks for results of comparison or synchronization
   static final int CMP_MASK       = 0x00000003; // 0 : ==, 1 : >, 2 : <, 3 : <> (non comparable)
   static final int QUESTION_FLAG  = 0x00000004;
   static final int DIR_PARAM_FLAG = 0x00000008;
   static final int MODIFIED_FLAG  = 0x00000010;
   static final int PENDING_FLAG   = 0x00000020;

   protected static TaskManager taskManager = TaskManager.getInstance();

   protected int type = TYPE_PARAM;
   protected String name = null;
   protected DirectoryImpl parent = null;
   protected String pathname = null;
   protected Object value = null; // value == null iff value non loaded, otherwise value must at least have a default value
   protected int revision = 0; // révision actuelle stock�e
   protected ArrayList previousRevisions = new ArrayList(); // révisions antérieures à la révision actuelle
   protected long timestamp = 0;
   protected boolean ignored = false; // if the data is ignored by this device (not stored)
   protected boolean persistent = true; // cas particulier : on garde la valeur mais quand on y accède, on envoie systématiquement pour savoir s'il y a des maj. En l'absence de réponse (ex: en mode d�connect�), on utilise la valeur qu'on a
   private boolean broadcasted = false; // obligatoire pour les persistents
   private boolean lazyStorage = false; // ça dépend de l'équipement : certains mettront en lazy tout ce qui est distant... mais ça peut être plus élaboré que ça... dépendre du volume...
   // par défaut, tt ce qui est distant sera lazy. Ensuite, un device pourra demandé qu'un objet soit tenu à jour.
   private boolean grouped = false;
   protected boolean local = false; // local / distant (déterminé automatiquement selon qu'il est défini localement ou ajouté par un équipement distant)
   protected DataValuer valuer = null; // pour les locaux uniquement
   protected boolean original = true; // true if this is an original data, false if this is a copy

   // En cours de synchronisation
   protected int synchroState = 0;
   protected int peerRevision = 0; // Revision reçue avec laquelle est effectuée la synchronisation en cours
   protected ArrayList peerPreviousRevisions = new ArrayList(); // révisions antérieures reçues
   protected boolean requested = false; // flag non transmis au parent
   protected boolean reversecmp = false; // id.

   private transient ArrayList listeners = null;

   /*
    * @return Result of the comparison :
    *          2 => r1 < r2 (r1 older)
    *          0 => r1 == r2
    *          1 => r1 > r2 (r1 newer)
    *          3 => r1 <> r2 (non comparable)
    */
   protected static int _revcmp(int r1, int r2)
   {
      int res = 3;
      if ((r1 & HomeSharedDataImpl.DEV_MASK) == (r2 & HomeSharedDataImpl.DEV_MASK))
      {
         res = (r1 < r2 ? 2 : (r1 == r2 ? 0 : 1));
      }
      else if (r2 == 0) // révision 0 antérieure à tout
      {
         res = (r1 == 0 ? 0 : 1);
      }
      else if (r1 == 0)
      {
         res = 2;
      }
      return res;
   }

   protected static boolean _isPossiblyNewerThan(int r1, int r2)
   {
      return (_revcmp(r1, r2) & 1) != 0;
   }

   /*
    * @return The previous declared with the same device id as rev
    */
   protected Integer _getPrevious(int rev)
   {
      Integer res = null;
      if (previousRevisions == null) { previousRevisions = new ArrayList(); } // si non initialisé (robustesse)
      int dev = rev & HomeSharedDataImpl.DEV_MASK;
      Iterator it = previousRevisions.iterator();
      while (it.hasNext())
      {
         Integer prev = (Integer)it.next();
         if ((prev.intValue() & HomeSharedDataImpl.DEV_MASK) == dev)
         {
            res = prev;
            break;
         }
      }
      return res;
   }

   /*
    * @param rev Revision to compare with 'this' revision
    *
    * @return Result of the comparison :
    *          2 => data < this (older)
    *          0 => data == this
    *          1 => data > this (newer)
    *          3 => data <> this (non comparable)
    */
   protected int _revcmp(int rev)
   {
      int res = _revcmp(rev, revision);
      if (res == 3) // because 2 different devices
      {
         Integer prev = _getPrevious(rev);
         if ((prev != null) && (rev <= prev.intValue())) // 2 si data plus ancien qu'une rév antérieure (ou égal) alors data plus ancien que this
         {
            res = 2;
         }
      }
      return res;
   }

   /*
    * @param data DataImpl to compare with the current
    * @return Result of the comparison :
    *          2 => data < this (older)
    *          0 => data == this
    *          1 => data > this (newer)
    *          3 => data <> this (non comparable)
    *          7 => data >< this (antagonistic) inconsistent information : each data is mentioned in the list of the other
    */
   protected int _revcmp(DataImpl data)
   {
      int res = _revcmp(data.revision, revision);
      if (res == 3) // because 2 different devices
      {
         Integer prev = data._getPrevious(revision);
         if ((prev != null) && (revision <= prev.intValue())) // 1 si this plus ancien qu'une rév antérieure de data (ou égal) alors this plus ancien que data
         {
            res = 1;
         }
         prev = _getPrevious(data.revision);
         if ((prev != null) && (data.revision <= prev.intValue())) // 2 si data plus ancien qu'une rév antérieure (ou égal) alors data plus ancien que this
         {
            res = (res == 1 ? 7 : 2);
         }
      }
      return res;
   }

   static String _revisionToString(int r)
   {
      return (r >> 24) + "." + (r & ~HomeSharedDataImpl.DEV_MASK);
   }

   public String revisionToString()
   {
      return _revisionToString(revision);
   }

   public String fullRevisionToString()
   {
      String res = "(" + _revisionToString(revision);
      String separ = " > ";
      if (previousRevisions != null) // si non initialisé (robustesse)
      {
         Iterator it = previousRevisions.iterator();
         while (it.hasNext())
         {
            Integer prev = (Integer)it.next();
            res += separ + _revisionToString(prev.intValue());
            separ = ", ";
         }
      }
      res += ")";
      return res;
   }

   /**
    * @param parent
    * @param name
    * @param value
    * @param type
    */
   public DataImpl(DirectoryImpl parent, String name, Object value, int type)
   {
      this.parent = parent;
      this.name = name;
      this.type = type;
      this.value = value;
   }

   /**
    * @param parent
    * @param name
    * @param pathname
    * @param type
    * @param value
    * @param revision
    * @param timestamp
    * @param orig
    */
   public DataImpl(DirectoryImpl parent, String name, String pathname, int type, Object value, int revision, long timestamp, boolean orig)
   {
      this.parent = parent;
      this.name = name;
      this.pathname = pathname;
      this.type = type;
      this.value = value;
      this.revision = revision;
      this.timestamp = timestamp;
      this.original = orig;
   }

   /**
    * @param pathname
    * @param type
    * @param value
    * @param persistent
    * @param broadcasted
    * @param lazyStorage
    * @param grouped
    * @param valuer
    */
   public DataImpl(String pathname, int type, Object value, boolean persistent, boolean broadcasted, boolean lazyStorage, boolean grouped, DataValuer valuer)
   {
      this.pathname = pathname;
      this.type = type;
      this.value = value;
      this.persistent = persistent;
      this.broadcasted = broadcasted;
      this.lazyStorage = lazyStorage;
      this.grouped = grouped;
      this.valuer = valuer;
   }

   /**
    * @param name
    * @param value
    * @param revision
    * @param previous
    * @param timestamp
    * @param type
    */
   protected DataImpl(String name, Object value, int revision, ArrayList previous, long timestamp, int type)
   {
      this.pathname = name;
      this.type = type;
      this.value = value;
      this.revision = revision;
      this.previousRevisions = previous;
      this.timestamp = timestamp;
   }

   public Object clone()
   {
      return clone(null);
   }

   protected abstract Object clone(DirectoryImpl dirParent);

   /**
    * @return Returns the relative name.
    */
   public String getName()
   {
      if ((name == null) && (pathname != null))
      {
         int i = -1;
         int e = -1;
         do
         {
            int j = pathname.indexOf('.', i+1);
            int k = pathname.indexOf('[', i+1);
            if (k > j) { j = k; }
            if (j == -1) break;
            i = j;
            e = pathname.indexOf(']', i+1);
         }
         while (true);
         // i : index du dernier '.' ou '[', e : index du dernier ']' ou -1
         name = (i == -1 ? pathname : (e == -1 ? pathname.substring(i+1) : pathname.substring(i+1, e)));
      }
      return name;
   }

   /**
    * @return Returns the pathname.
    */
   public String getPathname()
   {
      if (pathname == null)
      {
         if (parent == null)
         {
            pathname = name;
         }
         else
         {
            String parentpath = parent.getPathname();
            pathname = (((parentpath == null) || parentpath.isEmpty()) ? name : parentpath + (parent.getType() == TYPE_SPE_DIR ? "[" + name + "]" : "." + name));
         }
      }
      return pathname;
   }

   /**
    * @return Returns the parent directory.
    */
   public Directory getParent()
   {
      if ((parent == null) && original && (pathname != null) && !pathname.isEmpty() && (getName() != null)) // à la racine parent = null
      {
         int i = pathname.length() - name.length() - 1;
         if (!pathname.endsWith("."+name))
         {
            i = (pathname.endsWith("[" + name + "]") ? i-1 : -1);
         }
         parent = (DirectoryImpl)HomeSharedDataImpl.getRootDirectory();
         if (i != -1)
         {
            try
            {
               parent = (DirectoryImpl)parent.getData(pathname.substring(0, i));
            }
            catch (DataAccessException exc)
            {
            }
         }
      }
      return parent;
   }

   /**
    * @return Returns the type.
    */
   public int getType()
   {
      return type;
   }

   /**
    * @return Returns the revision.
    */
   public int getRevision()
   {
      return revision;
   }

   /**
    * @param revision The revision to set.
    */
   public void setRevision(int revision)
   {
      this.revision = revision;
   }

   /**
    * @param commited If new revision must be commited (i.e. no other change in this revision).
    * 
    * Assign new revision and timestamp.
    *
    */
   public void setNewRevision(boolean commited)
   {
      _setNewRevision(HomeSharedDataImpl.newLocalRevision(commited), System.currentTimeMillis());
   }

   /**
    * @param newRev The revision to set.
    * 
    * Set the new revision provided and notify.
    *
    */
   protected void _setNewRevision(int newRev, long time)
   {
      if (newRev != revision) // pour éviter de répéter le set
      {
         if ((revision != 0) // Ce n'est pas une data nouvellement créée
               && ((newRev & HomeSharedDataImpl.DEV_MASK) != (revision & HomeSharedDataImpl.DEV_MASK))) // alors pas le m�me device, positionner revision comme ant�rieure
         {
            Integer prev = _getPrevious(newRev);
            if (prev != null) { previousRevisions.remove(prev); } // on supprime d'abord la r�v du m�me device que newRev comme ant�rieure
            previousRevisions.add(new Integer(revision)); // on ajoute revision qui, d'apr�s l'invariant, n'a pas d�j� d�j� d'occurence avec le m�me device
         }
         revision = newRev;
         timestamp = time;
         _notifyChange();
         if (getParent() != null)
         {
            parent._setNewRevision(newRev, time);
         }
      }
      /*else // trace à surveiller !!
      {
         System.out.println("Double setRev name="+getPathname()+" rev="+revision);
      }*/
   }

   /**
    * @param rRevision The received revision to apply.
    * @param rPrevRevisions The received previous revisions to apply.
    */
   protected void setLaterRevision(int rRevision, ArrayList rPrevRevisions)
   {
      // on construit la meilleure liste previous
      if (previousRevisions == null) { previousRevisions = new ArrayList(); }
      if (((rRevision & HomeSharedDataImpl.DEV_MASK) != (revision & HomeSharedDataImpl.DEV_MASK)) && (revision != 0))
      {
         previousRevisions.add(new Integer(revision));
      }
      revision = rRevision;
      Integer prev = _getPrevious(revision);
      if (prev != null) { previousRevisions.remove(prev); }
      if (rPrevRevisions != null)
      {
         Iterator it = rPrevRevisions.iterator();
         while (it.hasNext())
         {
            Integer rprev = (Integer)it.next();
            prev = _getPrevious(rprev.intValue());
            if ((prev == null) || (prev.intValue() < rprev.intValue()))
            {
               if (prev != null) { previousRevisions.remove(prev); }
               previousRevisions.add(rprev);
            }
         }
      }
      timestamp = System.currentTimeMillis(); // réactualisation
      if (value != null) { _notifyChange(); }
   }

   /**
    * @param rRevision The received revision which is previous to this.
    * @param rPrevRevisions The received previous revisions which is previous to this.
    */
   protected void setRevisionAsPrevious(int rRevision, ArrayList rPrevRevisions)
   {
      if (rRevision != 0)
      {
         if ((rRevision & HomeSharedDataImpl.DEV_MASK) != (revision & HomeSharedDataImpl.DEV_MASK))
         {
            Integer prev = _getPrevious(rRevision);
            if ((prev == null) || (prev.intValue() < rRevision))
            {
               if (prev != null) { previousRevisions.remove(prev); }
               previousRevisions.add(new Integer(rRevision));
            }
         }
         if (rPrevRevisions != null)
         {
            Iterator it = rPrevRevisions.iterator();
            while (it.hasNext())
            {
               Integer rprev = (Integer)it.next();
               if ((rprev.intValue() & HomeSharedDataImpl.DEV_MASK) != (revision & HomeSharedDataImpl.DEV_MASK))
               {
                  Integer prev = _getPrevious(rprev.intValue());
                  if ((prev == null) || (prev.intValue() < rprev.intValue()))
                  {
                     if (prev != null) { previousRevisions.remove(prev); }
                     previousRevisions.add(rprev);
                  }
               }
            }
         }
      }
   }

   /**
    * @return Returns the timestamp.
    */
   public long getTimestamp()
   {
      return timestamp;
   }

   /**
    * @return Returns the persistent.
    */
   public boolean isPersistent()
   {
      return persistent;
   }

   /**
    * @return Returns the broadcasted.
    */
   public boolean isBroadcasted()
   {
      return broadcasted;
   }

   /**
    * @return Returns the lazyStorage.
    */
   public boolean isLazyStorage()
   {
      return lazyStorage;
   }

   /**
    * @return Returns the grouped.
    */
   public boolean isGrouped()
   {
      return grouped;
   }

   /**
    * @return Returns the valuer.
    */
   public DataValuer getValuer()
   {
      return valuer;
   }

   /**
    * @return the ignored
    */
   public boolean isIgnored()
   {
      return ignored;
   }

   /**
    * @param ignored the ignored to set
    */
   public void setIgnored(boolean ignored)
   {
      this.ignored = ignored;
   }

   private void _notifyChange()
   {
      if (original)
      {
         if ((listeners != null) && !listeners.isEmpty())
         {
            TaskManager.addTask(new NotifyTask(this));
         }
         if ((this == HomeSharedDataImpl.getRootDirectory())
               && _isPossiblyNewerThan(revision, peerRevision)) // on ne renvoit pas de msg de synchro avec la même révision que celle reçue
         {
            TaskManager.addTask(new SendTask(null, revision-1)); // floorRevision = revision-1 pour n'envoyer qu'un diff
         }
      }
   }

   public void notifyChange()
   {
      if (listeners != null)
      {
         Iterator it = listeners.iterator();
         while (it.hasNext())
         {
            ValueChangeListener listener = (ValueChangeListener)it.next();
            listener.valueChange(new EventObject(this));
         }
      }
   }

   public void addValueChangeListener(ValueChangeListener listener)
   {
      taskManager.lock();
      try
      {
         if (listeners == null)
         {
            listeners = new ArrayList();
         }
         if (!listeners.contains(listener))
         {
            listeners.add(listener);
         }
      }
      finally
      {
         taskManager.unlock();
      }
   }

   public void removeValueChangeListener(ValueChangeListener listener)
   {
      taskManager.lock();
      try
      {
         if (listeners != null)
         {
            listeners.remove(listener);
         }
      }
      finally
      {
         taskManager.unlock();
      }
   }

   /*
    * @param received newer than 'this'
    *
    * @return PENDING_FLAG|0 + MODIFIED_FLAG|0 + DIR_PARAM_FLAG|0
    */
   protected abstract int _replace(DataImpl received);

   /*
    * @param received newer than 'this'
    *
    * @return PENDING_FLAG|0 + MODIFIED_FLAG|0 + DIR_PARAM_FLAG|0
    */
   protected int _replaceThisBy(DataImpl received)
   {
      peerRevision = received.revision;
      peerPreviousRevisions = received.previousRevisions;
      requested = false;
      int cmp = _replace(received);
      if ((cmp & PENDING_FLAG) == 0) // fait dans replace ??
      {
         peerPreviousRevisions.clear(); // pour libérer la mémoire
      }
      synchroState &= CMP_MASK; // on garde l'info de comparaison
      synchroState |= cmp;
      return cmp;
   }

   /*
    * Relocate the data in the local SDS + Send messages if necessary to get the inner values
    *
    * @return 0 or PENDING_FLAG (synchroState initialized to 0)
    */
   protected abstract int _relocate();

   /*
    * 
    * @return Result of the synchronization :
    *                 2 => received < this (this not changed) => msg to send
    *                 0 => received == this (this not changed)
    *                 1 => received > this (this replaced by received)
    *  + QUESTION_FLAG  => received > this to signal (this replaced by received) => msg to send
    *  + DIR_PARAM_FLAG => received > this still to replace (this which is a DataImpl must be replaced by a Directory received)
    *  + PENDING_FLAG   => received > this + conditional
    *  + MODIFIED_FLAG  => received > this + modified
    *
    * Condition : received.value et value non null
    */
   protected abstract int _merge(DataImpl received, long lag) throws DataAccessException;

   /*
    * @param received data provided in message to synchronize with 'this'
    * @param lag timelag between devices, used to compare timestamp (if needed)
    * 
    * @return Result of the synchronization :
    *                 2 => received < this (this not changed) => msg to send from root
    *                 0 => received == this (this not changed)
    *                 1 => received > this (this replaced by received)
    *  + QUESTION_FLAG  => received > this to signal (this replaced by received) => msg to send from root (arbitrary ordering)
    *  + MODIFIED_FLAG  => created > received, this (this replaced by new revision created) => msg to send from root
    *  + DIR_PARAM_FLAG => received > this still to replace (DataImpl to be replaced by a Directory or vice versa, must be done by the parent)
    *  + IGNORED_FLAG   => received ignored
    *  + MODIFIED_FLAG + IGNORED_FLAG => created > this (not > received) => msg to send from root
    *  + PENDING_FLAG                 => + conditional (= pending)
    *  + PENDING_FLAG + MODIFIED_FLAG => + modified
    */
   protected int _synchronize(DataImpl received, long lag) throws DataAccessException
   {
      peerRevision = received.revision;
      peerPreviousRevisions = received.previousRevisions;
      requested = false;
      boolean revcmp = false;
      int cmp = _revcmp(received); // comparaison avec this
      if (cmp == 7) // received >< this (antagonistic)
      {
         // Choix arbitraire (mais sûr) de la révision pour laquelle le devId est < (l'autre est abandonnée)
         cmp = (revision < received.revision ? 2 : 1);
      }
      if ((cmp == 2) || (cmp == 0))  // received < this || received == this
      {
         setRevisionAsPrevious(received.revision, received.previousRevisions);
      }
      else if (cmp == 1) // received > this
      {
         cmp |= _replace(received);
      }
      else // if (cmp == 3) i.e. received <> this (non comparables)
      {
         if (((value == null) && (received.value == null)) || ignored) // à vérifier !!
         {
            cmp = 3; // (on ne change pas la valeur de cmp) La révision reçue est ignorée. Le device courant ne peut rien apporter au merge.
         }
         else if (received.value == null)
         {
            // merge à faire
            TaskManager.addTask(new SendTask(getPathname(), peerRevision, revision)); // interroge pour obtenir la valeur
            requested = true;
            cmp |= PENDING_FLAG; // peerRevision ignorée dans l'immédiat
         }
         else if (value == null) // && (received.value != null)
         {
            // on remplace la valeur locale par la valeur reçue. La rev locale devient l'expectedRevision pour terminer le merge.
            peerRevision = revision;
            revision = 0; // on efface pour ne pas traiter la révision actuelle comme antérieure
            previousRevisions.clear(); // id.
            cmp |= _replace(received);
            if ((cmp & DIR_PARAM_FLAG) != 0) { received.peerRevision = peerRevision; }
            TaskManager.addTask(new SendTask(getPathname(), peerRevision, revision)); // interroge pour obtenir la valeur manquante
            requested = true;
            revcmp = true;
            cmp |= MODIFIED_FLAG | PENDING_FLAG; // merge à faire. On devrait recevoir la peerRevision et alors une nouvelle rev sera créée > this et received.
            // Sinon, une nouvelle rev sera créée localement.
         }
         else // (value != null) && (received.value != null)
         {
            cmp = _merge(received, lag);
         }
      }
      if ((cmp & PENDING_FLAG) == 0)
      {
         peerPreviousRevisions.clear(); // pour libérer la mémoire
      }
      if (reversecmp) // le résultat de la synchro doit être inversé
      {
         int cmpop = cmp & CMP_MASK;
         if ((cmpop == 1) || (cmpop == 2))
         {
            cmpop = 3 - cmpop; // 2(<) <-> 1(>)
            cmp = (cmp & ~CMP_MASK) | cmpop; 
         }
      }
      synchroState = cmp;
      reversecmp = revcmp;
      return cmp;
   }

   /*
    */
   protected abstract String writeValueTo(String prefixName, DataOutputStream out, int floorRevision) throws IOException;

   protected String _writeTo(String prefixName, DataOutputStream out, int floorRevision) throws IOException
   {
      boolean isRequest = (floorRevision != 0) && requested && (floorRevision == peerRevision);
      boolean valueToWrite = !isRequest
            && (value != null)
            && ((type == TYPE_INT) || (type == TYPE_BOOL) // on écrit systématiquement les int et bool. Ca ne mange pas de pain !
                  || ((this instanceof DirectoryImpl) && ((HashMap)value).isEmpty()) // id. Ca ne coute rien d'écrire le contenu d'un directory vide !
                  || (_revcmp(floorRevision) == 2));          // == (this.revision > floorRevision)
      out.write(type);
      String nameToWrite = getPathname(); // == pathname
      if ((prefixName != null) && !prefixName.isEmpty())
      {
         String dots = "";
         while (!pathname.startsWith(prefixName))
         {
            int k= prefixName.length()-1; // >= 0
            while ((k>0) && (prefixName.charAt(k) != '.') && (prefixName.charAt(k) != '[')) { k--; }
            if (k == 0)
            {
               prefixName = "";
               dots = "";
               break;
            }
            else
            {
               prefixName = prefixName.substring(0, k);
               dots += ".";
            }
         }
         nameToWrite = dots + pathname.substring(prefixName.length());
      }
      out.writeUTF(nameToWrite);
      if (isRequest) // c'est une valeur requise dans la révision floorRevision
      {
         out.writeInt(peerRevision);
      }
      else
      {
         out.writeInt(revision);
         Iterator it = previousRevisions.iterator();
         while (it.hasNext())
         {
            Integer prev = (Integer)it.next();
            if (prev.intValue() > 0) { out.writeInt(prev.intValue()); }
         }
      }
      out.writeInt(0);
      out.writeLong(timestamp);
      out.writeBoolean(valueToWrite);
      // calcul du prochain prefixName
      if (this instanceof ParameterImpl)
      {
         prefixName = null;
         int len = pathname.length() - getName().length() - 1;
         if (len > 0)
         {
            prefixName = pathname.substring(0, len);
         }
      }
      else
      {
         prefixName = pathname;
      }
      if (valueToWrite)
      {
         prefixName = writeValueTo(prefixName, out, floorRevision);
      }
      return prefixName;
   }

   /*
    * @param out Output stream
    * @param name Name of the data to write
    * @param floorRevision Revision threshold to write value (if 'this' revision <= floorRevision then the value is not written) 
    */
   protected void writeTo(DataOutputStream out, int floorRevision) throws IOException
   {
      if ((floorRevision == -1) // si -1, envoi des dernieres modif
       || (_revcmp(floorRevision) != 2)) // si floorRevision n'est pas inférieure à la révision courante, on envoie qd même un niveau
      {
         floorRevision = (revision == 0 ? 0 : revision-1);
      }
      _writeTo(null, out, floorRevision);
   }

   static protected DataImpl readFrom(DataInputStream in) throws DataAccessException, IOException
   {
      DataImpl data = null;
      DirectoryImpl dir = null;
      int dirNameLen = 0;
      int dirType = TYPE_GEN_DIR;
      String prefixName = "";

      int type = 0;
      while ((byte)(type = in.read()) != -1)
      {
         String nameread = in.readUTF();
         String pathname = nameread;
         if (nameread.startsWith(".") || nameread.startsWith("["))
         {
            while (!prefixName.isEmpty() && (nameread.startsWith("..") || nameread.startsWith(".["))) // remonter prefixe d'un cran
            {
               int k= prefixName.length()-1; // >= 0
               while ((k>=0) && (prefixName.charAt(k) != '.') && (prefixName.charAt(k) != '[')) { k--; }
               prefixName = prefixName.substring(0, k);
               nameread = nameread.substring(1);
            }
            pathname = prefixName + nameread;
         }
         int rev = in.readInt();
         ArrayList prev = new ArrayList();
         int revp = 0;
         while ((revp = in.readInt()) != 0) { prev.add(new Integer(revp)); }
         long ts = in.readLong();
         boolean valueWritten = in.readBoolean();
         if ((type != TYPE_GEN_DIR) && (type != TYPE_SPE_DIR))
         {
            Object value = null;
            if (valueWritten)
            {
               String str = in.readUTF();
               if ((type == TYPE_INT) && !str.isEmpty()) { value = new Integer(str); }
               else if (type == TYPE_BOOL) { value = new Boolean(str); }
               else { value = str; }
            }
            data = new ParameterImpl(pathname, value, rev, prev, ts, type);
            if (dir != null)
            {
               String relName = pathname.substring(dirNameLen);
               if (dirType == TYPE_SPE_DIR)
               {
                  int k = relName.indexOf(']');
                  if (k >= 0)
                  {
                     relName = relName.substring(0, k) + relName.substring(k+1);
                  }
               }
               dir.putData(relName, data);
            }
            // calcul prochain prefixName
            int len = pathname.length() - data.getName().length() - 1;
            prefixName = (len > 0 ? pathname.substring(0, len) : "");
         }
         else
         {
            DirectoryImpl d = (valueWritten ? new DirectoryImpl(pathname, rev, prev, ts) :  new DirectoryImpl(pathname, null, rev, prev, ts));
            d.type = type;
            if (dir == null)
            {
               dir = d;
               dirNameLen = pathname.length();
               dirType = type;
               if (dirNameLen > 0) { dirNameLen++; }
            }
            else
            {
               String relName = pathname.substring(dirNameLen);
               if (dirType == TYPE_SPE_DIR)
               {
                  int k = relName.indexOf(']');
                  if (k >= 0)
                  {
                     relName = relName.substring(0, k) + relName.substring(k+1);
                  }
               }
               dir.putData(relName, d);
            }
            prefixName = pathname; // prochain prefixName
         }
      }
      return (dir != null ? dir : data);
   }

   protected static int _TREE_DISPLAY_MODE = 3;

   protected abstract String toStyledString(String prefix);
}
