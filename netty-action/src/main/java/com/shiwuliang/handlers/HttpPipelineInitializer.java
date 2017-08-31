package com.shiwuliang.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * HttpPipelineInitializer
 *
 * @author ziyuan
 * @since 2017-08-31
 */
public class HttpPipelineInitializer extends ChannelInitializer<Channel> {

    private final boolean isClient;

    public HttpPipelineInitializer(boolean isClient) {
        this.isClient = isClient;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();
        if (isClient) {
            //客户端 添加response decoder处理来自服务器的响应
            channelPipeline.addLast("decoder", new HttpResponseDecoder());
            //客户端 添加request encoder发送请求
            channelPipeline.addLast("encoder", new HttpRequestEncoder());
        } else {
            //服务端 解码请求
            channelPipeline.addLast("decoder", new HttpRequestDecoder());
            //服务端 编码响应
            channelPipeline.addLast("encoder", new HttpResponseEncoder());
        }
    }
}
