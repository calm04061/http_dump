package com.calm.proxy.listener;

import com.calm.proxy.ProxyHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static com.calm.proxy.ProxyHandler.*;

public class AfterConnectionListener implements ChannelFutureListener {
    Logger LOGGER = LoggerFactory.getLogger(AfterConnectionListener.class);

    private final ChannelHandlerContext ctx;
    private final FullHttpRequest request;
    private final HttpHeaders headers;
    private final String content;
    private final String uri;
    private final HttpMethod method;

    public AfterConnectionListener(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers, HttpMethod method, String uri, String content) {
        this.ctx = ctx;
        this.request = request;
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.content = content;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isSuccess()) {
            //代理服务器连接目标服务器成功
            //发送消息到目标服务器
            //关闭长连接
            headers.set(HttpHeaderNames.CONNECTION, "close");
            headers.set(HttpHeaderNames.HOST, "recite.gray.perfectlingo.com");
            Channel channel = channelFuture.channel();
            LOGGER.info("{}", channel);


            channel.attr(REQUEST_BODY_KEY).set(content);
            channel.attr(REQUEST_URI_KEY).set(uri);
            channel.attr(REQUEST_METHOD_KEY).set(method);

            String auth = request.headers().get(AUTH_HEADER);
            if (StringUtils.hasText(auth)) {
                String decode = URLDecoder.decode(auth, StandardCharsets.UTF_8);
                Map<String, String> stringListMap = ProxyHandler.parseKV(decode);
                channel.attr(UID_KEY).set(stringListMap.get("u"));
            }
            URI uri1 = URI.create(uri);
            String rawQuery = Optional.of(uri1).map(URI::getRawQuery).map(e -> "?" + e).orElse("");
            request.setUri(uri1.getPath() + rawQuery);
            //转发请求到目标服务器
            channel.writeAndFlush(request).addListener(new AfterRequestCloseListener(ctx));
        } else {
            ReferenceCountUtil.retain(request);
            ctx.writeAndFlush(getResponse(HttpResponseStatus.BAD_REQUEST, "代理服务连接远程服务失败")).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
