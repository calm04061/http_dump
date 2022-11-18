package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.entity.UserPlanInfo;
import com.calm.proxy.service.UserPlanInfoService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Component
public class DumpPauseOrUpdatePlanProxyHandler extends AbstractDumpProxyHandler implements ProxyHandler {
    @Resource
    private UserPlanInfoService userPlanInfoService;

    @Override
    public boolean isSupport(FullHttpRequest request) {
        String path = URI.create(request.uri()).getPath();
        if (!StringUtils.hasText(path)) {
            return false;
        }
        if (!path.contains("/plan")) {
            return false;
        }
        return (path.contains("pause") || path.contains("update") || path.contains("switch"));
    }

    @Override
    public boolean preHandler(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        super.preHandler(ctx, request, headers);
        String s = request.content().toString(StandardCharsets.UTF_8);
        Map<String, String> stringStringMap = ProxyHandler.parseKV(s);
        String planId = stringStringMap.get("plan_id");
        String uid = ctx.channel().attr(UID_KEY).get();

        Optional<UserPlanInfo> byOriginPlanId = userPlanInfoService.findByOriginPlanId(uid, planId);
        if (byOriginPlanId.isPresent()) {
            stringStringMap.put("plan_id", byOriginPlanId.get().getPlanId());
            String s1 = ProxyHandler.toString(stringStringMap);
            byte[] bytes = s1.getBytes(StandardCharsets.UTF_8);
            request.content().clear();
            request.content().writeBytes(bytes);
            return true;
        }
        return false;
    }
}
