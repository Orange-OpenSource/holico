/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.sds.sds-test
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
package com.francetelecom.rd.sds.tests;

import java.util.EventObject;
import java.util.Timer;
import java.util.TimerTask;

import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;
import com.francetelecom.rd.sds.HomeSharedData;
import com.francetelecom.rd.sds.Parameter;
import com.francetelecom.rd.sds.ValueChangeListener;
import com.francetelecom.rd.sds.impl.HomeSharedDataImpl;

public class MainTest {

	HomeSharedData hsd;
	Directory root;

	Timer timer;
	static String publishPath ;
	private int publishCounter;

	public MainTest(int sdsId) {

		assert (sdsId > 0 && sdsId < 125);

		hsd = HomeSharedDataImpl.getInstance();
		assert (hsd != null);

		root = hsd.getRootDirectory(true, null, sdsId);
		assert (root != null);
		
		timer = new Timer();

	}

	public static void main(String[] args) throws InterruptedException {
		System.out.println("usage : <sdsId> <command> <arg>");
		System.out.println(" where <command> = listen | publish");

		int sdsId = Integer.parseInt(args[0]);
		if (sdsId < 1 || sdsId > 125) {
			System.out.println(" sdsId must be > 1 and < 125 ");
			return;
		}

		MainTest main = new MainTest(sdsId);

		String command = args[1];
		if ("listen".equals(command)) {

			String path = args[2];

			main.startListener(path);

		} else if ("publish".equals(command)) {
			publishPath = args[2];

			main.startPeriodicDataUpdate(publishPath);
		}

		while (true) {
			Thread.sleep(10000);

		}
	}

	void startListener(final String path) {

		System.out.println("Trying to listen for changes on " + path);

		if (root.contains( path)) {

			try {
				root.addValueChangeListener(path, new ValueChangeListener() {
	
					@Override
					public void valueChange(EventObject evt) {
	
						Data data = (Data) evt.getSource();
						System.out.println("Value Changed for Path "
								+ data.getPathname());
						if (data instanceof Parameter) {
	
							Parameter param = (Parameter) data;
							System.out.println("It's a param, new value is "
									+ param.getValue());
	
						} else if (data instanceof Directory) {
	
							System.out.println(" It's a directory");
	
						}
	
					}
				});
			} catch (DataAccessException e) {
				e.printStackTrace();
			}
		}
		else {
			
			// the tree does not contains the path
			// either it's a wrong path, or we hav not synchronized this part of the
			// path yet
			
			System.out.print("Path not found, trying again in 5 sec");
			
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					startListener(path);
					
				}
			}, 5000);
			
		}
	}

	void startPeriodicDataUpdate(String path) {

		try {

			Data data = root.newData(path, Data.TYPE_INT, true);
			assert data != null;
			publishCounter = 0;
			publish(path, publishCounter);

		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		
		timer.schedule(new PublishTimerTask(), 10000);

	}

	void publish(String path, int value) {
		Data data;
		try {
			data = root.getChild(path);

			if (data instanceof Parameter) {
				
				System.out.println("Publishing " + publishCounter + " at " + path );
				((Parameter) data).setValue(new Integer(publishCounter));
				
			} else if (data instanceof Directory) {
				
				System.out.println("!!! Path " + path
						+ " is a directory, cannot publish on it !!!");
			}

		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}

	class PublishTimerTask extends TimerTask {

		@Override
		public void run() {

			publish(publishPath, publishCounter);
			publishCounter++;
			timer.schedule(new PublishTimerTask(), 10000);

		}
	}

}
