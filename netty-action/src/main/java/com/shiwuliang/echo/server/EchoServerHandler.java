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

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println(buf.toString(CharsetUtil.UTF_8));
        //这个时候不会写到套接字里，只会放到
        // if (buf.isWritable()) {
        //     这里抛出异常，是直接抛出去了，并不会调用exceptionCaught方法。
        //     throw new NullPointerException("buf is null");
        // }
        ctx.write(buf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(new ChannelFutureListener() {
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
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //如果不重写这个方法，异常会直接被传递到整个channelPipline的尾端，所以程序应该最少提供一个实现了exceptionCaught的handler
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush("Err is : " + cause.toString()).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
