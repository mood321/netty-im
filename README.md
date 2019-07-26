## Netty IM 练习项目
+ 实现用户的头像及信息修改
+ 添加好友 请求和接受
+ 好友消息的接受和发送
   ### serivce  后端java程序 
   + HTTP 请求由springboot 处理
   + WebSocket 请求有netty 处理
   + 数据库 mysql 库结构在resources/sql下
   + 文件服务器fastdfs 访问可由nginx模块代理服务 
   + 服务可发布war或springboot 由nginx代理
   ### web  客户端
   + 用HBuilder搭建的MUI 项目  可打包安卓和IOS 