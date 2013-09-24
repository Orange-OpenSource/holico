/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.sds.sds-tusynchro
 * Version:     0.1-SNAPSHOT
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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.EventObject;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.text.html.ListView;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;
import com.francetelecom.rd.sds.Parameter;
import com.francetelecom.rd.sds.ValueChangeListener;
import com.francetelecom.rd.sds.impl.DirectoryImpl;
import com.francetelecom.rd.sds.impl.HomeSharedDataImpl;
import com.francetelecom.rd.sds.impl.SendTask;

public class SynchroTest extends TestCase {

	// configure log system to use configuration file from classpath : 
	static {
		final InputStream inputStream = SynchroTest.class.getResourceAsStream("/logging.properties");
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

	// ==============================================================================

	static int deviceId = 125;
	static int deviceId2 = 124;
	static String paramPath = "my_path";
	static String dirPath = "my_dir";

	static Directory hsRoot;
	static Process stimulatorProcess;

	// ==============================================================================

	protected void setUp() throws Exception {

		System.out
		.println(" Global setup " + SynchroTest.class.getName());

		HomeSharedDataImpl hsd = HomeSharedDataImpl.getInstance();
		hsRoot = hsd.getRootDirectory(true, null, deviceId);
		assertNotNull(hsRoot);
	}

	protected void tearDown() throws Exception {

		System.out.println(" Global tearDown "
				+ SynchroTest.class.getName());

		reset();

		if(stimulatorProcess != null)
		{
			stimulatorProcess.destroy();
		}
	}

	private void reset() {
		if(hsRoot != null)
		{
			recursiveDelete(hsRoot);
			hsRoot = null;	
		}
	}

	private void recursiveDelete(Directory dir) {
		if(dir == null) return;
		Data[] clildren = dir.getChildren();
		for (int i = 0; i < clildren.length; ++i) {
			if (clildren[i].getType() == Data.TYPE_GEN_DIR
					|| clildren[i].getType() == Data.TYPE_SPE_DIR) {
				recursiveDelete((Directory) clildren[i]);
				try {
					dir.deleteData(clildren[i].getName());
				} catch (DataAccessException e) {
					e.printStackTrace();
				}
			} else {
				try {
					dir.deleteData(clildren[i].getName());
				} catch (DataAccessException e) {
					e.printStackTrace();
				}
			}
		}

	}



	// ==============================================================================
	// ==============================================================================


	public void test_singleTreeSynchro() throws Exception {

		// declare custom listener for test
		class CustomListener implements ValueChangeListener{

			private int tuCase = 0;
			private int tuIndex = 1;
			private boolean allTuPassed = false;
			private boolean errorOccured = false;
			private Timer nextTuTimeout;			

			public boolean isAllTuPassed() 
			{
				return allTuPassed;
			}

			public boolean errorOccured() 
			{
				return errorOccured;
			}

			private void manageTimeout()
			{
				TimerTask task = new TimerTask() {

					@Override
					public void run() {
						_display("**************************");
						_display("Next TU timeout");
						_display("**************************");
						errorOccured = true;					
					}
				};

				if(nextTuTimeout != null)
				{
					nextTuTimeout.cancel();
				}
				nextTuTimeout = new Timer();
				nextTuTimeout.schedule(task, 10000);
			}

			public void valueChange(EventObject evt)
			{
				tuCase++;	

				if(tuCase > 6)
				{
					tuCase = 1;
					tuIndex++;
				}

				manageTimeout();

				if(hsRoot == null)
				{
					_display("Root = NULL");
					errorOccured = true;
					return;
				}

				if(errorOccured)
				{
					if(nextTuTimeout != null)
					{
						nextTuTimeout.cancel();
					}
					return;
				}

				_display("==========================");
				_display("      root updated");
				_display("==========================");
				_display("", hsRoot);
				_display("--------------------------");

				boolean aTuMatch = false;

				try
				{
					// ----------------------------------------------
					// TU CASE 1
					try
					{
						_display("launch TU CASE 1 (" + tuIndex + ")");
						checkTUCase1();
						aTuMatch = true;                            
					}
					catch(AssertionFailedError e)
					{
						_display("TU CASE 1 (" + tuIndex + ") failed");
						_display("reason :  " + e.getMessage());
					}

					if(aTuMatch)
					{
						_display("TU CASE 1 (" + tuIndex + ") passed");
						assertEquals("expected TU CASE " + tuCase + " and result match TU CASE 1", 1, tuCase);
						tuCase = 1;
					}

					if(!aTuMatch)
					{
						// ----------------------------------------------
						// TU CASE 2
						try
						{
							_display("launch TU CASE 2 (" + tuIndex + ")");
							checkTUCase2();
							aTuMatch = true;                            
						}
						catch(AssertionFailedError e)
						{
							_display("TU CASE 2 (" + tuIndex + ") failed");
							_display("reason :  " + e.getMessage());
						}

						if(aTuMatch)
						{
							_display("TU CASE 2 (" + tuIndex + ") passed");
							assertEquals("expected TU CASE " + tuCase + " and result match TU CASE 2", 2, tuCase);
							tuCase = 2;
						}
					}

					if(!aTuMatch)
					{
						// ----------------------------------------------
						// TU CASE 3
						try
						{
							_display("launch TU CASE 3 (" + tuIndex + ")");
							checkTUCase3();
							aTuMatch = true;                            
						}
						catch(AssertionFailedError e)
						{
							_display("TU CASE 3 (" + tuIndex + ") failed");
							_display("reason :  " + e.getMessage());
						}

						if(aTuMatch)
						{
							_display("TU CASE 3 (" + tuIndex + ") passed");
							assertEquals("expected TU CASE " + tuCase + " and result match TU CASE 3", 3, tuCase);
							tuCase = 3;
						}
					}

					if(!aTuMatch)
					{
						// ----------------------------------------------
						// TU CASE 4/6
						try
						{
							_display("launch TU CASE 4/6 (" + tuIndex + ")");
							checkTUCase46();
							aTuMatch = true;                            
						}
						catch(AssertionFailedError e)
						{
							_display("TU CASE 4/6 (" + tuIndex + ") failed");
							_display("reason :  " + e.getMessage());
						}

						if(aTuMatch)
						{
							_display("TU CASE 4/6 (" + tuIndex + ") passed");
							boolean tu46 = (tuCase == 4) || (tuCase == 6);
							assertTrue("expected TU CASE " + tuCase + " and result match TU CASE 4/6", tu46);
							if(tuCase<=4) tuCase = 4;
							else tuCase = 6;
						}
					}

					if(!aTuMatch)
					{
						// ----------------------------------------------
						// TU CASE 5
						try
						{
							_display("launch TU CASE 5 (" + tuIndex + ")");
							checkTUCase5();
							aTuMatch = true;                            
						}
						catch(AssertionFailedError e)
						{
							_display("TU CASE 5 (" + tuIndex + ") failed");
							_display("reason :  " + e.getMessage());
						}

						if(aTuMatch)
						{
							_display("TU CASE 5 (" + tuIndex + ") passed");
							assertEquals("expected TU CASE " + tuCase + " and result match TU CASE 5", 5, tuCase);
							tuCase = 5;
						}
					}

					// ----------------------------------------------

					if(!aTuMatch)
					{
						// NOT expected
						_display("**************************");
						_display("* all TU CASE failed");           
						_display("* better on next synchro ?");
						_display("**************************");
						tuCase--;
					}   
					else
					{
						if((tuIndex * tuCase)  == 30)
						{
							_display("ALL TU PASSED :)");   
							allTuPassed = true;
						}  
					}
				}
				catch(AssertionFailedError e)
				{
					errorOccured = true;

					// Exception NOT expected
					_display("**************************");
					_display("* ERROR DURING TU");           
					_display("* " + e.getMessage());
					_display("**************************");
				}
			}



			// -------------------------------------------------------------------------------------------

			private void checkTUCase1() throws AssertionFailedError
			{
				Data[] children = hsRoot.getChildren();
				assertEquals(2, children.length);
				assertTrue(children[0] instanceof Directory);
				assertTrue(children[1] instanceof Directory);
				assertTrue((children[0].getName().equals("1") && children[1].getName().equals("2"))
						|| (children[0].getName().equals("2") && children[1].getName().equals("1")));

				Data[] children1 = ((Directory)children[0]).getChildren();
				assertEquals(2, children1.length);
				assertTrue(children1[0] instanceof Parameter);
				assertTrue(children1[1] instanceof Parameter);
				assertTrue((children1[0].getName().equals("1") && children1[1].getName().equals("2"))
						|| (children1[0].getName().equals("2") && children1[1].getName().equals("1")));					

				Data[] children2 = ((Directory)children[1]).getChildren();
				assertEquals(2, children2.length);
				assertTrue(children2[0] instanceof Parameter);
				assertTrue(children2[1] instanceof Parameter);
				assertTrue((children2[0].getName().equals("1") && children2[1].getName().equals("2"))
						|| (children2[0].getName().equals("2") && children2[1].getName().equals("1")));

				if(children[0].getName().equals("1"))
				{
					if(children1[0].getName().equals("1"))
					{
						assertEquals(Data.TYPE_BOOL, ((Parameter)children1[0]).getType());
						assertEquals(Data.TYPE_INT, ((Parameter)children1[1]).getType());
						assertEquals(new Boolean(false), ((Parameter)children1[0]).getValue());
						assertEquals(new Integer(0), ((Parameter)children1[1]).getValue());
					}
					else
					{
						assertEquals(Data.TYPE_INT, ((Parameter)children1[0]).getType());
						assertEquals(Data.TYPE_BOOL, ((Parameter)children1[1]).getType());
						assertEquals(new Integer(0), ((Parameter)children1[0]).getValue());
						assertEquals(new Boolean(false), ((Parameter)children1[1]).getValue());						
					}

					if(children2[0].getName().equals("1"))
					{
						assertEquals(Data.TYPE_BOOL, ((Parameter)children2[0]).getType());
						assertEquals(Data.TYPE_STRING, ((Parameter)children2[1]).getType());
						assertEquals(new Boolean(false), ((Parameter)children2[0]).getValue());
						assertEquals(new String(""), ((Parameter)children2[1]).getValue());
					}
					else
					{
						assertEquals(Data.TYPE_STRING, ((Parameter)children2[0]).getType());
						assertEquals(Data.TYPE_BOOL, ((Parameter)children2[1]).getType());
						assertEquals(new String(""), ((Parameter)children2[0]).getValue());
						assertEquals(new Boolean(false), ((Parameter)children2[1]).getValue());
					}
				}
				else
				{
					if(children2[0].getName().equals("1"))
					{
						assertEquals(Data.TYPE_BOOL, ((Parameter)children2[0]).getType());
						assertEquals(Data.TYPE_INT, ((Parameter)children2[1]).getType());
						assertEquals(new Boolean(false), ((Parameter)children2[0]).getValue());
						assertEquals(new Integer(0), ((Parameter)children2[1]).getValue());
					}
					else
					{
						assertEquals(Data.TYPE_INT, ((Parameter)children2[0]).getType());
						assertEquals(Data.TYPE_BOOL, ((Parameter)children2[1]).getType());
						assertEquals(new Integer(0), ((Parameter)children2[0]).getValue());
						assertEquals(new Boolean(false), ((Parameter)children2[1]).getValue());						
					}

					if(children1[0].getName().equals("1"))
					{
						assertEquals(Data.TYPE_BOOL, ((Parameter)children1[0]).getType());
						assertEquals(Data.TYPE_STRING, ((Parameter)children1[1]).getType());
						assertEquals(new Boolean(false), ((Parameter)children1[0]).getValue());
						assertEquals(new String(""), ((Parameter)children1[1]).getValue());
					}
					else
					{
						assertEquals(Data.TYPE_STRING, ((Parameter)children1[0]).getType());
						assertEquals(Data.TYPE_BOOL, ((Parameter)children1[1]).getType());
						assertEquals(new String(""), ((Parameter)children1[0]).getValue());
						assertEquals(new Boolean(false), ((Parameter)children1[1]).getValue());
					}
				}
			}

			// -------------------------------------------------------------------------------------------

			private void checkTUCase2() throws AssertionFailedError
			{
				Data[] children = hsRoot.getChildren();
				assertEquals(2, children.length);
				assertTrue(children[0] instanceof Directory);
				assertTrue(children[1] instanceof Directory);
				assertTrue((children[0].getName().equals("1") && children[1].getName().equals("2"))
						|| (children[0].getName().equals("2") && children[1].getName().equals("1")));

				Data[] children1 = ((Directory)children[0]).getChildren();
				assertEquals(2, children1.length);
				assertTrue(children1[0] instanceof Parameter);
				assertTrue(children1[1] instanceof Parameter);
				assertTrue((children1[0].getName().equals("1") && children1[1].getName().equals("2"))
						|| (children1[0].getName().equals("2") && children1[1].getName().equals("1")));					

				Data[] children2 = ((Directory)children[1]).getChildren();
				assertEquals(2, children2.length);
				assertTrue(children2[0] instanceof Parameter);
				assertTrue(children2[1] instanceof Parameter);
				assertTrue((children2[0].getName().equals("1") && children2[1].getName().equals("2"))
						|| (children2[0].getName().equals("2") && children2[1].getName().equals("1")));

				if(children[0].getName().equals("1"))
				{
					if(children1[0].getName().equals("1"))
					{
						assertEquals(Data.TYPE_BOOL, ((Parameter)children1[0]).getType());
						assertEquals(Data.TYPE_INT, ((Parameter)children1[1]).getType());
						assertEquals(new Boolean(true), ((Parameter)children1[0]).getValue());
						assertEquals(new Integer(10), ((Parameter)children1[1]).getValue());
					}
					else
					{
						assertEquals(Data.TYPE_INT, ((Parameter)children1[0]).getType());
						assertEquals(Data.TYPE_BOOL, ((Parameter)children1[1]).getType());
						assertEquals(new Integer(10), ((Parameter)children1[0]).getValue());
						assertEquals(new Boolean(true), ((Parameter)children1[1]).getValue());						
					}

					if(children2[0].getName().equals("1"))
					{
						assertEquals(Data.TYPE_BOOL, ((Parameter)children2[0]).getType());
						assertEquals(Data.TYPE_STRING, ((Parameter)children2[1]).getType());
						assertEquals(new Boolean(true), ((Parameter)children2[0]).getValue());
						assertEquals(new String("my_value"), ((Parameter)children2[1]).getValue());
					}
					else
					{
						assertEquals(Data.TYPE_STRING, ((Parameter)children2[0]).getType());
						assertEquals(Data.TYPE_BOOL, ((Parameter)children2[1]).getType());
						assertEquals(new String("my_value"), ((Parameter)children2[0]).getValue());
						assertEquals(new Boolean(true), ((Parameter)children2[1]).getValue());
					}
				}
				else
				{
					if(children2[0].getName().equals("1"))
					{
						assertEquals(Data.TYPE_BOOL, ((Parameter)children2[0]).getType());
						assertEquals(Data.TYPE_INT, ((Parameter)children2[1]).getType());
						assertEquals(new Boolean(true), ((Parameter)children2[0]).getValue());
						assertEquals(new Integer(10), ((Parameter)children2[1]).getValue());
					}
					else
					{
						assertEquals(Data.TYPE_INT, ((Parameter)children2[0]).getType());
						assertEquals(Data.TYPE_BOOL, ((Parameter)children2[1]).getType());
						assertEquals(new Integer(10), ((Parameter)children2[0]).getValue());
						assertEquals(new Boolean(true), ((Parameter)children2[1]).getValue());						
					}

					if(children1[0].getName().equals("1"))
					{
						assertEquals(Data.TYPE_BOOL, ((Parameter)children1[0]).getType());
						assertEquals(Data.TYPE_STRING, ((Parameter)children1[1]).getType());
						assertEquals(new Boolean(true), ((Parameter)children1[0]).getValue());
						assertEquals(new String("my_value"), ((Parameter)children1[1]).getValue());
					}
					else
					{
						assertEquals(Data.TYPE_STRING, ((Parameter)children1[0]).getType());
						assertEquals(Data.TYPE_BOOL, ((Parameter)children1[1]).getType());
						assertEquals(new String("my_value"), ((Parameter)children1[0]).getValue());
						assertEquals(new Boolean(true), ((Parameter)children1[1]).getValue());
					}
				}
			}

			// -------------------------------------------------------------------------------------------

			private void checkTUCase3() throws AssertionFailedError
			{
				Data[] children = hsRoot.getChildren();
				assertEquals(2, children.length);
				assertTrue(children[0] instanceof Directory);
				assertTrue(children[1] instanceof Directory);
				assertTrue((children[0].getName().equals("1") && children[1].getName().equals("2"))
						|| (children[0].getName().equals("2") && children[1].getName().equals("1")));

				Data[] children1 = ((Directory)children[0]).getChildren();
				assertEquals(1, children1.length);
				assertTrue(children1[0] instanceof Parameter);
				assertEquals("1", children1[0].getName());			

				Data[] children2 = ((Directory)children[1]).getChildren();
				assertEquals(1, children2.length);
				assertTrue(children2[0] instanceof Parameter);
				assertEquals("1", children2[0].getName());	

				if(children[0].getName().equals("1"))
				{
					assertEquals(Data.TYPE_BOOL, ((Parameter)children1[0]).getType());
					assertEquals(new Boolean(true), ((Parameter)children1[0]).getValue());					
				}
				else
				{
					assertEquals(Data.TYPE_BOOL, ((Parameter)children2[0]).getType());
					assertEquals(new Boolean(true), ((Parameter)children2[0]).getValue());					
				}
			}

			// -------------------------------------------------------------------------------------------

			private void checkTUCase46() throws AssertionFailedError
			{
				Data[] children = hsRoot.getChildren();
				assertEquals(0, children.length);				
			}

			// -------------------------------------------------------------------------------------------

			private void checkTUCase5() throws AssertionFailedError
			{
				Directory currentDir = hsRoot;

				for(int i=1; i<=20; i++)
				{
					Data[] children = currentDir.getChildren();
					assertEquals(1, children.length);

					if(i<20)
					{
						assertTrue(children[0] instanceof Directory);
						assertEquals(new String("" + i), children[0].getName());
						currentDir = (Directory)children[0];
					}
					else
					{
						assertTrue(children[0] instanceof Parameter);
						assertEquals(new String("end_of_branch"), ((Parameter)children[0]).getValue());
					}
				}
			}


		};

		_display("**********************************");
		_display("**********************************");
		_display("******** SINGLE TREE TU **********");
		_display("**********************************");
		_display("**********************************");

		// create the listener
		CustomListener listener = new CustomListener();

		SendTask.setConnectionStatus(true);

		// register to notif on root
		hsRoot.addValueChangeListener(listener);


		try
		{
			// launch stimulator
			_launchStimulator("singleTree");

			// waiting TU execution
			while(!listener.isAllTuPassed()
					&& !listener.errorOccured())
			{
				Thread.sleep(1000);
			}

			// TU finished
			assertTrue(listener.isAllTuPassed());
			assertFalse(listener.errorOccured());

			stimulatorProcess.destroy();

			// unregister to notif on root
			hsRoot.removeValueChangeListener(listener);
		}
		catch(Exception e)
		{      
			// unregister to notif on root
			hsRoot.removeValueChangeListener(listener);

			// exception NOT expected
			assertTrue(false);
		}
	}

	// ==============================================================================
	// ==============================================================================



	public void test_twoTreesSynchro() throws Exception {

		// declare custom listener for test
		class CustomListener implements ValueChangeListener{

			private boolean allTuPassed = false;
			private boolean errorOccured = false;
			private Timer nextTuTimeout;        


			public boolean isAllTuPassed() 
			{
				return allTuPassed;
			}

			public boolean errorOccured() 
			{
				return errorOccured;
			}

			private void manageTimeout()
			{
				TimerTask task = new TimerTask() {

					@Override
					public void run() {
						_display("**************************");
						_display("Next TU timeout");
						_display("**************************");
						errorOccured = true;             
					}
				};

				if(nextTuTimeout != null)
				{
					nextTuTimeout.cancel();
				}
				nextTuTimeout = new Timer();
				nextTuTimeout.schedule(task, 10000);
			}

			public void valueChange(EventObject evt) 
			{            
				manageTimeout();

				if(hsRoot == null)
				{
					_display("Root = NULL");
					errorOccured = true;
					return;
				}

				if(errorOccured)
				{
					if(nextTuTimeout != null)
					{
						nextTuTimeout.cancel();
					}
					return;
				}

				_display("==========================");
				_display("      root updated");
				_display("==========================");
				_display("", hsRoot);
				_display("--------------------------");

				try
				{
					_display("launch TU CASE");
					checkTUCase();    
					_display("TU CASE passed");
					allTuPassed = true;
				}
				catch(AssertionFailedError e)
				{
					// exception NOT expected
					_display("**************************");
					_display("* TU CASE failed");
					_display("* " + e.getMessage());             
					_display("* better on next synchro ?");
					_display("**************************");
				}
			}


			// -------------------------------------------------------------------------------------------

			private void checkTUCase() throws AssertionFailedError
			{
				Data[] children = hsRoot.getChildren();
				assertEquals(2, children.length);
				assertTrue(children[0] instanceof Directory);
				assertTrue(children[1] instanceof Directory);
				assertTrue((children[0].getName().equals("A") && children[1].getName().equals("B"))
						|| (children[0].getName().equals("B") && children[1].getName().equals("A")));

				// A
				Data[] children1 = ((Directory)children[0]).getChildren();
				assertEquals(2, children1.length);
				assertTrue(children1[0] instanceof Directory);
				assertTrue(children1[1] instanceof Directory);
				assertTrue((children1[0].getName().equals("1") && children1[1].getName().equals("2"))
						|| (children1[0].getName().equals("2") && children1[1].getName().equals("1")));              

				assertTrue(((Directory)children[0]).contains("1.1.1.1"));
				assertTrue(((Directory)children[0]).contains("1.2"));
				assertTrue(((Directory)children[0]).contains("2.1.1.1"));
				assertTrue(((Directory)children[0]).contains("2.2.1.1"));
				assertTrue(((Directory)children[0]).contains("2.2.1.2"));

				// B
				Data[] children2 = ((Directory)children[1]).getChildren();
				assertEquals(2, children2.length);
				assertTrue(children2[0] instanceof Directory);
				assertTrue(children2[1] instanceof Directory);
				assertTrue((children2[0].getName().equals("1") && children2[1].getName().equals("2"))
						|| (children2[0].getName().equals("2") && children2[1].getName().equals("1")));

				assertTrue(((Directory)children[1]).contains("1.1.1.1"));
				assertTrue(((Directory)children[1]).contains("1.1.1.2"));
				assertTrue(((Directory)children[1]).contains("2.1.1"));
				assertTrue(((Directory)children[1]).contains("2.1.2"));
				assertTrue(((Directory)children[1]).contains("2.2"));
			}

			// -------------------------------------------------------------------------------------------


		};

		_display("**********************************");
		_display("**********************************");
		_display("********* TWO TREES TU ***********");
		_display("**********************************");
		_display("**********************************");

		// create the listener
		CustomListener listener = new CustomListener();

		SendTask.setConnectionStatus(true);

		// register to notif on root
		hsRoot.addValueChangeListener(listener);

		// create a local tree
		// will be syncrobnized with Stimulator one
		hsRoot.newData("B.1.1.1.1", Data.TYPE_BOOL, true);
		hsRoot.newData("B.1.1.1.2", Data.TYPE_BOOL, true);
		hsRoot.newData("B.2.1.1", Data.TYPE_BOOL, true);
		hsRoot.newData("B.2.1.2", Data.TYPE_BOOL, true);
		hsRoot.newData("B.2.2", Data.TYPE_BOOL, true);



		try
		{
			// launch stimulator
			_launchStimulator("twoTrees");

			// waiting TU execution
			while(!listener.isAllTuPassed()
					&& !listener.errorOccured())
			{
				Thread.sleep(1000);
			}

			// TU finished
			assertTrue(listener.isAllTuPassed());
			assertFalse(listener.errorOccured());

			stimulatorProcess.destroy();

			// unregister to notif on root
			hsRoot.removeValueChangeListener(listener);
		}
		catch(IOException e)
		{
			// unregister to notif on root
			hsRoot.removeValueChangeListener(listener);

			// exception NOT expected
			assertTrue(false);
		}

	}

	// ==============================================================================
	// ==============================================================================



	public void test_stressedUpdate() throws Exception {

		// declare custom listener for test
		class CustomListener implements ValueChangeListener{

			private boolean allTuPassed = false;
			private boolean errorOccured = false;
			private Timer nextTuTimeout;       
			
			private int desiredValue = 5000;
			private long desiredTransmissionTime = 20; // 20ms between update and notif reception


			public boolean isAllTuPassed() 
			{
				return allTuPassed;
			}

			public boolean errorOccured() 
			{
				return errorOccured;
			}

			private void manageTimeout()
			{
				TimerTask task = new TimerTask() {

					@Override
					public void run() {
						_display("**************************");
						_display("Next TU timeout");
						_display("**************************");
						errorOccured = true;             
					}
				};

				if(nextTuTimeout != null)
				{
					nextTuTimeout.cancel();
				}
				nextTuTimeout = new Timer();
				nextTuTimeout.schedule(task, 10000);
			}

			public void valueChange(EventObject evt) 
			{    
				long currentTimeMillis = System.currentTimeMillis();
				
				manageTimeout();

				if(hsRoot == null)
				{
					_display("Root = NULL");
					errorOccured = true;
					return;
				}

				if(errorOccured)
				{
					if(nextTuTimeout != null)
					{
						nextTuTimeout.cancel();
					}
					return;
				}
									
				_display("==========================");
				_display("      root updated");
				_display("==========================");
				_display("", hsRoot);
				_display("--------------------------");
							
				try
				{			
					_display("launch TU CASE");
					checkTUCase(currentTimeMillis);    
					_display("TU CASE passed");
					
					if(desiredValue <= desiredTransmissionTime)
					{
						allTuPassed = true;
					}
				}
				catch(AssertionFailedError e)
				{
					// exception NOT expected
					_display("**************************");
					_display("* TU CASE failed");
					_display("* " + e.getMessage());       
					_display("* better on next synchro ?");
					_display("**************************");
				}
			}


			// -------------------------------------------------------------------------------------------

			private void checkTUCase(long currentTimeMillis) throws AssertionFailedError
			{         

				assertTrue(hsRoot.contains("A.delay_before_next_update"));
				assertTrue(hsRoot.contains("A.sent_at"));

				try 
				{
					assertEquals(Data.TYPE_INT, ((Parameter)hsRoot.getParameter("A.delay_before_next_update")).getType());
					assertEquals(Data.TYPE_STRING, ((Parameter)hsRoot.getParameter("A.sent_at")).getType());
					
					assertEquals(new Integer(desiredValue), ((Parameter)hsRoot.getParameter("A.delay_before_next_update")).getValue());
					
					long sentTimeMillis = Long.parseLong((String)((Parameter)hsRoot.getParameter("A.sent_at")).getValue());
					long delay = (currentTimeMillis - sentTimeMillis);
					
					_display("transmission takes : " + delay + "ms");
					
					errorOccured = delay > desiredTransmissionTime;
					
					assertFalse("transmission of update takes more than " + desiredTransmissionTime + "ms", errorOccured);									
					
					desiredValue = desiredValue/2;
				} 
				catch (DataAccessException e) 
				{
					errorOccured = true;
					e.printStackTrace();
				}
				
			}

			// -------------------------------------------------------------------------------------------


		};

		_display("**********************************");
		_display("**********************************");
		_display("****** STRESSSED UPDATE TU *******");
		_display("**********************************");
		_display("**********************************");

		// create the listener
		CustomListener listener = new CustomListener();

		SendTask.setConnectionStatus(true);

		// register to notif on root
		hsRoot.addValueChangeListener(listener);


		try
		{
			// launch stimulator
			_launchStimulator("stressedUpdate");

			// waiting TU execution
			while(!listener.isAllTuPassed()
					&& !listener.errorOccured())
			{
				Thread.sleep(1000);
			}

			// TU finished
			assertTrue(listener.isAllTuPassed());
			assertFalse(listener.errorOccured());

			stimulatorProcess.destroy();

			// unregister to notif on root
			hsRoot.removeValueChangeListener(listener);
		}
		catch(Exception e)
		{      
			// unregister to notif on root
			hsRoot.removeValueChangeListener(listener);

			// exception NOT expected
			assertTrue(false);
		}
	}


	// ==============================================================================
	// ==============================================================================

	private static void _simpleDisplay(String msg)
	{
		System.out.println(msg);
	}

	private static void _display(String msg)
	{
		System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] \t TU          =>  "  + msg);
	}

