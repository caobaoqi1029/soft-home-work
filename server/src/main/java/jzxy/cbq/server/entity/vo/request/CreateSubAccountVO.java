package jzxy.cbq.server.entity.vo.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
public class CreateSubAccountVO {
    @Parameter(name = "username", description = "用户名", in = ParameterIn.PATH, required = true)
    @Length(min = 1, max = 10, message = "用户名长度 [1,10]")
    String username;
    @Parameter(name = "email", description = "邮箱地址", in = ParameterIn.PATH, required = true)
    @Email(message = "请输入有效的邮箱地址")
    String email;
    @Parameter(name = "password", description = "密码", in = ParameterIn.PATH, required = true)
    @Length(min = 6, max = 20, message = "密码长度 [6,20]")
    String password;
    @Parameter(name = "clients", description = "关联的客户端列表", in = ParameterIn.PATH, required = true)
    @Size(min = 1, message = "至少选择一个关联客户端")
    List<Integer> clients;
}
