package jzxy.cbq.server.entity.vo.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SshConnectionVO {
    @Parameter(description = "SSH 连接 ID", required = true)
    private int id;

    @Parameter(description = "SSH 端口号", required = true)
    private int port;

    @Parameter(description = "SSH 用户名", required = true)
    @NotNull(message = "SSH 用户名不能为空")
    @Length(min = 1, message = "SSH 用户名至少包含 1 个字符")
    private String username;

    @Parameter(description = "SSH 密码", required = true)
    @NotNull(message = "SSH 密码不能为空")
    @Length(min = 1, message = "SSH 密码至少包含")
    private String password;
}
