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
import java.io.File;

import junit.framework.TestCase;

import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;
import com.francetelecom.rd.sds.impl.HomeSharedDataImpl;

public class HomeSharedDataTest extends TestCase {

	// ==============================================================================

	int deviceId = 125;
	int badDeviceId = 135;

	String fileName = "testSDS.data";

	Directory hsRoot;

	// ==============================================================================

	protected void setUp() throws Exception {

		System.out
		.println(" Global setup " + HomeSharedDataTest.class.getName());
		
		// clean temp file
		File f = new File(fileName);
		if(f.exists())
		{
			f.delete();
		}
	}

	protected void tearDown() throws Exception {

		System.out.println(" Global tearDown "
				+ HomeSharedDataTest.class.getName());
		
		resetRoot();
	}

	private void resetRoot() {
		recursiveDelete(hsRoot);
		hsRoot = null;
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

	public void test_rootDirectoryCreationFromScratch() throws Exception {

		HomeSharedDataImpl hsd = HomeSharedDataImpl.getInstance();

		// create a root with a bad device id
		hsRoot = hsd.getRootDirectory(true, null, badDeviceId);
		assertNull(hsRoot);

		// create a root with a good device id
		hsRoot = hsd.getRootDirectory(true, null, deviceId);
		assertNotNull(hsRoot);

		// retreive the device id of the created root
		int devId = HomeSharedDataImpl.getDeviceId();
		assertEquals(deviceId, devId);
	}
	
	// ==============================================================================
	
	public void test_getRootDirectory() throws Exception {

		HomeSharedDataImpl hsd = HomeSharedDataImpl.getInstance();

		// create a root with a good device id
		hsRoot = hsd.getRootDirectory(true, null, deviceId);
		assertNotNull(hsRoot);

		// is the root recoverable ?
		Directory root = HomeSharedDataImpl.getRootDirectory();
		assertSame(hsRoot, root);
	}

	// ==============================================================================
	
	public void test_rootDirectorySaveFile() throws Exception {

		HomeSharedDataImpl hsd = HomeSharedDataImpl.getInstance();

		// create a root with a good device id
		hsRoot = hsd.getRootDirectory(true, null, deviceId);
		assertNotNull(hsRoot);

		// save root in file
		hsd.save(fileName);
		File f = new File(fileName);
		assertTrue(f.exists());
	}

	// ==============================================================================
	
	public void test_rootDirectoryCreationFromFile() throws Exception {

		HomeSharedDataImpl hsd = HomeSharedDataImpl.getInstance();

		// create a root with a good device id
		hsRoot = hsd.getRootDirectory(true, null, deviceId);
		assertNotNull(hsRoot);

		// save root in file
		hsd.save(fileName);
		File f = new File(fileName);
		assertTrue(f.exists());

		// reset the local root
		resetRoot();
		assertNull(hsRoot);

		// load the root from file, use a new device id
		hsRoot = hsd.getRootDirectory(false, fileName, deviceId+1);
		assertNotNull(hsRoot);
		
		// is the device id corresponds to the first one ?
		int devId = HomeSharedDataImpl.getDeviceId();
		assertEquals(deviceId, devId);
	}

	// ==============================================================================

}
