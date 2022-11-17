package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.handler.DumpCreatePlanHandler;
import com.calm.proxy.listener.AfterConnectionListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


public class DumpCreatePlanProxyHandler implements ProxyHandler {
    Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DumpCreatePlanProxyHandler.class);

    @Override
    public boolean isSupport(FullHttpRequest request) {
        String path = URI.create(request.uri()).getPath();
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return path.contains("/plan");
    }

    @Override
    public void doHandle(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        LOGGER.info("handle:{} {}", request.method(), request.uri());

        String auth = headers.get(AUTH_HEADER);
        Map<String, List<String>> stringListMap = ProxyHandler.parseKV(auth);
        List<String> u = stringListMap.get("u");
        if (u != null && !u.isEmpty()) {
            ctx.channel().attr(ORIGIN_UID_KEY).set(u.get(0));
        }
        String newAUth = modifyKV(auth, "u", UID);
        headers.set(AUTH_HEADER, newAUth);
        ByteBuf content = request.content();
        String requestBody = content.toString(StandardCharsets.UTF_8);
        String uri = request.uri();
        HttpMethod method = request.method();
        //创建客户端连接目标机器
        ChannelFuture channelFuture = connectToRemote(ctx, URI.create(request.uri()).getHost(), 80, 10000, new DumpCreatePlanHandler());
        channelFuture.addListener(new AfterConnectionListener(ctx, request, headers, method, uri, requestBody));
    }


}
