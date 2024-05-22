## V 1.2.0

> [!TIP]
>
> - 撰稿人：曹蓓
> - 日期：2024.5.12 10.30
> - 主题：[添加 登录、注册 功能及 dev INFO 相关的文档](https://github.com/caobaoqi1029/monitor/issues/7) 后端源代码说明部分

## 一、项目后端说明

### 1.1 项目结构说明

![image-20240511222435402](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/image-20240511222435402.png)

### 1.2 项目环境搭建

1. 克隆此仓库 `git clone https://github.com/caobaoqi1029/monitor.git --depth=1 && cd monitor`
2. 安装依赖通过 `maven`安装即可
3. 安装环境 (通过 docker 直接安装) `cd docker/path-install ` 然后 `docker compose up -d`

![image-20240511222640076](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/image-20240511222640076.png)

4. 参照 `application-prod.yaml`的信息配置 `application-dev.yaml` 即可
5. SpringBoot 启动 

![image-20240512150352263](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/image-20240512150352263.png)

 ### 1.3 项目部署

项目通过 `github workflow server/build.yaml`打包为 jar 后发布到 release 通过 `java -jar`运行即可 其依赖环境如 `mysql、redis、RabbitMQ、influxdb` 等通过 `docker compose` （docker/path-install/docker-compose.yml）的方式进行构建

```yaml
name: release
permissions:
  contents: write
on:
  push:
    tags:
      - v*

jobs:
  build:
    name: build and release
    runs-on: ${{ matrix.os }}
    defaults:
      run:
        shell: bash
        working-directory: ./server

    if: startsWith(github.ref, 'refs/tags/')
    strategy:
      fail-fast: false
      matrix:
        os: [ubuntu-latest]

    steps:
      - name: 读取仓库内容 👓
        uses: actions/checkout@v4

      - name: 构建 docker-compose.yaml 文件 🔨
        run: |
          cd ..
          zip -r ./docker-compose-server.zip ./docker
          echo "docker-compose-server.zip 打包成功"
          cp ./server-docker-compose.zip ./build/


      - name: 设置 JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'adopt'

      - name: 构建 jar 包 🔨
        run: mvn package -DskipTests

      - name: upload artifacts 📦
        uses: actions/upload-artifact@v4
        with:
          name: ${{ matrix.os }}
          path: build

      - name: release 😶‍🌫️
        uses: softprops/action-gh-release@v1
        if: startsWith(github.ref, 'refs/tags/')
        with:
          files: 'build/**'
        env:
          GITHUB_TOKEN: ${{ secrets.ACCESS_TOKEN }}
```

## 二、src 源代码说明

### 2.1 config 配置类

> [!IMPORTANT]
>
> config 配置类主要用于定义应用程序的配置信息，使得项目的配置更加集中和模块化其中包括：
>
> - `SecurityConfiguration` Security 安全配置类
> - `SwaggerConfiguration` Swagger API 配置类
> - `WebConfiguration` Web 配置类
> - `RabbitConfiguration` Rabbit 相关配置类

#### 2.1.1 SecurityConfiguration

```java
@Configuration
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RequestLogFilter requestLogFilter;
    private final JwtUtils utils;
    private final AccountService service;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter, RequestLogFilter requestLogFilter, JwtUtils utils, AccountService service) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.requestLogFilter = requestLogFilter;
        this.utils = utils;
        this.service = service;
    }

    /**
     * 配置 SpringSecurity 的过滤器链，定义安全规则和认证处理
     *
     * @param http 安全配置器
     * @return 自定义的安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().hasAnyRole(Const.ROLE_ADMIN, Const.ROLE_NORMAL)
                )
                .formLogin(conf -> conf
                        .loginProcessingUrl("/api/auth/login")
                        .failureHandler(this::handleProcess)
                        .successHandler(this::handleProcess)
                        .permitAll()
                )
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                .exceptionHandling(conf -> conf
                        .accessDeniedHandler(this::handleProcess)
                        .authenticationEntryPoint(this::handleProcess)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(requestLogFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, RequestLogFilter.class)
                .build();
    }

    /**
     * 统一处理登录、登录失败、无权限和退出登录的逻辑
     *
     * @param request                   HTTP 请求
     * @param response                  HTTP 响应
     * @param exceptionOrAuthentication 异常对象或认证对象
     * @throws IOException IO 异常
     */
    private void handleProcess(HttpServletRequest request,
                               HttpServletResponse response,
                               Object exceptionOrAuthentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        if (exceptionOrAuthentication instanceof AccessDeniedException exception) {
            writer.write(RestBean
                    .forbidden(exception.getMessage()).asJsonString());
        } else if (exceptionOrAuthentication instanceof Exception exception) {
            writer.write(RestBean
                    .unauthorized(exception.getMessage()).asJsonString());
        } else if (exceptionOrAuthentication instanceof Authentication authentication) {
            User user = (User) authentication.getPrincipal();
            Account account = service.findAccountByNameOrEmail(user.getUsername());
            String jwt = utils.createJwt(user, account.getUsername(), account.getId());
            if (jwt == null) {
                writer.write(RestBean.forbidden("登录验证频繁，请稍后再试").asJsonString());
            } else {
                AuthorizeVO vo = account.asViewObject(AuthorizeVO.class, o -> o.setToken(jwt));
                vo.setExpire(utils.expireTime());
                writer.write(RestBean.success(vo).asJsonString());
            }
        }
    }

    /**
     * 处理用户退出登录逻辑，包括将令牌加入黑名单
     *
     * @param request        HTTP 请求
     * @param response       HTTP 响应
     * @param authentication 认证信息
     * @throws IOException IO 异常
     */
    private void onLogoutSuccess(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        String authorization = request.getHeader("Authorization");
        if (utils.invalidateJwt(authorization)) {
            writer.write(RestBean.success("退出登录成功").asJsonString());
            return;
        }
        writer.write(RestBean.failure(400, "退出登录失败").asJsonString());
    }
}
```




#### 2.1.2 SwaggerConfiguration

```java
@Configuration
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "Bearer",
        name = "Authorization", in = SecuritySchemeIn.HEADER)
@OpenAPIDefinition(security = { @SecurityRequirement(name = "Authorization") })
public class SwaggerConfiguration {

    /**
     * 配置 OpenAPI 文档的基本信息，包括标题、描述、版本和授权信息。
     *
     * @return OpenAPI 对象，包含配置的 API 文档信息。
     */
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Monitor-CBQ API 文档")
                        .description("晋中学院 2024 计算机专升本 2301 班 软件工程课程设计")
                        .version("V1.0.0")
                        .license(new License()
                                .name("Github")
                                .url("https://github.com/caobaoqi1029/monitor")
                        )
                )
                .externalDocs(new ExternalDocumentation()
                        .description("About Me")
                        .url("https://github.com/caobaoqi1029")
                );
    }

    /**
     * 配置自定义的 OpenAPI 相关信息，例如全局请求头等。
     *
     * @return OpenApiCustomizer 用于定制 OpenAPI 的回调对象。
     */
    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer() {
        return api -> this.authorizePathItems().forEach(api.getPaths()::addPathItem);
    }

    /**
     * 手动添加特定路径的操作，例如登录和退出登录接口，这些接口可能不会被自动扫描到。
     *
     * @return Map 包含路径和对应 PathItem 的映射。
     */
    private Map<String, PathItem> authorizePathItems(){
        Map<String, PathItem> map = new HashMap<>();
        map.put("/api/auth/login", new PathItem()
                .post(new Operation()
                        .tags(List.of("登录校验相关"))
                        .summary("登录验证接口")
                        .addParametersItem(new QueryParameter()
                                .name("username")
                                .required(true)
                        )
                        .addParametersItem(new QueryParameter()
                                .name("password")
                                .required(true)
                        )
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content().addMediaType("*/*", new MediaType()
                                                .example(RestBean.success(new AuthorizeVO()).asJsonString())
                                        ))
                                )
                        )
                )
        );
        map.put("/api/auth/logout", new PathItem()
                .get(new Operation()
                        .tags(List.of("登录校验相关"))
                        .summary("退出登录接口")
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content().addMediaType("*/*", new MediaType()
                                                .example(RestBean.success())
                                        ))
                                )
                        )
                )

        );
        return map;
    }
}

```



#### 2.1.3 WebConfiguration

```java
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    /**
     * 创建并返回一个 BCryptPasswordEncoder 实例
     * 该方法配置了一个密码编码器，它使用 BCrypt 算法来加密和验证密码
     *
     * @return 返回一个 BCryptPasswordEncoder 实例，用于密码的加密处理
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
```



#### 2.1.4 RabbitConfiguration

```java
@Configuration
public class RabbitConfiguration {
    /**
     * 创建并定义一个名为 mail 的持久化队列
     *
     * @return Queue 返回构建好的队列对象
     */
    @Bean("mailQueue")
    public Queue queue() {
        return QueueBuilder
                .durable(Const.MQ_MAIL)
                .build();
    }
}
```



### 2.2 controller 控制器

> [!IMPORTANT]
>
> controller 控制器类，处理外部请求并返回响应。控制器层位于MVC（Model-View-Controller）架构的 “C” 部分，是用户界面（UI）和应用程序后端之间的桥梁
>
> - AuthorizeController 登录校验相关类


#### 2.2.1 AuthorizeController

```java
@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name = "登录校验相关", description = "包括用户登录、注册、验证码请求等接口")
public class AuthorizeController {

    private final AccountService accountService;

    public AuthorizeController(AccountService accountService) {
        this.accountService = accountService;
    }

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
            requestBody = @io.swagger.v2.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = EmailRegisterVO.class)))
    )
    public RestBean<Void> register(@io.swagger.v2.oas.annotations.parameters.RequestBody(description = "邮箱注册 vo") @RequestBody @Valid EmailRegisterVO vo) {
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
            requestBody = @io.swagger.v2.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = ConfirmResetVO.class))),
            tags = {"登录校验相关"})
    @ApiResponse(responseCode = "200", description = "确认成功",
            content = @Content(schema = @Schema(implementation = RestBean.class)))
    @ApiResponse(responseCode = "400", description = "确认失败（参数错误或验证码不匹配）",
            content = @Content(schema = @Schema(implementation = RestBean.class)))
    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@io.swagger.v2.oas.annotations.parameters.RequestBody(description = "确认重置 vo") @RequestBody @Valid ConfirmResetVO vo) {
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
            requestBody = @io.swagger.v2.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = EmailResetVO.class))),
            tags = {"登录校验相关"})
    @ApiResponse(responseCode = "200", description = "重置成功",
            content = @Content(schema = @Schema(implementation = RestBean.class)))
    @ApiResponse(responseCode = "400", description = "重置失败（参数错误或邮箱未找到）",
            content = @Content(schema = @Schema(implementation = RestBean.class)))
    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@io.swagger.v2.oas.annotations.parameters.RequestBody(description = "重置邮箱 vo") @RequestBody @Valid EmailResetVO vo) {
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
```



### 2.3 entity 实体类

> [!IMPORTANT]
>
> entity 实体类映射数据库中的表。实体类通常与数据库中的表一一对应，用于表示数据库表中的数据行
>
> - Account 账户类

#### 2.2.1 Account

```java
@Data
@AllArgsConstructor
public class Account implements BaseData {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    Integer id;
    /**
     * username 用户名
     */
    String username;
    /**
     * password 密码
     */
    String password;
    /**
     * email 邮箱
     */
    String email;
    /**
     * role 角色
     */
    String role;
    /**
     * avatar 头像 url
     */
    String avatar;
    /**
     * registerTime 注册时间
     */
    Date registerTime;
    /**
     * clients 客户端
     */
    String clients;

    /**
     * 获取该用户客户端列表
     * @return List<Integer> 客户端 id list
     */
    public List<Integer> getClientList() {
        if (clients == null) return Collections.emptyList();
        return JSONArray.parse(clients).toList(Integer.class);
    }
}
```



### 2.4 exception 统一异常处理

> [!IMPORTANT]
>
> exception 统一异常处理类用于处理整个应用程序中发生的异常。通过统一的异常处理，可以捕获并处理不同类型的异常，返回给用户更加友好的错误信息，同时使得代码更加清洁和易于维护
>
> - ErrorPageController 页面异常处理类
> - ValidationController 参数校验异常处理类

#### 2.4.1 ErrorPageController

```java
@RestController
@RequestMapping({"${server.error.path:${error.path:/error}}"})
public class ErrorPageController extends AbstractErrorController {
    public ErrorPageController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    /**
     * 所有错误在这里统一处理，自动解析状态码和原因
     * @param request 请求
     * @return 失败响应
     */
    @RequestMapping
    public RestBean<Void> error(HttpServletRequest request) {
        HttpStatus status = this.getStatus(request);
        Map<String, Object> errorAttributes = this.getErrorAttributes(request, this.getAttributeOptions());
        String message = this.convertErrorMessage(status)
                .orElse(errorAttributes.get("message").toString());
        return RestBean.failure(status.value(), message);
    }

    /**
     * 对于一些特殊的状态码，错误信息转换
     * @param status 状态码
     * @return 错误信息
     */
    private Optional<String> convertErrorMessage(HttpStatus status){
        String value = switch (status.value()) {
            case 400 -> "请求参数有误";
            case 404 -> "请求的接口不存在";
            case 405 -> "请求方法错误";
            case 500 -> "内部错误，请联系管理员";
            default -> null;
        };
        return Optional.ofNullable(value);
    }

    /**
     * 错误属性获取选项，这里额外添加了错误消息和异常类型
     * @return 选项
     */
    private ErrorAttributeOptions getAttributeOptions(){
        return ErrorAttributeOptions
                .defaults()
                .including(ErrorAttributeOptions.Include.MESSAGE,
                        ErrorAttributeOptions.Include.EXCEPTION);
    }
}
```



#### 2.4.2 ValidationController

```java
@Slf4j
@RestControllerAdvice
public class ValidationController {

    /**
     * 与SpringBoot 保持一致，校验不通过打印警告信息，而不是直接抛出异常
     * @param exception 验证异常
     * @return 校验结果
     */
    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> validateError(ValidationException exception) {
        log.warn("Resolved [{}: {}]", exception.getClass().getName(), exception.getMessage());
        return RestBean.failure(400, "请求参数有误");
    }
}
```



### 2.5 filter 过滤器

> [!IMPORTANT]
>
> filter 过滤器类用于在请求到达控制器之前或响应发送给客户端之前执行过滤任务。过滤器可以用于日志记录、身份验证、请求数据的预处理等
>
> - CorsFilter 跨域请求过滤器类
> - FlowLimitingFilter  接口限流过滤器类
> - JwtAuthenticationFilter 请求校验过滤器类
> - RequestLogFilter 请求日志过滤器类

#### 2.5.1 CorsFilter

```java
@Component
@Order(Const.ORDER_CORS)
public class CorsFilter extends HttpFilter {
    @Value("${spring.web.cors.origin}")
    String origin;
    @Value("${spring.web.cors.credentials}")
    boolean credentials;
    @Value("${spring.web.cors.methods}")
    String methods;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.addCorsHeader(request, response);
        chain.doFilter(request, response);
    }

    /**
     * 添加所有跨域相关响应头
     *
     * @param request  请求
     * @param response 响应
     */
    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", this.resolveOrigin(request));
        response.addHeader("Access-Control-Allow-Methods", this.resolveMethod());
        response.addHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        if (credentials) {
            response.addHeader("Access-Control-Allow-Credentials", "true");
        }
    }

    /**
     * 解析配置文件中的请求方法
     *
     * @return 解析得到的请求头值
     */
    private String resolveMethod() {
        return methods.equals("*") ? "GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE, PATCH" : methods;
    }

    /**
     * 解析配置文件中的请求原始站点
     *
     * @param request 请求
     * @return 解析得到的请求头值
     */
    private String resolveOrigin(HttpServletRequest request) {
        return origin.equals("*") ? request.getHeader("Origin") : origin;
    }
}
```



#### 2.5.2 FlowLimitingFilter

```java
@Slf4j
@Component
@Order(Const.ORDER_FLOW_LIMIT)
public class FlowLimitingFilter extends HttpFilter {
    @Resource
    StringRedisTemplate template;
    @Value("${spring.web.flow.limit}")
    int limit;
    @Value("${spring.web.flow.period}")
    int period;
    @Value("${spring.web.flow.block}")
    int block;
    @Resource
    FlowUtils utils;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String address = request.getRemoteAddr();
        if (!tryCount(address))
            this.writeBlockMessage(response);
        else
            chain.doFilter(request, response);
    }

    /**
     * 尝试对指定 IP 地址请求计数，如果被限制则无法继续访问
     * @param address 请求 IP 地址
     * @return 是否操作成功
     */
    private boolean tryCount(String address) {
        synchronized (address.intern()) {
            if(Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_BLOCK + address)))
                return false;
            String counterKey = Const.FLOW_LIMIT_COUNTER + address;
            String blockKey = Const.FLOW_LIMIT_BLOCK + address;
            return utils.limitPeriodCheck(counterKey, blockKey, block, limit, period);
        }
    }

    /**
     * 为响应编写拦截内容，提示用户操作频繁
     * @param response 响应
     * @throws IOException 可能的异常
     */
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(RestBean.forbidden("操作频繁，请稍后再试").asJsonString());
    }
}
```



#### 2.5.3 JwtAuthenticationFilter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Resource
    JwtUtils utils;
    @Resource
    AccountService accountService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
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
        if (Const.ROLE_ADMIN.equals(userRole.substring(5))) {
            return true;
        } else {
            Account account = accountService.getById(userId);
            return account.getClientList().contains(clientId);
        }
    }
}

