package com.calm.proxy.recode;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface HandlerRecode {
    boolean support(Response response);

    void handle(Response response) throws JsonProcessingException;
}
