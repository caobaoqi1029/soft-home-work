
package jzxy.cbq.monitorclient.config;

import jzxy.cbq.monitorclient.task.MonitorJobBean;
import jzxy.cbq.monitorclient.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz配置类，用于配置Quartz定时任务框架。
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Slf4j
@Configuration
public class QuartzConfiguration {

    /**
     * 配置定时任务的细节。
     *
     * @return 返回配置的 JobDetail 实例，代表一个定时任务的详细信息。
     */
    @Bean
    public JobDetail jobDetailFactoryBean() {
        return JobBuilder.newJob(MonitorJobBean.class)
                .withIdentity(Const.QUARTZ_JOB_ID)
                .storeDurably()
                .build();
    }

    /**
     * 配置 cron类 型的触发器，用于按照指定的 cron 表达式定时触发任务。
     *
     * @param detail 定时任务的详细信息
      * @return 返回配置的 Trigger 实例，代表一个任务的触发器。
     */
    @Bean
    public Trigger cronTriggerFactoryBean(JobDetail detail) {
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule(Const.QUARTZ_TRIGGER_CRON);
        return TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity(Const.QUARTZ_TRIGGER_ID)
                .withSchedule(cron)
                .build();
    }
}