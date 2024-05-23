package jzxy.cbq.server.entity.vo.response;

import com.alibaba.fastjson2.JSONArray;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class SubAccountVO {
    @Parameter(description = "子账户ID", required = true)
    private int id;

    @Parameter(description = "子账户用户名", required = true)
    private String username;

    @Parameter(description = "子账户邮箱", required = true)
    private String email;

    @Parameter(description = "子账户关联的客户端列表", required = true)
    JSONArray clientList;
}
