package jzxy.cbq.server.entity.vo.response;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class RuntimeHistoryVO {

    @Parameter(description = "磁盘使用情况（单位：GB）", required = true)
    private double disk;

    @Parameter(description = "内存使用情况（单位：GB）", required = true)
    private double memory;

    @Parameter(description = "运行时历史数据列表", required = true)
    List<JSONObject> list = new LinkedList<>();}