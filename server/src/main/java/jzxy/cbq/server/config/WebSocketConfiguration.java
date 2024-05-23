package jzxy.cbq.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


/**
 * WebSocket 配置类
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Configuration
public class WebSocketConfiguration {
    /**
     * 创建并返回一个 ServerEndpointExporter 的实例。
     * ServerEndpointExporter 是用于扫描并注册 WebSocket 端点的组件。
     *
     * @return ServerEndpointExporter 返回一个 ServerEndpointExporter 实例。
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
