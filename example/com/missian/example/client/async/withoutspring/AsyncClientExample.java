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
package com.missian.example.client.async.withoutspring;


import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.missian.client.async.AsyncFuture;
import com.missian.client.async.AsyncMissianProxyFactory;
import com.missian.example.bean.client.Hello;


/**
 * description:
 *
 */
public class AsyncClientExample {
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		AsyncMissianProxyFactory factory = new AsyncMissianProxyFactory(new SimpleBeanLocator());
		factory.setHessian2Request(true);
		factory.setHessian2Response(true);
		Hello hello = (Hello)factory.create(Hello.class, "http://localhost:1235/hello");
		AsyncFuture<String> future = hello.hello("gg", 25, String.class);
		System.out.println(future.get());
	}
}
