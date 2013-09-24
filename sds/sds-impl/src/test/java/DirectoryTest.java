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
import java.util.EventObject;

import junit.framework.TestCase;

import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.DataAccessException;
import com.francetelecom.rd.sds.Directory;
import com.francetelecom.rd.sds.Parameter;
import com.francetelecom.rd.sds.ValueChangeListener;
import com.francetelecom.rd.sds.impl.DirectoryImpl;
import com.francetelecom.rd.sds.impl.HomeSharedDataImpl;

public class DirectoryTest extends TestCase {

	// ==============================================================================

	int deviceId = 125;
	String paramPath = "my_path";
	String dirPath = "my_dir";

	Directory hsRoot;

	// ==============================================================================

	protected void setUp() throws Exception {

		System.out
		.println(" Global setup " + DirectoryTest.class.getName());
					
		HomeSharedDataImpl hsd = HomeSharedDataImpl.getInstance();

		hsRoot = hsd.getRootDirectory(true, null, deviceId);
		assertNotNull(hsRoot);
	}

	protected void tearDown() throws Exception {

		System.out.println(" Global tearDown "
				+ DirectoryTest.class.getName());
		
		resetRoot();
	}

	private void resetRoot() {
		if(hsRoot == null) return;
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

	public void test_newData() throws Exception {

		// create a new boolean data
		Data myData = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData);

		// is the data path recoverable ?
		String path = myData.getPathname();
		assertEquals(path, path);

		// is the data type recoverable ?
		int type = myData.getType();
		assertEquals(Data.TYPE_BOOL, type);
	}

	public void test_newDataOverwrite() throws Exception {

		// create a new boolean data
		Data myData1 = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData1);

