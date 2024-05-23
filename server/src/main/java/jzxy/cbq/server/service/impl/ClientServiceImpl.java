package jzxy.cbq.server.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jzxy.cbq.server.entity.dto.Client;
import jzxy.cbq.server.entity.dto.ClientDetail;
import jzxy.cbq.server.entity.dto.ClientSsh;
import jzxy.cbq.server.entity.vo.request.*;
import jzxy.cbq.server.entity.vo.response.*;
import jzxy.cbq.server.mapper.ClientDetailMapper;
import jzxy.cbq.server.mapper.ClientMapper;
import jzxy.cbq.server.mapper.ClientSshMapper;
import jzxy.cbq.server.service.ClientService;
import jzxy.cbq.server.utils.InfluxDbUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端服务实现类，提供客户端相关操作的实现
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Service
public class ClientServiceImpl extends ServiceImpl<ClientMapper, Client> implements ClientService {
    private String registerToken = this.generateNewToken();
    private final Map<Integer, Client> clientIdCache = new ConcurrentHashMap<>();
    private final Map<String, Client> clientTokenCache = new ConcurrentHashMap<>();
    private final Map<Integer, RuntimeDetailVO> currentRuntime = new ConcurrentHashMap<>();

    @Resource
    ClientDetailMapper detailMapper;
    @Resource
    InfluxDbUtils influx;
    @Resource
    ClientSshMapper sshMapper;

    /**
     * 初始化客户端缓存。
     */
    @PostConstruct
    public void initClientCache() {
        clientTokenCache.clear();
        clientIdCache.clear();
        this.list().forEach(this::addClientCache);
    }

    /**
     * 获取注册令牌。
     *
     * @return 注册令牌字符串。
     */
    @Override
    public String registerToken() {
        return registerToken;
    }

    /**
     * 根据客户端 ID 查找客户端信息。
     *
     * @param id 客户端 ID。
     * @return 客户端信息对象。
     */
    @Override
    public Client findClientById(int id) {
        return clientIdCache.get(id);
    }

    /**
     * 根据令牌查找客户端信息。
     *
     * @param token 客户端令牌。
     * @return 客户端信息对象。
     */
    @Override
    public Client findClientByToken(String token) {
        return clientTokenCache.get(token);
    }

    /**
     * 验证令牌并注册客户端。
     *
     * @param token 注册令牌。
     * @return 注册成功返回 true，失败返回 false。
     */
    @Override
    public boolean verifyAndRegister(String token) {
        if (this.registerToken.equals(token)) {
            int id = this.randomClientId();
            Client client = new Client(id, "未命名主机", token, "cn", "未命名节点", new Date());
            if (this.save(client)) {
                registerToken = this.generateNewToken();
                this.addClientCache(client);
                return true;
            }
        }
        return false;
    }

    /**
     * 更新客户端详情。
     *
     * @param vo 请求视图对象。
     * @param client 客户端信息对象。
     */
    @Override
    public void updateClientDetail(ClientDetailVO vo, Client client) {
        ClientDetail detail = new ClientDetail();
        BeanUtils.copyProperties(vo, detail);
        detail.setId(client.getId());
        if(Objects.nonNull(detailMapper.selectById(client.getId()))) {
            detailMapper.updateById(detail);
        } else {
            detailMapper.insert(detail);
        }
    }

    /**
     * 更新客户端运行时详情。
     *
     * @param vo 运行时详情视图对象。
     * @param client 客户端信息对象。
     */
    @Override
    public void updateRuntimeDetail(RuntimeDetailVO vo, Client client) {
        currentRuntime.put(client.getId(), vo);
        influx.writeRuntimeData(client.getId(), vo);
    }

    /**
     * 列出所有客户端信息。
     *
     * @return 客户端预览信息列表。
     */
    @Override
    public List<ClientPreviewVO> listClients() {
        return clientIdCache.values().stream().map(client -> {
            ClientPreviewVO vo = client.asViewObject(ClientPreviewVO.class);
            BeanUtils.copyProperties(detailMapper.selectById(vo.getId()), vo);
            RuntimeDetailVO runtime = currentRuntime.get(client.getId());
            if(this.isOnline(runtime)) {
                BeanUtils.copyProperties(runtime, vo);
                vo.setOnline(true);
            }
            return vo;
        }).toList();
    }

    /**
     * 获取客户端简略列表。
     *
     * @return 客户端简略信息列表。
     */
    @Override
    public List<ClientSimpleVO> listSimpleList() {
        return clientIdCache.values().stream().map(client -> {
            ClientSimpleVO vo = client.asViewObject(ClientSimpleVO.class);
            BeanUtils.copyProperties(detailMapper.selectById(vo.getId()), vo);
            return vo;
        }).toList();
    }

