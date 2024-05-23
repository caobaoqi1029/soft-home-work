package jzxy.cbq.server.config;

import jzxy.cbq.server.utils.Const;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置类
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Configuration
public class RabbitConfiguration {
    /**
     * 创建并定义一个名为 mail 的持久化队列。
     *
     * @return Queue 返回构建好的队列对象。
     */
    @Bean("mailQueue")
    public Queue queue(){
        return QueueBuilder
                .durable(Const.MQ_MAIL)
                .build();
    }
}