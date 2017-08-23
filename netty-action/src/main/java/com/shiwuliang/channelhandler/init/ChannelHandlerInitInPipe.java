package com.shiwuliang.channelhandler.init;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

import java.net.InetSocketAddress;

/**
 * ChannelHandlerInitInPipe
 * ChannelInitializer接口可以用来在初始化阶段向ChannelPipeline中添加多个ChannelHandler
 *
 * @author ziyuan
 * @since 2017-08-23
 */
public class ChannelHandlerInitInPipe {

    public static void main(String[] args) throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializerImpl());
        ChannelFuture f = serverBootstrap.bind(new InetSocketAddress(8080));
        f.sync();
    }

    static final class ChannelInitializerImpl extends ChannelInitializer<Channel> {

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline c = ch.pipeline();
            c.addLast(new HttpClientCodec());
            c.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
        }
    }
}
