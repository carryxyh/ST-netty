package com.shiwuliang.echo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * EchoServer
 *
 * @author ziyuan
 * @since 2017-08-07
 */
public class EchoServer {

    public static void main(String[] args) throws InterruptedException {
        final EchoServerHandler serverHandler = new EchoServerHandler();
        EventLoopGroup loopGroup = new NioEventLoopGroup(2);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(loopGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(10111))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(serverHandler);
//                        throw new RuntimeException("run time ex...");
                    }
                });
        try {
            ChannelFuture future = bootstrap.bind().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            loopGroup.shutdownGracefully().sync();
        }
    }
}
