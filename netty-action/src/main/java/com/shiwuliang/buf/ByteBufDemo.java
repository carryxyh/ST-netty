package com.shiwuliang.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;

/**
 * ByteBufDemo
 *
 * @author ziyuan
 * @since 2017-08-09
 */
public class ByteBufDemo {

    public static void main(String[] args) {

        //这种属于分配在堆中
        ByteBuf byteBuf = Unpooled.buffer();
        if (byteBuf.hasArray()) { //检查bytebuf是否有一个支撑数组
            byte[] array = byteBuf.array(); //如果上面不检查，这里报错
            int offset = byteBuf.arrayOffset() + byteBuf.readerIndex(); //计算第一个可读字节的偏移量
            int length = byteBuf.readableBytes(); //可读字节数
            //handle...
        }

        //分配在堆外，避免了每次调用I/O操作之前或者之后，将缓冲区的内容复制到一个中间缓冲区
        //缺点：堆外收集和分配比较昂贵，使用需要复制，如下所示
        ByteBuf directBuf = Unpooled.directBuffer();
        if (!directBuf.hasArray()) {
            int length = directBuf.readableBytes();
            byte[] array = new byte[length];
            directBuf.getBytes(directBuf.readerIndex(), array);//将字节复制到array中
            //handle...
        }

        CompositeByteBuf messageBuf = Unpooled.compositeBuffer();
        messageBuf.addComponents(byteBuf, directBuf);

        messageBuf.removeComponent(0);//移除第一个bytebuf

        for (ByteBuf b : messageBuf) {
            //遍历
        }

        //访问数据类似于访问直接缓冲
        int length = messageBuf.readableBytes();
        byte[] arr = new byte[length];
        messageBuf.getBytes(messageBuf.readerIndex(), arr);
        //handle...
    }

    public void slice() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.copiedBuffer("Netty in action rocks !", utf8);
        ByteBuf slice = buf.slice(0, 15);
        System.out.println(slice.toString());
        buf.setByte(0, 'J');

        assert buf.getByte(0) == slice.getByte(0); //这里将会成功，对其中一个做修改，另外一个buf也是可见的
    }
}
