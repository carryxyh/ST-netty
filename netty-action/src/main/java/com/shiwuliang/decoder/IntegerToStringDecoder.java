package com.shiwuliang.decoder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * IntegerToStringDecoder
 *
 * @author ziyuan
 * @since 2017-08-25
 */
public class IntegerToStringDecoder extends MessageToMessageDecoder<Integer> {

    /**
     * Integer to String
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, Integer msg, List<Object> out) throws Exception {
        out.add(String.valueOf(msg));
    }
}
