# ST-netty
那些年折腾过的netty？

### 关于netty的内存管理：
netty在4.1.x版本默认使用的allocator是PooledByteBufAllocator，在分配ByteBuf的时候，引入了新的alloc机制——jemalloc，netty内部自己按照jemalloc的机制，实现了一个java版的（这个有待考证，目前我翻了一下源码，好像是java实现的，没有调用native的部分）

### 关于Channel.write和ChannelHandlerContext.write：
ChannelHandlerContext的write方法，把值写到channelPipline的后一个channelHandler节点中，而channel.write不同，因为write是个outbound事件，所以DefaultChannelPipeline直接找到tail部分的context，调用其write()方法是从尾部向前传递的。（这个时候，头指的是朝向外部的一端，尾部指朝向本地的一端）
更详细的说明：
http://blog.csdn.net/zxhoo/article/details/17264263

### 关于netty的ByteBuf：
netty大块来说有两种buf，一种分配在堆内存中，一种分配在堆外内存（就是系统内存）里。各有各的好处。
1.分配在堆外，避免了每次调用I/O操作之前或者之后，将缓冲区的内容复制到一个中间缓冲区，缺点也很明显——堆外收集和分配比较昂贵，使用需要复制到堆内存中。
2.分配在堆内存中的，byteBuf.hasArray()将会返回true，说明在堆内存中有一个`支持数组`。

### 关于ChannelHandlerContext：
每当有ChannelHandler被添加到ChannelPipeline中，都会创建一个ChannelHandlerContext，二者之间的绑定关系是永远不会改变的，所以可以缓存某个ChannelHandler的ChannelHandlerContext。但是一个channelHandler可以被安装到多个ChannelPipeline中，但是需要加@Sharable注解。

### 关于netty的异常处理：
如果入站处理中出现了异常，异常会像所有入站事件一样向后传递，如果一直传递到底都没有被处理，那么netty将会warn级别记录，并且尝试释放该异常。

### 关于netty的EventLoop：
Netty in action这本书中讲的是：一个EventLoop由一个线程来支撑，这个我翻了一下源码，看到的确实是这个样子。后面接着说：如果提交任务的线程就是支撑EventLoop的线程，那么任务将直接被执行；如果不是，那么EventLoop将调度该任务以便稍后执行，并将它放到内部的队列中。
这个队列是个LinkedBlockingQueue，我把DefaultEventLoop的execute方法（继承自SingleThreadEventExecutor）拿出来：

``
@Override
public void execute(Runnable task) {
    if (task == null) {
        throw new NullPointerException("task");
    }
    boolean inEventLoop = inEventLoop();
    if (inEventLoop) {
        addTask(task);
    } else {
        startThread();
        addTask(task);
        if (isShutdown() && removeTask(task)) {
            reject();
        }
    }
    if (!addTaskWakesUp && wakesUpForTask(task)) {
        wakeup(inEventLoop);
    }
}
``

再看看inEventLoop方法：

``
@Override
public boolean inEventLoop(Thread thread) {
    //这里我省略了一个方法，入参的thread是从Thread.currentThread()方法返回的，即当前线程
    return thread == this.thread;
}
``

这么来看我们可以看到，应该是：是支撑EventLoop的线程，把任务放到队列中，不是的直接执行。不知道是作者笔误？
继续debug了一下netty，我发现，startThread()这个方法的调用是在AbstractChannel初始化的时候就执行了的，这个时候整个线程以及跑起来了，所以这个时候只要添加任何一个任务，就会直接拿出来执行，不知道作者的意思是不是这样的。但是其实还有一个问题，如果这个时候taskQueue中不是空的，那么就算调用任务的线程是支撑EventLoop的线程，这个任务也不会马上执行(因为这个任务不管怎么样都是在taskQueue中)，只能保证比其他别的线程调用的任务稍微快一点点执行（其他线程的任务要先调用startThread这个方法）。

### 关于Netty的传输类型和EventLoopGroup：
我们看一下这种代码：
``
EventLoopGroup e = new NioEventLoopGroup();
BootStrap b = new BootStrap();
b.channel(OioSocketChannel.class)
...
``
以上这段代码将会报错，因为我们选用不兼容的传输。BootStrap.handler方法尤其重要，因为它要配置好ChannelPipeline。

### Netty的优雅关闭：
``
EventLoopGroup group = new NioEventLoopGroup();
Bootstrap b = new Bootstrap();
b.group(group)...
Future<?> f = group.shutdownGracefully();
f.syncUninterruptibly()
``

### 关于netty的解码器的一些细节：
对于编码器和解码器来说，一旦消息被编码或者解码，它就会被ReferenceCountUtil.release(message)调用自动释放。如果不想消息被释放，可以调用ReferenceCountUtil.retain(message)增加引用计数。

