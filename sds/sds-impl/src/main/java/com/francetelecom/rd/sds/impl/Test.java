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

import java.util.GregorianCalendar;

import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.Directory;
import com.francetelecom.rd.sds.Parameter;
import com.francetelecom.rd.sds.DataAccessException;

/**
 * @author goul5436
 *
 */
class Test
{

   private static void _display(String prefix, Directory dir, boolean rev)
   {
      Data[] data = dir.getChildren();
      for (int i=0; i<data.length; i++)
      {
         boolean isDir = data[i] instanceof Directory;
         String line = prefix + " " +  data[i].getName();
         if (isDir) { line += (data[i].getType() == Data.TYPE_GEN_DIR ? "." : "[]"); }
         if (rev) { line += " " + data[i].fullRevisionToString(); }
         System.out.println(line);
         if (isDir)
         {
            String pr = (prefix.length() == 0 ? " \u2514\u2500\u2500" : "    " + prefix);
            _display(pr, (Directory)data[i], rev);
         }
      }
   }

   public static void createTree(Directory root)
   {
      try
      {
         Parameter d1 = (Parameter)root.newData("n1.n11.d3", Data.TYPE_INT, false);
         d1.setValue(new Integer(10));
         Parameter d2 = (Parameter)root.newData("n1.n12.n123.d2", Data.TYPE_INT, false);
         d2.setValue(new Integer(100));
         Parameter d3 = (Parameter)root.newData("n1.n12.d5", Data.TYPE_STRING, false);
         d3.setValue("Coucou");
         DataImpl d5 = (DataImpl)root.newData("n1.n11.n123.", Data.TYPE_GEN_DIR, true);
         d5.setRevision(3);
         DataImpl d4 = new ParameterImpl("n1.n12.n123.date", Data.TYPE_PARAM, null, false, false, true, false, new DataValuer()
         {
            public Object getValue(String pathname)
            {
               return new GregorianCalendar().getTime();
            }
         });
         ((DirectoryImpl)root).putData("n1.n12.n123.date", d4);
         _display("", root, true);
      }
      catch (DataAccessException e)
      {
         e.printStackTrace();
      }
   }

   public static void test1(Directory root)
   {
      createTree(root);
      try
      {
         Directory rootCopy = (Directory)(root.clone());
         Object obj = rootCopy.getParameterValue(" n1.n12.d5");
         if (obj instanceof Integer)
         {
            System.out.println("Integer:"+obj);
         }
         else if (obj instanceof String)
         {
            System.out.println("String:"+obj);
         }
         else
         {
            System.out.println("Autre:"+obj);
         }
         root.deleteData("n1.n12.n123");
         System.out.println("====>");
         _display("", root, false);
      }
      catch (DataAccessException e)
      {
         e.printStackTrace();
      }
   }

   public static void test2(Directory root)
   {
      createTree(root);
      try
      {
         Directory dir = (Directory)root.getChild("n1.n12.n123");
         dir.setParameterValue("d2", "323");
         System.out.println("====>");
         _display("", root, true);
      }
      catch (DataAccessException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      Directory home = HomeSharedDataImpl.getInstance().getRootDirectory(true, null, 1);
      test2(home);
      System.exit(0);
   }

}
