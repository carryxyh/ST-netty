# ST-netty
那些年折腾过的netty？

关于netty的内存管理：
netty在4.1.x版本默认使用的allocator是PooledByteBufAllocator，在分配ByteBuf的时候，引入了新的alloc机制——jemalloc，netty内部自己按照jemalloc的机制，实现了一个java版的（这个有待考证，目前我翻了一下源码，好像是java实现的，没有调用native的部分）

关于Channel.write和ChannelHandlerContext.write：
ChannelHandlerContext的write方法，把值写到channelPipline的后一个channelHandler节点中，而channel.write不同，因为write是个outbound事件，所以DefaultChannelPipeline直接找到tail部分的context，调用其write()方法是从尾部向前传递的。（这个时候，头指的是朝向外部的一端，尾部指朝向本地的一端）
更详细的说明：
http://blog.csdn.net/zxhoo/article/details/17264263
