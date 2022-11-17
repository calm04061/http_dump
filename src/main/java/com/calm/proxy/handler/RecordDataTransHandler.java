package com.calm.proxy.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;

public class RecordDataTransHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private final Channel channel;
    private final String uri;
    private final HttpMethod method;

    public RecordDataTransHandler(Channel channel, String uri, HttpMethod method) {
        this.channel = channel;
        this.uri = uri;
        this.method = method;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        if (!channel.isOpen()) {
            ReferenceCountUtil.release(msg);
            return;
        }
        String s = msg.content().toString(StandardCharsets.UTF_8);
        System.out.println(s);
        msg.retain();
        channel.writeAndFlush(msg);
    }


}