### 关于netty中的WebSocket：
WebSocketServerHandshaker有好多种实现，比如WebSocketServerHandshaker13，就是13版的WebSocket实现。WebSocketServerProtocolHandler按照WebSocket规范的要求，处理WebSocket升级握手、PingWebSocketFrame、PongWebSocketFrame和CloseWebSocketFrame。
WebSocket升级之前的ChannelPipeline中的状态：（这里只是根据我们的例子中的handler）

HtthRequestDecoder -> HttpResponseEncoder -> HttpObjectAggregator 
-> HttpRequestHandler -> WebSocketServerProtocolHandler
-> TextWebSocketFrameHandler

HttpObjectAggregator:将一个HttpMessage和跟随它的多个HttpContent聚合为单个FullHttpRequest或者FullHttpResponse（取决于它是被用来处理请求还是响应）。安装这个之后，ChannelPipeline中的下一个ChannelHandler将只会收到完整的HTTP请求或响应。

WebSocket协议升级完成之后，WebSocketServerProtocolHandler将会把HttpRequestDecoder替换为WebSocketFrameDecoder，把HTTPResponseEncoder替换为WebSocketFrameEncoder。为了性能最大化，它将移除任何不再被WebSocket连接所需要的ChannelHandler。也包含了HttpObjectAggregator和HttpRequestHandler。Netty根据客户端支持的版本，自动选择WebSocketFrameEncoder和Decoder，下面是升级之后的Pipeline：
WebSocketFrameDecoder13 -> WebSocketFrameEncoder13
-> WebSocketServerProtocolHandler -> TextWebSocketFrameHandler
（假设我们选用13版的WebSocket协议）

### 关于WebSocket的握手：
我们上文说道WebSocketServerProtocolHandler会做很多替换Handler和移除Handler的方法，WebSocketServerProtocolHandler重写了handlerAdded方法，会在管道中添加一个WebSocketServerProtocolHandshakeHandler，这个方法在channelRead的时候会执行握手`handshaker.handshake`，这个handshaker是WebSocketServerHandshaker的实体，我们可以看到这个握手过程中，会进行handler的替换、移除。

### 关于WebSocket的Idle状态：
触发IdleStateEvent的是IdleStateHandler，这个Handler的channelActive、channelRegistried和handlerAdded的时候schedule一个Task，不停地来触发IdleStateEvent。

### 关于Netty的bind过程：
ServerBootStrap为例（简称sbs），new一个sbs的时候，什么也不会做，但是在调用sbs的channel方法的时候，会初始化ChannelFactory，默认用的是ReflectiveChannelFactory。bind的时候会初始化一个channel，这个channel是从ChannelFactory中创建出来的，然后在initAndRegister中调用：
`ChannelFuture regFuture = config().group().register(channel);`这里这个group方法返回的就是group方法传入的EventLoopGroup。这就会在EventLoopGroup中注册一个Channel。我们以NioEventLoopGroup为例，调用了父类MultithreadEventLoopGroup的register方法，这里会先使用EventExecutorChooser来选择一个EventLoop，然后调用EventLoop的Channel注册到一个EventLoop中，这就是我们之前说的，一个EventLoop会绑定很多Channel，但是一个Channel只会绑在一个EventLoop上。

### 关于EPOLL：
epoll：
1. 初始化的时候，向内核申请一块区域，存储被监控的句柄文件(linux中一切皆文件，进程需要操作文件，句柄就是把进程和文件关联起来的东西)。调用epoll_create时，会在这个区域中创建file节点，同时epoll会开辟一块告诉缓存区，以红黑树在缓存中保存这些被监控的句柄。然后还会创建一个list链表，用于存储准备就绪的事件（epoll和poll，就差了个e，这个e就是event）
2. 把socket句柄放到epoll创建的内核区域的file节点红黑树上的时候，会给内核注册一个回调函数，如果句柄发生中断，就把句柄放到list链表中。当一个socket上有数据到了，内核在把网卡上的数据copy到内核中后，就把socket插入到就绪链表里。
3. epoll会观察链表里有没有数据，有就返回，否则就sleep指定时间。一直没有也返回。
总结一下：epoll用红黑树存储被监听的句柄。这个红黑树在高速缓存和文件系统中都有。然后还维护的了一个准备就绪的链表集合，增加被监听的句柄的时候，通过回调让内核把中断的句柄加入到这个集合中。epoll仅仅观察这个链表中有没有数据，有就返回数据，没有就sleep，但是只会sleep指定时间。

