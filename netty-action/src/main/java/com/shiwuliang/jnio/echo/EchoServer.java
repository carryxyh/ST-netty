package com.shiwuliang.jnio.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class EchoServer {
    public static final int port = 10880;

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel listener = ServerSocketChannel.open();
        // 绑定地址，并监听
        listener.socket().bind(new InetSocketAddress("localhost", port));

        // 设置非阻塞
        listener.configureBlocking(false);
        // 注册ACCPET事件
        listener.register(selector, SelectionKey.OP_ACCEPT);
        NIOServerConnection conn;
        while (true) {
            // 每1秒选择一次
            if (selector.select(1000) == 0) {
                System.out.print(".");
                continue;
            }

            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    listener = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = listener.accept();
                    // 设置地址复用
                    clientChannel.socket().setReuseAddress(true);
                    clientChannel.configureBlocking(false);
                    // 将接受的客户端通道设置可读
                    SelectionKey connKey = clientChannel.register(selector, SelectionKey.OP_READ);
                    // 使用该类处理读写请求
                    conn = new NIOServerConnection(connKey);
                    connKey.attach(conn);
                }

                if (key.isReadable()) {
                    conn = (NIOServerConnection) key.attachment();
                    conn.handleRead();
                }

                if (key.isValid() && key.isWritable()) {
                    conn = (NIOServerConnection) key.attachment();
                    conn.handleWrite();
                }

            }
        }

    }
}