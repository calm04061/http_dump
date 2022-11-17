package com.calm.proxy.recode;

import io.netty.handler.codec.http.HttpMethod;

public class Response {
    private String uid;
    private String requestBody;
    private String responseBody;
    private String uri;
    private HttpMethod method;
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "Response{" +
                "uid='" + uid + '\'' +
                ", requestBody='" + requestBody + '\'' +
                ", responseBody='" + responseBody + '\'' +
                ", uri='" + uri + '\'' +
                ", method=" + method +
                '}';
    }
}
