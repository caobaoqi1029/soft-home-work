package jzxy.cbq.server.entity.vo.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RenameClientVO {
    @Parameter(name = "id", description = "客户端 id", in = ParameterIn.PATH, required = true)
    @NotNull(message = "客户端 id 不能为空")
    int id;

    @Parameter(name = "name", description = "客户端名称", in = ParameterIn.PATH, required = true)
    @Length(min = 1, max = 10, message = "客户端名称 [1,10]")
    String name;
}
