
package jzxy.cbq.monitorclient.entity;

import com.alibaba.fastjson2.JSONObject;

/**
 * 响应实体类，用于封装接口响应信息。
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
public record Response(int id, int code, Object data, String message) {
    /**
     * 判断响应是否成功。
     *
     * @return boolean 返回 true 表示成功，false 表示失败
     */
    public boolean success() {
        return code == 200;
    }

    /**
     * 将响应数据转换为 JSON 对象。
     *
     * @return JSONObject 响应数据的 JSON 对象。
     */
    public JSONObject asJson() {
        return JSONObject.from(data);
    }

    /**
     * 将响应数据转换为字符串。
     *
     * @return String 响应数据的字符串形式。
     */
    public String asString() {
        return data.toString();
    }

    /**
     * 创建一个错误响应实体。
     *
     * @param e 异常对象，用于生成错误响应。
     * @return Response 错误响应实体。
     */
    public static Response errorResponse(Exception e) {
        return new Response(0, 500, null, e.getMessage());
    }
}