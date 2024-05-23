package jzxy.cbq.monitorclient.entity;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ConnectionConfig 类用于存储 Connection 相关的配置信息
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionConfig {
    /**
     * 服务器地址
     */
    String address;
    /**
     * token
     */
    String token;
}
