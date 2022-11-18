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
import java.util.Map;
import java.util.Optional;

@Component
public class DumpDeletePlanProxyHandler extends AbstractDumpProxyHandler implements ProxyHandler {
    @Resource
    private UserPlanInfoService userPlanInfoService;

    @Override
    public boolean isSupport(FullHttpRequest request) {
        String path = URI.create(request.uri()).getPath();
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return path.contains("/plan") && path.contains("delete");
    }

    @Override
    public boolean preHandler(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        super.preHandler(ctx, request, headers);
        String uri = request.uri();
        URI uriObj = URI.create(uri);
        String rawQuery = URI.create(uri).getRawQuery();
        Map<String, String> stringListMap1 = ProxyHandler.parseKV(rawQuery);
        String planId = stringListMap1.get("plan_id");
        Optional<UserPlanInfo> byOriginPlanId = userPlanInfoService.findByOriginPlanId(UID, planId);
        if (byOriginPlanId.isPresent()) {
            UserPlanInfo userPlanInfo = byOriginPlanId.get();
            String newQuery = modifyKV(rawQuery, "plan_id", userPlanInfo.getPlanId());
            uri = String.format("%s://%s%s?%s", uriObj.getScheme(), uriObj.getHost(), uriObj.getPath(), newQuery);
            request.setUri(uri);
            return true;
        }
        return false;
    }

}
