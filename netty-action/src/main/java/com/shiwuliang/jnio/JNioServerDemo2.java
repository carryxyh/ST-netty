package com.shiwuliang.jnio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * JNioServerDemo2
 *
 * @author ziyuan
 * @since 2017-09-24
 */
public class JNioServerDemo2 {

    public static void main(String[] args) throws IOException {
        int port = 8888;
        int bufSize = 1024;

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(port));
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            int n = selector.select();
            if (n == 0) continue;
            Iterator ite = selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next();
                if (key.isAcceptable()) {
                    SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
                    clntChan.configureBlocking(false);
                    //将选择器注册到连接到的客户端信道，
                    //并指定该信道key值的属性为OP_READ，
                    //同时为该信道指定关联的附件
                    clntChan.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufSize));
                }
                if (key.isReadable()) {
//                    handleRead(key);
                }
                if (key.isWritable() && key.isValid()) {
//                    handleWrite(key);
                }
                if (key.isConnectable()) {
                    System.out.println("isConnectable = true");
                }
                ite.remove();
            }
        }
    }
}
//服务端连接过程
//1、创建ServerSocketChannel实例serverSocketChannel，并bind到指定端口。
//2、创建Selector实例selector；
//3、将serverSocketChannel注册到selector，并指定事件OP_ACCEPT。
//4、while循环执行：
//  4.1、调用select方法，该方法会阻塞等待，直到有一个或多个通道准备好了I/O操作或等待超时。
//  4.2、获取选取的键列表；
//  4.3、循环键集中的每个键：
//      4.3.a、获取通道，并从键中获取附件（如果添加了附件）；
//      4.3.b、确定准备就绪的操纵并执行，如果是accept操作，将接收的信道设置为非阻塞模式，并注册到选择器；
//      4.3.c、如果需要，修改键的兴趣操作集；
//      4.3.d、从已选键集中移除键

//在步骤3中，selector只注册了serverSocketChannel的OP_ACCEPT事件
//1.如果有客户端A连接服务，执行select方法时，可以通过serverSocketChannel获取客户端A的socketChannel，并在selector上注册socketChannel的OP_READ事件。
//2.如果客户端A发送数据，会触发read事件，这样下次轮询调用select方法时，就能通过socketChannel读取数据，同时在selector上注册该socketChannel的OP_WRITE事件，实现服务器往客户端写数据。
