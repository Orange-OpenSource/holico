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
 * Thrown to indicate an error when accessing data (non existent pathname, pathname already existing, ...)  
 * 
 * @author goul5436
 */
public class DataAccessException extends Exception
{
   private static final long serialVersionUID = -4541560416235435015L;

   public static final int INTERNAL_ERROR         = 0;
   public static final int INVALID_PATHNAME       = 1;
   public static final int PATHNAME_NOT_FOUND     = 2;
   public static final int NOT_A_PARAMETER        = 3;
   public static final int NOT_A_DIRECTORY        = 4;
   public static final int NOT_A_STRING           = 5;
   public static final int NOT_AN_INTEGER         = 6;
   public static final int NOT_A_BOOLEAN          = 7;
   public static final int NOT_A_GENERIC_DIR      = 8;
   public static final int NOT_A_SPECIFIC_DIR     = 9;
   public static final int WRONG_TYPE_DECLARATION = 10;
   public static final int ALREADY_DEFINED        = 11;
   public static final int ALREADY_DEFINED_AS_DIR = 12;
   public static final int STRING_EXPECTED        = 13;
   public static final int INTEGER_EXPECTED       = 14;
   public static final int BOOLEAN_EXPECTED       = 15;
   public static final int UNEXPECTED_VALUE       = 16;
   public static final int UNEXPECTED_TYPE        = 17;

   protected static final String[] ERROR_MESSAGES =
   {
      "Internal error",
      "Invalid pathname",
      "'%s' not found",
      "'%s' is not a parameter",
      "'%s' is not a directory",
      "'%s' is not a string",
      "'%s' is not an integer",
      "'%s' is not a boolean",
      "'%s' is not a generic directory",
      "'%s' is not a specific directory",
      "Wrong type declaration",
      "Data '%s' already exists",
      "Data '%s' already defined as directory",
      "Wrong value : string expected",
      "Wrong value : integer expected",
      "Wrong value : boolean expected",
      "Unexpected value",
      "Unexpected type"
   };

   private static String _name(String msg, String name)
   {
      int k = msg.indexOf("%s");
      if (k != -1)
      {
         msg = msg.substring(0,k) + name + msg.substring(k+2);
      }
      return msg;
   }

   public DataAccessException(int errCode)
   {
      super(ERROR_MESSAGES[errCode]);
   }

   public DataAccessException(int errCode, String name)
   {
      super(_name(ERROR_MESSAGES[errCode], name));
   }
}
