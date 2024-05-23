package jzxy.cbq.server.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmResetVO {
    @Email(message = "请输入有效的邮箱地址")
    String email;
    @Length(max = 6, min = 6,message = "验证码长度需为 6")
    String code;
}
