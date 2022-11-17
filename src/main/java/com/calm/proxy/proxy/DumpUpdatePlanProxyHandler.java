package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.entity.UserPlanInfo;
import com.calm.proxy.handler.UpdatePlanHandler;
import com.calm.proxy.listener.AfterConnectionListener;
import com.calm.proxy.repository.UserPlanInfoRepository;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

//@Component
public class DumpUpdatePlanProxyHandler implements ProxyHandler {
    private final UserPlanInfoRepository planInfoRepository;

    public DumpUpdatePlanProxyHandler(UserPlanInfoRepository planInfoRepository) {
        this.planInfoRepository = planInfoRepository;
    }

    @Override
    public boolean isSupport(FullHttpRequest request) {
        String path = URI.create(request.uri()).getPath();
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return path.contains("plan/update");
    }

    @Override
    public void doHandle(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        LOGGER.info("dump request :{} {}", request.method(), request.uri());
        String uid = ctx.channel().attr(UID_ATTR_KAY).get();
        modifyUser(headers, uid);
//        ByteBuf content = request.content();
//        String s = content.toString(StandardCharsets.UTF_8);
//        String planId = planInfoRepository.getUserPlanInfoByUid(uid).map(UserPlanInfo::getPlanId).orElse("");
//
//        String body = modifyKV(s, "plan_id", planId);
//        content.clear();
//        content.writeCharSequence(body, StandardCharsets.UTF_8);
//        request.replace(content);
        //创建客户端连接目标机器
        ProxyHandler.connectToRemote(ctx, URI.create(request.uri()), 10000,
//                new HttpContentDecompressor(),
                new HttpObjectAggregator(1000 * 1024 * 1024), new UpdatePlanHandler(planInfoRepository, ctx.channel())).addListener(new AfterConnectionListener(ctx, request, headers));
    }

}
