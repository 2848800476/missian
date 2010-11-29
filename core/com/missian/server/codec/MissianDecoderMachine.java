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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

import org.apache.asyncweb.common.MutableHttpRequest;
import org.apache.asyncweb.common.codec.HttpRequestDecodingStateMachine;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;
import org.apache.mina.filter.codec.statemachine.DecodingStateMachine;
import org.apache.mina.filter.codec.statemachine.FixedLengthDecodingState;
import org.apache.mina.filter.codec.statemachine.IntegerDecodingState;

import com.missian.client.TransportProtocol;
import com.missian.common.io.IoBufferInputStream;
import com.missian.common.util.Constants;

public class MissianDecoderMachine extends DecodingStateMachine {
	private CharsetDecoder charsetDecoder = Charset.forName(Constants.BEAN_NAME_CHARSET).newDecoder();
	@Override
	protected void destroy() throws Exception {
		charsetDecoder.reset();
	}

	@Override
	protected DecodingState finishDecode(List<Object> childProducts,
			ProtocolDecoderOutput out) throws Exception {
		if(childProducts.size()<4) {
			return null;
		}
		TransportProtocol transport = (TransportProtocol)childProducts.get(0);
		int childs = childProducts.size();
		for(int i=1; i<childs; i=i+3) {
			MissianRequest request = new MissianRequest();
			request.setTransportProtocol(transport);
			request.setAsync((Boolean)childProducts.get(i));
			request.setBeanName((String)childProducts.get(i+1));
			request.setInputStream((InputStream)childProducts.get(i+2));
			out.write(request);
			charsetDecoder.reset();
		}

		return null;
	}

	@Override
	protected DecodingState init() throws Exception {
//		System.out.println(this);
		return checkTransportState;
	}
	
	private DecodingState checkTransportState = new DecodingState() {

		public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out)
				throws Exception {
			if(in.hasRemaining()) {
				byte b = in.get();
				if(b!=0 && b!=1) {
					out.write(TransportProtocol.http);
					in.position(in.position()-1);//HTTP-decoding-machine will decode this byte again
					return httpDecodingStateMachine;
				} else {
					out.write(TransportProtocol.tcp);//transport
					out.write(b==1);//async/sync flag. 1==async, 0==sync
					return beanNameLengthState;
				}
			}
			return this;
		}

		public DecodingState finishDecode(ProtocolDecoderOutput out)
				throws Exception {
			return null;
		}
		
	};
	
	private DecodingState beanNameLengthState = new IntegerDecodingState() {
		
		@Override
		protected DecodingState finishDecode(int s, ProtocolDecoderOutput out)
				throws Exception {
			return new FixedLengthDecodingState(s) {
				
				@Override
				protected DecodingState finishDecode(IoBuffer product,
						ProtocolDecoderOutput out) throws Exception {
					out.write(product.getString(charsetDecoder));
					return bodyState;
				}
			};
		}
	}; 
	
	private DecodingState bodyState = new IntegerDecodingState() {
		
		@Override
		protected DecodingState finishDecode(int value, ProtocolDecoderOutput out)
				throws Exception {
			return new FixedLengthDecodingState(value) {
				
				@Override
				protected DecodingState finishDecode(IoBuffer product,
						ProtocolDecoderOutput out) throws Exception {
					out.write(new IoBufferInputStream(product));
					return null;
				}
			};
		}
	};
	
    
    private HttpRequestDecodingStateMachine httpDecodingStateMachine = new HttpRequestDecodingStateMachine() {

		@Override
		protected DecodingState finishDecode(List<Object> childProducts,
				ProtocolDecoderOutput out) throws Exception {
			for(Object child : childProducts){
				MutableHttpRequest request = (MutableHttpRequest)child;
				String async = request.getHeader(Constants.HTTP_HEADER_ASYNC);
				out.write(async!=null && async.equals("true"));//async-flag
				String beanName = request.getRequestUri().getPath().substring(1);
				out.write(beanName);
				out.write(new IoBufferInputStream(request.getContent()));
			}
			return null;
		}
    	
    };
}
