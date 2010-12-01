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
package com.missian.client.async;

import java.io.InputStream;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.HessianProtocolException;
import com.missian.client.async.message.AsyncClientResponse;


public class AsyncClientHandler extends IoHandlerAdapter {
//	private BeanLocator beanLocator;
	private AsyncMissianProxyFactory _factory;
	private Logger log = LoggerFactory.getLogger(AsyncClientHandler.class);
	public AsyncClientHandler(AsyncMissianProxyFactory factory) {
		_factory = factory;
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		session.close(true);
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		AsyncClientResponse response = (AsyncClientResponse) message;
		String beanName = response.getBeanName();
		
		String methodName = response.getMethodName();
		Callback callback = _factory.getCallBack(beanName, methodName);
		if(callback == null) {
			return;
		}
		InputStream is = response.getInputStream();
		AbstractHessianInput in;

		int code = is.read();

		if (code == 'H') {
			is.read();//int major
			is.read();//int minor  

			in = _factory.getHessian2Input(is);

			Object value = null;
			try {
				value = in.readReply(callback.getAcceptValueType());
				callback.call(value);
			} catch (Throwable e) {
				log.error("callback failed. bean={}, method={}, value={}", new Object[]{beanName, methodName, value});
				log.error("callback failed", e);
			}

			
		} else if (code == 'r') {
			is.read();//int major = 
			is.read();//int minor = 

			in = _factory.getHessianInput(is);

			try {
				in.startReplyBody();
			} catch (Throwable e) {
				log.error("callback failed. bean={}, method={}", new Object[]{beanName, methodName});
				log.error("callback failed", e);
				return;
			}

			Object value = in.readObject(callback.getAcceptValueType());

			in.completeReply();

			callback.call(value);
		} else
			throw new HessianProtocolException("'" + (char) code
					+ "' is an unknown code");
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		session.close(false);
	}
}
