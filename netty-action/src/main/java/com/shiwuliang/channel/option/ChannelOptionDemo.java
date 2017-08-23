package com.shiwuliang.channel.option;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

/**
 * ChannelOptionDemo
 *
 * @author ziyuan
 * @since 2017-08-23
 */
public class ChannelOptionDemo {

    public static void main(String[] args) {
        final AttributeKey<Integer> id = AttributeKey.newInstance("ID");

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup()).channel(NioSocketChannel.class).handler(new SimpleChannelInboundHandler<ByteBuf>() {

            @Override
            public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                Integer idValue = ctx.channel().attr(id).get();
                //do something...
                System.out.println(idValue);
            }

            @Override
            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                System.out.println("Received data...");
            }
        });

        bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        //这里输入一个值
        bootstrap.attr(id, 123456);
        ChannelFuture f = bootstrap.connect(new InetSocketAddress("www.baidu.com", 80));
        f.syncUninterruptibly();
    }
}
