Create an asynchronous client stub and make an asynchronous remote call.
It's updated from v0.30.

# Introduction #

Create an asynchronous client stub and make an asynchronous remote call.


# Details #

## Step 1:Create a Callback class ##
An asynchronous call requires a callback class which has a method accepting thre result object:
```
public class HelloCallback {
	public void hello(String returnValue) {
		System.out.println(returnValue);
	}
}
```
'Hello' interface has a "String hello(String, int)" method, so this callback class must have a "hello(String)" method.

## Step 2: Use CallbackTarget annotation to annotate the interface method ##
The Hello interface looks like this for an synchronous call:
```
public interface Hello {
	public String hello(String name, int age);
}
```
But for an asynchronous call:
```
@CallbackTarget("helloCallback")
public interface Hello {
	public String hello(String name, int age);
}
```

## Step 3: Configure the AsyncMissianProxyFactory in spring ##
```
	<bean id="asyncMissianProxyFactory" class="com.missian.client.async.AsyncMissianProxyFactory" init-method="init" destroy-method="destroy">
		<constructor-arg >
			<bean class="com.missian.common.beanlocate.SpringLocator"/>
		</constructor-arg>
	</bean>
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
## Step 4: Configure the callback bean in spring ##
```
	<bean id="helloCallback" class="com.missian.example.bean.HelloCallback">
	</bean>
```
## Step 5: Get the asyncMissianProxyFactory bean ##
> ### Get from the ApplicationContext ###
```
AsyncMissianProxyFactory asyncMissianProxyFactory = (AsyncMissianProxyFactory)context.getBean("asyncMissianProxyFactory");
```
> ### Inject by spring ###
```
class ...{
     private AsyncMissianProxyFactory asyncMissianProxyFactory;
     public setAsyncMissianProxyFactory (AsyncMissianProxyFactory asyncMissianProxyFactory) {
            this.asyncMissianProxyFactory = asyncMissianProxyFactory;
     ..... 
}
```
```
<bean id="xxx" class="xxx">
      <property name="asyncMissianProxyFactory" ref="asyncMissianProxyFactory"/>
</bean>
```
## Step 6: Create an asynchronous remote stub ##
```
Hello hello = (Hello)factory.create(Hello.class, "tcp://localhost:1235/hello");
```
## Step 7: Make an asynchronous remote call ##
```
hello.hello("gg", 25);
```
Please notice that it's an asynchronous call so the return value is always null if not 'void'.
When the response(return value) received from the server side, the HelloCallback will be invoked.