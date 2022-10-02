```text
      _____ _     _
  __ /__   \ |__ (_)_ __   __ _
 / _` |/ /\/ '_ \| | '_ \ / _` |
| (_| / /  | | | | | | | | (_| |
 \__,_\/   |_| |_|_|_| |_|\__, |
                          |___/

Just a Thing
```

# 核心框架

![License](https://img.shields.io/badge/license-MIT-brightgreen)
![Language](https://img.shields.io/badge/language-java-brightgreen)

## 框架使用

### 添加仓库

```xml
<!-- pom.xml增加仓库 -->
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/athingx/athing</url>
    </repository>
</repositories>
```

### 构建客户端

```xml
<!-- pom.xml增加引用 -->
<dependency>
    <groupId>io.github.athingx.athing</groupId>
    <artifactId>athing-thing</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```java
// 创建设备客户端
final var thing = new ThingBuilder(new ThingPath(PRODUCT_ID,THING_ID))
        .clientFactory(new AliyunMqttClientFactory()
            .secret(SECRET)
            .remote(REMOTE)
        )
        .build();
```

### 构建服务端

```xml
<!-- pom.xml增加引用 -->
<dependency>
    <groupId>io.github.athingx.athing</groupId>
    <artifactId>athing-platform</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```java
// 创建设备服务端
final var platform = new ThingPlatformBuilder()
        .clientFactory(new AliyunIAcsClientFactory()
            .region("cn-shanghai")
            .identity(PLATFORM_IDENTITY)
            .secret(PLATFORM_SECRET)
        )
        .consumer(new AliyunThingMessageConsumerFactory()
            .queue(PLATFORM_JMS_GROUP)
            .connection(new AliyunJmsConnectionFactory()
                .queue(PLATFORM_JMS_GROUP)
                .remote(PLATFORM_REMOTE)
                .identity(PLATFORM_IDENTITY)
                .secret(PLATFORM_SECRET)
            )
            .listener(message -> {
                // 消费设备消息
            })
        )
        .build();
```
