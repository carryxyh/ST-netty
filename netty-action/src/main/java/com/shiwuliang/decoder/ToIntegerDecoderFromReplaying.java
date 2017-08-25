package com.shiwuliang.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * ToIntegerDecoderFromReplaying
 * ReplayingDecoder稍慢于ByteToMessageDecoder
 *
 * @author ziyuan
 * @since 2017-08-25
 */
public class ToIntegerDecoderFromReplaying extends ReplayingDecoder<Void> {

    /**
     * 从in中读取一个int，如果没有足够字节，抛出一个error（实际上是一个Signal）
     * 当有更多的数据可供读取时，decode方法会再次被调用
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        out.add(in.readInt());
    }
}
