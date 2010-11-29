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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.caucho.hessian.io.HessianRemoteObject;
import com.missian.client.MissianProxyFactory;
import com.missian.client.TransportProtocol;
import com.missian.client.TransportURL;
import com.missian.client.async.codec.AsyncClientCodecFactory;
import com.missian.common.beanlocate.BeanLocator;
import com.missian.common.util.Constants;

/**
 * description:
 * Factory for creating async missian client stubs.  The returned stub will
 * call the remote object for all methods asynchronously.
 *
 * <pre>
 * String url = "tcp://localhost:8080/hello";
 * HelloHome hello = (HelloHome) factory.create(HelloHome.class, url);
 * </pre>
 *
 * After creation, the stub can be like a regular Java class.  Because
 * it makes remote calls, it can throw more exceptions than a Java class.
 * In particular, it may throw protocol exceptions.
 *
 * The factory can also be configured in spring.
 *
 * Within spring, the above example would be configured as:
 * <pre>
 * 	&lt;bean id="asyncMissianProxyFactory" class="com.missian.client.async.AsyncMissianProxyFactory" init-method="init" destroy-method="destroy">
 * 		&lt;constructor-arg name="transport" value="tcp"/>
 * 		&lt;constructor-arg name="beanLocator">
 * 			&lt;bean class="com.missian.common.beanlocate.SpringLocator"/>
 * 		&lt;/constructor-arg>
 * 	&lt;/bean>
 * </pre>
 *
 *
 * The SpringLocator is to lookup the callback object. 
 * the callback target of a remote method is configured as annotation:
 * <pre>	
 *  @CallbackTarget("helloCallback")
 *	public String hello(String name, int age);
 * </pre>
 * Then missian looks up a bean named 'helloCallback' in the spring context and
 * invokes the execute() method when received the returned object.
 *
 * <h3>Authentication</h3>
 *
 * <p>The proxy can use HTTP basic authentication if the user and the
 * password are set.
 */
public class AsyncMissianProxyFactory extends MissianProxyFactory {
	private static final int DEFAULT_THREAD_POOL = 4;
	private Map<String, Map<String, Callback>> callbackMap = new ConcurrentHashMap<String, Map<String, Callback>>();
	private int initBufSize = Constants.INIT_BUF_SIZE;
	private NioSocketConnector connector;
	private BeanLocator callbackLoacator;
	private int callbackIoProcesses;
	private boolean logBeforeCodec;
	private boolean logAfterCodec;
	private ExecutorService threadPool;
	private boolean threadPoolCreated;
	private TransportProtocol transport;
	private ConcurrentHashMap<String, IoSession> sessionMap = new ConcurrentHashMap<String, IoSession>();
	private ReentrantLock lock = new ReentrantLock();
		
	/**
	 * @param transport
	 * @param callbackLoacator
	 * @param threadPool
	 * @param callbackIoProcesses
	 * @param logBeforeCodec
	 * @param logAfterCodec
	 */
	public AsyncMissianProxyFactory(TransportProtocol transport, BeanLocator callbackLoacator, ExecutorService threadPool,
			int callbackIoProcesses, boolean logBeforeCodec,
			boolean logAfterCodec) {
		super();
		this.callbackLoacator = callbackLoacator;
		this.callbackIoProcesses = callbackIoProcesses;
		this.logBeforeCodec = logBeforeCodec;
		this.logAfterCodec = logAfterCodec;
		this.threadPool = threadPool;
		this.transport = transport;
	}

	public AsyncMissianProxyFactory(TransportProtocol transport, BeanLocator callbackLoacator, ExecutorService threadPool) {
		this(transport, callbackLoacator, threadPool, 1, false, true);
	}		
	
	public AsyncMissianProxyFactory(TransportProtocol transport, BeanLocator callbackLoacator, int threadPoolSize,
			int callbackIoProcesses, boolean logBeforeCodec,
			boolean logAfterCodec) {
		this(transport, callbackLoacator, Executors.newFixedThreadPool(threadPoolSize), callbackIoProcesses, logBeforeCodec, logAfterCodec);
		this.threadPoolCreated = true;
	}
	
	public AsyncMissianProxyFactory(TransportProtocol transport, BeanLocator callbackLoacator, int threadPoolSize) {
		this(transport, callbackLoacator, threadPoolSize, 1, false, true);
	}
	
