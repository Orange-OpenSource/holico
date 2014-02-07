/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.sds.sds-gui
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
package com.francetelecom.rd.sds.gui_test;

import java.awt.Font;
import java.io.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
//import javax.swing.text.BadLocationException;

import com.francetelecom.rd.sds.*;
import com.francetelecom.rd.sds.impl.HomeSharedDataImpl;
import com.francetelecom.rd.sds.impl.SendTask;

/**
 * @author goul5436
 *
 */
/**
 * An example which shows off a functional simple text editor.  Includes a variety of events.
 */
public class Console extends JFrame implements DataChangeListener
{
   // configure log system to use configuration file from classpath : 
   static {
      final InputStream inputStream = Console.class.getResourceAsStream("/logging.properties");
      try
      {
         LogManager.getLogManager().readConfiguration(inputStream);
      }
      catch (final IOException e)
      {
         Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
         Logger.getAnonymousLogger().severe(e.getMessage());
      }
   }

   static final boolean FORCE_REINIT = false;
   static final String HOME_FILE_NAME = "sds.data";
   static final String CONNECTED_ICON_FILE_NAME = "/connected.png";
   static final String DISCONNECTED_ICON_FILE_NAME = "/disconnected.png";

   static final boolean LOG_IN_CONSOLE = true;

   private static int treeDisplayMode = 0;

   private static final long serialVersionUID = 1L;

   private static Console instance = null;
   private static HomeSharedData home = null;
   private static Directory root = null;

   private javax.swing.JPanel jContentPane = null;
   private javax.swing.JLabel jPathLabel = new javax.swing.JLabel();
   private javax.swing.JLabel jConnectionStatus = null;
   private javax.swing.JPanel jPanelHeader = null;
   private javax.swing.ImageIcon connectedIcon = null;
   private javax.swing.ImageIcon disconnectedIcon = null;
   private javax.swing.JPanel jPanelCommand = null;
   private javax.swing.JPanel jPanelButtons = null;
   private javax.swing.JButton jButtonClear = null;
   private javax.swing.JButton jButtonSaveAs = null;
   private javax.swing.JButton jButtonExit = null;
   private javax.swing.JScrollPane jScrollPaneDisplay = null;
   private javax.swing.JTextArea jTextAreaDisplay = null;
   private javax.swing.JScrollPane jScrollPaneCommand = null;
   private javax.swing.JTextArea jTextAreaCommand = null;
   private javax.swing.JFileChooser jFileChooser = null; //  @jve:visual-info  decl-index=0 visual-constraint="582,36"

   private boolean hasChanged = false;
   private static final String title = "Operating and Programming Environment";

