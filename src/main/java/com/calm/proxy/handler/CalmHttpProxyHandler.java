package com.calm.proxy.handler;

import com.calm.proxy.ProxyHandler;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.stream.Collectors;

import static com.calm.proxy.ProxyHandler.HANDLER_NAME_ATTR_KAY;
import static com.calm.proxy.ProxyHandler.UID_ATTR_KAY;

public class CalmHttpProxyHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final ObjectProvider<ProxyHandler> handlerObjectProvider;

    public CalmHttpProxyHandler(ObjectProvider<ProxyHandler> handlerObjectProvider) {
        this.handlerObjectProvider = handlerObjectProvider;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        List<ProxyHandler> collect = handlerObjectProvider.stream().collect(Collectors.toList());
        Channel channel = ctx.channel();
        for (ProxyHandler handler : collect) {
            FullHttpRequest copy = request.retainedDuplicate();
            channel.attr(UID_ATTR_KAY).set(ProxyHandler.UID);
            channel.attr(HANDLER_NAME_ATTR_KAY).set(handler.getClass().getName());
            if (handler.isSupport(copy)) {
                handler.handle(ctx, copy);
            }
//            copy.release();
        }
    }
}