```



#### 2.5.4 RequestLogFilter

```java
@Slf4j
@Component
public class RequestLogFilter extends OncePerRequestFilter {
    @Resource
    SnowflakeIdGenerator generator;

    private final Set<String> ignores = Set.of("/swagger-ui", "/v3/api-docs", "/monitor/runtime",
            "/api/monitor/list", "/api/monitor/runtime-now");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(this.isIgnoreUrl(request.getServletPath())) {
            filterChain.doFilter(request, response);
        } else {
            long startTime = System.currentTimeMillis();
            this.logRequestStart(request);
            ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
            filterChain.doFilter(request, wrapper);
            this.logRequestEnd(wrapper, startTime);
            wrapper.copyBodyToResponse();
        }
    }

    /**
     * 判定当前请求 url 是否不需要日志打印
     * @param url 路径
     * @return 是否忽略
     */
    private boolean isIgnoreUrl(String url){
        for (String ignore : ignores) {
            if(url.startsWith(ignore)) return true;
        }
        return false;
    }

    /**
     * 请求结束时的日志打印，包含处理耗时以及响应结果
     * @param wrapper 用于读取响应结果的包装类
     * @param startTime 起始时间
     */
    public void logRequestEnd(ContentCachingResponseWrapper wrapper, long startTime){
        long time = System.currentTimeMillis() - startTime;
        int status = wrapper.getStatus();
        String content = status != 200 ?
                status + " 错误" : new String(wrapper.getContentAsByteArray());
        log.info("请求处理耗时: {}ms | 响应结果: {}", time, content);
    }

    /**
     * 请求开始时的日志打印，包含请求全部信息，以及对应用户角色
     * @param request 请求
     */
    public void logRequestStart(HttpServletRequest request){
        long reqId = generator.nextId();
        MDC.put("reqId", String.valueOf(reqId));
        JSONObject object = new JSONObject();
        request.getParameterMap().forEach((k, v) -> object.put(k, v.length > 0 ? v[0] : null));
        Object id = request.getAttribute(Const.ATTR_USER_ID);
        if(id != null) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            log.info("请求URL: \"{}\" ({}) | 远程IP地址: {} │ 身份: {} (UID: {}) | 角色: {} | 请求参数列表: {}",
                    request.getServletPath(), request.getMethod(), request.getRemoteAddr(),
                    user.getUsername(), id, user.getAuthorities(), object);
        } else {
            log.info("请求URL: \"{}\" ({}) | 远程IP地址: {} │ 身份: 未验证 | 请求参数列表: {}",
                    request.getServletPath(), request.getMethod(), request.getRemoteAddr(), object);
        }
    }
}
```



### 2.6 listener 监听器

> [!IMPORTANT]
>
> listener 监听器类用于监听应用程序中的事件。监听器可以对各种事件（如HTTP请求、应用上下文事件）作出响应，执行特定的逻辑
>
> - MailQueueListener 邮件队列监听器类

#### 2.5.1 MailQueueListener

```java
@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {
    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;

    /**
     * 处理邮件发送
     *
     * @param data 邮件信息
     */
    @RabbitHandler
    public void sendMailMessage(Map<String, Object> data) {
        String email = data.get("email").toString();
        Integer code = (Integer) data.get("code");
        SimpleMailMessage message = switch (data.get("type").toString()) {
            case "register" -> createMessage("欢迎注册我们的网站",
                    "您的邮件注册验证码为: " + code + "，有效时间 3 分钟，为了保障您的账户安全，请勿向他人泄露验证码信息",
                    email);
            case "reset" -> createMessage("您的密码重置邮件",
                    "你好，您正在执行重置密码操作，验证码: " + code + "，有效时间 3 分钟，如非本人操作，请无视",
                    email);
            case "modify" -> createMessage("您的邮件修改验证邮件",
                    "您好，您正在绑定新的电子邮件地址，验证码: " + code + "，有效时间 3 分钟，如非本人操作，请无视",
                    email);
            default -> null;
        };
        if (message == null) return;
        sender.send(message);
    }

    /**
     * 快速封装简单邮件消息实体
     *
     * @param title   标题
     * @param content 内容
     * @param email   收件人
     * @return 邮件实体
     */
    private SimpleMailMessage createMessage(String title, String content, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(email);
        message.setFrom(username);
        return message;
    }
}

