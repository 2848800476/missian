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

import org.apache.commons.pool.impl.GenericKeyedObjectPool;

import com.missian.client.sync.SyncMissianProxyFactory;

public class CommonSocketPool implements SocketPool {
	private GenericKeyedObjectPool pool;
	
	public CommonSocketPool(SyncMissianProxyFactory syncMissianProxyFactory) {
        pool = new GenericKeyedObjectPool(new CommonSocketFactory(syncMissianProxyFactory));
    }

    /**
     * Create a new <code>CommonSocketPool</code> using the specified values.
     * @param factory the <code>KeyedPoolableObjectFactory</code> to use to create, validate, and destroy objects
     * if not <code>null</code>
     * @param config a non-<code>null</code> {@link CommonSocketPool.Config} describing the configuration
     */
    public CommonSocketPool(SyncMissianProxyFactory syncMissianProxyFactory, GenericKeyedObjectPool.Config config) {
    	pool = new GenericKeyedObjectPool(new CommonSocketFactory(syncMissianProxyFactory), config);
    }

	
	
	
	public Socket getSocket(String host, int port) throws Exception {
		return (Socket)pool.borrowObject(new ServerAddress(host, port));
	}

	public void returnSocket(String host, int port, Socket socket) throws Exception {
		pool.returnObject(new ServerAddress(host, port), socket);
	}

}
