package com.calm.proxy.handler;

import com.calm.proxy.recode.HandlerRecode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.ReferenceCountUtil;
import org.springframework.beans.factory.ObjectProvider;

public class RecordDataTransHandler extends ResponseDataHandler {
    private final Channel clientChannel;

    public RecordDataTransHandler(ObjectProvider<HandlerRecode> handlerRecodes, Channel clientChannel) {
        super(handlerRecodes);
        this.clientChannel = clientChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        if (!clientChannel.isOpen()) {
            ReferenceCountUtil.release(msg);
            return;
        }
        super.channelRead0(ctx, msg);
        msg.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        msg.retain();
        clientChannel.writeAndFlush(msg);
    }


}
