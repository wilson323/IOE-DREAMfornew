Nacos is starting, you can docker logs your container

         ,--.
       ,--.'|
   ,--,:  : |                                           Nacos 2.3.0
,`--.'`|  ' :                       ,---.               Running in cluster mode, All function modules
|   :  :  | |                      '   ,'\   .--.--.    Port: 8848
:   |   \ | :  ,--.--.     ,---.  /   /   | /  /    '   Pid: 1
|   : '  '; | /       \   /     \.   ; ,. :|  :  /`./   Console: http://172.17.0.2:8848/nacos/index.html
'   ' ;.    ;.--.  .-. | /    / ''   | |: :|  :  ;_
|   | | \   | \__\/: . ..    ' / '   | .; : \  \    `.      https://nacos.io
'   : |  ; .' ," .--.; |'   ; :__|   :    |  `----.   \
|   | '`--'  /  /  ,.  |'   | '.'|\   \  /  /  /`--'  /
'   : |     ;  :   .'   \   :    : `----'  '--'.     /
;   |.'     |  ,     .-./\   \  /            `--'---'
'---'        `--`---'     `----'

2025-12-07 16:37:51,898 INFO The server IP list of Nacos is []

2025-12-07 16:37:52,899 INFO Nacos is starting...

2025-12-07 16:37:53,899 INFO Nacos is starting...

2025-12-07 16:37:54,900 INFO Nacos is starting...

2025-12-07 16:37:55,900 INFO Nacos is starting...

2025-12-07 16:37:56,901 INFO Nacos is starting...

2025-12-07 16:37:57,901 INFO Nacos is starting...

2025-12-07 16:37:58,902 INFO Nacos is starting...

2025-12-07 16:37:59,902 INFO Nacos is starting...

2025-12-07 16:38:00,798 INFO Nacos Log files: /home/nacos/logs

2025-12-07 16:38:00,799 INFO Nacos Log files: /home/nacos/conf

2025-12-07 16:38:00,799 INFO Nacos Log files: /home/nacos/data

2025-12-07 16:38:00,799 ERROR Startup errors : 

org.springframework.context.ApplicationContextException: Unable to start web server; nested exception is org.springframework.boot.web.server.WebServerException: Unable to start embedded Tomcat
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.onRefresh(ServletWebServerApplicationContext.java:165)
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:577)
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:147)
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:731)
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:408)
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:307)
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1303)
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1292)
	at com.alibaba.nacos.Nacos.main(Nacos.java:48)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.springframework.boot.loader.MainMethodRunner.run(MainMethodRunner.java:49)
	at org.springframework.boot.loader.Launcher.launch(Launcher.java:108)
	at org.springframework.boot.loader.Launcher.launch(Launcher.java:58)
	at org.springframework.boot.loader.PropertiesLauncher.main(PropertiesLauncher.java:467)