	private static void _display(String prefix, Directory dir)
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
				if (!isDir) { line += "=" + ((Parameter)data[i]).getValue(); }
				//if ((treeDisplayMode & 2) != 0) { line += " " + data[i].fullRevisionToString(); }
				_display(line);
				if (isDir)
				{
					String pr = (prefix.length() == 0 ? " |_" : "    " + prefix);
					_display(pr, (Directory)data[i]);
				}
			}
		}
	}

	private static void _launchStimulator(String argument) throws IOException
	{
		_display("LAUNCH STIMULATOR");

		String java = "";
		String stimulator = "";
		File stimulorJarPath = null;

		String OS = System.getProperty("os.name").toLowerCase();
		_display("OS : " + OS);

		if (OS.startsWith("windows")) 
		{
			// On Windows
			java = System.getProperty("java.home") + "/bin/java.exe";
		} 
		else 
		{
			// Else (= on Jenkins : Debian)
			java = "java";
		} 
		_display("java : " + java);

		stimulorJarPath = new File( System.getProperty("user.dir") + "/../sds-tusynchro-stimulor/target");
		_display("stimulorJarPath : " + stimulorJarPath);

		class JarFilter implements FileFilter
		{
			public boolean accept(File arg0) {

				if(    arg0.isFile()
						&& arg0.getName().endsWith(".jar"))
				{
					return true;
				}
				return false;
			}
		};
		File[] targetFiles = stimulorJarPath.listFiles(new JarFilter());
		for(int i=0 ;i<targetFiles.length; i++)
		{
			File target = targetFiles[i];
			if(target.getName().endsWith("-jar-with-dependencies.jar"))
			{
				stimulator = stimulorJarPath + "/" + target.getName();
				break;
			}
		}
		if (OS.startsWith("windows")) 
		{
			// On Windows
			//stimulator = "\"" + stimulator + "\"";
		} 
		else 
		{
			// Else (= on Jenkins : Debian)
			stimulator = stimulator.replaceAll(" ", "\\ ");			
		} 
		_display("stimulator : " + stimulator);

		if(!stimulator.isEmpty())
		{
			class StreamGobbler extends Thread {
				InputStream is;

				StreamGobbler(InputStream is) {
					this.is = is;
				}

				public void run() {
					try {
						InputStreamReader isr = new InputStreamReader(is);
						BufferedReader br = new BufferedReader(isr);
						String line = null;
						while ((line = br.readLine()) != null)
						{
							_simpleDisplay(line);
						}
					}catch(IOException e){
						e.printStackTrace();
					}
				}
			}

			String[] command = new String[] { 	java,
					"-jar",
					stimulator,
					"" + deviceId2,
					argument
			};

			String commandeLine = "";
			for(String data : command)
			{
				commandeLine += data + " ";
			}						
			_display("commandeLine : " + commandeLine);

			Runtime rt = Runtime.getRuntime();
			stimulatorProcess = rt.exec(command);
			StreamGobbler errorGobbler = new StreamGobbler(stimulatorProcess.getErrorStream());
			StreamGobbler outputGobbler = new StreamGobbler(stimulatorProcess.getInputStream());
			errorGobbler.start();
			outputGobbler.start();


		}
		else
		{
			assertTrue(false); // stimulator missing
		}
	}
}
