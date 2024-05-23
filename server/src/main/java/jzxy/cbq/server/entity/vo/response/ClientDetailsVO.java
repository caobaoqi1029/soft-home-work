package jzxy.cbq.server.entity.vo.response;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class ClientDetailsVO {

    @Parameter(description = "客户端ID", required = true)
    private int id;

    @Parameter(description = "客户端名称", required = true)
    private String name;

    @Parameter(description = "在线状态", required = true)
    private boolean online;

    @Parameter(description = "节点名称", required = true)
    private String node;

    @Parameter(description = "地理位置", required = true)
    private String location;

    @Parameter(description = "IP地址", required = true)
    private String ip;

    @Parameter(description = "CPU型号", required = true)
    private String cpuName;

    @Parameter(description = "操作系统名称", required = true)
    private String osName;

    @Parameter(description = "操作系统版本", required = true)
    private String osVersion;

    @Parameter(description = "内存大小（单位：GB）", required = true)
    private double memory;

    @Parameter(description = "CPU核心数", required = true)
    private int cpuCore;

    @Parameter(description = "硬盘容量（单位：GB）", required = true)
    private double disk;
}