Caused by: org.springframework.boot.web.server.WebServerException: Unable to start embedded Tomcat
	at org.springframework.boot.web.embedded.tomcat.TomcatWebServer.initialize(TomcatWebServer.java:142)
	at org.springframework.boot.web.embedded.tomcat.TomcatWebServer.<init>(TomcatWebServer.java:104)
	at org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory.getTomcatWebServer(TomcatServletWebServerFactory.java:481)
	at org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory.getWebServer(TomcatServletWebServerFactory.java:211)
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.createWebServer(ServletWebServerApplicationContext.java:184)
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.onRefresh(ServletWebServerApplicationContext.java:162)
	... 16 common frames omitted
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'basicAuthenticationFilter' defined in class path resource [com/alibaba/nacos/prometheus/filter/PrometheusAuthFilter.class]: Unsatisfied dependency expressed through method 'basicAuthenticationFilter' parameter 0; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'nacosAuthConfig' defined in URL [jar:file:/home/nacos/target/nacos-server.jar!/BOOT-INF/lib/default-auth-plugin-2.3.0.jar!/com/alibaba/nacos/plugin/auth/impl/NacosAuthConfig.class]: Unsatisfied dependency expressed through constructor parameter 3; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'nacosUserDetailsServiceImpl': Unsatisfied dependency expressed through field 'userPersistService'; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'externalUserPersistServiceImpl': Invocation of init method failed; nested exception is java.lang.RuntimeException: java.lang.RuntimeException: [db-load-error]load jdbc.properties error
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:800)
	at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:541)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.instantiateUsingFactoryMethod(AbstractAutowireCapableBeanFactory.java:1352)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1195)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:582)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:213)
	at org.springframework.boot.web.servlet.ServletContextInitializerBeans.getOrderedBeansOfType(ServletContextInitializerBeans.java:213)
	at org.springframework.boot.web.servlet.ServletContextInitializerBeans.getOrderedBeansOfType(ServletContextInitializerBeans.java:204)
	at org.springframework.boot.web.servlet.ServletContextInitializerBeans.addServletContextInitializerBeans(ServletContextInitializerBeans.java:98)
	at org.springframework.boot.web.servlet.ServletContextInitializerBeans.<init>(ServletContextInitializerBeans.java:86)
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.getServletContextInitializerBeans(ServletWebServerApplicationContext.java:262)
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.selfInitialize(ServletWebServerApplicationContext.java:236)
	at org.springframework.boot.web.embedded.tomcat.TomcatStarter.onStartup(TomcatStarter.java:53)
	at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:4940)
	at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:183)
	at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1328)
	at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1318)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at org.apache.tomcat.util.threads.InlineExecutorService.execute(InlineExecutorService.java:75)
	at java.util.concurrent.AbstractExecutorService.submit(AbstractExecutorService.java:134)
	at org.apache.catalina.core.ContainerBase.startInternal(ContainerBase.java:866)
	at org.apache.catalina.core.StandardHost.startInternal(StandardHost.java:795)
	at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:183)
	at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1328)
	at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1318)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at org.apache.tomcat.util.threads.InlineExecutorService.execute(InlineExecutorService.java:75)
	at java.util.concurrent.AbstractExecutorService.submit(AbstractExecutorService.java:134)
	at org.apache.catalina.core.ContainerBase.startInternal(ContainerBase.java:866)
	at org.apache.catalina.core.StandardEngine.startInternal(StandardEngine.java:249)
	at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:183)
	at org.apache.catalina.core.StandardService.startInternal(StandardService.java:428)
	at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:183)
	at org.apache.catalina.core.StandardServer.startInternal(StandardServer.java:922)
	at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:183)
	at org.apache.catalina.startup.Tomcat.start(Tomcat.java:486)
	at org.springframework.boot.web.embedded.tomcat.TomcatWebServer.initialize(TomcatWebServer.java:123)
	... 21 common frames omitted
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'nacosAuthConfig' defined in URL [jar:file:/home/nacos/target/nacos-server.jar!/BOOT-INF/lib/default-auth-plugin-2.3.0.jar!/com/alibaba/nacos/plugin/auth/impl/NacosAuthConfig.class]: Unsatisfied dependency expressed through constructor parameter 3; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'nacosUserDetailsServiceImpl': Unsatisfied dependency expressed through field 'userPersistService'; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'externalUserPersistServiceImpl': Invocation of init method failed; nested exception is java.lang.RuntimeException: java.lang.RuntimeException: [db-load-error]load jdbc.properties error
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:800)
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:229)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1372)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1222)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:582)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208)
	at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:410)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.instantiateUsingFactoryMethod(AbstractAutowireCapableBeanFactory.java:1352)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1195)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:582)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208)
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1391)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1311)
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:887)
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791)
	... 61 common frames omitted
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'nacosUserDetailsServiceImpl': Unsatisfied dependency expressed through field 'userPersistService'; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'externalUserPersistServiceImpl': Invocation of init method failed; nested exception is java.lang.RuntimeException: java.lang.RuntimeException: [db-load-error]load jdbc.properties error
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.resolveFieldValue(AutowiredAnnotationBeanPostProcessor.java:662)
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.inject(AutowiredAnnotationBeanPostProcessor.java:642)
	at org.springframework.beans.factory.annotation.InjectionMetadata.inject(InjectionMetadata.java:119)
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.postProcessProperties(AutowiredAnnotationBeanPostProcessor.java:399)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.populateBean(AbstractAutowireCapableBeanFactory.java:1431)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:619)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208)
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1391)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1311)
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:887)
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791)
	... 84 common frames omitted
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'externalUserPersistServiceImpl': Invocation of init method failed; nested exception is java.lang.RuntimeException: java.lang.RuntimeException: [db-load-error]load jdbc.properties error
	at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.postProcessBeforeInitialization(InitDestroyAnnotationBeanPostProcessor.java:160)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.applyBeanPostProcessorsBeforeInitialization(AbstractAutowireCapableBeanFactory.java:440)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1796)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:620)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208)
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1391)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1311)
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.resolveFieldValue(AutowiredAnnotationBeanPostProcessor.java:659)
	... 99 common frames omitted
