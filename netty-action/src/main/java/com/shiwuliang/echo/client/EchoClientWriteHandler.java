package com.shiwuliang.echo.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 *
 * EchoClientWriteHandler
 *
 * @author ziyuan
 * @since 2017-08-16
 */
public class EchoClientWriteHandler extends ChannelOutboundHandlerAdapter {

    //测试过后我发现，这里也会调用，看了一下文档，这个方法的注释是：Intercepts {@link ChannelHandlerContext#read()}.
    //说白了，它拦截了这个OutBoundHandler的ctx的read方法
    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {

        //如果把这句注释掉，我们会发现，没法从server接收发过来的消息了，说白了，入站事件也走过了ChannelOutboundHandlerAdapter，很不理解
        super.read(ctx);

        System.out.println("write handler's read method is done...");
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        System.out.println("write method is done");
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
        System.out.println("flush method is done");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}
