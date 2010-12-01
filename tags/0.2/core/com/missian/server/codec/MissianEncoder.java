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
package com.missian.server.codec;

import org.apache.asyncweb.common.DefaultHttpResponse;
import org.apache.asyncweb.common.HttpHeaderConstants;
import org.apache.asyncweb.common.HttpResponseStatus;
import org.apache.asyncweb.common.HttpVersion;
import org.apache.asyncweb.common.codec.HttpResponseEncoder;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.missian.common.io.TransportProtocol;
import com.missian.common.util.Constants;


public class MissianEncoder implements ProtocolEncoder {
	private HttpResponseEncoder httpResponseEncoder = new HttpResponseEncoder();
	public void dispose(IoSession session) throws Exception {
		// TODO Auto-generated method stub

	}

	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {
		MissianResponse resp = (MissianResponse)message;
		if(resp.getTransportProtocol()==TransportProtocol.tcp) {
			IoBuffer buf = IoBuffer.allocate(session.getConfig().getReadBufferSize());
			buf.setAutoExpand(true);
			buf.put((byte)(resp.isAsync() ? 1 : 0));//async or sync
			if(resp.isAsync()) {
				byte[] beanNameBytes = resp.getBeanName().getBytes(Constants.BEAN_NAME_CHARSET);
				buf.putInt(beanNameBytes.length);
				buf.put(beanNameBytes);
				byte[] methodNameBytes = resp.getMethodName().getBytes(Constants.BEAN_NAME_CHARSET);
				buf.putInt(methodNameBytes.length);
				buf.put(methodNameBytes);
				buf.putInt(resp.getOutputBuffer().limit());
			}
			buf.put(resp.getOutputBuffer());
			buf.flip();
			out.write(buf);
		} else {
			DefaultHttpResponse httpResponse = new DefaultHttpResponse();
			
			httpResponse.setStatus(HttpResponseStatus.OK);
			httpResponse.setProtocolVersion(HttpVersion.HTTP_1_1);
			httpResponse.setContentType("application/x-hessian");
			httpResponse.setKeepAlive(true);
			httpResponse.setHeader(HttpHeaderConstants.KEY_SERVER, "Missian");
			if(resp.isAsync()) {
				httpResponse.setHeader(Constants.HTTP_HEADER_ASYNC, "true");
				httpResponse.setHeader(Constants.HTTP_HEADER_BEANNAME, resp.getBeanName());
				httpResponse.setHeader(Constants.HTTP_HEADER_METHOD, resp.getMethodName());				
			}
			
			IoBuffer body = resp.getOutputBuffer();
			httpResponse.addHeader("Content-Length", String.valueOf(body.limit()));
			httpResponse.setContent(body);
			
			httpResponseEncoder.encode(session, httpResponse, out);
		}
	}

}
