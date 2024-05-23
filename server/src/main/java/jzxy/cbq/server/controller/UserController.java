package jzxy.cbq.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jzxy.cbq.server.entity.RestBean;
import jzxy.cbq.server.entity.vo.request.ChangePasswordVO;
import jzxy.cbq.server.entity.vo.request.CreateSubAccountVO;
import jzxy.cbq.server.entity.vo.request.ModifyEmailVO;
import jzxy.cbq.server.entity.vo.response.SubAccountVO;
import jzxy.cbq.server.service.AccountService;
import jzxy.cbq.server.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器类，负责处理用户相关的 API 请求
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Tag(name = "子用户相关", description = "包括创建、删除、获取子账号以及修改密码、邮箱等接口")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    AccountService service;

    /**
     * 修改密码接口
     *
     * @param vo     包含旧密码和新密码的对象
     * @param userId 当前用户 ID
     * @return 操作结果
     */
    @Operation(summary = "修改密码", description = "修改当前用户的密码")
    @ApiResponse(responseCode = "200", description = "密码修改成功")
    @ApiResponse(responseCode = "401", description = "原密码输入错误",
            content = @Content(schema = @Schema(example = "{\n  \"code\": 401,\n  \"msg\": \"原密码输入错误！\"\n}")))
    @PostMapping("/change-password")
    public RestBean<Void> changePassword(
            @Parameter(description = "包含旧密码和新密码的对象", required = true)
            @RequestBody @Valid ChangePasswordVO vo,
            @Parameter(description = "当前用户 ID", required = true)
            @RequestAttribute(Const.ATTR_USER_ID) int userId) {
        return service.changePassword(userId, vo.getPassword(), vo.getNew_password()) ?
                RestBean.success() : RestBean.failure(401, "原密码输入错误！");
    }

    /**
     * 修改邮箱接口
     *
     * @param id 用户 ID
     * @param vo 包含新邮箱地址的对象
     * @return 操作结果
     */
    @Operation(summary = "修改邮箱", description = "修改用户邮箱地址")
    @ApiResponse(responseCode = "200", description = "邮箱修改成功")
    @ApiResponse(responseCode = "401", description = "邮箱修改失败，原因见返回消息",
            content = @Content(schema = @Schema(example = "{\n  \"code\": 401,\n  \"msg\": \"邮箱已存在或格式不正确\"\n}")))
    @PostMapping("/modify-email")
    public RestBean<Void> modifyEmail(
            @Parameter(description = "用户 ID", required = true)
            @RequestAttribute(Const.ATTR_USER_ID) int id,
            @Parameter(description = "包含新邮箱地址的对象", required = true)
            @RequestBody @Valid ModifyEmailVO vo) {
        String result = service.modifyEmail(id, vo);
        if (result == null) {
            return RestBean.success();
        } else {
            return RestBean.failure(401, result);
        }
    }

    /**
     * 创建子账号接口
     *
     * @param vo 包含子账号信息的对象
     * @return 操作结果
     */
    @Operation(summary = "创建子账号", description = "为当前用户创建一个新的子账号")
    @ApiResponse(responseCode = "200", description = "子账号创建成功")
    @PostMapping("/sub/create")
    public RestBean<Void> createSubAccount(
            @Parameter(description = "包含子账号信息的对象", required = true)
            @RequestBody @Valid CreateSubAccountVO vo) {
        service.createSubAccount(vo);
        return RestBean.success();
    }

    /**
     * 删除子账号接口
     *
     * @param uid    要删除的子账号 ID
     * @param userId 当前操作的用户 ID
     * @return 操作结果
     */
    @Operation(summary = "删除子账号", description = "删除指定的子账号")
    @ApiResponse(responseCode = "200", description = "子账号删除成功")
    @GetMapping("/sub/delete")
    public RestBean<Void> deleteSubAccount(
            @Parameter(description = "要删除的子账号 ID", required = true)
            int uid,
            @Parameter(description = "当前操作的用户 ID", required = true)
            @RequestAttribute(Const.ATTR_USER_ID) int userId) {
        if (uid == userId) {
            return RestBean.failure(401, "非法参数");
        }
        service.deleteSubAccount(uid);
        return RestBean.success();
    }


    /**
     * 获取子账号列表接口
     *
     * @return 返回子账号列表的 RestBean
     */
    @Operation(summary = "获取子账号列表", description = "获取当前用户的子账号列表")
    @ApiResponse(responseCode = "200", description = "成功返回子账号列表",
            content = @Content(schema = @Schema(type = "array", implementation = SubAccountVO.class)))
    @GetMapping("/sub/list")
    public RestBean<List<SubAccountVO>> subAccountList() {
        return RestBean.success(service.listSubAccount());
    }
}