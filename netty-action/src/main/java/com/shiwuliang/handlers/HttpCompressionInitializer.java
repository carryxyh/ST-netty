package com.shiwuliang.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * HttpCompressionInitializer
 * http压缩开启的http头：
 * Accept-Encoding: gzip , deflate
 *
 * @author ziyuan
 * @since 2017-09-04
 */
public class HttpCompressionInitializer extends ChannelInitializer<Channel> {

    private final boolean isClient;

    public HttpCompressionInitializer(boolean isClient) {
        this.isClient = isClient;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();
        if (isClient) {
            //客户端 解压
            channelPipeline.addLast("decompressor", new HttpContentDecompressor());
        } else {
            //如果是服务器，添加server codec
            channelPipeline.addLast("codec", new HttpServerCodec());
            //压缩数据如果客户端支持
            channelPipeline.addLast("compressor", new HttpContentCompressor());
        }
    }
}
