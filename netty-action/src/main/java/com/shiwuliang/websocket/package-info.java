/**
 * websocket以帧的方式传输数据，每一帧代表消息的一部分，一个完整的消息可能包含很多帧
 * <p>
 * IETF发布的WebSocket RFC，定义了6种帧，Netty体用了POJO类实现
 * BinaryWebSocketFrame 二进制数据
 * TextWebSocketFrame 文本数据
 * CloseWebSocketFrame 一个CLOSE请求，包含一个关闭的状态码和关闭的原因
 * ContinuationWebSocketFrame 包含属于上一个BinaryWebSocketFrame或TextWebSocketFrame的文本数据或者二进制数据
 * PongWebSocketFrame 作为一个对于PingWebSocketFrame的响应被发送
 * PingWebSocketFrame 请求传输一个PingWebSocketFrame
 */

package com.shiwuliang.websocket;