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

import com.missian.common.io.TransportProtocol;

/**
 * description:
 * Trasnport url
 * 
 * <pre>
 * TransportURL url = new TransportURL("tcp://localhost:8080/hello");
 * transport=tcp, host=localhost, host=8080, beanName=hello
 * </pre>
 * 
 */
public class TransportURL {
	private String url;
	private TransportProtocol transport;
	private String host;
	private int port;
	private String query;
	public TransportURL(String url) {
		super();
		this.url = url;
		int idx1 = url.indexOf("://");
		if(idx1<=0) {
			throw new IllegalArgumentException("Illegal url:"+url);
		}
		int idx2 = url.indexOf('/', idx1+4);
		if(idx2<=0) {
			throw new IllegalArgumentException("Illegal url:"+url);
		}
		transport = TransportProtocol.valueOf(url.substring(0, idx1).toLowerCase());
		query = url.substring(idx2+1);
		String hostPort = url.substring(idx1+3, idx2);
		int idx3 = hostPort.indexOf(':');
		if(idx3<0) {
			host = hostPort;
			port = transport.getDefaultPort();
		} else {
			host = hostPort.substring(0, idx3);
			port = Integer.parseInt(hostPort.substring(idx3+1));
		}
	}
	public String getUrl() {
		return url;
	}
	public TransportProtocol getTransport() {
		return transport;
	}
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}
	public String getQuery() {
		return query;
	}
}
