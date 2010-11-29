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
package com.missian.common.io;

import java.io.IOException;
import java.io.InputStream;

import org.apache.mina.core.buffer.IoBuffer;

/**
 *  
 * It works for HessianInputStream and Hessian2InputStream, but Do NOT use this class anywhere else.
 *
 */
public class IoBufferInputStream extends InputStream {
	private IoBuffer buffer;
	public IoBufferInputStream(IoBuffer buffer) {
		this.buffer = buffer;
	}
	@Override
	public int read() throws IOException {
		return buffer.get();
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	};

	public int read(byte[] b, int off, int len)
			throws IOException {
		len = Math.min(len, buffer.remaining());
		len = Math.min(len, b.length);
		buffer.get(b, off, len);
		return len;
	};
}