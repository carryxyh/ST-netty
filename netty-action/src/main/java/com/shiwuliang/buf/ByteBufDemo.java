package com.shiwuliang.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * ByteBufDemo
 *
 * @author ziyuan
 * @since 2017-08-09
 */
public class ByteBufDemo {

    public static void main(String[] args) {

        ByteBuf byteBuf = Unpooled.buffer();
        if (byteBuf.hasArray()) { //检查bytebuf是否有一个支撑数组
            byte[] array = byteBuf.array(); //如果上面不检查，这里报错
            int offset = byteBuf.arrayOffset() + byteBuf.readerIndex(); //计算第一个可读字节的偏移量
            int length = byteBuf.readableBytes(); //可读字节数
            //handle
        }
    }
}
