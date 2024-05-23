package jzxy.cbq.monitorclient.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>系统基础信息实体类</p>
 * <p>该类用于存储操作系统、CPU、内存、硬盘等基础硬件信息</p>
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Data
@Accessors(chain = true)
public class BaseDetail {
    /**
     * 操作系统架构
     */
    private String osArch;
    /**
     * 操作系统名称
     */
    private String osName;
    /**
     * 操作系统版本
     */
    private String osVersion;
    /**
     * 操作系统的位数（32 位或 64 位）
     */
    private int osBit;
    /**
     * CPU 型号
     */
    private String cpuName;
    /**
     * CPU 核心数
     */
    private int cpuCore;
    /**
     * 内存大小
     */
    private double memory;
    /**
     * 硬盘大小
     */
    private double disk;
    /**
     * 服务器 IP 地址
     */
    private String ip;
}