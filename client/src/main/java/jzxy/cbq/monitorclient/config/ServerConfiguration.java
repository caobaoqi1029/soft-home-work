package jzxy.cbq.monitorclient.config;

import com.alibaba.fastjson2.JSONObject;
import jzxy.cbq.monitorclient.entity.CbqConfig;
import jzxy.cbq.monitorclient.entity.ConnectionConfig;
import jzxy.cbq.monitorclient.utils.MonitorUtils;
import jzxy.cbq.monitorclient.utils.NetUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 服务器配置类，负责在应用启动时加载服务端连接配置，以及处理与服务端的注册逻辑。
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Slf4j
@Configuration
public class ServerConfiguration implements ApplicationRunner {
    private static final Scanner scanner = new Scanner(System.in);
    @Resource
    CbqConfig config;
    @Resource
    NetUtils net;
    @Resource
    MonitorUtils monitor;
    boolean flag = false;

    /**
     * 应用启动时执行的逻辑，用于向服务端更新基本系统信息。
     *
     * @param args 应用启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("正在向服务端更新基本系统信息...");
        net.updateBaseDetails(monitor.monitorBaseDetail());
    }

    /**
     * 创建服务端连接配置。
     */
    @Bean
    ConnectionConfig connectionConfig() {
        ConnectionConfig configByFile = this.readConfigurationFromFile();
        if (configByFile != null) {
            log.info("加载到 config/server.json 文件，开始通过文件注册 INFO {} , {}", configByFile.getAddress(), configByFile.getToken());
        } else {
            while (!flag) {
                ConnectionConfig connectionConfig = new ConnectionConfig(config.getAddress(), config.getToken());

                log.warn("未找到 config/server.json 文件，开始通过加载 ENV 方式注册");
                log.info("加载到 ENV Token: {} Address: {}", connectionConfig.getToken(), connectionConfig.getAddress());
                flag = net.registerToServer(connectionConfig.getAddress(), connectionConfig.getToken());
                if (flag) {
                    saveConfigurationToFile(connectionConfig);
                    log.info("通过 ENV 注册成功，已将配置保存到 config/server.json 文件");
                    return connectionConfig;
                }
                log.warn("文件注册和 ENV 注册均失败，开始通过手动录入方式注册");
                connectionConfig = this.readConfigurationFromScanner();
                flag = net.registerToServer(connectionConfig.getAddress(), connectionConfig.getToken());
                if (flag) {
                    saveConfigurationToFile(connectionConfig);
                    log.info("通过 手动录入方式 注册成功，已将配置保存到 config/server.json 文件");
                    return connectionConfig;
                }
            }
        }
        log.info("通过 config/server.json 文件 注册成功");
        return configByFile;
    }

    /**
     * 向服务端注册，并保存注册后的配置信息。
     *
     * @return ConnectionConfig 注册后的服务端连接配置
     */
    private ConnectionConfig readConfigurationFromScanner() {
        log.info("请输入需要注册的服务端访问地址，地址类似于 'http://192.168.0.22:8080' 这种写法:");
        config.setAddress(scanner.nextLine());
        log.info("请输入服务端生成的用于注册客户端的 Token 秘钥:");
        config.setToken(scanner.nextLine());
        return new ConnectionConfig(config.getAddress(), config.getToken());
    }

    /**
     * 将服务端连接配置保存到文件中。
     *
     * @param config 服务端连接配置
     */
    private void saveConfigurationToFile(ConnectionConfig config) {
        File dir = new File("config");
        if (!dir.exists() && dir.mkdir())
            log.info("创建用于保存服务端连接信息的目录已完成");
        File file = new File("config/server.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(JSONObject.from(config).toJSONString());
        } catch (IOException e) {
            log.error("保存配置文件时出现问题", e);
        }
        log.info("服务端连接信息已保存成功！");
    }

    /**
     * 从文件中读取服务端连接配置。
     *
     * @return ConnectionConfig 读取到的服务端连接配置，如果文件不存在或读取失败则返回 null
     */
    private ConnectionConfig readConfigurationFromFile() {
        File configurationFile = new File("config/server.json");
        if (configurationFile.exists()) {
            try (FileInputStream stream = new FileInputStream(configurationFile)) {
                String raw = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                return JSONObject.parseObject(raw).to(ConnectionConfig.class);
            } catch (IOException e) {
                log.error("读取配置文件时出错", e);
            }
        }
        return null;
    }
}