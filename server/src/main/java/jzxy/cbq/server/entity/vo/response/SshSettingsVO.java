package jzxy.cbq.server.entity.vo.response;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class SshSettingsVO {

    @Parameter(description = "SSH 服务器 IP 地址", required = true)
    private String ip;

    @Parameter(description = "SSH 服务器端口号")
    private int port = 22;

    @Parameter(description = "SSH 登录用户名", required = true)
    private String username;

    @Parameter(description = "SSH 登录密码", required = true)
    private String password;
}