	public AsyncMissianProxyFactory(TransportProtocol transport, BeanLocator callbackLoacator) {
		this(transport, callbackLoacator, DEFAULT_THREAD_POOL);
	}

	public int getInitBufSize() {
		return initBufSize;
	}

	public void setInitBufSize(int initBufSize) {
		this.initBufSize = initBufSize;
	}
	
	public void destroy() {
		connector.dispose();
		if(threadPoolCreated) {
			this.threadPool.shutdown();
		}
	}
		
	public void init() {
		connector = new NioSocketConnector(callbackIoProcesses);
		if(logBeforeCodec) {
			connector.getFilterChain().addLast("log.1", new LoggingFilter());
		}
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new AsyncClientCodecFactory()));
		connector.getFilterChain().addLast("log.2", new LoggingFilter());
		if(logAfterCodec) {
			connector.getFilterChain().addLast("executor", new ExecutorFilter(threadPool));
		}
		connector.getSessionConfig().setReadBufferSize(getReceiveBufferSize());
		connector.getSessionConfig().setSendBufferSize(getSendBufferSize());
		connector.getSessionConfig().setReuseAddress(isReuseAddress());
		connector.getSessionConfig().setTcpNoDelay(isTcpNoDelay());
		connector.getSessionConfig().setSoLinger(getSoLinger());		
		connector.setHandler(new AsyncClientHandler(this));
		connector.setConnectTimeoutMillis(getConnectTimeout()*1000);
	}
	/**
	 * The AsyncClientHandler calls this method to get the callback objects.
	 * @param beanName
	 * @param methodName
	 * @return
	 */
	Callback getCallBack(String beanName, String methodName) {
		Map<String, Callback> submap = callbackMap.get(beanName);
		return submap == null ? null : submap.get(methodName);
	}
	
	/**
	 * The proxy instances call this method to cache the callback objects.
	 * @param beanName
	 * @param methodName
	 * @param callback
	 */
	void setCallback(String beanName, String methodName, Callback callback) {
		Map<String, Callback> submap = callbackMap.get(beanName);
		if(submap==null) {
			submap = new ConcurrentHashMap<String, Callback>();
			callbackMap.put(beanName, submap);
		}
		submap.put(methodName, callback);
	}
	
	/**
	 * Create a remote stub for api class.
	 * @param api the interface to create stub for.
	 * @param url the url this stub talks to.
	 * @param loader the classloader.
	 * @return
	 * @throws IOException
	 */
	public Object create(Class<?> api, String url,
			ClassLoader loader) throws IOException  {
		if (api == null)
			throw new NullPointerException(
					"api must not be null for HessianProxyFactory.create()");
		InvocationHandler handler = null;
		TransportURL u = new TransportURL(url);
		if(u.getTransport()!=transport){
			throw new IllegalArgumentException("Unacceptable protocol:"+u.getTransport());
		}
		handler = new AsyncMissianProxy(callbackLoacator, u, this);
		return Proxy.newProxyInstance(loader, new Class[] { api,
				HessianRemoteObject.class }, handler);

	}
	
	/**
	 * Create a remote stub for api class.
	 * @param api the interface to create stub for.
	 * @param url the url this stub talks to, like 'tcp://host:port/beanName'
	 * @return
	 * @throws IOException
	 */
	public Object create(Class<?> api, String url) throws IOException  {
		return create(api, url, Thread.currentThread().getContextClassLoader());
	}
	
	/**
	 * Called by all AsyncMissianProxy instances, which is created by this factory,
	 * to get an IoSession.
	 * There is only one keep-alive IoSession instance for a host:port pair. The sessions 
	 * will be removed from the sessionMap when closed, and recreate when required.  
	 * @param host
	 * @param port
	 * @return
	 */
	IoSession getIoSession(String host, int port) {
		final String key = host +":" + port;
		IoSession session = sessionMap.get(key);
		if(session==null) {
			lock.lock();
			try {
				if(sessionMap.get(key)==null) {
					ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
					future.await();
					session = future.getSession();
					session.getCloseFuture().addListener(new IoFutureListener<IoFuture>() {
				        public void operationComplete(IoFuture future) {
				        	sessionMap.remove(key);
				        }
				    });
					sessionMap.put(key, session);
				} else {
					session = sessionMap.get(key);
				}
			} catch (InterruptedException e) {
				;
			} finally {
				lock.unlock();
			}
		}
		return session;
	}
}
