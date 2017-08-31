package com.shiwuliang.udp.logevent;

import java.net.InetSocketAddress;

/**
 * LogEvent
 * 格式是  文件名：消息
 *
 * @author ziyuan
 * @since 2017-08-31
 */
public final class LogEvent {

    public static final byte SEPARATOR = ':';

    private final InetSocketAddress source;

    /**
     * 文件名
     */
    private final String logfile;

    /**
     * 日志消息
     */
    private final String msg;

    private final long received;

    public LogEvent(String logfile, String msg) {
        this(null, logfile, msg, -1);
    }

    public LogEvent(InetSocketAddress source, String logfile, String msg, long received) {
        this.source = source;
        this.logfile = logfile;
        this.msg = msg;
        this.received = received;
    }

    public InetSocketAddress getSource() {
        return source;
    }

    public String getLogfile() {
        return logfile;
    }

    public String getMsg() {
        return msg;
    }

    public long getReceived() {
        return received;
    }
}
