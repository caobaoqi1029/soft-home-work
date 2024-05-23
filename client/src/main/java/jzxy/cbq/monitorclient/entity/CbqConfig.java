
package jzxy.cbq.monitorclient.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * CbqConfig 类用于存储 CBQ 相关的配置信息
 *
 * @version 1.0.0 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@ConfigurationProperties(prefix = "cbq")
@Configuration
@Data
public class CbqConfig {
    private String token;
    private String address;
}