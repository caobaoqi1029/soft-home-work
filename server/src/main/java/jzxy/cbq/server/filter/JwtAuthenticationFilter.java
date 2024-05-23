package jzxy.cbq.server.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import jzxy.cbq.server.entity.RestBean;
import jzxy.cbq.server.entity.dto.Account;
import jzxy.cbq.server.entity.dto.Client;
import jzxy.cbq.server.service.AccountService;
import jzxy.cbq.server.service.ClientService;
import jzxy.cbq.server.utils.Const;
import jzxy.cbq.server.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Jwt认证过滤器，用于校验请求头中的Jwt令牌，并为当前请求添加用户验证信息。
 * 将用户的ID存储在请求对象属性中，以便后续使用。
 *
 * @version 1.0.0
 * @author: cbq
 * @date: 2024/3/27 14:08
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Resource
    JwtUtils utils;
    @Resource
    ClientService service;
    @Resource
    AccountService accountService;

    /**
     * 对请求进行过滤，校验Jwt令牌。
     *
     * @param request  HttpServletRequest对象
     * @param response HttpServletResponse对象
     * @param filterChain 过滤链对象
     * @throws ServletException 如果处理请求时发生异常
     * @throws IOException      如果读取请求或写入响应时发生异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String uri = request.getRequestURI();

        if(uri.startsWith("/monitor")) {
            if(!uri.endsWith("/register")) {
                Client client = service.findClientByToken(authorization);
                if(client == null) {
                    response.setStatus(401);
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().write(RestBean.failure(401, "未注册").asJsonString());
                    return;
                } else {
                    request.setAttribute(Const.ATTR_CLIENT, client);
                }
            }
        } else {
            DecodedJWT jwt = utils.resolveJwt(authorization);
            if(jwt != null) {
                UserDetails user = utils.toUser(jwt);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                request.setAttribute(Const.ATTR_USER_ID, utils.toId(jwt));
                request.setAttribute(Const.ATTR_USER_ROLE, new ArrayList<>(user.getAuthorities()).get(0).getAuthority());

                if(request.getRequestURI().startsWith("/terminal/") && !accessShell(
                        (int) request.getAttribute(Const.ATTR_USER_ID),
                        (String) request.getAttribute(Const.ATTR_USER_ROLE),
                        Integer.parseInt(request.getRequestURI().substring(10)))) {
                    response.setStatus(401);
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().write(RestBean.failure(401, "无权访问").asJsonString());
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 校验用户是否有权限访问终端。
     *
     * @param userId   用户ID
     * @param userRole 用户角色
     * @param clientId 客户端ID
     * @return 如果用户有权限返回true，否则返回false
     */
    private boolean accessShell(int userId, String userRole, int clientId) {
        if(Const.ROLE_ADMIN.equals(userRole.substring(5))) {
            return true;
        } else {
            Account account = accountService.getById(userId);
            return account.getClientList().contains(clientId);
        }
    }
}