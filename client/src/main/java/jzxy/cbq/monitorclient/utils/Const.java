package jzxy.cbq.monitorclient.utils;

/**
 * Const 类包含应用程序中使用的静态常量。
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/29 下午7:12
 */
public final class Const {
    /**
     * Quartz 作业的唯一标识符
     */
    public static final String QUARTZ_JOB_ID = "monitor-task";
    /**
     * Quartz 作业的触发器使用的时间表达式，此处为每 10 秒触发一次
     */
    public static final String QUARTZ_TRIGGER_CRON = "*/10 * * * * ?";
    /**
     * Quartz 触发器的唯一标识符
     */
    public static final String QUARTZ_TRIGGER_ID = "monitor-trigger";
}
