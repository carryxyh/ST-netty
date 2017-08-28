package com.shiwuliang.encoder.duplex;

import io.netty.channel.CombinedChannelDuplexHandler;

/**
 * CombinedChannelDuplexHandler<ChannelInboundHandler,ChannelOutboundHandler>
 * 这种可以把编解码器都结合到一个Handler中，又可以保证复用性，更加灵活
 *
 * @author ziyuan
 * @since 2017-08-28
 */
public class CombinedByteCharCodec extends CombinedChannelDuplexHandler<ByteToCharDecoder, CharToByteEncoder> {

    public CombinedByteCharCodec() {
        super(new ByteToCharDecoder(), new CharToByteEncoder());
    }
}
