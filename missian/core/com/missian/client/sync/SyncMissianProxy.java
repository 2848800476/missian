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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.client.HessianRuntimeException;
import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.services.server.AbstractSkeleton;
import com.missian.client.TransportURL;
import com.missian.common.util.Constants;

/**
 * @author stanley
 * 
 */
public class SyncMissianProxy implements InvocationHandler, Serializable {
	private static final Logger log = LoggerFactory.getLogger(SyncMissianProxy.class);
	private String host;
	private int port;
	private String beanName;
	private SyncMissianProxyFactory _factory;
	private WeakHashMap<Method, String> _mangleMap = new WeakHashMap<Method, String>();

	public SyncMissianProxy(TransportURL url,
			SyncMissianProxyFactory syncMissianProxyFactory) {
		super();
		this.host = url.getHost();
		this.port = url.getPort();
		this.beanName = url.getQuery();
		this._factory = syncMissianProxyFactory;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1380891452634342681L;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
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

				if (!(proxyHandler instanceof SyncMissianProxy))
					return Boolean.FALSE;

				SyncMissianProxy handler = (SyncMissianProxy) proxyHandler;

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

		InputStream is = null;
		Socket conn = null;

		try {
			if (log.isDebugEnabled()) {
				log.debug("Missian[" + toString() + "] calling " + mangleName);
			}

			conn = sendRequest(mangleName, args);

			is = conn.getInputStream();
			
			AbstractHessianInput in;

			int code = is.read();

			if (code == 'H') {
				is.read();//int major
				is.read();//int minor  

				in = _factory.getHessian2Input(is);

				Object value = in.readReply(method.getReturnType());

				return value;
			} else if (code == 'r') {
				is.read();//int major
				is.read();//int minor  
				
				in = _factory.getHessianInput(is);

				in.startReplyBody();

				Object value = in.readObject(method.getReturnType());

				if (value instanceof InputStream) {
					value = new ResultInputStream(conn, is, in,
							(InputStream) value);
					is = null;
					conn = null;
				} else
					in.completeReply();

				return value;
			} else
				throw new HessianProtocolException("'" + (char) code
						+ "' is an unknown code");
		} catch (HessianProtocolException e) {
			throw new HessianRuntimeException(e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception e) {
				log.error(e.toString(), e);
			}

			try {
				if (conn != null)
					_factory.destroySocket(host, port, conn);
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}
	}

	private Socket sendRequest(String mangleName, Object[] args) throws Exception {
		Socket conn = _factory.getSocket(host, port);
		boolean isValid = false;

		try {

			OutputStream os = null;
			
			try {
				os = conn.getOutputStream();
			} catch (Exception e) {
				throw new HessianRuntimeException(e);
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
			
			
			AbstractHessianOutput out = _factory.getHessianOutput(baos);
			out.call(mangleName, args);
			out.flush();
//			os.write(baos.size());
			byte[] beanNameBytes = beanName.getBytes(Constants.BEAN_NAME_CHARSET);
			byte[] headerBytes = new byte[beanNameBytes.length+9];
			//header[0] = 0 for a sync call, do not need to set 
			System.arraycopy(intToByteArray(beanNameBytes.length), 0, headerBytes, 1, 4);
			System.arraycopy(beanNameBytes, 0, headerBytes, 5, beanNameBytes.length);
			System.arraycopy(intToByteArray(baos.size()), 0, headerBytes, 5+beanNameBytes.length, 4);
			
			os.write(headerBytes);
			baos.writeTo(os);
			
			isValid = true;

			return conn;
		} finally {
			if (!isValid && conn != null)
				_factory.destroySocket(host, port, conn);
		}
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
		SyncMissianProxy other = (SyncMissianProxy) obj;
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
		return "SyncMissianProxy [beanName=" + beanName + ", port=" + port
				+ ", host=" + host + "]";
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