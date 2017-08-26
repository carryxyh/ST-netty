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
f.syncUninterruptibly();
``

### 关于netty的解码器的一些细节：
对于编码器和解码器来说，一旦消息被编码或者解码，它就会被ReferenceCountUtil.release(message)调用自动释放。如果不想消息被释放，可以调用ReferenceCountUtil.retain(message)增加引用计数。








