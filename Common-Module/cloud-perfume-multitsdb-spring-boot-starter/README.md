# cloud-perfume-multitsdb-spring-boot-starter

用于给其他 Spring Boot module 提供统一的多时序库读写能力。

当前支持：

- IoTDB
- TDEngine

starter 会自动装配 `MultiTSDBTemplate`，业务侧直接注入这个工具类即可，不需要通过 HTTP 接口转一层。

## 提供的能力

- 按时序库类型写入数据
- 按时序库类型查询数据
- 按时序库类型按时间范围查询
- 按时序库类型直接执行 SQL
- 自动选择默认时序库类型
- 自动选择默认数据库
- `timestamp` 缺省时自动补当前时间
- IoTDB 批量写入时自动优先走 `Tablet`
- IoTDB 树模型下会把 `tags` 当普通字段一起写入，尽量与 TDEngine 保持一致

## Maven 引入

版本号由根 `pom.xml` 中的 `multitsdb.starter.version` 统一控制。

例如：

```xml
<properties>
    <multitsdb.starter.version>1.0.0</multitsdb.starter.version>
</properties>
```

如果要发 `1.0.1`，只需要改成：

```xml
<properties>
    <multitsdb.starter.version>1.0.1</multitsdb.starter.version>
</properties>
```

```xml
<dependency>
    <groupId>com.alandevise</groupId>
    <artifactId>cloud-perfume-multitsdb-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

安装到本地 Maven 仓库：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home \
/Applications/IntelliJ\ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn \
-pl Common-Module/cloud-perfume-multitsdb-spring-boot-starter \
-am -DskipTests install
```

安装完成后，本地仓库里的坐标会变成：

```text
com.alandevise:cloud-perfume-multitsdb-spring-boot-starter:1.0.0
```

## 配置方式

```yaml
tsdb:
  default-type: iotdb
  default-database: cloud_platform
  iotdb:
    enabled: true
    host: 127.0.0.1
    port: 6667
    username: root
    password: root
    use-table-model: false
  tdengine:
    enabled: true
    host: 127.0.0.1
    port: 6041
    username: root
    password: taosdata
```

配置说明：

- `tsdb.default-type`: 默认时序库类型。未配置时默认 `iotdb`
- `tsdb.default-database`: 默认数据库。未配置时默认 `cloud_platform`
- `tsdb.iotdb.use-table-model`: `false` 为树模型，`true` 为表模型

## 使用方式

### 1. 注入工具类

```java
@Autowired
private MultiTSDBTemplate multiTSDBTemplate;
```

### 2. 写入数据

```java
TimeSeriesData data = new TimeSeriesData();
data.setDevice("device_001");
data.setMeasurement("env_metrics");
data.setFields(Map.of("value", 23.7, "status", "normal"));
data.setTags(Map.of("location", "line_1", "sensor_type", "combo_sensor"));

multiTSDBTemplate.write("iotdb", "cloud_platform", data);
```

### 3. 批量写入

```java
List<TimeSeriesData> dataList = new ArrayList<>();
dataList.add(data1);
dataList.add(data2);

multiTSDBTemplate.batchWrite("iotdb", "cloud_platform", dataList);
```

### 4. 查询

显式指定 `tsdbType` 和 `database`：

```java
QueryResult result = multiTSDBTemplate.query("tdengine", "cloud_platform", "SELECT * FROM env_metrics LIMIT 10");
```

省略 `tsdbType`，使用配置中的 `tsdb.default-type`：

```java
QueryResult result = multiTSDBTemplate.query("cloud_platform", "SELECT * FROM env_metrics LIMIT 10");
```

同时省略 `tsdbType` 和 `database`：

```java
QueryResult result = multiTSDBTemplate.query(null, "SELECT * FROM env_metrics LIMIT 10");
```

说明：

- 第一种写法：显式使用 `tdengine` + `cloud_platform`
- 第二种写法：`tsdbType` 省略，`database` 显式传入
- 第三种写法：`tsdbType` 和 `database` 都省略
- 当 `database` 省略时，会回退到 `tsdb.default-database`
- 当 `tsdbType` 省略时，会回退到 `tsdb.default-type`

