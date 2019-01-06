RedTorch 
----------

Version: 2019-01 Preview
==============================


.. image:: https://raw.githubusercontent.com/sun0x00/RedTorch-Pages/master/content/images/RedTorch20181230Snapshort.png
   :height: 992px
   :width: 1929px
   :scale: 50 %
   :alt: alternate text
   :align: center
   

简介
-----

项目是基于Java语言开发的开源量化交易程序开发框架。  

框架起始完全移植自vn.py,在这里首先向项目作者致谢；经过数次迭代，架构已与vn.py有较大区别，如果Java语言经验不足，建议移步使用 `vn.py <http://www.vnpy.org/>`_，Python语言的学习成本要远低于Java。  

使用Java的主要原因：

+ Python GIL带来的性能问题难以突破，不能有效使用多核CPU，利用Java能比较好的解决这一问题，在多账户多合约方面有一定便利。

+ 作为便捷的动态语言，Python在数据分析等领域有着天生的优势，但在数据类型控制、重构方面会遇到一定的的障碍。

+ 得益于JVM的良好设计，框架具备较好的扩展性和尚可接受的延迟，曾考虑使用C++，但因工作量太大只能作罢，且已有大量此类开源工具。。

重要提示
--------
+ 项目尚处于预览阶段，因此使用前请务必严格测试。

+ 欢迎Star本项目，强烈建议Watch，任何问题的最新修正都会第一时间在dev分支发布。

+ 还望不吝赐教随手指正，相关问题请发issues，在此先表示感谢。

+ 欢迎发起 Pull Request。

+ 据热心人士反馈，Linux下在盘后有崩溃的情况，此问题仍在查证中。

+ 永久免费开源，但请遵循协议。

主要特性
--------

+ Web监控界面

+ 支持CPU多核运行

+ 程序内部内部T2T低延迟(相比于C/C++语言要慢，比动态语言快很多)

+ 策略支持支持多账户、多合约

+ 策略代码，同时适用于回测和实盘

+ 策略运行时异步线程存储数据，减少IO操作对延迟的影响

+ 策略支持分段回测、多合约回测、多线程回测

+ 多进程架构,采用RMI和MMAP通讯

+ 支持异构接入，采用HTTP和WebSocket接入

结构简介
---------

+ Java组件。使用Gradle拆分、构建。

  - **rt-core** 核心模块。包含核心引擎，事件引擎，ZEUS交易引擎，ZEUS回测引擎，以及相关的通讯、数据服务。
  - **rt-api-jctp** Swig封装官方CTP的API模块。详见底部FAQ。
  - **rt-gateway-jctp** 适配rt-api-jctp的接口模块。实现了rt-core中的Gateway接口。
  - **rt-api-ib** 盈透证券（IB）官方提供的接口源码
  - **rt-gateway-ib** 适配rt-api-ib的接口模块。实现了rt-core中的Gateway接口。
  - **rt-front-web** 的Web模块。由Spring Boot实现，提供HTTP、SocketIO接口，承载Web监控页面。
  - **rt-common** 通用模块。一些常见的Java工具类
  - **rt-strategy** 策略模块。提供了策略示例以及回测示例。
    
+ Web SPA

  - 由React语言编写，采用  `Ant Design Pro <https://pro.ant.design/>`_ 框架
  - 采取用户名密码登陆的方式获取连接令牌
  - 数据交换采用HTTP被动获取和WebSocket主动推送两种方式结合

+ Python客户端

  - **rt-front-web-client-python** Python接入客户端
  - 使用预置令牌进行接入
  - 数据交换采用HTTP被动获取和WebSocket主动推送两种方式结合
  

项目已知问题
-----------------

+ 分段回测、多合约回测已经实现，各合约回测结果已经写入csv文件，但尚未进行结果汇总，回测绘图等功能正在开发。

+ 使用Python将行情数据导入MongoDB的模版已经完成，但尚未整理发布。

+ 部分linux不能直接使用编译后的动态链接库，访问 `RedTorch-Resources <https://github.com/sun0x00/RedTorch-Resources>`_ 自行下载编译
   

数据流程简介
-----------------
+ 接入
  
  - Web SPA
  
    + 用户通过浏览器获取到SPA登录页面
    + 用户请求登录并通过验证后，获取到令牌（Token），存入浏览器sessionStorage
    + 通过获取到的令牌发起HTTP请求
    + 通过获取到的令牌建立Web Socket连接

  - Python客户端
    
    + 通过预置令牌发起HTTP请求
    + 通过预置令牌建立Web Socket连接

