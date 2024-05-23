package jzxy.cbq.monitorclient.task;

import jzxy.cbq.monitorclient.entity.RuntimeDetail;
import jzxy.cbq.monitorclient.utils.MonitorUtils;
import jzxy.cbq.monitorclient.utils.NetUtils;
import jakarta.annotation.Resource;
import org.quartz.JobExecutionContext;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * MonitorJobBean类，负责执行监控任务。
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/29 下午7:19
 */
@Component
public class MonitorJobBean extends QuartzJobBean {
    /**
     * MonitorUtils 工具类，用于监控运行时详情。
     */
    @Resource
    MonitorUtils monitor;

    /**
     * NetUtils 工具类，用于更新运行时详情到网络。
     */
    @Resource
    NetUtils net;

    /**
     * 执行内部任务逻辑。
     *
     * @param context 提供作业执行上下文的 JobExecutionContext。
     */
    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) {
        RuntimeDetail runtimeDetail = monitor.monitorRuntimeDetail();
        net.updateRuntimeDetails(runtimeDetail);
    }
}
