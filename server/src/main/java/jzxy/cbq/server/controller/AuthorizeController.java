package jzxy.cbq.server.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jzxy.cbq.server.entity.RestBean;
import jzxy.cbq.server.entity.vo.request.EmailRegisterVO;
import jzxy.cbq.server.entity.vo.request.ConfirmResetVO;
import jzxy.cbq.server.entity.vo.request.EmailResetVO;
import jzxy.cbq.server.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

/**
 * 用于验证相关 Controller 包含用户的注册、重置密码等操作
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name = "登录校验相关", description = "包括用户登录、注册、验证码请求等接口")
public class AuthorizeController {

    @Resource
    AccountService accountService;

    /**
     * 请求邮件验证码
     *
     * @param email   请求邮件
     * @param type    类型
     * @param request 请求
     * @return 是否请求成功
     */
    @Operation(summary = "请求邮件验证码",
            description = "发送邮件验证码给指定邮箱，用于注册、密码重置等操作",
            tags = {"登录校验相关"})
    @ApiResponse(responseCode = "200", description = "请求成功",
            content = @Content(schema = @Schema(implementation = RestBean.class)))
    @ApiResponse(responseCode = "400", description = "参数错误或请求失败",
            content = @Content(schema = @Schema(implementation = RestBean.class)))
    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@Parameter(description = "请求邮件", required = true, example = "2024cbq@gmail.com") @RequestParam @Email String email,
                                        @Parameter(description = "类型（reset|modify|register）", required = true, example = "register") @RequestParam @Pattern(regexp = "(reset|modify|register)") String type,
                                        HttpServletRequest request) {
        return this.messageHandle(() ->
                accountService.registerEmailVerifyCode(type, String.valueOf(email), request.getRemoteAddr()));
    }

    /**
     * 进行用户注册操作，需要先请求邮件验证码
     *
     * @param vo 注册信息
     * @return 是否注册成功
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册操作",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = EmailRegisterVO.class)))
    )
    public RestBean<Void> register(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "邮箱注册 vo") @RequestBody @Valid EmailRegisterVO vo) {
        return this.messageHandle(() ->
                accountService.registerEmailAccount(vo));
    }

    /**
     * 执行密码重置确认，检查验证码是否正确
     *
     * @param vo 密码重置信息
     * @return 是否操作成功
     */
    @Operation(summary = "密码重置确认",
            description = "验证邮箱、验证码是否匹配，确认是否允许进行密码重置",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = ConfirmResetVO.class))),
            tags = {"登录校验相关"})
    @ApiResponse(responseCode = "200", description = "确认成功",
            content = @Content(schema = @Schema(implementation = RestBean.class)))
    @ApiResponse(responseCode = "400", description = "确认失败（参数错误或验证码不匹配）",
            content = @Content(schema = @Schema(implementation = RestBean.class)))
    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "确认重置 vo") @RequestBody @Valid ConfirmResetVO vo) {
        return this.messageHandle(() -> accountService.resetConfirm(vo));
    }

    /**
     * 执行密码重置操作
     *
     * @param vo 密码重置信息
     * @return 是否操作成功
     */
    @Operation(summary = "密码重置操作",
            description = "使用邮箱和新密码重置用户账户密码",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = EmailResetVO.class))),
            tags = {"登录校验相关"})
    @ApiResponse(responseCode = "200", description = "重置成功",
            content = @Content(schema = @Schema(implementation = RestBean.class)))
    @ApiResponse(responseCode = "400", description = "重置失败（参数错误或邮箱未找到）",
            content = @Content(schema = @Schema(implementation = RestBean.class)))
    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "重置邮箱 vo") @RequestBody @Valid EmailResetVO vo) {
        return this.messageHandle(() ->
                accountService.resetEmailAccountPassword(vo));
    }

    /**
     * 针对于返回值为 String 作为错误信息的方法进行统一处理
     *
     * @param action 具体操作
     * @param <T>    响应结果类型
     * @return 响应结果
     */
    private <T> RestBean<T> messageHandle(Supplier<String> action) {
        String message = action.get();
        if (message == null)
            return RestBean.success();
        else
            return RestBean.failure(400, message);
    }
}
