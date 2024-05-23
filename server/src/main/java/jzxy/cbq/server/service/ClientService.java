
package jzxy.cbq.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jzxy.cbq.server.entity.dto.Client;
import jzxy.cbq.server.entity.vo.request.*;
import jzxy.cbq.server.entity.vo.response.*;

import java.util.List;

/**
 * <p>ClientService 接口</p>
 * <p>
 * 提供客户端相关的核心服务功能，包括但不限于客户端注册、查询、更新、删除等操作。
 * </p>
 *
 * @version 1.0.0
 * @author cbq
 * @since 2024/3/27 14:08
 */
public interface ClientService extends IService<Client> {

    /**
     * <p>注册并返回一个新的客户端令牌。</p>
     *
     * @return 新注册的令牌字符串
     */
    String registerToken();

    /**
     * <p>根据给定的客户端ID查找对应的客户端实体信息。</p>
     *
     * @param id 客户端ID
     * @return 客户端实体对象，如果找不到则可能返回 null
     */
    Client findClientById(int id);

    /**
     * <p>根据客户端令牌查找对应的客户端实体信息。</p>
     *
     * @param token 客户端令牌
     * @return 客户端实体对象，如果找不到则可能返回 null
     */
    Client findClientByToken(String token);

    /**
     * <p>验证并注册客户端，根据令牌有效性执行注册操作。</p>
     *
     * @param token 待验证与注册的令牌
     * @return 如果令牌有效且注册成功，返回 true；否则返回 false
     */
    boolean verifyAndRegister(String token);

    /**
     * <p>更新客户端的详细信息。</p>
     *
     * @param vo 包含客户端更新细节的请求视图对象
     * @param client 已存在的客户端实体，用于进行更新操作
     */
    void updateClientDetail(ClientDetailVO vo, Client client);

    /**
     * <p>更新客户端的运行时详细信息。</p>
     *
     * @param vo 包含运行时详情更新数据的请求视图对象
     * @param client 需要更新其运行时信息的客户端实体
     */
    void updateRuntimeDetail(RuntimeDetailVO vo, Client client);

    /**
     * <p>获取所有客户端的预览信息列表。</p>
     *
     * @return 客户端预览信息列表
     */
    List<ClientPreviewVO> listClients();

    /**
     * <p>获取简化版的客户端列表信息。</p>
     *
     * @return 简化版客户端信息列表
     */
    List<ClientSimpleVO> listSimpleList();

    /**
     * <p>根据提供的重命名参数，更改客户端的名称。</p>
     *
     * @param vo 包含新名称的重命名客户端请求视图对象
     */
    void renameClient(RenameClientVO vo);

    /**
     * <p>根据提供的重命名参数，更改节点名称。</p>
     *
     * @param vo 包含新名称的重命名节点请求视图对象
     */
    void renameNode(RenameNodeVO vo);

    /**
     * <p>获取指定客户端 ID 的详细信息视图对象。</p>
     *
     * @param clientId 客户端 ID
     * @return 客户端详细信息视图对象
     */
    ClientDetailsVO clientDetails(int clientId);

    /**
     * <p>获取指定客户端 ID 的历史运行时详情记录。</p>
     *
     * @param clientId 客户端 ID
     * @return 运行时详情历史记录视图对象
     */
    RuntimeHistoryVO clientRuntimeDetailsHistory(int clientId);

    /**
     * <p>获取指定客户端 ID 当前的运行时详情。</p>
     *
     * @param clientId 客户端 ID
     * @return 当前运行时详情视图对象
     */
    RuntimeDetailVO clientRuntimeDetailsNow(int clientId);

    /**
     * <p>删除指定 ID 的客户端及其关联信息。</p>
     *
     * @param clientId 要删除的客户端 ID
     */
    void deleteClient(int clientId);

    /**
     * <p>保存或更新客户端的 SSH 连接信息。</p>
     *
     * @param vo 包含 SSH 连接信息的视图对象
     */
    void saveClientSshConnection(SshConnectionVO vo);

    /**
     * <p>获取指定客户端 ID 的 SSH 设置信息。</p>
     *
     * @param clientId 客户端 ID
     * @return SSH 设置信息视图对象
     */
    SshSettingsVO sshSettings(int clientId);
}