### 关于NIO socket：
服务端的demo请看JNioServerDemo2.java。SocketChannel、ServerSocketChannel和Selector的实例初始化都通过SelectorProvider类实现，其中Selector是整个NIO Socket的核心实现。以KQueueSelectorImpl为例，初始化的时候初始化一个kqueueWrapper和fdMap，这个map的键就是被监听的句柄，值是MapEntry，这个MapEntry就是一个SelectionKeyImpl的包装类，同时记住个这个KEY被修改的次数。doSelect是通过kqueueWrapper的poll方法。
implRegister方法则会把SelectionKeyImpl和其绑定的channel的句柄放到这个map中。

### 关于NioEventLoop的启动流程：
我们在bind中说过，最终实际是把一个channel注册到了一个EventLoop上，我们看一下AbstractChannel的`register(EventLoop eventLoop, final ChannelPromise promise)`，这个方法中，有一段代码：
<br/>
if (eventLoop.inEventLoop()) {
    register0(promise);
} else {
    try {
        eventLoop.execute(new Runnable() {....}
第一次执行inEventLoop肯定会返回false，因为这个EventLoop被创建出来，这个thread还没有绑定。这个thread被绑定是在SingleThreadEventExecutor.doStartThread这个方法中。
我们看eventLoop.execute这个方法的实现，第一次执行inEventLoop一定返回false，所以会调用startThread，这个方法中会调用自己的run方法，run最终会执行NioEventloop的run，在循环中不停地select并且处理意见select到的键，以及处理taskQueue中的任务。


### 关于Netty的LengthFrameDecoder
Netty提供`LengthFrameDecoder`来对协议进行解码，目的是解决TCP的拆包粘包问题，我们可以使用这个Decoder对自定义的协议进行解码，而不用关心我们拿到的是否是整包数据。
构造函数的含义如下：

```java
/**
 * @param byteOrder           表示字节流表示的数据是大端还是小端，用于长度域的读取；
 * @param maxFrameLength      表示的是包的最大长度，超出包的最大长度netty将会做一些特殊处理
 * @param lengthFieldOffset   指的是长度域的偏移量，表示跳过指定长度个字节之后的才是长度域
 * @param lengthFieldLength   记录该帧数据长度的字段本身的长度
 * @param lengthAdjustment    该字段加长度字段等于数据帧的长度，包体长度调整的大小，长度域的数值表示的长度加上这个修正值表示的就是带header的包 如果为负数，则从Length结束往前推lengthAdjustment个字段一起解析，否则向后解析
 * @param initialBytesToStrip 从数据帧中跳过的字节数，表示获取完一个完整的数据包之后，忽略前面的指定的位数个字节，应用解码器拿到的就是不带长度域的数据包
 * @param failFast            如果为true，则表示读取到长度域，TA的值的超过maxFrameLength，就抛出一个 TooLongFrameException，而为false表示只有当真正读取完长度域的值表示的字节之后，才会抛出 TooLongFrameException。
 *                            默认情况下设置为true，建议不要修改，否则可能会造成内存溢出
 */
public LengthFrameDecoder(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
    super(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
}
```

### 关于EPOLL的LT和ET模式
`LT模式`：当epoll_wait检测到描述符事件发生并将此事件通知应用程序，应用程序可以不立即处理该事件。下次调用epoll_wait时，会再次响应应用程序并通知此事件。

`ET模式`：当epoll_wait检测到描述符事件发生并将此事件通知应用程序，应用程序必须立即处理该事件。如果不处理，下次调用epoll_wait时，不会再次响应应用程序并通知此事件。

**LT模式**
LT(level triggered)是缺省的工作方式，并且同时支持block和no-block socket。在这种做法中，内核告诉你一个文件描述符是否就绪了，然后你可以对这个就绪的fd进行IO操作。如果你不作任何操作，内核还是会继续通知你的。

**ET模式**
ET(edge-triggered)是高速工作方式，只支持no-block socket。在这种模式下，当描述符从未就绪变为就绪时，内核通过epoll告诉你。然后它会假设你知道文件描述符已经就绪，并且不会再为那个文件描述符发送更多的就绪通知，直到你做了某些操作导致那个文件描述符不再为就绪状态了(比如，你在发送，接收或者接收请求，或者发送接收的数据少于一定量时导致了一个EWOULDBLOCK 错误）。但是请注意，如果一直不对这个fd作IO操作(从而导致它再次变成未就绪)，内核不会发送更多的通知(only once)。
ET模式在很大程度上减少了epoll事件被重复触发的次数，因此效率要比LT模式高。epoll工作在ET模式的时候，必须使用非阻塞套接口，以避免由于一个文件句柄的阻塞读/阻塞写操作把处理多个文件描述符的任务饿死。

### 关于同步IO、异步IO、IO多路复用和信号驱动IO