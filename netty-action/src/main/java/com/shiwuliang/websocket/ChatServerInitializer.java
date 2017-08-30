package com.shiwuliang.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * ChatServerInitializer
 *
 * @author ziyuan
 * @since 2017-08-30
 */
public class ChatServerInitializer extends ChannelInitializer<Channel> {

    private final ChannelGroup channels;

    public ChatServerInitializer(ChannelGroup channels) {
        this.channels = channels;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        //将字节解码为HttpRequest、HttpContent和LastHttpContent。并将HttpRequest、HttpContent和LastHttpContent编码为字节
        pipeline.addLast(new HttpServerCodec());

        //写入一个文件的内容
        pipeline.addLast(new ChunkedWriteHandler());

        //将一个HttpMessage和跟随它的多个HttpContent聚合为单个FullHttpRequest或者FullHttpResponse（取决于它是被用来处理请求还是响应）。
        //安装这个之后，ChannelPipeline中的下一个ChannelHandler将只会收到完整的HTTP请求或响应
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));

        //处理FullHttpRequest（那些不发到/ws URI的请求）
        pipeline.addLast(new HttpRequestHandler("/ws"));

        //按照WebSocket规范的要求，处理WebSocket升级握手、PingWebSocketFrame、PongWebSocketFrame和CloseWebSocketFrame
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        //处理TextWebSocketFrame和握手完成事件
        pipeline.addLast(new TextWebSocketFrameHandler(channels));
    }
}
