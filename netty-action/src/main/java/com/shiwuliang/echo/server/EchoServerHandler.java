package com.shiwuliang.echo.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

/**
 * EchoServerHandler
 *
 * @author ziyuan
 * @since 2017-08-07
 */
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext ctx;

    //这里，每次一个EchoServerHandler被安装到ChannelPipeline中，都会触发这个方法。
    //如下所示，我们可以把这个ctx缓存到全局变量中，然后在别的地方调用这个ctx
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (this.ctx == null) {
            this.ctx = ctx;
        } else {
            /**
             * false这里输出了false，说明创建了新的ctx，就算是同一个EchoServerHandler，这里把添加EchoServerHandler的代码拿过来看一下
             *
             * final EchoServerHandler serverHandler = new EchoServerHandler();
             * ...
             * ch.pipeline().addLast(serverHandler).addFirst(serverHandler);
             * ...
             *
             * 我们可以看到，我们像管道中添加了同一个EchoServerHandler，但是这里输出false，说明第二次添加也产生了ctx，所以如果需要缓存ctx，要每次添加一个新的EchoServerHandler
             */
            System.out.println(this.ctx == ctx);
        }
        System.out.println(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("-----------" + buf.toString(CharsetUtil.UTF_8));
        //这个时候不会写到套接字里，只会放到缓冲区
//        if (buf.isWritable()) {
//            //这里抛出异常，是直接抛出去了，并不会调用exceptionCaught方法。
//            throw new NullPointerException("buf is null");
//        }
        ctx.write(buf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(new ChannelFutureListener() {
        ctx.writeAndFlush(Unpooled.copiedBuffer("server read complete..", CharsetUtil.UTF_8)).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                System.out.println("channel is closing...");
                future.channel().close();
            }
        });
    }

    //经过验证，这个方法并不是channelRead之类的方法之中出现异常调用的，而是
    // childHandler(new ChannelInitializer<SocketChannel>() {

    //             protected void initChannel(SocketChannel ch) throws Exception {
    //                 ch.pipeline().addLast(serverHandler);
    //             }
    //         });
    //这个方法中，调用initChannel的时候出现异常时候调用的，有待考证。。
    //
    //经过笔者验证，这个方法会在channelRead中产生异常的时候调用,而并不是上面说的initChannel时
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught is doing...");
        //如果不重写这个方法，异常会直接被传递到整个channelPipline的尾端，所以程序应该最少提供一个实现了exceptionCaught的handler
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush("Err is : " + cause.toString()).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
