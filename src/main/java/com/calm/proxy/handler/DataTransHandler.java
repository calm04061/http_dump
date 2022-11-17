package com.calm.proxy.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class DataTransHandler extends ChannelInboundHandlerAdapter {
    private final Channel channel;

    public DataTransHandler(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!channel.isOpen()) {
            ReferenceCountUtil.release(msg);
            return;
        }
        channel.writeAndFlush(msg);
    }


}
