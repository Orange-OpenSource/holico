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
import com.francetelecom.rd.sds.DataChangeListener;
import com.francetelecom.rd.sds.impl.DirectoryImpl;
import com.francetelecom.rd.sds.impl.HomeSharedDataImpl;

public class ParameterTest extends TestCase {

	// ==============================================================================

	String paramPath = "my_path";
	String dirPath = "my_dir";

	Directory hsRoot;

	// ==============================================================================

	protected void setUp() throws Exception {

		System.out
		.println(" Global setup " + ParameterTest.class.getName());
					
		HomeSharedDataImpl hsd = HomeSharedDataImpl.getInstance();

		hsRoot = hsd.getRootDirectory(true, null, null);
		assertNotNull(hsRoot);
	}

	protected void tearDown() throws Exception {

		System.out.println(" Global tearDown "
				+ ParameterTest.class.getName());
		
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

	public void test_getSetValue() throws Exception {

		// create a new data
		Data myData = hsRoot.newData(paramPath, Data.TYPE_BOOL, true);
		assertNotNull(myData);

		// get the param
		Parameter param = hsRoot.getParameter(paramPath);
		assertNotNull(param);
		
		// get the default value
		Object val = param.getValue();
		assertTrue(val instanceof Boolean);
		Boolean valBool = (Boolean)val;
		assertFalse(valBool.booleanValue());

		// set a new value with bad type
		try 
		{			
			param.setValue(new Integer(0));
			assertTrue(false);
		} 
		catch(Exception e)
		{
			// exception expected.
			assertTrue(true);
		}	
		
		// set a new value 
		param.setValue(new Boolean(true));
		
		// get the new value
		Object val2 = param.getValue();
		assertTrue(val2 instanceof Boolean);
		Boolean valBool2 = (Boolean)val2;
		assertTrue(valBool2.booleanValue());		
	}
}
