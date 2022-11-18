package com.calm.proxy.proxy;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
public class DumpReportWordProxyHandler extends AbstractDumpProxyHandler implements ProxyHandler {
    @Resource
    private UserPlanInfoService userPlanInfoService;

    @Override
    public boolean isSupport(FullHttpRequest request) {
        String path = URI.create(request.uri()).getPath();
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return path.contains("/plan/report-word");
    }

    @Override
    public boolean preHandler(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        super.preHandler(ctx, request, headers);
        String s = request.content().toString(StandardCharsets.UTF_8);
        Map<String, String> stringStringMap = ProxyHandler.parseKV(s);
        String req = stringStringMap.get("req");
        String uid = ctx.channel().attr(UID_KEY).get();
        JSONObject jsonObject = JSONObject.parseObject(req);
        JSONArray known = jsonObject.getJSONArray("known");
        replacePlanId(uid, known);
        known = jsonObject.getJSONArray("master");
        replacePlanId(uid, known);
        request.content().clear();

        String result = jsonObject.toJSONString();
        stringStringMap.put("req", result);
        String s1 = ProxyHandler.toString(stringStringMap);
        request.content().writeCharSequence(s1, StandardCharsets.UTF_8);
        return true;
    }

    private void replacePlanId(String uid, JSONArray known) {
        for (int i = 0; i < known.size(); i++) {
            JSONObject jsonObject1 = known.getJSONObject(i);
            String planId = jsonObject1.getString("plan_id");
            Optional<UserPlanInfo> byOriginPlanId = userPlanInfoService.findByOriginPlanId(uid, planId);
            byOriginPlanId.ifPresent(userPlanInfo -> jsonObject1.put("plan_id", userPlanInfo.getPlanId()));
        }
    }
}
