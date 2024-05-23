package jzxy.cbq.server.entity.vo.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RenameNodeVO {
    @Parameter(name = "id", description = "节点 id", in = ParameterIn.PATH, required = true)
    int id;
    @Length(min = 1, max = 10, message = "节点名称长度 [1,10]")
    String node;
    @Pattern(regexp = "(cn|hk|jp|us|sg|kr|de)")
    @Parameter(name = "location", description = "节点位置", in = ParameterIn.PATH, required = true)
    String location;
}
