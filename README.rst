！！！请暂时不要下载和使用此分支代码，开发中！！！
----------------------------------------------------

！！！请注意，当前为DEV分支，可能无法正常运行！！！
----------------------------------------------------

！！！请注意，当前为DEV分支，此README文件与实际代码有偏差！！！
-----------------------------------------------------------------


RedTorch 
^^^^^^^^
2019-01
#######


.. image:: https://raw.githubusercontent.com/sun0x00/RedTorch-Pages/master/content/images/RedTorch20181230Snapshort.png
   :height: 992px
   :width: 1929px
   :scale: 50 %
   :alt: alternate text
   :align: center
   

简介
-----

项目是基于Java语言开发的开源量化交易程序开发框架。


框架起始完全移植自 `vn.py <http://www.vnpy.org/>`_ 代码,在这里首先向vn.py项目作者致谢；项目经过数次迭代，架构已有较大区别，如果Java语言经验不足，建议移步使用 `vn.py <http://www.vnpy.org/>`_，Python语言的学习成本要远低于Java。



使用Java的主要原因：


+ Python GIL带来的性能问题难以突破，不能有效使用多核CPU，利用Java能比较好的解决这一问题，在多账户多合约方面有一定便利。

+ 作为便捷的动态语言，Python在数据分析等领域有着天生的优势，但会在类型控制、重构方面会遇到一定的的障碍。

+ 曾考虑使用C++，但因工作量太大只能作罢，且已有大量此类开源工具。得益于JVM的良好设计，框架具备较好的扩展性和尚可接受的延迟。



重要提示
--------
+ 项目尚处于开发预览阶段，因此使用前请务必严格测试。

+ 欢迎star本项目，强烈建议watch，任何问题的最新修正都会第一时间在dev分支发布。

+ 还望不吝赐教随手指正，相关问题请发issues，在此先表示感谢。

+ 欢迎发起 Pull Request。

+ 永久免费开源，但请遵循协议。

主要特性
--------

+ Web监控界面

+ 支持CPU多核运行

+ 程序内部内部T2T低延迟(相比于C/C++语言要慢，比动态语言快很多)

+ 运行时异步线程存储数据，减少IO操作对延迟的影响

+ 策略代码，同时适用于回测和实盘

+ 支持分段回测、多合约回测、多线程回测

+ 策略实盘和回测均支持支持多账户、多合约

+ 多进程架构,MMAP通讯

项目结构
---------

+ 项目Java部分使用Gradle拆分、构建

  - **rt-core** 核心模块，包含了核心引擎，事件引擎，ZEUS交易引擎，ZEUS回测引擎，以及相关的通讯、数据服务。
  - **rt-api-jctp** Swig封装官方CTP的API模块，详见底部FAQ。
  - **rt-gateway-jctp** 适配rt-api-jctp的接口模块，实现了rt-core中的Gateway接口。
  - **rt-api-ib** 盈透证券（IB）官方提供的接口源码
  - **rt-gateway-ib** 适配rt-api-ib的接口模块，实现了rt-core中的Gateway接口。
  - **rt-front-web** Spring Boot承载的Web模块，暴露HTTP、SocketIO接口，提供Web监控页面。
  - **rt-common** 通用模块
  - **rt-strategy** 策略模块，提供了策略示例以及回测示例。
    
+ Web界面

  - 由React语言编写，采用  `Ant Design Pro <https://pro.ant.design/>`_ 框架
  - 采取用户名密码登陆的方式获取连接令牌
  - 数据交换采用HTTP被动获取和SocketIO主动推送两种方式结合
  - 由Spring Boot提供HTTP服务

+ Python接入
  
  - 使用预置令牌进行接入
  - 数据交换采用HTTP被动获取和SocketIO主动推送两种方式结合
  

项目已知问题
-----------------

+ 分段回测、多合约回测已经实现，各合约回测结果已经写入csv文件，但尚未进行结果汇总，回测绘图等功能正在开发。

+ 使用Python将行情数据导入MongoDB的模版已经完成，但尚未整理发布。

+ 部分linux不能直接使用编译后的动态链接库，访问 `RedTorch-Resources <https://github.com/sun0x00/RedTorch-Resources>`_ 自行下载编译
   

数据流程简介
-----------------

+ 框架采用事件驱动架构,且利用多核。

    - 项目已经弃用早期采用的观察者模式，不再使用阻塞队列（LinkedBlockingQueue）
    
    - 使用 `LMAX Disruptor <https://github.com/LMAX-Exchange/disruptor/>`_ 重新设计了高速事件引擎（FastEventEngineService），并加入性能调节配置
    
    - 请注意,性能仍然需要通过多核CPU体现



项目文档
-----------
还在写，文档没有deadline，文档deadline不可能有的，这辈子不可能有deadline。

先看一下这个 `概要视频(注意选择分辨率) <https://v.youku.com/v_show/id_XMzc1ODY5OTk2NA==.html?spm=a2h3j.8428770.3416059.1>`_ 吧。




预览环境准备
--------------------

+ 安装MongoDB

+ 安装vs2013x64运行库 、 vs2015x64运行库（Linux跳过）

+ 安装JDK11 x64并设置环境变量（JAVA_HOME,PATH必须），兼容Java 8,请自行修改Gradle文件修改版本

+ IDE推荐使用最新版Eclipse IDE for Java EE Developers x64

+ 使Git克隆本项目或直接下载zip，在Eclipse中使用File->Import->Existing Gradle Projects导入本项目

+ 修改application.properties文件

    - 配置端口。默认为9099（web）、9098（SocketIO）
    
+ 修改RtConfig.properties

    - 配置ClientDB请修修改rt-core.properties
    
    - 配置Web认证口令（默认test test）
    
    - 配置数据库(用户名密码等可选,行情和ClientDB可以使用同一个MongoDB实例)
    
    - 日志路径（默认D:\\log，不存在请创建）
    
    - ZEUS引擎缓存路径（module.zeus.backtesting.output.dir默认D:\\redtorch_zeus_backtesting_output，不存在请创建或修改配置）
  
+ 如果部署在linux中，需要使用临时目录/tmp/xyz/redtorch/api/jctp/lib(rpath目录)和用户临时目录

+ 如果部署在windows中，需要使用用户临时目录
    
+ 一切就绪后运行RtApplication,访问链接:http://IP:9099/static/html/index.html,一般是:http://localhost:9099/static/html/index.html

FAQ
------
+ 策略如何配置

   请访问 `概要视频(注意选择分辨率) <https://v.youku.com/v_show/id_XMzc1ODY5OTk2NA==.html?spm=a2h3j.8428770.3416059.1>`_ 


+ 如何运行回测（请等待简要文档发布）

   请访问 `概要视频(注意选择分辨率) <https://v.youku.com/v_show/id_XMzc1ODY5OTk2NA==.html?spm=a2h3j.8428770.3416059.1>`_ 

+ CTP封装源码在哪里

    访问 `RedTorch-Resources <https://github.com/sun0x00/RedTorch-Resources>`_

联系作者
--------------
sun0x00@gmail.com

QQ:1055532121

License
---------
MIT

用户在遵循本项目协议的同时，如果用户下载、安装、使用本项目中所提供的软件，软件作者对任何原因在使用本项目中提供的软件时可能对用户自己或他人造成的任何形式的损失和伤害不承担任何责任。作者有权根据有关法律、法规的变化修改本项目协议。修改后的协议会随附于本项目的新版本中。当发生有关争议时，以最新的协议文本为准。如果用户不同意改动的内容，用户可以自行删除本项目。如果用户继续使用本项目，则视为您接受本协议的变动。