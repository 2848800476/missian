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

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.statemachine.DecodingStateProtocolDecoder;

import com.missian.common.util.Constants;

public class MissianCodecFactory implements ProtocolCodecFactory {
	
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		ProtocolDecoder decoder = (ProtocolDecoder)session.getAttribute(Constants.DECODER);
		if(decoder!=null) {
			return decoder;
		}
		synchronized (session) {
			decoder = (ProtocolDecoder)session.getAttribute(Constants.DECODER);
			if(decoder==null) {
				decoder = new DecodingStateProtocolDecoder(new MissianDecoderMachine());
				session.setAttribute(Constants.DECODER, decoder);
			}			
		}
		return decoder;
	}

	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return new MissianEncoder();
	}

}