### 5. 按时间范围查询

```java
QueryResult result = multiTSDBTemplate.queryByTimeRange(
        "iotdb",
        "cloud_platform",
        "device_001",
        0L,
        System.currentTimeMillis(),
        100
);
```

### 6. 执行 SQL

显式指定 `tsdbType` 和 `database`：

```java
QueryResult result = multiTSDBTemplate.executeSql("tdengine", "cloud_platform", "SHOW DATABASES");
```

省略 `tsdbType`，使用配置中的 `tsdb.default-type`：

```java
QueryResult result = multiTSDBTemplate.executeSql("cloud_platform", "SHOW DATABASES");
```

同时省略 `tsdbType` 和 `database`：

```java
QueryResult result = multiTSDBTemplate.executeSql(null, "SHOW DATABASES");
```

说明：

- `query`、`executeSql`、`queryByTimeRange`、`write`、`batchWrite` 都支持省略 `tsdbType`
- `query`、`executeSql`、`queryByTimeRange`、`write`、`batchWrite` 也都支持省略 `database`
- 省略逻辑统一走 starter 内部的兜底规则

## 参数要求

### `TimeSeriesData`

字段说明：

- `device`: 必填
- `measurement`: 必填
- `fields`: 建议必填，至少应提供一组业务字段
- `timestamp`: 选填
- `tags`: 选填

### 不必填字段的兜底逻辑

`timestamp`：

- 如果显式传入，就使用调用方传入值
- 如果没有传入，starter 自动使用 `System.currentTimeMillis()`

`database`：

- 如果显式传入，就使用调用方传入值
- 如果没有传入，就使用配置文件中的 `tsdb.default-database`
- 如果配置文件也没有配置，就默认使用 `cloud_platform`

`tsdbType`：

- 如果显式传入，就使用调用方传入值
- 如果没有传入，就使用配置文件中的 `tsdb.default-type`
- 如果配置文件也没有配置，就默认使用 `iotdb`

`tags`：

- 不传没有问题，写入仍然可以成功
- 如果传了：
  - 在 TDEngine 中会作为普通列写入
  - 在 IoTDB 树模型中也会作为普通字段写入
  - 在 IoTDB 表模型中会按表模型语义处理

## IoTDB 批量写入优化

当满足以下条件时，starter 会自动优先走 `Tablet`：

- 当前使用的是 IoTDB
- 当前使用的是树模型
- 批量数据中存在同设备、同 schema 的分组

特点：

- 调用方不需要显式选择 `Tablet`
- starter 会自动判断是否适合
- 如果 `Tablet` 不适用或执行失败，会自动回退到通用写入逻辑

示例：

```java
List<TimeSeriesData> batch = new ArrayList<>();

for (int i = 0; i < 3; i++) {
    TimeSeriesData data = new TimeSeriesData();
    data.setDevice("tablet_device_001");
    data.setMeasurement("env_metrics");
    data.setFields(Map.of(
            "value", 20.0 + i,
            "status", "normal"
    ));
    data.setTags(Map.of(
            "location", "line_1",
            "sensor_type", "combo_sensor"
    ));
    batch.add(data);
}

multiTSDBTemplate.batchWrite("iotdb", "cloud_platform", batch);
```

这段代码里：

- 调用方只是正常调用 `batchWrite`
- 没有任何 `Tablet` 相关代码
- starter 会发现这是 IoTDB、树模型、同设备、同 schema 的批量数据
- 然后自动优先走 `Tablet`

## 表不存在时的行为

### IoTDB

- starter 会确保默认数据库存在
- 树模型下具体测点写入依赖 IoTDB 自身的 schema 自动创建能力

### TDEngine

- starter 会主动确保数据库存在
- starter 会主动检查表是否存在
- 表不存在时会自动建表
- 列不存在时会自动补列

## 建议

- 如果是业务模块直接接入，优先使用 `MultiTSDBTemplate`
- 如果你希望两边字段结构尽量一致，建议把共用维度放在 `tags` 中，同时使用相同的 `fields` 键名
