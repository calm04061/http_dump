package com.calm.proxy.recode;

import com.alibaba.fastjson.JSONObject;
import com.calm.proxy.ProxyHandler;
import com.calm.proxy.service.UserPlanInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class CreatePlanRecodeHandler implements HandlerRecode {
    Logger LOGGER = LoggerFactory.getLogger(CreatePlanRecodeHandler.class);

    @Resource
    private UserPlanInfoService userPlanInfoService;

    @Override
    public boolean support(Response response) {
        String uri = response.getUri();
        if (!uri.contains("plan")) {
            return false;
        }
        return uri.contains("create");

    }

    @Override
    public void handle(Response response) {
        JSONObject jsonObject = JSONObject.parseObject(response.getResponseBody());
        Integer code = jsonObject.getInteger("code");
        if (code != 0) {
            LOGGER.error("{}", jsonObject.getString("err_msg"));
            return;
        }
        Map<String, String> stringListMap = ProxyHandler.parseKV(response.getRequestBody());
        String belongType = stringListMap.get("belong_type");
        String belongId = stringListMap.get("belong_id");
        String planId = jsonObject.getJSONObject("data").getString("plan_id");

        userPlanInfoService.newPlan(response.getUid(), belongId, Long.parseLong(belongType),planId);
    }
}
