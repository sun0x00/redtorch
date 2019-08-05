# redtorch

`但知行好事，莫要问前程。`

*在您下载和使用本项目前,请务必阅读相关协议和注意事项*

<img src="https://raw.githubusercontent.com/sun0x00/RedTorch-Pages/master/docs/images/redtorch_20190805024300.png" width="100%" align=center>



## 项目简介

项目是基于Java语言开发的开源量化交易程序开发框架。

框架起始完全移植自vn.py,在这里首先向项目作者致谢；经过数次迭代，架构已与vn.py完全不同，如果Java语言经验不足，或首次接触程序化交易，强烈建议使用[vn.py](https://www.vnpy.com/ "vn.py")，Python语言的学习成本要远低于Java，而且vn.py有良好的社区支持，更丰富的功能文档，便于初学者学习。

项目仓库地址：[ https://github.com/sun0x00/redtorch](https://github.com/sun0x00/redtorch " https://github.com/sun0x00/redtorch")

## 注意事项

+ 使用前请务必严格测试，欢迎Star本项目。
+ 还望不吝赐教随手指正，相关问题请发issues，在此先表示感谢。
+ 欢迎发起 Pull Request。
+ 部分情况下，从节点会在收盘时崩溃，此问题仍在查证中。
+ 从节点长时间可以长时间不重启。
+ 永久免费开源，但**请遵循协议**。

## 开发语言
Java

## 框架或类库
+ Spring Boot
+ [LMAX-Exchange Disruptor](https://github.com/LMAX-Exchange/disruptor/ "LMAX-Exchange Disruptor")

## 关联项目
+ [redtorch-resources](https://github.com/sun0x00/redtorch-resources "redtorch-resources") 封装相关
+ [redtorch-web-react](https://github.com/sun0x00/redtorch-web-react "redtorch-web-react") WEB前端
+ [redtorch-python-client](https://github.com/sun0x00/redtorch-python-client "redtorch-python-client") Python客户端

## 主要特性
+ 集成WEB界面，无需单独部署客户端
+ 分布式架构，具备良好的可伸缩性/可扩展性
+ 支持多用户统一管理，支持合约级别的交易权限管理和账户级别的读取控制,适合一般小型机构
+ 采用websocket+protobuf构建的双向RPC框架能够支持更多复杂的业务需求，同时提供更低的延迟
+ Disruptor多核事件引擎为数据流处理提供了有力的支持

###### 与旧版本的差别和优劣对比
- 移除socket.io,采用标准websocket协议暴露接口，更多异构语言接入
- 舍弃了MMAP的多进程通讯方式，通讯延迟上升较大，重构更简便
- 采用protobuf,进一步规范了数据结构，同时在一定程度上弥补了的通讯性能损失
- 前端舍弃Ant Design Pro，采用排版更佳致密、列表性能优化更好Microsoft Office UI Fabric
- Python客户端由Python 2.7升级到Python 3.7
- 理清了用户和操作员关系，支持了更细粒度的权限管理
- 计划单独创建项目redtorch-mini满足单账户高性能用户的需求（仅设想）
- ** Java策略引擎被暂时移除，经过优化的能够支持动态编译的策略引擎已经经过基础测试，正在加紧开发。**
- ** 合约标识，账户标识等采用`@`为分隔符替换`.`分隔符。**


## 配置注意事项
+ 主节点和从节点的配置文件均为application.properties
+ 超级管理员admin的默认密码只能通过配置文件修改，所配置的字符串是SHA-256加密结果，请注意使用正确方式替换
+ ** 超级管理员admin具有非常高的管理权限，请注意妥善保存密码 **
+ 非超级管理员用户存储在数据库中，密码同样采用SHA-156保存，一般创建后自动生成
+ 主节点和从节点都需要配置operatorId,用于权限识别
+ 日志文件存储路径默认在当前运行目录
+ 数据库用户名密码等可选，请根据实际情况处理,行情和配置数据库可以使用同一个MongoDB实例
+ 如果部署在linux中，需要使用临时目录/tmp/xyz/redtorch/下的相关目录
+ 如果部署在windows中，需要使用用户临时目录
+ ** 配置从节点时需要先在主节点中配置从节点的节点ID和认证信息，否则无法连接 **

## 预览环境准备

+ 安装MongoDB
+ 安装vs2013x64运行库 、 vs2015x64运行库（Linux跳过）
+ 安装JDK11 x64并设置环境变量（JAVA_HOME,PATH必须），如需兼容Java 8,请自行修改
+ IDE推荐使用最新版Eclipse IDE for Java EE Developers x64, (IntelliJ IDEA 和 Spring Boot存在兼容问题，请自行查询页面访问不到的解决方案)
+ 使Git克隆本项目或直接下载zip，在Eclipse中使用File->Import->Existing Gradle Projects导入本项目
+ 通过rt-node-master中的RtNodeMasterApplication.java启动主节点
+ 通过rt-node-slave中的RtNodeSlaveApplication.java启动从节点
+ 通过 http://IP:9099/ （一般是:http://localhost:9099/ ） 访问项目，默认超级管理员用户名`admin`，密码 `rt-admin`

## 打包部署

+ 可以将主节点和从节点分别打包为jar，并使用命令 `java -jar <文件名>.jar` 运行
+ 如果运行目录存在application.properties，会覆盖jar文件中的application.properties

## FAQ

+ 有没有群
	- >无
+ 是否考虑商业化支持
	- >不考虑
+ 是否支持OS X
	- >框架支持，但是接口底层API运行库几乎都不支持OS X，因此无法交易。如果仅运行主节点可以。
+ accountId是什么
	- >`账户代码@币种@网关ID`
+ unifiedSymbol是什么
	- >替换旧版中的rtSymbol，一般由 `品种代码日期@交易所代码@产品类型` 组成
+ 页面如何修改
	- >请先安装node.js,推荐使用vscode打开ReactSPA目录。
	- 推荐安装yarn并使用如下命令
	- 命令 `yarn start`，进入开发模式，端口默认 3000
	- 命令 `yarn build`，将会编译至build目录，编译完成后请手动复制到Java项目的静态资源目录中替换
	- npm命令亦可使用
+ 一个从节点是否支持同时接入多个主节点
	- >不支持
+ 同一操作系统中是否支持运行多个从节点
	- >支持，但不能在同一个目录，从节点例用PID文件识别重复启动，因此会冲突
+ ** 从节点无法启动，显示PID已经存在 **
	- >**如果确认当前目录的从节点未在运行状态，很有可能是pid文件过期，内部的记录值已经不在准确，可以手动删除此PID文件再次尝试启动**


## 使用协议（License）
MIT

**用户在遵循MIT协议的同时，如果用户下载、安装、使用本项目中所提供的软件，软件作者对任何原因在使用本项目中提供的软件时可能对用户自己或他人造成的任何形式的损失和伤害不承担任何责任。作者有权根据有关法律、法规的变化修改本项目协议。修改后的协议会随附于本项目的新版本中。当发生有关争议时，以最新的协议文本为准。如果用户不同意改动的内容，用户可以自行删除本项目。如果用户继续使用本项目，则视为您接受本协议的变动。**

## 友情提示

**市场莫测 风险自负**
**请务必充分理解各类相关风险**


