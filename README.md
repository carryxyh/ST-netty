# ST-netty
那些年折腾过的netty？

关于netty的内存管理：
netty在4.1.x版本默认使用的allocator是PooledByteBufAllocator，在分配ByteBuf的时候，引入了新的alloc机制——jemalloc，netty内部自己按照jemalloc的机制，实现了一个java版的（这个有待考证，目前我翻了一下源码，好像是java实现的，没有调用native的部分）

关于Channel.write和ChannelHandlerContext.write：
ChannelHandlerContext的write方法，把值写到channelPipline的后一个channelHandler节点中，而channel.write不同，因为write是个outbound事件，所以DefaultChannelPipeline直接找到tail部分的context，调用其write()方法是从尾部向前传递的。（这个时候，头指的是朝向外部的一端，尾部指朝向本地的一端）
更详细的说明：
http://blog.csdn.net/zxhoo/article/details/17264263

关于netty的ByteBuf：
netty大块来说有两种buf，一种分配在堆内存中，一种分配在堆外内存（就是系统内存）里。各有各的好处。
1.分配在堆外，避免了每次调用I/O操作之前或者之后，将缓冲区的内容复制到一个中间缓冲区，缺点也很明显——堆外收集和分配比较昂贵，使用需要复制到堆内存中。
2.分配在堆内存中的，byteBuf.hasArray()将会返回true，说明在堆内存中有一个`支持数组`。

关于ChannelHandlerContext：
每当有ChannelHandler被添加到ChannelPipeline中，都会创建一个ChannelHandlerContext，二者之间的绑定关系是永远不会改变的，所以可以缓存某个ChannelHandler的ChannelHandlerContext。但是一个channelHandler可以被安装到多个ChannelPipeline中，但是需要加@Sharable注解。

关于netty的异常处理：
如果入站处理中出现了异常，异常会像所有入站事件一样向后传递，如果一直传递到底都没有被处理，那么netty将会warn级别记录，并且尝试释放该异常。
