CombinedMq Spring
========================
[![Build Status](https://travis-ci.com/combinedmq/combinedmq-spring.svg?branch=master)](https://travis-ci.com/combinedmq/combinedmq-spring)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.combinedmq/combinedmq-spring.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.combinedmq%22%20AND%20a:%22combinedmq-spring%22)

[English](https://github.com/combinedmq/combinedmq-spring/blob/v1.0.x/README.md)

CombinedMq Spring可以帮助您将CombinedMq集成到Spring中，通过spring schema配置的方式，能够非常方便的完成集成工作。

### 概述

schema配置主要有三个元素：
1. queue - 定义一个队列，并关联一个接口
2. producer - 队列接口的代理，用于发送消息
3. consumer - 引用一个队列接口的实现，用于接收消息
## 使用步骤
### Step 1: Maven依赖
```xml
<dependency>
  <groupId>com.github.combinedmq</groupId>
  <artifactId>combinedmq-spring</artifactId>
  <version>1.0.1</version>
</dependency>
```
### Step 2: 创建一个接口
该接口的所有方法返回类型都只能是void类型:
```java
public interface GreetingService {
    void sayHi(String name);
}
```
### Step 3: Spring Schema配置 - producer
在applicationContext.xml中加入CombinedMq的schema内容
```
<?xml version="1.0" encoding="UTF-8"?>
<beans ...
       xmlns:combinedmq="http://www.github.com/schema/combinedmq"

       xsi:schemaLocation="...
		http://www.github.com/schema/combinedmq
        http://www.github.com/schema/combinedmq/combinedmq-spring.xsd">

    <combinedmq:queue id="greetingServiceQueue" name="x.y.z" type="point_to_point"
                      interface="com.github.combinedmq.spring.service.GreetingService"/>

    <combinedmq:producer id="greetingServiceProducer" queue-ref="greetingServiceQueue"/>

</beans>
```
可以通过@Autowired注解得到一个producer代理:
```java
@Service
public class ProducerTest {
    @Autowired
    private GreetingService greetingService;

    public void sayHi(String name) {
        greetingService.sayHi(name);
    }
}
```
### Step 4: Spring Schema配置 - producer
在applicationContext.xml中加入CombinedMq的schema内容

```
<?xml version="1.0" encoding="UTF-8"?>
<beans ...
       xmlns:combinedmq="http://www.github.com/schema/combinedmq"

       xsi:schemaLocation="...
		http://www.github.com/schema/combinedmq
        http://www.github.com/schema/combinedmq/combinedmq-spring.xsd">

    <bean id="greetingService" class="com.github.combinedmq.spring.service.GreetingServiceImpl"/>

    <combinedmq:queue id="greetingServiceQueue" name="x.y.z" type="point_to_point"
                      interface="com.github.combinedmq.spring.service.GreetingService"/>
    <combinedmq:consumer queue-ref="greetingServiceQueue" implement-ref="greetingService"/>

</beans>
```
### Step 5: 连接相关的配置文件 - combinedmq.yml
在资源根目录下添加combinedmq.yml文件，如果您使用rabbitmq，可以在yml文件中加入下面内容：
 
```yaml
rabbitmq:
  host: 10.1.7.22
  port: 5672
  username: xiaoyu
  password: xiaoyu
  virtualHost: /
  consumerListener:
    concurrency: 5 #消费者数量
  producerPool: #连接池配置，连接池只是针对生产者有效
    maxTotal: 100
    maxIdle: 20
    minIdle: 10
    maxWaitMillis: 30000
    minEvictableIdleTimeMillis: 60000
    timeBetweenEvictionRunsMillis: 30000
    testOnBorrow: false
    testOnReturn: false
    testWhileIdle: true
#consumerListener和producerPool可以不同时存在
```
activemq、kafka等其他配置可以查看此[配置文件](https://github.com/combinedmq/combinedmq/blob/master/src/test/resources/combinedmq.yml)。
（在集成到spring中时，combinedmq.yml文件中只能使用rabbitmq、activemq、kafka其中一种）