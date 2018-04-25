RedTorch
^^^^^^^^

简介
-----

项目是基于Java语言开发的开源量化交易程序开发框架，主要架构思想来自开源项目 `vn.py <http://www.vnpy.org/>`_，其中大约有70%的逻辑是直接移植过来的，因此在这里首先向vn.py项目作者致谢。

本框架需要有Java语言使用经验，因此Java编程经验少于两年的朋友请审慎使用，建议移步 `vn.py <http://www.vnpy.org/>`_，Python语言的学习成本要远低于Java。

使用Java重构的原因主要有以下几点：

+ Python GIL带来的性能问题难以突破，不能有效使用多核CPU。对于绝大部分策略可能对实盘T2T延迟并没有太高的要求，使用vn.py便捷可靠，但当策略回测时需要使用Tick级别或多合约回测，控制性能问题带来的时间成本就显得格外重要。

+ 作为便捷的动态语言，原生Python没有编译期检查，但这是一把双刃剑。在决定将vn.py移植到Java之前，本人亦使用Python交易，但某日由于Python计算结果中出现NaN但不被解释器判断为异常，导致未及时止损造成巨大亏损的严重后果。虽然Java也有令人诟病的 NullPointerException，但还是可以暴露一些潜在的问题。

+ 曾考虑使用C++，但因工作量太大只能作罢，而且已有此类工具。得益于JVM的良好设计，框架在多核利用和内部T2T延迟方面表现良好，绝大部分时候内部T2T延迟控制在250μs~500μs，而vn.py创始前辈对vn.py早期的T2T测试结果大概24ms,这只是早期数据，现在应该更快了。


重要提示
--------
+ 项目尚处于开发预览阶段，测试覆盖率不足30%，因此使用前请务必严格测试。

+ 强烈建议watch本项目，任何问题的最新修正都会在第一时间发布。

+ 预计第一个Relase版本将在5月下旬发布。

+ 从决定移植到20180422本项目第一次发布，大概用了20个工作日，因只有一人在做，且时间仓促，难免有错误之处，还望不吝赐教随手指正，相关问题请发issues，在此先表示感谢。

+ 永久免费开源，但请遵循协议。

主要特性
--------

+ Web界面管理

+ 支持CPU多核运行

+ 支持分段回测，多合约回测

+ 异步数据存储，避开IO延迟

+ 内部T2T低延迟(比纯C++实现还是要慢，但比动态语言快很多)

+ 一套策略代码，同时适用于回测和实盘

+ 支持策略多账户（审慎使用，严禁违规操作）

项目结构
---------

+ 项目使用Maven构建

+ 框架采用事件驱动架构,使用观察者模式(Observer Pattern)构建事件引擎,且利用多核。

    - 每个事件观察者（Observer或者监听者Listener，例如策略线程、SocketIO数据线推送线程程等都属于这个范畴，下文统一用观察者代指）都运行在独立的线程中,项目中绝大部分事件引擎推送的事件都被存入了观察者各自的事件队列中，这些队列大部分都采用了阻塞队列（LinkedBlockingQueue）， 每个观察者各自的线程通过阻塞take方法实现事件处理。 因此彻底解决了Python GIL带来的性能问题。 
    
    - P.s.性能问题解决前提是需要运行在多核CPU环境中。采用阻塞队列并不会比轮询稍慢，因take方法的阻塞使用的是操作系统中的condition wait，因此当新数据进入队列后，被取出处理的延迟是极低的，在框架使用的场景中，此方法可以很大程度的节省CPU。

+ Web界面部分采用SPA架构，使用VUE编写，数据交换采用HTTP被动获取和SocketIO主动推送两种方式结合。Web的承载核心框架为Spring Boot，但是主引擎（MainEngine）、ZEUS策略引擎（ZeusEngine）、行情/交易接口实现（Gateway）等交易相关的都不依赖Spring Boot。因此Web部分可轻松剥离，不过值得一提的是，在多线程下，Web部分对交易部分的性能影响完全可以忽略不计，而且提供了一个应急操纵的交互接口。

+ 事件引擎（EventEngine）是整个交易框架的核心之一，是其他引擎、接口之间的事件媒介。

