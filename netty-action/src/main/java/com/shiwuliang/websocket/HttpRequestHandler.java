package com.shiwuliang.websocket;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * HttpRequestHandler 扩展SimpleChannelInboundHandler来处理FullHttpRequest请求
 *
 * @author ziyuan
 * @since 2017-08-28
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String wsUri;

    private static final File INDEX;

    static {
        URL location = HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            String path = location.toURI() + "index.html";
            path = !path.contains("file:") ? path : path.substring(5);
            INDEX = new File(path);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to locate index.html", e);
        }
    }

    public HttpRequestHandler(String wsUri) {
        this.wsUri = wsUri;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (wsUri.equalsIgnoreCase(request.uri())) {
            //如果请求了WebSocket协议升级，则增加引用计数，并将它传递给下一个ChannelInboundHandler
            //调用channelRead之后会release这个request
            ctx.fireChannelRead(request.retain());
        } else {
            if (HttpUtil.is100ContinueExpected(request)) {
                //处理100 continue 请求以符合HTTP 1.1规范
                //如果客户端发送了 HTTP 1.1的HTTP头信息Expect:100-continue，那么会发送一个100 Continue的响应
                send100Continue(ctx);
            }

            RandomAccessFile file = new RandomAccessFile(INDEX, "r"); // 读取index.html
            HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }

            //http response写到客户端，注意这不是一个FullHttpResponse，只是请求的第一部分，所以没有flush
            ctx.write(response);

            //如故不需要加密和压缩，那么可以通过将index.html的内容存储到DefaultFileRegion中来达到最佳效率，这会利用零拷贝特性来进行内容的传输
            //否则使用ChunkedNioFile。
            if (ctx.pipeline().get(SslHandler.class) == null) {
                ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
            } else {

                ctx.write(new ChunkedNioFile(file.getChannel()));
            }

            //将lastHttpContent冲刷到远端，标记响应的结束
            //如果没有请求keep-alive，那么添加一个ChannelFutureListener到最后一次写出动作的ChannelFuture，并关闭连接
            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!keepAlive) {
                //如果没有请求keep-alive，则在写操作完成之后关闭
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }
}
