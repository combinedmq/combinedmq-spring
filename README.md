CombinedMq Spring
========================
[![Build Status](https://travis-ci.com/combinedmq/combinedmq-spring.svg?branch=master)](https://travis-ci.com/combinedmq/combinedmq-spring)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.combinedmq/combinedmq-spring.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.combinedmq%22%20AND%20a:%22combinedmq-spring%22)

[中文版文档](https://github.com/combinedmq/combinedmq-spring/blob/master/README_zh.md)

CombinedMq Spring can help you integrate CombinedMq into Spring, and it is very convenient to complete the integration work through the spring schema configuration.
### Overview

The schema configuration has three main elements:
1. queue - Define a queue and associate an interface
2. producer - Queue interface proxy for sending messages
3. consumer - An implementation that references a queue interface for receiving messages
## Steps for usage
### Step 1: Maven dependency
```xml
<dependency>
  <groupId>com.github.combinedmq</groupId>
  <artifactId>combinedmq-spring</artifactId>
  <version>1.0.1</version>
</dependency>
```
### Step 2: Create an interface
All method return types of this interface can only be void type:
```java
public interface GreetingService {
    void sayHi(String name);
}
```
### Step 3: Spring Schema configuration - producer
Add the schema content of CombinedMq in applicationContext.xml
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
A producer proxy can be obtained by annotating @Autowired:
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
### Step 4: Spring Schema configuration - producer
Add the schema content of CombinedMq in applicationContext.xml
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
### Step 5: Connection related configuration file - combinedmq.yml
Add the combinedmq.yml file in the resource root directory. If you use rabbitmq, you can add the following to the yml file: 
```yaml
rabbitmq:
  host: 10.1.7.22
  port: 5672
  username: xiaoyu
  password: xiaoyu
  virtualHost: /
  consumerListener:
    concurrency: 5 #Number of consumers
  producerPool: #Connection pool configuration, connection pool is only valid for producers
    maxTotal: 100
    maxIdle: 20
    minIdle: 10
    maxWaitMillis: 30000
    minEvictableIdleTimeMillis: 60000
    timeBetweenEvictionRunsMillis: 30000
    testOnBorrow: false
    testOnReturn: false
    testWhileIdle: true
#The consumer listener and producer pool can exist at different times
```
activemq, kafka and other configurations can view this[configuration file](https://github.com/combinedmq/combinedmq/blob/master/src/test/resources/combinedmq.yml)。
（When integrated into spring, only one of rabbitmq, activemq, and kafka can be used in the combinedmq.yml file.）