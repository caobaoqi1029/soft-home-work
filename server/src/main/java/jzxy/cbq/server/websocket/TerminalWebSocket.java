package jzxy.cbq.server.websocket;

import jzxy.cbq.server.entity.dto.ClientDetail;
import jzxy.cbq.server.entity.dto.ClientSsh;
import jzxy.cbq.server.mapper.ClientDetailMapper;
import jzxy.cbq.server.mapper.ClientSshMapper;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WebSocket 服务器端，用于处理终端连接和数据传输。
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Slf4j
@Component
@ServerEndpoint("/terminal/{clientId}")
public class TerminalWebSocket {
    private static ClientDetailMapper detailMapper;
    @Resource
    public void setDetailMapper(ClientDetailMapper detailMapper) {
        TerminalWebSocket.detailMapper = detailMapper;
    }
    private static ClientSshMapper sshMapper;
    @Resource
    public void setSshMapper(ClientSshMapper sshMapper) {
        TerminalWebSocket.sshMapper = sshMapper;
    }
    private static final Map<Session, Shell> sessionMap = new ConcurrentHashMap<>();
    private final ExecutorService service = Executors.newSingleThreadExecutor();

    /**
     * 当 WebSocket 连接打开时的处理逻辑。
     * 根据 clientId 查找对应的用户详细信息和 SSH 信息，尝试建立 SSH 连接。
     *
     * @param session WebSocket 会话
     * @param clientId 客户端 ID，用于查找用户和 SSH 配置信息
     * @throws Exception 可能抛出的异常包括 SSH 连接失败、查找用户或 SSH 信息失败等
     */
    @OnOpen
    public void onOpen(Session session,
                        @PathParam(value = "clientId") String clientId) throws Exception {
        ClientDetail detail = detailMapper.selectById(clientId);
        ClientSsh ssh = sshMapper.selectById(clientId);
        if(detail == null || ssh == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "无法识别此主机"));
            return;
        }
        if(this.createSshConnection(session, ssh, detail.getIp())) {
            log.info("主机 {} 的 SSH 连接已创建", detail.getIp());
        }
    }

    /**
     * 当 WebSocket 接收到消息时的处理逻辑。
     * 将接收到的消息写入到对应的 SSH 通道的输出流中。
     *
     * @param session WebSocket 会话
     * @param message 客户端发送的消息
     * @throws IOException 通信异常
     */
    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        Shell shell = sessionMap.get(session);
        OutputStream output = shell.output;
        output.write(message.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }

    /**
     * 当 WebSocket 连接关闭时的处理逻辑。
     * 关闭对应的 SSH 连接，从 sessionMap 中移除当前 Session。
     *
     * @param session WebSocket 会话
     * @throws IOException 通信异常
     */
    @OnClose
    public void onClose(Session session) throws IOException {
        Shell shell = sessionMap.get(session);
        if(shell != null) {
            shell.close();
            sessionMap.remove(session);
            log.info("主机 {} 的 SSH 连接已断开", shell.js.getHost());
        }
    }

    /**
     * 当 WebSocket 连接出现错误时的处理逻辑。
     * 记录错误日志，并关闭 WebSocket 连接。
     *
     * @param session WebSocket 会话
     * @param error 错误异常
     * @throws IOException 通信异常
     */
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        log.error("用户 WebSocket 连接出现错误", error);
        session.close();
    }

    /**
     * 创建并初始化 SSH 连接。
     * 根据提供的 SSH 信息尝试建立连接，成功则将 Shell 实例添加到 sessionMap 中。
     *
     * @param session WebSocket 会话
     * @param ssh 客户端 SSH 配置信息
     * @param ip SSH 服务器 IP 地址
     * @return 建立 SSH 连接成功返回 true，否则返回 false
     * @throws IOException 通信异常
     */
    private boolean createSshConnection(Session session, ClientSsh ssh, String ip) throws IOException{
        try {
            JSch jSch = new JSch();
            com.jcraft.jsch.Session js = jSch.getSession(ssh.getUsername(), ip, ssh.getPort());
            js.setPassword(ssh.getPassword());
            js.setConfig("StrictHostKeyChecking", "no");
            js.setTimeout(3000);
            js.connect();
            ChannelShell channel = (ChannelShell) js.openChannel("shell");
            channel.setPtyType("xterm");
            channel.connect(1000);
            sessionMap.put(session, new Shell(session, js, channel));
            return true;
        } catch (JSchException e) {
            String message = e.getMessage();
            if(message.equals("Auth fail")) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
                        "登录 SSH 失败，用户名或密码错误"));
                log.error("连接 SSH 失败，用户名或密码错误，登录失败");
            } else if(message.contains("Connection refused")) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
                        "连接被拒绝，可能是没有启动 SSH 服务或是放开端口"));
                log.error("连接SSH失败，连接被拒绝，可能是没有启动 SSH 服务或是放开端口");
            } else {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, message));
                log.error("连接 SSH 时出现错误", e);
            }
        }
        return false;
    }

    /**
     * Shell 类封装了 SSH 连接的会话、通道、输入输出流等信息。
     * 提供建立连接、读取输入流和关闭连接的方法。
     */
    private class Shell {
        private final Session session;
        private final com.jcraft.jsch.Session js;
        private final ChannelShell channel;
        private final InputStream input;
        private final OutputStream output;

        public Shell(Session session, com.jcraft.jsch.Session js, ChannelShell channel) throws IOException {
            this.js = js;
            this.session = session;
            this.channel = channel;
            this.input = channel.getInputStream();
            this.output = channel.getOutputStream();
            service.submit(this::read);
        }

        /**
         * 从 SSH 通道的输入流中读取数据，并通过 WebSocket 发送给客户端。
         */
        private void read() {
            try {
                byte[] buffer = new byte[1024 * 1024];
                int i;
                while ((i = input.read(buffer)) != -1) {
                    String text = new String(Arrays.copyOfRange(buffer, 0, i), StandardCharsets.UTF_8);
                    session.getBasicRemote().sendText(text);
                }
            } catch (Exception e) {
                log.error("读取 SSH 输入流时出现问题", e);
            }
        }

        /**
         * 关闭 SSH 连接相关的所有资源。
         *
         * @throws IOException 通信异常
         */
        public void close() throws IOException {
            input.close();
            output.close();
            channel.disconnect();
            js.disconnect();
            service.shutdown();
        }
    }
}
