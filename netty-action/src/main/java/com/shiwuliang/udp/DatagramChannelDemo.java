package com.shiwuliang.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.oio.OioDatagramChannel;

import java.net.InetSocketAddress;

/**
 * DatagramChannelDemo
 * netty提供基于UDP的实现，唯一的区别就是要使用BootStrap的bind
 *
 * @author ziyuan
 * @since 2017-08-23
 */
public class DatagramChannelDemo {

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new OioEventLoopGroup())
                .channel(OioDatagramChannel.class)
                .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                        //do something...
                    }
                });

        //注意这里用了bind，因为该协议是无连接的
        ChannelFuture f = bootstrap.bind(new InetSocketAddress(0));
        f.addListener((ChannelFutureListener) future -> {
            //do something...
        });
    }
}
