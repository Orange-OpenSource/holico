/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.holico.hlc-connector-impl
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
package com.francetelecom.rd.hlc.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.hlc.Resource;
import com.francetelecom.rd.sds.Data;
import com.francetelecom.rd.sds.Directory;
import com.francetelecom.rd.sds.HomeSharedData;
import com.francetelecom.rd.sds.Parameter;
import com.francetelecom.rd.sds.impl.HomeSharedDataImpl;

public class Tools {

	// ==============================================================================

	static SecureRandom secureRandom = null;

	final static Logger logger = LoggerFactory.getLogger(Tools.class.getName());

	// ==============================================================================

	public static void displayTreeRepresentation(int deviceId, Directory dir,
			String prefix, boolean rev) {

		if (prefix.equals("")) {
			logger.info("============================================");
		}

		HomeSharedData hsData = HomeSharedDataImpl.getInstance();

		Directory hsRoot = dir;
		if (hsRoot == null) {
			// FIXME deviceId : relation with HLC nodeId or device ID ?
			// 0-255 and not GUID ?
			boolean forceReinit = true;
			String filename = null;
			hsRoot = hsData.getRootDirectory(forceReinit, filename, deviceId);
		}

		Data[] data = hsRoot.getChildren();
		for (int i = 0; i < data.length; i++) {
			boolean isDir = data[i] instanceof Directory;
			String line = prefix + " " + data[i].getName();
			if (isDir) {
				line += (data[i].getType() == Data.TYPE_GEN_DIR ? "." : "[]");
			} else {
				Parameter param = (Parameter) data[i];
				if (param.getValue() != null) {
					line += " = " + param.getValue().toString();
				}

			}

			if (rev) {
				line += " " + data[i].fullRevisionToString();
			}
			logger.info(line);

			if (isDir) {
				String pr = (prefix.length() == 0 ? " \u2514\u2500\u2500"
						: "    " + prefix);
				displayTreeRepresentation(deviceId, (Directory) data[i], pr,
						rev);
			}
		}
	}

	// ==============================================================================

	public static void initializaidGeneration() {
		// Initialize SecureRandom
		// This is a lengthy operation, to be done only upon
		// initialization of the application
		try {
			if (secureRandom == null) {
				secureRandom = SecureRandom.getInstance("SHA1PRNG");
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static String generateId() {

		if (secureRandom == null) {
			initializaidGeneration();
		}
		// we cannot use the UUID class, it's not in java 1.4 ...
		String id = null;
		try {
			// generate a random number
			String randomNum = new Integer(secureRandom.nextInt()).toString();

			// get its digest
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] result = sha.digest(randomNum.getBytes());

			id = hexEncode(result);
			logger.info("new Id Generated : " + id);

		} catch (NoSuchAlgorithmException ex) {

			// should never happen !
			logger.error(
					"WTF, NoSuchAlgorithmException : cannot generate random Id ",
					ex);

		}

		// POST condition
		assert (id != null) && (id.length() != 0);
		return id;
	}

	/**
	 * The byte[] returned by MessageDigest does not have a nice textual
	 * representation, so some form of encoding is usually performed.
	 * 
	 * This implementation follows the example of David Flanagan's book
	 * "Java In A Nutshell", and converts a byte array into a String of hex
	 * characters.
	 * 
	 * Another popular alternative is to use a "Base64" encoding.
	 */
	static private String hexEncode(byte[] aInput) {
		// Precondition
		assert aInput != null && aInput.length != 0;

		// No string builder in java cdc ( ~=1.4) !!
		String result = new String();

		char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		for (int idx = 0; idx < aInput.length; ++idx) {
			byte b = aInput[idx];
			result = result + String.valueOf(digits[(b & 0xf0) >> 4]);
			result = result + String.valueOf(digits[b & 0x0f]);
		}

		// Post condition
		assert result != null && result.length() != 0;
		return result;
	}

	// ==============================================================================

	public static int getSdsTypeFromConnectorType(int type) {

		int res = -1;
		switch (type) {
		case Resource.TYPE_VALUE_BOOL:
			res = Data.TYPE_BOOL;
			break;
		case Resource.TYPE_VALUE_INT:
			res = Data.TYPE_INT;
			break;
		case Resource.TYPE_VALUE_STRING:
			res = Data.TYPE_STRING;
			break;
		case Resource.TYPE_SHARED:
			res = Data.TYPE_GEN_DIR;
			break;
		case Resource.TYPE_MULTI:
			res = Data.TYPE_SPE_DIR;
			break;
		}
		return res;
	}

	public static int getConnectorTypeFromSdsType(int type) {

		int res = -1;
		switch (type) {
		case Data.TYPE_BOOL:
			res = Resource.TYPE_VALUE_BOOL;
			break;
		case Data.TYPE_INT:
			res = Resource.TYPE_VALUE_INT;
			break;
		case Data.TYPE_STRING:
			res = Resource.TYPE_VALUE_STRING;
			break;
		case Data.TYPE_GEN_DIR:
			res = Resource.TYPE_SHARED;
			break;
		case Data.TYPE_SPE_DIR:
			res = Resource.TYPE_MULTI;
			break;
		}
		return res;
	}
}
