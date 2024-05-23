package jzxy.cbq.server.entity.vo.response;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class ClientSimpleVO {
    @Parameter(description = "客户端ID", required = true)
    private int id;

    @Parameter(description = "客户端名称", required = true)
    private String name;

    @Parameter(description = "地理位置", required = true)
    private String location;

    @Parameter(description = "操作系统名称", required = true)
    private String osName;

    @Parameter(description = "操作系统版本", required = true)
    private String osVersion;

    @Parameter(description = "IP地址", required = true)
    private String ip;
}
