package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.handler.DefaultProxyDataHandler;
import com.calm.proxy.listener.AfterConnectionListener;
import com.calm.proxy.repository.UserPlanInfoRepository;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DefaultProxyHandler implements ProxyHandler, Ordered {
    @Resource
    private UserPlanInfoRepository planInfoRepository;

    @Override
    public boolean isSupport(FullHttpRequest request) {
        return true;
    }

    @Override
    public void preHandle(ChannelHandlerContext ctx, FullHttpRequest request) {
        String auth = Optional.of(request).map(HttpMessage::headers).map(e -> e.get(ProxyHandler.AUTH_HEADER)).orElse("");
        Map<String, List<String>> stringListMap = ProxyHandler.parseKV(auth);
        List<String> u = stringListMap.get("u");
        if (u.isEmpty()) {
            return;
        }
        String s = u.get(0);
        ctx.channel().attr(ORIGIN_UID_ATTR_KAY).set(s);
    }

    @Override
    public void doHandle(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        //创建客户端连接目标机器
        ProxyHandler.connectToRemote(ctx, URI.create(request.uri()), 10000, new HttpContentDecompressor(), new HttpObjectAggregator(1000 * 1024 * 1024), new DefaultProxyDataHandler(planInfoRepository, request, ctx.channel())).addListener(new AfterConnectionListener(ctx, request, headers));
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
