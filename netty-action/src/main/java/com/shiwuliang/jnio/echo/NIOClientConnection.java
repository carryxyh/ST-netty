package com.shiwuliang.jnio.echo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NIOClientConnection {
    private final SelectionKey key;
    private ByteBuffer data;
    private final ByteBuffer dataLengthBuffer;

    public NIOClientConnection(SelectionKey key) {
        this.key = key;
        dataLengthBuffer = ByteBuffer.allocate(4);
    }

    public void doWrite(String msg) {
        dataLengthBuffer.clear();
        byte[] b = msg.getBytes();
        int len = b.length;
        // put操作后需要flip，wrap操作不用flip
        dataLengthBuffer.putInt(len);
        dataLengthBuffer.flip();
        data = ByteBuffer.wrap(b);
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            System.out.println("send => " + msg);
            long bs = channel.write(new ByteBuffer[]{dataLengthBuffer, data});
            if (bs == 0)
                channel.close();
            else
                key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doRead() {
        dataLengthBuffer.clear();
        data.clear();
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            long bs = channel.read(new ByteBuffer[]{dataLengthBuffer, data});
            if (bs == 0 || bs == -1) {
                return;
            }
            dataLengthBuffer.flip();
            int len = dataLengthBuffer.getInt();
            byte[] buf = data.array();
            assert len + 4 == bs;
            System.out.println("receive => " + new String(buf));
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                channel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}