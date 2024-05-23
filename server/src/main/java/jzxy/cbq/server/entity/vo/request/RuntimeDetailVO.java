package jzxy.cbq.server.entity.vo.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RuntimeDetailVO {

    @Parameter(description = "时间戳（毫秒）", required = true)
    @NotNull
    private long timestamp;

    @Parameter(description = "CPU 使用率（百分比）", required = true)
    @NotNull
    private double cpuUsage;

    @Parameter(description = "内存使用率（百分比）", required = true)
    @NotNull
    private double memoryUsage;

    @Parameter(description = "磁盘使用率（百分比）", required = true)
    @NotNull
    private double diskUsage;

    @Parameter(description = "网络上传速率（单位自定义，如 Mbps）", required = true)
    @NotNull
    private double networkUpload;

    @Parameter(description = "网络下载速率（单位自定义，如 Mbps）", required = true)
    @NotNull
    private double networkDownload;

    @Parameter(description = "磁盘读取速率（单位自定义，如 MB/s）", required = true)
    @NotNull
    private double diskRead;

    @Parameter(description = "磁盘写入速率（单位自定义，如 MB/s）", required = true)
    @NotNull
    private double diskWrite;
}
