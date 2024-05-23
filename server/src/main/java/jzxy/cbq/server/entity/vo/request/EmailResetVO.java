package jzxy.cbq.server.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 密码重置表单实体
 */
@Data
public class EmailResetVO {
    @Email(message = "请输入有效的邮箱地址")
    String email;
    @Length(max = 6, min = 6, message = "验证码长度需为 6")
    String code;
    @Length(min = 6, max = 20,message = "密码长度 [6,20]")
    String password;
}