+ 订阅行情

  - Web SPA、Python客户端或其它异构系统
    
    + 通过HTTP发起订阅请求,身份统一识别为 WEB_API ，并建立订阅关系
    + 由于未区分订阅身份，客户端A接入订阅的行情有可能被客户端B取消订阅关系
    + 订阅后接收为广播模式，客户端需要自行识别行情ID进行过滤

  - 策略
    
    + 策略首先策略引擎发起订阅，策略引擎通过MMAP进行进程间通讯
    + 通过策略引擎发起订阅，并根据策略ID进行身份区分，建立订阅关系
    + 策略被重新加载或策略进程心跳消失后，会根据ID取消订阅关系

+ 发单

  - Web SPA、Python客户端或其它异构系统
    
    + 在OrderReq中，以令牌作为OperatorID

  - 策略

    + 在OrderReq中，以策略ID作为OperatorID


+ 数据推送

  - 基础架构使用 `LMAX Disruptor <https://github.com/LMAX-Exchange/disruptor/>`_ 作为引擎推送事件，性能可根据实际硬件情况调节配置
  - Web SPA、Python客户端或其他异构系统通过WebSocket接收数据推送
  - 策略进程通过MMAP接收数据推送

项目文档
-----------
还在写，文档没有deadline，文档deadline不可能有的，这辈子不可能有deadline。


预览环境准备
--------------------

+ 安装MongoDB

+ 安装vs2013x64运行库 、 vs2015x64运行库（Linux跳过）

+ 安装JDK11 x64并设置环境变量（JAVA_HOME,PATH必须），兼容Java 8,请自行修改Gradle文件修改版本

+ IDE推荐使用最新版Eclipse IDE for Java EE Developers x64, (IntelliJ IDEA 和 Spring Boot存在兼容问题，请自行查询页面访问不到的解决方案)

+ 使Git克隆本项目或直接下载zip，在Eclipse中使用File->Import->Existing Gradle Projects导入本项目

+ 修改 **rt-front-web** application.properties文件


    -注意：务必 配置修改Web认证口令（默认test test）
    
    - 注意：务必配置修改预置接入令牌，此令牌具有很高的访问权限
    
+ 修改rt-core.properties
  
    -提示：数据库用户名密码等可选,行情和ClientDB可以使用同一个MongoDB实例
    
    - 日志路径（默认D:\\log，不存在请创建
    
    - ZEUS引擎缓存路径（module.zeus.backtesting.output.dir默认D:\\redtorch_zeus_backtesting_output，不存在请创建或修改配置）

    - 修改MMAP路径chronicleQueueBasePath，请注意，策略中也需要配置此路径 
  
+ 修改 **rt-strategy** application.properties文件
  
    - 配置策略ID，请注意，数据库中应存在此ID对应的配置记录，一个策略进程只允许一个策略，如有需要，可自行修改支持多策略，但不建议这么做。
  
  - 将Resource中的策略配置示例导入数据库中

  
+ 如果部署在linux中，需要使用临时目录/tmp/xyz/redtorch/api/jctp/lib(rpath目录)和用户临时目录

+ 如果部署在windows中，需要使用用户临时目录
    
+ 一切就绪后运行web项目中的RtApplication,访问链接:http://IP:9099/,一般是:http://localhost:9099/

+ 随后运行StrategyApplication，在web界面中可看到已经加载的绿色提示

FAQ
------

+ 有没有群

    木有，有个不错的QQ交流群，群号在此  MTAxNDQxODU1

+ 是否考虑商业化支持

    不考虑
    
+ 是否支持OS X

    框架支持，但是接口底层API运行库几乎都不支持OS X，因此无法交易

+ 策略配置中的RtAccountID是什么

    一般是 账户ID.币种.网关ID ，因此配置前请先确定相关ID
    
+ 为何不通过GatewayID下单

    常见接口都是一个网关实例对应一个账户，部分小众接口存在一个网关下存在多个子账户的情况，因此需要加以区分
 
    
+ 订阅也是通过RtAccountID区分吗
    
    不是，订阅是通过GatewayID，Web页面采用RtAccountID进行区分主要是为了方便展示


+ CTP封装源码在哪里

    访问 `RedTorch-Resources <https://github.com/sun0x00/RedTorch-Resources>`_

联系作者
--------------
sun0x00@gmail.com

QQ:1055532121

License（使用协议）
---------------------------
MIT

用户在遵循本项目协议的同时，如果用户下载、安装、使用本项目中所提供的软件，软件作者对任何原因在使用本项目中提供的软件时可能对用户自己或他人造成的任何形式的损失和伤害不承担任何责任。作者有权根据有关法律、法规的变化修改本项目协议。修改后的协议会随附于本项目的新版本中。当发生有关争议时，以最新的协议文本为准。如果用户不同意改动的内容，用户可以自行删除本项目。如果用户继续使用本项目，则视为您接受本协议的变动。