Caused by: java.lang.RuntimeException: java.lang.RuntimeException: [db-load-error]load jdbc.properties error
	at com.alibaba.nacos.persistence.datasource.DynamicDataSource.getDataSource(DynamicDataSource.java:60)
	at com.alibaba.nacos.plugin.auth.impl.persistence.ExternalUserPersistServiceImpl.init(ExternalUserPersistServiceImpl.java:55)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor$LifecycleElement.invoke(InitDestroyAnnotationBeanPostProcessor.java:389)
	at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor$LifecycleMetadata.invokeInitMethods(InitDestroyAnnotationBeanPostProcessor.java:333)
	at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.postProcessBeforeInitialization(InitDestroyAnnotationBeanPostProcessor.java:157)
	... 111 common frames omitted
Caused by: java.lang.RuntimeException: [db-load-error]load jdbc.properties error
	at com.alibaba.nacos.persistence.datasource.ExternalDataSourceServiceImpl.init(ExternalDataSourceServiceImpl.java:118)
	at com.alibaba.nacos.persistence.datasource.DynamicDataSource.getDataSource(DynamicDataSource.java:55)
	... 119 common frames omitted
Caused by: java.io.IOException: java.lang.RuntimeException: java.sql.SQLNonTransientConnectionException: Could not create connection to database server. Attempted reconnect 3 times. Giving up.
	at com.alibaba.nacos.persistence.datasource.ExternalDataSourceServiceImpl.reload(ExternalDataSourceServiceImpl.java:167)
	at com.alibaba.nacos.persistence.datasource.ExternalDataSourceServiceImpl.init(ExternalDataSourceServiceImpl.java:115)
	... 120 common frames omitted
Caused by: java.lang.RuntimeException: java.sql.SQLNonTransientConnectionException: Could not create connection to database server. Attempted reconnect 3 times. Giving up.
	at com.alibaba.nacos.persistence.utils.ConnectionCheckUtil.checkDataSourceConnection(ConnectionCheckUtil.java:42)
	at com.alibaba.nacos.persistence.datasource.ExternalDataSourceServiceImpl.lambda$reload$0(ExternalDataSourceServiceImpl.java:137)
	at com.alibaba.nacos.persistence.datasource.ExternalDataSourceProperties.build(ExternalDataSourceProperties.java:97)
	at com.alibaba.nacos.persistence.datasource.ExternalDataSourceServiceImpl.reload(ExternalDataSourceServiceImpl.java:135)
	... 121 common frames omitted
