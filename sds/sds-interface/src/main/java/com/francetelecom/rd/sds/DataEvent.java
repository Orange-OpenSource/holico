/**
 * 
 */
package com.francetelecom.rd.sds;

import java.util.EventObject;

/**
 * @author GOUL5436
 *
 */
public class DataEvent extends EventObject
{
   private static final long serialVersionUID = 3184590133989861215L;

   public static final int UNDEFINED       = 0;
   public static final int DATA_ADDED      = 1;
   public static final int DATA_REMOVED    = 2;
   public static final int TYPE_CHANGED    = 3;
   public static final int VALUE_CHANGED   = 4;

   private int type;
   private String pathname; // relative pathname from source, null for VALUE_CHANGED type

   /**
    * @param source
    */
   public DataEvent(Data source, int type, String pathname)
   {
      super(source);
      this.type = type;
      this.pathname = pathname;
   }

   /**
    * @return the type
    */
   public int getType()
   {
      return type;
   }

   /**
    * @return the pathname
    */
   public String getPathname()
   {
      return pathname;
   }
}
