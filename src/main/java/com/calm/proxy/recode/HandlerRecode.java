package com.calm.proxy.recode;

public interface HandlerRecode {
    boolean support(Response response);

    void handle(Response response);
}