   /**
    * This method initializes 
    * 
    */
   public Console()
   {
      super();
      initialize();
   }

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      instance = new Console();
      home = HomeSharedDataImpl.getInstance();
      root = home.getRootDirectory(FORCE_REINIT, HOME_FILE_NAME, null);
      instance._updatePathLabel();
      root.addDataChangeListener(instance);
      instance.setVisible(true);
      instance.jTextAreaCommand.requestFocus();
   }

   public static void log(String msg)
   {
      if (LOG_IN_CONSOLE)
      {
         instance._display(msg);
      }
      else
      {
         System.out.println(msg);
      }
   }

   private void _updatePathLabel()
   {
      jPathLabel.setText("root " + root.fullRevisionToString());
   }

   private void _display(String msg)
   {
      jTextAreaDisplay.append(msg + "\n");
      jTextAreaDisplay.setCaretPosition(jTextAreaDisplay.getText().length());
   }

   /**
    * This method initializes jContentPane
    * 
    * @return javax.swing.JPanel
    */
   private javax.swing.JPanel getJContentPane()
   {
      if (jContentPane == null)
      {
         jContentPane = new javax.swing.JPanel();
         jContentPane.setLayout(new java.awt.BorderLayout());
         jContentPane.add(getJPanelHeader(), java.awt.BorderLayout.NORTH);
         jContentPane.add(getJScrollPaneDisplay(), java.awt.BorderLayout.CENTER);
         jContentPane.add(getJPanelCommand(), java.awt.BorderLayout.SOUTH);
         jContentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
      }
      return jContentPane;
   }

   /**
    * This method initializes this
    * 
    * @return void
    */
   private void initialize()
   {
      this.setContentPane(getJContentPane());
      this.setTitle(title);
      this.setSize(480, 640);
      this.setLocation(30, 20);
      this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
      java.awt.event.WindowAdapter listener = new java.awt.event.WindowAdapter()
      {
         public void windowClosing(java.awt.event.WindowEvent e)
         {
            doExit();
         }

         public void windowGainedFocus(java.awt.event.WindowEvent e) // ??
         {
            jTextAreaCommand.setText("");
         }
      };
      this.addWindowListener(listener);
      this.addWindowFocusListener(listener);
   }

   /**
    * This method initializes jPanelHeader
    * 
    * @return javax.swing.JPanel
    */
   private javax.swing.JPanel getJPanelHeader()
   {
      if (jPanelHeader == null)
      {
         connectedIcon = new javax.swing.ImageIcon(getClass().getResource(CONNECTED_ICON_FILE_NAME));
         disconnectedIcon = new javax.swing.ImageIcon(getClass().getResource(DISCONNECTED_ICON_FILE_NAME));
         jConnectionStatus = new javax.swing.JLabel(connectedIcon);
         jPanelHeader = new javax.swing.JPanel();
         jPanelHeader.setLayout(new java.awt.BorderLayout());
         jPanelHeader.add(jPathLabel, java.awt.BorderLayout.WEST);
         jPanelHeader.add(jConnectionStatus, java.awt.BorderLayout.EAST);
         jConnectionStatus.addMouseListener(new java.awt.event.MouseAdapter()
         {
            public void mouseClicked(java.awt.event.MouseEvent e)
            {
               boolean status = (jConnectionStatus.getIcon() == disconnectedIcon);
               jConnectionStatus.setIcon(status ? connectedIcon : disconnectedIcon);
               SendTask.setConnectionStatus(status);
            }
         });
      }
      return jPanelHeader;
   }

   /**
    * This method initializes jPanelCommand
    * 
    * @return javax.swing.JPanel
    */
   private javax.swing.JPanel getJPanelCommand()
   {
      if (jPanelCommand == null)
      {
         jPanelCommand = new javax.swing.JPanel();
         jPanelCommand.setLayout(new java.awt.BorderLayout());
         jPanelCommand.add(getJPanelButtons(), java.awt.BorderLayout.SOUTH);
         jPanelCommand.add(getJScrollPaneCommand(), java.awt.BorderLayout.CENTER);
         //jPanelCommand.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
      }
      return jPanelCommand;
   }

   /**
    * This method initializes jPanelButtons
    * 
    * @return javax.swing.JPanel
    */
   private javax.swing.JPanel getJPanelButtons()
   {
      if (jPanelButtons == null)
      {
         jPanelButtons = new javax.swing.JPanel();
         jPanelButtons.add(getJButtonClear(), null);
         jPanelButtons.add(getJButtonSaveAs(), null);
         jPanelButtons.add(getJButtonExit(), null);
      }
      return jPanelButtons;
   }

   /**
    * This method initializes jButton
    * 
    * @return javax.swing.JButton
    */
   private javax.swing.JButton getJButtonClear()
   {
      if (jButtonClear == null)
      {
         jButtonClear = new javax.swing.JButton();
         jButtonClear.setText("Clear");
         jButtonClear.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
               //loadFile();
               jTextAreaDisplay.setText("");
               hasChanged = false;
            }
         });
      }
      return jButtonClear;
   }

   /**
    * This method initializes jButton1
    * 
    * @return javax.swing.JButton
    */
   private javax.swing.JButton getJButtonSaveAs()
   {
      if (jButtonSaveAs == null)
      {
         jButtonSaveAs = new javax.swing.JButton();
         jButtonSaveAs.setText("Save As...");
         jButtonSaveAs.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
               saveFile();
            }
         });
      }
      return jButtonSaveAs;
   }

   /**
    * This method initializes jButton2
    * 
    * @return javax.swing.JButton
    */
   private javax.swing.JButton getJButtonExit()
   {
      if (jButtonExit == null)
      {
         jButtonExit = new javax.swing.JButton();
         jButtonExit.setText("Exit");
         jButtonExit.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
               doExit();
            }
         });
      }
      return jButtonExit;
   }

   private void performCommand(String cmd)
   {
      _display("\n---? " + cmd);
      String val = null;
      try
      {
         val = "---= " + eval(cmd);
      }
      catch (DataAccessException e)
      {
         val = "*** " + e.getMessage();
      }
      catch (NumberFormatException e)
      {
         val = "*** NumberFormatException " + e.getMessage();
      }
      catch (InterruptedException e)
      {
         doExit();
      }
      _display(val);
   }

   private javax.swing.JTextArea getJTextAreaCommand()
   {
      if (jTextAreaCommand == null)
      {
         jTextAreaCommand = new javax.swing.JTextArea();
         jTextAreaCommand.addKeyListener(new java.awt.event.KeyAdapter()
         {
            public void keyPressed(java.awt.event.KeyEvent e)
            {
               if (e.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER)
               {
                  performCommand(jTextAreaCommand.getText());
               }
            }

            public void keyReleased(java.awt.event.KeyEvent e)
            {
               if (e.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER)
               {
                  jTextAreaCommand.setText("");
               }
            }
         });
      }
      return jTextAreaCommand;
   }

   /**
    * This method initializes jScrollPaneCommand
    * 
    * @return javax.swing.JScrollPane
    */
   private javax.swing.JScrollPane getJScrollPaneCommand()
   {
      if (jScrollPaneCommand == null)
      {
         jScrollPaneCommand = new javax.swing.JScrollPane();
         //          jScrollPaneCommand.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
         jScrollPaneCommand.setPreferredSize(new java.awt.Dimension(10,70));
         jScrollPaneCommand.setViewportView(getJTextAreaCommand());
      }
      return jScrollPaneCommand;
   }

   /**
    * This method initializes jScrollPaneDisplay
    * 
    * @return javax.swing.JScrollPane
    */
   private javax.swing.JScrollPane getJScrollPaneDisplay()
   {
      if (jScrollPaneDisplay == null)
      {
         jScrollPaneDisplay = new javax.swing.JScrollPane();
         jScrollPaneDisplay.setViewportView(getJTextAreaDisplay());
      }
      return jScrollPaneDisplay;
   }

   /**
    * This method initializes jTextAreaDisplay
    * 
    * @return javax.swing.JTextArea
    */
   private javax.swing.JTextArea getJTextAreaDisplay()
   {
      if (jTextAreaDisplay == null)
      {
         jTextAreaDisplay = new javax.swing.JTextArea();
         jTextAreaDisplay.setLineWrap(true);
         jTextAreaDisplay.setWrapStyleWord(true);
         jTextAreaDisplay.setEditable(false);
         //jTextAreaDisplay.setFocusable(false);
         jTextAreaDisplay.setFont(new Font("Courier New", Font.PLAIN, 14));
         jTextAreaDisplay.setFont(jTextAreaDisplay.getFont().deriveFont((float)14));
         /*for (Font f :GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
            System.out.println("Size="+f);*/
      }
      return jTextAreaDisplay;
   }

   /**
    * This method initializes jFileChooser
    * 
    * @return javax.swing.JFileChooser
    */
   private javax.swing.JFileChooser getJFileChooser()
   {
      if (jFileChooser == null)
      {
         jFileChooser = new javax.swing.JFileChooser();
         jFileChooser.setMultiSelectionEnabled(false);
      }
      return jFileChooser;
   }

   //      private void loadFile()
   //      {
   //         int state = getJFileChooser().showOpenDialog(this);
   //         if (state == JFileChooser.APPROVE_OPTION)
   //         {
   //            File f = getJFileChooser().getSelectedFile();
   //            try
   //            {
   //               BufferedReader br = new BufferedReader(new FileReader(f));
   //               getJTextAreaDisplay().read(br, null);
   //               br.close();
   //               setTitle(title);
   //               hasChanged = false;
   //            }
   //            catch (FileNotFoundException e1)
   //            {
   //               e1.printStackTrace();
   //            }
   //            catch (IOException e1)
   //            {
   //               e1.printStackTrace();
   //            }
   //         }
   //      }

   private void saveFile()
   {
      int state = getJFileChooser().showSaveDialog(this);
      if (state == JFileChooser.APPROVE_OPTION)
      {
         File f = getJFileChooser().getSelectedFile();
         try
         {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            getJTextAreaDisplay().write(bw);
            bw.close();
            setTitle(title);
            hasChanged = false;
         }
         catch (FileNotFoundException e1)
         {
            e1.printStackTrace();
         }
         catch (IOException e1)
         {
            e1.printStackTrace();
         }
      }
   }

   private void doExit()
   {
      if (hasChanged)
      {
         int state = JOptionPane.showConfirmDialog(this,
               "File has been changed. Save before exit?");
         if (state == JOptionPane.YES_OPTION)
         {
            saveFile();
         }
         else if (state == JOptionPane.CANCEL_OPTION)
         {
            return;
         }
      }
      home.save(HOME_FILE_NAME);
      System.exit(0);
   }

   private void _display(String prefix, Directory dir)
   {
      if (prefix.isEmpty()) _display("."+dir.fullRevisionToString());
      Data[] data = dir.getChildren();
      if (data != null)
      {
         for (int i=0; i<data.length; i++)
         {
            boolean isDir = data[i] instanceof Directory;
            String line = prefix + " " +  data[i].getName();
            if (isDir) { line += (data[i].getType() == Data.TYPE_GEN_DIR ? "." : "[]"); }
            if (!isDir && ((treeDisplayMode & 1) != 0)) { line += "=" + ((Parameter)data[i]).getValue(); }
            if ((treeDisplayMode & 2) != 0) { line += " " + data[i].fullRevisionToString(); }
            _display(line);
            if (isDir)
            {
               String pr = (prefix.length() == 0 ? " \u2514\u2500\u2500" : "    " + prefix);
               _display(pr, (Directory)data[i]);
            }
         }
      }
   }

   public String eval(String cmd) throws DataAccessException, InterruptedException
   {
      String res = null;
      String[] words = cmd.split("\\s+");
      if (words.length > 0)
      {
         if (("add".equals(words[0]) || "redef".equals(words[0])) && (words.length > 1))
         {
            int type = Data.TYPE_PARAM;
            if (words.length > 2)
            {
               type = ( words[2].equals("int") ? Data.TYPE_INT
                     : words[2].equals("bool") ? Data.TYPE_BOOL
                           : words[2].equals("string") ? Data.TYPE_STRING
                                 : words[2].equals("shared") ? Data.TYPE_GEN_DIR
                                       : words[2].equals("multi") ? Data.TYPE_SPE_DIR
                                             : Data.TYPE_PARAM);
            }
            root.newData(words[1], type, "redef".equals(words[0]));
            res = "ok";
         }
         else if (("remove".equals(words[0])) && (words.length > 1))
         {
            root.deleteData(words[1]);
            res = "ok";
         }
         else if (("get".equals(words[0])) && (words.length > 1))
         {
            Object val = root.getParameterValue(words[1]);
            res = (val == null ? "null" : val.toString());
         }
         else if (("set".equals(words[0])) && (words.length > 2))
         {
            root.setParameterValue(words[1], words[2]);
            res = "ok";
         }
         else if (("typeof".equals(words[0])) && (words.length > 1))
         {
            Data data = root.getChild(words[1]);
            if (data == null)
            {
               throw new DataAccessException(DataAccessException.INVALID_PATHNAME);
            }
            res = ( data.getType() == Data.TYPE_INT ? "int"
                  : data.getType() == Data.TYPE_BOOL ? "bool"
                        : data.getType() == Data.TYPE_STRING ? "string"
                              : data.getType() == Data.TYPE_PARAM ? "any"
                                    : data.getType() == Data.TYPE_GEN_DIR ? "shared"
                                          : data.getType() == Data.TYPE_SPE_DIR ? "multi"
                                                : "unknown");
         }
         else if (("exists".equals(words[0])) && (words.length > 1))
         {
            res = (root.contains(words[1]) ? "yes" : "no");
         }
         else if ("list".equals(words[0]))
         {
            String pathname = (words.length < 2 ? null : words[1]);
            String[] list = root.getChildNames(pathname);
            for (int i=0; i<list.length; i++)
            {
               _display(list[i]);
            }
            res = "ok";
         }
         else if ("tree".equals(words[0]))
         {
            treeDisplayMode = 0;
            if (words.length > 1)
            {
               if (words[1].indexOf('v') != -1) { treeDisplayMode  = 1; }
               if (words[1].indexOf('r') != -1) { treeDisplayMode |= 2; }
               if (words[1].indexOf('u') != -1) { treeDisplayMode |= 4; }
            }
            _display("", root);
            res = "ok";
         }
         else if ("exit".equals(words[0]))
         {
            throw new InterruptedException();
         }
         else if ("test".equals(words[0]) && (words.length > 1))
         {
            if (words[1].equals("listener") && (words.length > 2))
            {
               //// Test DataChangeListener. Exemple : test listener a1.a12.a123 ////
               Console.log("*** Ajout d'un listener sur " + words[2]);
               try
               {
                  root.addValueChangeListener(words[2],
                        new DataChangeListener()
                  {
                     public void dataChange(ArrayList<DataEvent> events)
                     {
                        for (DataEvent evt : events)
                        {
                           // Console.log("La valeur de " + ((DataImpl)evt.getSource()).getName() + " est modifiée !");
                           Data src = (Data)evt.getSource();
                           switch(evt.getType())
                           {
                              case DataEvent.LOCAL_DATA_ADDED :
                              case DataEvent.REMOTE_DATA_ADDED :
                                 Console.log("*** Data " + evt.getPathname() + " added to " + src.getPathname());
                                 break;
                              case DataEvent.LOCAL_DATA_REMOVED :
                              case DataEvent.REMOTE_DATA_REMOVED :
                                 Console.log("*** Data " + evt.getPathname() + " removed from " + src.getPathname());
                                 break;
                              case DataEvent.LOCAL_TYPE_CHANGED :
                              case DataEvent.REMOTE_TYPE_CHANGED :
                                 Console.log("*** Data " + src.getPathname() + " changed to type " + src.getType());
                                 break;
                              case DataEvent.LOCAL_VALUE_CHANGED:
                              case DataEvent.REMOTE_VALUE_CHANGED:
                                 if (src instanceof Parameter)
                                 {
                                    Console.log("*** Value changed : " + src.getPathname() + "=" + ((Parameter)src).getValue());
                                 }
                                 else
                                 {
                                    Console.log("*** Event error on " + src.getPathname());
                                 }
                                 break;
                              default :
                                 break;
                           }
                        }
                     }
                  }
                        );
               }
               catch (DataAccessException e)
               {
                  Console.log("DataAccessException : " + e.getMessage());
               }
            }
            else if (words[1].equals("seq1"))
            {
               home.lock();
               try
               {
                  root.newData("a1.a12.a121", Data.TYPE_INT, true);
                  root.newData("a1.a12.a122", Data.TYPE_BOOL, true);
                  root.newData("a1.a12.a123", Data.TYPE_STRING, true);
                  root.setParameterValue("a1.a12.a121", "121");
                  root.setParameterValue("a1.a12.a122", "true");
                  root.setParameterValue("a1.a12.a123", "toto");
               }
               finally
               {
                  home.unlock();
               }
            }
            else if (words[1].equals("inc") && (words.length > 4))
            {
               //// Test DataChangeListener. Exemple : test inc a1.a2.c 3 1000 ////
               Console.log("*** Incrémentation itérative sur " + words[2]);
               final String pathname = words[2];
               final int step = Integer.parseInt(words[3]);
               long interval = Long.parseLong(words[4]);
               new Timer().schedule(new TimerTask()
               {
                  public void run()
                  {
                     try
                     {
                        int val = root.getParameterIntValue(pathname);
                        root.setParameterValue(pathname, Integer.valueOf(val + step));
                     }
                     catch (DataAccessException e)
                     {
                     }
                  }
               }, interval, interval);
               res = "ok";
            }
            else if (words[1].equals("iter") && (words.length > 4))
            {
               //// Test DataChangeListener. Exemple : test iter a1.a2.c first second third 1000 ////
               Console.log("*** Affectations itératives sur " + words[2]);
               final String pathname = words[2];
               final String[] values = new String[words.length - 4];
               for (int i=0; i<words.length - 4; i++) { values[i] = words[i+3]; }
               long interval = Long.parseLong(words[words.length - 1]);
               new Timer().schedule(new TimerTask()
               {
            	  private int i=0;
                  public void run()
                  {
                     try
                     {
                        root.setParameterValue(pathname, values[i]);
                     }
                     catch (DataAccessException e)
                     {
                     }
                     i = (i+1) % values.length;
                  }
               }, interval, interval);
               res = "ok";
            }
            /*else if (words[1].equals("synchro1"))
            {
               DirectoryImpl dirRoot = new DirectoryImpl(null, 71);
               DirectoryImpl dir_a1 = new DirectoryImpl("a1", null, 71);
               dirRoot.putData("a1.", dir_a1);
               DirectoryImpl dir_a2 = new DirectoryImpl("a2", null, 17);
               dirRoot.putData("a2.", dir_a2);
               root.synchronize(null, dirRoot, 0);
            }
            else if (words[1].equals("synchro2"))
            {
               DirectoryImpl dir_a1 = new DirectoryImpl(null, 71);
               DataImpl data_a11 = new DataImpl("a11", "548", 70);
               dir_a1.putData("a11", data_a11);
               DataImpl data_a12 = new DataImpl("a12", "77", 15);
               dir_a1.putData("a12", data_a12);
               root.synchronize("a1.", dir_a1, 0);
            }*/
            res = "ok";
         }
         else if ("help".equals(words[0]))
         {
            _display("add pathname [int|bool|string|shared|multi]");
            _display("redef pathname [int|bool|string|shared|multi]");
            _display("remove pathname");
            _display("get pathname");
            _display("set pathname value");
            _display("typeof pathname");
            _display("exists pathname");
            _display("get pathname");
            _display("list [pathname]");
            _display("tree [v][r][u]");
            _display("exit");
            _display("test listener pathname");
            _display("test inc pathname step interval");
            _display("test iter pathname val1 val2 ... valN interval");
            _display("test seq1");
            res = "ok";
         }
      }

      return res;
   }

   public void dataChange(ArrayList<DataEvent> events)
   {
      _updatePathLabel();
      if (treeDisplayMode >= 4)
      {
         _display("");
         _display("", root); }
   }
}
