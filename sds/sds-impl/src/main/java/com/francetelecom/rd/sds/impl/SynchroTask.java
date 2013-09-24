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

/**
 * @author goul5436
 *
 */
class SynchroTask implements Runnable
{
   // ---------------------------------------------------------------------
   // init logger

   private static final Logger logger = LoggerFactory
         .getLogger(SynchroTask.class.getName());

   // ---------------------------------------------------------------------

   private DataImpl data = null;
   private long timelag = 0;

   // ---------------------------------------------------------------------

   /**
    * @param data
    */
   public SynchroTask(DataImpl data, long timelag)
   {
      this.data = data;
      this.timelag = timelag;
   }

   protected boolean isExpected()
   {
      boolean res = false;
      try
      {
         DirectoryImpl root = (DirectoryImpl)HomeSharedDataImpl.getRootDirectory();
         if (root != null)
         {
            res = root.isExpected(data);
         }
      }
      catch (DataAccessException e)
      {
      }
      return res;
   }

   public void run()
   {
      try
      {
         DirectoryImpl root = (DirectoryImpl)HomeSharedDataImpl.getRootDirectory();
         if (root != null)
         {
            root.synchronize(data, timelag);
            TaskManager.removePendingRequest(data.pathname, data.revision);
         }
      }
      catch (DataAccessException e)
      {
         logger.error("synchro task failed : " + e.getMessage());
         e.printStackTrace();
      }

   }

}
