package com.calm.proxy.handler;

import com.calm.proxy.recode.HandlerRecode;
import com.calm.proxy.recode.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static com.calm.proxy.ProxyHandler.*;

public class ResponseDataHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectProvider<HandlerRecode> handlerRecodes;

    public ResponseDataHandler(ObjectProvider<HandlerRecode> handlerRecodes) {
        this.handlerRecodes = handlerRecodes;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        //修改http响应体返回至客户端
        String responseBody = msg.content().toString(StandardCharsets.UTF_8);
        String uid = ctx.channel().attr(UID_KEY).get();
        String requestBody = ctx.channel().attr(REQUEST_BODY_KEY).get();
        String uri = ctx.channel().attr(REQUEST_URI_KEY).get();
        HttpMethod method = ctx.channel().attr(REQUEST_METHOD_KEY).get();
        Response response = new Response();
        response.setUid(uid);
        response.setResponseBody(responseBody);
        response.setRequestBody(requestBody);
        response.setUri(uri);
        response.setMethod(method);
        response.setRequestBody(requestBody);
        List<HandlerRecode> collect = handlerRecodes.stream().collect(Collectors.toList());
        for (HandlerRecode handlerRecode : collect) {
            if (handlerRecode.support(response)) {
                try {
                    handlerRecode.handle(response);
                } catch (JsonProcessingException e) {
                    logger.error("", e);
                }
            }
        }
    }
}
