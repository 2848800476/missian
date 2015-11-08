Create a synchronous client stub and make a synchronous call.

# Introduction #

Create a synchronous client stub and make a synchronous call.


# Details #

## Create the SyncMissianProxyFactory ##
Here is an example:
```
	public static void main(String[] args) throws IOException {
		SyncMissianProxyFactory factory = new SyncMissianProxyFactory(TransportProtocol.tcp);
		factory.setReadTimeout(100);
		//set anyother parameters here, like sendBufferSize, tcpNoDelay...
	}
```

You notice that the SyncMissianProxyFactory accept only one contruct argument: TransportProtocol. TransportProtocol.tcp is the only implemented one, and I'm planning to implement TransportProtocol.http.

With this constructor, all the socket will be closed when the response is received. If you want the socket keeping alive, please use another constructor:
```
public SyncMissianProxyFactory(TransportProtocol transport, SocketPool socketPool){}
```
Yes, a SocketPool is required and missian provide a default implementation CommonSocketPool(based on apache commons-pool).

CommonSocketPool has two constructor:
One of the constructor accept one SyncMissianProxyFactory argument, and the pool parameters will be default.
```
public CommonSocketPool(SyncMissianProxyFactory syncMissianProxyFactory){}
```
The other one accept one more argument:GenericKeyedObjectPool.Config. You can set configure as you want.
```
public CommonSocketPool(SyncMissianProxyFactory syncMissianProxyFactory, GenericKeyedObjectPool.Config config){}
```

## Create the stub ##
```
Hello hello = (Hello)factory.create(Hello.class, "tcp://localhost:1235/hello");
```
Please notice the url string:
  * tcp is transport *** localhost is server host name**
  * 1235 is the server port *** hello is the beanName configured in the server side spring context.**

## Make a remote call. ##
```
String returnObj = hello.hello("hy", 27);
```