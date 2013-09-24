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
class ResponseToRequestForDataTask implements Runnable, ExpectedData
{
   // ---------------------------------------------------------------------
   // init logger

   private static final Logger logger = LoggerFactory
         .getLogger(ResponseToRequestForDataTask.class.getName());

   // ---------------------------------------------------------------------
   
   private String pathname = null;
   private int expectedRevision = 0;
   private int floorRevision = 0;

   // ---------------------------------------------------------------------

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
    * @param data
    */
   public ResponseToRequestForDataTask(String pathname, int expectedRev, int floorRev)
   {
      this.pathname = (pathname == null ? "" : pathname);
      this.expectedRevision = expectedRev;
      this.floorRevision = floorRev;
   }

   public void run()
   {
      TaskManager.removePendingRequest(pathname, 0);
      try
      {
         DirectoryImpl root = (DirectoryImpl)HomeSharedDataImpl.getRootDirectory();
         if (root != null)
         {
            root.respondToRequestForData(pathname, expectedRevision, floorRevision);
         }
      }
      catch (DataAccessException e)
      {
         logger.error("response to request for data task failed : " + e.getMessage());
         e.printStackTrace();
      }

   }
}