```



### 2.7 mapper 接口层

> [!IMPORTANT]
>
> mapper 接口层定义了数据库操作的方法。这些方法对应于SQL语句的执行，用于数据的持久化操作
>
> - AccountMapper 账户操作类

#### 2.7.1 AccountMapper

```java
public interface AccountMapper extends BaseMapper<Account> {
}
```



### 2.8 service 服务层

> [!IMPORTANT]
>
> service 服务层包含业务逻辑的核心部分。服务层位于控制器和数据访问层之间，负责处理业务需求，调用数据访问层完成数据的持久化
>
> - AccountService 账户服务类

#### 2.8.1 AccountService

```java
public interface AccountService extends IService<Account>, UserDetailsService {
    /**
     * 通过用户名或邮箱查找账户
     *
     * @param text 查找关键字，可以是用户名或邮箱
     * @return 返回匹配的账户信息，如果没有找到返回 null
     */
    Account findAccountByNameOrEmail(String text);

    /**
     * 注册邮箱验证代码
     *
     * @param type    验证类型，例如注册、重置密码等
     * @param email   需要验证的邮箱
     * @param address 验证码发送地址
     * @return 返回生成的验证码或错误提示
     */
    String registerEmailVerifyCode(String type, String email, String address);

    String registerEmailAccount(EmailRegisterVO info);


