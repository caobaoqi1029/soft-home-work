package jzxy.cbq.server.entity.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jzxy.cbq.server.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Client implements BaseData {
    @TableId
    Integer id;
    String name;
    String token;
    String location;
    String node;
    Date registerTime;
}
