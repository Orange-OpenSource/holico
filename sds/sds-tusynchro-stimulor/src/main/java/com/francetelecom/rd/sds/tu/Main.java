/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.sds.sds-tusynchro-stimulor
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
package com.francetelecom.rd.sds.tu;



import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;
import com.francetelecom.rd.sds.HomeSharedData;
import com.francetelecom.rd.sds.impl.HomeSharedDataImpl;
import com.francetelecom.rd.sds.impl.SendTask;

public class Main {

	// configure log system to use configuration file from classpath : 
	static {
		final InputStream inputStream = Main.class.getResourceAsStream("/logging.properties");
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

	HomeSharedData hsd;
	Directory root;

	int waitingDelayBetween2TU_step1 = 5000;
	int waitingDelayBetween2TU_step2 = 4000;
	int waitingDelayBetween2TU_step3 = 3000;
	int waitingDelayBetween2TU_step4 = 2000;
	int waitingDelayBetween2TU_step5 = 1000;

	int disconnectedDelay = 10000;

	// ==============================================================================
	// ==============================================================================

	public Main(int sdsId) {

		assert (sdsId > 0 && sdsId < 125);

		hsd = HomeSharedDataImpl.getInstance();
		assert (hsd != null);

		root = hsd.getRootDirectory(true, null, sdsId);
		assert (root != null);
	}

	// ==============================================================================
	// ==============================================================================

	public static void main(String[] args) throws InterruptedException {

		if(args.length != 2)
		{
			printUsage();
			return;
		}

		int sdsId = Integer.parseInt(args[0]);
		if (sdsId < 1 || sdsId > 125) 
		{
			printUsage();
			return;
		}

		String tuType = new String(args[1]);
		if(	   !tuType.equals("singleTree") 
			&& !tuType.equals("twoTrees")
			&& !tuType.equals("stressedUpdate"))
		{
			printUsage();
			return;
		}

		System.out.println("Stimulator started with " + tuType + " mode");

		Main main = new Main(sdsId);

		if(tuType.equals("singleTree"))
		{
			main.startSingleTreeStimulation();		
		}
		else if(tuType.equals("twoTrees"))
		{
			main.startTwoTreesStimulation();		
		}
		else if(tuType.equals("stressedUpdate"))
		{
			main.startStressedUpdateStimulation();		
		}


		System.out.println("Stimulator stoped");
	}

	// ==============================================================================
	// ==============================================================================

	void startSingleTreeStimulation()
	{
		try {

			int waitingDelay = 0;

			for(int i=1; i<=5; i++)
			{
				if(i==1) waitingDelay = waitingDelayBetween2TU_step1;
				if(i==2) waitingDelay = waitingDelayBetween2TU_step2;
				if(i==3) waitingDelay = waitingDelayBetween2TU_step3;
				if(i==4) waitingDelay = waitingDelayBetween2TU_step4;
				if(i==5) waitingDelay = waitingDelayBetween2TU_step5;

				// 1. create a tree from scratch
				// root
				// |_ 1
				//    |_ 11 (bool)
				//    |_ 12 (int)
				// |_ 2
				//    |_ 21 (bool)
				//    |_ 22 (string)	
				_display("TU CASE 1 (" + i + ") => create default tree from scratch");
				_display("start action for TU CASE 1 (" + i + ")");
				hsd.lock();
				root.newData("1", Data.TYPE_GEN_DIR, true);
				root.newData("1.1", Data.TYPE_BOOL, true);
				root.newData("1.2", Data.TYPE_INT, true);
				root.newData("2", Data.TYPE_GEN_DIR, true);
				root.newData("2.1", Data.TYPE_BOOL, true);
				root.newData("2.2", Data.TYPE_STRING, true);
				hsd.unlock();

				// TU CASE 1
				_display("ready for TU CASE 1 (" + i + ")");
				// wait => local tree creation + notif 		
				for(int j=0; j<10; j++)
				{
					_display("waiting...");
					Thread.sleep(waitingDelay/10);
				}

				// 2. set values
				_display("TU CASE 2 (" + i + ") => set parameters values");
				_display("start action for TU CASE 2 (" + i + ")");
				hsd.lock();
				root.setParameterValue("1.1", new Boolean(true));
				root.setParameterValue("1.2", new Integer(10));
				root.setParameterValue("2.1", new Boolean(true));
				root.setParameterValue("2.2", new String("my_value"));
				hsd.unlock();

				// TU CASE 2
				_display("ready for TU CASE 2 (" + i + ")");
				// wait => local tree update + notif 
				for(int j=0; j<10; j++)
				{
					_display("waiting...");
					Thread.sleep(waitingDelay/10);
				}

				_display("TU CASE 3 (" + i + ") => delete parameters");
				// 3. delete parameters
				// root
				// |_ 1
				//    |_ 11 (bool)
				// |_ 2
				//    |_ 21 (bool)	
				_display("start action for TU CASE 3 (" + i + ")");
				hsd.lock();
				root.deleteData("1.2");
				root.deleteData("2.2");
				hsd.unlock();

				// TU CASE 3
				_display("ready for TU CASE 3 (" + i + ")");
				// wait => local tree update + notif 
				for(int j=0; j<10; j++)
				{
					_display("waiting...");
					Thread.sleep(waitingDelay/10);
				}

				// 4. remove all
				_display("TU CASE 4 (" + i + ") => remove all");
				// root
				_display("start action for TU CASE 4 (" + i + ")");
				hsd.lock();
				root.deleteData("1");
				root.deleteData("2");
				hsd.unlock();

				// TU CASE 4
				_display("ready for TU CASE 4 (" + i + ")");
				// wait => local tree update + notif 
				for(int j=0; j<10; j++)
				{
					_display("waiting...");
					Thread.sleep(waitingDelay/10);
				}

				// 5. add a long branch
				_display("TU CASE 5 (" + i + ") => create a long branch");
				// root				
				// |_ 1
				//    |_ 2
				//       |_ 3
				//          |_ ...
				//                |_ 20 (string)
				_display("start action for TU CASE 5 (" + i + ")");
				hsd.lock();
				root.newData("1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20", Data.TYPE_STRING, true);
				root.setParameterValue("1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20", new String("end_of_branch"));
				hsd.unlock();

				// TU CASE 5
				_display("ready for TU CASE 5 (" + i + ")");
				// wait => local tree update + notif 
				for(int j=0; j<10; j++)
				{
					_display("waiting...");
					Thread.sleep(waitingDelay/10);
				}

				// 6. remove all
				_display("TU CASE 6 (" + i + ") => remove all");
				// root	
				_display("start action for TU CASE 6 (" + i + ")");
				hsd.lock();
				root.deleteData("1");
				hsd.unlock();

				// TU CASE 6
				_display("ready for TU CASE 6 (" + i + ")");
				// wait => local tree update + notif 
				for(int j=0; j<10; j++)
				{
					_display("waiting...");
					Thread.sleep(waitingDelay/10);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void startTwoTreesStimulation()
	{
		try {
			// disconnect
			//SendTask.setConnectionStatus(false);
			//_display("disconnected an create a local tree");

			// TU could succeed if we wait here
			//Thread.sleep(5000);

			hsd.lock();
			root.newData("A.1.1.1.1", Data.TYPE_BOOL, true);
			root.newData("A.1.2", Data.TYPE_BOOL, true);
			root.newData("A.2.1.1.1", Data.TYPE_BOOL, true);
			root.newData("A.2.2.1.1", Data.TYPE_BOOL, true);
			root.newData("A.2.2.1.2", Data.TYPE_BOOL, true);
			hsd.unlock();

			// waiting before connection
			//Thread.sleep(disconnectedDelay);

			// connect to synchronize entire tree with remote one (TU)
			//SendTask.setConnectionStatus(true);
			//_display("reconnected to synchronize trees");

			while(true)
			{
				Thread.sleep(500);
			}

		} 
		catch (DataAccessException e) 
		{
			e.printStackTrace();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}

	void startStressedUpdateStimulation()
	{
		try 
		{
			int waitingDelay = 5000;

			_display("start action stress");
			hsd.lock();
			root.newData("A.sent_at", Data.TYPE_STRING, true);
			root.newData("A.delay_before_next_update", Data.TYPE_INT, true);
			root.setParameterValue("A.sent_at", new String("" + System.currentTimeMillis()));
			root.setParameterValue("A.delay_before_next_update", new Integer(waitingDelay));
			hsd.unlock();	

			while (waitingDelay > 20)
			{
				_display("waiting... (" + waitingDelay + "ms)");
				Thread.sleep(waitingDelay);

				waitingDelay = waitingDelay / 2;

				_display("update resource");
				hsd.lock();
				root.setParameterValue("A.sent_at", new String("" + System.currentTimeMillis()));			
				root.setParameterValue("A.delay_before_next_update", new Integer(waitingDelay));
				hsd.unlock();
			}	
		}
		catch (DataAccessException e) 
		{
			e.printStackTrace();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}

	// ==============================================================================
	// ==============================================================================

	private void _display(String msg)
	{
		System.err.println("[" + new Timestamp(System.currentTimeMillis()) + "] \t Stimulator  =>  "  + msg);
	}

	private static void printUsage()
	{
		System.out.println("usage     : java -jar sds-tusynchro-stimulor.jar <sdsId> <tu_type>");
		System.out.println("<ssid>    : must be > 1 and < 125");
		System.out.println("<tu_type> : must be singleTree, twoTrees or stressedUpdate");
	}

}
