0.31发布了，增加了一个Future风格的异步方式。

更新历史

0.31 主要更新：
1、原异步方式增加对重载方法的支持
2、支持从方法中在最后一个参数传入Callback
3、添加一个Future风格的异步方式。

0.30 主要更新：
1. 异步回调方式更新：以前是每个方法需要实现一个回调类，回调类要求继承Callback。现在的实现方式是每个interface一个回调类，每个方法在这个回调类里面对应一个回调方法。无侵入性。

0.20主要更新点：
提供了HTTP兼容性，Hessian客户端可以调用Missian服务，Missian同步客户端也可以调用Hessian服务了。

0.10发布：
实现主要的技术架构和目标。


首先，最新代码请移步到svn下载；
wiki下面有一些简单的教程，最好的方式是直接看example里面的例子并逐一运行看看；
http://missian.javaeye.com/ 则有一些比较详尽的教程，正在逐渐充实中。

大家都知道，Hessian是一个了不起的RPC框架。但是，它的调用是同步的，并且只能基于HTTP传输。

我创建missian（mina+hessian的意思）的目的有二：<br>
1、实现异步的RPC调用。同步远程操作带来的损耗有时候是无法忍受的。异步操作要复杂一些，但是能够提高系统的并发能力和响应时间。<br>
2、让hessian可以在tcp上传输。HTTP是构建在tcp之上的应用层协议，本身是很复杂的，对HTTP编码解码的过程也无疑是一个性能损耗。如果把HTTP这一层去掉，能够一定程度的提供性能。<br>
<br>
有多种办法可以对hessian进行扩展以支持tcp传输，但是很难让它异步，因此我决定大刀阔斧的对其进行改造，以达到我的目的。<br>
<br>
Missian的服务器端是基于mina的；同步客户端之基于传统的阻塞式Socket实现的，支持连接池；异步的客户端基于mina NioSocketConnector。<br>
<br>
这里也推荐一下mina这个了不起的nio框架。我从06年底就开始使用。最近的一个项目中，写的基于mina的http服务，在全部击中缓存的情况下（仍有一定的逻辑，诸如几个小列表进行取并集、交集，返回数据包在1-2K之间），测试达到了23000个TPS，此时CPU仅达到40%左右。我推荐大家使用mina来开发网络通信方面的东西，包括服务器。<br>
<br>
Missian没有绑定spring，但是我强烈推荐你使用spring，这样missian可以直接去spring里面找到对应的bean，否则还需要你自己实现一个BeanLocator接口。<br>
<br>
同时我提供了几个例子：<br>
1、构建服务器端（基于spring）<br>
2、构建服务器端（无spring）<br>
3、同步客户端(无spring，使用spring来创建也很简单)<br>
4、异步客户端（基于spring）<br>
5、异步客户端（无spring）<br>
<br>
Missian同时也兼容基于HTTP的异步/同步调用，这时协议和hessian是完全一致的，因此missian http客户端可以调用hessian servlet（仅同步调用，因为异步需要服务器端回传方法名称，hessian是没有这个的），hessian也可以调用missian的服务。Missian http客户端调用missian服务则即可以是同步的也可以是异步的。<br>
<br>
English introduction：<br>
<br>
0.30 announced, now I achieved my target, so the missian API will keep stable until 1.0 announced.<br>
<br>
Update history:<br>
0.30 updates:<br>
A new non-invasive async-callback implementation<br>
<br>
0.20 updates<br>
Provide http-compatibility and hessian-compatibility<br>
<br>
0.10 implements main targets.<br>
<br>
As you know, hessian is a great RPC libary over http. But it's synchronous and only supports http.<br>
<br>
I create missian(which means mina + hessian) for two intentions:<br>
1、asynchronous RPC calls.<br>
2、support hession over tcp.<br>
<br>
The server is build on mina, which is a great nio framework. The synchronous client is based on blocking old socket, and asynchronous client is based on mina connector.<br>
<br>
Spring is optional, but recommend.<br>
<br>
There are some examples in the example source fold:<br>
1、server with spring<br>
2、server without spring<br>
3、synchronous client(without spring)<br>
4、asynchronous client with spring<br>
5、asynchronous client without spring<br>
<br>
Synchronous/asynchronous RPC call over http will be supported soon. When we finish this, hessian clients can call missian http server. On the other hand, missian http clients can call hessian servlets synchronously/asynchronously.