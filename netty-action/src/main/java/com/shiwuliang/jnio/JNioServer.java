package com.shiwuliang.jnio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * JNioServer
 *
 * @author ziyuan
 * @since 2017-08-08
 */
public class JNioServer {

    /**
     * 在不适用netty的情况下，使用java原生nio
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        ServerSocket serverSocket = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(10111);
        serverSocket.bind(address); //将服务器绑定到特定端口

        Selector selector = Selector.open(); //打开selector来处理channel
        serverChannel.register(selector, SelectionKey.OP_ACCEPT); //将serverChannel注册到Selector以接受链接

        final ByteBuffer msg = ByteBuffer.wrap("Hi".getBytes());

        for (; ; ) {
            selector.select(); //等待需要处理的新事件；阻塞将一直持续到下一个传入事件

            Set<SelectionKey> readyKeys = selector.selectedKeys(); //获取所有链接事件的selectionKey实例
            Iterator<SelectionKey> iter = readyKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey k = iter.next();
                iter.remove();

                try {
                    if (k.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) k.channel();
                        SocketChannel client = server.accept();

                        //接受客户端并将他注册到选择器
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, msg);
                    }
                    if (k.isWritable()) { //检查套接字是否已经准备好写数据
                        SocketChannel client = (SocketChannel) k.channel();
                        ByteBuffer buffer = (ByteBuffer) k.attachment();
                        while (buffer.hasRemaining()) {
                            if (client.write(buffer) == 0) {
                                break;
                            }
                        }
                        client.close();
                    }
                } catch (Exception e) {
                    k.channel();
                    k.channel().close();
                }
            }
        }
    }
}
