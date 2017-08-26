package com.shiwuliang.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * ShortToByteEncoder
 * netty提供了这样MessageToByteEncoder一个编码器，我们可以基于这个encoder实现自己的encoder
 *
 * @author ziyuan
 * @since 2017-08-26
 */
public class ShortToByteEncoder extends MessageToByteEncoder<Short> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Short msg, ByteBuf out) throws Exception {
        out.writeShort(msg);
    }
}
