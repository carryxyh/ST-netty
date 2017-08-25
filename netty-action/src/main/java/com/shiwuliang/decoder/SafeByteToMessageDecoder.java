package com.shiwuliang.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

/**
 * SafeByteToMessageDecoder
 * 我们可以在数据量过大的时候抛出一个TooLongFrameException来告诉pipeline，数据量太大
 *
 * @author ziyuan
 * @since 2017-08-25
 */
public class SafeByteToMessageDecoder extends ByteToMessageDecoder {

    private static final int MAX_FRAME_SIZE = 1024;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() > MAX_FRAME_SIZE) {
            in.skipBytes(in.readableBytes());
            throw new TooLongFrameException("Frame too big");
        }
    }
}
