package com.calm.proxy.recode;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.service.UserPlanInfoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class CreatePlanRecodeHandler implements HandlerRecode {
    final Logger LOGGER = LoggerFactory.getLogger(CreatePlanRecodeHandler.class);

    @Resource
    private UserPlanInfoService userPlanInfoService;
    @Resource
    private ObjectMapper objectMapper;
    @Override
    public boolean support(Response response) {
        String uri = response.getUri();
        if (!uri.contains("plan")) {
            return false;
        }
        return uri.contains("create");

    }

    @Override
    public void handle(Response response) throws JsonProcessingException {
        JsonNode jsonObject = objectMapper.readTree(response.getResponseBody());
        int code = jsonObject.get("code").asInt(0);
        if (code != 0) {
            LOGGER.error("{}", jsonObject.get("err_msg"));
            return;
        }
        Map<String, String> stringListMap = ProxyHandler.parseKV(response.getRequestBody());
        String belongType = stringListMap.get("belong_type");
        String belongId = stringListMap.get("belong_id");
        String planId = jsonObject.get("data").get("plan_id").textValue();

        userPlanInfoService.newPlan(response.getUid(), belongId, Long.parseLong(belongType), planId);
    }
}
