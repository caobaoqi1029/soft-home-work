package jzxy.cbq.monitorclient.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * RuntimeDetail 类封装了系统运行时的详细监控指标，包括
 * CPU 使用率、内存使用率、磁盘使用率、网络上传/下载速率以及磁盘读写速率
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Data
@Accessors(chain = true)
public class RuntimeDetail {
    /**
     * 时间戳，记录数据收集的具体时间点
     */
    long timestamp;
    /**
     * CPU使用率
     */
    double cpuUsage;
    /**
     * 内存使用率
     */
    double memoryUsage;
    /**
     * 磁盘使用率
     */
    double diskUsage;
    /**
     * 网络上传速度
     */
    double networkUpload;
    /**
     * 网络下载速度
     */
    double networkDownload;
    /**
     * 磁盘读取速度
     */
    double diskRead;
    /**
     * 磁盘写入速度
     */
    double diskWrite;
}