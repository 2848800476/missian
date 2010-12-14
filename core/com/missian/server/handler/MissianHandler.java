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
package com.missian.server.handler;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.caucho.hessian.io.SerializerFactory;
import com.missian.common.beanlocate.BeanLocator;
import com.missian.common.io.IoBufferOutputStream;
import com.missian.server.codec.MissianRequest;
import com.missian.server.codec.MissianResponse;

public class MissianHandler extends IoHandlerAdapter {
	private BeanLocator beanLocator;
	private SerializerFactory serializerFactory = new SerializerFactory();


	public MissianHandler(BeanLocator beanLocator) {
		super();
		this.beanLocator = beanLocator;
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		session.close(true);
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		MissianRequest request = (MissianRequest) message;
		Object bean = beanLocator.lookup(request.getBeanName());
		MissianSkeleton service = new MissianSkeleton(bean, bean.getClass());
		
		IoBufferOutputStream os = new IoBufferOutputStream();
		String methodName = service.invoke(request.getInputStream(), os, serializerFactory);
		
		MissianResponse resp = new MissianResponse();
		resp.setTransportProtocol(request.getTransportProtocol());
		resp.setAsync(request.isAsync());
		resp.setOutputBuffer(os.flip());
		resp.setBeanName(request.getBeanName());
		resp.setMethodName(methodName);
		resp.setSequence(request.getSequence());
		session.write(resp);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		session.close(false);
	}
}
