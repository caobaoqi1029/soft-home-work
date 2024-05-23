package jzxy.cbq.server.entity.dto;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jzxy.cbq.server.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 数据库中的用户信息
 */
@Data
@AllArgsConstructor
public class Account implements BaseData {
    @TableId(type = IdType.AUTO)
    Integer id;
    String username;
    String password;
    String email;
    String role;
    String avatar;
    Date registerTime;
    String clients;

    public List<Integer> getClientList() {
        if (clients == null) return Collections.emptyList();
        return JSONArray.parse(clients).toList(Integer.class);
    }
}
