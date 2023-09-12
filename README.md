### char-httpclient

[![Build](https://github.com/CharLemAznable/char-httpclient/actions/workflows/build.yml/badge.svg)](https://github.com/CharLemAznable/char-httpclient/actions/workflows/build.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.charlemaznable/char-httpclient/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.charlemaznable/char-httpclient/)
[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)
![GitHub code size](https://img.shields.io/github/languages/code-size/CharLemAznable/char-httpclient)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-httpclient&metric=alert_status)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-httpclient)

[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-httpclient&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-httpclient)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-httpclient&metric=bugs)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-httpclient)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-httpclient&metric=security_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-httpclient)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-httpclient&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-httpclient)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-httpclient&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-httpclient)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-httpclient&metric=sqale_index)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-httpclient)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-httpclient&metric=code_smells)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-httpclient)

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-httpclient&metric=ncloc)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-httpclient)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-httpclient&metric=coverage)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-httpclient)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_char-httpclient&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=CharLemAznable_char-httpclient)

封装http客户端, 使用okhttp/vertx-web-client/webflux-webclient.

##### Maven Dependency

```xml
<dependency>
  <groupId>com.github.charlemaznable</groupId>
  <artifactId>char-httpclient</artifactId>
  <version>2023.2.1</version>
</dependency>
```

##### Maven Dependency SNAPSHOT

```xml
<dependency>
  <groupId>com.github.charlemaznable</groupId>
  <artifactId>char-httpclient</artifactId>
  <version>2023.2.2-SNAPSHOT</version>
</dependency>
```

#### 快速开始

##### 简易的http客户端

```java
String getResp = new OhReq("http://host:port/path").get();

String postResp = new OhReq("http://host:port/path").post();

Future<String> getFuture = new OhReq("http://host:port/path").getFuture();

Future<String> postFuture = new OhReq("http://host:port/path").postFuture();
```

```OhReq```对象可设置http请求的部分属性, 如
* 请求路径
* 字符集
* 请求体格式
* 请求头
* url参数
* 请求体
* OkHttpClient.Builder配置

```java
new VxReq("http://host:port/path").get(asyncResult -> {
});

new VxReq("http://host:port/path").post(asyncResult -> {
});

Future<String> getFuture = new VxReq("http://host:port/path").get();

Future<String> postFuture = new VxReq("http://host:port/path").post();
```

```VxReq```对象可设置http请求的部分属性, 如
* 请求路径
* 字符集
* 请求体格式
* 请求头
* url参数
* 请求体
* WebClientOptions配置

```java
Mono<String> getMono = new WfReq("http://host:port/path").get();

Mono<String> postMono = new WfReq("http://host:port/path").post();

Future<String> getFuture = new WfReq("http://host:port/path").getFuture();

Future<String> postFuture = new WfReq("http://host:port/path").postFuture();
```

```WfReq```对象可设置http请求的部分属性, 如
* 请求路径
* 字符集
* 请求体格式
* 请求头
* url参数
* 请求体
* WebClient.Builder配置

简易的http客户端可用于大部分简单的业务场景.

##### 使用注解标识接口, 自动装配http客户端

```java
@OhClient
@Mapping("http://host:port")
public interface MyHttpClient {
    @Mapping("/path") // 可省略, 默认使用: "/methodName"
    String path();
}

MyHttpClient client = OhFactory.getClient(MyHttpClient.class);
String getResp = client.path();
```

```java
@VxClient
@Mapping("http://host:port")
public interface MyHttpClient {
    @Mapping("/path") // 可省略, 默认使用: "/methodName"
    Future<String> path();
}

MyHttpClient client = VxFactory.getClient(MyHttpClient.class);
client.path().onSuccess(getResp -> {
    
});
```

```java
@WfClient
@Mapping("http://host:port")
public interface MyHttpClient {
    @Mapping("/path") // 可省略, 默认使用: "/methodName"
    Mono<String> path();
}

MyHttpClient client = WfFactory.getClient(MyHttpClient.class);
client.path().subscribe(getResp -> {
    
});
```

定义接口, 添加注解配置http客户端, 调用工厂创建客户端实例, 即可发起http请求.

#### 配置注解

##### ```@OhClient```/```@VxClient```/```@WfClient```

标识作用的注解, 接口必须添加此注解, 才能通过工厂方法创建对应的客户端实例.

##### ```@Mapping```

配置http请求路径, 可添加在接口或方法上. 支持配置多个路径, 用于客户端负载均衡.

http客户端方法的请求路径为接口```@Mapping```的value值与方法```@Mapping```的value值直接连接的字符串.

如果方法未添加```@Mapping```注解, 则默认value值为```"/{methodName}"```.

如果配置了多个路径, 则请求路径集合为接口```@Mapping```的value值与方法```@Mapping```的value值的笛卡尔积连接字符串.

即:
```java
@OhClient
@Mapping({"http://host1", "http://host2"})
public interface MyHttpClient {
    @Mapping({"/path1", "/path2"})
    String req();
}

// 请求路径集合为:
// 1. http://host1/path1
// 1. http://host1/path2
// 1. http://host2/path1
// 1. http://host2/path2
```

##### ```@MappingMethodNameDisabled```

