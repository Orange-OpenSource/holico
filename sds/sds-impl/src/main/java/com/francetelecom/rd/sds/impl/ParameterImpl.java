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
import java.io.IOException;

import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataEvent;
import com.francetelecom.rd.sds.Parameter;
import com.francetelecom.rd.sds.DataAccessException;

/**
 * @author GOUL5436
 *
 */
public class ParameterImpl extends DataImpl implements Parameter
{

   /**
    * @param parent
    * @param name
    * @param type
    */
   public ParameterImpl(DirectoryImpl parent, String name, Object value, int type)
   {
      super(parent, name, value, type);
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
   public ParameterImpl(DirectoryImpl parent, String name, String pathname, int type, Object value, int revision, long timestamp, boolean orig)
   {
      super(parent, name, pathname, type, value, revision, timestamp, orig);
   }

   /**
    * @param name
    * @param value
    * @param revision
    * @param previous
    * @param timestamp
    * @param type
    */
   protected ParameterImpl(String name, Object value, int revision, java.util.ArrayList previous, long timestamp, int type)
   {
      super(name, value, revision, previous, timestamp, type);
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
   protected ParameterImpl(String pathname, int type, Object value, boolean persistent, boolean broadcasted, boolean lazyStorage, boolean grouped,
         DataValuer valuer)
   {
      super(pathname, type, value,persistent, broadcasted, lazyStorage, grouped, valuer);
   }

   protected Object clone(DirectoryImpl dirParent)
   {
      ParameterImpl param = null;
      boolean largest = taskManager.lock();
      try
      {
         param = new ParameterImpl(dirParent, getName(), pathname, type, value, revision, timestamp, false);
      }
      finally
      {
         taskManager.unlock(largest);
      }
      return param;
   }

   /* (non-Javadoc)
    * @see com.francetelecom.rd.sds.Parameter#getValue()
    */
   public Object getValue()
   {
      return getValueImpl();
   }

   /**
    * @return Returns the int value.
    */
   public int getIntValue() throws DataAccessException
   {
      int res = 0;
      if (type != Data.TYPE_INT)
      {
         throw new DataAccessException(DataAccessException.NOT_AN_INTEGER, getPathname());
      }
      Integer val = (Integer)getValueImpl();
      if (val != null)
      {
         res = val.intValue();
      }
      return res;
   }

   /**
    * @return Returns the boolean value.
    */
   public boolean getBooleanValue() throws DataAccessException
   {
      boolean res = false;
      if (type != Data.TYPE_BOOL)
      {
         throw new DataAccessException(DataAccessException.NOT_A_BOOLEAN, getPathname());
      }
      Boolean val = (Boolean)getValueImpl();
      if (val != null)
      {
         res = val.booleanValue();
      }
      return res;
   }

   /**
    * @return Returns the string alue.
    */
   public String getStringValue() throws DataAccessException
   {
      if (type != Data.TYPE_STRING)
      {
         throw new DataAccessException(DataAccessException.NOT_A_STRING, getPathname());
      }
      return (String)getValueImpl();
   }

   /* (non-Javadoc)
    * @see com.francetelecom.rd.sds.Parameter#setValue(java.lang.Object)
    */
   public void setValue(Object value) throws DataAccessException
   {
      boolean largest = taskManager.lock();
      try
      {
         setValueImpl(value);
      }
      finally
      {
         taskManager.unlock(largest);
      }
   }

   protected void setType(int type) throws DataAccessException
   {
      this.value = _convertValue(this.value, type, true);
      this.type = type;
      setNewRevision(new DataEvent(this, DataEvent.TYPE_CHANGED, null));
   }

   /**
    * @return Returns the value.
    */
   protected Object getValueImpl()
   {
      if ((valuer != null) && ((value == null) || !persistent))
      {
         value = valuer.getValue("");
      }
      return value;
   }

   /**
    * @param value The value to set.
    */
   protected void setValueImpl(Object value) throws DataAccessException
   {
      this.value = _convertValue(value, type, false);
      setNewRevision(new DataEvent(this, DataEvent.VALUE_CHANGED, null));
   }

   /**
    *
    */
   private static Object _convertValue(Object val, int type, boolean changingType) throws DataAccessException
   {
      if ((type == TYPE_GEN_DIR) || (type == TYPE_SPE_DIR))
      {
         throw new DataAccessException(DataAccessException.UNEXPECTED_TYPE);
      }
      if (val != null)
      {
         switch (type)
         {
            case TYPE_INT:
               if (val instanceof String)
               {
                  try
                  {
                     val = new Integer((String)val);
                  }
                  catch (NumberFormatException exc)
                  {
                  }
               }
               else if (changingType && (val instanceof Boolean))
               {
                  val = new Integer(((Boolean)val).booleanValue() ? 1 : 0);
               }
               if (!(val instanceof Integer))
               {
                  throw new DataAccessException(DataAccessException.INTEGER_EXPECTED);
               }
               break;
            case TYPE_BOOL:
               if (val instanceof String)
               {
                  val = new Boolean((String)val);
               }
               else if (changingType && (val instanceof Integer))
               {
                  val = (((Integer)val).intValue() == 0 ? Boolean.FALSE : Boolean.TRUE);
               }
               else if (!(val instanceof Boolean))
               {
                  throw new DataAccessException(DataAccessException.BOOLEAN_EXPECTED);
               }
               break;
            case TYPE_STRING:
               val = val.toString();
               break;
            case TYPE_PARAM:
               break;
            default:
               throw new DataAccessException(DataAccessException.UNEXPECTED_VALUE);
         }
      }
      return val;
   }

   /*
    * @param received newer than 'this'
    *
    * @return PENDING_FLAG|0 + MODIFIED_FLAG|0 + DIR_PARAM_FLAG|0
    */
   protected void _replace(DataImpl received)
   {
      if ((received instanceof ParameterImpl) && ((received.value != null) || (value == null)))
      {
         if ((revision != 0) && (received.revision != revision))
         {
            synchroState |= MODIFIED_FLAG;
            addDataEvent(new DataEvent(this, (type != received.type ? DataEvent.TYPE_CHANGED : DataEvent.VALUE_CHANGED), null));
         }
         value = received.value;
         type = received.type;
         setLaterRevision(received.revision, received.previousRevisions);
         if ((value == null) && !ignored)
         {
            synchroState |= PENDING_FLAG | REQUESTED_FLAG;
            TaskManager.addTask(new SendTask(getPathname(), revision, 0)); // interroge pour obtenir la valeur
         }
      }
      else if (received.value == null) // (received instanceof DirectoryImpl) || received.value == null ET value != null (donc !ignored)
      {
         TaskManager.addTask(new SendTask(getPathname(), received.revision, 0)); // interroge pour obtenir la valeur
         synchroState |= PENDING_FLAG | REQUESTED_FLAG;
      }
      else // received instanceof DirectoryImpl && (received.value != null)
      {
         synchroState |= DIR_PARAM_FLAG;
      }
   }

   /*
    * Relocate the data in the local SDS + Send messages if necessary to get the inner values
    *
    * @return 0 or PENDING_FLAG (synchroState initialized to 0)
    */
   protected void _relocate()
   {
      name = null;
      parent = null;
      if ((value == null) && !ignored)
      {
         synchroState = PENDING_FLAG | REQUESTED_FLAG;
         TaskManager.addTask(new SendTask(getPathname(), revision, 0)); // interroge pour obtenir la valeur
      }
   }

   /*
    * 
    * @return Result of the synchronization :
    *                 2 => received < this (this not changed) => msg to send
    *                 0 => received == this (this not changed)
    *                 1 => received > this (this replaced by received)
    *  + QUESTION_FLAG  => received > this to signal (this replaced by received) => msg to send
    *  + DIR_PARAM_FLAG => received > this to replace (this which is a DataImpl must be replaced by a Directory received)
    *  + PENDING        => received > this + conditional
    *
    *  Condition :  (value != null) && (received.value != null)
    */
   protected void _merge(DataImpl received, long lag) throws DataAccessException
   {
      synchroState = 0;
      if (received instanceof DirectoryImpl) // on prend systématiquement le Directory, considéré comme plus récent
      {
         synchroState = 1 | DIR_PARAM_FLAG;
      }
      else if ((received.type == type) && (received.value.equals(value))) // même valeur (et même type)
      {
         // Choix de la révision pour laquelle le devId est <
         if (HomeSharedDataImpl.isPriorTo(revision >> 24, received.revision >> 24)) // choix rev locale
         {
            setRevisionAsPrevious(received.revision, received.previousRevisions);
         }
         else // choix rev reçue
         {
            setLaterRevision(received.revision, received.previousRevisions);
         }
         // on garde cmp == 0
      }
      else if (received.timestamp+lag > timestamp) // on choisit la valeur reçue (+ récente), choix empirique
      {
         synchroState = 1 | (received.revision < revision ? QUESTION_FLAG : 0); // devId choisi <, choix prioritaire à signaler/interroger
         _replace(received);
      }
      else // la valeur locale est plus récente (ou d'égale ancienneté)
      {
         setRevisionAsPrevious(received.revision, received.previousRevisions);
         synchroState = 2;
      }
   }

   /*
    * A compléter avec les différents type de values !!
    */
   protected String writeValueTo(String prefixName, DataOutputStream out, int floorRevision, int maxLevel) throws IOException
   {
      out.writeUTF(value == null ? "" : value instanceof String ? (String)value : value.toString());
      return prefixName;
   }

   protected String toStyledString(String prefix)
   {
      String line = prefix + " " +  getName();
      if ((_TREE_DISPLAY_MODE & 1) != 0) { line += "=" + value; }
      if ((_TREE_DISPLAY_MODE & 2) != 0) { line += " " + fullRevisionToString(); }
      return line;
   }
}
