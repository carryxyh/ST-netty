package com.shiwuliang.eventloop.share;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * ShareEventLoop
 * 贡献eventLoop主要应用场景：
 * 比如我们app和consumer-api交互，app给consumer-api发请求，需要consumer-api作为客户端去调用第三方的服务，比如授权
 * <p>
 * 做法：
 * 1.我们可以创建新的BootStrap去connect
 * 2.EventLoop共享
 * <p>
 * 区别：新建BootStrap会创建新的EventLoop，会产生新的线程，而且 我们的consumer-api接受的链接产生的子Channel，和consumer-api作为客户端链接第三方时connect产生的channel交互，必然带来上下文切换
 * <p>
 * 我们要尽可能的复用EventLoop，以减少线程的开销
 *
 * @author ziyuan
 * @since 2017-08-23
 */
public class ShareEventLoop {

    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new SimpleChannelInboundHandler<ByteBuf>() {

                    ChannelFuture connectFuture;

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        Bootstrap bootstrap = new Bootstrap();
                        bootstrap.channel(NioSocketChannel.class).handler(new SimpleChannelInboundHandler<ByteBuf>() {

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                System.out.println("Received data");
                            }
                        });

                        //使用与分配给已被接受的子channel相同的EventLoop
                        bootstrap.group(ctx.channel().eventLoop());
                        connectFuture = bootstrap.connect(new InetSocketAddress("www.baidu.com", 80));
                    }

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                        if (connectFuture.isDone()) {
                            //do some thing...
                        }
                    }
                });

        ChannelFuture future = serverBootstrap.bind(new InetSocketAddress(8080));
        future.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                System.out.println("Server bound");
            } else {
                //bind失败
                f.cause().printStackTrace();
                ;
            }
        });
    }
}