    /**
     * 通过邮箱重置账户密码
     *
     * @param info 包含邮箱验证信息和新密码的数据对象
     * @return 返回操作结果，成功或失败的原因
     */
    String resetEmailAccountPassword(EmailResetVO info);

    /**
     * 邮箱重置密码后的确认操作
     *
     * @param info 包含新密码和验证码的数据对象
     * @return 返回确认操作的结果，成功或失败的原因
     */
    String resetConfirm(ConfirmResetVO info);

    /**
     * 更改账户密码
     *
     * @param id      用户 ID
     * @param oldPass 原密码
     * @param newPass 新密码
     * @return 返回密码更改结果，成功或失败的原因
     */
    boolean changePassword(int id, String oldPass, String newPass);

    /**
     * 修改账户的邮箱
     *
     * @param id 用户 ID
     * @param vo 包含新邮箱和验证信息的数据对象
     * @return 返回邮箱修改结果，成功或失败的原因
     */
    String modifyEmail(int id, ModifyEmailVO vo);
}

```

```java
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {
    @Value("${spring.web.verify.mail-limit}")
    int verifyLimit;

    private final AmqpTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final FlowUtils flow;

    public AccountServiceImpl(AmqpTemplate rabbitTemplate, StringRedisTemplate stringRedisTemplate, PasswordEncoder passwordEncoder, FlowUtils flow) {
        this.rabbitTemplate = rabbitTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.flow = flow;
    }


    /**
     * 从数据库中通过用户名或邮箱查找用户详细信息
     *
     * @param username 用户名
     * @return 用户详细信息
     * @throws UsernameNotFoundException 如果用户未找到则抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if (account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    /**
     * 生成注册验证码存入 Redis 中，并将邮件发送请求提交到消息队列等待发送
     *
     * @param type    类型
     * @param email   邮件地址
     * @param address 请求 IP 地址
     * @return 操作结果，null 表示正常，否则为错误原因
     */
    public String registerEmailVerifyCode(String type, String email, String address) {
        synchronized (address.intern()) {
            if (!this.verifyLimit(address))
                return "请求频繁，请稍后再试";
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            rabbitTemplate.convertAndSend(Const.MQ_MAIL, data);
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        }
    }

    /**
     * 邮件验证码注册账号操作，需要检查验证码是否正确以及邮箱、用户名是否存在重名
     *
     * @param info 注册基本信息
     * @return 操作结果，null表示正常，否则为错误原因
     */
    public String registerEmailAccount(EmailRegisterVO info) {
        String email = info.getEmail();
        String code = this.getEmailVerifyCode(email);
        if (code == null) return "请先获取验证码";
        if (!code.equals(info.getCode())) return "验证码错误，请重新输入";
        if (this.findAccountByNameOrEmail(email) != null) return "该邮件地址已被注册";
        String username = info.getUsername();
        if (this.findAccountByNameOrEmail(username) != null) return "该用户名已被他人使用，请重新更换";
        String password = passwordEncoder.encode(info.getPassword());
        Account account = new Account(null, info.getUsername(),
                password, email, Const.ROLE_ADMIN, Const.DEFAULT_AVATAR, new Date(), null);
        if (!this.save(account)) {
            return "内部错误，注册失败";
        } else {
            this.deleteEmailVerifyCode(email);
            return null;
        }
    }

    /**
     * 邮件验证码重置密码操作，需要检查验证码是否正确
     *
     * @param info 重置基本信息
     * @return 操作结果，null 表示正常，否则为错误原因
     */
    @Override
    public String resetEmailAccountPassword(EmailResetVO info) {
        String verify = resetConfirm(new ConfirmResetVO(info.getEmail(), info.getCode()));
        if (verify != null) return verify;
        String email = info.getEmail();
        String password = passwordEncoder.encode(info.getPassword());
        boolean update = this.update().eq("email", email).set("password", password).update();
        if (update) {
            this.deleteEmailVerifyCode(email);
        }
        return update ? null : "更新失败，请联系管理员";
    }

    /**
     * 重置密码确认操作，验证验证码是否正确
     *
     * @param info 验证基本信息
     * @return 操作结果，null 表示正常，否则为错误原因
     */
    @Override
    public String resetConfirm(ConfirmResetVO info) {
        String email = info.getEmail();
        String code = this.getEmailVerifyCode(email);
        if (code == null) return "请先获取验证码";
        if (!code.equals(info.getCode())) return "验证码错误，请重新输入";
        return null;
    }

    /**
     * 修改密码
     *
     * @param id      id
     * @param oldPass oldPass
     * @param newPass newPass
     * @return 操作结果
     */
    @Override
    public boolean changePassword(int id, String oldPass, String newPass) {
        Account account = this.getById(id);
        String password = account.getPassword();
        if (!passwordEncoder.matches(oldPass, password))
            return false;
        this.update(Wrappers.<Account>update().eq("id", id)
                .set("password", passwordEncoder.encode(newPass)));
        return true;
    }

    /**
     * 修改邮箱
     *
     * @param id id
     * @param vo vo
     * @return 操作结果
     */
    @Override
    public String modifyEmail(int id, ModifyEmailVO vo) {
        String code = getEmailVerifyCode(vo.getEmail());
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        this.deleteEmailVerifyCode(vo.getEmail());
        Account account = this.findAccountByNameOrEmail(vo.getEmail());
        if (account != null && account.getId() != id) return "该邮箱账号已经被其他账号绑定，无法完成操作";
        this.update()
                .set("email", vo.getEmail())
                .eq("id", id)
                .update();
        return null;
    }

    /**
     * 移除 Redis 中存储的邮件验证码
     *
     * @param email 电邮
     */
    private void deleteEmailVerifyCode(String email) {
        String key = Const.VERIFY_EMAIL_DATA + email;
        stringRedisTemplate.delete(key);
    }

    /**
     * 获取 Redis 中存储的邮件验证码
     *
     * @param email 电邮
     * @return 验证码
     */
    private String getEmailVerifyCode(String email) {
        String key = Const.VERIFY_EMAIL_DATA + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 针对 IP 地址进行邮件验证码获取限流
     *
     * @param address 地址
     * @return 是否通过验证
     */
    private boolean verifyLimit(String address) {
        String key = Const.VERIFY_EMAIL_LIMIT + address;
        return flow.limitOnceCheck(key, verifyLimit);
    }

    /**
     * 通过用户名或邮件地址查找用户
     *
     * @param text 用户名或邮件
     * @return 账户实体
     */
    public Account findAccountByNameOrEmail(String text) {
        return this.query()
                .eq("username", text).or()
                .eq("email", text)
                .one();
    }
}
```





### 2.9 utils 工具类

> [!IMPORTANT]
>
> utils 工具类包含一系列静态方法，用于执行通用的任务，如字符串处理、日期计算、加密解密等。工具类的目的是减少代码重复，并提供一种方便的方式来执行频繁使用的操作
>
> - Const 常量
> - FlowUtils 限流工具类
> - JwtUtils 校验工具类
> - SnowFlakeIdGenerator 雪花 ID 生成器

#### 2.9.1 Const 常量

```java
public final class Const {
    /**
     * 默认头像 url
     */
    public final static String DEFAULT_AVATAR = "https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/avatar.png";
    /**
     * JWT 黑名单键值前缀，用于标识特定黑名单条目
     */
    public final static String JWT_BLACK_LIST = "jwt:blacklist:";
    /**
     * JWT 频率控制键值前缀，用于关联与 JWT 相关的频率限制数据
     */
    public final static String JWT_FREQUENCY = "jwt:frequency:";
    /**
     * 用户全局黑名单键值前缀，存储被禁用或受限用户的标识
     */
    public final static String USER_BLACK_LIST = "user:blacklist:";
    /**
     * 流量控制计数器键值前缀，用于统计服务调用频次
     */
    public final static String FLOW_LIMIT_COUNTER = "flow:counter:";
    /**
     * 流量控制阻断状态键值前缀，用于记录因超出阈值而被临时阻止的服务调用
     */
    public final static String FLOW_LIMIT_BLOCK = "flow:block:";
    /**
     * 邮箱验证频率限制记录键值前缀，用于追踪单个邮箱的验证请求次数
     */
    public final static String VERIFY_EMAIL_LIMIT = "verify:email:limit:";
    /**
     * 邮箱验证数据存储键值前缀，用于存储邮箱验证过程中的相关数据
     */
    public final static String VERIFY_EMAIL_DATA = "verify:email:data:";
    /**
     * 订单流程异常时使用的特殊标记，表示订单流控限制已被触发
     */
    public final static int ORDER_FLOW_LIMIT = -101;
    /**
     * 订单跨域资源共享（CORS）失败时的错误码标识
     */
    public final static int ORDER_CORS = -102;
    /**
     * 用户 ID 属性名，在上下文或其他对象中用于标识用户唯一 ID
     */
    public final static String ATTR_USER_ID = "userId";
    /**
     * 用户角色属性名，用于存储或传递用户的权限角色信息
     */
    public final static String ATTR_USER_ROLE = "userRole";
    /**
     * 客户端属性名，在上下文中标识客户端类型或实例
     */
    public final static String ATTR_CLIENT = "client";
    /**
     * 消息队列主题标识，对应邮件发送服务
     */
    public final static String MQ_MAIL = "mail";
    /**
     * 表示管理员角色的字符串标识符
     */
    public final static String ROLE_ADMIN = "admin";
    /**
     * 表示普通用户角色的字符串标识符
     */
    public final static String ROLE_NORMAL = "user";
}
```



#### 2.9.2 FlowUtils

```java
@Slf4j
@Component
public class FlowUtils {
    @Resource
    StringRedisTemplate template;
    /**
     * 针对于单次频率限制，请求成功后，在冷却时间内不得再次进行请求，如 3 秒内不能再次发起请求
     * @param key key
     * @param blockTime 限制时间
     * @return 是否通过限流检查
     */
    public boolean limitOnceCheck(String key, int blockTime){
        return this.internalCheck(key, 1, blockTime, (overclock) -> false);
    }

    /**
     * 针对于单次频率限制，请求成功后，在冷却时间内不得再次进行请求
     * 如 3 秒内不能再次发起请求，如果不听劝阻继续发起请求，将限制更长时间
     * @param key key
     * @param frequency 请求频率
     * @param baseTime 基础限制时间
     * @param upgradeTime 升级限制时间
     * @return 是否通过限流检查
     */
    public boolean limitOnceUpgradeCheck(String key, int frequency, int baseTime, int upgradeTime){
        return this.internalCheck(key, frequency, baseTime, (overclock) -> {
                    if (overclock)
                        template.opsForValue().set(key, "1", upgradeTime, TimeUnit.SECONDS);
                    return false;
                });
    }

    /**
     * 针对于在时间段内多次请求限制，如3秒内限制请求 20 次，超出频率则封禁一段时间
     * @param counterKey 计数键
     * @param blockKey 封禁键
     * @param blockTime 封禁时间
     * @param frequency 请求频率
     * @param period 计数周期
     * @return 是否通过限流检查
     */
    public boolean limitPeriodCheck(String counterKey, String blockKey, int blockTime, int frequency, int period){
        return this.internalCheck(counterKey, frequency, period, (overclock) -> {
                    if (overclock)
                        template.opsForValue().set(blockKey, "", blockTime, TimeUnit.SECONDS);
                    return !overclock;
                });
    }

    /**
     * 内部使用请求限制主要逻辑
     * @param key key
     * @param frequency 请求频率
     * @param period 计数周期
     * @param action 限制行为与策略
     * @return 是否通过限流检查
     */
    private boolean internalCheck(String key, int frequency, int period, LimitAction action){
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            Long value = Optional.ofNullable(template.opsForValue().increment(key)).orElse(0L);
            return action.run(value > frequency);
        } else {
            template.opsForValue().set(key, "1", period, TimeUnit.SECONDS);
            return true;
        }
    }

    /**
     * 内部使用，限制行为与策略
     */
    private interface LimitAction {
        boolean run(boolean overclock);
    }
}
```



#### 2.9.3 JwtUtils

```java
@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    private String key;
    @Value("${spring.security.jwt.expire}")
    private int expire;
    @Value("${spring.security.jwt.limit.base}")
    private int limit_base;
    @Value("${spring.security.jwt.limit.upgrade}")
    private int limit_upgrade;
    @Value("${spring.security.jwt.limit.frequency}")
    private int limit_frequency;
    @Resource
    StringRedisTemplate template;
    @Resource
    FlowUtils utils;

    /**
     * 让指定 Jwt 令牌失效
     * @param headerToken 请求头中携带的令牌
     * @return 是否操作成功
     */
    public boolean invalidateJwt(String headerToken){
        String token = this.convertToken(headerToken);
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            return deleteToken(verify.getId(), verify.getExpiresAt());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 根据配置快速计算过期时间
     * @return 过期时间
     */
    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire);
        return calendar.getTime();
    }

    /**
     * 根据 UserDetails 生成对应的 Jwt 令牌
     * @param user 用户信息
     * @return 令牌
     */
    public String createJwt(UserDetails user, String username, int userId) {
        if(this.frequencyCheck(userId)) {
            Algorithm algorithm = Algorithm.HMAC256(key);
            Date expire = this.expireTime();
            return JWT.create()
                    .withJWTId(UUID.randomUUID().toString())
                    .withClaim("id", userId)
                    .withClaim("name", username)
                    .withClaim("authorities", user.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority).toList())
                    .withExpiresAt(expire)
                    .withIssuedAt(new Date())
                    .sign(algorithm);
        } else {
            return null;
        }
    }

    /**
     * 解析Jwt令牌
     * @param headerToken 请求头中携带的令牌
     * @return DecodedJWT
     */
    public DecodedJWT resolveJwt(String headerToken){
        String token = this.convertToken(headerToken);
        if(token == null) return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            if(this.isInvalidToken(verify.getId())) return null;
            if(this.isInvalidUser(verify.getClaim("id").asInt())) return null;
            Map<String, Claim> claims = verify.getClaims();
            return new Date().after(claims.get("exp").asDate()) ? null : verify;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * 将jwt对象中的内容封装为 UserDetails
     * @param jwt 已解析的 Jwt 对象
     * @return UserDetails
     */
    public UserDetails toUser(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    /**
     * 将 jwt 对象中的用户 ID 提取出来
     * @param jwt 已解析的 Jwt 对象
     * @return 用户 ID
     */
    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    /**
     * 频率检测，防止用户高频申请 Jwt 令牌，并且采用阶段封禁机制
     * 如果已经提示无法登录的情况下用户还在刷，那么就封禁更长时间
     * @param userId 用户 ID
     * @return 是否通过频率检测
     */
    private boolean frequencyCheck(int userId){
        String key = Const.JWT_FREQUENCY + userId;
        return utils.limitOnceUpgradeCheck(key, limit_frequency, limit_base, limit_upgrade);
    }

    /**
     * 校验并转换请求头中的 Token 令牌
     * @param headerToken 请求头中的 Token
     * @return 转换后的令牌
     */
    private String convertToken(String headerToken){
        if(headerToken == null || !headerToken.startsWith("Bearer "))
            return null;
        return headerToken.substring(7);
    }

    /**
     * 将 Token 列入 Redis 黑名单中
     * @param uuid 令牌 ID
     * @param time 过期时间
     * @return 是否操作成功
     */
    private boolean deleteToken(String uuid, Date time){
        if(this.isInvalidToken(uuid))
            return false;
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0);
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MILLISECONDS);
        return true;
    }

    public void deleteUser(int uid) {
        template.opsForValue().set(Const.USER_BLACK_LIST + uid, "", expire, TimeUnit.HOURS);
    }

    private boolean isInvalidUser(int uid){
        return Boolean.TRUE.equals(template.hasKey(Const.USER_BLACK_LIST + uid));
    }

    /**
     * 验证 Token 是否被列入 Redis 黑名单
     * @param uuid 令牌 ID
     * @return 是否操作成功
     */
    private boolean isInvalidToken(String uuid){
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }
}
```



#### 2.9.4 SnowflakeIdGenerator

```java
@Component
public class SnowflakeIdGenerator {
    private static final long START_TIMESTAMP = 1691087910202L;
    private static final long DATA_CENTER_ID_BITS = 5L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;
    private final long dataCenterId;
    private final long workerId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowflakeIdGenerator() {
        this(1, 1);
    }

