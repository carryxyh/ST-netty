package com.shiwuliang.udp.logevent;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

/**
 * LogEventDecoder
 *
 * @author ziyuan
 * @since 2017-08-31
 */
public class LogEventDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        ByteBuf data = msg.content();
        int index = data.indexOf(0, data.readableBytes(), LogEvent.SEPARATOR);
        String fileName = data.slice(0, index).toString(CharsetUtil.UTF_8);
        String logMsg = data.slice(index + 1, data.readableBytes()).toString(CharsetUtil.UTF_8);

        LogEvent event = new LogEvent(msg.sender(), fileName, logMsg, System.currentTimeMillis());
        out.add(event);
    }
}
