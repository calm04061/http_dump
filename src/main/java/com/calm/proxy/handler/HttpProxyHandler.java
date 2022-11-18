package com.calm.proxy.handler;

import com.calm.proxy.ProxyHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
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
        if (StringUtils.hasText(auth)) {
            auth = ProxyHandler.toString(parseKV(URLDecoder.decode(auth,StandardCharsets.UTF_8)));
            request.headers().set(AUTH_HEADER, auth);
        }
        Channel channel = ctx.channel();

        String requestBody = request.content().toString(StandardCharsets.UTF_8);
        channel.attr(REQUEST_BODY_KEY).set(requestBody);
        Map<String, String> stringListMap = ProxyHandler.parseKV(auth);
        channel.attr(UID_KEY).set(stringListMap.get("u"));
        for (ProxyHandler handler:collect){
            FullHttpRequest copy = request.copy();

            if(handler.isSupport(copy)){
                handler.handle(ctx,copy);
            }
        }
    }
}
