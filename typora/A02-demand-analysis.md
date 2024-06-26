## V 1.2.0

> [!TIP]
>
> - 撰稿人：曹蓓
> - 日期：2024.5.12 10.30
> - 主题：[添加 登录、注册 功能及 dev INFO 相关的文档](https://github.com/caobaoqi1029/monitor/issues/7) 需求分析部分

## 一、需求分析

### 1.1 需求概述

#### 1.1.1 目的

本文档旨在详细阐述服务器监控系统的开发需求，包括系统的功能需求和非功能需求。通过对这些需求的明确，指导开发团队进行规范、安全、严谨的设计和开发，确保软件项目的工程化管理和顺利实施

#### 1.1.2 范围

本需求分析文档覆盖服务器监控系统的全部预期功能及性能指标，包括用户管理、服务器数据实时收集与监控、远程管理以及数据可视化等核心功能。同时，将对系统的可用性、可靠性、性能和安全性等非功能性需求进行详细描述

#### 1.1.3 业务背景

在Linux、计算机网络以及软件工程课程的学习中，我们深入理解了 Linux 操作系统和计算机网络的核心概念，并对阿里云提供的远程连接服务产生了浓厚的兴趣。我们认识到，尽管 Linux 命令行终端强大，但在服务器管理方面，图形化界面和实时数据展示能极大地提升用户体验。因此，我们决定开发一个服务器监控系统，以实现简易的服务器管理和数据可视化功能

#### 1.1.4 目标

本项目旨在开发一个服务器监控系统，该系统能够为用户提供一个直观友好的界面，用于实时监控和管理服务器的运行状态。项目的目标不仅包括实现基本的用户登录注册、服务器数据的实时收集与监控、远程管理等功能，还要通过数据可视化技术使得服务器状态一目了然，提高服务器管理的效率和便捷性

### 1.2 功能需求

#### 1.2.1 用户管理功能

#### 概述

用户管理功能是服务器监控系统的核心组成部分，旨在提供安全、便捷的用户访问和个人信息管理体验。通过这一功能模块，用户可以进行注册、登录、密码修改和找回，以及查看个人信息等操作。

#### 详细设计

1. **注册**
   - 用户通过提供邮箱地址、用户名和密码进行注册。
   - 系统对密码进行加密处理后存储，确保用户信息的安全。
   - 注册过程中，系统发送一封验证邮件到用户提供的邮箱地址，用户需按照相关信息进行邮箱验证，以激活账户。
2. **登录**
   - 用户可以使用注册的用户名或邮箱和密码登录系统。
   - 登录时提供“记住我”选项，若用户勾选，系统将在本地存储加密的登录凭证，以实现用户再次访问时无需登录。
3. **记住我**
   - 实现记住我功能，通过在客户端存储加密的用户身份标识，实现用户长时间内无需重复登录。
   - 出于安全考虑，记住我的有效期应设定为一定时间范围，例如30天。
4. **退出**
   - 用户在完成操作后可以选择退出登录，系统将清除所有相关的会话信息和本地存储的登录凭证，确保账户安全。
5. **修改密码**
   - 用户在登录状态下，可以通过提供旧密码和设置新密码的方式来修改密码。
   - 修改密码时，系统应进行实时的密码强度检测，引导用户设置安全性更高的密码。
6. **找回密码**
   - 用户在忘记密码时，可以通过注册邮箱启动密码找回流程。
   - 系统通过发送包含密码重置链接的邮件到用户的注册邮箱，用户点击链接后跳转到密码重置页面，设置新密码后完成密码找回。
7. **个人中心页**
   - 用户登录后，可以访问个人中心页，查看和编辑个人信息，如邮箱、用户名、头像等。
   - 个人中心页还应提供密码修改和账户注销等功能。

#### 安全性考虑

- 所有用户信息的传输均应通过 SSL 加密，防止数据在传输过程中被截获
- 密码在服务器端存储前需进行哈希处理，且应使用盐值增加破解难度
- 对于“记住我”功能，存储在本地的凭证应进行加密处理，并定期更新加密策略，以防止长期使用同一加密方法可能带来的安全风险。

#### 1.2.2 服务器数据实时收集与监控

#### 概述

服务器数据实时收集与监控功能旨在实时跟踪服务器的状态和性能，包括CPU使用率、内存使用、磁盘I/O、网络流量等关键指标。通过对这些数据的实时监控，管理员可以快速发现并解决服务器可能遇到的问题。

#### 详细设计

1. **数据收集**
   - 系统需要部署一个轻量级的监控代理在目标服务器上。该代理负责定期收集服务器的性能指标数据。
   - 收集的数据包括但不限于CPU使用率、内存使用量、磁盘读写速率、网络带宽使用情况等。
2. **数据传输**
   - 收集到的数据通过安全的通道（如TLS加密）发送回监控系统。
   - 为减少网络负载，数据在发送前应进行压缩处理。
3. **实时展示**
   - 系统将收集到的数据实时展示在用户界面上，以图表或仪表盘的形式呈现。
   - 用户可以自定义监控仪表盘，选择关注的性能指标。
4. **警报机制**
   - 用户可以为关键性能指标设置阈值。当数据超过阈值时，系统将触发警报，通过邮件或短信通知管理员。
   - 支持警报历史记录查询和管理。

#### 安全性考虑

- 监控代理与服务器监控系统之间的通信需要加密，确保数据传输的安全性。
- 监控代理的部署和更新应通过安全的手段进行，防止恶意代码的植入。

#### 1.2.3 远程管理

#### 概述

远程管理功能允许管理员远程执行命令或脚本，对服务器进行管理和维护。这包括但不限于重启服务、更新软件、修改配置文件等操作。

#### 详细设计

1. **命令执行**
   - 管理员可以通过监控系统的界面输入需要执行的命令或脚本。
   - 系统将命令发送到目标服务器上的监控代理，由代理执行命令并返回执行结果。
2. **任务调度**
   - 支持定时执行任务的功能，管理员可以设定在特定时间执行特定的命令或脚本。
   - 支持周期性任务调度，如每天、每周或每月执行的任务。

#### 安全性考虑

- 执行命令时，系统应限制可执行命令的范围，防止潜在的安全风险。
- 系统应记录所有执行的命令和结果，以供审计和故障排查。

#### 1.2.4 数据可视化

#### 概述

数据可视化功能将服务器的监控数据以图形化的方式展示，帮助管理员直观了解服务器的状态和性能。这包括时间序列图、饼图、柱状图等多种展示方式。

#### 详细设计

1. **图表展示**
   - 根据收集的监控数据，系统生成各种图表，如CPU和内存使用率的时间序列图，磁盘和网络使用的饼图等。
   - 用户可以根据需要选择不同的数据指标和图表类型进行展示。
2. **历史数据分析**
   - 系统存储历史监控数据，支持历史数据的查询和分析。
   - 用户可以选择时间范围，回顾过去一段时间内服务器的性能变化。

#### 安全性考虑

- 确保存储和展示的数据不包含敏感信息，如密码、个人信息等。
- 对于访问历史数据的功能，应实施访问控制，确保只有授权用户才能访问。

### 1.3 非功能需求

在设计服务器监控系统时，除了功能需求之外，非功能需求也是非常关键的一部分。这些需求确保系统的安全性、性能、可靠性以及维护和拓展性，是系统长期运行和升级的基础。

#### 1.3.1 系统安全性

- **数据加密**：所有通过网络传输的数据，包括用户认证信息、监控数据等，都应使用强加密协议（如TLS）进行加密，防止数据泄露或被篡改。
- **用户认证和授权**：系统应实现强认证机制，如两因素认证，确保只有授权用户才能访问系统。此外，应实现细粒度的访问控制，根据用户角色限制对特定功能和数据的访问。
- **日志和审计**：系统应记录详细的操作日志，包括用户登录、执行命令、修改配置等操作，以便于事后审计和问题排查。
- **定期安全审查**：系统应定期进行安全审查和漏洞扫描，及时发现并修复安全漏洞。

#### 1.3.2 性能需求

- **实时性**：监控数据的收集和展示应尽可能实时，确保管理员可以及时发现并响应问题。
- **高并发处理**：系统应能够处理高并发请求，保证在多用户同时使用时仍能保持良好的性能。
- **数据处理能力**：系统应具备高效的数据处理能力，能够快速处理和分析大量监控数据

#### 1.3.3 可靠性需求

- **高可用性**：系统应设计为高可用架构，通过技术如负载均衡、故障转移等确保系统的持续运行。
- **数据备份和恢复**：系统应定期备份关键数据，并能在数据丢失或损坏后快速恢复。
- **故障监测和自动恢复**：系统应能够自动监测到故障并尽可能自动恢复，减少人工干预

#### 1.3.4 维护和拓展性需求

- **模块化设计**：系统应采用模块化设计，各个功能模块之间耦合度低，便于单独维护和升级。
- **文档和API**：提供完整的系统文档和API文档，方便开发者和管理员理解和使用系统。
- **拓展性**：系统设计应考虑未来拓展，如新增监控指标、集成第三方服务等，应能够方便地进行拓展

### 1.4 验收标准

为确保服务器监控系统满足既定的设计目标和用户需求，制定详细的验收标准是必要的。这些标准将用于评估系统的功能性和非功能性需求是否得到满足。

#### 1.4.1 功能需求验收标准

1. **服务器数据实时收集与监控**
   - 系统能够无缝监控CPU使用率、内存使用、磁盘I/O、网络流量等关键性能指标。
   - 监控数据的更新频率不低于每分钟一次，以确保数据的实时性。
   - 系统提供的数据需要有高度的准确性，误差范围不超过5%。
2. **远程管理**
   - 系统能够支持远程执行命令或脚本，包括但不限于重启服务、更新软件、修改配置文件等操作。
   - 命令执行结果需要在用户界面上清晰展示，包括执行状态和任何输出信息。
   - 系统需要记录所有远程执行的命令和结果，供审计和故障排查使用。
3. **数据可视化**
   - 系统能够提供包括时间序列图、饼图、柱状图等多种数据可视化形式。
   - 用户可以自定义图表，选择不同的数据指标和图表类型进行展示。
   - 系统支持历史数据的查询和分析，允许用户选择时间范围进行数据回顾。

#### 1.4.2 非功能需求验收标准

1. **系统安全性**
   - 系统通过安全测试，包括但不限于渗透测试、漏洞扫描。
   - 所有网络传输的数据必须使用TLS或等效加密协议。
   - 系统实现的用户认证和授权机制能够防止未授权访问。
2. **性能需求**
   - 系统能够在高并发条件下正常运行，支持的并发用户数至少达到设计指标的95%。
   - 监控数据的收集和展示的延迟时间不超过5秒。
3. **可靠性需求**
   - 系统的年平均故障间隔时间（MTBF）不低于1000小时。
   - 数据备份和恢复流程能够在数据丢失或损坏后迅速恢复，数据恢复时间不超过4小时。
4. **维护和拓展性需求**
   - 系统的模块化设计允许独立升级或替换单个模块而不影响整体系统。
   - 系统提供的API文档完整，能够支持第三方开发者进行扩展开发。

