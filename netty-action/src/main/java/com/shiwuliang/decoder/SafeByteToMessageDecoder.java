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

    /**
     * 解码器有时候需要在channel关闭之后产生一个消息，所以有了这个方法
     * 但是编码器不会有，因为channel我们再产生一个消息是没有意义的（channel关闭，没法写出去）
     */
    @Override
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("decode last");
        super.decodeLast(ctx, in, out);
    }
}
