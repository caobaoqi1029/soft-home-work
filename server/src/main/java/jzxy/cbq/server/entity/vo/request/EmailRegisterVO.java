package jzxy.cbq.server.entity.vo.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * EmailRegisterVO
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/30 上午11:40
 *
 * <p>EmailRegisterVO</p>
 */
@Data
public class EmailRegisterVO {
    @Parameter(name = "email", description = "邮箱地址", in = ParameterIn.PATH, required = true)
    @Email(message = "请输入有效的邮箱地址")
    String email;

    @Parameter(name = "code", description = "验证码", in = ParameterIn.PATH, required = true)
    @Length(max = 6, min = 6, message = "验证码长度需为 6")
    String code;

    @Parameter(name = "username", description = "用户名", in = ParameterIn.PATH, required = true)
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$")
    @Length(min = 1, max = 10, message = "用户名长度需为 1-10")
    String username;

    @Parameter(name = "password", description = "密码", in = ParameterIn.PATH, required = true)
    @Length(min = 6, max = 20, message = "密码长度需为 6-20")
    String password;
}
