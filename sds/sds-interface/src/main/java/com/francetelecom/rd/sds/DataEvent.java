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

   public static final int LOCAL_UNDEFINED      = 0x00;
   public static final int LOCAL_DATA_ADDED     = 0x01;
   public static final int LOCAL_DATA_REMOVED   = 0x02;
   public static final int LOCAL_TYPE_CHANGED   = 0x03;
   public static final int LOCAL_VALUE_CHANGED  = 0x04;
   public static final int REMOTE_UNDEFINED     = 0x10;
   public static final int REMOTE_DATA_ADDED    = 0x11;
   public static final int REMOTE_DATA_REMOVED  = 0x12;
   public static final int REMOTE_TYPE_CHANGED  = 0x13;
   public static final int REMOTE_VALUE_CHANGED = 0x14;

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