    private SnowflakeIdGenerator(long dataCenterId, long workerId) {
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("Data center ID can't be greater than " + MAX_DATA_CENTER_ID + " or less than 0");
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID can't be greater than " + MAX_WORKER_ID + " or less than 0");
        }
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    /**
     * 生成一个新的雪花算法 ID 加锁
     *
     * @return 雪花 ID
     */
    public synchronized long nextId() {
        long timestamp = getCurrentTimestamp();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards. Refusing to generate ID.");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = getNextTimestamp(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT) |
                (dataCenterId << DATA_CENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    private long getNextTimestamp(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }
}


```



### 2.10 SpringBoot 启动 

> [!IMPORTANT]
>
> 原神，启动 ！！1

```java
@SpringBootApplication
@MapperScan("jzxy.cbq.server.mapper")
@Slf4j
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);

        log.info("ServerApplication run success ");
    }
}
```

## 三、resource 项目配置说明

### 3.1 application.yaml

```yaml
spring:
  application:
    name: monitor-server
  profiles:
    active: dev
```



### 3.2 application-prod.yaml

```yaml
server:
  port: 9090
springdoc:
  paths-to-match: /**
  paths-to-exclude: /error/**
  swagger-ui:
    operations-sorter: alpha
spring:
  mail:
    host: TODO
    username:
    password:
  rabbitmq:
    addresses:
    username:
    password:
    virtual-host:
  data:
    redis:
      host:
      port:
  datasource:
    url:
    username:
    password: TODO
    driver-class-name: com.mysql.cj.jdbc.Driver
  security:
    jwt:
      key: TODO
      expire: 72
      limit:
        base: 10
        upgrade: 300
        frequency: 30
    filter:
      order: -100
    web:
      verify:
        mail-limit: 60
      flow:
        period: 5
        limit: 100
        block: 30
      cors:
        origin: '*'
        credentials: false
        methods: '*'
mybatis-plus:
  global-config:
    db-config:
      table-prefix: tb_
```



### 3.3 application-dev.yaml

```yaml
server:
  port: 8080
springdoc:
  paths-to-match: /**
  paths-to-exclude: /error/**
  swagger-ui:
    operations-sorter: alpha
spring:
  influx:
    url: http://localhost:8086
    user: cbq
    password: cbq.0515
  mail:
    host: smtp.162.com
    username: 15340791287@162.com
    password: PMFQPODUGZIJJZBT
  rabbitmq:
    addresses: localhost
    username: cbq
    password: cbq
    virtual-host: /
  data:
    redis:
      host: localhost
      port: 6379
  datasource:
    url: jdbc:mysql://localhost:3306/monitor
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  security:
    jwt:
      key: cbq.monitor
      expire: 72
      limit:
        base: 10
        upgrade: 300
        frequency: 30
    filter:
      order: -100
  web:
    verify:
      mail-limit: 60
    flow:
      period: 5
      limit: 100
      block: 30
    cors:
      origin: '*'
      credentials: false
      methods: '*'
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      table-prefix: tb_
```

## 四、测试

#### 4.1 AuthorizeControllerTest

```java
@SpringBootTest
class AuthorizeControllerTest {
    @Resource
    AccountService service;

    @Test
    void askVerifyCode() {
        Assertions.assertNull(service.registerEmailVerifyCode("register", "2024cbq@gmail.com", "127.0.0.1"));
    }

    @Test
    void register() {
        EmailRegisterVO vo = new EmailRegisterVO();
        vo.setEmail("2024cbq@gmail.com");
        vo.setUsername("cbq");
        vo.setCode("TODO");
        vo.setPassword("cbq@cb.123");
        Assertions.assertNull(service.registerEmailAccount(vo));
    }

    @Test
    void resetConfirm() {
        ConfirmResetVO vo = new ConfirmResetVO();
        vo.setEmail("2024cbq@gmail.com");
        vo.setCode("TODO");
        Assertions.assertNull(service.resetConfirm(vo));
    }

    @Test
    void resetPassword() {
        EmailResetVO vo = new EmailResetVO();
        vo.setEmail("2024cbq@gmail.com");
        vo.setCode("826660");
        vo.setPassword("cb@cbq.456");
        Assertions.assertNull(service.resetEmailAccountPassword(vo));
    }
}
```



## 五、其它

### 5.1 项目依赖说明

> [!IMPORTANT]
>
> 项目使用 maven 进行打包构建和依赖管理

#### 5.1.1 starter 场景启动器

Spring Boot Starter 是 Spring Boot 的一部分，旨在简化新 Spring 应用的初始搭建以及开发过程。Starter 依赖是一种便捷方式，允许你在项目中包含一组依赖项，这些依赖项协同工作提供特定功能

1. **`spring-boot-starter-web`**:
   - 用于构建 Web 应用，包括 RESTful 应用。它使用 Spring MVC 作为底层框架来处理 web 请求
   - 默认包括 Tomcat 作为内嵌服务器，但也可以通过排除 Tomcat 依赖并添加其他服务器依赖来切换到其他服务器，如 Jetty 或Undertow
   - 提供了对静态资源、Web 或 Restful 应用的支持，以及错误处理
2. **`spring-boot-starter-mail`**:
   - 提供了发送电子邮件的功能。
   - 它封装了 Java Mail API，并简化了邮件发送过程中的配置
3. **`spring-boot-starter-validation`**:
   - 用于支持 Java Bean 验证 API（JSR-303 和 JSR-349）
   - 它允许你轻松地添加校验逻辑到你的应用中，通常是通过注解方式
   - 适用于对 Controller 层的请求参数或持久层的实体进行验证
4. **`spring-boot-starter-security`**:
   - 提供了 Spring Security 的支持，用于为你的应用添加安全保护
   - 它支持多种安全保护方式，包括基于表单的认证、OAuth2、方法级别的安全控制等
   - 默认配置提供了一套基本的安全设置，可以通过配置进行自定义和扩展
5. **`spring-boot-starter-data-redis`**:
   - 提供了对 Redis 的支持，包括应用与Redis数据库的交互。
   - 支持使用 Spring Data Redis 简化数据访问代码，以及通过 RedisConnectionFactory 进行 Redis 操作
   - 适用于实现缓存解决方案、消息队列等功能
6. **`spring-boot-starter-amqp`**:
   - 提供了高级消息队列协议（AMQP）的支持，主要与 RabbitMQ 一起使用
   - 通过简化配置和提供一些便捷的模板类，使得发送和接收消息变得更加容易
   - 适合于需要消息队列进行异步处理、解耦服务组件等场景


#### 5.1.2 三方依赖

1. **`mybatis-plus-boot-starter` 2.5.2.1**:
   - 这是一个用于集成 MyBatis Plus 到 Spring Boot 应用的Starter。MyBatis Plus 是 MyBatis 的增强工具，提供了更多的便捷特性，比如 CRUD 操作的简化、分页插件、乐观锁等
   - 它旨在简化 MyBatis 的使用和配置，同时提升开发效率
2. **`mysql-connector-j`**:
   - 这是 MySQL 数据库的官方 JDBC 驱动，用于 Java 应用与 MySQL 数据库之间的通信
   - 它支持所有 MySQL 版本的核心功能，包括 SSL 连接、性能增强等
   - 该驱动允许开发者在 Java 应用中执行 SQL 语句，进行数据查询和更新操作
3. **`springdoc-openapi-starter-webmvc-ui` 2.1.0**:
   - 这个 Starter 用于为 Spring Boot 应用集成 OpenAPI 3 规范的文档生成工具。SpringDoc OpenAPI 是一个库，用于自动生成和提供 Swagger UI 界面，以展示和测试 API
   - 它支持 WebMVC 项目，自动从你的 Spring Boot 应用的路由、控制器和模型中生成 API 文档
4. **`java-jwt` 4.2.0**:
   - 这是一个用于生成和验证 JSON Web Tokens (JWT) 的 Java 库。JWT 是一种开放标准（RFC 7519），用于安全地在两个体系间传递信息
   - 它通常用于实现无状态的认证机制，如在 RESTful API 中
5. **`fastjson2` 2.0.25**:
   - Fastjson 是一个高性能的 JSON 库，用于在 Java 对象和 JSON 格式数据之间进行转换
   - `fastjson2`是 Fastjson 的一个更新版本，提供了更好的性能、更多的特性和更高的安全性
6. **`lombok`**:
   - Lombok 是一个 Java 库，旨在通过注解的方式减少 Java 代码的冗余，特别是对于数据模型（如 POJOs）的简化
   - 它提供了一系列注解，如`@Data`、`@Getter`、`@Setter`等，自动为你的类生成 getter、setter、equals、hashCode 和 toString方法等
   - 使用Lombok可以显著减少样板代码，使代码更加简洁


#### 5.1.3 插件

1. `spring-boot-maven-plugin`

**`spring-boot-maven-plugin`** 是一个用于 Spring Boot 应用的 Maven 插件，它简化了 Spring Boot 应用的打包、运行和其他构建流程。这个插件提供了一系列功能，使得开发者能够轻松地管理和执行与 Spring Boot 应用相关的构建任务。下面是该插件的一些主要功能和特点：

1. **简化打包过程**：`spring-boot-maven-plugin`能够自动创建可执行的 jar 或 war 文件，这些文件包含了所有必要的依赖项，使得应用可以通过简单的`java -jar`命令运行。这种打包方式称为 "fat jar" 或 "uber jar"

2. **支持应用运行**：该插件提供了`spring-boot:run` 目标（goal），允许开发者直接从 Maven 命令行运行 Spring Boot 应用，而无需先打包。这对于开发和测试阶段非常有用，因为它可以加快迭代速度

3. **集成应用属性**：插件支持读取 `application.properties `或 `application.yml` 中定义的属性，并将它们应用到构建过程中。这意味着开发者可以在不同环境（如开发、测试、生产）中使用不同的配置，而无需改变代码

4. **简化依赖管理**：通过使用 Spring Boot 的依赖管理功能，`spring-boot-maven-plugin`能够确保应用使用的依赖版本之间相互兼容，减少了依赖冲突的可能性

5. **提供额外工具**：该插件还提供了一些额外的工具，如生成构建信息（包括版本号、构建时间等），这些信息可以在运行时用于显示或日志记录

6. **自定义打包行为**：开发者可以配置插件的行为，例如排除某些依赖项、添加额外的类路径资源等，以满足特定的打包需求

使用`spring-boot-maven-plugin`，开发者可以更加高效地构建和管理Spring Boot应用。它不仅简化了构建过程，还提供了灵活性和控制力，以适应不同的开发和部署需求

#### 5.1.4 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w2.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.2</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>jzxy.cbq</groupId>
    <artifactId>server</artifactId>
    <version>1.0.0</version>
    <name>monitor-server</name>
    <description>server</description>
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>2.5.2.1</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.1.0</version>
        </dependency>
        <dependency>
            <groupId>com.auth0</groupId>
            <artifactId>java-jwt</artifactId>
            <version>4.2.0</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba.fastjson2</groupId>
            <artifactId>fastjson2</artifactId>
            <version>2.0.25</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>annotationProcessor</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.amqp</groupId>
            <artifactId>spring-rabbit-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <finalName>monitor-server-v1.1.0</finalName>
                    <outputDirectory>../build</outputDirectory>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>

```