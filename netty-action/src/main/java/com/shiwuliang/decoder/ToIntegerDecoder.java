package com.shiwuliang.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * ToIntegerDecoder
 *
 * @author ziyuan
 * @since 2017-08-23
 */
public class ToIntegerDecoder extends ByteToMessageDecoder {

    /**
     * 假设in中包含四个字节的值，我们把他读入到 out 中，然后传递给下一个ChannelInboundHandler
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() >= 4) {
            out.add(in.readInt());
        }
    }
}
