package com.calm.proxy.handler;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

public abstract class CalmRequestSimpleChannelInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
}
