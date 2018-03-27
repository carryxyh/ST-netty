package com.shiwuliang.decoder;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

/**
 * LengthFrameDecoder
 * Netty提供了LengthFieldBasedFrameDecoder，自动屏蔽TCP底层的拆包和粘 包问题
 * http://blog.163.com/linfenliang@126/blog/static/127857195201210821145721/
 * 看一下LengthFieldBasedFrameDecoder的注释含义即可
 *
 * @author xiuyuhang [xiuyuhang]
 * @since 2018-03-27
 */
public class LengthFrameDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * @param byteOrder           表示字节流表示的数据是大端还是小端，用于长度域的读取；
     * @param maxFrameLength      表示的是包的最大长度，超出包的最大长度netty将会做一些特殊处理
     * @param lengthFieldOffset   指的是长度域的偏移量，表示跳过指定长度个字节之后的才是长度域
     * @param lengthFieldLength   记录该帧数据长度的字段本身的长度
     * @param lengthAdjustment    该字段加长度字段等于数据帧的长度，包体长度调整的大小，长度域的数值表示的长度加上这个修正值表示的就是带header的包 如果为负数，则从Length结束往前推lengthAdjustment个字段一起解析，否则向后解析
     * @param initialBytesToStrip 从数据帧中跳过的字节数，表示获取完一个完整的数据包之后，忽略前面的指定的位数个字节，应用解码器拿到的就是不带长度域的数据包
     * @param failFast            如果为true，则表示读取到长度域，TA的值的超过maxFrameLength，就抛出一个 TooLongFrameException，而为false表示只有当真正读取完长度域的值表示的字节之后，才会抛出 TooLongFrameException。
     *                            默认情况下设置为true，建议不要修改，否则可能会造成内存溢出
     */
    public LengthFrameDecoder(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }
}