Caused by: java.sql.SQLNonTransientConnectionException: Could not create connection to database server. Attempted reconnect 3 times. Giving up.
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:110)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:97)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:89)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:63)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:73)
	at com.mysql.cj.jdbc.ConnectionImpl.connectWithRetries(ConnectionImpl.java:899)
	at com.mysql.cj.jdbc.ConnectionImpl.createNewIO(ConnectionImpl.java:824)
	at com.mysql.cj.jdbc.ConnectionImpl.<init>(ConnectionImpl.java:449)
	at com.mysql.cj.jdbc.ConnectionImpl.getInstance(ConnectionImpl.java:242)
	at com.mysql.cj.jdbc.NonRegisteringDriver.connect(NonRegisteringDriver.java:198)
	at com.zaxxer.hikari.util.DriverDataSource.getConnection(DriverDataSource.java:138)
	at com.zaxxer.hikari.pool.PoolBase.newConnection(PoolBase.java:354)
	at com.zaxxer.hikari.pool.PoolBase.newPoolEntry(PoolBase.java:202)
	at com.zaxxer.hikari.pool.HikariPool.createPoolEntry(HikariPool.java:473)
	at com.zaxxer.hikari.pool.HikariPool.checkFailFast(HikariPool.java:554)
	at com.zaxxer.hikari.pool.HikariPool.<init>(HikariPool.java:115)
	at com.zaxxer.hikari.HikariDataSource.getConnection(HikariDataSource.java:112)
	at com.alibaba.nacos.persistence.utils.ConnectionCheckUtil.checkDataSourceConnection(ConnectionCheckUtil.java:40)
	... 124 common frames omitted
Caused by: com.mysql.cj.exceptions.CJCommunicationsException: Communications link failure

The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.
	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
	at com.mysql.cj.exceptions.ExceptionFactory.createException(ExceptionFactory.java:61)
	at com.mysql.cj.exceptions.ExceptionFactory.createException(ExceptionFactory.java:105)
	at com.mysql.cj.exceptions.ExceptionFactory.createException(ExceptionFactory.java:151)
	at com.mysql.cj.exceptions.ExceptionFactory.createCommunicationsException(ExceptionFactory.java:167)
	at com.mysql.cj.protocol.a.NativeSocketConnection.connect(NativeSocketConnection.java:89)
	at com.mysql.cj.NativeSession.connect(NativeSession.java:120)
	at com.mysql.cj.jdbc.ConnectionImpl.connectWithRetries(ConnectionImpl.java:843)
	... 136 common frames omitted
Caused by: java.net.UnknownHostException: ${MYSQL_SERVICE_HOST}
	at java.net.InetAddress$CachedAddresses.get(InetAddress.java:764)
	at java.net.InetAddress.getAllByName0(InetAddress.java:1291)
	at java.net.InetAddress.getAllByName(InetAddress.java:1144)
	at java.net.InetAddress.getAllByName(InetAddress.java:1065)
	at com.mysql.cj.protocol.StandardSocketFactory.connect(StandardSocketFactory.java:133)
	at com.mysql.cj.protocol.a.NativeSocketConnection.connect(NativeSocketConnection.java:63)
	... 138 common frames omitted
2025-12-07 16:38:00,803 WARN [WatchFileCenter] start close

2025-12-07 16:38:00,803 WARN [WatchFileCenter] start to shutdown this watcher which is watch : /home/nacos/conf

2025-12-07 16:38:00,804 WARN [WatchFileCenter] already closed

2025-12-07 16:38:00,804 WARN [NotifyCenter] Start destroying Publisher

2025-12-07 16:38:00,804 WARN [NotifyCenter] Destruction of the end

2025-12-07 16:38:00,804 ERROR Nacos failed to start, please see /home/nacos/logs/nacos.log for more details.

2025-12-07 16:38:00,840 WARN [ThreadPoolManager] Start destroying ThreadPool

2025-12-07 16:38:00,840 WARN [ThreadPoolManager] Destruction of the end

