package jzxy.cbq.server.entity.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jzxy.cbq.server.entity.BaseData;
import lombok.Data;

@Data
public class ClientSsh implements BaseData {
    @TableId
    Integer id;
    Integer port;
    String username;
    String password;
}