		// overwrite the data with same path ans type
		Data myData2 = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData2);

		// is always the same data ?
		assertSame(myData1, myData2);

		// overwrite the data with same path and diffrent type
		try 
		{			
			Data myData3 = hsRoot.newData(paramPath, Data.TYPE_INT, true);
			assertNotNull(myData3);
		} 
		catch(Exception e)
		{
			// exception NOT expected.
			assertTrue(false);
		}	
	}

	@SuppressWarnings("unused")
	public void test_newDataNoOverwrite() throws Exception {

		// create a new boolean data
		Data myData1 = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData1);

		// create a new int data with same path
		try 
		{
			Data myData2 = hsRoot.newData(paramPath, Data.TYPE_BOOL, false);
			assertTrue(false);
		} 
		catch(Exception e)
		{
			// exception expected.
			assertTrue(true);
		}		
	}

	@SuppressWarnings("unused")
	public void test_deleteData() throws Exception {

		// create a new boolean data
		Data myData = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData);

		// is the data in the root ?
		boolean isInRoot = hsRoot.contains(paramPath);
		assertTrue(isInRoot);

		// delete the data with wrong path
		try 
		{			
			Data deletedData = hsRoot.deleteData(paramPath + "_wrong");
			assertTrue(false);
		} 
		catch(Exception e)
		{
			// exception expected.
			assertTrue(true);
		}	

		// delete the data
		Data deletedData = hsRoot.deleteData(paramPath);

		// is the deleted data the same as desired ?
		assertSame(myData, deletedData);

		// is the data in the root ?
		isInRoot = hsRoot.contains(paramPath);
		assertFalse(isInRoot);
	}

	// ==============================================================================

	@SuppressWarnings("unused")
	public void test_getDirectory() throws Exception 
	{
		// create subDir
		Data subDir = hsRoot.newData(dirPath, Data.TYPE_GEN_DIR, true);
		assertNotNull(subDir);

		// create a parameter
		Data myData = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData);
				
		// retreive the sub dir
		Directory dir = hsRoot.getDirectory(dirPath);
		assertNotNull(dir);
		assertSame(subDir, dir);
		
		// retreive a bad Directory
		try 
		{			
			Directory dir2 = hsRoot.getDirectory(paramPath);
			assertTrue(false);
		} 
		catch(Exception e)
		{
			// exception expected.
			assertTrue(true);
		}		
	}
	
	@SuppressWarnings("unused")
	public void test_getParameter() throws Exception {

		// create subDir
		Data subDir = hsRoot.newData(dirPath, Data.TYPE_GEN_DIR, true);
		assertNotNull(subDir);
		
		// create a new boolean data
		Data myData1 = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData1);

		// retreive parameter as Object
		Parameter param = hsRoot.getParameter(paramPath);
		assertNotNull(param);
		assertSame(myData1, param);

		// retreive parameter with wrong path
		try 
		{			
			Parameter param2 = hsRoot.getParameter(paramPath + "_wrong");
			assertTrue(false);
		} 
		catch(Exception e)
		{
			// exception expected.
			assertTrue(true);
		}	

		// retreive parameter as boolean
		boolean paramBool = hsRoot.getParameterBooleanValue(paramPath);
		assertEquals(false, paramBool);

		// retreive parameter as int
		try 
		{			
			int paramInt = hsRoot.getParameterIntValue(paramPath);
			assertTrue(false);
		} 
		catch(Exception e)
		{
			// exception expected.
			assertTrue(true);
		}	
		
		// retreive a bad Parameter
		try 
		{			
			Parameter param3 = hsRoot.getParameter(dirPath);
			assertTrue(false);
		} 
		catch(Exception e)
		{
			// exception expected.
			assertTrue(true);
		}
	}

	public void test_setParameter() throws Exception {

		// create a new boolean data
		Data myData1 = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData1);

		// set a new value
		hsRoot.setParameterValue(paramPath, true);

		// retreive parameter as boolean
		boolean paramBool = hsRoot.getParameterBooleanValue(paramPath);
		assertEquals(true, paramBool);

		// set a new value with wrong path
		try 
		{			
			hsRoot.setParameterValue(paramPath + "_wrong", true);
			assertTrue(false);
		} 
		catch(Exception e)
		{
			// exception expected.
			assertTrue(true);
		}	

		// set a new value with wrong type
		try 
		{			
			hsRoot.setParameterValue(paramPath, 0);
			assertTrue(false);
		} 
		catch(Exception e)
		{
			// exception expected.
			assertTrue(true);
		}	
	}

	// ==============================================================================

	public void test_contains() throws Exception 
	{
		// create a new boolean data
		Data myData = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData);

		// is the data in the root ?
		boolean isInRoot = hsRoot.contains(paramPath);
		assertTrue(isInRoot);

		// is the data with wrong path in the root ?
		isInRoot = hsRoot.contains(paramPath + "_wrong");
		assertFalse(isInRoot);
	}

	// ==============================================================================

	public void test_getChild() throws Exception 
	{
		// create a new boolean data
		Data myData = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData);

		// retreive the child
		Data child = hsRoot.getChild(paramPath);
		assertSame(myData, child);

		// retreive the child with wrong path
		child = hsRoot.getChild(paramPath + "_wrong");
		assertNull(child);
	}

	public void test_getChildNames() throws Exception 
	{
		// create subDir
		Data subDir = hsRoot.newData(dirPath, Data.TYPE_GEN_DIR, true);
		assertNotNull(subDir);

		// create children from root
		Data child1 = hsRoot.newData(dirPath + ".1", Data.TYPE_BOOL, true);
		assertNotNull(child1);
		// create children from subDir
		Data child2 = ((Directory)subDir).newData("2", Data.TYPE_BOOL, true);
		assertNotNull(child2);

		// retreive child names
		String[] childNames = hsRoot.getChildNames(dirPath);
		assertEquals(2, childNames.length);
		assertTrue((childNames[0].equals("1") && childNames[1].equals("2"))
				|| (childNames[0].equals("2") && childNames[1].equals("1")));
	}

	public void test_getChildren() throws Exception 
	{
		// create subDir
		Data subDir = hsRoot.newData(dirPath, Data.TYPE_GEN_DIR, true);
		assertNotNull(subDir);

		// create children from root
		Data child1 = hsRoot.newData(dirPath + ".1", Data.TYPE_BOOL, true);
		assertNotNull(child1);
		// create children from subDir
		Data child2 = ((Directory)subDir).newData("2", Data.TYPE_BOOL, true);
		assertNotNull(child2);

		// retreive root children
		Data[] rootChildren = hsRoot.getChildren();
		assertEquals(1, rootChildren.length);
		assertEquals(dirPath, rootChildren[0].getPathname());
		
		// retreive sub dir children
		Data[] subdirChildren = ((Directory)subDir).getChildren();
		assertEquals(2, subdirChildren.length);
		assertTrue((subdirChildren[0].getName().equals("1") && subdirChildren[1].getName().equals("2"))
				|| (subdirChildren[0].getName().equals("2") && subdirChildren[1].getName().equals("1")));
	}
	
	// ==============================================================================

	public void test_addValueChangeListenerCurrentDir() throws Exception 
	{		
		// declare custom listener for test
		class CustomListener implements ValueChangeListener{

			int nbOfNotif = 0;
			
			@Override
			public void valueChange(EventObject evt) {
				nbOfNotif++;
			}
			
			public int getNbOfNotif() {
				return nbOfNotif;
			}
		};
		
		// create the listener
		CustomListener listener = new CustomListener();
		
		// register to notif on root
		hsRoot.addValueChangeListener(listener);
		
		// create a new boolean data
		Data myData1 = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData1);
		
		// waiting for notif
		Thread.sleep(200);
		
		// the notif must has been fired
		assertEquals(1, listener.getNbOfNotif());
		
		// create a new boolean data
		Data myData2 = hsRoot.newData(paramPath + "2", Data.TYPE_BOOL, true);
		assertNotNull(myData2);
		
		// waiting for notif
		Thread.sleep(200);
		
		// the second notif must has been fired
		assertEquals(2, listener.getNbOfNotif());		
	}
	
	public void test_addValueChangeListenerOnSubDir() throws Exception 
	{		
		// declare custom listener for test
		class CustomListener implements ValueChangeListener{

			int nbOfNotif = 0;
			
			@Override
			public void valueChange(EventObject evt) {
				nbOfNotif++;
			}
			
			public int getNbOfNotif() {
				return nbOfNotif;
			}
		};
		
		// create the listener
		CustomListener listener = new CustomListener();
		
		// create subDir
		Data subDir = hsRoot.newData(dirPath, Data.TYPE_GEN_DIR, true);
		assertNotNull(subDir);
		
		// register to notif on bad sub dir
		try 
		{			
			hsRoot.addValueChangeListener(dirPath + "_wrong", listener);
			assertTrue(false);
		} 
		catch(Exception e)
		{
			// exception expected.
			assertTrue(true);
		}				
				
		// register to notif on sub dir
		hsRoot.addValueChangeListener(dirPath, listener);
		
		// create a new boolean data
		Data myData1 = ((Directory)subDir).newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData1);
		
		// waiting for notif
		Thread.sleep(200);
		
		// the notif must has been fired
		assertEquals(1, listener.getNbOfNotif());					
	}
	
	public void test_removeValueChangeListenerCurrentDir() throws Exception 
	{		
		// declare custom listener for test
		class CustomListener implements ValueChangeListener{

			int nbOfNotif = 0;
			
			@Override
			public void valueChange(EventObject evt) {
				nbOfNotif++;
			}
			
			public int getNbOfNotif() {
				return nbOfNotif;
			}
		};
		
		// create the listener
		CustomListener listener = new CustomListener();
		
		// register to notif on root
		hsRoot.addValueChangeListener(listener);
		
		// create a new boolean data
		Data myData1 = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData1);
		
		// waiting for notif
		Thread.sleep(200);
		
		// the notif must has been fired
		assertEquals(1, listener.getNbOfNotif());
		
		// unregister to notif on root
		hsRoot.removeValueChangeListener(listener);
				
		// create a new boolean data
		Data myData2 = hsRoot.newData(paramPath + "2", Data.TYPE_BOOL, true);
		assertNotNull(myData2);
		
		// waiting for notif
		Thread.sleep(200);
		
		// the second notif was not fired
		assertEquals(1, listener.getNbOfNotif());		
	}
	
}
