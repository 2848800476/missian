/*
 *	 Copyright [2010] Stanley Ding(Dingshengyu)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   Missian is based on hessian, mina and spring. Any project who uses 
 *	 missian must agree to hessian, mima and spring's license.
 *	  Hessian: http://hessian.caucho.com/
 *    Mina:http://mina.apache.org
 *	  Spring(Optional):http://www.springsource.org/	 
 *
 *   @author stanley
 *	 @date 2010-11-28
 */
package com.missian.client.sync;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.hessian.io.AbstractHessianInput;

public class ResultInputStream extends InputStream {
	private static final Logger log = Logger.getLogger(ResultInputStream.class
			.getName());
	private Socket _conn;
	private InputStream _connIs;
	private AbstractHessianInput _in;
	private InputStream _hessianIs;

	ResultInputStream(Socket conn, InputStream is,
			AbstractHessianInput in, InputStream hessianIs) {
		_conn = conn;
		_connIs = is;
		_in = in;
		_hessianIs = hessianIs;
	}

	public int read() throws IOException {
		if (_hessianIs != null) {
			int value = _hessianIs.read();

			if (value < 0)
				close();

			return value;
		} else
			return -1;
	}

	public int read(byte[] buffer, int offset, int length) throws IOException {
		if (_hessianIs != null) {
			int value = _hessianIs.read(buffer, offset, length);

			if (value < 0)
				close();

			return value;
		} else
			return -1;
	}

	public void close() throws IOException {
		Socket conn = _conn;
		_conn = null;

		InputStream connIs = _connIs;
		_connIs = null;

		AbstractHessianInput in = _in;
		_in = null;

		InputStream hessianIs = _hessianIs;
		_hessianIs = null;

		try {
			if (hessianIs != null)
				hessianIs.close();
		} catch (Exception e) {
			log.log(Level.FINE, e.toString(), e);
		}

		try {
			if (in != null) {
				in.completeReply();
				in.close();
			}
		} catch (Exception e) {
			log.log(Level.FINE, e.toString(), e);
		}

		try {
			if (connIs != null) {
				connIs.close();
			}
		} catch (Exception e) {
			log.log(Level.FINE, e.toString(), e);
		}

		try {
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			log.log(Level.FINE, e.toString(), e);
		}
	}
}