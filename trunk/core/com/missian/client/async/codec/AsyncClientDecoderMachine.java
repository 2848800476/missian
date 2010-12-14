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
package com.missian.client.async.codec;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

import org.apache.asyncweb.common.MutableHttpResponse;
import org.apache.asyncweb.common.codec.HttpResponseDecodingState;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;
import org.apache.mina.filter.codec.statemachine.DecodingStateMachine;
import org.apache.mina.filter.codec.statemachine.FixedLengthDecodingState;
import org.apache.mina.filter.codec.statemachine.IntegerDecodingState;

import com.missian.client.async.message.AsyncClientResponse;
import com.missian.common.io.IoBufferInputStream;
import com.missian.common.util.Constants;

/**
 * description:
 *
 */
public class AsyncClientDecoderMachine extends DecodingStateMachine {
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
		int childs = childProducts.size();
		for(int i=0; i<childs; i=i+4) {
			AsyncClientResponse response = new AsyncClientResponse();
			response.setBeanName((String)childProducts.get(i));
			response.setMethodName((String)childProducts.get(i+1));
			response.setSequence((Integer)childProducts.get(i+2));
			response.setInputStream((InputStream)childProducts.get(i+3));
			out.write(response);
			charsetDecoder.reset();	
		}
		return null;
	}

	@Override
	protected DecodingState init() throws Exception {
		return checkTransportState;
	}
	
	private DecodingState checkTransportState = new DecodingState() {

		public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out)
				throws Exception {
			if(in.hasRemaining()) {
				byte b = in.get();
				if(b!=0 && b!=1) {
					in.position(in.position()-1);//HTTP-decoding-machine will decode this byte again
					return httpDecodingState;
				} else {
					return beanNameState;
				}
			}
			return this;
		}

		public DecodingState finishDecode(ProtocolDecoderOutput out)
				throws Exception {
			return null;
		}
		
	};
	
	private DecodingState beanNameState = new IntegerDecodingState() {
		
		@Override
		protected DecodingState finishDecode(int s, ProtocolDecoderOutput out)
				throws Exception {
			return new FixedLengthDecodingState(s) {
				
				@Override
				protected DecodingState finishDecode(IoBuffer product,
						ProtocolDecoderOutput out) throws Exception {
					out.write(product.getString(charsetDecoder));
					return methodNameState;
				}
			};
		}
	};
	
	private DecodingState methodNameState = new IntegerDecodingState() {
		
		@Override
		protected DecodingState finishDecode(int s, ProtocolDecoderOutput out)
				throws Exception {
			return new FixedLengthDecodingState(s) {
				
				@Override
				protected DecodingState finishDecode(IoBuffer product,
						ProtocolDecoderOutput out) throws Exception {
					out.write(product.getString(charsetDecoder));
					return sequenceState;
				}
			};
		}
	}; 
	
	
	private DecodingState sequenceState = new IntegerDecodingState() {

		@Override
		protected DecodingState finishDecode(int value,
				ProtocolDecoderOutput out) throws Exception {
			out.write(value);
			return bodyState;
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
	
	private HttpResponseDecodingState httpDecodingState = new HttpResponseDecodingState() {

		@Override
		protected DecodingState finishDecode(List<Object> childProducts,
				ProtocolDecoderOutput out) throws Exception {
			for(Object child: childProducts) {
				MutableHttpResponse response = (MutableHttpResponse)child;
				String beanName = response.getHeader(Constants.HTTP_HEADER_BEANNAME);
				String methodName = response.getHeader(Constants.HTTP_HEADER_METHOD);
				String sequence = response.getHeader(Constants.HTTP_HEADER_SEQ);
				IoBuffer content = response.getContent();
				out.write(beanName);
				out.write(methodName);
				out.write(Integer.parseInt(sequence));
				out.write(new IoBufferInputStream(content));
			}
			return null;
		}
		
	};
}
