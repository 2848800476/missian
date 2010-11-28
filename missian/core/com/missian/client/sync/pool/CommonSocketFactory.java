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
package com.missian.client.sync.pool;

import java.net.Socket;

import org.apache.commons.pool.KeyedPoolableObjectFactory;

import com.missian.client.sync.SyncMissianProxyFactory;

public class CommonSocketFactory implements KeyedPoolableObjectFactory {
	private SyncMissianProxyFactory syncMissianProxyFactory;
	public CommonSocketFactory(SyncMissianProxyFactory syncMissianProxyFactory) {
		super();
		this.syncMissianProxyFactory = syncMissianProxyFactory;
	}

	@Override
	public void activateObject(Object key, Object obj) throws Exception {
		;
	}

	@Override
	public void destroyObject(Object key, Object obj) throws Exception {
		Socket socket = (Socket)obj;
		socket.close();
	}

	@Override
	public Object makeObject(Object key) throws Exception {
		ServerAddress address = (ServerAddress)key;
		return syncMissianProxyFactory.createSocket(address.getHost(), address.getPort());
	}

	@Override
	public void passivateObject(Object key, Object obj) throws Exception {
		;
	}

	@Override
	public boolean validateObject(Object key, Object obj) {
		Socket socket = (Socket)obj;
		return socket.isConnected();
	}

}
