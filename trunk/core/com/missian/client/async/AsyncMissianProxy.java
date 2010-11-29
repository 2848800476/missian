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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.caucho.hessian.client.HessianRuntimeException;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.services.server.AbstractSkeleton;
import com.missian.client.TransportProtocol;
import com.missian.client.TransportURL;
import com.missian.client.async.message.AsyncClientRequest;
import com.missian.common.beanlocate.BeanLocator;
import com.missian.common.io.IoBufferOutputStream;

/**
 * description:
 * Async proxy implementation for missian clients.  Applications will generally
 * use AsyncMissianProxyFactory to create proxy clients.
 */
public class AsyncMissianProxy implements InvocationHandler, Serializable {
	private static final Logger log = Logger.getLogger(AsyncMissianProxy.class
			.getName());
	private String host;
	private int port;
	private String beanName;
	private AsyncMissianProxyFactory _factory;
	private BeanLocator beanLocator;
	private TransportProtocol transportProtocol;
	public AsyncMissianProxy(BeanLocator beanLocator, TransportURL url, AsyncMissianProxyFactory asyncMissianProxyFactory) throws IOException {
		super();
		this.host = url.getHost();
		this.port = url.getPort();
		this.beanName = url.getQuery();
		this._factory = asyncMissianProxyFactory;
		this.beanLocator = beanLocator;
		this.transportProtocol = url.getTransport();
	}

	private WeakHashMap<Method, String> _mangleMap = new WeakHashMap<Method, String>();

	/**
	 * 
	 */
	private static final long serialVersionUID = -138089145263434181L;

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		CallbackTarget async = method.getAnnotation(CallbackTarget.class);
		Callback cb = async == null ? null : (Callback)beanLocator.lookup(async.value());
		String mangleName;

		synchronized (_mangleMap) {
			mangleName = _mangleMap.get(method);
		}

		if (mangleName == null) {
			String methodName = method.getName();
			Class<?>[] params = method.getParameterTypes();

			// equals and hashCode are special cased
			if (methodName.equals("equals") && params.length == 1
					&& params[0].equals(Object.class)) {
				Object value = args[0];
				if (value == null || !Proxy.isProxyClass(value.getClass()))
					return Boolean.FALSE;

				Object proxyHandler = Proxy.getInvocationHandler(value);

				if (!(proxyHandler instanceof AsyncMissianProxy))
					return Boolean.FALSE;

				AsyncMissianProxy handler = (AsyncMissianProxy) proxyHandler;

				return equals(handler);
			} else if (methodName.equals("hashCode") && params.length == 0)
				return hashCode();
			else if (methodName.equals("getHessianType"))
				return proxy.getClass().getInterfaces()[0].getName();
			else if (methodName.equals("getHessianURL"))
				return "sync://" + host + ":" + port + "/" + beanName;
			else if (methodName.equals("toString") && params.length == 0)
				return toString();

			if (!_factory.isOverloadEnabled())
				mangleName = method.getName();
			else
				mangleName = mangleName(method);

			synchronized (_mangleMap) {
				_mangleMap.put(method, mangleName);
			}
		}

		try {
			if (log.isLoggable(Level.FINER))
				log.finer("Missian[" + toString() + "] calling " + mangleName);
			if(cb!=null) {
				_factory.setCallback(beanName, mangleName, cb);
			}
			sendRequest(mangleName, args);
			return null;
		} catch (HessianProtocolException e) {
			throw new HessianRuntimeException(e);
		} finally {
			
		}
	}
	
	private void sendRequest(String mangleName, Object[] args) throws IOException {
		try {
			AsyncClientRequest request = new AsyncClientRequest();
			request.setBeanName(beanName);
			request.setTransportProtocol(transportProtocol);
			request.setHost(host);
			request.setPort(port);
			IoBufferOutputStream baos = new IoBufferOutputStream(_factory.getInitBufSize());		
			AbstractHessianOutput out = _factory.getHessianOutput(baos);
			out.call(mangleName, args);
			out.flush();
			
			IoBuffer body = baos.flip();
			request.setOutputBuffer(body);

			getSession().write(request);
		} finally {
			
		}
	}

	/**
	 * @return
	 */
	private IoSession getSession() {
		return _factory.getIoSession(host, port);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((beanName == null) ? 0 : beanName.hashCode());
		result = prime * result + port;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AsyncMissianProxy other = (AsyncMissianProxy) obj;
		if (beanName == null) {
			if (other.beanName != null)
				return false;
		} else if (!beanName.equals(other.beanName))
			return false;
		if (port != other.port)
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AsyncMissianProxy [beanName=" + beanName + ", port=" + port
				+ ", server=" + host + "]";
	}

	protected String mangleName(Method method) {
		Class<?>[] param = method.getParameterTypes();

		if (param == null || param.length == 0)
			return method.getName();
		else
			return AbstractSkeleton.mangleName(method, false);
	}

	public static final byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16),
				(byte) (value >>> 8), (byte) value };
	}

	public static final int byteArrayToInt(byte[] b) {
		return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8)
				+ (b[3] & 0xFF);
	}

}
