package com.shiwuliang.channel.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

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


        //EventLoop的一些用法，EventLoop跟我们JUC中的Executor功能非常相似，看源码就知道，其实就是继承自Executor
        ScheduledFuture<?> future = c.eventLoop().schedule(() -> System.out.println("Netty rocks!"), 60, TimeUnit.SECONDS);
        //参数的含义是，需不需要中断这个任务，false 会让任务执行完
        future.cancel(true);
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
