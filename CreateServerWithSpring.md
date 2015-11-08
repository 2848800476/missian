Create server with spring.

# Introduction #

Create missian server with spring


# Details #

## Step 1:Create an spring context configuration file ##
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">
</beans>
```
## Step 2:Create a custom editors to convert a 'host:port' string to SocketAddress ##
```
	<bean class="org.springframework.beans.factory.config.CustomEditorConfigurer">
		<property name="customEditors">
			<map>
				<entry key="java.net.SocketAddress">
					<bean class="org.apache.mina.integration.beans.InetSocketAddressEditor" />
				</entry>
			</map>
		</property>
	</bean>
```
## Step 3:Build filters especially the Codec filter ##
```
	<bean id="executorFilter" class="org.apache.mina.filter.executor.ExecutorFilter" />
	<bean id="codecFilter" class="org.apache.mina.filter.codec.ProtocolCodecFilter">
		<constructor-arg>
			<bean class="com.missian.server.codec.MissianCodecFactory" />
		</constructor-arg>
	</bean>
	<bean id="loggingFilter" class="org.apache.mina.filter.logging.LoggingFilter">
		<property name="messageReceivedLogLevel" value="DEBUG"/>
		<property name="messageSentLogLevel" value="DEBUG"/>
		<property name="sessionCreatedLogLevel" value="DEBUG"/>
		<property name="sessionClosedLogLevel" value="DEBUG"/>
		<property name="sessionIdleLogLevel" value="DEBUG"/>
		<property name="sessionOpenLogLevel" value="DEBUG"/>
	</bean>
```
## Step 4:Create the filter chain ##
```
	<bean id="filterChainBuilder"
		class="org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder">
		<property name="filters">
			<map>
				<entry key="codecFilter" value-ref="codecFilter" />
				<entry key="executor" value-ref="executorFilter" />
				<entry key="loggingFilter" value-ref="loggingFilter" />
			</map>
		</property>
	</bean>
```
## Step 5:Create the message handler ##
```
	<bean id="minaHandler" class="com.missian.server.handler.MissianHandler">
		<constructor-arg>
			<bean class="com.missian.common.beanlocate.SpringLocator" />
		</constructor-arg>
	</bean>
```
Please node that we create an SpringLocator instance and inject it to the MissianHandler, so that the handler can lookup beans in the spring context.

SpringLocator is provided by missian and its source code as below:
```
public class SpringLocator implements BeanLocator, ApplicationContextAware {
	private ApplicationContext applicationContext;
	@Override
	public Object lookup(String beanName) {
		return applicationContext.getBean(beanName);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
```

## Step 6: Mina NioSocketAcceptor ##
```
	<bean id="minaAcceptor" class="org.apache.mina.transport.socket.nio.NioSocketAcceptor"
		init-method="bind" destroy-method="unbind">
		<property name="defaultLocalAddress" value=":1235" /><!---->
		<property name="handler" ref="minaHandler" />
		<property name="reuseAddress" value="true" />
		<property name="filterChainBuilder" ref="filterChainBuilder" />
	</bean>
```
## Step 7:Create business beans ##
Create an interface， for example：
```
public interface Hello {
	public String hello(String name, int age);
}
```

Implements the business interface
```
public class HelloImpl implements Hello {

	@Override
	public String hello(String name, int age) {
		return "hi, "+name+", "+age;
	}

}
```

And now configure this business implementation in spring:
```
<bean id="hello" class="com.missian.example.bean.HelloImpl"></bean>
```
## Step 8:Startup the server ##
```
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("com/missian/example/server/withspring/applicationContext-*.xml");
	}
```

Now missian clients can call 'hello' bean.