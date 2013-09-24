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
public interface Directory extends Data
{
   /**
    * Returns the value of a parameter.
    * 
    * @param pathname parameter name whose the value is requested.
    * 
    * @return the value requested.
    * 
    * @throws DataAccessException
    */
   public Object getParameterValue(String pathname) throws DataAccessException;

   /**
    * Returns the value of a parameter.
    * 
    * @param pathname parameter name whose the value is requested.
    * 
    * @return the value requested.
    * 
    * @throws DataAccessException
    */
   public String getParameterStringValue(String pathname) throws DataAccessException;

   /**
    * Returns the value of a parameter.
    * 
    * @param pathname parameter name whose the value is requested.
    * 
    * @return the value requested.
    * 
    * @throws DataAccessException
    */
   public int getParameterIntValue(String pathname) throws DataAccessException;

   /**
    * Returns the value of a parameter.
    * 
    * @param pathname parameter name whose the value is requested.
    * 
    * @return the value requested.
    * 
    * @throws DataAccessException
    */
   public boolean getParameterBooleanValue(String pathname) throws DataAccessException;

   /**
    * Sets the parameter with the provided value.
    * 
    * @param pathname parameter name
    * @param val new value to set
    *  
    * @throws DataAccessException
    */
   public void setParameterValue(String pathname, Object val) throws DataAccessException, NumberFormatException;

   /**
    * Returns true if the named data can be accessed from this directory.
    * 
    * @param pathname pathname of a searched data.
    * 
    * @return true if the named data can be accessed from this directory.
    */
   public boolean contains(String pathname);

   /**
    * Adds a data with the provided name.
    * 
    * @param pathname name of the new data (parameter or node).
    * @param type type of the new data.
    * @param overwrite true to allow redefinition of a data already defined without throwing an exception.
    * However, when changing the type of an existing data, an exception remains possible when the existing value can not be converted to the new type.  
    * 
    * @return the data newly created, a node if pathname ends with a dot, otherwise a parameter.
    * 
    * @throws DataAccessException
    */
   public Data newData(String pathname, int type, boolean overwrite) throws DataAccessException;

   /**
    * Removes a data, parameter or directory.
    * 
    * @param pathname name of the data to remove
    * 
    * @return the data that was removed
    * 
    * @throws DataAccessException
    */
   public Data deleteData(String pathname) throws DataAccessException;

   /**
    * Returns the named child if it exists, or null if not.
    * 
    * @param pathname name of a data relatively to the current directory
    * 
    * @return Data the named child
    * 
    * @throws DataAccessException
    */
   public Data getChild(String pathname) throws DataAccessException;

   /**
    * Returns the named child directory if it exists, or null if not.
    * 
    * @param pathname name of a directory relatively to the current directory
    * 
    * @return Data the named directory
    * 
    * @throws DataAccessException If the pathname is incorrect or if the data is not a directory
    */
   public Directory getDirectory(String pathname) throws DataAccessException;

   /**
    * Returns the named child parameter if it exists, or null if not.
    * 
    * @param pathname name of a parameter relatively to the current directory
    * 
    * @return Data the named parameter
    * 
    * @throws DataAccessException If the pathname is incorrect or if the data is not a parameter
    */
   public Parameter getParameter(String pathname) throws DataAccessException;

   /**
   /**
    * Returns the children of a directory.
    * 
    * @param pathname name of a directory relatively to the current directory, null for the current directory
    * 
    * @return array of children
    * 
    * @throws DataAccessException
    */
   public String[] getChildNames(String pathname) throws DataAccessException;

   /**
    * Returns the children of this directory
    * 
    * @return array of Data children of the current directory
    */
   public Data[] getChildren();

   /**
    * Adds the specified listener to received notification events when the value of the provided parameter changes.
    *  
    * @param pathname name of the monitored parameter.
    * 
    * @param listener the ValueChangeListener
    * 
    * @throws DataAccessException
    */
   public void addValueChangeListener(String pathname, ValueChangeListener listener) throws DataAccessException;

   /**
    * Removes the listener previously specified.
    * 
    * @param pathname name of the monitored parameter.
    * 
    * @param listener listener previously specified
    * 
    * @throws DataAccessException
    */
   public void removeValueChangeListener(String pathname, ValueChangeListener listener) throws DataAccessException;
}
