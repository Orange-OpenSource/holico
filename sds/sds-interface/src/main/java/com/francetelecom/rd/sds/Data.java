/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.sds.sds-interface
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
package com.francetelecom.rd.sds;

/**
 * @author goul5436
 *
 */
public interface Data
{
   public static final char PATH_SEPARATOR = '.';
   public static final char LEFT_BRACKET_SEPARATOR = '[';
   public static final char RIGHT_BRACKET_SEPARATOR = ']';

   public static final int TYPE_PARAM   = 0;
   public static final int TYPE_INT     = 1;
	public static final int TYPE_BOOL    = 2;
	public static final int TYPE_STRING  = 3;
	public static final int TYPE_GEN_DIR = 4;
	public static final int TYPE_SPE_DIR = 5;

   /**
    * @return The revision information as a string
    */
   public String fullRevisionToString();


   /**
    * @return Returns the parent directory.
    */
   public Directory getParent();

   /**
    * @return Returns the relative name.
    */
   public String getName();

   /**
    * @return Returns the pathname (absolute name).
    */
   public String getPathname();

   /**
    * @return Returns the type.
    */
   public int getType();

   /**
    * @return Returns the timestamp.
    */
   public long getTimestamp();

   /**
    * @return Returns the revision.
    */
   public int getRevision();

   /**
    * Marks the data as modified.
    */
   public void touch();

   /**
    * Adds the specified listener to received notification events when the value of this data changes.
    *  
    * @param listener the DataChangeListener
    */
   public void addDataChangeListener(DataChangeListener listener);

   /**
    * Removes the listener previously specified.
    * 
    * @param listener to remove
    */
   public void removeDataChangeListener(DataChangeListener listener);

   /**
    * 
    * @return a copy of the Data object
    */
   public abstract Object clone();
}
