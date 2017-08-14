package com.shiwuliang.echo.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * EchoClientHandler
 *
 * @author ziyuan
 * @since 2017-08-07
 */
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        //从 channel 获取一个ByteBufAllocator
//        Channel c = ctx.channel();
//        ByteBufAllocator allocator = c.alloc();

        //ctx获取一个ByteBufAllocator
        ByteBufAllocator a = ctx.alloc();

        ByteBuf b = a.directBuffer();
        assert b.refCnt() == 1;

        //减少该对象的活动引用，当减少到0时，该对象释放，并且方法返回true
        boolean released = b.release();

        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("A client handler is over...");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println("Client received : " + msg.toString(CharsetUtil.UTF_8));
    }
}
