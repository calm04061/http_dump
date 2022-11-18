package com.calm.proxy.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import  com.fasterxml.jackson.databind.*;
import com.calm.proxy.ProxyHandler;
import com.calm.proxy.entity.UserPlanInfo;
import com.calm.proxy.service.UserPlanInfoService;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    @Resource
    private ObjectMapper objectMapper;
    @Override
    public boolean isSupport(FullHttpRequest request) {
        String path = URI.create(request.uri()).getPath();
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return path.contains("/plan/report-word");
    }

    @Override
    public boolean preHandler(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) throws JsonProcessingException {
        super.preHandler(ctx, request, headers);
        String s = request.content().toString(StandardCharsets.UTF_8);
        Map<String, String> stringStringMap = ProxyHandler.parseKV(s);
        String req = stringStringMap.get("req");
        String uid = ctx.channel().attr(UID_KEY).get();
        JsonNode jsonNode = objectMapper.readTree(req);
        JsonNode known = jsonNode.get("known");
        replacePlanId(uid, known);
        known = jsonNode.get("master");
        replacePlanId(uid, known);
        request.content().clear();

        String result = objectMapper.writeValueAsString(jsonNode);
        stringStringMap.put("req", result);
        String s1 = ProxyHandler.toString(stringStringMap);
        request.content().writeCharSequence(s1, StandardCharsets.UTF_8);
        return true;
    }

    private void replacePlanId(String uid, JsonNode known) {
        for (int i = 0; i < known.size(); i++) {
            JsonNode jsonObject1 = known.get(i);
            String planId = jsonObject1.get("plan_id").textValue();
            Optional<UserPlanInfo> byOriginPlanId = userPlanInfoService.findByOriginPlanId(uid, planId);
            byOriginPlanId.ifPresent(userPlanInfo -> ((ObjectNode)jsonObject1).put("plan_id", userPlanInfo.getPlanId()));
        }
    }
}
