package jzxy.cbq.server.entity.vo.response;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class ClientPreviewVO {

    @Parameter(description = "客户端ID", required = true)
    private int id;

    @Parameter(description = "在线状态", required = true)
    private boolean online;

    @Parameter(description = "客户端名称", required = true)
    private String name;

    @Parameter(description = "地理位置", required = true)
    private String location;

    @Parameter(description = "操作系统名称", required = true)
    private String osName;

    @Parameter(description = "操作系统版本", required = true)
    private String osVersion;

    @Parameter(description = "IP地址", required = true)
    private String ip;

    @Parameter(description = "CPU型号", required = true)
    private String cpuName;

    @Parameter(description = "CPU核心数", required = true)
    private int cpuCore;

    @Parameter(description = "内存大小（单位：GB）", required = true)
    private double memory;

    @Parameter(description = "CPU使用率（百分比）", required = true)
    private double cpuUsage;

    @Parameter(description = "内存使用率（百分比）", required = true)
    private double memoryUsage;

    @Parameter(description = "网络上传速率（单位：Mbps）", required = true)
    private double networkUpload;

    @Parameter(description = "网络下载速率（单位：Mbps）", required = true)
    private double networkDownload;
}