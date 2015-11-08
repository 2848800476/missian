Create server without spring.

# Introduction #

Create a missian server without spring.


# Details #

## Step 1:Implements a BeanLocator ##

BeanLocator has only one implementation in missian: SpringLocator, but it does not work without spring, so we have to implement one, for example:
```
public class ExampleBeanLocator implements BeanLocator{

	@Override
	public Object lookup(String beanName) {
		if(beanName.equals("hello")) {
			return new HelloImpl();
		}
		throw new IllegalArgumentException("No bean was found:"+beanName);
	}

}
```

## Step 2:Build an mina server ##
For example:
```
public class StandaloneServer {
	public static void main(String[] args) throws IOException {
		NioSocketAcceptor acceptor = new NioSocketAcceptor();
		acceptor.setDefaultLocalAddress(new InetSocketAddress(1235));
		acceptor.setHandler(new MissianHandler(new ExampleBeanLocator()));
		acceptor.getSessionConfig().setReuseAddress(true);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 20);
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MissianCodecFactory()));
		acceptor.getFilterChain().addLast("log", new LoggingFilter());
		acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(20));//use 20 thread's pool to do the business.
		acceptor.bind();
	}
}
```
Actually you can build the acceptor as you want, except that you must use MissianCodecFactory to codec and use MissianHandler to handler the messages.