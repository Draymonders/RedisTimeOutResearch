# RedisTimeOutResearch (doing)
redis time out 调研

# 打包并且打镜像
- `cd sturnus && mvn clean package -DskipTests=true`
- `docker build --build-arg JAR_FILE=sturnus-v0.1.jar -t sturnus .`
- 将`develop/docker-compose.yml`里的`REDIS_HOST`替换为您的服务器ip
- `docker-compose up -f develop/docker-compose.yml -d`

# 问题进展
线上用的redis，在压力大的会出现`RedisTimeOut`的异常，但我们的系统要求高可用 && 高性能嘛，就想着尽量避免这种情况，现在也做了本地cache，但是还是会有`RedisTimeOut`的问题存在。
  
分析了一个星期，没啥实质性进展，所以来问问各位大神。

- Spring版本: `2.1.7.RELEASE`
- Redis版本: `6.0.7`
- Java的redis-client: Lettuce


我自己又单独写了一个小project来测试。redis相关的配置如下
```
spring:
  redis:
    database: 0
    host: ${REDIS_HOST:127.0.0.1}
    port: ${REDIS_PORT:6379}
    lettuce:
      pool:
        max-active: 300
        max-wait: -1
        max-idle: 100
        min-idle: 10
    timeout: 1000
```
然后用的`docker-compose`进行的部署
```
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                     NAMES
3ed653397fd8        sturnus             "java -jar /usr/shar…"   3 hours ago         Up 3 hours                                    sturnus-redis-app
6e1e63a9eb34        redis:6.0.7         "docker-entrypoint.s…"   3 hours ago         Up 3 hours          0.0.0.0:12300->6379/tcp   redis
```

然后用到了`blade`模拟`redis`处理慢的问题

```
blade create docker network delay --time 930 --interface eth0 --local-port 6379 --container-id 6e1e63a9eb34
```

然后application侧打印的log如下
```
2020-09-04 20:43:17.101  WARN 1 --- [nio-8080-exec-3] o.s.d.r.c.l.LettuceConnectionFactory     : Validation of shared connection failed. Creating a new connection.
2020-09-04 20:43:17.101 ERROR 1 --- [nio-8080-exec-4] c.y.s.w.RedisController                  : add ts: 1599223394768 error, errMsg:
 
org.springframework.dao.QueryTimeoutException: Redis command timed out; nested exception is io.lettuce.core.RedisCommandTimeoutException: io.lettuce.core.RedisCommandTimeoutException: Command timed out after 1 second(s)
        at org.springframework.data.redis.connection.lettuce.LettuceExceptionConverter.convert(LettuceExceptionConverter.java:70) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.connection.lettuce.LettuceExceptionConverter.convert(LettuceExceptionConverter.java:41) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.PassThroughExceptionTranslationStrategy.translate(PassThroughExceptionTranslationStrategy.java:44) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.FallbackExceptionTranslationStrategy.translate(FallbackExceptionTranslationStrategy.java:42) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.connection.lettuce.LettuceConnection.convertLettuceAccessException(LettuceConnection.java:268) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.connection.lettuce.LettuceZSetCommands.convertLettuceAccessException(LettuceZSetCommands.java:903) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.connection.lettuce.LettuceZSetCommands.zAdd(LettuceZSetCommands.java:72) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.connection.DefaultedRedisConnection.zAdd(DefaultedRedisConnection.java:679) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.core.DefaultZSetOperations.lambda$add$0(DefaultZSetOperations.java:53) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.core.RedisTemplate.execute(RedisTemplate.java:224) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.core.RedisTemplate.execute(RedisTemplate.java:184) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.core.AbstractOperations.execute(AbstractOperations.java:95) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.core.DefaultZSetOperations.add(DefaultZSetOperations.java:53) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at org.springframework.data.redis.core.DefaultBoundZSetOperations.add(DefaultBoundZSetOperations.java:59) ~[spring-data-redis-2.1.10.RELEASE.jar!/:2.1.10.RELEASE]
        at com.yitu.sturnus.web.RedisController.addZSet(RedisController.java:80) [classes!/:?]
        at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[?:?]
        at jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[?:?]
        at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[?:?]
        at java.lang.reflect.Method.invoke(Method.java:566) ~[?:?]
        at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:190) [spring-web-5.1.9.RELEASE.jar!/:5.1.9.RELEASE]
        at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:138) [spring-web-5.1.9.RELEASE.jar!/:5.1.9.RELEASE]
        at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:104) [spring-webmvc-5.1.9.RELEASE.jar!/:5.1.9.RELEASE]
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:892) [spring-webmvc-5.1.9.RELEASE.jar!/:5.1.9.RELEASE]
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:797) [spring-webmvc-5.1.9.RELEASE.jar!/:5
```

整个链路是  java-redis-client发送请求 `->` 建立connection(从connectionPools取) `->` 网络传输 `->`  redis中命令排队 `->`  redis命令执行  `->`  网络传输 `->` java-redis-client拿到响应


现在可以通过`blade`工具排查应该不是client连接池的问题，所以接下来怎么排查呢，大佬们能给个思路嘛~
