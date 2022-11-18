package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class AbstractDumpProxyHandler extends AbstractProxyHandler{
    @Override
    public boolean preHandler(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) throws JsonProcessingException {
        String auth = headers.get(AUTH_HEADER);
        Map<String, String> stringListMap = ProxyHandler.parseKV(URLEncoder.encode(auth, StandardCharsets.UTF_8));
        ctx.channel().attr(ORIGIN_UID_KEY).set(stringListMap.get("u"));
       modifyUser(headers,  UID);
        ctx.channel().attr(UID_KEY).set(UID);
        return true;
    }
}
