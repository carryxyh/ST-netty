package com.shiwuliang.echo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;

/**
 * EchoServer
 *
 * @author ziyuan
 * @since 2017-08-07
 */
public class MultiPortEchoServer {

    public static void main(String[] args) throws InterruptedException {
        final EchoServerHandler serverHandler = new EchoServerHandler();
        EventLoopGroup loopGroup = new NioEventLoopGroup(2);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(loopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    protected void initChannel(SocketChannel ch) throws Exception {
//                        ch.pipeline().addLast(serverHandler).addFirst(serverHandler);
                        ch.pipeline().addLast(serverHandler);
//                        throw new RuntimeException("run time ex...");
                    }
                });
        try {
            ChannelFuture future1 = bootstrap.bind(10111).sync();
            ChannelFuture future2 = bootstrap.bind(10112).sync();

            // 使用回调的方式，这种方式是异步的
            future1.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    System.out.println("close1");
                }
            });

            future2.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    System.out.println("close2");
                }
            });

            // 用下面这种方式绑定多个端口是不好的，因为sync会阻塞住，即future1.channel().closeFuture().sync()阻塞，导致下面那一句没法执行
//            future1.channel().closeFuture().sync();
//            future2.channel().closeFuture().sync();
            System.out.println("start over...");
        } catch (InterruptedException e) {
            loopGroup.shutdownGracefully().sync();
        }
    }
}
