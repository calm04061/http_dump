package com.calm.proxy.handler;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public abstract class CalmResponseSimpleChannelInboundHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
}
