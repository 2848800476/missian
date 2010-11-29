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
package com.missian.client;
import java.io.InputStream;
import java.io.OutputStream;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.caucho.hessian.io.SerializerFactory;

/**
 * description:
 * Base proxy factory class.
 */
public abstract class MissianProxyFactory {
	private ClassLoader loader;
	private SerializerFactory serializerFactory;
	// tcp configurations
	private int connectTimeout = 10;
	private boolean tcpNoDelay = true;
	private boolean reuseAddress = true;
	private int soLinger = -1;
	private int sendBufferSize = 256;
	private int receiveBufferSize = 1024;

	// hessian configurationso
	private boolean hessian2Request;
	private boolean hessian2Response;
	private boolean overloadEnabled;
	public void setOverloadEnabled(boolean overloadEnabled) {
		this.overloadEnabled = overloadEnabled;
	}

	

	public MissianProxyFactory() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public MissianProxyFactory(ClassLoader loader) {
		this.loader = loader;
	}

	public boolean isHessian2Request() {
		return hessian2Request;
	}

	public void setHessian2Request(boolean hessian2Request) {
		this.hessian2Request = hessian2Request;
	}

	public boolean isHessian2Response() {
		return hessian2Response;
	}

	public void setHessian2Response(boolean hessian2Response) {
		this.hessian2Response = hessian2Response;
	}

	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public void setReceiveBufferSize(int receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
	}

	public ClassLoader getLoader() {
		return loader;
	}

	public void setLoader(ClassLoader loader) {
		this.loader = loader;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}

	public void setTcpNoDelay(boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
	}

	public boolean isReuseAddress() {
		return reuseAddress;
	}

	public void setReuseAddress(boolean reuseAddress) {
		this.reuseAddress = reuseAddress;
	}

	public int getSoLinger() {
		return soLinger;
	}

	public void setSoLinger(int soLinger) {
		this.soLinger = soLinger;
	}

	public int getSendBufferSize() {
		return sendBufferSize;
	}

	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

	public AbstractHessianOutput getHessianOutput(OutputStream os) {
		AbstractHessianOutput out;

		if (hessian2Request)
			out = new Hessian2Output(os);
		else {
			HessianOutput out1 = new HessianOutput(os);
			out = out1;

			if (hessian2Response)
				out1.setVersion(2);
		}

		out.setSerializerFactory(getSerializerFactory());

		return out;
	}

	public AbstractHessianInput getHessianInput(InputStream is) {
		return getHessian2Input(is);
	}

	public AbstractHessianInput getHessian1Input(InputStream is) {
		AbstractHessianInput in;

		in = new HessianInput(is);

		in.setSerializerFactory(getSerializerFactory());

		return in;
	}

	public AbstractHessianInput getHessian2Input(InputStream is) {
		AbstractHessianInput in;

		in = new Hessian2Input(is);

		in.setSerializerFactory(getSerializerFactory());

		return in;
	}

	/**
	 * Gets the serializer factory.
	 */
	public SerializerFactory getSerializerFactory() {
		if (serializerFactory == null)
			serializerFactory = new SerializerFactory(loader);

		return serializerFactory;
	}

	public boolean isOverloadEnabled() {
		return overloadEnabled;
	}
}