+ 数据引擎（DataEngine）管理与数据库之间的连接，目前区分了行情数据库和默认库，为满足多人使用同一个行情数据库提供了基础，也可以通过修改配置ClientID兼容同一个数据库实例下使用多个平台实例。

+ 主引擎（MainEngine）负责管理数据引擎(DataEngine), 接口（Gateway）,模块（Module），对外暴露管理API，依赖事件引擎

+ 接口（Gateway）一般是指 带有事件推送的行情/交易接口，目前本项目仅实现了上期技术的CTP接口。如果有其他需要，可以参考CTP接口加入其他行情交易接口，未来可能优先考虑加入飞创接口。

  P.s. CTP接口采用Swig封装，已作为单独项目发布，详见底部FAQ。由于JNI对底层字符编码转换不可逆转，因此在编译时对CTP的用到的相关中文接口进行了批量转码处理。

+ 模块（Module）依赖主引擎，间接依赖事件引擎,数据引擎。ZEUS策略引擎（ZeusEngine）也是一个模块（Module）。ZEUS引擎的接口有两个实现，分别为实盘和回测。回测不需要启动SpringBoot，仅需要Java main方法直接启动。


项目文档
-----------
还在写，文档没有deadline，文档deadline不可能有的，这辈子不可能有deadline。不过第一个Relase版本发布时会提供一个简要的文档，后续会组织补充。


项目已知问题
-----------------

+ 分段回测、多合约回测已经实现，各合约回测结果已经写入csv文件，但尚未进行结果汇总，回测绘图等功能正在开发，第一个Relase版本前将会提供。

+ 使用Python将行情数据导入MongoDB的模版已经完成，但尚未整理发布。

+ 部分linux不能直接使用编译后的动态链接库，访问 `RedTorch-Resources <https://github.com/sun0x00/RedTorch-Resources>`_ 自行下载编译

预览环境准备
--------------------

+ 安装MongoDB

+ 安装vs2013x64运行库 、 vs2015x64运行库（Linux跳过）

+ 安装JDK8+x64并设置环境变量（JAVA_HOME,PATH必须），最低要求JDK8，JDK9 JDK10尚未测试

+ IDE推荐使用最新版Eclipse IDE for Java EE Developers x64

+ 安装Maven3.x(可选，也可以使用Eclipse嵌入式)

+ 使Git克隆本项目或直接下载zip，在Eclipse中使用File->Import->Existing Maven Projects导入本项目

+ 修改application.properties文件

    - 配置端口。默认为9099（web）、9098（SocketIO）
    
+ 修改RtConfig.properties

    - 配置ClientID(如果多人共享使用ClientDB)
    
    - 配置Web认证口令（默认test test）
    
    - 配置数据库(用户名密码等可选,行情和ClientDB可以使用同一个MongoDB实例)
    
    - 日志路径（默认D:\\log，不存在请创建）
    
    - ZEUS引擎缓存路径（module.zeus.backtesting.output.dir默认D:\\redtorch_zeus_backtesting_output，不存在请创建）
  
+ 如果部署在linux中，需要使用临时目录/tmp/xyz/redtorch/api/jctp/lib(rpath目录)和用户临时目录

+ 如果部署在windows中，需要使用用户临时目录
    
+ 一切就绪后运行ZeusApplication,访问链接:http://IP:9099/static/html/index.html,一般是:http://localhost:9099/static/html/index.html

FAQ
------
+ 策略如何配置

   如果没有对目录进行特殊配置，请寻找ZeusStartegyConfig文件夹，对应的<StrategyClassName>-setting.json文件，配置文件和策略的相关说明请等待文档发布


+ 如何运行回测（请等待简要文档发布）


+ CTP封装源码在哪里

    访问 `RedTorch-Resources <https://github.com/sun0x00/RedTorch-Resources>`_

联系作者
--------------
sun0x00@gmail.com

License
---------
MIT

用户在遵循本项目协议的同时，如果用户下载、安装、使用本项目中所提供的软件，软件作者对任何原因在使用本项目中提供的软件时可能对用户自己或他人造成的任何形式的损失和伤害不承担任何责任。作者有权根据有关法律、法规的变化修改本项目协议。修改后的协议会随附于本项目的新版本中。当发生有关争议时，以最新的协议文本为准。如果用户不同意改动的内容，用户可以自行删除本项目。如果用户继续使用本项目，则视为您接受本协议的变动。