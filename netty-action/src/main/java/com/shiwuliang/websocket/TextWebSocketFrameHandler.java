package com.shiwuliang.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * TextWebSocketFrameHandler
 *
 * @author ziyuan
 * @since 2017-08-30
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final ChannelGroup channels;

    public TextWebSocketFrameHandler(ChannelGroup channels) {
        this.channels = channels;
    }

    /**
     * 重写处理自定义事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            //如果该事件表示握手成功，则从ChannelPipeline中移除HttpRequestHandler，因为不会接收到任何HTTP消息了
            ctx.pipeline().remove(HttpRequestHandler.class);
            Channel c = ctx.channel();
            channels.writeAndFlush(new TextWebSocketFrame("Client " + c + " joined"));
            //新的websocket channel添加到channelGroup中，以便它可以接收到所有的消息
            channels.add(c);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        channels.writeAndFlush(msg.retain());
    }
}
