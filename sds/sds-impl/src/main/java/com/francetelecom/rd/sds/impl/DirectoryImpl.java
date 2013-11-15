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

import java.io.IOException;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.sds.*;

/**
 * A directory belonging to the Shared DataImpl Structure
 *
 * @author goul5436
 */
public class DirectoryImpl extends DataImpl implements Directory
{
   // ---------------------------------------------------------------------
   // init logger

   private static final Logger logger = LoggerFactory.getLogger(DirectoryImpl.class.getName());

   // Données utiles uniquement aux Directories
   private boolean revisionToVerify = true;

   protected static String[] _EMPTY_STRING_ARRAY = new String[0];
   protected static Data[]   _EMPTY_DATA_ARRAY   = new Data[0];

   /**
    * @param parent
    * @param name
    * @param type
    */
   protected DirectoryImpl(DirectoryImpl parent, String name, int type)
   {
      super(parent, (name == null ? "" : name), new HashMap(), (type == 0 ? TYPE_GEN_DIR : type));
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
   public DirectoryImpl(DirectoryImpl parent, String name, String pathname, int type, Object value, int revision, long timestamp, boolean orig)
   {
      super(parent, name, pathname, type, value, revision, timestamp, orig);
   }

   /**
    * @param name
    * @param hashMap
    * @param revision
    * @param previous
    * @param timestamp
    */
   protected DirectoryImpl(String name, HashMap hashMap, int revision, ArrayList previous, long timestamp)
   {
      super(name, hashMap, revision, previous, timestamp, TYPE_GEN_DIR);
   }

   /**
    * @param name
    * @param revision
    * @param previous
    * @param timestamp
    */
   protected DirectoryImpl(String name, int revision, ArrayList previous, long timestamp)
   {
      super(name, new HashMap(), revision, previous, timestamp, TYPE_GEN_DIR);
   }

   public Object clone(DirectoryImpl dirParent)
   {
      DirectoryImpl dir = null;
      taskManager.lock();
      try
      {
         HashMap hashMapCopy = (value == null ? null : new HashMap());
         dir = new DirectoryImpl(dirParent, getName(), pathname, type, hashMapCopy, revision, timestamp, false);
         if (value != null)
         {
	         Iterator it = ((HashMap)value).keySet().iterator();
	         while (it.hasNext())
	         {
	            String key = (String)it.next();
	            DataImpl data = (DataImpl)((HashMap)value).get(key);
	            hashMapCopy.put(key, data.clone(dir));
	         }
         }
      }
      finally
      {
         taskManager.unlock();
      }
      return dir;
   }

   /**
    * Returns the value of a parameter.
    * 
    * @param pathname parameter name whose the value is requested.
    * 
    * @return the value requested.
    * 
    * @throws DataAccessException
    */
   public Object getParameterValue(String pathname) throws DataAccessException
   {
      Object res = null;
      taskManager.lock();
      try
      {
         DataImpl data = getData(pathname);
         if ((data != null) && (data instanceof ParameterImpl)) { res = ((ParameterImpl)data).getValueImpl(); }
         else { throw new DataAccessException((data == null ? DataAccessException.PATHNAME_NOT_FOUND : DataAccessException.NOT_A_PARAMETER), pathname); }
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   }

   /**
    * Returns the value of a parameter.
    * 
    * @param pathname parameter name whose the value is requested.
    * 
    * @return the value requested.
    * 
    * @throws DataAccessException
    */
   public String getParameterStringValue(String pathname) throws DataAccessException
   {
      String res = null;
      taskManager.lock();
      try
      {
         DataImpl data = getData(pathname);
         if (data == null)
         {
            throw new DataAccessException(DataAccessException.PATHNAME_NOT_FOUND, pathname);
         }
         if (data.type != Data.TYPE_STRING)
         {
            throw new DataAccessException(DataAccessException.NOT_A_STRING, pathname);
         }
         res = (String)((ParameterImpl)data).getValueImpl();
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   }

   /**
    * Returns the value of a parameter.
    * 
    * @param pathname parameter name whose the value is requested.
    * 
    * @return the value requested.
    * 
    * @throws DataAccessException
    */
   public int getParameterIntValue(String pathname) throws DataAccessException
   {
      int res = 0;
      taskManager.lock();
      try
      {
         DataImpl data = getData(pathname);
         if (data == null)
         {
            throw new DataAccessException(DataAccessException.PATHNAME_NOT_FOUND, pathname);
         }
         if (data.type != Data.TYPE_INT)
         {
            throw new DataAccessException(DataAccessException.NOT_AN_INTEGER, pathname);
         }
         Integer val = (Integer)((ParameterImpl)data).getValueImpl();
         if (val != null)
         {
            res = val.intValue();
         }
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   }

   /**
    * Returns the value of a parameter.
    * 
    * @param pathname parameter name whose the value is requested.
    * 
    * @return the value requested.
    * 
    * @throws DataAccessException
    */
   public boolean getParameterBooleanValue(String pathname) throws DataAccessException
   {
      boolean res = false;
      taskManager.lock();
      try
      {
         DataImpl data = getData(pathname);
         if (data == null)
         {
            throw new DataAccessException(DataAccessException.PATHNAME_NOT_FOUND, pathname);
         }
         if (data.type != Data.TYPE_BOOL)
         {
            throw new DataAccessException(DataAccessException.NOT_A_BOOLEAN, pathname);
         }
         Boolean val = (Boolean)((ParameterImpl)data).getValueImpl();
         if (val != null)
         {
            res = val.booleanValue();
         }
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   }

   /**
    * Sets the parameter with the provided value.
    * 
    * @param pathname parameter name
    * @param val new value to set
    *  
    * @throws DataAccessException
    */
   public void setParameterValue(String pathname, Object val) throws DataAccessException
   {
      taskManager.lock();
      try
      {
         _operateData(pathname.trim(), 2, val, 0);
      }
      finally
      {
         taskManager.unlock();
      }
   }

   /**
    * Returns the data requested.
    * 
    * @param pathname parameter name requested.
    * 
    * @return The data with the provided name.
    * 
    * @throws DataAccessException
    */
   protected DataImpl getData(String pathname) throws DataAccessException
   {
      return (pathname == null ? this : _operateData(pathname.trim(), 0, null, 0));
   }

   /**
    * Returns true if the named data can be accessed from this directory.
    * 
    * @param pathname pathname of a searched data.
    * 
    * @return true if the named data can be accessed from this directory.
    */
   public boolean contains(String pathname)
   {
      boolean res = false;
      taskManager.lock();
      try
      {
         res = (getData(pathname) != null);
      }
      catch (DataAccessException e)
      {
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   }

   protected DataImpl _operateData(String pathname, int operation, Object val, int type) throws DataAccessException
   {
      DataImpl res = null;
      if ((pathname == null) || pathname.isEmpty())
      {
         if (operation == 0) { res = this; }
      }
      else
      {
         int k = pathname.indexOf('.');
         int j = pathname.indexOf('[');
         int l = pathname.indexOf(']');
         boolean speDir = ((j != -1) && ((k==-1) || (j < k)));
         if (speDir) { k = j; }
         if (k != -1) // cas des pathnames terminés par "." ou "[]"
         {
            if (l != -1 ? (l < k) : speDir)
            {
               throw new DataAccessException(DataAccessException.INVALID_PATHNAME);
            }
            if (pathname.length() <= k + (speDir ? 2 : 1))
            {
               pathname = pathname.substring(0, k);
               k = -1;
               l = -1;
               int ty = (speDir ? Data.TYPE_SPE_DIR : Data.TYPE_GEN_DIR);
               if (type != ty)
               {
                  if (type != Data.TYPE_PARAM)
                  {
                     throw new DataAccessException(DataAccessException.WRONG_TYPE_DECLARATION);
                  }
                  type = ty;
               }
            }
         }
         if (k == -1) // c'est le dernier
         {
            if (pathname.isEmpty() || (l != -1))
            {
               throw new DataAccessException(DataAccessException.INVALID_PATHNAME);
            }
            HashMap hashMap = (HashMap)value;
            switch (operation)
            {
               case 0: // get
            	  if (hashMap != null)
            	  {
                     res = (DataImpl)hashMap.get(pathname);
            	  }
                  break;
               case 1: // put
                  if (!(val instanceof DataImpl))
                  {
                     throw new DataAccessException(DataAccessException.INTERNAL_ERROR);
                  }
                  res = (DataImpl)val;
                  if (hashMap == null)
                  {
                	  hashMap = new HashMap();
                	  value = hashMap;
                  }
                  hashMap.put(pathname, res);
                  break;
               case 2: // set
                  if (hashMap != null)
                  {
            	     res = (DataImpl)hashMap.get(pathname);
                  }
                  if (res == null)
                  {
                     throw new DataAccessException(DataAccessException.PATHNAME_NOT_FOUND, pathname);
                  }
                  if (!(res instanceof ParameterImpl))
                  {
                     throw new DataAccessException(DataAccessException.NOT_A_PARAMETER, pathname);
                  }
                  ((ParameterImpl)res).setValueImpl(val);
                  break;
               case 3: // delete
                  if (hashMap != null)
                  {
            	     res = (DataImpl)hashMap.remove(pathname);
                  }
                  if (res == null)
                  {
                     throw new DataAccessException(DataAccessException.PATHNAME_NOT_FOUND, pathname);
                  }
                  setNewRevision(true); // le directory courant a changé
                  break;
               case 4: // new (création dans le répertoire courant)
                  if (hashMap == null)
                  {
                     hashMap = new HashMap();
                 	 value = hashMap;
                  }
                  else
                  {
                     res = (DataImpl)hashMap.get(pathname);
                  }
                  if (res == null)
                  {
                	 switch (type)
                	 {
             	        case TYPE_GEN_DIR:
            	        case TYPE_SPE_DIR:
            	        	res = new DirectoryImpl(this, pathname, type);
            	        	break;
            	        case TYPE_INT:
            	        	res = new ParameterImpl(this, pathname, Integer.valueOf(0), type);
            	        	break;
            	        case TYPE_BOOL:
            	        	res = new ParameterImpl(this, pathname, Boolean.FALSE, type);
            	        	break;
            	        default: // TYPE_STRING, TYPE_PARAM
            	        	res = new ParameterImpl(this, pathname, "", type);
            	        	break;
                	 }
                     hashMap.put(pathname, res);
                     res.setNewRevision(true);
                  }
                  else
                  {
                     if (val == null) // !overwrite
                     {
                        throw new DataAccessException(DataAccessException.ALREADY_DEFINED, pathname);
                     }
                     else if (res.type != type) // overwrite allowed but with the same type
                     {
                        if (res instanceof ParameterImpl) // on essaie de convertir le paramètre dans le nouveau type
                        {
                           ((ParameterImpl)res).setType(type);
                        }
                        else
                        {
                           throw new DataAccessException(DataAccessException.ALREADY_DEFINED_AS_DIR, pathname);
                        }
                     }
                  }
                  break;
            }
         }
         else if (k == 0)
         {
            throw new DataAccessException(DataAccessException.INVALID_PATHNAME);
         }
         else
         {
            String dirName = pathname.substring(0, k);
            String nestName = pathname.substring(k+1);
            if (speDir)
            {
               j = nestName.indexOf(']');
               if (j != -1)
               {
                  nestName = nestName.substring(0,j) + nestName.substring(j+1);
               }
            }
            DataImpl dir = null;
            if (value != null)
            {
               dir = (DataImpl)((HashMap)value).get(dirName);
            }
            boolean newDir = (dir == null);
            if (newDir)
            {
               if (operation < 4) // si new, on créera les directory en remontant
               {
                  throw new DataAccessException(DataAccessException.NOT_A_DIRECTORY, dirName);
               }
               dir = new DirectoryImpl(this, dirName, (speDir ? Data.TYPE_SPE_DIR : Data.TYPE_GEN_DIR)); // création d'un répertoire intermédiaire
            }
            else
            {
               if (speDir)
               {
                  if (dir.type != Data.TYPE_SPE_DIR)
                  {
                     throw new DataAccessException(DataAccessException.NOT_A_SPECIFIC_DIR, dirName);
                  }
               }
               else
               {
                  if (dir.type != Data.TYPE_GEN_DIR)
                  {
                     throw new DataAccessException(DataAccessException.NOT_A_GENERIC_DIR, dirName);
                  }
               }
            }
            res = ((DirectoryImpl)dir)._operateData(nestName, operation, val, type);
            if (newDir) // arrivé là, on est sûr qu'il n'y a pas eu d'exception, on peut appliquer l'ajout et attacher le dir créé
            {
               if (value == null) { value = new HashMap(); }
               ((HashMap)value).put(dirName, dir);
            }
         }
      }
      return res;
   }

   /**
    * Adds a data with the provided name.
    * 
    * @param pathname name of the new data (parameter or node).
    * @param type type of the new data.
    * @param overwrite true to allow redefinition of a data already defined without throwing an exception.
    * 
    * @return the data newly created, a node if pathname ends with a dot, otherwise a parameter.
    * 
    * @throws DataAccessException
    */
   public Data newData(String pathname, int type, boolean overwrite) throws DataAccessException
   {
      DataImpl res = null;
      taskManager.lock();
      try
      {
         res = _operateData(pathname.trim(), 4, (overwrite ? this : null), type);
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   }

   protected void putData(String pathname, DataImpl newData) throws DataAccessException
   {
      taskManager.lockUnconditionally();
      try
      {
         _operateData(pathname.trim(), 1, newData, TYPE_PARAM);
      }
      finally
      {
         taskManager.unlock();
      }
   }

   /**
    * Adds a directory with the provided name.
    * 
    * @param pathname name of the directory (without the end dot)
    * 
    * @return The directory newly created
    * 
    * @throws DataAccessException
    */
   public DirectoryImpl newDirectory(String pathname) throws DataAccessException
   {
      DirectoryImpl res = null;
      taskManager.lock();
      try
      {
         res = (DirectoryImpl)_operateData(pathname.trim(), 4, null, TYPE_GEN_DIR);
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   }

   /**
    * Removes a data, parameter or directory.
    * 
    * @param pathname name of the data to remove
    * 
    * @return the data that was removed
    * 
    * @throws DataAccessException
    */
   public Data deleteData(String pathname) throws DataAccessException
   {
      Data res = null;
      taskManager.lock();
      try
      {
         res = _operateData(pathname.trim(), 3, null, 0);
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   } 

   /**
    * Returns the named child if it exists, or null if not.
    * 
    * @param pathname name of a data relatively to the current directory
    * 
    * @return Data the named child 
    */
   public Data getChild(String pathname) throws DataAccessException
   {
      Data res = null;
      taskManager.lock();
      try
      {
         res = getData(pathname);
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   }

   /**
    * Returns the named child directory if it exists, or null if not.
    * 
    * @param pathname name of a directory relatively to the current directory
    * 
    * @return Data the named directory
    * 
    * @throws DataAccessException If the pathname is incorrect or if the data is not a directory
    */
   public Directory getDirectory(String pathname) throws DataAccessException
   {
      Directory res = null;
      taskManager.lock();
      try
      {
         Data data = getData(pathname);
         if (data instanceof Directory)
         {
            res = (Directory)data;
         }
         else
         {
            throw new DataAccessException(DataAccessException.NOT_A_DIRECTORY, pathname);
         }
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   }

   /**
    * Returns the named child parameter if it exists, or null if not.
    * 
    * @param pathname name of a parameter relatively to the current directory
    * 
    * @return Data the named parameter
    * 
    * @throws DataAccessException If the pathname is incorrect or if the data is not a parameter
    */
   public Parameter getParameter(String pathname) throws DataAccessException
   {
      Parameter res = null;
      taskManager.lock();
      try
      {
         Data data = getData(pathname);
         if (data instanceof Parameter)
         {
            res = (Parameter)data;
         }
         else
         {
            throw new DataAccessException(DataAccessException.NOT_A_PARAMETER, pathname);
         }
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   }

   /**
    * Returns the children of a directory.
    * 
    * @param pathname name of a directory
    * 
    * @return array of children
    * 
    * @throws DataAccessException
    */
   public String[] getChildNames(String pathname) throws DataAccessException
   {
      String[] res = _EMPTY_STRING_ARRAY;
      taskManager.lock();
      try
      {
         DataImpl dir = ((pathname == null) || (pathname.isEmpty()) ? this : getData(pathname));
         if (dir instanceof Directory)
         {
            HashMap hashMap = (HashMap)((DirectoryImpl)dir).value;
            Set keys = hashMap.keySet();
            res = new String[keys.size()];
            int i = 0;
            Iterator it = keys.iterator();
            while (it.hasNext())
            {
               res[i] = (String)it.next();
               /*String key = (String)it.next();
               DataImpl data = (DataImpl)hashMap.get(key);
               res[i] = " " + key + (data instanceof Directory ? ". [" : " [") + data.revisionToString() + "]";*/
               i++;
            }
         }
         else
         {
            throw new DataAccessException(DataAccessException.NOT_A_DIRECTORY, pathname);
         }
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   }

   /**
    * Returns the children of this directory
    * 
    * @return array of Data children of the current directory
    */
   public Data[] getChildren()
   {
      Data[] res = _EMPTY_DATA_ARRAY;
      taskManager.lock();
      try
      {
         if (value != null)
         {
            Collection values = ((HashMap)value).values();
            res = new Data[values.size()];
            Iterator it = values.iterator();
            for (int i=0; i<res.length; i++)
            {
               res[i] = (Data)it.next();
            }
         }
      }
      finally
      {
         taskManager.unlock();
      }
      return res;
   }

   /**
    * Adds the specified listener to received notification events when the value of the provided parameter changes.
    *  
    * @param pathname name of the monitored parameter.
    * 
    * @param listener the ValueChangeListener
    * 
    * @throws DataAccessException
    */
   public void addValueChangeListener(String pathname, ValueChangeListener listener) throws DataAccessException
   {
      taskManager.lock();
      try
      {
         getData(pathname).addValueChangeListener(listener);
      }
      finally
      {
         taskManager.unlock();
      }
   }

   /**
    * Removes the listener previously specified.
    * 
    * @param pathname name of the monitored parameter.
    * 
    * @param listener listener previously specified
    * 
    * @throws DataAccessException
    */
   public void removeValueChangeListener(String pathname, ValueChangeListener listener) throws DataAccessException
   {
      taskManager.lock();
      try
      {
         getData(pathname).removeValueChangeListener(listener);
      }
      finally
      {
         taskManager.unlock();
      }
   }

   /*
    * Relocate the data in the local SDS + Send messages if necessary to get the inner values
    *
    * @return 0 or PENDING_FLAG or PENDING_FLAG|REQUESTED_FLAG (synchroState initialized to 0)
    */
   protected void _relocate()
   {
      name = null;
      parent = null;
      if (!ignored)
      {
         if (value == null)
         {
            synchroState = PENDING_FLAG | REQUESTED_FLAG;
            TaskManager.addTask(new SendTask(getPathname(), revision, 0)); // interroge pour obtenir la valeur
         }
         else // value != null
         {
            HashMap hashMap = (HashMap)value;
            Iterator it = hashMap.keySet().iterator();
            while (it.hasNext())
            {
               DataImpl data = (DataImpl)hashMap.get(it.next());
               data._relocate();
               synchroState |= data.synchroState & PENDING_FLAG;
            }
         }
      }
   }

   /*
    * @param sync Résultat de la synchro sans modif ou résult après modif si modif
    */
   private void _computeSynchro()
   {
      int cmp = synchroState & CMP_MASK;
      boolean modified = ((synchroState & MODIFIED_FLAG) != 0);
      if (((synchroState & PENDING_FLAG) == 0) && (cmp == 3)) // nlle révision à créer
      {
         modified = true;
         cmp = 2; // peerRev < this (modified rev) 
      }
      if ((((cmp == 0) && (revision < peerRevision)) || (cmp == 2)) && !modified) // received <= this sans modif, si == on choisit la révision dont le devId est le plus petit
      {
         setRevisionAsPrevious(peerRevision, peerPreviousRevisions);
      }
      else if ((cmp & 2) == 0) // received >= this, après replace
      {
         setLaterRevision(peerRevision, peerPreviousRevisions);
      }
      else if (modified) // nlle révision créée. Sinon on ignore
      {
         setNewRevision(false);
         if (cmp != 3) // nlle révision créée > received
         {
            setRevisionAsPrevious(peerRevision, peerPreviousRevisions);
         }
      }
      // sinon on laisse la donnée en l'état
   }

   /*
    * La révision attendue a été affectée au moins en partie
    *
    * @param sync New sync result for child to integrate
    */
   private void _updateIfNoLongerPending()
   {
      // PRECOND : (value != null) && (peerRevision != 0)
      // Y a-t-il encore un fils en attente ?
      boolean pending = false;
      HashMap hashMap = (HashMap)value;
      Iterator it = hashMap.keySet().iterator();
      while (!pending && it.hasNext())
      {
         String key = (String)it.next();
         DataImpl data = (DataImpl)hashMap.get(key);
         pending = ((data.synchroState & PENDING_FLAG) != 0);
      }
      if (!pending) // plus aucun fils en attente
      {
         synchroState &= ~PENDING_FLAG;
         _computeSynchro();
         synchroState = 0;
         peerPreviousRevisions.clear();
         if (getParent() != null)
         {
            parent._updateIfNoLongerPending();
         }
      }
   }

   /*
    * @param received newer than 'this'
    *
    * @return PENDING_FLAG|0 + MODIFIED_FLAG|0 + DIR_PARAM_FLAG|0
    */
   protected void _replace(DataImpl received)
   {
      if (received instanceof Directory)
      {
         type = received.type;
         boolean thisEmpty = (value == null) || ((HashMap)value).isEmpty();
         if ((received.value != null) && !thisEmpty)
         {
            HashMap hashMap = (HashMap)value;
            HashMap hashMapRcvd = (HashMap)received.value;
            // on supprime les éléments supprimés dans la nouvelle révision
            while (true)
            {
               String found = null;
               Iterator it1 = hashMap.keySet().iterator();
               while (it1.hasNext())
               {
                  String key = (String)it1.next();
                  if (hashMapRcvd.get(key) == null)
                  {
                     found = key;
                     break;
                  }
               }
               if (found == null)
               {
                  break;
               }
               hashMap.remove(found);
               synchroState |= MODIFIED_FLAG;
            }
            // on synchronise tous les éléments restants
            Iterator it2 = hashMapRcvd.keySet().iterator();
            while (it2.hasNext())
            {
               String key = (String)it2.next();
               DataImpl elemRcvd = (DataImpl)hashMapRcvd.get(key);
               DataImpl elem = (DataImpl)hashMap.get(key);
               boolean aGreffer = (elem == null);
               if (!aGreffer)
               {
                  int cmp = elem._revcmp(elemRcvd);
                  if ((cmp == 1) || (cmp == 3)) // elemRcvd > elem (ou non comparable), sinon aucun remplacement à faire
                  {
                     elem._replaceThisBy(elemRcvd); // on met à jour récursivement
                     synchroState |= elem.synchroState & UPWARD_MASK;
                     aGreffer = ((elem.synchroState & DIR_PARAM_FLAG) != 0);
                  }
               }
               if (aGreffer)
               {
                  elemRcvd._relocate();
                  synchroState |= (elemRcvd.synchroState & PENDING_FLAG) | MODIFIED_FLAG;
                  hashMap.put(key, elemRcvd); // on ajoute tout simplement, élément ajouté ds la rév plus récente ou on remplace un dir par un param
               }
            }
            if ((synchroState & PENDING_FLAG) == 0) // on verifie qu'on n'a pas le flag PENDING
            {
               setLaterRevision(received.revision, received.previousRevisions);
               synchroState |= MODIFIED_FLAG;
            }
         }
         else if (!thisEmpty) // ET received.value == null
         {
            TaskManager.addTask(new SendTask(getPathname(), received.revision, revision)); // interroge pour obtenir la valeur
            synchroState |= PENDING_FLAG | REQUESTED_FLAG;
         }
         else //thisEmpty
         {
            received._relocate();
            synchroState |= (received.synchroState & PENDING_FLAG);
            if ((revision != 0) && (received.revision != revision))
            {
               //synchroState |= (received.synchroState & PENDING_FLAG) | MODIFIED_FLAG;
               synchroState |= MODIFIED_FLAG;
            }
            value = received.value; // éventuellement null
            if (((synchroState & PENDING_FLAG) == 0) // on verifie qu'on n'a pas le flag PENDING
               || (revision == 0)) // cas root non initialisée
            {
               setLaterRevision(received.revision, received.previousRevisions);
            }
         }
      }
      else if (received.value == null) // ET received instanceof Parameter
      {
         TaskManager.addTask(new SendTask(getPathname(), received.revision, revision)); // interroge pour obtenir la valeur
         synchroState |= PENDING_FLAG | REQUESTED_FLAG;
      }
      else // received instanceof Parameter
      {
         synchroState |= DIR_PARAM_FLAG;
      }
   }

   /*
    * @return Result of the synchronization
    *
    *  Condition : (value != null) && (received.value != null)
    */
   protected void _merge(DataImpl received, long lag) throws DataAccessException
   {
      synchroState = 0;
      if (!(received instanceof Directory)) // on garde plutôt le Directory
      {
         synchroState = 2;
      }
      else
      {
         HashMap hashMap = (HashMap)value;
         HashMap hashMapRcvd = (HashMap)received.value;
         int lenThis = hashMap.size();
         int covered = 0;
         Iterator it = hashMapRcvd.keySet().iterator();
         while (it.hasNext())
         {
            int cmpi = 0;
            String key = (String)it.next();
            DataImpl elemRcvd = (DataImpl)hashMapRcvd.get(key);
            DataImpl elem = (DataImpl)hashMap.get(key);
            boolean aGreffer = (elem == null);
            if (!aGreffer)
            {
               elem._synchronize(elemRcvd, lag);
               cmpi = elem.synchroState;
               aGreffer = ((cmpi & DIR_PARAM_FLAG) != 0); // l'élément reçu est un Directory et remplace une DataImpl, ou l'inverse
               covered++;
            }
            if (aGreffer) // élément à ajouter, considéré comme + récent
            {
               elemRcvd._relocate();
               cmpi = /*1 |*/ elemRcvd.synchroState; // l'élément reçu est + récent
               synchroState |= MODIFIED_FLAG;
               hashMap.put(key, elemRcvd);
            }
            if ((cmpi & PENDING_FLAG) != 0) { synchroState |= PENDING_FLAG; } // on ne remonte à ce moment-là que le PENDING. Le résultat définitif sera remonté lors d'une prochaine synchro ou par un _makeStable.
            else { synchroState |= (cmpi & UPWARD_MASK); }
         }
         if (covered < lenThis) // this contient des éléments en plus, ce qui le ferait considéré comme + récent
         {
            synchroState |= 2;
         }
         else if (synchroState == 0) // rev égales
         {
            synchroState = (received.revision < revision // pour garantir une rev unique, on choisit l'id de device le plus petit
                                              ? 1   // received considérée comme plus récente
                                              : 2); // création d'une nlle révision
         }
      }
      if ((synchroState & PENDING_FLAG) == 0) // plus aucun fils en attente
      {
         _computeSynchro();
      }
   }

   protected boolean isExpected(DataImpl received) throws DataAccessException
   {
      taskManager.lockUnconditionally();
      try
      {
         DataImpl dest = getData(received.getPathname());
         return ((dest != null)
               && (((_revcmp(received.revision, dest.revision) == 0) && (dest.value == null) && !dest.ignored) // on récupère la valeur qu'on ne connaissait pas
                  || (((dest.synchroState & REQUESTED_FLAG) != 0) && (_revcmp(received.revision, dest.peerRevision) == 0))) );
      }
      finally
      {
         taskManager.unlock();
      }
   }

   /**
    * Request data for given pathname in the expected revision
    *
    * @param pathname pathname of the data requested
    * @param expectedRevision expected revision, 0 : any revisions. Only the expected revision must be returned.
    * @param floorRevision floor revision for the sub-data returned, 0 : no floor revision, -1 : expectedRevision-1.
    * @throws DataAccessException
    */
   public void respondToRequestForData(String pathname, int expectedRevision, int floorRevision) throws DataAccessException
   {
      if (original) // pas de synchro sur une copie
      {
         taskManager.lockUnconditionally();
         try
         {
            boolean okToRespond = (expectedRevision == 0); // dans le cas 0, on envoie de toute façon la dernière révision, à moins qu'on ait pas encore de données locales (revision == 0)
            if (!okToRespond) // alors on vérifie qu'on a la donnée dans une révision attendue
            {
               DataImpl data = null;
               try
               {
                  data = getData(pathname);
               }
               catch (DataAccessException exc)
               {
               }
               if (data != null)
               {
                  if (data.revision == expectedRevision) // data == expected, c'est exactement ce qu'il nous faut
                  {
                     okToRespond = (data.value != null); // il faut tout de même avoir la valeur demandée (sinon on ne répond pas)
                  }
                  else // soit révision reçue ancienne => maj distante nécessaire (depuis la racine)
                       // soit révision reçue plus récente => maj locale nécessaire (depuis la racine)
                       // soit révisions incomparables => synchro nécessaire
                  {
                     TaskManager.addTask(new SendTask(null, -1)); // envoi de la dernière rev root locale pour synchro
                  }
               }
            }
            if (okToRespond && (revision != 0)) // si on a rien localement, ce n'est pas la peine
            {
               TaskManager.addTask(new SendTask(pathname, floorRevision));
            }
         }
         finally
         {
            taskManager.unlock();
         }
      }
   }

   protected static boolean isInStableState()
   {
      DirectoryImpl root = (DirectoryImpl)HomeSharedDataImpl.getRootDirectory();
      return ((root == null) || ((root.synchroState & PENDING_FLAG) == 0));
   }

   protected static void ensureStability()
   {
      DirectoryImpl root = (DirectoryImpl)HomeSharedDataImpl.getRootDirectory();
      if ((root != null) && ((root.synchroState & PENDING_FLAG) != 0))
      {
         // avant une opération nécessitant un état stable
         logger.info("FORCED EXIT OF TRANSITION STATE");
         root._makeStable();
         HomeSharedDataImpl.commitLocalRevision();
      }
   }

   public void synchronize(DataImpl received, long lag) throws DataAccessException
   {
      if (original) // pas de synchro sur une copie
      {
         taskManager.lockUnconditionally();
         try
         {
            DataImpl dest = null;
            try
            {
               dest = getData(received.getPathname());
            }
            catch (DataAccessException exc)
            {
            }
            if (dest != null) // sinon on laisse tomber !!
            {
               DirectoryImpl root = (DirectoryImpl)HomeSharedDataImpl.getRootDirectory();
               int floorRevision = -2; // pour envoyer une réponse depuis la racine
               int synchro = _revcmp(received.revision, dest.revision); // 2 : received < dest, 0 : received == dest, 1 : received > dest, 3 : received <> dest
   
               // 2 cas possibles :
               // 1) synchro avec une nouvelle révision depuis la racine => s'applique uniquement sur un état stable => stabiliser d'abord l'état
               // 2) synchro avec une révision attendue par le milieu (pas la racine) : soit 2.1) pour récupérer une donnée attendue, soit 2.2) pour compléter un SDS partiel
   
               if (dest == root) // cas 1)
               {
                  if ((root.synchroState & PENDING_FLAG) != 0) // synchro en cours
                  {
                     synchro = 0; // pour l'instant, on évite ce cas !!
                     /*if (received._revcmp(peerRevision) == 2) // si received > peerRevision, on démarre une nouvelle synchro avec cette version + récente
                     {
                        ensureStability();
                        synchro = 1;
                     }
                     else // sinon on ignore
                     {
                        synchro = 0;
                     }*/
                  }
                  if ((synchro == 1) || (synchro ==3)) // en écartant le cas ==, on ignore les messages répétés
                  {
                     // s'assurer que la rev de base locale est bien supérieure à celle reçue pour le même deviceId
                     if (revisionToVerify) // pour prendre en compte le cas d'un arbre (ré)initialisé ou dont le sds.data ne reflète pas le tout dernier état 
                     {
                        int nextRev = HomeSharedDataImpl.getBaseRevision();
                        int baseRev = 0;
                        if ((received.revision & HomeSharedDataImpl.DEV_MASK) == (nextRev & HomeSharedDataImpl.DEV_MASK))
                        {
                           baseRev = received.revision;
                        }
                        else
                        {
                           Integer prev = received._getPrevious(nextRev);
                           if (prev != null)
                           {
                              baseRev = prev.intValue();
                           }
                        }
                        if (baseRev >= nextRev) { HomeSharedDataImpl.setBaseRevision(baseRev); }
                        revisionToVerify = false;
                     }
                     dest._synchronize(received, lag);
                     synchro = dest.synchroState;
                     if ((synchro & PENDING_FLAG) == 0) // synchro terminée
                     {
                        HomeSharedDataImpl.commitLocalRevision();
                     }
                  }
                  if ((synchro & PENDING_FLAG) == 0)
                  {
                     if ((synchro & QUESTION_FLAG) != 0)
                     {
                        floorRevision = received.revision-1; // on remonte à partir de received.revision (>=) pour confirmation du choix
                     }
                     else if ((synchro & 2) != 0) // received.revision < revision, avant synchro ou après création par synchro d'une nlle rev locale
                     {
                        floorRevision = received.revision;
                     }
                  }
               }
               else // cas 2)
               {
                  // on appelle donnée attendue, une donnée qui vient compléter (par le milieu) l'arbre SDS, et qui peut s'appliquer sur un état transitoire
                  // on accepte ainsi 2 cas de synchro : 2.1) avec une donnée correspondant à une peerRevision (pendant un état transitoire)
                  // 2.2) avec une donnée (dans la révision attendue) dont la valeur n'avait pas encore été récupérée (aussi bien pendant un état stable que transitoire)
   
                  DirectoryImpl dir = null;
                  if ((synchro == 0) && (dest.value == null)) // cas 2.2) on récupère la valeur qui n'est pas encore stockée localement
                  {
                     if (!dest.ignored) // sauf si on ne veut pas stocker localement
                     {
                        dir = (DirectoryImpl)dest.getParent();
                        dest._replaceThisBy(received);
                     }
                  }
                  else if ((dest.synchroState & REQUESTED_FLAG) != 0) // sinon on a déjà reçu alors on ignore
                  {
                     synchro = _revcmp(received.revision, dest.peerRevision);
                     if (synchro == 0) // cas 2.1) c'est exactement la révision attendue
                     {
                        dir = (DirectoryImpl)dest.getParent();
                        if ((dest.synchroState & CMP_MASK) == 1) // on sait déjà que c'est un replace
                        {
                           dest._replaceThisBy(received);
                        }
                        else
                        {
                           dest._synchronize(received, lag); // que deviennent les éventuels conditionals de dest ??
                        }
                     }
                     else if ((synchro & 1) != 0)
                     {
                        // la révision reçue est potentiellement supérieure à celle attendue, c'est donc que les noeuds parents ne sont pas à jour
                        // => envoi d'un message avec la révision locale de la racine pour déclencher une mise à jour
                        floorRevision = -1;
                     }
                  }
                  if (dir != null)
                  {
                     if ((dest.synchroState & DIR_PARAM_FLAG) != 0)
                     {
                        received._relocate();
                        dest.synchroState &= ~DIR_PARAM_FLAG;
                        dest.synchroState |= (received.synchroState & PENDING_FLAG) | MODIFIED_FLAG;
                        ((HashMap)dir.value).put(dest.name, received);
                     }
                     if ((dest.synchroState & PENDING_FLAG) == 0)
                     {
                        dir._updateIfNoLongerPending(); // différents cas : on reste dans l'état conditionnel, remplacement, merge, ...
                     }
                     if ((root.synchroState & PENDING_FLAG) == 0) // synchro terminée
                     {
                        HomeSharedDataImpl.commitLocalRevision();
                        if ((root.synchroState & QUESTION_FLAG) != 0)
                        {
                           floorRevision = -1; // on envoie la dernière rev pour confirmation
                        }
                        else if ((root.synchroState & 2) != 0) // received.revision < revision, avant synchro ou après création par synchro d'une nlle rev locale
                        {
                           floorRevision = root.peerRevision;
                        }
                     }
                  }
               }
               if (floorRevision != -2)
               {
                  TaskManager.addTask(new SendTask(null, floorRevision));
               }
            }
         }
         finally
         {
            taskManager.unlock();
         }
      }
   }

   /*
    * @return Retourne le résultat de synchro (avant raz) pour le _makeStable appelant
    */
   private void _makeStable()
   {
      if ((synchroState & PENDING_FLAG) != 0)
      {
         if (value != null) // sinon valeur restant inconnue, on laisse tomber
         {
            if ((synchroState & REQUESTED_FLAG) != 0) { synchroState |= 3; }

            HashMap hashMap = (HashMap)value;
            Iterator it = hashMap.keySet().iterator();
            while (it.hasNext())
            {
               String key = (String)it.next();
               DataImpl data = (DataImpl)hashMap.get(key);
               if ((data.synchroState & PENDING_FLAG) != 0)
               {
                  boolean requested = ((data.synchroState & REQUESTED_FLAG) != 0);
                  if (requested && (data.value != null))
                  {
                     // on ne fait plus de requête directe sur cette donnée
                     TaskManager.removePendingRequest(getPathname(), 0);
                  }
                  if (data instanceof ParameterImpl)
                  {
                     if (data.value != null) // sinon valeur restant inconnue, on laisse tomber
                     {
                        if (requested) { synchroState |= 3; }
                     }
                     data.synchroState &= ~PENDING_FLAG;
                     data.peerPreviousRevisions.clear();
                  }
                  else
                  {
                     ((DirectoryImpl)data)._makeStable();
                  }
                  synchroState |= data.synchroState & UPWARD_MASK;
               }
            }
            _computeSynchro();
         }
         synchroState &= ~PENDING_FLAG;
         peerPreviousRevisions.clear();
      }
   }

   /*
    */
   protected String writeValueTo(String prefixName, DataOutputStream out, int floorRevision) throws IOException
   {
      HashMap hashMap = (HashMap)value;
      Iterator it = hashMap.keySet().iterator();
      while (it.hasNext())
      {
         String key = (String)it.next();
         DataImpl data = (DataImpl)hashMap.get(key);
         prefixName = data._writeTo(prefixName, out, floorRevision);
      }
      return prefixName;
   }

   protected String toStyledString(String prefix)
   {
      String line = prefix + " " +  getName() + (type == TYPE_GEN_DIR ? "." : "[]");
      if ((_TREE_DISPLAY_MODE & 2) != 0) { line += " " + fullRevisionToString(); }

      if (value != null)
      {
         String pr = (prefix.isEmpty() ? "\n \u2514\u2500\u2500" : "\n    " + prefix.substring(1));
         Iterator it = ((HashMap)value).values().iterator();
         while (it.hasNext())
         {
        	 line += ((DataImpl)it.next()).toStyledString(pr);
         }
      }
      return line;
   }
}
