package jzxy.cbq.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jzxy.cbq.server.entity.RestBean;
import jzxy.cbq.server.entity.dto.Account;
import jzxy.cbq.server.entity.vo.request.RenameClientVO;
import jzxy.cbq.server.entity.vo.request.RenameNodeVO;
import jzxy.cbq.server.entity.vo.request.RuntimeDetailVO;
import jzxy.cbq.server.entity.vo.request.SshConnectionVO;
import jzxy.cbq.server.entity.vo.response.*;
import jzxy.cbq.server.service.AccountService;
import jzxy.cbq.server.service.ClientService;
import jzxy.cbq.server.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监控控制器，提供客户端管理相关的API接口
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@RestController
@RequestMapping("/api/monitor")
@Tag(name = "服务端监控相关", description = "包括获取所有客户端信息、重命名客户端、重命名节点等接口")
public class MonitorController {

    @Resource
    ClientService service;

    @Resource
    AccountService accountService;

    /**
     * 获取所有客户端信息列表
     *
     * @param userId   用户 ID
     * @param userRole 用户角色
     * @return 返回客户端信息列表的 RestBean 对象
     */
    @Operation(summary = "获取所有客户端信息列表",
            description = "Retrieve a list of all client preview information based on the user's role and access permissions.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved client list",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientPreviewVO.class)))),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
            })
    @GetMapping("/list")
    public RestBean<List<ClientPreviewVO>> listAllClient(@Parameter(description = "User ID", required = true) @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                                         @Parameter(description = "User Role", required = true) @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        List<ClientPreviewVO> clients = service.listClients();
        if (this.isAdminAccount(userRole)) {
            return RestBean.success(clients);
        } else {
            List<Integer> ids = this.accountAccessClients(userId);
            return RestBean.success(clients.stream()
                    .filter(vo -> ids.contains(vo.getId()))
                    .toList());
        }
    }

    /**
     * 获取简化版客户端列表
     *
     * @param userRole 用户角色
     * @return 如果用户是管理员，返回简化版客户端列表的 RestBean 对象；否则返回无权限信息
     */
    @Operation(summary = "获取简化版客户端列表",
            description = "Retrieve a list of simplified client information if the user is an administrator; otherwise, return a no-permission response.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved simplified client list",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientSimpleVO.class)))),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
            })
    @GetMapping("/simple-list")
    public RestBean<List<ClientSimpleVO>> simpleClientList(@Parameter(description = "User Role", required = true) @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.isAdminAccount(userRole)) {
            return RestBean.success(service.listSimpleList());
        } else {
            return RestBean.noPermission();
        }
    }

    /**
     * 重命名客户端
     *
     * @param vo       提交的重命名请求数据
     * @param userId   用户 ID
     * @param userRole 用户角色
     * @return 成功则返回成功信息的 RestBean 对象，否则返回无权限信息
     */
    @Operation(summary = "重命名客户端",
            description = "Change the name of a client based on the provided request data, user ID, and role. Returns a success response or a no-permission response.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = RenameClientVO.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Client renamed successfully"),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
            })
    @PostMapping("/rename")
    public RestBean<Void> renameClient(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Rename request data") @RequestBody @Valid RenameClientVO vo,
                                       @Parameter(description = "User ID", required = true) @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                       @Parameter(description = "User Role", required = true) @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, vo.getId())) {
            service.renameClient(vo);
            return RestBean.success();
        } else {
            return RestBean.noPermission();
        }
    }

    /**
     * 重命名节点
     *
     * @param vo       提交的重命名节点请求数据
     * @param userId   用户 ID
     * @param userRole 用户角色
     * @return 成功则返回成功信息的RestBean对象，否则返回无权限信息
     */
    @Operation(summary = "重命名节点",
            description = "Change the name of a node based on the provided request data, user ID, and role. Returns a success response or a no-permission response.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = RenameNodeVO.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Node renamed successfully"),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
            })
    @PostMapping("/node")
    public RestBean<Void> renameNode(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Rename request data") @RequestBody @Valid RenameNodeVO vo,
                                     @Parameter(description = "User ID", required = true) @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                     @Parameter(description = "User Role", required = true) @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, vo.getId())) {
            service.renameNode(vo);
            return RestBean.success();
        } else {
            return RestBean.noPermission();
        }
    }

    /**
     * 获取客户端详细信息
     *
     * @param clientId 客户端 ID
     * @param userId   用户 ID
     * @param userRole 用户角色
     * @return 成功则返回客户端详细信息的 RestBean 对象，否则返回无权限信息
     */
    @Operation(summary = "获取客户端详细信息",
            description = "Retrieve detailed information about a client based on the client ID, user ID, and role. Returns a client details object or a no-permission response.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved client details",
                            content = @Content(schema = @Schema(implementation = ClientDetailsVO.class))),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
            })
    @GetMapping("/details")
    public RestBean<ClientDetailsVO> details(@Parameter(description = "Client ID", required = true) int clientId,
                                             @Parameter(description = "User ID", required = true) @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                             @Parameter(description = "User Role", required = true) @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, clientId)) {
            return RestBean.success(service.clientDetails(clientId));
        } else {
            return RestBean.noPermission();
        }
    }

    /**
     * 获取客户端运行历史详情
     *
     * @param clientId 客户端 ID
     * @param userId   用户 ID
     * @param userRole 用户角色
     * @return 成功则返回客户端运行历史详情的 RestBean 对象，否则返回无权限信息
     */
    @Operation(summary = "获取客户端运行历史详情",
            description = "Retrieve historical runtime details for a client based on the client ID, user ID, and role. Returns a runtime history object or a no-permission response.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved client runtime history",
                            content = @Content(schema = @Schema(implementation = RuntimeHistoryVO.class))),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
            })
    @GetMapping("/runtime-history")
    public RestBean<RuntimeHistoryVO> runtimeDetailsHistory(@Parameter(description = "Client ID", required = true) int clientId,
                                                            @Parameter(description = "User ID", required = true) @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                                            @Parameter(description = "User Role", required = true) @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, clientId)) {
            return RestBean.success(service.clientRuntimeDetailsHistory(clientId));
        } else {
            return RestBean.noPermission();
        }
    }

    /**
     * 获取客户端当前运行详情
     *
     * @param clientId 客户端 ID
     * @param userId   用户 ID
     * @param userRole 用户角色
     * @return 成功则返回客户端当前运行详情的 RestBean 对象，否则返回无权限信息
     */
    @Operation(summary = "获取客户端当前运行详情",
            description = "Retrieve current runtime details for a client based on the client ID, user ID, and role. Returns a runtime detail object or a no-permission response.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved client current runtime details",
                            content = @Content(schema = @Schema(implementation = RuntimeDetailVO.class))),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
            })
    @GetMapping("/runtime-now")
    public RestBean<RuntimeDetailVO> runtimeDetailsNow(@Parameter(description = "Client ID", required = true) int clientId,
                                                       @Parameter(description = "User ID", required = true) @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                                       @Parameter(description = "User Role", required = true) @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, clientId)) {
            return RestBean.success(service.clientRuntimeDetailsNow(clientId));
        } else {
            return RestBean.noPermission();
        }
    }

    /**
     * 注册客户端 Token
     *
     * @param userRole 用户角色
     * @return 成功则返回成功信息的 RestBean 对象，否则返回无权限信息
     */
    @Operation(summary = "注册客户端 Token",
            description = "Register a token for a client if the user is an administrator. Returns a success response or a no-permission response.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token registered successfully"),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
            })
    @GetMapping("/register")
    public RestBean<String> registerToken(@Parameter(description = "User Role", required = true) @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.isAdminAccount(userRole)) {
            return RestBean.success(service.registerToken());
        } else {
            return RestBean.noPermission();
        }
    }

    /**
     * 删除客户端
     *
     * @param clientId 客户端 ID
     * @param userRole 用户角色
     * @return 成功则返回成功信息的 RestBean 对象，否则返回无权限信息
     */
    @Operation(summary = "删除客户端",
            description = "Delete a client based on the client ID and user role. Returns a success response or a no-permission response.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Client deleted successfully"),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
            })
    @GetMapping("/delete")
    public RestBean<String> deleteClient(@Parameter(description = "Client ID", required = true) int clientId,
                                         @Parameter(description = "User Role", required = true) @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.isAdminAccount(userRole)) {
            service.deleteClient(clientId);
            return RestBean.success();
        } else {
            return RestBean.noPermission();
        }
    }

    /**
     * 保存 SSH 连接信息
     *
     * @param vo       提交的 SSH 连接信息
     * @param userId   用户 ID
     * @param userRole 用户角色
     * @return 成功则返回成功信息的 RestBean 对象，否则返回无权限信息
     */
    @Operation(summary = "保存 SSH 连接信息",
            description = "Update or create an SSH connection for a specific client based on the user's role and access permissions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSH connection saved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "400", description = "Invalid SSH connection data provided")
    })
    @PostMapping("/ssh-save")
    public RestBean<Void> saveSshConnection(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "SSH connection details to be saved", required = true, content = @Content(
            schema = @Schema(implementation = SshConnectionVO.class)))
                                            @RequestBody @Valid SshConnectionVO vo,
                                            @Parameter(description = "User ID", required = true) @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                            @Parameter(description = "User Role", required = true) @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, vo.getId())) {
            service.saveClientSshConnection(vo);
            return RestBean.success();
        } else {
            return RestBean.noPermission();
        }
    }

    /**
     * 获取 SSH 设置信息
     *
     * @param clientId 客户端 ID
     * @param userId   用户 ID
     * @param userRole 用户角色
     * @return 成功则返回 SSH 设置信息的 RestBean 对象，否则返回无权限信息
     */
    @Operation(summary = "获取 SSH 设置信息",
            description = "Retrieve SSH settings for a specific client based on the user's role and access permissions.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved SSH settings",
                            content = @Content(schema = @Schema(implementation = SshSettingsVO.class))),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
            })
    @GetMapping("/ssh")
    public RestBean<SshSettingsVO> sshSettings(@Parameter(description = "Client ID", required = true) int clientId,
                                               @Parameter(description = "User ID", required = true) @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                               @Parameter(description = "User Role", required = true) @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, clientId)) {
            return RestBean.success(service.sshSettings(clientId));
        } else {
            return RestBean.noPermission();
        }
    }

    private List<Integer> accountAccessClients(int uid) {
        Account account = accountService.getById(uid);
        return account.getClientList();
    }

    private boolean isAdminAccount(String role) {
        role = role.substring(5);
        return Const.ROLE_ADMIN.equals(role);
    }

    private boolean permissionCheck(int uid, String role, int clientId) {
        if (this.isAdminAccount(role)) return true;
        return this.accountAccessClients(uid).contains(clientId);
    }
}