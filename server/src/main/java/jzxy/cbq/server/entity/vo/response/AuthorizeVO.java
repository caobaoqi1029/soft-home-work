package jzxy.cbq.server.entity.vo.response;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

import java.util.Date;

/**
 * 登录验证成功的用户信息响应
 */
@Data
public class AuthorizeVO {
    @Parameter(description = "用户名", required = true)
    private String username;

    @Parameter(description = "用户邮箱", required = true)
    private String email;

    @Parameter(description = "用户头像URL")
    private String avatar;

    @Parameter(description = "用户角色", required = true)
    private String role;

    @Parameter(description = "授权令牌", required = true)
    private String token;

    @Parameter(description = "令牌过期时间（日期对象）", required = true)
    private Date expire;
}
