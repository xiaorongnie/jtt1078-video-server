package com.transcodegroup.jtt1078.ws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * 集成 websocket-原生注解 wss://localhost:8443/talk
 * <P>
 * 输入wss://localhost:8443/websocket/1/1,在界面中也可以看到“连接成功”的字样。
 * <P>
 * 此时需主意的是：不能用IP去访问，需要用域名，否则连接不会成功。从WebSocketde源码阅读可以看出，如果是wss的话，WebSocket会去验证域名，验证不通过直接就抛出io异常，导致链接不成功。
 * 
 * @author eason
 * @date 2021/04/28
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpoint() {
        return new ServerEndpointExporter();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduling = new ThreadPoolTaskScheduler();
        scheduling.setPoolSize(10);
        scheduling.initialize();
        return scheduling;
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 接收消息大小设置为5M
        container.setMaxTextMessageBufferSize(5 * 1024 * 1024);
        container.setMaxBinaryMessageBufferSize(5 * 1024 * 1024);
        return container;
    }

}
