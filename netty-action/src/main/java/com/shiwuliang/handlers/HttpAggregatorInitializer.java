package com.shiwuliang.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * HttpAggregatorInitializer
 * 这个聚合器，能够让我们不再关心碎片的http请求，能保证下一个httpHandler处理完整的request(FullRequest)或者response(FullResponse)
 * 由于需要缓冲，有一定的开销，好处是不用关注碎片请求了
 *
 * @author ziyuan
 * @since 2017-08-31
 */
public class HttpAggregatorInitializer extends ChannelInitializer<Channel> {

    private final boolean isClient;

    public HttpAggregatorInitializer(boolean isClient) {
        this.isClient = isClient;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (isClient) {
            pipeline.addLast("codec", new HttpClientCodec());
        } else {
            pipeline.addLast("codec", new HttpServerCodec());
        }
        //将最大的消息为512kb的HttpObjectAggregator加到ChannelPipeline中
        pipeline.addLast("aggregator", new HttpObjectAggregator(512 * 1024));
    }
}
