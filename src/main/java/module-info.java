module com.calm.proxy {
    requires io.netty.buffer;
    requires io.netty.transport;
    requires io.netty.codec.http;
    requires io.netty.codec;
    requires org.slf4j;
    requires spring.beans;
    requires java.annotation;
    requires io.netty.common;
    requires spring.core;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.data.jpa;
    requires java.persistence;
    requires org.hibernate.orm.core;
    requires com.fasterxml.jackson.databind;
    opens com.calm.proxy to spring.core;
    opens com.calm.proxy.entity to org.hibernate.orm.core, spring.core;
    opens com.calm.proxy.service to spring.core;
    opens com.calm.proxy.recode to spring.core;
    opens com.calm.proxy.proxy to spring.core;
    exports com.calm.proxy to spring.beans, spring.context;
    exports com.calm.proxy.proxy to spring.beans;
    exports com.calm.proxy.service to spring.beans;
    exports com.calm.proxy.recode to spring.beans;
    exports com.calm.proxy.entity to spring.beans;
}
