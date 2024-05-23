package jzxy.cbq.server.entity.vo.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class ChangePasswordVO {
    @Parameter(name = "password", description = "密码", in = ParameterIn.PATH, required = true)
    @Length(min = 6, max = 20, message = "密码长度 [6,20]")
    String password;
    @Parameter(name = "password", description = "密码", in = ParameterIn.PATH, required = true)
    @Length(min = 6, max = 20, message = "密码长度 [6,20]")
    String new_password;
}
