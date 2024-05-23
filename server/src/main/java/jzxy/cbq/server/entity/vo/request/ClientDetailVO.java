package jzxy.cbq.server.entity.vo.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientDetailVO {

    @Parameter(description = "操作系统架构", required = true)
    @NotNull
    private String osArch;

    @Parameter(description = "操作系统名称", required = true)
    @NotNull
    private String osName;

    @Parameter(description = "操作系统版本", required = true)
    @NotNull
    private String osVersion;

    @Parameter(description = "操作系统位数（32/64位）", required = true)
    @NotNull
    private int osBit;

    @Parameter(description = "CPU 名称", required = true)
    @NotNull
    private String cpuName;

    @Parameter(description = "CPU 核心数", required = true)
    @NotNull
    private int cpuCore;

    @Parameter(description = "内存大小（单位自定义，如 GB）", required = true)
    @NotNull
    private double memory;

    @Parameter(description = "磁盘容量（单位自定义，如 GB）", required = true)
    @NotNull
    private double disk;

    @Parameter(description = "客户端 IP 地址", required = true)
    @NotNull
    private String ip;
}