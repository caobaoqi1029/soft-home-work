package jzxy.cbq.monitorclient.utils;

import jzxy.cbq.monitorclient.entity.BaseDetail;
import jzxy.cbq.monitorclient.entity.RuntimeDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

/**
 * 监控工具类
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/29 下午 7:33
 */
@Slf4j
@Component
public class MonitorUtils {

    private final SystemInfo info = new SystemInfo();
    private final Properties properties = System.getProperties();

    /**
     * 监控基础详情
     * <p>
     * 获取操作系统的架构、名称、版本，CPU 名称、核心数，内存总量，磁盘总量以及主机 IP。
     *
     * @return BaseDetail 基础详情实体
     */
    public BaseDetail monitorBaseDetail() {
        OperatingSystem os = info.getOperatingSystem();
        HardwareAbstractionLayer hardware = info.getHardware();
        double memory = hardware.getMemory().getTotal() / 1024.0 / 1024 / 1024; // 内存总量（GB）
        double diskSize = Arrays.stream(File.listRoots()).mapToLong(File::getTotalSpace).sum() / 1024.0 / 1024 / 1024; // 磁盘总量（GB）
        String ip = Objects.requireNonNull(this.findNetworkInterface(hardware)).getIPv4addr()[0]; // 主机IP
        return new BaseDetail()
                .setOsArch(properties.getProperty("os.arch"))
                .setOsName(os.getFamily())
                .setOsVersion(os.getVersionInfo().getVersion())
                .setOsBit(os.getBitness())
                .setCpuName(hardware.getProcessor().getProcessorIdentifier().getName())
                .setCpuCore(hardware.getProcessor().getLogicalProcessorCount())
                .setMemory(memory)
                .setDisk(diskSize)
                .setIp(ip);
    }

    /**
     * 监控运行时详情
     * <p>
     * 获取 CPU 使用率、内存使用率、磁盘使用率、网络上传速度、网络下载速度、磁盘读取速度和磁盘写入速度。
     *
     * @return RuntimeDetail 运行时详情实体
     */
    public RuntimeDetail monitorRuntimeDetail() {
        double statisticTime = 0.5;
        try {
            HardwareAbstractionLayer hardware = info.getHardware();
            NetworkIF networkInterface = Objects.requireNonNull(this.findNetworkInterface(hardware));
            CentralProcessor processor = hardware.getProcessor();
            double upload = networkInterface.getBytesSent(), download = networkInterface.getBytesRecv();
            double read = hardware.getDiskStores().stream().mapToLong(HWDiskStore::getReadBytes).sum();
            double write = hardware.getDiskStores().stream().mapToLong(HWDiskStore::getWriteBytes).sum();
            long[] ticks = processor.getSystemCpuLoadTicks();
            Thread.sleep((long) (statisticTime * 1000));
            networkInterface = Objects.requireNonNull(this.findNetworkInterface(hardware));
            upload = (networkInterface.getBytesSent() - upload) / statisticTime; // 网络上传速度（KB/s）
            download = (networkInterface.getBytesRecv() - download) / statisticTime; // 网络下载速度（KB/s）
            read = (hardware.getDiskStores().stream().mapToLong(HWDiskStore::getReadBytes).sum() - read) / statisticTime; // 磁盘读取速度（MB/s）
            write = (hardware.getDiskStores().stream().mapToLong(HWDiskStore::getWriteBytes).sum() - write) / statisticTime; // 磁盘写入速度（MB/s）
            double memory = (hardware.getMemory().getTotal() - hardware.getMemory().getAvailable()) / 1024.0 / 1024 / 1024; // 内存使用量（GB）
            double disk = Arrays.stream(File.listRoots())
                    .mapToLong(file -> file.getTotalSpace() - file.getFreeSpace()).sum() / 1024.0 / 1024 / 1024; // 磁盘使用量（GB）
            return new RuntimeDetail()
                    .setCpuUsage(this.calculateCpuUsage(processor, ticks))
                    .setMemoryUsage(memory)
                    .setDiskUsage(disk)
                    .setNetworkUpload(upload / 1024)
                    .setNetworkDownload(download / 1024)
                    .setDiskRead(read / 1024 / 1024)
                    .setDiskWrite(write / 1024 / 1024)
                    .setTimestamp(new Date().getTime());
        } catch (Exception e) {
            log.error("读取运行时数据出现问题", e);
        }
        return null;
    }

    /**
     * 计算 CPU 使用率
     *
     * @param processor 中央处理器对象
     * @param prevTicks 上一统计周期的 CPUtick 数
     * @return CPU使用率
     */
    private double calculateCpuUsage(CentralProcessor processor, long[] prevTicks) {
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()]
                - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softIrq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()]
                - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()]
                - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long cUser = ticks[CentralProcessor.TickType.USER.getIndex()]
                - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()]
                - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()]
                - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = cUser + nice + cSys + idle + ioWait + irq + softIrq + steal;
        return (cSys + cUser) * 1.0 / totalCpu;
    }

    /**
     * 查找网络接口
     * <p>
     * 遍历所有网络接口，找到第一个符合条件的网络接口。条件包括：非回环接口、非点对点接口、处于启用状态、非虚拟接口、接口名称以"eth"、"en"或"wlp"开头、具有IPv4地址。
     *
     * @param hardware 硬件抽象层对象
     * @return NetworkIF 符合条件的网络接口对象
     */
    private NetworkIF findNetworkInterface(HardwareAbstractionLayer hardware) {
        try {
            for (NetworkIF network : hardware.getNetworkIFs()) {
                String[] ipv4Addr = network.getIPv4addr();
                NetworkInterface ni = network.queryNetworkInterface();
                if (!ni.isLoopback() && !ni.isPointToPoint() && ni.isUp() && !ni.isVirtual()
                        && (ni.getName().startsWith("eth") || ni.getName().startsWith("en") || ni.getName().startsWith("wlp"))
                        && ipv4Addr.length > 0) {
                    return network;
                }
            }
        } catch (IOException e) {
            log.error("读取网络接口信息时出错", e);
        }
        return null;
    }
}
