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
package com.missian.example.client.sync;

import java.io.IOException;

import com.missian.client.sync.SyncMissianProxyFactory;
import com.missian.client.sync.pool.CommonSocketPool;
import com.missian.example.bean.Hello;

public class SyncClientExample {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		SyncMissianProxyFactory factory = new SyncMissianProxyFactory();
		factory.setReadTimeout(100);
		factory.setSocketPool(new CommonSocketPool(factory));
		Hello hello = (Hello)factory.create(Hello.class, "http://localhost:8080/hessianTest/hessian", Thread.currentThread().getContextClassLoader());
		System.out.println(hello.hello("hy", 27));
	}

}
