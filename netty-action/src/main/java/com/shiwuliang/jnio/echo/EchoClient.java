package com.shiwuliang.jnio.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

public class EchoClient {
    public static final String[] messages = new String[]{"nio", "netty", "love", "java", "learn"};
    public static final Random rand = new Random();
    public static final String BLANK = " ";

    public static String getMsg() {
        StringBuilder msg = new StringBuilder();
        int len = messages.length;
        int words = rand.nextInt(len) + 1;
        for (int i = 0; i < words; i++) {
            msg.append(messages[rand.nextInt(len)] + BLANK);
        }
        return msg.toString() + getDate();
    }

    public static String getDate() {
        Date date = new Date();
        return date.toString();
    }

    public static void main(String[] args) {
        SocketChannel clientChannel = null;
        try {
            Selector selector = Selector.open();
            clientChannel = SocketChannel.open();
            clientChannel.connect(new InetSocketAddress("localhost", EchoServer.port));
            clientChannel.configureBlocking(false);
            SelectionKey key = clientChannel.register(selector, SelectionKey.OP_WRITE);
            NIOClientConnection conn = new NIOClientConnection(key);
            key.attach(conn);
            while (true) {
                Thread.sleep(2000);
                selector.select();
                Iterator<SelectionKey> iters = selector.selectedKeys().iterator();
                while (iters.hasNext()) {
                    key = iters.next();
                    iters.remove();
                    if (key.isWritable()) {
                        conn = (NIOClientConnection) key.attachment();
                        conn.doWrite(getMsg());
                    }
                    if (key.isReadable()) {
                        conn = (NIOClientConnection) key.attachment();
                        conn.doRead();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                clientChannel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}