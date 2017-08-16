package com.shiwuliang.channel.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * ChannelPiplineModifyDemo
 *
 * @author ziyuan
 * @since 2017-08-16
 */
public class ChannelPiplineModifyDemo extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        Channel c = ctx.channel();
        ChannelPipeline pipeline = c.pipeline();
        pipeline.addLast("handler1", new FirstHandler());
        //这个时候 pipeline中 头部 SecondHandler  FirstHandler 尾部
        pipeline.addFirst("handler2", new SecondHandler());
        //这个时候 pipeline中 头部 SecondHandler  FirstHandler SecondHandler 尾部
        pipeline.addLast("handler3", new SecondHandler());

        //返回和channelHandler绑定的ChannelHandlerContext
        pipeline.context("handler1");

        pipeline.remove("handler3");
        pipeline.replace("handler2", "handler4", new SecondHandler());
    }
}

class FirstHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

    }
}

class SecondHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

    }
}
