Create an asynchronous client stub and make an asynchronous remote call.

# Introduction #

Create an asynchronous client stub and make an asynchronous remote call.


# Details #

## Step 1:Create a Callback class ##
An asynchronous call requires a callback class which extends from Callback:
```
public class HelloCallback extends Callback{
	public HelloCallback() {
		super(String.class);
	}

	@Override
	public void call(Object obj) {
		System.out.println(obj);
	}
}
```


## Step 2: Use CallbackTarget annotation to annotate the interface method ##
The Hello interface looks like this for an synchronous call:
```
public interface Hello {
	public String hello(String name, int age);
}
```
But for an asynchronous call:
```
public interface Hello {
	@CallbackTarget("helloCallback")
	public String hello(String name, int age);
}
```

## Step3: Implements a BeanLocator for missian to lookup callback beans ##
BeanLocator has only one implementation(SpringLocator), and SpringLocator does not work without spring. So we have to implement one.
```
public class SimpleBeanLocator implements BeanLocator {

	@Override
	public Object lookup(String beanName) {
		if(beanName.equals("helloCallback")) {
			return new HelloCallback();
		}
		throw new IllegalArgumentException("No bean was found:"+beanName);
	}

}
```

## Step 4: Create the AsyncMissianProxyFactory ##
```
AsyncMissianProxyFactory factory = new AsyncMissianProxyFactory(TransportProtocol.tcp, new SimpleBeanLocator());
factory.setHessian2Request(true);
factory.setHessian2Response(true);
//set anyother parameters here.
factory.init();
```
We use this constructor:
```
public AsyncMissianProxyFactory(TransportProtocol transport, BeanLocator callbackLoacator)
```
It will create a thread pool with 4 threads for bussiness logic, and only one thread for I/O codec. If you want to change the thread numers, or pass an existing thread pool to it, please refer to the other constructors.
```
public AsyncMissianProxyFactory(TransportProtocol transport, BeanLocator callbackLoacator, int threadPoolSize){}
public AsyncMissianProxyFactory(TransportProtocol transport, BeanLocator callbackLoacator, int threadPoolSize, int callbackIoProcesses, boolean logBeforeCodec, boolean logAfterCodec) {}
public AsyncMissianProxyFactory(TransportProtocol transport, BeanLocator callbackLoacator, ExecutorService threadPool) {}
public AsyncMissianProxyFactory(TransportProtocol transport, BeanLocator callbackLoacator, ExecutorService threadPool, int callbackIoProcesses, boolean logBeforeCodec, boolean logAfterCodec) {}
```
**Don't forget to call the init() method of the factory.**
## Step 5: Create an asynchronous remote stub ##
```
Hello hello = (Hello)factory.create(Hello.class, "tcp://localhost:1235/hello");
```
## Step 6: Make an asynchronous remote call ##
```
hello.hello("gg", 25);
```
Please notice that it's an asynchronous call so the return value is always null if not 'void'.
When the response(return value) received from the server side, the HelloCallback will be invoked.