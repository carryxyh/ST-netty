package com.shiwuliang.jnio.echo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NIOServerConnection {
    private final SelectionKey key;
    private ByteBuffer data;
    private final ByteBuffer dataLengthBuffer;

    public NIOServerConnection(SelectionKey key) {
        this.key = key;
        dataLengthBuffer = ByteBuffer.allocate(4);
    }

    public void handleRead() {
        dataLengthBuffer.clear();
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            long bytesRead = channel.read(dataLengthBuffer);
            assert bytesRead == 4;
            // 标记一下
            dataLengthBuffer.flip().mark();
            int len = dataLengthBuffer.getInt();
            // 在读取的时候，还要重置的
            dataLengthBuffer.reset();
            data = ByteBuffer.allocate(len);
            bytesRead += channel.read(data);
            // 断言全部读取完毕
            assert bytesRead == (4 + len);
            byte[] b = data.array();
            int port = channel.socket().getPort();
            System.out.println("client port : " + port + " ; msg : " + new String(b));
            if (bytesRead == -1) {
                channel.close();
            } else {
                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                channel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void handleWrite() {
        data.flip();
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            channel.write(new ByteBuffer[] { dataLengthBuffer, data });
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!data.hasRemaining())
            key.interestOps(SelectionKey.OP_READ);
        data.compact();
    }

}