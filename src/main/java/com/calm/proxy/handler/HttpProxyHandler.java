package com.calm.proxy.handler;

import com.calm.proxy.ProxyHandler;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.stream.Collectors;

public class HttpProxyHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final ObjectProvider<ProxyHandler> handlerObjectProvider;

    public HttpProxyHandler(ObjectProvider<ProxyHandler> handlerObjectProvider) {
        this.handlerObjectProvider = handlerObjectProvider;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        List<ProxyHandler> collect = handlerObjectProvider.stream().collect(Collectors.toList());
        for (ProxyHandler handler:collect){
            FullHttpRequest copy = request.copy();

            if(handler.isSupport(copy)){
                handler.handle(ctx,copy);
            }
        }
    }
}
