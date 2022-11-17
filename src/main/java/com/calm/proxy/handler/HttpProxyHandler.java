package com.calm.proxy.handler;

import com.calm.proxy.ProxyHandler;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.springframework.beans.factory.ObjectProvider;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.calm.proxy.ProxyHandler.*;

public class HttpProxyHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final ObjectProvider<ProxyHandler> handlerObjectProvider;

    public HttpProxyHandler(ObjectProvider<ProxyHandler> handlerObjectProvider) {
        this.handlerObjectProvider = handlerObjectProvider;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        List<ProxyHandler> collect = handlerObjectProvider.stream().collect(Collectors.toList());
        String auth = request.headers().get(AUTH_HEADER);
        Channel channel = ctx.channel();
        String requestBody = request.content().toString(StandardCharsets.UTF_8);
        channel.attr(REQUEST_BODY_KEY).set(requestBody);
        Map<String, List<String>> stringListMap = ProxyHandler.parseKV(auth);
        List<String> u = stringListMap.get("u");
        if (u != null && !u.isEmpty()) {
            channel.attr(UID_KEY).set(u.get(0));
        }
        for (ProxyHandler handler:collect){
            FullHttpRequest copy = request.copy();

            if(handler.isSupport(copy)){
                handler.handle(ctx,copy);
            }
        }
    }
}