    /**
     * 重命名客户端。
     *
     * @param vo 更名视图对象。
     */
    @Override
    public void renameClient(RenameClientVO vo) {
        this.update(Wrappers.<Client>update().eq("id", vo.getId()).set("name", vo.getName()));
        this.initClientCache();
    }

    /**
     * 重命名节点。
     *
     * @param vo 更名视图对象。
     */
    @Override
    public void renameNode(RenameNodeVO vo) {
        this.update(Wrappers.<Client>update().eq("id", vo.getId())
                .set("node", vo.getNode()).set("location", vo.getLocation()));
        this.initClientCache();
    }

    /**
     * 获取客户端详细信息。
     *
     * @param clientId 客户端 ID。
     * @return 客户端详细信息视图对象。
     */
    @Override
    public ClientDetailsVO clientDetails(int clientId) {
        ClientDetailsVO vo = this.clientIdCache.get(clientId).asViewObject(ClientDetailsVO.class);
        BeanUtils.copyProperties(detailMapper.selectById(clientId), vo);
        vo.setOnline(this.isOnline(currentRuntime.get(clientId)));
        return vo;
    }

    /**
     * 获取客户端运行时历史详情。
     *
     * @param clientId 客户端 ID。
     * @return 客户端运行时历史详情视图对象。
     */
    @Override
    public RuntimeHistoryVO clientRuntimeDetailsHistory(int clientId) {
        RuntimeHistoryVO vo = influx.readRuntimeData(clientId);
        ClientDetail detail = detailMapper.selectById(clientId);
        BeanUtils.copyProperties(detail, vo);
        return vo;
    }

    /**
     * 获取当前客户端运行时详情。
     *
     * @param clientId 客户端 ID。
     * @return 客户端当前运行时详情视图对象。
     */
    @Override
    public RuntimeDetailVO clientRuntimeDetailsNow(int clientId) {
        return currentRuntime.get(clientId);
    }

    /**
     * 删除客户端。
     *
     * @param clientId 客户端 ID。
     */
    @Override
    public void deleteClient(int clientId) {
        this.removeById(clientId);
        detailMapper.deleteById(clientId);
        this.initClientCache();
        currentRuntime.remove(clientId);
    }

    /**
     * 保存客户端 SSH 连接信息。
     *
     * @param vo SSH 连接视图对象。
     */
    @Override
    public void saveClientSshConnection(SshConnectionVO vo) {
        Client client = clientIdCache.get(vo.getId());
        if(client == null) return;
        ClientSsh ssh = new ClientSsh();
        BeanUtils.copyProperties(vo, ssh);
        if(Objects.nonNull(sshMapper.selectById(client.getId()))) {
            sshMapper.updateById(ssh);
        } else {
            sshMapper.insert(ssh);
        }
    }

    /**
     * 获取 SSH 设置信息。
     *
     * @param clientId 客户端 ID。
     * @return SSH 设置视图对象。
     */
    @Override
    public SshSettingsVO sshSettings(int clientId) {
        ClientDetail detail = detailMapper.selectById(clientId);
        ClientSsh ssh = sshMapper.selectById(clientId);
        SshSettingsVO vo;
        if(ssh == null) {
            vo = new SshSettingsVO();
        } else {
            vo = ssh.asViewObject(SshSettingsVO.class);
        }
        vo.setIp(detail.getIp());
        return vo;
    }

    /**
     * 判断客户端是否在线。
     *
     * @param runtime 运行时详情对象。
     * @return 在线返回 true，离线返回 false。
     */
    private boolean isOnline(RuntimeDetailVO runtime) {
        return runtime != null && System.currentTimeMillis() - runtime.getTimestamp() < 60 * 1000;
    }

    /**
     * 添加客户端信息到缓存。
     *
     * @param client 客户端信息对象。
     */
    private void addClientCache(Client client) {
        clientIdCache.put(client.getId(), client);
        clientTokenCache.put(client.getToken(), client);
    }

    /**
     * 生成随机客户端 ID。
     *
     * @return 随机生成的客户端 ID。
     */
    private int randomClientId() {
        return new Random().nextInt(90000000) + 10000000;
    }

    /**
     * 生成新的注册令牌。
     *
     * @return 生成的注册令牌字符串。
     */
    private String generateNewToken() {
        String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(24);
        for (int i = 0; i < 24; i++)
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        return sb.toString();
    }
}