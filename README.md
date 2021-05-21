# c-mall-only

#### 介绍
独立开发版本

#### 软件架构
1.  项目的结构
    a. mall-parent 没有代码，仅仅只是对我们所使用的三方的依赖的版本，做了一个统一的管理
	b. mall-commons 里面主要放的都是，多个模块可能需要共用的一些工具
	c. comment-service, shopping-service, order-service, user-service
	   1) xxx-api 对外暴露的公共接口，以及实体类等，只放需要对外暴露的公共的东西
	   2) xxx-provider 才是xxx服务真正的提供者
	   
	d. gateway  充当我们的api 网关的角色，接收请求，并通过远程调用，调用远程服务的功能完成请求
	
-----------------------------------------------------------------------------------------------------
2.  详细的讲解

    a. comment-service：没有代码，不需要实现(不需要实现)
	   1) xxx-api 主要放，外部所要用到的状态描述枚举类，服务暴露的接口，需要暴露的实体类
       2) xxx-provider: 单独启动
           a. 启动类
           b. 实现api中的service	
           c. dal
                实体类
                对应的xml 的mapper文件（持久化相关）
				
		   d. converter DO -> DTO
		   
		   
    b. gateway: api 网关
	   1) 有启动类，可以单独启动
	   2) 只有gateway运行在tomcat容器中，需要依赖spring-boot-starter-web
	   3) gateway中的controller，都是服务的消费者
	   4) gateway中不用 引入xxx-provider
	   
	c. user-service
	   user-sdk:
	   1) 自定义注解 Anonymous
	   2) TokenInterceptor：登录身份认证的，基于Token的
	   

         				
#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
