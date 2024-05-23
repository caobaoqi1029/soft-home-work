package jzxy.cbq.monitorclient.utils;

import com.alibaba.fastjson2.JSONObject;
import jzxy.cbq.monitorclient.entity.BaseDetail;
import jzxy.cbq.monitorclient.entity.ConnectionConfig;
import jzxy.cbq.monitorclient.entity.Response;
import jzxy.cbq.monitorclient.entity.RuntimeDetail;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * 网络工具类，提供注册客户端、更新系统基本信息和运行时详情等功能
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/29 下午7:25
 */
@Slf4j
@Component
public class NetUtils {
    /**
     * HttpClient
     */
    private final HttpClient client = HttpClient.newHttpClient();
    /**
     * ConnectionConfig
     */
    @Lazy
    @Resource
    ConnectionConfig config;

    /**
     * 向服务端注册客户端
     *
     * @param address 客户端地址
     * @param token   客户端令牌
     * @return 注册是否成功的布尔值
     */
    public boolean registerToServer(String address, String token) {
        log.info("正在像服务端注册，请稍后...");
        Response response = this.doGet("/register", address, token);
        if (response.success()) {
            log.info("客户端注册已完成！");
        } else {
            log.error("客户端注册失败: {}", response.message());
        }
        return response.success();
    }

    /**
     * 执行 GET 请求
     *
     * @param url 请求的 URL 路径
     * @return 服务端响应
     */
    private Response doGet(String url) {
        return this.doGet(url, config.getAddress(), config.getToken());
    }

    /**
     * 执行带参数的 GET 请求
     *
     * @param url     请求的 URL 路径
     * @param address 服务端地址
     * @param token   访问令牌
     * @return 服务端响应
     */
    private Response doGet(String url, String address, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder().GET()
                    .uri(new URI(address + "/monitor" + url))
                    .header("Authorization", token)
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return JSONObject.parseObject(response.body()).to(Response.class);
        } catch (Exception e) {
            log.error("在发起服务端请求时出现问题", e);
            return Response.errorResponse(e);
        }
    }

    /**
     * 更新系统基本信息
     *
     * @param detail 包含系统基本信息的对象
     */
    public void updateBaseDetails(BaseDetail detail) {
        Response response = this.doPost("/detail", detail);
        if (response.success()) {
            log.info("系统基本信息已更新完成");
        } else {
            log.error("系统基本信息更新失败: {}", response.message());
        }
    }

    /**
     * 更新运行时详情
     *
     * @param detail 包含运行时详情的对象
     */
    public void updateRuntimeDetails(RuntimeDetail detail) {
        Response response = this.doPost("/runtime", detail);
        if (!response.success()) {
            log.warn("更新运行时状态时，接收到服务端的异常响应内容: {}", response.message());
        }
    }

    /**
     * 执行 POST 请求
     *
     * @param url  请求的 URL 路径
     * @param data 请求的数据对象
     * @return 服务端响应
     */
    private Response doPost(String url, Object data) {
        try {
            String rawData = JSONObject.from(data).toJSONString();
            HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(rawData))
                    .uri(new URI(config.getAddress() + "/monitor" + url))
                    .header("Authorization", config.getToken())
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return JSONObject.parseObject(response.body()).to(Response.class);
        } catch (Exception e) {
            log.error("在发起服务端请求时出现问题", e);
            return Response.errorResponse(e);
        }
    }
}