在接口上添加此注解, 则:

如果方法未添加```@Mapping```注解, 则默认value值为```""```.

##### ```@AcceptCharset```

指定字符集, 默认值为: UTF-8.

##### ```@ContentFormat```

指定请求的Content-Type和请求体的序列化方式, 默认值为: "application/x-www-form-urlencoded", url表单格式.

##### ```@RequestMethod```

指定请求方法, 默认值为: GET.

##### ```@FixedHeader``` ```@FixedHeaders```

指定预定义的请求头.

##### ```@FixedPathVar``` ```@FixedPathVars```

指定预定义的路径参数, 路径参数使用```{pathvar}```格式定义在请求路径中.

##### ```@FixedParameter``` ```@FixedParameters```

指定预定义的请求参数, 发起GET/HEAD请求时请求参数添加在UrlQuery中, 发起其他请求时则按配置统一序列化为请求体.

##### ```@FixedContext``` ```@FixedContexts```

指定预定义的上下文参数, 可在请求处理过程中使用.

##### ```@Header```

添加在方法参数上, 指定调用请求方法时添加的请求头.

##### ```@PathVar```

添加在方法参数上, 指定调用请求方法时添加的路径参数.

##### ```@Parameter```

添加在方法参数上, 指定调用请求方法时添加的请求参数.

##### ```@Context```

添加在方法参数上, 指定调用请求方法时添加的上下文参数.

##### ```@RequestBodyRaw```

添加在方法参数上, 指定请求体内容全文, 参数类型必须为```String```.

##### ```@Bundle```

添加在方法参数上, 当参数为JavaBean时, 解析其所有字段加入请求, 字段注解支持以上五种, 默认识别为```@Parameter```.

##### ```@StatusFallback``` ```@StatusFallbacks```

指定按请求响应的HTTP状态码调用的异常处理方法.

##### ```@StatusSeriesFallback``` ```@StatusSeriesFallbacks```

指定按请求响应的HTTP状态码集合调用的异常处理方法.

##### ```@DefaultFallbackDisabled```

默认的HTTP状态码异常处理方法为: 4xx或5xx响应将抛出```StatusError```异常.

在接口上添加此注解, 则停用此默认处理方法.

##### ```@RequestExtend``` ```@RequestExtend.Disabled```

指定自定义的请求前置处理方法, 特殊处理请求的```headers, pathvars, parameters, contexts```.

##### ```@ResponseParse``` ```@ResponseParse.Disabled```

指定自定义的响应内容解析方法.

##### ```@ExtraUrlQuery``` ```@ExtraUrlQuery.Disabled```

指定请求的UrlQuery内容的特殊处理, 例如可在POST请求路径后添加UrlQuery.

##### ```@MappingBalance```

指定客户端负载均衡方式, 默认为随机选择, 可使用内置的RoundRobin轮询, 也可自定义负载均衡方式.

##### ```ResilienceBulkhead``` ```ResilienceRateLimiter``` ```ResilienceCircuitBreaker``` ```ResilienceRetry```

使用resilience4j支持的容错机制, 装饰顺序为: Retry ( CircuitBreaker ( RateLimiter ( Bulkhead ( Function ) ) ) ) .

#### 响应解析

默认按照请求方法的返回值类型解析响应内容:
1. Integer: 返回HTTP状态码
2. HttpStatus / HttpStatus.Series
3. Boolean: HTTP状态码是否为2xx
4. okhttp: ResponseBody / InputStream / BufferedSource / byte[] / Reader / String: 响应体原文
5. vertx-web-client: Buffer / byte[] / JsonObject / JsonArray / String: 响应体原文
6. webflux-webclient: byte[] / String: 响应体原文
7. JavaBean: 优先按配置的```@ResponseParse```解析, 否则尝试解析xml/json
8. List / Map: 将JavaBean映射为List/Map
9. Pair / Triple: 支持同时返回多种格式的响应解析结果, 例如状态码和JavaBean
10. Future / CompletionStage: 支持异步获取响应
11. 特殊支持: 方法参数类型实现```CncRequest```接口后, 指定返回类型实现```CncResponse```接口, 则可支持泛型请求对应泛型响应.

#### 支持环境变量

注解value值支持环境变量, 使用```${key}```格式标识.

环境变量源为类路径中的```char-httpclient.env.props```配置文件和```Arguments```启动参数.

其中```Arguments```变量的优先级高于配置文件.

#### 支持[westcache](https://github.com/bingoohuang/westcache)

可添加westcache注解, 支持请求结果的缓存.

#### 在Spring中使用

使用```@OhScan```/```@VxScan```/```@WfScan```指定扫描加载包路径.

包路径中所有添加```@OhClient```/```@VxClient```/```@WfClient```注解的接口都将生成对应的http客户端实例并注入SpringContext.

#### 在Guice中使用

使用```OhModular```/```VxModuler```/```WfModuler```按类或包路径扫描加载.

创建的```Module```中将包含对应的http客户端实例.

#### 支持WSDL

使用```@WsOhClient```/```@WsOhClient12```/```@WsVxClient```/```@WsVxClient12```/```@WsWfClient```/```@WsWfClient12```注解, 支持WSDL.

具体使用方法, 请参考测试用例.
