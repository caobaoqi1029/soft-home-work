package jzxy.cbq.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jzxy.cbq.server.entity.RestBean;
import jzxy.cbq.server.entity.dto.Client;
import jzxy.cbq.server.entity.vo.request.ClientDetailVO;
import jzxy.cbq.server.entity.vo.request.RuntimeDetailVO;
import jzxy.cbq.server.service.ClientService;
import jzxy.cbq.server.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 客户端控制器
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Tag(name = "客户端相关", description = "包括客户端注册、更新客户端详细、运行时信息等接口")
@RestController
@RequestMapping("/monitor")
public class ClientController {
    @Resource
    ClientService service;

    /**
     * 客户端注册接口
     * 使用客户端传来的 Token 进行验证并注册客户端信息
     *
     * @param token 客户端认证 Token
     * @return 注册成功返回成功信息，失败返回错误信息
     */
    @Operation(summary = "客户端注册", description = "使用客户端提供的 Token 进行验证并注册客户端信息")
    @ApiResponse(responseCode = "200", description = "注册成功")
    @ApiResponse(responseCode = "401", description = "客户端注册失败，Token 错误或无效",
            content = @Content(schema = @Schema(example = "{\n  \"code\": 401,\n  \"msg\": \"客户端注册失败，请检查 Token 是否正确\"\n}")))
    @GetMapping("/register")
    public RestBean<Void> registerClient(
            @Parameter(name = "Authorization", description = "客户端认证 Token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String token) {
        return service.verifyAndRegister(token) ?
                RestBean.success() : RestBean.failure(401, "客户端注册失败，请检查 Token 是否正确");
    }

    /**
     * 更新客户端详细信息
     * 接收客户端传来的详细信息并更新至服务器端
     *
     * @param client 当前请求的客户端信息
     * @param vo     客户端更新后的详细信息
     * @return 更新成功返回成功信息
     */
    @Operation(summary = "更新客户端详细信息", description = "接收客户端传来的详细信息并更新至服务器端")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PostMapping("/detail")
    public RestBean<Void> updateClientDetails(
            @Parameter(hidden = true)
            @RequestAttribute(Const.ATTR_CLIENT) Client client,
            @Parameter(description = "客户端更新后的详细信息", required = true)
            @RequestBody @Valid ClientDetailVO vo) {
        service.updateClientDetail(vo, client);
        return RestBean.success();
    }

    /**
     * 更新客户端运行时详细信息
     * 接收客户端传来的运行时详细信息并更新至服务器端
     *
     * @param client 当前请求的客户端信息
     * @param vo     客户端更新后的运行时详细信息
     * @return 更新成功返回成功信息
     */
    @Operation(summary = "更新客户端运行时详细信息", description = "接收客户端传来的运行时详细信息并更新至服务器端")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PostMapping("/runtime")
    public RestBean<Void> updateRuntimeDetails(
            @Parameter(hidden = true) @RequestAttribute(Const.ATTR_CLIENT) Client client,
            @Parameter(description = "客户端更新后的运行时详细信息", required = true)
            @RequestBody @Valid RuntimeDetailVO vo) {
        service.updateRuntimeDetail(vo, client);
        return RestBean.success();
    }
}